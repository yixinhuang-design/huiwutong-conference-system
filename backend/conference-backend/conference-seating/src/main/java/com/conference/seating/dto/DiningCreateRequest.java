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
 * 用餐安排创建请求 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用餐安排创建请求")
public class DiningCreateRequest {
    
    @NotNull(message = "会议ID不能为空")
    @Schema(description = "会议ID", example = "1")
    private Long conferenceId;
    
    @NotBlank(message = "餐厅名称不能为空")
    @Schema(description = "餐厅名称", example = "主餐厅")
    private String restaurantName;
    
    @NotBlank(message = "用餐类型不能为空")
    @Schema(description = "用餐类型", example = "BREAKFAST|LUNCH|DINNER")
    private String mealType;
    
    @NotNull(message = "人数容量不能为空")
    @Min(value = 1, message = "容量至少1人")
    @Schema(description = "用餐容量", example = "100")
    private Integer capacity;
    
    @Schema(description = "用餐地址", example = "一楼大厅")
    private String location;
    
    @Schema(description = "用餐时间", example = "12:00-13:30")
    private String mealTime;
    
    @Schema(description = "菜单或特殊要求", example = "素食可选")
    private String remarks;
}

