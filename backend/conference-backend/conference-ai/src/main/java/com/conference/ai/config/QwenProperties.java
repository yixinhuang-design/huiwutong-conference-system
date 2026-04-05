package com.conference.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 通义千问配置
 * @author AI Executive
 * @date 2026-04-02
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qwen")
public class QwenProperties {
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * 模型名称
     */
    private String model = "qwen-turbo";
    
    /**
     * API地址
     */
    private String endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    
    /**
     * 最大Token数
     */
    private Integer maxTokens = 2000;
    
    /**
     * 温度参数 (0-1)
     */
    private Double temperature = 0.7;
    
    /**
     * TopP采样
     */
    private Double topP = 0.8;
}
