package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 住宿安排详情响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "住宿安排详情响应")
public class AccommodationDetailResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "会议ID")
    private Long conferenceId;

    @Schema(description = "酒店名称")
    private String hotelName;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "房间类型")
    private String roomType;

    @Schema(description = "房间容量")
    private Integer capacity;

    @Schema(description = "已分配床位数")
    private Integer assignedCount;

    @Schema(description = "可用床位数")
    private Integer availableBeds;

    @Schema(description = "入住时间")
    private LocalDateTime checkInTime;

    @Schema(description = "退房时间")
    private LocalDateTime checkOutTime;

    @Schema(description = "已分配人员列表")
    private List<AssignedAttendeeResponse> assignees;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
