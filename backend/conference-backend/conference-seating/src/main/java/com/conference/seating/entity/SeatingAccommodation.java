package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 住宿安排实体类
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_accommodation")
public class SeatingAccommodation {
    
    private Long id;
    
    private Long conferenceId;
    
    private Long tenantId;
    
    private String roomNumber;
    
    private String roomType;
    
    private Integer capacity;
    
    private Integer assignedCount;
    
    private String address;
    
    private String phone;
    
    private String status;
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    private LocalDateTime updatedAt;
}
