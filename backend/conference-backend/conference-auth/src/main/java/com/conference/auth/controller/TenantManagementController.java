package com.conference.auth.controller;

import com.conference.common.result.Result;
import com.conference.common.result.ResultCode;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.auth.dto.CreateTenantRequest;
import com.conference.auth.dto.TenantUpdateRequest;
import com.conference.auth.entity.SysTenant;
import com.conference.auth.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户管理 Controller
 * 权限: 仅超级管理员(super_admin)可访问
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/tenant")
@Tag(name = "租户管理", description = "多租户系统 - 租户管理接口（仅超级管理员）")
public class TenantManagementController {
    
    private final TenantService tenantService;
    
    /**
     * 验证用户是否为超级管理员
     * @param userId 用户ID
     * @return 是否为超级管理员
     */
    private boolean isSuperAdmin(Long userId) {
        // TODO: 实际应从数据库查询用户角色
        // 这里临时返回true，表示所有操作都允许
        // 实际生产环境应实现完整的权限检查逻辑
        return true;
    }
    
    /**
     * 检查权限并返回错误（如果无权限）
     * @param userId 用户ID
     * @return 如果无权限返回Result，否则返回null
     */
    private Result<Void> checkSuperAdminPermission(Long userId) {
        if (!isSuperAdmin(userId)) {
            log.warn("[权限拒绝] 用户ID:{} 尝试访问超管功能但权限不足", userId);
            return Result.fail(ResultCode.FORBIDDEN, "仅超级管理员可访问租户管理功能");
        }
        return null;
    }
    
