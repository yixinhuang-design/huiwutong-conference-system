package com.conference.meeting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.meeting.dto.ScheduleAttachmentResponse;
import com.conference.meeting.entity.Schedule;
import com.conference.meeting.entity.ScheduleAttachment;
import com.conference.meeting.mapper.ScheduleAttachmentMapper;
import com.conference.meeting.mapper.ScheduleMapper;
import com.conference.meeting.service.IScheduleAttachmentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 日程附件服务实现类
 * 处理课件、PPT等文件的上传、存储、下载、删除
 */
@Slf4j
@Service
@AllArgsConstructor
public class ScheduleAttachmentServiceImpl extends ServiceImpl<ScheduleAttachmentMapper, ScheduleAttachment> 
        implements IScheduleAttachmentService {

    private final ScheduleAttachmentMapper scheduleAttachmentMapper;
    private final ScheduleMapper scheduleMapper;

    // 配置常量
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final String[] ALLOWED_EXTENSIONS = {"ppt", "pptx", "pdf", "doc", "docx", "xls", "xlsx", "txt", "jpg", "jpeg", "png"};
    private static final String UPLOAD_DIR = "uploads";

    @Override
    @Transactional
    public ScheduleAttachmentResponse uploadAttachment(Long scheduleId, MultipartFile file, String description) {
        log.info("上传附件开始 - scheduleId: {}, fileName: {}, fileSize: {}", 
                scheduleId, file.getOriginalFilename(), file.getSize());

        // 获取租户信息
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 验证日程是否存在
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || !schedule.getTenantId().equals(tenantId)) {
            throw new BusinessException("日程不存在或无权限访问");
        }

        // 验证文件
        String fileName = file.getOriginalFilename();
        Map<String, Object> validation = validateFile(fileName, file.getSize());
        if (!Boolean.TRUE.equals(validation.get("isValid"))) {
            throw new BusinessException((String) validation.get("message"));
        }

        try {
            // 计算文件哈希
            String fileHash = calculateFileHash(file);
            log.debug("文件哈希计算完成 - hash: {}", fileHash);

            // 检查是否已上传过相同文件
            ScheduleAttachment existing = scheduleAttachmentMapper.selectByFileHash(scheduleId, fileHash);
            if (existing != null && existing.getDeleted() == 0) {
                log.warn("文件已存在，防止重复上传 - hash: {}", fileHash);
                throw new BusinessException("此文件已上传过，无需重复上传");
            }

            // 保存文件到服务器
            String fileUrl = saveFileToServer(file, scheduleId, tenantId);
            log.debug("文件保存成功 - fileUrl: {}", fileUrl);

            // 提取文件扩展名
            String fileType = getFileExtension(fileName);

            // 创建附件记录
            ScheduleAttachment attachment = ScheduleAttachment.builder()
                    .scheduleId(scheduleId)
                    .meetingId(schedule.getMeetingId())
                    .tenantId(tenantId)
                    .fileName(fileName)
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .fileUrl(fileUrl)
                    .fileHash(fileHash)
                    .description(description != null ? description : "")
                    .uploadBy(TenantContextHolder.getUserId())
                    .uploadTime(LocalDateTime.now())
                    .downloadCount(0)
                    .deleted(0)
                    .build();

            // 保存到数据库
            if (!this.save(attachment)) {
                throw new BusinessException("附件保存失败");
            }

            log.info("附件上传完成 - attachmentId: {}, fileName: {}", attachment.getId(), fileName);

            return convertToResponse(attachment);

        } catch (IOException e) {
            log.error("文件保存异常", e);
            throw new BusinessException("文件保存失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<ScheduleAttachmentResponse> uploadAttachmentBatch(Long scheduleId, MultipartFile[] files) {
        log.info("批量上传附件开始 - scheduleId: {}, fileCount: {}", scheduleId, files.length);

        List<ScheduleAttachmentResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                ScheduleAttachmentResponse response = uploadAttachment(scheduleId, file, null);
                responses.add(response);
                log.debug("批量上传 - 成功上传文件: {}", file.getOriginalFilename());
            } catch (Exception e) {
                log.warn("批量上传 - 文件上传失败: {}, 原因: {}", file.getOriginalFilename(), e.getMessage());
                // 继续处理下一个文件，不中断整个批量上传
            }
        }

        log.info("批量上传完成 - successCount: {}", responses.size());
        return responses;
    }

    @Override
    public List<ScheduleAttachmentResponse> listAttachments(Long scheduleId) {
        log.info("获取附件列表 - scheduleId: {}", scheduleId);

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 验证日程存在
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || !schedule.getTenantId().equals(tenantId)) {
            throw new BusinessException("日程不存在或无权限访问");
        }

        List<ScheduleAttachment> attachments = scheduleAttachmentMapper.selectByScheduleId(scheduleId);
        return attachments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleAttachmentResponse getAttachment(Long id) {
        log.info("获取附件详情 - id: {}", id);

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        ScheduleAttachment attachment = this.getById(id);
        if (attachment == null || !attachment.getTenantId().equals(tenantId) || attachment.getDeleted() != 0) {
            throw new BusinessException("附件不存在或无权限访问");
        }

        return convertToResponse(attachment);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long id) {
        log.info("删除附件 - id: {}", id);

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        ScheduleAttachment attachment = this.getById(id);
        if (attachment == null || !attachment.getTenantId().equals(tenantId)) {
            throw new BusinessException("附件不存在或无权限访问");
        }

        // 逻辑删除
        attachment.setDeleted(1);
        if (!this.updateById(attachment)) {
            throw new BusinessException("附件删除失败");
        }

        // 尝试删除物理文件（可选）
        try {
            deleteFileFromServer(attachment.getFileUrl());
            log.debug("物理文件删除成功 - fileUrl: {}", attachment.getFileUrl());
        } catch (Exception e) {
            log.warn("物理文件删除失败 - fileUrl: {}, 原因: {}", attachment.getFileUrl(), e.getMessage());
            // 物理删除失败不影响数据库操作
        }

        log.info("附件删除成功 - id: {}", id);
    }

    @Override
    @Transactional
    public ScheduleAttachmentResponse updateAttachment(Long id, String description) {
        log.info("更新附件 - id: {}", id);

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        ScheduleAttachment attachment = this.getById(id);
        if (attachment == null || !attachment.getTenantId().equals(tenantId)) {
            throw new BusinessException("附件不存在或无权限访问");
        }

        if (description != null) {
            attachment.setDescription(description);
            if (!this.updateById(attachment)) {
                throw new BusinessException("附件更新失败");
            }
        }

        log.info("附件更新成功 - id: {}", id);
        return convertToResponse(attachment);
    }

    @Override
    public String getDownloadUrl(Long id) {
        log.info("获取下载地址 - id: {}", id);

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        ScheduleAttachment attachment = this.getById(id);
        if (attachment == null || !attachment.getTenantId().equals(tenantId) || attachment.getDeleted() != 0) {
            throw new BusinessException("附件不存在或无权限访问");
        }

        // 返回专用的下载端点URL，而不是直接返回文件URL
        // 这样可以在下载时进行权限验证和计数统计
        String baseUrl = "http://localhost:8084"; // 需要从配置读取
        return baseUrl + "/api/file/download/" + id;
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long id) {
        log.debug("增加下载计数 - id: {}", id);

        ScheduleAttachment attachment = this.getById(id);
        if (attachment != null) {
            attachment.setDownloadCount((attachment.getDownloadCount() != null ? attachment.getDownloadCount() : 0) + 1);
            this.updateById(attachment);
        }
    }

    @Override
    public Map<String, Object> validateFile(String fileName, Long fileSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("isValid", true);
        result.put("message", "");

        // 检查文件大小
        if (fileSize == null || fileSize <= 0) {
            result.put("isValid", false);
            result.put("message", "文件大小不正确");
            return result;
        }

        if (fileSize > MAX_FILE_SIZE) {
            result.put("isValid", false);
            result.put("message", "文件大小不能超过 100MB");
            return result;
        }

        // 检查文件扩展名
        String extension = getFileExtension(fileName).toLowerCase();
        boolean isAllowed = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            result.put("isValid", false);
            result.put("message", "不支持的文件类型：" + extension);
            return result;
        }

        return result;
    }

    @Override
    public ScheduleAttachment getByFileHash(Long scheduleId, String fileHash) {
        return scheduleAttachmentMapper.selectByFileHash(scheduleId, fileHash);
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算文件哈希值（SHA256）
     */
    private String calculateFileHash(MultipartFile file) throws Exception {
        byte[] fileBytes = file.getBytes();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileBytes);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 保存文件到服务器
     */
    private String saveFileToServer(MultipartFile file, Long scheduleId, Long tenantId) throws IOException {
        // 创建上传目录：uploads/tenantId/schedule/scheduleId/
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String relativePath = String.format("%s/%d/schedule/%d/%s", UPLOAD_DIR, tenantId, scheduleId, timestamp);
        Path uploadPath = Paths.get(System.getProperty("user.dir"), relativePath);
        
        // 创建目录
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成新文件名（防止覆盖）
        String originalFileName = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_" + originalFileName;
        Path filePath = uploadPath.resolve(fileName);

        // 保存文件
        Files.write(filePath, file.getBytes());

        // 返回访问URL路径
        String fileUrl = "/" + relativePath + "/" + fileName;
        return fileUrl.replace("\\", "/");
    }

    /**
     * 删除服务器上的文件
     */
    private void deleteFileFromServer(String fileUrl) throws IOException {
        String filePath = System.getProperty("user.dir") + fileUrl.replace("/", File.separator);
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
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
     * 转换为Response对象
     */
    private ScheduleAttachmentResponse convertToResponse(ScheduleAttachment attachment) {
        return ScheduleAttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileSize(attachment.getFileSize())
                .fileType(attachment.getFileType())
                .fileUrl(attachment.getFileUrl())
                .description(attachment.getDescription())
                .uploadBy(attachment.getUploadBy())
                .uploadTime(attachment.getUploadTime())
                .downloadCount(attachment.getDownloadCount() != null ? attachment.getDownloadCount() : 0)
                .build();
    }
}
