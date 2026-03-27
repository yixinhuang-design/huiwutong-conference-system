package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会场实体类
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_venue")
public class SeatingVenue {
    
    private Long id;
    
    private Long conferenceId;
    
    private Long scheduleId;
    
    private Long tenantId;
    
    private String venueName;
    
    private String venueType;
    
    private Integer totalRows;
    
    private Integer totalColumns;
    
    private Integer capacity;
    
    private Integer assignedCount;
    
    private String layoutData;
    
    private String status;
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    private LocalDateTime updatedAt;
}
