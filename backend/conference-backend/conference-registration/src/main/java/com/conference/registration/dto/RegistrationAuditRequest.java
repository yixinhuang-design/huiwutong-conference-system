package com.conference.registration.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 报名审核请求 DTO
 */
@Data
public class RegistrationAuditRequest {
    
    /**
     * 报名 ID
     */
    @NotNull(message = "报名ID不能为空")
    private Long registrationId;
    
    /**
     * 审核结果：approved-通过, rejected-拒绝
     */
    @NotBlank(message = "审核结果不能为空")
    private String auditResult;
    
    /**
     * 审核备注
     */
    private String remark;
}
