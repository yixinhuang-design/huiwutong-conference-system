package com.conference.collaboration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * UniPush推送服务
 * 基于个推(GeTui) UniPush 2.0 REST API
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniPushService {

    private final UniPushProperties pushProperties;

    private volatile String authToken;
    private volatile long tokenExpireTime;

    /**
     * 单推 - 向指定客户端推送消息
     * @param clientId UniPush客户端标识(CID)
     * @param title 推送标题
     * @param content 推送内容
     */
    public void pushToSingle(String clientId, String title, String content) {
        if (!pushProperties.isEnabled()) {
            log.info("[Push-disabled] 推送未启用, clientId={}, title={}", clientId, title);
            return;
        }

        if (clientId == null || clientId.isEmpty()) {
            log.warn("推送发送失败: clientId为空");
            return;
        }

        try {
            String token = getAuthToken();
            if (token == null) {
                log.error("推送发送失败: 获取auth token失败");
                return;
            }

            String url = String.format("https://restapi.getui.com/v2/%s/push/single/cid", 
                pushProperties.getAppId());

            // 构建推送payload (UniPush 2.0 API格式)
            String payload = String.format("""
                {
                    "request_id": "%s",
                    "audience": {
                        "cid": ["%s"]
                    },
                    "push_message": {
                        "notification": {
                            "title": "%s",
                            "body": "%s",
                            "click_type": "startapp"
                        }
                    }
                }
                """, UUID.randomUUID().toString(), clientId, 
                escapeJson(title), escapeJson(content));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json;charset=utf-8")
                    .header("token", token)
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("推送发送成功: clientId={}, title={}", clientId, title);
            } else {
                log.error("推送发送失败: clientId={}, status={}, response={}", 
                    clientId, response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("推送发送异常: clientId={}", clientId, e);
        }
    }

    /**
     * 批量推送
     */
    public void pushToMultiple(List<String> clientIds, String title, String content) {
        for (String clientId : clientIds) {
            pushToSingle(clientId, title, content);
        }
    }

    /**
     * 获取认证token (缓存机制)
     */
    private synchronized String getAuthToken() {
        if (authToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return authToken;
        }

        try {
            long timestamp = System.currentTimeMillis();
            String sign = sha256(pushProperties.getAppKey() + timestamp + pushProperties.getMasterSecret());

            String url = String.format("https://restapi.getui.com/v2/%s/auth", pushProperties.getAppId());
            String payload = String.format("""
                {
                    "sign": "%s",
                    "timestamp": "%d",
                    "appkey": "%s"
                }
                """, sign, timestamp, pushProperties.getAppKey());

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json;charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body().contains("\"code\":0")) {
                // 简单解析token (避免引入JSON库依赖)
                String body = response.body();
                int tokenStart = body.indexOf("\"token\":\"") + 9;
                int tokenEnd = body.indexOf("\"", tokenStart);
                if (tokenStart > 8 && tokenEnd > tokenStart) {
                    authToken = body.substring(tokenStart, tokenEnd);
                    // token有效期为1天，提前1小时刷新
                    tokenExpireTime = System.currentTimeMillis() + 23 * 3600 * 1000L;
                    log.info("UniPush auth token获取成功");
                    return authToken;
                }
            }

            log.error("获取UniPush auth token失败: response={}", response.body());
            return null;

        } catch (Exception e) {
            log.error("获取UniPush auth token异常", e);
            return null;
        }
    }

    private String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
    }

    // ==================== 配置属性 ====================

    @Component
    @ConfigurationProperties(prefix = "notification.unipush")
    @lombok.Data
    public static class UniPushProperties {
        /** 是否启用推送通知 */
        private boolean enabled = false;
        /** 个推AppId */
        private String appId = "";
        /** 个推AppKey */
        private String appKey = "";
        /** 个推MasterSecret */
        private String masterSecret = "";
    }
}
