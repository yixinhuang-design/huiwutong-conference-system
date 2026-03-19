package com.conference.common.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtils {
    
    @Value("${jwt.secret:ConferenceSystemJwtSecretKey2026ForSmartMeetingSystem}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration; // 24小时
    
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration; // 7天
    
    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, Long tenantId, String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("tenantId", tenantId);
        payload.put("username", username);
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("exp", (System.currentTimeMillis() + expiration) / 1000);
        
        return JWTUtil.createToken(payload, JWTSignerUtil.hs384(secret.getBytes()));
    }
    
    /**
     * 生成刷新 Token
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("type", "refresh");
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("exp", (System.currentTimeMillis() + refreshExpiration) / 1000);
        
        return JWTUtil.createToken(payload, JWTSignerUtil.hs384(secret.getBytes()));
    }
    
    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Object userId = jwt.getPayload("userId");
            if (userId != null) {
                return Long.parseLong(userId.toString());
            }
        } catch (Exception e) {
            log.error("Failed to extract userId from token: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 从 Token 中获取租户 ID
     */
    public Long getTenantIdFromToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Object tenantId = jwt.getPayload("tenantId");
            if (tenantId != null) {
                return Long.parseLong(tenantId.toString());
            }
        } catch (Exception e) {
            log.error("Failed to extract tenantId from token: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            return (String) jwt.getPayload("username");
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 验证 Token 是否有效
     */
    public Boolean validateToken(String token) {
        try {
            return JWTUtil.verify(token, JWTSignerUtil.hs384(secret.getBytes()));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查 Token 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Long exp = (Long) jwt.getPayload("exp");
            return exp != null && exp * 1000 < System.currentTimeMillis();
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 从 Authorization Header 中提取 Token
     * 支持两种格式：
     * 1. "Bearer <token>"
     * 2. 直接的 token 值
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }
        
        // 支持 "Bearer " 前缀格式
        if (authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        
        // 支持直接 token 格式（不需要 Bearer 前缀）
        String token = authHeader.trim();
        return token.isEmpty() ? null : token;
    }
}



