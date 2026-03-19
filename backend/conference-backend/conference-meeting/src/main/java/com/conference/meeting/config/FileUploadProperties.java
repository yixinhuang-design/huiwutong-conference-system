package com.conference.meeting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /**
     * 最大文件大小 (默认100MB)
     */
    private Long maxFileSize = 100L * 1024 * 1024;

    /**
     * 分块大小 (默认5MB)
     * 用于断点续传时的分块大小
     */
    private Long chunkSize = 5L * 1024 * 1024;

    /**
     * 临时上传目录 (相对于项目根目录)
     */
    private String tempDir = "uploads/.temp";

    /**
     * 真实上传目录
     */
    private String uploadDir = "uploads";

    /**
     * 允许的文件类型
     */
    private String[] allowedExtensions = {
            "ppt", "pptx", "pdf", "doc", "docx", "xls", "xlsx",
            "txt", "jpg", "jpeg", "png", "gif"
    };

    /**
     * 启用图片压缩
     */
    private Boolean enableImageCompress = true;

    /**
     * 图片压缩质量 (0-100)
     */
    private Integer imageQuality = 85;

    /**
     * 启用PDF压缩
     */
    private Boolean enablePdfCompress = true;

    /**
     * 临时文件过期时间 (毫秒，默认24小时)
     */
    private Long tempFileExpireTime = 24 * 60 * 60 * 1000L;

    /**
     * 启用断点续传
     */
    private Boolean enableResumeUpload = true;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 重试延迟 (毫秒)
     */
    private Long retryDelay = 1000L;
}
