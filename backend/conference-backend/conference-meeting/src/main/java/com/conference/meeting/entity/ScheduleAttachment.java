package com.conference.meeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日程附件实体类
 * 对应表: conf_schedule_attachment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_schedule_attachment")
public class ScheduleAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 日程ID
     */
    @TableField("schedule_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 会议ID
     */
    @TableField("meeting_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long meetingId;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型（ppt, pptx, pdf, doc, docx等）
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件访问URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件哈希值（防止重复上传）
     */
    @TableField("file_hash")
    private String fileHash;

    /**
     * 文件描述
     */
    @TableField("description")
    private String description;

    /**
     * 上传者ID
     */
    @TableField("upload_by")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uploadBy;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime uploadTime;

    /**
     * 下载次数
     */
    @TableField("download_count")
    private Integer downloadCount;

    /**
     * 删除标志
     */
    @TableField("deleted")
    private Integer deleted;
}
