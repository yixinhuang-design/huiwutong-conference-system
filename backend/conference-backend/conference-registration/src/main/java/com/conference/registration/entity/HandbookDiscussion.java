package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 手册讨论题目
 */
@Data
@TableName("conf_handbook_discussion")
public class HandbookDiscussion {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("handbook_id")
    private Long handbookId;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("content")
    private String content;

    @TableField("reference")
    private String referenceText;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
