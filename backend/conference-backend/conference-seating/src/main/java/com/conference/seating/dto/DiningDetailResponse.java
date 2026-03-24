package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用餐安排详情响应 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用餐安排详情响应")
public class DiningDetailResponse {
    
    @Schema(description = "ID", example = "1")
    private Long id;
    
    @Schema(description = "会议ID", example = "1")
    private Long conferenceId;
    
    @Schema(description = "餐厅名称", example = "主餐厅")
    private String restaurantName;
    
    @Schema(description = "用餐类型", example = "BREAKFAST|LUNCH|DINNER")
    private String mealType;
    
    @Schema(description = "用餐容量", example = "100")
    private Integer capacity;
    
    @Schema(description = "已分配人数", example = "45")
    private Integer assignedCount;
    
    @Schema(description = "可用餐位数", example = "55")
    private Integer availableSeats;
    
    @Schema(description = "用餐日期")
    private LocalDate diningDate;
    
    @Schema(description = "用餐时间")
    private LocalTime diningTime;
    
    @Schema(description = "用餐地址", example = "一楼大厅")
    private String location;
    
    @Schema(description = "用餐时间(字符串格式)", example = "12:00-13:30")
    private String mealTime;
    
    @Schema(description = "菜单或特殊要求", example = "素食可选")
    private String remarks;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
