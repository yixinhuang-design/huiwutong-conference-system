package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 日程安排实体类
 * 对应数据库表: conf_seating_schedule
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_schedule")
public class SeatingSchedule {
    
    private Long id;
    
    private Long conferenceId;
    
    private Long tenantId;
    
    /** 日程名称 - DB列: schedule_name */
    @TableField("schedule_name")
    private String scheduleTitle;
    
    /** 日程描述 - DB列: description */
    @TableField("description")
    private String scheduleDescription;
    
    /** 日程日期 - DB列: schedule_date (DATE) */
    private LocalDate scheduleDate;
    
    /** 开始时间 - DB列: start_time (TIME) */
    private LocalTime startTime;
    
    /** 结束时间 - DB列: end_time (TIME) */
    private LocalTime endTime;
    
    /** 是否默认日程 - DB列: is_default */
    private Boolean isDefault;
    
    /** 兼容字段 - 数据库中不存在 */
    @TableField(exist = false)
    private String location;
    
    /** 兼容字段 - 数据库中不存在 */
    @TableField(exist = false)
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
