package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 用餐安排更新请求 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用餐安排更新请求")
public class DiningUpdateRequest {
    
    @Schema(description = "餐厅名称", example = "主餐厅")
    private String restaurantName;
    
    @Schema(description = "用餐类型", example = "BREAKFAST|LUNCH|DINNER")
    private String mealType;
    
    @Min(value = 1, message = "容量至少1人")
    @Schema(description = "用餐容量", example = "100")
    private Integer capacity;
    
    @Schema(description = "用餐地址", example = "一楼大厅")
    private String location;
    
    @Schema(description = "用餐日期", example = "2026-03-12")
    private LocalDate diningDate;
    
    @Schema(description = "用餐时间", example = "12:00")
    private LocalTime diningTime;
    
    @Schema(description = "用餐时间(字符串格式，兼容)", example = "12:00-13:30")
    private String mealTime;
    
    @Schema(description = "菜单或特殊要求", example = "素食可选")
    private String remarks;
}
