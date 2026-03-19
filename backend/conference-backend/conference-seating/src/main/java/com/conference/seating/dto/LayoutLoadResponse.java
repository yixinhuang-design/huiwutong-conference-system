package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 座位布局整体加载响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "座位布局整体加载响应")
public class LayoutLoadResponse {

    @Schema(description = "会议ID")
    private Long conferenceId;

    @Schema(description = "日程ID")
    private Long scheduleId;

    @Schema(description = "座位区列表")
    private List<LayoutSaveRequest.AreaData> areas;

    @Schema(description = "参会人员列表")
    private List<LayoutSaveRequest.AttendeeData> attendees;

    @Schema(description = "保存时间")
    private String savedAt;
}
