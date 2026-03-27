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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_archive_material")
public class ArchiveMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("meeting_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long meetingId;

    @TableField("tenant_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    /** 分类: courseware-课件, interaction-互动 */
    private String category;

    /** 子分类: ppt/pdf/word/video/image 或 photo/video/experience/exchange */
    @TableField("sub_category")
    private String subCategory;

    private String title;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_size")
    private String fileSize;

    @TableField("file_type")
    private String fileType;

    @TableField("thumbnail_url")
    private String thumbnailUrl;

    private String content;

    private String author;

    @TableField("upload_by")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uploadBy;

    @TableField("upload_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime uploadTime;

    @TableField("download_count")
    private Integer downloadCount;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer deleted;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
