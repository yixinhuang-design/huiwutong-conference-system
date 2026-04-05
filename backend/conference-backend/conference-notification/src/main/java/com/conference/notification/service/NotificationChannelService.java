package com.conference.notification.service;

import com.conference.notification.config.AliyunSMSProperties;
import com.conference.notification.config.UniPushProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知渠道服务
 * 整合短信、推送等多种渠道
 * 本地开发模式：使用日志模拟，不依赖第三方SDK
 * @author AI Executive
 * @date 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationChannelService {
    
    private final AliyunSMSProperties aliyunSMSProperties;
    private final UniPushProperties uniPushProperties;
    
    /**
     * 初始化
     */
    public void init() {
        log.info("通知渠道服务初始化（本地模拟模式）");
        if (aliyunSMSProperties.getAccessKeyId() != null) {
            log.info("阿里云短信配置已加载，签名={}", aliyunSMSProperties.getSignName());
        }
        if (uniPushProperties.getAppId() != null) {
            log.info("UniPush配置已加载，AppId={}", uniPushProperties.getAppId());
        }
    }
    
    /**
     * 发送短信
     * @param phoneNumber 手机号
     * @param content 短信内容
     * @return 是否成功
     */
    public boolean sendSMS(String phoneNumber, String content) {
        // 本地开发模式：日志模拟发送
        log.info("[模拟短信] 手机号={}, 签名={}, 内容={}", phoneNumber, aliyunSMSProperties.getSignName(), content);
        return true;
    }
    
    /**
     * UniPush推送
     * @param clientId 客户端ID
     * @param title 标题
     * @param content 内容
     * @return 是否成功
     */
    public boolean pushNotification(String clientId, String title, String content) {
        // 本地开发模式：日志模拟推送
        log.info("[模拟推送] clientId={}, 标题={}, 内容={}", clientId, title, content);
        return true;
    }
    
    /**
     * 群推（推送给所有用户）
     * @param title 标题
     * @param content 内容
     * @return 是否成功
     */
    public boolean pushToAll(String title, String content) {
        // 本地开发模式：日志模拟群推
        log.info("[模拟群推] 标题={}, 内容={}", title, content);
        return true;
    }
}
