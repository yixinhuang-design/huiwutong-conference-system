package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会场更新请求 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "会场更新请求")
public class VenueUpdateRequest {
    
    @Schema(description = "会场名称", example = "主会议厅")
    private String venueName;
    
    @Schema(description = "会场类型", example = "NORMAL|USHAPE|ROUND|THEATER")
    private String venueType;
    
    @Min(value = 1, message = "行数至少为1")
    @Schema(description = "座位行数", example = "10")
    private Integer totalRows;
    
    @Min(value = 1, message = "列数至少为1")
    @Schema(description = "座位列数", example = "20")
    private Integer totalColumns;
}
