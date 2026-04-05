package com.conference.collaboration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务模板实体
 * @author AI Executive
 * @date 2026-04-01
 */
@Data
@TableName("task_template")
public class TaskTemplate {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务类别
     */
    private String category;

    /**
     * 完成方式
     */
    private String completionMethod;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 完成配置 (JSON)
     */
    private String config;

    /**
     * 模板图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否系统模板
     */
    private Integer isSystem;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
