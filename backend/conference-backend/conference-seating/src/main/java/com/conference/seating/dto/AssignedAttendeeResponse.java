package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已分配人员响应 DTO（所有辅助安排通用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "已分配人员信息")
public class AssignedAttendeeResponse {

    @Schema(description = "分配记录ID")
    private Long assignId;

    @Schema(description = "参会人员ID")
    private Long attendeeId;

    @Schema(description = "姓名")
    private String attendeeName;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "分配时间")
    private LocalDateTime createdAt;
}
