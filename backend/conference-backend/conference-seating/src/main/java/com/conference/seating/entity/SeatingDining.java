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
 * 用餐安排实体类
 * 对应数据库表: conf_seating_dining
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_dining")
public class SeatingDining {
    
    private Long id;
    
    private Long conferenceId;
    
    private Long tenantId;
    
    /** 用餐名称 - DB列: dining_name */
    @TableField("dining_name")
    private String restaurantName;
    
    /** 用餐类型 - DB列: dining_type */
    @TableField("dining_type")
    private String mealType;
    
    private Integer capacity;
    
    private Integer assignedCount;
    
    /** 用餐日期 - DB列: dining_date (DATE) */
    private LocalDate diningDate;
    
    /** 用餐时间 - DB列: dining_time (TIME) */
    private LocalTime diningTime;
    
    private String location;
    
    /** 兼容字段(原用餐时间字符串) - 数据库中不存在 */
    @TableField(exist = false)
    private String mealTime;
    
    /** 兼容字段 - 数据库中不存在 */
    @TableField(exist = false)
    private String remarks;
    
    private LocalDateTime createdAt;
    
    /** 兼容字段 - 数据库中不存在updated_at列 */
    @TableField(exist = false)
    private LocalDateTime updatedAt;
}
