package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学员手册配置表
 */
@Data
@TableName("conf_handbook")
public class Handbook {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("meeting_id")
    private Long meetingId;

    @TableField("title")
    private String title;

    /** 0-草稿, 1-已生成, 2-已发布 */
    @TableField("status")
    private Integer status;

    /** 封面配置 JSON */
    @TableField("cover_config")
    private String coverConfig;

    /** 目录配置 JSON */
    @TableField("toc_config")
    private String tocConfig;

    /** 名册配置 JSON */
    @TableField("roster_config")
    private String rosterConfig;

    /** 名册字段配置 JSON */
    @TableField("roster_fields")
    private String rosterFields;

    /** 座位配置 JSON */
    @TableField("seating_config")
    private String seatingConfig;

    /** 乘车配置 JSON */
    @TableField("transport_config")
    private String transportConfig;

    /** 住宿配置 JSON */
    @TableField("hotel_config")
    private String hotelConfig;

    /** 就餐配置 JSON */
    @TableField("meal_config")
    private String mealConfig;

    /** 讨论配置 JSON */
    @TableField("discussion_config")
    private String discussionConfig;

    /** 封底配置 JSON */
    @TableField("backcover_config")
    private String backcoverConfig;

    /** 其他事项内容 */
    @TableField("notes_content")
    private String notesContent;

    /** 板块启用和排序配置 JSON */
    @TableField("sections_config")
    private String sectionsConfig;

    /** 分组配置 JSON */
    @TableField("grouping_config")
    private String groupingConfig;

    /** 生成的PDF路径 */
    @TableField("pdf_url")
    private String pdfUrl;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;
}
