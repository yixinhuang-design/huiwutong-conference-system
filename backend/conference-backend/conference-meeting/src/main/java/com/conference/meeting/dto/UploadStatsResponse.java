package com.conference.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传统计信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadStatsResponse {

    /**
     * 上传任务ID
     */
    private String uploadId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件总大小（字节）
     */
    private Long totalSize;

    /**
     * 已上传大小（字节）
     */
    private Long uploadedSize;

    /**
     * 总分块数
     */
    private Integer totalChunks;

    /**
     * 已上传分块数
     */
    private Integer uploadedChunks;

    /**
     * 上传进度（0-100%）
     */
    private Integer progress;

    /**
     * 上传状态（UPLOADING, COMPLETED, FAILED, PAUSED）
     */
    private String status;

    /**
     * 估算剩余时间（秒）
     */
    private Long estimatedTime;

    /**
     * 上传速度（字节/秒）
     */
    private Long uploadSpeed;

    /**
     * 已用时间（毫秒）
     */
    private Long elapsedTime;
}
