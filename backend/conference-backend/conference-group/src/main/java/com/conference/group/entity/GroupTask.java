package com.conference.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群组任务实体
 */
@Data
@TableName("conference_group_task")
public class GroupTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 任务类型：meeting会议/reminder提醒/collect收集/feedback反馈
     */
    private String taskType;
    
    /**
     * 任务状态：pending待开始/in_progress进行中/completed已完成/cancelled已取消
     */
    private String status;
    
    /**
     * 优先级：high高/medium中/low低
     */
    private String priority;
    
    /**
     * 指派给（用户ID列表）
     */
    private String assigneeIds;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 完成进度（0-100）
     */
    private Integer progress;
    
    /**
     * 总目标数量（如：收集100份反馈）
     */
    private Integer totalTarget;
    
    /**
     * 已完成数量
     */
    private Integer completedCount;
    
    /**
     * 任务备注
     */
    private String notes;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 提醒时间
     */
    private LocalDateTime reminderTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * Web Key（用于前端识别和访问）
     */
    private String webKey;
}
