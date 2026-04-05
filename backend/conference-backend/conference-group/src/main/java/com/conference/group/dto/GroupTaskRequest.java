package com.conference.group.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 群组任务请求
 */
@Data
public class GroupTaskRequest {
    
    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 任务类型
     */
    @NotBlank(message = "任务类型不能为空")
    private String taskType;
    
    /**
     * 优先级
     */
    private String priority;
    
    /**
     * 指派人员ID列表
     */
    private java.util.List<Long> assigneeIds;
    
    /**
     * 截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 总目标数量
     */
    private Integer totalTarget;
    
    /**
     * 提醒时间
     */
    private LocalDateTime reminderTime;
    
    /**
     * 任务备注
     */
    private String notes;
}
