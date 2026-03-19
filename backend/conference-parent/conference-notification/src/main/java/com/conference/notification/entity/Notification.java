package com.conference.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知记录实体
 */
@Data
@TableName("conf_notification")
public class Notification {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long conferenceId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 通知类型 */
    private String type;

    private String title;

    private String content;

    /** 发送渠道JSON: ["sms","wechat"] */
    private String channels;

    /** 受众筛选条件JSON */
    private String recipientFilter;

    /** 接收人数 */
    private Integer recipientCount;

    /** 发送时机: now/delayed/scheduled */
    private String sendTiming;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;

    /** 状态: draft/pending/sending/sent/failed */
    private String status;

    private Integer sentCount;

    private Integer deliveredCount;

    private Integer readCount;

    private Integer failedCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentTime;

    /** 发送选项JSON */
    private String options;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer deleted;
}
