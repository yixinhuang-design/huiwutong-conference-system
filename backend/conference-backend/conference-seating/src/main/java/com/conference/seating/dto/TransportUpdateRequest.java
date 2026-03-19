package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆运输更新请求 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "车辆运输更新请求")
public class TransportUpdateRequest {
    
    @Schema(description = "车牌号", example = "浙A·12345")
    private String licensePlate;
    
    @Schema(description = "车辆类型", example = "BUS|VAN|CAR")
    private String vehicleType;
    
    @Min(value = 1, message = "座位容量至少1")
    @Schema(description = "座位容量", example = "45")
    private Integer capacity;
    
    @Schema(description = "出发地", example = "机场")
    private String departure;
    
    @Schema(description = "目的地", example = "酒店")
    private String destination;
    
    @Schema(description = "出发时间", example = "08:00")
    private String departureTime;
    
    @Schema(description = "驾驶员名字", example = "张三")
    private String driver;
    
    @Schema(description = "驾驶员电话", example = "13800138000")
    private String driverPhone;
}
