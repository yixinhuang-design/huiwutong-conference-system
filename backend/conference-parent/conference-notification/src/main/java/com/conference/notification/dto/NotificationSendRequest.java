package com.conference.notification.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 发送通知请求DTO
 */
@Data
public class NotificationSendRequest {

    /** 会议ID */
    private Long conferenceId;

    /** 通知类型: conference/registration/checkin/schedule/seat/bus/accommodation/full/custom */
    private String type;

    /** 模板ID(可选) */
    private Long templateId;

    /** 通知标题 */
    private String title;

    /** 通知内容(支持{变量}替换) */
    private String content;

    /** 发送渠道: ["sms","wechat","enterprise","qrcode"] */
    private List<String> channels;

    /** 受众筛选条件 */
    private Map<String, Object> filters;

    /** 接收人数 */
    private Integer recipientCount;

    /** 发送时机: now/delayed/scheduled */
    private String sendTiming;

    /** 定时发送时间(yyyy-MM-dd HH:mm:ss) */
    private String scheduledTime;

    /** 延迟分钟数(当sendTiming=delayed时) */
    private Integer delayedMinutes;

    /** 延迟单位: minutes/hours */
    private String delayedUnit;

    /** 发送选项 */
    private Map<String, Object> options;
}
