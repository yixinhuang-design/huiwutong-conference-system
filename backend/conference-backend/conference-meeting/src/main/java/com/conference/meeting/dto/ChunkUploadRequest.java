package com.conference.meeting.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上传分块请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChunkUploadRequest {

    /**
     * 上传任务ID（标识这是哪个文件的上传）
     */
    private String uploadId;

    /**
     * 分块索引（从0开始）
     */
    private Integer chunkIndex;

    /**
     * 总分块数
     */
    private Integer totalChunks;

    /**
     * 分块大小（字节）
     */
    private Long chunkSize;

    /**
     * 文件总大小（字节）
     */
    private Long totalSize;

    /**
     * 文件哈希值（整个文件的SHA256）
     */
    private String fileHash;

    /**
     * 分块哈希值（这个分块的SHA256）
     */
    private String chunkHash;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 日程ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 文件描述
     */
    private String description;
}
