package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 车辆运输详情响应 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "车辆运输详情响应")
public class TransportDetailResponse {
    
    @Schema(description = "ID", example = "1")
    private Long id;
    
    @Schema(description = "会议ID", example = "1")
    private Long conferenceId;
    
    @Schema(description = "车辆名称", example = "1号大巴")
    private String vehicleName;
    
    @Schema(description = "车牌号", example = "浙A·12345")
    private String licensePlate;
    
    @Schema(description = "车辆类型", example = "BUS")
    private String vehicleType;
    
    @Schema(description = "座位容量", example = "45")
    private Integer capacity;
    
    @Schema(description = "已分配座位数", example = "30")
    private Integer assignedCount;
    
    @Schema(description = "可用座位数", example = "15")
    private Integer availableSeats;
    
    @Schema(description = "出发地", example = "机场")
    private String departure;
    
    @Schema(description = "目的地", example = "酒店")
    private String destination;
    
    @Schema(description = "出发时间", example = "2026-03-12T08:00:00")
    private LocalDateTime departureTime;
    
    @Schema(description = "驾驶员名字", example = "张三")
    private String driver;
    
    @Schema(description = "驾驶员电话", example = "13800138000")
    private String driverPhone;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
