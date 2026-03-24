package com.conference.meeting.service.impl;

import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.meeting.config.FileUploadProperties;
import com.conference.meeting.dto.*;
import com.conference.meeting.entity.Schedule;
import com.conference.meeting.entity.ScheduleAttachment;
import com.conference.meeting.mapper.ScheduleAttachmentMapper;
import com.conference.meeting.mapper.ScheduleMapper;
import com.conference.meeting.service.IChunkUploadService;
import com.conference.meeting.service.IScheduleAttachmentService;
import com.conference.meeting.util.FileCompressionUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 断点续传服务实现类
 */
@Slf4j
@Service
@AllArgsConstructor
public class ChunkUploadServiceImpl implements IChunkUploadService {

    private final FileUploadProperties uploadProperties;
    private final ScheduleAttachmentMapper scheduleAttachmentMapper;
    private final ScheduleMapper scheduleMapper;
    private final IScheduleAttachmentService scheduleAttachmentService;
    private final FileCompressionUtil fileCompressionUtil;

    /**
     * 内存中维护上传任务元数据
     * 注意：服务重启后上传状态会丢失，后续应迁移至Redis或数据库
     */
    private static final ConcurrentHashMap<String, UploadTaskMetadata> UPLOAD_TASKS = new ConcurrentHashMap<>();

    /** 过期上传任务清理阈值（24小时） */
    private static final long UPLOAD_EXPIRE_HOURS = 24;

