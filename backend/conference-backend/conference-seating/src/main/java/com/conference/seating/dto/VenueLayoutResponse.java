package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 会场布局响应 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "会场布局响应")
public class VenueLayoutResponse {
    
    @Schema(description = "会场ID")
    private Long venueId;
    
    @Schema(description = "会场名称")
    private String venueName;
    
    @Schema(description = "总行数")
    private Integer totalRows;
    
    @Schema(description = "总列数")
    private Integer totalColumns;
    
    @Schema(description = "座位列表")
    private List<?> seats;
}
