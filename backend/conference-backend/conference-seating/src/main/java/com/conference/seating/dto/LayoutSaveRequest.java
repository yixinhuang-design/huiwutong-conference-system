package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 座位布局整体保存请求 DTO
 * 前端将整个座位区配置（含座位矩阵）一次性提交
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "座位布局整体保存请求")
public class LayoutSaveRequest {

    @NotNull(message = "会议ID不能为空")
    @Schema(description = "会议ID")
    private Long conferenceId;

    @Schema(description = "日程ID（可选，用于多日程隔离）")
    private Long scheduleId;

    @Schema(description = "座位区列表")
    private List<AreaData> areas;

    @Schema(description = "参会人员列表（含座位分配）")
    private List<AttendeeData> attendees;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "座位区数据")
    public static class AreaData {
        @Schema(description = "区域ID（前端生成）")
        private String areaId;

        @Schema(description = "区域名称")
        private String name;

        @Schema(description = "区域类型：stage/vip/regular/banner/enterprise")
        private String type;

        @Schema(description = "会场类型：normal/ushape/round")
        private String venueType;

        @Schema(description = "行数")
        private Integer rows;

        @Schema(description = "列数")
        private Integer cols;

        @Schema(description = "旋转角度")
        private Integer angle;

        @Schema(description = "编号模式")
        private String numberingMode;

        @Schema(description = "起始编号")
        private Integer startNumber;

        @Schema(description = "布局JSON（gridLayout、aisleConfig等前端配置原样存储）")
        private String layoutJson;

        @Schema(description = "座位列表")
        private List<SeatData> seats;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "单个座位数据")
    public static class SeatData {
        @Schema(description = "座位前端ID")
        private String seatId;

        @Schema(description = "行号")
        private Integer row;

        @Schema(description = "列号")
        private Integer col;

        @Schema(description = "座位编号")
        private String seatNumber;

        @Schema(description = "座位类型：normal/vip/reserved/aisle")
        private String seatType;

        @Schema(description = "状态：available/occupied/aisle")
        private String status;

        @Schema(description = "分配的参会人员前端ID")
        private String occupantId;

        @Schema(description = "分配的参会人员名称")
        private String occupantName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "参会人员数据")
    public static class AttendeeData {
        @Schema(description = "前端人员ID")
        private String attendeeId;

        @Schema(description = "姓名")
        private String name;

        @Schema(description = "部门")
        private String department;

        @Schema(description = "职位")
        private String position;

        @Schema(description = "单位")
        private String company;

        @Schema(description = "是否VIP")
        private Boolean isVip;

        @Schema(description = "手机号")
        private String phone;
    }
}
