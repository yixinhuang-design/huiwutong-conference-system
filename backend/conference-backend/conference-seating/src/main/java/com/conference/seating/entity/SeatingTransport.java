package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 车辆运输实体类
 * 对应数据库表: conf_seating_transport
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
    
    /** 车辆名称 - DB列: vehicle_name */
    private String vehicleName;
    
    /** 车牌号 - DB列: plate_number */
    @TableField("plate_number")
    private String licensePlate;
    
    private String vehicleType;
    
    private Integer capacity;
    
    private Integer assignedCount;
    
    private String departure;
    
    private String destination;
    
    /** 出发时间 - DB列: departure_time (DATETIME) */
    private LocalDateTime departureTime;
    
    /** 兼容字段 - 数据库中不存在 */
    @TableField(exist = false)
    private String driver;
    
    /** 兼容字段 - 数据库中不存在 */
    @TableField(exist = false)
    private String driverPhone;
    
    /** 兼容字段 - 数据库中不存在 */
    @TableField(exist = false)
    private String remarks;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
