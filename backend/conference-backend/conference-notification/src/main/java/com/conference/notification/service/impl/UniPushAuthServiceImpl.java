package com.conference.notification.service.impl;

import com.conference.notification.service.UniPushAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * UniPush认证服务实现（UniPush 2.0 云函数模式）
 * 
 * uni-push 2.0 不需要 MasterSecret，不需要个推 REST API 鉴权。
 * 推送通过 uniCloud 云函数 URL 化接口完成，云函数内部免鉴权。
 * 本服务仅保留配置检查功能，认证相关方法返回占位值。
 *
 * @author Conference System
 * @since 2026-04-05
 */
@Slf4j
@Service
public class UniPushAuthServiceImpl implements UniPushAuthService {

    @Value("${unipush.cloud-function-url:}")
    private String cloudFunctionUrl;

    @Value("${unipush.app-id:}")
    private String appId;

    /**
     * uni-push 2.0 通过云函数推送，不需要访问令牌。
     * 返回云函数URL作为标识，供调用方判断是否已配置。
     */
    @Override
    public String getAccessToken() {
        if (!isConfigured()) {
            log.warn("[UniPush] 云函数URL未配置，无法推送");
            return null;
        }
        // 返回云函数URL作为"token"标识（非真正的鉴权token）
        return cloudFunctionUrl;
    }

    /**
     * uni-push 2.0 无需刷新令牌
     */
    @Override
    public String refreshAccessToken() {
        return getAccessToken();
    }

    /**
     * 检查推送配置是否完整
     * uni-push 2.0 只需要云函数URL即可推送
     */
    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(cloudFunctionUrl);
    }

    @Override
    public void clearCache() {
        log.info("[UniPush] 云函数模式无需清除缓存");
    }

    /**
     * 获取云函数URL（供 UniPushServiceImpl 使用）
     */
    public String getCloudFunctionUrl() {
        return cloudFunctionUrl;
    }
}