    /**
     * 清理过期的上传任务（超过24小时未更新的任务）
     * 可由外部定时任务或Controller接口调用
     */
    public int cleanExpiredTasks() {
        int cleaned = 0;
        LocalDateTime threshold = LocalDateTime.now().minusHours(UPLOAD_EXPIRE_HOURS);
        Iterator<Map.Entry<String, UploadTaskMetadata>> it = UPLOAD_TASKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UploadTaskMetadata> entry = it.next();
            UploadTaskMetadata meta = entry.getValue();
            if (meta.getLastUpdateTime() != null && meta.getLastUpdateTime().isBefore(threshold)) {
                log.info("[ChunkUpload] 清理过期任务: uploadId={}, lastUpdate={}", meta.getUploadId(), meta.getLastUpdateTime());
                it.remove();
                cleaned++;
            }
        }
        if (cleaned > 0) {
            log.info("[ChunkUpload] 已清理{}个过期上传任务，当前剩余: {}", cleaned, UPLOAD_TASKS.size());
        }
        return cleaned;
    }

    /**
     * 上传任务元数据
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UploadTaskMetadata {
        private String uploadId;
        private String fileName;
        private Long totalSize;
        private String fileHash;
        private Long scheduleId;
        private Integer totalChunks;
        private Long chunkSize;
        private Map<Integer, String> uploadedChunks; // chunkIndex -> chunkPath
        private LocalDateTime createTime;
        private LocalDateTime lastUpdateTime;
        private String status; // UPLOADING, COMPLETED, FAILED, PAUSED
    }

    @Override
    public UploadProgressResponse initializeUpload(String uploadId, String fileName, 
                                                    Long totalSize, String fileHash, Long scheduleId) {
        log.info("初始化上传任务 - uploadId: {}, fileName: {}, totalSize: {}", 
                uploadId, fileName, totalSize);

        // 验证参数
        if (totalSize > uploadProperties.getMaxFileSize()) {
            throw new BusinessException("文件大小超过限制，最大: " + uploadProperties.getMaxFileSize());
        }

        // 验证日程
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("日程不存在");
        }

        Long tenantId = TenantContextHolder.getTenantId();
        if (!schedule.getTenantId().equals(tenantId)) {
            throw new BusinessException("无权限访问该日程");
        }

        // 计算分块数
        long chunkSize = uploadProperties.getChunkSize();
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

        // 创建临时目录
        Path tempDir = createTempUploadDir(uploadId);

        // 创建上传任务
        UploadTaskMetadata metadata = new UploadTaskMetadata();
        metadata.setUploadId(uploadId);
        metadata.setFileName(fileName);
        metadata.setTotalSize(totalSize);
        metadata.setFileHash(fileHash);
        metadata.setScheduleId(scheduleId);
        metadata.setTotalChunks(totalChunks);
        metadata.setChunkSize(chunkSize);
        metadata.setUploadedChunks(new ConcurrentHashMap<>());
        metadata.setCreateTime(LocalDateTime.now());
        metadata.setLastUpdateTime(LocalDateTime.now());
        metadata.setStatus("UPLOADING");

        UPLOAD_TASKS.put(uploadId, metadata);

        log.info("上传任务创建成功 - uploadId: {}, totalChunks: {}", uploadId, totalChunks);

        // 返回进度信息
        return buildProgressResponse(metadata);
    }

    @Override
    @Transactional
    public ChunkUploadResponse uploadChunk(ChunkUploadRequest request, MultipartFile chunk) {
        String uploadId = request.getUploadId();
        Integer chunkIndex = request.getChunkIndex();

        log.debug("上传分块 - uploadId: {}, chunkIndex: {}, chunkSize: {}", 
                uploadId, chunkIndex, chunk.getSize());

        // 获取上传任务
        UploadTaskMetadata metadata = UPLOAD_TASKS.get(uploadId);
        if (metadata == null) {
            throw new BusinessException("上传任务不存在或已过期");
        }

        // 验证分块索引
        if (chunkIndex < 0 || chunkIndex >= metadata.getTotalChunks()) {
            throw new BusinessException("分块索引无效");
        }

        // 验证分块大小
        if (chunk.getSize() > uploadProperties.getChunkSize()) {
            throw new BusinessException("分块大小超过限制");
        }

        try {
            // 计算分块哈希值
            String chunkHash = calculateHash(chunk);
            if (request.getChunkHash() != null && !request.getChunkHash().equals(chunkHash)) {
                throw new BusinessException("分块哈希值校验失败");
            }

            // 保存分块到临时目录
            Path tempDir = getTempUploadDir(uploadId);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            String chunkFileName = String.format("chunk_%d", chunkIndex);
            Path chunkPath = tempDir.resolve(chunkFileName);

            // 写入分块文件
            chunk.transferTo(chunkPath.toFile());

            // 记录上传的分块
            metadata.getUploadedChunks().put(chunkIndex, chunkPath.toString());
            metadata.setLastUpdateTime(LocalDateTime.now());

            // 检查是否全部上传完成
            boolean allUploaded = metadata.getUploadedChunks().size() == metadata.getTotalChunks();

            log.debug("分块上传成功 - uploadId: {}, chunkIndex: {}, 已上传: {}/{}", 
                    uploadId, chunkIndex, metadata.getUploadedChunks().size(), metadata.getTotalChunks());

            // 构建响应
            ChunkUploadResponse response = ChunkUploadResponse.builder()
                    .uploadId(uploadId)
                    .chunkIndex(chunkIndex)
                    .success(true)
                    .message("分块上传成功")
                    .uploadedChunks(metadata.getUploadedChunks().size())
                    .totalChunks(metadata.getTotalChunks())
                    .progress(calculateProgress(metadata))
                    .build();

            // 如果全部上传完成，自动合并
            if (allUploaded) {
                log.info("所有分块已上传，开始合并 - uploadId: {}", uploadId);
                metadata.setStatus("COMPLETED");
                Object attachment = mergeChunks(uploadId, metadata.getScheduleId(), "");
                response.setAttachment((ScheduleAttachmentResponse) attachment);
            }

            return response;

        } catch (IOException e) {
            log.error("分块保存失败", e);
            metadata.setStatus("FAILED");
            throw new BusinessException("分块保存失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("分块上传异常", e);
            metadata.setStatus("FAILED");
            throw new BusinessException("分块上传失败：" + e.getMessage());
        }
    }

    @Override
    public UploadProgressResponse queryProgress(String uploadId) {
        UploadTaskMetadata metadata = UPLOAD_TASKS.get(uploadId);
        if (metadata == null) {
            throw new BusinessException("上传任务不存在");
        }

        return buildProgressResponse(metadata);
    }

    @Override
    @Transactional
    public Object mergeChunks(String uploadId, Long scheduleId, String description) {
        log.info("合并分块 - uploadId: {}, scheduleId: {}", uploadId, scheduleId);

        UploadTaskMetadata metadata = UPLOAD_TASKS.get(uploadId);
        if (metadata == null) {
            throw new BusinessException("上传任务不存在");
        }

        try {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                throw new BusinessException("未获取到租户信息");
            }

            // 验证日程
            Schedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule == null || !schedule.getTenantId().equals(tenantId)) {
                throw new BusinessException("日程不存在或无权限访问");
            }

            // 验证所有分块已上传
            if (metadata.getUploadedChunks().size() != metadata.getTotalChunks()) {
                throw new BusinessException("还有分块未上传");
            }

            // 合并分块到目标文件
            Path tempDir = getTempUploadDir(uploadId);
            String fileName = metadata.getFileName();
            String fileType = getFileExtension(fileName);
            
            // 创建最终存储目录
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String relativePath = String.format("uploads/%d/schedule/%d/%s", tenantId, scheduleId, timestamp);
            Path uploadDir = Paths.get(System.getProperty("user.dir"), relativePath);
            
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成最终文件名
            String finalFileName = System.currentTimeMillis() + "_" + fileName;
            Path finalPath = uploadDir.resolve(finalFileName);
            
            // 合并分块
            try (FileOutputStream fos = new FileOutputStream(finalPath.toFile())) {
                for (int i = 0; i < metadata.getTotalChunks(); i++) {
                    Path chunkPath = Paths.get(metadata.getUploadedChunks().get(i));
                    if (!Files.exists(chunkPath)) {
                        throw new BusinessException("分块文件丢失：" + i);
                    }
                    
                    byte[] chunkData = Files.readAllBytes(chunkPath);
                    fos.write(chunkData);
                }
            }

            log.info("分块合并完成 - uploadId: {}, finalPath: {}", uploadId, finalPath);

            // 尝试压缩文件
            Path compressedPath = finalPath;
            if (fileCompressionUtil.shouldCompress(fileType)) {
                Path tempCompressedPath = uploadDir.resolve(finalFileName.replace(".", "_compressed."));
                if (fileCompressionUtil.compressFile(finalPath, tempCompressedPath, fileType)) {
                    // 使用压缩后的文件
                    Files.delete(finalPath);
                    compressedPath = tempCompressedPath;
                    log.info("文件压缩成功 - uploadId: {}", uploadId);
                } else {
                    log.debug("文件压缩失败或跳过，使用原文件");
                }
            }

            // 保存附件元数据
            long fileSize = Files.size(compressedPath);
            String fileUrl = "/" + relativePath + "/" + compressedPath.getFileName();
            
            ScheduleAttachment attachment = ScheduleAttachment.builder()
                    .scheduleId(scheduleId)
                    .meetingId(schedule.getMeetingId())
                    .tenantId(tenantId)
                    .fileName(fileName)
                    .fileSize(fileSize)
                    .fileType(fileType)
                    .fileUrl(fileUrl)
                    .fileHash(metadata.getFileHash())
                    .description(description != null ? description : "")
                    .uploadBy(TenantContextHolder.getUserId())
                    .uploadTime(LocalDateTime.now())
                    .downloadCount(0)
                    .deleted(0)
                    .build();

            if (scheduleAttachmentMapper.insert(attachment) <= 0) {
                throw new BusinessException("保存附件元数据失败");
            }

            log.info("附件保存成功 - attachmentId: {}, fileName: {}", attachment.getId(), fileName);

            // 清理临时目录
            deleteTempUploadDir(uploadId);
            UPLOAD_TASKS.remove(uploadId);

            // 返回附件信息
            return ScheduleAttachmentResponse.builder()
                    .id(attachment.getId())
                    .fileName(attachment.getFileName())
                    .fileSize(attachment.getFileSize())
                    .fileType(attachment.getFileType())
                    .fileUrl(attachment.getFileUrl())
                    .description(attachment.getDescription())
                    .uploadTime(attachment.getUploadTime())
                    .downloadCount(0)
                    .build();

        } catch (IOException e) {
            log.error("合并分块失败", e);
            metadata.setStatus("FAILED");
            throw new BusinessException("合并分块失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("合并分块异常", e);
            metadata.setStatus("FAILED");
            throw new BusinessException("合并分块异常：" + e.getMessage());
        }
    }

    @Override
    public void cancelUpload(String uploadId) {
        log.info("取消上传 - uploadId: {}", uploadId);

        UploadTaskMetadata metadata = UPLOAD_TASKS.get(uploadId);
        if (metadata != null) {
            metadata.setStatus("PAUSED");
            deleteTempUploadDir(uploadId);
        }
    }

    @Override
    public void cleanExpiredTempFiles() {
        log.info("清理过期的临时文件");

        long expireTime = uploadProperties.getTempFileExpireTime();
        LocalDateTime now = LocalDateTime.now();

        List<String> expiredUploads = UPLOAD_TASKS.entrySet().stream()
                .filter(entry -> {
                    LocalDateTime lastUpdate = entry.getValue().getLastUpdateTime();
                    long diff = System.currentTimeMillis() - lastUpdate.toLocalTime().toNanoOfDay() / 1_000_000;
                    return diff > expireTime;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (String uploadId : expiredUploads) {
            try {
                deleteTempUploadDir(uploadId);
                UPLOAD_TASKS.remove(uploadId);
                log.info("已清理过期上传任务 - uploadId: {}", uploadId);
            } catch (Exception e) {
                log.warn("清理过期上传任务失败 - uploadId: {}", uploadId, e);
            }
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建临时上传目录
     */
    private Path createTempUploadDir(String uploadId) {
        Path tempDir = Paths.get(System.getProperty("user.dir"), 
                uploadProperties.getTempDir(), uploadId);
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            return tempDir;
        } catch (IOException e) {
            log.error("创建临时目录失败: {}", tempDir, e);
            throw new BusinessException("创建临时目录失败");
        }
    }

    /**
     * 获取临时上传目录
     */
    private Path getTempUploadDir(String uploadId) {
        return Paths.get(System.getProperty("user.dir"), 
                uploadProperties.getTempDir(), uploadId);
    }

    /**
     * 删除临时上传目录
     */
    private void deleteTempUploadDir(String uploadId) {
        Path tempDir = getTempUploadDir(uploadId);
        try {
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("删除文件失败: {}", path, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.warn("删除临时目录失败: {}", tempDir, e);
        }
    }

    /**
     * 计算文件哈希值
     */
    private String calculateHash(MultipartFile file) throws Exception {
        byte[] data = file.getBytes();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 计算上传进度百分比
     */
    private Integer calculateProgress(UploadTaskMetadata metadata) {
        if (metadata.getTotalChunks() == 0) return 0;
        return (int) ((double) metadata.getUploadedChunks().size() / metadata.getTotalChunks() * 100);
    }

    /**
     * 构建进度响应
     */
    private UploadProgressResponse buildProgressResponse(UploadTaskMetadata metadata) {
        int uploadedCount = metadata.getUploadedChunks().size();
        int totalChunks = metadata.getTotalChunks();
        int progress = calculateProgress(metadata);
        long uploadedSize = (long) uploadedCount * metadata.getChunkSize();

        return UploadProgressResponse.builder()
                .uploadId(metadata.getUploadId())
                .fileName(metadata.getFileName())
                .totalSize(metadata.getTotalSize())
                .totalChunks(totalChunks)
                .uploadedChunks(buildUploadedChunksList(metadata))
                .uploadedCount(uploadedCount)
                .progress(progress)
                .status(metadata.getStatus())
                .uploadedSize(Math.min(uploadedSize, metadata.getTotalSize()))
                .estimatedTime(estimateRemainingTime(uploadedSize, metadata.getTotalSize()))
                .build();
    }

    /**
     * 构建已上传分块列表
     */
    private List<Boolean> buildUploadedChunksList(UploadTaskMetadata metadata) {
        List<Boolean> list = new ArrayList<>();
        for (int i = 0; i < metadata.getTotalChunks(); i++) {
            list.add(metadata.getUploadedChunks().containsKey(i));
        }
        return list;
    }

    /**
     * 估算剩余时间（秒）
     */
    private Long estimateRemainingTime(long uploaded, long total) {
        if (uploaded == 0) return null;
        
        long remaining = total - uploaded;
        return remaining / Math.max(1, uploaded);
    }
}
