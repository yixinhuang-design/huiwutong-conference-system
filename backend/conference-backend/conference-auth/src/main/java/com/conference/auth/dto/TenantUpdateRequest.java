package com.conference.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * 更新租户请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUpdateRequest {
    
    // 基本信息
    private String tenantName;           // 客户名称
    private String contactName;          // 管理员名称
    private String contactPhone;         // 登录号码
    private String contactEmail;         // 邮箱
    private Integer status;              // 状态：1=启用, 0=禁用
    
    // 客户参数
    private String type;                 // 客户类型
    private LocalDate validStartDate;    // 使用期限开始日期
    private LocalDate validEndDate;      // 使用期限结束日期
    private Integer maxUsers;            // 最大用户数
    private Integer maxConferences;      // 最大会议数
    private Long storageQuota;           // 存储配额
}
