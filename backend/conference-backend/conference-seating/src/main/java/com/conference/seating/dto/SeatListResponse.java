package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 座位列表响应 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "座位列表响应")
public class SeatListResponse {
    
    @Schema(description = "会场ID")
    private Long venueId;
    
    @Schema(description = "座位总数")
    private Integer totalSeats;
    
    @Schema(description = "已分配座位数")
    private Integer assignedSeats;
}
