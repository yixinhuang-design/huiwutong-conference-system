package com.conference.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则实体
 */
@Data
@TableName("conf_alert_rule")
public class AlertRule {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long conferenceId;

    private String name;

    /** 监控指标: registrationRate/checkinRate/taskCompletionRate/apiResponseTime/systemErrorRate */
    private String metric;

    /** 比较运算符: </>/<=/>=/== */
    private String operator;

    private BigDecimal threshold;

    /** 级别: high/medium/low */
    private String severity;

    /** 短信通知: 0=否, 1=是 */
    private Integer notifySms;

    /** 系统通知: 0=否, 1=是 */
    private Integer notifySystem;

    /** 启用状态: 0=禁用, 1=启用 */
    private Integer enabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer deleted;
}
