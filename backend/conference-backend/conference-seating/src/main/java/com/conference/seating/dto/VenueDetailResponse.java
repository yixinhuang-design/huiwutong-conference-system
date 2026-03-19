package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会场详情响应 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "会场详情响应")
public class VenueDetailResponse {
    
    @Schema(description = "ID", example = "1")
    private Long id;
    
    @Schema(description = "会议ID", example = "1")
    private Long conferenceId;
    
    @Schema(description = "会场名称", example = "主会议厅")
    private String venueName;
    
    @Schema(description = "会场类型", example = "NORMAL")
    private String venueType;
    
    @Schema(description = "座位行数", example = "10")
    private Integer totalRows;
    
    @Schema(description = "座位列数", example = "20")
    private Integer totalColumns;
    
    @Schema(description = "座位总数", example = "200")
    private Integer capacity;
    
    @Schema(description = "已分配座位数", example = "150")
    private Integer assignedSeats;
    
    @Schema(description = "可用座位数", example = "50")
    private Integer availableSeats;
    
    @Schema(description = "布局数据（JSON格式）")
    private String layoutData;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
