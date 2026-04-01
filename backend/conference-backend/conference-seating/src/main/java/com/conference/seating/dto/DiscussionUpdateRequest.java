package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 讨论室更新请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "讨论室更新请求")
public class DiscussionUpdateRequest {

    @Schema(description = "讨论室名称", example = "第一讨论室")
    private String roomName;

    @Schema(description = "位置", example = "三楼会议区")
    private String location;

    @Min(value = 1, message = "容纳人数至少1人")
    @Schema(description = "容纳人数", example = "15")
    private Integer capacity;

    @Schema(description = "开始时间", example = "2026-03-12T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-03-12T11:00:00")
    private LocalDateTime endTime;

    @Schema(description = "描述/议题", example = "技术方案讨论")
    private String description;
}
