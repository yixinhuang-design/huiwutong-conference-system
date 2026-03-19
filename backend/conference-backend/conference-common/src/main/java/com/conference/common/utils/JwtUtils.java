package com.conference.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
public class JwtUtils {
    
    /** 默认密钥（生产环境应从配置中读取） */
    private static final String DEFAULT_SECRET = "ConferenceSystemJwtSecretKey2026ForSmartMeetingSystem";
    
    /** 默认过期时间：24小时 */
    private static final long DEFAULT_EXPIRATION = 24 * 60 * 60 * 1000L;
    
    /** 刷新Token过期时间：7天 */
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    
    /**
     * 生成Token
     * @param subject 主题（通常是用户ID）
     * @param claims 自定义声明
     * @return JWT Token
     */
    public static String generateToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, DEFAULT_EXPIRATION);
    }
    
    /**
     * 生成Token
     * @param subject 主题
     * @param claims 自定义声明
     * @param expiration 过期时间（毫秒）
     * @return JWT Token
     */
    public static String generateToken(String subject, Map<String, Object> claims, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        SecretKey key = Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * 生成刷新Token
     */
    public static String generateRefreshToken(String subject) {
        return generateToken(subject, Map.of("type", "refresh"), REFRESH_EXPIRATION);
    }
    
    /**
     * 解析Token
     * @param token JWT Token
     * @return Claims
     */
    public static Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("Token解析失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 获取Token中的用户ID
     */
    public static String getSubject(String token) {
        return parseToken(token).getSubject();
    }
    
    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证Token是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 从Token获取租户ID
     */
    public static Long getTenantId(String token) {
        Claims claims = parseToken(token);
        Object tenantId = claims.get("tenantId");
        if (tenantId != null) {
            return Long.valueOf(tenantId.toString());
        }
        return null;
    }
    
    /**
     * 从Token获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }
}
