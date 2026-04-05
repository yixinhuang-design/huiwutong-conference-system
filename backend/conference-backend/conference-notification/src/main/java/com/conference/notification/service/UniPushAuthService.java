package com.conference.notification.service;

/**
 * UniPush认证服务（UniPush 2.0 云函数模式）
 * 
 * uni-push 2.0 不需要 MasterSecret，不需要个推 REST API 鉴权。
 * 推送通过 uniCloud 云函数 URL 化接口完成，云函数内部免鉴权。
 * 本接口保留用于配置检查和兼容性。
 *
 * @author Conference System
 * @since 2026-04-05
 */
public interface UniPushAuthService {

    /**
     * 获取推送凭证（云函数模式下返回云函数URL）
     *
     * @return 云函数URL，未配置时返回null
     */
    String getAccessToken();

    /**
     * 刷新凭证（云函数模式下等同于getAccessToken）
     *
     * @return 云函数URL
     */
    String refreshAccessToken();

    /**
     * 验证UniPush配置是否完整
     * 
     * uni-push 2.0 只需要云函数URL即可推送
     *
     * @return true如果云函数URL已配置
     */
    boolean isConfigured();

    /**
     * 清除缓存（云函数模式下无操作）
     */
    void clearCache();
}
