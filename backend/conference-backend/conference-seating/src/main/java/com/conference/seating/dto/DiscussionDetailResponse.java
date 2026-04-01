package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 讨论室详情响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "讨论室详情响应")
public class DiscussionDetailResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "会议ID")
    private Long conferenceId;

    @Schema(description = "讨论室名称")
    private String roomName;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "容纳人数")
    private Integer capacity;

    @Schema(description = "已分配人数")
    private Integer assignedCount;

    @Schema(description = "可用名额")
    private Integer availableSeats;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "描述/议题")
    private String description;

    @Schema(description = "已分配人员列表")
    private List<AssignedAttendeeResponse> assignees;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
