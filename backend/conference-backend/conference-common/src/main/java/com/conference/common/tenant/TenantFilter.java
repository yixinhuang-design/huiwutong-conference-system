package com.conference.common.tenant;

import com.conference.common.util.JwtUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 租户信息拦截器
 * 从 JWT Token 中提取租户 ID 并设置到上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter implements Filter {
    
    private final JwtUtils jwtUtils;
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        
        try {
            String uri = request.getRequestURI();
            log.info("[TenantFilter] 处理请求: {} {}", request.getMethod(), uri);
            
            // OPTIONS 预检请求直接放行，不做租户解析
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                log.info("[TenantFilter] OPTIONS预检请求，直接放行");
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            
            // 非API路径直接放行（根路径、favicon、actuator等）
            if (!uri.startsWith("/api/")) {
                log.debug("[TenantFilter] 非API路径，跳过租户解析: {}", uri);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            
            // 从 Authorization Header 中提取 Token
            String authHeader = request.getHeader("Authorization");
            log.info("[TenantFilter] Authorization Header: {}", authHeader);
            
            if (StringUtils.hasText(authHeader)) {
                log.info("[TenantFilter] 开始提取Token...");
                String token = jwtUtils.extractTokenFromHeader(authHeader);
                log.info("[TenantFilter] 提取到的Token: {}", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");
                
                if (StringUtils.hasText(token)) {
                    log.info("[TenantFilter] 开始验证Token...");
                    boolean isValid = jwtUtils.validateToken(token);
                    log.info("[TenantFilter] Token验证结果: {}", isValid);
                    
                    if (isValid) {
                        // 从 Token 中提取租户 ID 和用户 ID
                        Long tenantId = jwtUtils.getTenantIdFromToken(token);
                        Long userId = jwtUtils.getUserIdFromToken(token);
                        
                        log.info("[TenantFilter] 从Token提取userId: {}, tenantId: {}", userId, tenantId);
                        
                        // 设置到上下文
                        if (tenantId != null) {
                            TenantContextHolder.setTenantId(tenantId);
                            log.info("[TenantFilter] ✅ 已设置tenantId到Context: {}", tenantId);
                        } else {
                            log.error("[TenantFilter] ❌ Token中tenantId为null！Token claims: {}", token);
                        }
                        if (userId != null) {
                            TenantContextHolder.setUserId(userId);
                            log.info("[TenantFilter] 已设置userId到Context: {}", userId);
                        } else {
                            log.warn("[TenantFilter] Token中userId为null，跳过设置");
                        }
                        
                        log.info("[TenantFilter] 租户上下文已设置 - TenantID: {}, UserID: {}", tenantId, userId);
                    } else {
                        log.warn("[TenantFilter] Token验证失败");
                    }
                } else {
                    log.warn("[TenantFilter] Token为空");
                }
            } else {
                log.warn("[TenantFilter] Authorization Header为空");
            }
            
            // 回退：如果 JWT 没有设置 tenantId，则从 X-Tenant-Id header 读取
            if (TenantContextHolder.getTenantId() == null) {
                String tenantIdHeader = request.getHeader("X-Tenant-Id");
                if (StringUtils.hasText(tenantIdHeader)) {
                    try {
                        Long headerTenantId = Long.parseLong(tenantIdHeader.trim());
                        TenantContextHolder.setTenantId(headerTenantId);
                        log.info("[TenantFilter] ✅ 从X-Tenant-Id header设置tenantId: {}", headerTenantId);
                    } catch (NumberFormatException e) {
                        log.warn("[TenantFilter] X-Tenant-Id header值无效: {}", tenantIdHeader);
                    }
                } else {
                    log.warn("[TenantFilter] 未能设置tenantId：无JWT Token也无X-Tenant-Id header");
                }
            }
            
            // 继续处理请求
            filterChain.doFilter(servletRequest, servletResponse);
            
        } finally {
            // 请求处理完成后，清除上下文
            TenantContextHolder.clear();
        }
    }
}
