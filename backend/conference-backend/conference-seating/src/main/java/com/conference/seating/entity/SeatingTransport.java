package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 车辆运输实体类
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_transport")
public class SeatingTransport {
    
    private Long id;
    
    private Long conferenceId;
    
    private Long tenantId;
    
    private String licensePlate;
    
    private String vehicleType;
    
    private Integer capacity;
    
    private Integer assignedCount;
    
    private String departure;
    
    private String destination;
    
    private String departureTime;
    
    private String driver;
    
    private String driverPhone;
    
    private String remarks;
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    private LocalDateTime updatedAt;
}
