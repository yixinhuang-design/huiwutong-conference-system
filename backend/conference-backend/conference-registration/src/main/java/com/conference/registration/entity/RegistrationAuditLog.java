package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核日志实体
 */
@Data
@TableName("registration_audit_log")
public class RegistrationAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 报名 ID
     */
    @TableField("registration_id")
    private Long registrationId;

    /**
     * 审核人 ID
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核人姓名
     */
    @TableField("auditor_name")
    private String auditorName;

    /**
     * 审核动作：approve-通过, reject-拒绝, return-退回
     */
    @TableField("action")
    private String action;

    /**
     * 审核备注
     */
    @TableField("comment")
    private String comment;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
