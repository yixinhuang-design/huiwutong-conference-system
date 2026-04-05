package com.conference.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云短信配置
 * @author AI Executive
 * @date 2026-03-24
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSMSProperties {
    
    /**
     * AccessKey ID
     */
    private String accessKeyId;
    
    /**
     * AccessKey Secret
     */
    private String accessKeySecret;
    
    /**
     * 短信签名
     */
    private String signName = "智能会议系统";
    
    /**
     * 地域节点
     */
    private String regionId = "cn-hangzhou";
    
    /**
     * 短信模板CODE
     */
    private String templateCode;
}
