package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名审核记录表
 */
@Data
@TableName("registration_audit")
public class RegistrationAudit {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 报名 ID
     */
    @TableField("registration_id")
    private Long registrationId;
    
    /**
     * 租户 ID
     */
    @TableField("tenant_id")
    private Long tenantId;
    
    /**
     * 审核人 ID
     */
    @TableField("auditor_id")
    private Long auditorId;
    
    /**
     * 审核结果：approved-通过, rejected-拒绝, pending-待审核
     */
    @TableField("audit_result")
    private String auditResult;
    
    /**
     * 审核备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 审核时间
     */
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    /**
     * 审核方式：auto-自动审核, manual-人工审核
     */
    @TableField("audit_method")
    private String auditMethod;

    @TableField("deleted")
    private Integer deleted;
}
