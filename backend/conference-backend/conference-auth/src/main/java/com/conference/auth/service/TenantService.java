package com.conference.auth.service;

import com.conference.auth.dto.CreateTenantRequest;
import com.conference.auth.entity.SysTenant;
import java.util.List;

/**
 * 租户管理服务
 */
public interface TenantService {
    
    /**
     * 创建租户
     */
    SysTenant createTenant(CreateTenantRequest request);
    
    /**
     * 获取租户列表
     */
    List<SysTenant> listTenants();
    
    /**
     * 获取租户详情
     */
    SysTenant getTenantById(Long tenantId);
    
    /**
     * 根据租户编码获取租户
     */
    SysTenant getTenantByCode(String tenantCode);
    
    /**
     * 更新租户
     */
    void updateTenant(SysTenant tenant);
    
    /**
     * 删除租户
     */
    void deleteTenant(Long tenantId);
}
