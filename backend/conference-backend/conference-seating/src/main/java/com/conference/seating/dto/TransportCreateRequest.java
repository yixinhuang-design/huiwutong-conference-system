package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆运输创建请求 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "车辆运输创建请求")
public class TransportCreateRequest {
    
    @NotNull(message = "会议ID不能为空")
    @Schema(description = "会议ID", example = "1")
    private Long conferenceId;
    
    @NotBlank(message = "车牌号不能为空")
    @Schema(description = "车牌号", example = "浙A·12345")
    private String licensePlate;
    
    @NotBlank(message = "车辆类型不能为空")
    @Schema(description = "车辆类型", example = "BUS|VAN|CAR")
    private String vehicleType;
    
    @NotNull(message = "座位容量不能为空")
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
