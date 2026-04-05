package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核规则实体
 */
@Data
@TableName("registration_audit_rule")
public class RegistrationAuditRule {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 会议 ID
     */
    @TableField("conference_id")
    private Long conferenceId;

    /**
     * 租户 ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则类型：auto_approve-自动通过, auto_reject-自动拒绝, manual-人工审核
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 规则条件（JSON格式）
     */
    @TableField("rule_condition")
    private String ruleCondition;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 是否启用：0-禁用, 1-启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
