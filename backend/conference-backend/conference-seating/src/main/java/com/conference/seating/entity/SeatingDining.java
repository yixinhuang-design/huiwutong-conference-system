package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用餐安排实体类
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
    
    private String restaurantName;
    
    private String mealType;
    
    private Integer capacity;
    
    private Integer assignedCount;
    
    private String location;
    
    private String mealTime;
    
    private String remarks;
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    private LocalDateTime updatedAt;
}
