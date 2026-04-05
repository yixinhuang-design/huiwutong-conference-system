package com.conference.collaboration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务依赖关系实体
 * @author AI Executive
 * @date 2026-04-01
 */
@Data
@TableName("task_dependency")
public class TaskDependency {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 租户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    /**
     * 任务ID (后置任务)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    /**
     * 前置任务ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long predecessorTaskId;

    /**
     * 依赖类型
     * finish_to_start: 前置任务完成后才能开始
     * start_to_start: 前置任务开始后才能开始
     * finish_to_finish: 前置任务完成后才能完成
     */
    private String dependencyType;

    /**
     * 时间偏移(分钟)
     * 例如：前置任务完成后30分钟才能开始
     */
    private Integer timeOffsetMinutes;

    /**
     * 是否阻塞
     * true: 必须等待前置任务完成
     * false: 仅提醒不阻塞
     */
    private Boolean isBlocking;

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
}
