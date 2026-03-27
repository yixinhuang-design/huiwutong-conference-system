package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会场座位统计 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "会场座位统计")
public class VenueSeatStatsDto {
    
    @Schema(description = "会场ID", example = "1")
    private Long venueId;
    
    @Schema(description = "座位总数", example = "200")
    private Integer totalSeats;
    
    @Schema(description = "已分配座位数", example = "150")
    private Integer assignedSeats;
    
    @Schema(description = "可用座位数", example = "50")
    private Integer availableSeats;
    
    @Schema(description = "保留座位数", example = "5")
    private Integer reservedSeats;
    
    @Schema(description = "过道数", example = "4")
    private Integer aisleCount;
    
    @Schema(description = "VIP座位数", example = "20")
    private Integer vipSeats;
}
