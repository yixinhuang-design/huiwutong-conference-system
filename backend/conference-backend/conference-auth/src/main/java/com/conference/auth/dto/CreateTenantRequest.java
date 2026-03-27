package com.conference.auth.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 创建租户请求 DTO
 */
@Data
public class CreateTenantRequest {
    
    // 基本信息（步骤1）
    private String tenantName;           // 客户名称
    private String tenantCode;           // 客户编码（唯一）
    private String contactName;          // 管理员名称
    private String contactPhone;         // 登录号码（登录用户名）
    private String contactEmail;         // 邮箱
    private Integer status;              // 状态：1=启用, 0=禁用
    
    // 客户参数（步骤2）
    private String type;                 // 客户类型：self-rent/full-pay
    private LocalDate validStartDate;    // 使用期限开始日期
    private LocalDate validEndDate;      // 使用期限结束日期
    private Integer maxUsers;            // 最大用户数限制
    private Integer maxConferences;      // 最大会议数限制
    private Long storageQuota;           // 存储配额(GB)
}


