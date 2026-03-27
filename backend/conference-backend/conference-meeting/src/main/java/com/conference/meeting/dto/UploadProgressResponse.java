package com.conference.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 上传进度查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadProgressResponse {

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
     * 总分块数
     */
    private Integer totalChunks;

    /**
     * 已上传分块列表（true=已上传, false=未上传）
     */
    private List<Boolean> uploadedChunks;

    /**
     * 已上传的分块数
     */
    private Integer uploadedCount;

    /**
     * 上传进度百分比 (0-100)
     */
    private Integer progress;

    /**
     * 上传状态: UPLOADING, COMPLETED, FAILED, PAUSED
     */
    private String status;

    /**
     * 已上传的数据大小（字节）
     */
    private Long uploadedSize;

    /**
     * 估算剩余时间（秒）
     */
    private Long estimatedTime;
}
