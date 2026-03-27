package com.conference.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建租户请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantCreateRequest {
    
    /**
     * 租户编码（唯一）
     */
    private String tenantCode;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 租户描述
     */
    private String description;
}
