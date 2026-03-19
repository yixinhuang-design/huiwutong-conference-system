package com.conference.gateway.filter;

import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局认证过滤器
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String SECRET = "ConferenceSystemJwtSecretKey2026ForSmartMeetingSystem";
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> whiteList;

    public AuthGlobalFilter(Environment environment) {
        this.whiteList = Binder.get(environment)
                .bind("gateway.white-list", Bindable.listOf(String.class))
                .orElse(Collections.emptyList());
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // 白名单路径直接放行
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }
        
        // 获取Token
        String token = getToken(request);
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "未提供认证Token");
        }
        
        // 验证Token
        try {
            Claims claims = parseToken(token);
            
            // 将用户信息添加到请求头，传递给下游服务
            ServerHttpRequest newRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-Tenant-Id", String.valueOf(claims.get("tenantId")))
                    .header("X-Username", String.valueOf(claims.get("username")))
                    .build();
            
            return chain.filter(exchange.mutate().request(newRequest).build());
            
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return unauthorized(exchange, "Token已过期，请重新登录");
        } catch (JwtException e) {
            log.warn("Token无效: {}", e.getMessage());
            return unauthorized(exchange, "Token无效");
        }
    }
    
    @Override
    public int getOrder() {
        return -100; // 优先级最高
    }
    
    /**
     * 判断是否是白名单路径
     */
    private boolean isWhitePath(String path) {
        if (whiteList == null || whiteList.isEmpty()) {
            return false;
        }
        return whiteList.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    /**
     * 从请求头获取Token
     */
    private String getToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(TOKEN_PREFIX)) {
            return authorization.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
    
    /**
     * 解析Token
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("timestamp", System.currentTimeMillis());
        
        String body = JSON.toJSONString(result);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
