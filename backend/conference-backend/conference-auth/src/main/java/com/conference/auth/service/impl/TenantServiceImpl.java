package com.conference.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.auth.dto.CreateTenantRequest;
import com.conference.auth.entity.SysTenant;
import com.conference.auth.mapper.SysTenantMapper;
import com.conference.auth.service.TenantService;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户管理服务实现
 */
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    
    private final SysTenantMapper tenantMapper;
    
    @Override
    @Transactional
    public SysTenant createTenant(CreateTenantRequest request) {
        // 检查租户编码是否已存在
        SysTenant existing = tenantMapper.selectOne(
            new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getTenantCode, request.getTenantCode())
        );
        
        if (existing != null) {
            throw new BusinessException(ResultCode.TENANT_CODE_EXIST);
        }
        
        // 创建新租户
        SysTenant tenant = new SysTenant();
        // 基本信息
        tenant.setTenantName(request.getTenantName());
        tenant.setTenantCode(request.getTenantCode());
        tenant.setContactName(request.getContactName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setContactEmail(request.getContactEmail());
        tenant.setStatus(request.getStatus() != null ? request.getStatus() : 1); // 默认启用
        
        // 客户参数
        tenant.setTenantType(request.getType());
        tenant.setValidStartDate(request.getValidStartDate());
        tenant.setValidEndDate(request.getValidEndDate());
        tenant.setMaxUsers(request.getMaxUsers());
        tenant.setMaxConferences(request.getMaxConferences());
        // 前端传入的是 GB，需要转换为字节存储
        if (request.getStorageQuota() != null) {
            tenant.setStorageQuota(request.getStorageQuota() * 1024 * 1024 * 1024);
        }
        
        tenant.setCreateTime(LocalDateTime.now());
        
        tenantMapper.insert(tenant);
        
        return tenant;
    }
    
    @Override
    public List<SysTenant> listTenants() {
        return tenantMapper.selectList(
            new LambdaQueryWrapper<SysTenant>()
                .orderByDesc(SysTenant::getCreateTime)
        );
    }
    
    @Override
    public SysTenant getTenantById(Long tenantId) {
        return tenantMapper.selectById(tenantId);
    }
    
    @Override
    public SysTenant getTenantByCode(String tenantCode) {
        return tenantMapper.selectOne(
            new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getTenantCode, tenantCode)
        );
    }
    
    @Override
    @Transactional
    public void updateTenant(SysTenant tenant) {
        tenant.setUpdateTime(LocalDateTime.now());
        tenantMapper.updateById(tenant);
    }
    
    @Override
    @Transactional
    public void deleteTenant(Long tenantId) {
        tenantMapper.deleteById(tenantId);
    }
}
