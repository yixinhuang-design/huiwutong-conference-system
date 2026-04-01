package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 住宿安排创建请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "住宿安排创建请求")
public class AccommodationCreateRequest {

    @NotNull(message = "会议ID不能为空")
    @Schema(description = "会议ID", example = "1")
    private Long conferenceId;

    @Schema(description = "酒店名称", example = "国际大酒店")
    private String hotelName;

    @NotBlank(message = "房间号不能为空")
    @Schema(description = "房间号", example = "101")
    private String roomNumber;

    @Schema(description = "房间类型(SINGLE, DOUBLE, SUITE)", example = "DOUBLE")
    private String roomType;

    @NotNull(message = "房间容量不能为空")
    @Min(value = 1, message = "房间容量最少为1")
    @Schema(description = "房间容量", example = "2")
    private Integer capacity;

    @Schema(description = "入住时间", example = "2026-03-12T14:00:00")
    private LocalDateTime checkInTime;

    @Schema(description = "退房时间", example = "2026-03-14T12:00:00")
    private LocalDateTime checkOutTime;
}
