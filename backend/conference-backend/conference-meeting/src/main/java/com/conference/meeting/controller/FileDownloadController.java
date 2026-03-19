package com.conference.meeting.controller;

import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.meeting.entity.ScheduleAttachment;
import com.conference.meeting.mapper.ScheduleAttachmentMapper;
import com.conference.meeting.service.IScheduleAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件下载控制器
 * 处理课件、PPT等文件的下载
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileDownloadController {

    @Autowired
    private ScheduleAttachmentMapper scheduleAttachmentMapper;

    @Autowired
    private IScheduleAttachmentService scheduleAttachmentService;

    /**
     * 下载日程附件
     * GET /api/file/download/{attachmentId}
     * 
     * @param attachmentId 附件ID
     * @return 文件流
     */
    @GetMapping("/download/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            if (attachmentId == null || attachmentId <= 0) {
                throw new BusinessException("附件ID无效");
            }

            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                throw new BusinessException("未获取到租户信息");
            }

            log.info("下载附件 - attachmentId: {}", attachmentId);

            // 查询附件
            ScheduleAttachment attachment = scheduleAttachmentMapper.selectById(attachmentId);
            if (attachment == null || !attachment.getTenantId().equals(tenantId) || attachment.getDeleted() != 0) {
                throw new BusinessException("附件不存在或无权限访问");
            }

            // 构建完整文件路径
            String fileUrl = attachment.getFileUrl();
            String filePath = System.getProperty("user.dir") + fileUrl.replace("/", File.separator);
            Path path = Paths.get(filePath);

            // 验证文件是否存在
            if (!Files.exists(path)) {
                log.warn("文件不存在 - filePath: {}", filePath);
                throw new BusinessException("文件已被删除");
            }

            // 增加下载计数
            scheduleAttachmentService.incrementDownloadCount(attachmentId);

            // 创建文件资源
            Resource resource = new FileSystemResource(path);

            // 设置响应头
            String fileName = attachment.getFileName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, getMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(attachment.getFileSize()))
                    .body(resource);

        } catch (BusinessException e) {
            log.warn("下载失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("文件下载异常", e);
            throw new BusinessException("文件下载失败：" + e.getMessage());
        }
    }

    /**
     * 在线预览附件（支持某些文件类型）
     * GET /api/file/preview/{attachmentId}
     * 
     * @param attachmentId 附件ID
     * @return 文件内容
     */
    @GetMapping("/preview/{attachmentId}")
    public ResponseEntity<Resource> previewAttachment(@PathVariable Long attachmentId) {
        try {
            if (attachmentId == null || attachmentId <= 0) {
                throw new BusinessException("附件ID无效");
            }

            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                throw new BusinessException("未获取到租户信息");
            }

            log.info("预览附件 - attachmentId: {}", attachmentId);

            // 查询附件
            ScheduleAttachment attachment = scheduleAttachmentMapper.selectById(attachmentId);
            if (attachment == null || !attachment.getTenantId().equals(tenantId) || attachment.getDeleted() != 0) {
                throw new BusinessException("附件不存在或无权限访问");
            }

            // 只允许预览特定类型的文件
            String fileType = attachment.getFileType().toLowerCase();
            if (!isPreviewable(fileType)) {
                throw new BusinessException("不支持预览此文件类型：" + fileType);
            }

            // 构建完整文件路径
            String fileUrl = attachment.getFileUrl();
            String filePath = System.getProperty("user.dir") + fileUrl.replace("/", File.separator);
            Path path = Paths.get(filePath);

            // 验证文件是否存在
            if (!Files.exists(path)) {
                log.warn("文件不存在 - filePath: {}", filePath);
                throw new BusinessException("文件已被删除");
            }

            // 创建文件资源
            Resource resource = new FileSystemResource(path);

            // 设置响应头（inline显示而不是下载）
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, getMediaType(fileType))
                    .body(resource);

        } catch (BusinessException e) {
            log.warn("预览失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("文件预览异常", e);
            throw new BusinessException("文件预览失败：" + e.getMessage());
        }
    }

    /**
     * 获取Media Type
     */
    private String getMediaType(String fileType) {
        if (fileType == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String type = fileType.toLowerCase();
        return switch (type) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

    /**
     * 判断是否支持在线预览
     */
    private boolean isPreviewable(String fileType) {
        if (fileType == null) {
            return false;
        }

        String type = fileType.toLowerCase();
        return type.equals("pdf") || type.equals("jpg") || type.equals("jpeg") 
               || type.equals("png") || type.equals("gif") || type.equals("txt");
    }
}
