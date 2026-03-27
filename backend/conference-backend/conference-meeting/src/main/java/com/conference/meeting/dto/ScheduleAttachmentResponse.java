package com.conference.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 日程附件响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleAttachmentResponse {

    /**
     * 附件ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 上传者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uploadBy;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime uploadTime;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 格式化的文件大小
     */
    private String fileSizeText;
}
