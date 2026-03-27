package com.conference.meeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程设置实体类
 * 对应表: conf_schedule_settings
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_schedule_settings")
public class ScheduleSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 日程ID
     */
    @TableField("schedule_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 会议ID
     */
    @TableField("meeting_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long meetingId;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    /**
     * 是否需要报到
     */
    @TableField("need_report")
    private Integer needReport;

    /**
     * 报到方式：print（打印表单签字）、qrcode（二维码扫码）
     */
    @TableField("report_method")
    private String reportMethod;

    /**
     * 报到说明文字
     */
    @TableField("report_description")
    private String reportDescription;

    /**
     * 是否需要签到
     */
    @TableField("need_checkin")
    private Integer needCheckin;

    /**
     * 签到方式：print（打印表单）、qrcode（二维码扫码）
     */
    @TableField("checkin_method")
    private String checkinMethod;

    /**
     * 签到说明文字
     */
    @TableField("checkin_description")
    private String checkinDescription;

    /**
     * 是否需要提醒
     */
    @TableField("need_reminder")
    private Integer needReminder;

    /**
     * 提醒对象：staff（工作人员）、all（全体人员）、user（用户自主设置）
     */
    @TableField("reminder_target")
    private String reminderTarget;

    /**
     * 提前提醒时间（分钟）
     */
    @TableField("reminder_time")
    private Integer reminderTime;

    /**
     * 提醒方式数组：["app", "sms", "wechat"]
     */
    @TableField("reminder_methods")
    private String reminderMethods;

    /**
     * 是否允许更改地点
     */
    @TableField("allow_change_location")
    private Integer allowChangeLocation;

    /**
     * 是否自动播放日程
     */
    @TableField("auto_broadcast")
    private Integer autoBroadcast;

    /**
     * 直播地址
     */
    @TableField("broadcast_url")
    private String broadcastUrl;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    /**
     * 删除标志
     */
    @TableField("deleted")
    private Integer deleted;

    /**
     * 将reminder_methods字符串转换为List
     */
    @TableField(exist = false)
    private List<String> reminderMethodList;
}
