package com.conference.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * UniPush配置
 * @author AI Executive
 * @date 2026-03-24
 */
@Data
@Component
@ConfigurationProperties(prefix = "unipush")
public class UniPushProperties {
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * 应用密钥（客户端SDK用，服务端推送不需要）
     */
    private String appKey;
    
    /**
     * uniCloud云函数URL化地址
     * uni-push 2.0 通过云函数推送，不需要MasterSecret
     * 格式示例: https://fc-xxxxxxxx.next.bspapp.com/push-notification
     */
    private String cloudFunctionUrl;
    
    /**
     * 是否使用HTTPS
     */
    private boolean https = true;
}
