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
 * 会场创建请求 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "会场创建请求")
public class VenueCreateRequest {
    
    @NotNull(message = "会议ID不能为空")
    @Schema(description = "会议ID", example = "1")
    private Long conferenceId;
    
    @NotBlank(message = "会场名称不能为空")
    @Schema(description = "会场名称", example = "主会议厅")
    private String venueName;
    
    @NotBlank(message = "会场类型不能为空")
    @Schema(description = "会场类型", example = "NORMAL|USHAPE|ROUND|THEATER")
    private String venueType;
    
    @NotNull(message = "行数不能为空")
    @Min(value = 1, message = "行数至少为1")
    @Schema(description = "座位行数", example = "10")
    private Integer totalRows;
    
    @NotNull(message = "列数不能为空")
    @Min(value = 1, message = "列数至少为1")
    @Schema(description = "座位列数", example = "20")
    private Integer totalColumns;
}
