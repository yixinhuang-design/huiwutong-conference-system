package com.conference.meeting.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 日程设置请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSettingsRequest {

    /**
     * 是否需要报到
     */
    @JsonAlias("need_report")
    private Boolean needReport;

    /**
     * 报到方式
     */
    @JsonAlias("report_method")
    private String reportMethod;

    /**
     * 报到说明
     */
    @JsonAlias("report_description")
    private String reportDescription;

    /**
     * 是否需要签到
     */
    @JsonAlias("need_checkin")
    private Boolean needCheckin;

    /**
     * 签到方式
     */
    @JsonAlias("checkin_method")
    private String checkinMethod;

    /**
     * 签到说明
     */
    @JsonAlias("checkin_description")
    private String checkinDescription;

    /**
     * 是否需要提醒
     */
    @JsonAlias("need_reminder")
    private Boolean needReminder;

    /**
     * 提醒对象
     */
    @JsonAlias("reminder_target")
    private String reminderTarget;

    /**
     * 提醒时间（分钟）
     */
    @JsonAlias("reminder_time")
    private Integer reminderTime;

    /**
     * 提醒方式列表
     */
    @JsonAlias("reminder_methods")
    private List<String> reminderMethods;

    /**
     * 是否允许更改地点
     */
    @JsonAlias("allow_change_location")
    private Boolean allowChangeLocation;

    /**
     * 是否自动播放
     */
    @JsonAlias("auto_broadcast")
    private Boolean autoBroadcast;

    /**
     * 直播地址
     */
    @JsonAlias("broadcast_url")
    private String broadcastUrl;
}