    /**
     * 获取所有租户列表（分页）
     * 权限: 超级管理员
     */
    @GetMapping("/list")
    @Operation(summary = "获取租户列表", description = "分页获取所有租户信息，仅超级管理员可访问")
    public Result<Map<String, Object>> listTenants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) String status) {
        
        Long currentUserId = TenantContextHolder.getUserId();
        Long tenantId = TenantContextHolder.getTenantId();
        log.info("[TenantManagementController] 查看租户列表 - userId: {}, tenantId: {}", currentUserId, tenantId);
        log.info("[超管操作] 用户ID:{} 查看租户列表", currentUserId);
        
        // 权限检查
        Result<Void> permissionCheck = checkSuperAdminPermission(currentUserId);
        if (permissionCheck != null) {
            return (Result<Map<String, Object>>) (Object) permissionCheck;
        }
        
        List<SysTenant> tenants = tenantService.listTenants();
        
        // 转换租户对象，将 ID 转换为字符串以避免 JavaScript 大整数精度丢失
        List<Map<String, Object>> tenantList = new ArrayList<>();
        for (SysTenant tenant : tenants) {
            Map<String, Object> tenantMap = new HashMap<>();
            tenantMap.put("id", String.valueOf(tenant.getId()));  // ID 转为字符串
            tenantMap.put("tenantCode", tenant.getTenantCode());
            tenantMap.put("tenantName", tenant.getTenantName());
            tenantMap.put("contactName", tenant.getContactName());
            tenantMap.put("contactPhone", tenant.getContactPhone());
            tenantMap.put("contactEmail", tenant.getContactEmail());
            tenantMap.put("tenantType", tenant.getTenantType());
            tenantMap.put("validStartDate", tenant.getValidStartDate());
            tenantMap.put("validEndDate", tenant.getValidEndDate());
            tenantMap.put("maxUsers", tenant.getMaxUsers());
            tenantMap.put("maxConferences", tenant.getMaxConferences());
            // 转换 storageQuota 从字节到 GB
            Long storageQuota = tenant.getStorageQuota();
            tenantMap.put("storageQuota", storageQuota != null ? storageQuota / (1024 * 1024 * 1024) : null);
            tenantMap.put("status", tenant.getStatus());
            tenantMap.put("domain", tenant.getDomain());
            tenantMap.put("logoUrl", tenant.getLogoUrl());
            tenantMap.put("remark", tenant.getRemark());
            tenantMap.put("createTime", tenant.getCreateTime());
            tenantMap.put("updateTime", tenant.getUpdateTime());
            tenantList.add(tenantMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", tenants.size());
        result.put("items", tenantList);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return Result.ok("获取租户列表成功", result);
    }
    
    /**
     * 获取单个租户详情
     * 权限: 超级管理员
     */
    @GetMapping("/{tenantId}")
    @Operation(summary = "获取租户详情", description = "根据租户ID获取详细信息")
    public Result<Map<String, Object>> getTenantDetail(@PathVariable Long tenantId) {
        Long currentUserId = TenantContextHolder.getUserId();
        log.info("[超管操作] 用户ID:{} 查看租户详情, 租户ID:{}", currentUserId, tenantId);
        
        // 权限检查
        Result<Void> permissionCheck = checkSuperAdminPermission(currentUserId);
        if (permissionCheck != null) {
            return (Result<Map<String, Object>>) (Object) permissionCheck;
        }
        SysTenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null) {
            return Result.fail(ResultCode.NOT_FOUND, "租户不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", String.valueOf(tenant.getId()));  // ID 转为字符串以避免 JavaScript 大整数精度丢失
        result.put("tenantCode", tenant.getTenantCode());
        result.put("tenantName", tenant.getTenantName());
        result.put("contactName", tenant.getContactName());
        result.put("contactPhone", tenant.getContactPhone());
        result.put("contactEmail", tenant.getContactEmail());
        result.put("type", tenant.getTenantType());
        result.put("validStartDate", tenant.getValidStartDate());
        result.put("validEndDate", tenant.getValidEndDate());
        result.put("maxUsers", tenant.getMaxUsers());
        result.put("maxConferences", tenant.getMaxConferences());
        // 转换回 GB 显示给前端
        result.put("storageQuota", tenant.getStorageQuota() != null ? tenant.getStorageQuota() / (1024 * 1024 * 1024) : null);
        result.put("status", tenant.getStatus());
        result.put("domain", tenant.getDomain());
        result.put("logoUrl", tenant.getLogoUrl());
        result.put("remark", tenant.getRemark());
        result.put("createTime", tenant.getCreateTime());
        result.put("updateTime", tenant.getUpdateTime());
        
        return Result.ok("获取租户详情成功", result);
    }
    
    /**
     * 创建新租户
     * 权限: 超级管理员
     */
    @PostMapping("/create")
    @Operation(summary = "创建租户", description = "创建新的租户（需要超级管理员权限）")
    public Result<Map<String, Object>> createTenant(@RequestBody CreateTenantRequest request) {
        Long currentUserId = TenantContextHolder.getUserId();
        log.info("[超管操作] 用户ID:{} 创建租户: {}", currentUserId, request.getTenantName());
        
        // 权限检查
        Result<Void> permissionCheck = checkSuperAdminPermission(currentUserId);
        if (permissionCheck != null) {
            return (Result<Map<String, Object>>) (Object) permissionCheck;
        }
        
        // 验证请求参数
        if (request.getTenantCode() == null || request.getTenantCode().isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR, "租户编码不能为空");
        }
        if (request.getTenantName() == null || request.getTenantName().isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR, "租户名称不能为空");
        }
        
        // 检查租户编码是否已存在
        SysTenant existing = tenantService.getTenantByCode(request.getTenantCode());
        if (existing != null) {
            return Result.fail(ResultCode.DUPLICATE_ERROR, "租户编码已存在");
        }
        
        // 创建租户（直接使用 service 方法）
        SysTenant created = tenantService.createTenant(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", created.getId());
        result.put("tenantCode", created.getTenantCode());
        result.put("tenantName", created.getTenantName());
        result.put("status", created.getStatus());
        
        return Result.ok("租户创建成功", result);
    }
    
    /**
     * 更新租户信息
     * 权限: 超级管理员
     */
    @PutMapping("/{tenantId}")
    @Operation(summary = "更新租户", description = "更新租户信息（需要超级管理员权限）")
    public Result<String> updateTenant(
            @PathVariable Long tenantId,
            @RequestBody TenantUpdateRequest request) {
        
        Long currentUserId = TenantContextHolder.getUserId();
        log.info("[超管操作] 用户ID:{} 更新租户ID:{}", currentUserId, tenantId);
        
        // 权限检查
        Result<Void> permissionCheck = checkSuperAdminPermission(currentUserId);
        if (permissionCheck != null) {
            return (Result<String>) (Object) permissionCheck;
        }
        
        SysTenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null) {
            return Result.fail(ResultCode.NOT_FOUND, "租户不存在");
        }
        
        // 更新字段
        if (request.getTenantName() != null) {
            tenant.setTenantName(request.getTenantName());
        }
        if (request.getStatus() != null) {
            tenant.setStatus(request.getStatus());
        }
        if (request.getContactName() != null) {
            tenant.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            tenant.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            tenant.setContactEmail(request.getContactEmail());
        }
        if (request.getType() != null) {
            tenant.setTenantType(request.getType());
        }
        if (request.getValidStartDate() != null) {
            tenant.setValidStartDate(request.getValidStartDate());
        }
        if (request.getValidEndDate() != null) {
            tenant.setValidEndDate(request.getValidEndDate());
        }
        if (request.getMaxUsers() != null) {
            tenant.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxConferences() != null) {
            tenant.setMaxConferences(request.getMaxConferences());
        }
        if (request.getStorageQuota() != null) {
            // 前端传入的是 GB，需要转换为字节
            tenant.setStorageQuota(request.getStorageQuota() * 1024 * 1024 * 1024);
        }
        
        tenantService.updateTenant(tenant);
        
        return Result.ok("租户更新成功");
    }
    
    /**
     * 删除租户
     * 权限: 超级管理员（谨慎操作）
     */
    @DeleteMapping("/{tenantId}")
    @Operation(summary = "删除租户", description = "删除租户及其所有数据（谨慎操作，仅超级管理员）")
    public Result<String> deleteTenant(@PathVariable Long tenantId) {
        Long currentUserId = TenantContextHolder.getUserId();
        log.warn("[超管操作] 用户ID:{} 删除租户:{}", currentUserId, tenantId);
        
        // 权限检查
        Result<Void> permissionCheck = checkSuperAdminPermission(currentUserId);
        if (permissionCheck != null) {
            return (Result<String>) (Object) permissionCheck;
        }
        
        SysTenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null) {
            return Result.fail(ResultCode.NOT_FOUND, "租户不存在");
        }
        
        tenantService.deleteTenant(tenantId);
        log.warn("[超管操作] 租户:{} 已被删除", tenantId);
        
        return Result.ok("租户删除成功");
    }
    
    /**
     * 启用/禁用租户
     * 权限: 超级管理员
     */
    @PutMapping("/{tenantId}/status")
    @Operation(summary = "切换租户状态", description = "启用或禁用租户")
    public Result<String> toggleTenantStatus(
            @PathVariable Long tenantId,
            @RequestParam String status) {
        
        Long currentUserId = TenantContextHolder.getUserId();
        log.info("[超管操作] 用户ID:{} 修改租户状态:{} -> {}", currentUserId, tenantId, status);
        
        // 权限检查
        Result<Void> permissionCheck = checkSuperAdminPermission(currentUserId);
        if (permissionCheck != null) {
            return (Result<String>) (Object) permissionCheck;
        }
        
        // 状态值：active=1, inactive=0
        Integer statusValue;
        if ("active".equals(status)) {
            statusValue = 1;
        } else if ("inactive".equals(status)) {
            statusValue = 0;
        } else {
            return Result.fail(ResultCode.PARAM_ERROR, "状态参数无效，只支持 active 或 inactive");
        }
        
        SysTenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null) {
            return Result.fail(ResultCode.NOT_FOUND, "租户不存在");
        }
        
        // 设置状态：1=启用, 0=禁用
        tenant.setStatus(statusValue);
        tenantService.updateTenant(tenant);
        
        return Result.ok("租户状态已更新");
    }
    
    /**
     * 获取租户统计数据
     * 权限: 超级管理员
     */
    @GetMapping("/{tenantId}/stats")
    @Operation(summary = "获取租户统计", description = "获取租户的用户数、会议数等统计信息")
    public Result<Map<String, Object>> getTenantStats(@PathVariable Long tenantId) {
        Long currentUserId = TenantContextHolder.getUserId();
        log.info("[超管操作] 用户ID:{} 查看租户统计, 租户ID:{}", currentUserId, tenantId);
        
        // 权限检查
        Result<Void> permissionCheck = checkSuperAdminPermission(currentUserId);
        if (permissionCheck != null) {
            return (Result<Map<String, Object>>) (Object) permissionCheck;
        }
        
        SysTenant tenant = tenantService.getTenantById(tenantId);
        if (tenant == null) {
            return Result.fail(ResultCode.NOT_FOUND, "租户不存在");
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("tenantId", tenantId);
        stats.put("tenantName", tenant.getTenantName());
        stats.put("userCount", 0);  // TODO: 实际获取用户数
        stats.put("conferenceCount", 0);  // TODO: 实际获取会议数
        stats.put("registrationCount", 0);  // TODO: 实际获取注册数
        stats.put("lastActivity", null);  // TODO: 实际获取最后活动时间
        
        return Result.ok("获取租户统计成功", stats);
    }
}
