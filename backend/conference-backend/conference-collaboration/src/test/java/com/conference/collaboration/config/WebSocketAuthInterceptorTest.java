package com.conference.collaboration.config;

import com.conference.common.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * WebSocketAuthInterceptor 单元测试
 * 验证JWT认证拦截器
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("L3: WebSocket JWT认证测试")
class WebSocketAuthInterceptorTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebSocketHandler handler;

    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketAuthInterceptor(jwtUtils);
    }

    @Test
    @DisplayName("有效token应解析userId")
    void validToken_shouldExtractUserId() throws Exception {
        // Arrange
        String validToken = "valid.jwt.token";
        when(request.getURI()).thenReturn(new URI("ws://localhost:8089/ws/task?token=" + validToken + "&userId=201"));
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(validToken)).thenReturn(201L);
        when(jwtUtils.getTenantIdFromToken(validToken)).thenReturn(1L);
        when(jwtUtils.getUsernameFromToken(validToken)).thenReturn("张三");

        Map<String, Object> attributes = new HashMap<>();

        // Act
        boolean result = interceptor.beforeHandshake(request, response, handler, attributes);

        // Assert
        assertTrue(result);
        assertEquals("201", attributes.get("userId"));
        assertEquals("1", attributes.get("tenantId"));
        assertEquals("张三", attributes.get("username"));
        verify(jwtUtils).validateToken(validToken);
    }

    @Test
    @DisplayName("无效token应允许兼容模式连接")
    void invalidToken_shouldAllowCompatibilityMode() throws Exception {
        // Arrange
        String invalidToken = "invalid.token";
        when(request.getURI()).thenReturn(new URI("ws://localhost:8089/ws/task?token=" + invalidToken));
        when(jwtUtils.validateToken(invalidToken)).thenReturn(false);

        Map<String, Object> attributes = new HashMap<>();

        // Act
        boolean result = interceptor.beforeHandshake(request, response, handler, attributes);

        // Assert - 兼容模式允许连接
        assertTrue(result);
        assertNull(attributes.get("userId"));
    }

    @Test
    @DisplayName("无token参数应允许兼容模式连接")
    void noToken_shouldAllowCompatibilityMode() throws Exception {
        // Arrange
        when(request.getURI()).thenReturn(new URI("ws://localhost:8089/ws/task"));

        Map<String, Object> attributes = new HashMap<>();

        // Act
        boolean result = interceptor.beforeHandshake(request, response, handler, attributes);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("URL中userId参数应作为后备")
    void urlUserId_shouldBeUsedAsFallback() throws Exception {
        // Arrange
        when(request.getURI()).thenReturn(new URI("ws://localhost:8089/ws/task?userId=999"));

        Map<String, Object> attributes = new HashMap<>();

        // Act
        boolean result = interceptor.beforeHandshake(request, response, handler, attributes);

        // Assert
        assertTrue(result);
        assertEquals("999", attributes.get("userId"));
    }
}
