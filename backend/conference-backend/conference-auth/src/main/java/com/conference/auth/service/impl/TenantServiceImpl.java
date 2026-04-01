package com.conference.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.auth.dto.CreateTenantRequest;
import com.conference.auth.entity.SysRole;
import com.conference.auth.entity.SysTenant;
import com.conference.auth.entity.SysUser;
import com.conference.auth.entity.SysUserRole;
import com.conference.auth.mapper.SysRoleMapper;
import com.conference.auth.mapper.SysTenantMapper;
import com.conference.auth.mapper.SysUserMapper;
import com.conference.auth.mapper.SysUserRoleMapper;
import com.conference.auth.service.TenantService;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    
    private final SysTenantMapper tenantMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    
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
        
        // 自动为新租户创建默认管理员账户
        createDefaultAdmin(tenant);
        
        return tenant;
    }
    
    /**
     * 为新租户创建默认管理员账户
     * 用户名: admin  密码: Admin@123456
     */
    private void createDefaultAdmin(SysTenant tenant) {
        try {
            SysUser admin = new SysUser();
            admin.setTenantId(tenant.getId());
            admin.setUsername("admin");
            admin.setPassword(PASSWORD_ENCODER.encode("Admin@123456"));
            admin.setRealName(tenant.getContactName() != null ? tenant.getContactName() : "租户管理员");
            admin.setPhone(tenant.getContactPhone());
            admin.setEmail(tenant.getContactEmail());
            admin.setUserType("tenant_admin");
            admin.setStatus(1);
            admin.setDeleted(0);
            admin.setCreateTime(LocalDateTime.now());
            userMapper.insert(admin);
            
            // 查找 admin 角色并关联（新租户的管理员用 admin 角色）
            SysRole adminRole = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleCode, "admin")
                    .last("LIMIT 1")
            );
            if (adminRole != null) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(admin.getId());
                userRole.setRoleId(adminRole.getId());
                userRoleMapper.insert(userRole);
            }
            
            log.info("[租户创建] 已为租户 {} ({}) 创建默认管理员账户 admin", 
                    tenant.getTenantName(), tenant.getTenantCode());
        } catch (Exception e) {
            log.error("[租户创建] 创建默认管理员失败, tenantId={}, error={}", tenant.getId(), e.getMessage(), e);
            // 不抛出异常，租户创建不因管理员创建失败而回滚
        }
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
