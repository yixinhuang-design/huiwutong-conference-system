package com.conference.meeting.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分块上传响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChunkUploadResponse {

    /**
     * 上传任务ID
     */
    private String uploadId;

    /**
     * 分块索引
     */
    private Integer chunkIndex;

    /**
     * 分块是否上传成功
     */
    private Boolean success;

    /**
     * 上传状态消息
     */
    private String message;

    /**
     * 分块上传后的临时路径（用于调试）
     */
    private String tempPath;

    /**
     * 当前已上传的分块数（用于前端显示进度）
     */
    private Integer uploadedChunks;

    /**
     * 总分块数
     */
    private Integer totalChunks;

    /**
     * 上传进度百分比
     */
    private Integer progress;

    /**
     * 如果全部分块已上传，返回最终的附件信息
     */
    private ScheduleAttachmentResponse attachment;
}
