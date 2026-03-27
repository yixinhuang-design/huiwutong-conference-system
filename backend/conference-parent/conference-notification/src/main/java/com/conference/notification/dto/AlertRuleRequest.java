package com.conference.notification.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预警规则请求DTO
 */
@Data
public class AlertRuleRequest {

    /** 规则ID(更新时提供) */
    private Long id;

    /** 会议ID */
    private Long conferenceId;

    /** 规则名称 */
    private String name;

    /** 监控指标: registrationRate/checkinRate/taskCompletionRate/apiResponseTime/systemErrorRate */
    private String metric;

    /** 比较运算符: </>/<=/>=/== */
    private String operator;

    /** 阈值 */
    private BigDecimal threshold;

    /** 级别: high/medium/low */
    private String severity;

    /** 短信通知 */
    private Boolean notifySms;

    /** 系统通知 */
    private Boolean notifySystem;
}
