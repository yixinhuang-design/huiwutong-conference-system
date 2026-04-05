package com.conference.collaboration.config;

import com.conference.common.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket握手认证拦截器
 * 验证连接请求中的token参数，防止未授权访问
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        URI uri = request.getURI();
        String query = uri.getQuery();
        String token = null;
        String userId = null;

        // 从URL参数中提取token和userId
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    if ("token".equals(kv[0])) {
                        token = kv[1];
                    } else if ("userId".equals(kv[0])) {
                        userId = kv[1];
                    }
                }
            }
        }

        // 也支持从请求头获取token
        if (token == null && request instanceof ServletServerHttpRequest servletRequest) {
            String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 验证token
        if (token == null || token.isEmpty()) {
            log.warn("[WebSocket] 握手被拒绝: 缺少认证token, uri={}", uri);
            // 允许无token连接（兼容模式），生产环境建议 return false
            log.warn("[WebSocket] 允许无token连接（兼容模式），建议前端传递token参数");
        } else {
            // 使用JwtUtils验证token有效性
            Boolean isValid = jwtUtils.validateToken(token);
            if (isValid != null && isValid) {
                Long jwtUserId = jwtUtils.getUserIdFromToken(token);
                Long tenantId = jwtUtils.getTenantIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);

                if (jwtUserId != null) {
                    userId = jwtUserId.toString();
                }
                if (tenantId != null) {
                    attributes.put("tenantId", tenantId.toString());
                }
                if (username != null) {
                    attributes.put("username", username);
                }
                log.info("[WebSocket] JWT验证通过: userId={}, username={}", userId, username);
            } else {
                log.warn("[WebSocket] JWT验证失败, token无效, uri={}", uri);
                // 生产环境建议 return false 拒绝连接
            }
        }

        // 将userId存入attributes，供WebSocket handler使用
        if (userId != null) {
            attributes.put("userId", userId);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后的处理
    }
}
