package com.conference.notification.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.conference.notification.entity.Notification;
import com.conference.notification.service.UniPushAuthService;
import com.conference.notification.service.UniPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * UniPush推送服务实现（UniPush 2.0 云函数模式）
 * 
 * uni-push 2.0 架构：Java后端 → HTTP POST → uniCloud云函数(URL化) → uni-push服务 → 手机
 * 不再直接调用个推 REST API，不需要 MasterSecret
 *
 * @author Conference System
 * @since 2026-04-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniPushServiceImpl implements UniPushService {

    private final UniPushAuthService authService;

    @Value("${unipush.cloud-function-url:}")
    private String cloudFunctionUrl;

    @Value("${unipush.app-id:}")
    private String appId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Integer> pushToUsers(Notification notification, List<String> userIds) {
        Map<String, Integer> result = new HashMap<>();
        result.put("success", 0);
        result.put("failed", 0);

        if (!isConfigured()) {
            log.warn("[UniPush] 云函数URL未配置，跳过推送");
            return result;
        }

        if (userIds == null || userIds.isEmpty()) {
            log.warn("[UniPush] 用户CID列表为空，跳过推送");
            return result;
        }

        try {
            JSONObject requestBody = buildCloudFunctionRequest(notification);
            // 按官方文档：传入 cids 即为单推/批量推
            requestBody.put("cids", userIds);

            JSONObject response = callCloudFunction(requestBody);

            if (response != null && !response.containsKey("code")) {
                // 云函数直接返回 uni-push sendMessage 的原始结果
                log.info("[UniPush] 推送成功: notificationId={}, targets={}, result={}",
                    notification.getId(), userIds.size(), response);
                result.put("success", userIds.size());
            } else if (response != null && response.getIntValue("code", -1) == 0) {
                log.info("[UniPush] 推送成功: notificationId={}, targets={}",
                    notification.getId(), userIds.size());
                result.put("success", userIds.size());
            } else {
                String msg = response != null ? response.toJSONString() : "无响应";
                log.error("[UniPush] 推送失败: {}", msg);
                result.put("failed", userIds.size());
            }
        } catch (Exception e) {
            log.error("[UniPush] 推送异常: notificationId={}", notification.getId(), e);
            result.put("failed", userIds.size());
        }

        return result;
    }

    @Override
    public Map<String, Integer> pushToAll(Notification notification) {
        Map<String, Integer> result = new HashMap<>();
        result.put("success", 0);
        result.put("failed", 0);

        if (!isConfigured()) {
            log.warn("[UniPush] 云函数URL未配置，跳过全推");
            return result;
        }

        try {
            // 按官方文档：不传 cids 即为全推（每分钟不超过5次，10分钟内不能推重复消息体）
            JSONObject requestBody = buildCloudFunctionRequest(notification);

            JSONObject response = callCloudFunction(requestBody);

            if (response != null && !response.containsKey("code")) {
                log.info("[UniPush] 全推成功: notificationId={}, result={}", notification.getId(), response);
                result.put("success", 1);
            } else if (response != null && response.getIntValue("code", -1) == 0) {
                log.info("[UniPush] 全推成功: notificationId={}", notification.getId());
                result.put("success", 1);
            } else {
                String msg = response != null ? response.toJSONString() : "无响应";
                log.error("[UniPush] 全推失败: {}", msg);
                result.put("failed", 1);
            }
        } catch (Exception e) {
            log.error("[UniPush] 全推异常: notificationId={}", notification.getId(), e);
            result.put("failed", 1);
        }

        return result;
    }

    @Override
    public boolean isConfigured() {
        return authService.isConfigured();
    }

    @Override
    public Map<String, Integer> pushTransmission(Notification notification, List<String> userIds) {
        Map<String, Integer> result = new HashMap<>();
        result.put("success", 0);
        result.put("failed", 0);

        if (!isConfigured()) {
            log.warn("[UniPush] 云函数URL未配置，跳过透传推送");
            return result;
        }

        if (userIds == null || userIds.isEmpty()) {
            log.warn("[UniPush] 用户CID列表为空，跳过透传推送");
            return result;
        }

        try {
            // 透传消息：force_notification=false，客户端自行处理
            JSONObject requestBody = buildCloudFunctionRequest(notification);
            requestBody.put("cids", userIds);
            requestBody.put("force_notification", false);

            JSONObject response = callCloudFunction(requestBody);

            if (response != null && !response.containsKey("code")) {
                log.info("[UniPush] 透传推送成功: notificationId={}, result={}", notification.getId(), response);
                result.put("success", userIds.size());
            } else if (response != null && response.getIntValue("code", -1) == 0) {
                log.info("[UniPush] 透传推送成功: notificationId={}", notification.getId());
                result.put("success", userIds.size());
            } else {
                log.error("[UniPush] 透传推送失败: {}", response);
                result.put("failed", userIds.size());
            }
        } catch (Exception e) {
            log.error("[UniPush] 透传推送异常: notificationId={}", notification.getId(), e);
            result.put("failed", userIds.size());
        }

        return result;
    }

    /**
     * 构建云函数请求体
     */
    private JSONObject buildCloudFunctionRequest(Notification notification) {
        JSONObject request = new JSONObject();
        request.put("title", notification.getTitle() != null ? notification.getTitle() : "新通知");

        // 截取内容前100个字符作为推送摘要
        String content = notification.getContent();
        if (content != null && content.length() > 100) {
            content = content.substring(0, 100) + "...";
        }
        request.put("content", content != null ? content : "您有一条新的通知消息");
        request.put("force_notification", true);
        request.put("request_id", "conf_" + notification.getId() + "_" + System.currentTimeMillis());

        // 自定义数据（点击通知后传给客户端）
        JSONObject payload = new JSONObject();
        payload.put("notificationId", notification.getId());
        payload.put("conferenceId", notification.getConferenceId());
        payload.put("type", notification.getType());
        request.put("payload", payload);

        // 消息有效期：24小时
        JSONObject settings = new JSONObject();
        settings.put("ttl", 86400000);
        request.put("settings", settings);

        return request;
    }

    /**
     * 调用 uniCloud 云函数 URL 化接口
     * 
     * @param requestBody 请求体
     * @return 云函数返回的JSON响应
     */
    private JSONObject callCloudFunction(JSONObject requestBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

            log.info("[UniPush] 调用云函数: url={}, action={}", cloudFunctionUrl, requestBody.getString("action"));

            ResponseEntity<String> response = restTemplate.exchange(
                cloudFunctionUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                log.info("[UniPush] 云函数响应: {}", result);
                return result;
            } else {
                log.error("[UniPush] 云函数调用失败: status={}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("[UniPush] 云函数调用异常: {}", e.getMessage(), e);
            return null;
        }
    }
}
