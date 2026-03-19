package com.conference.meeting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置
 * 允许前端应用跨域访问会议服务
 */
// @Configuration  # 禁用CORS配置，由网关统一处理
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 开发环境允许所有来源
        corsConfiguration.addAllowedOrigin("*");
        // 允许所有HTTP方法
        corsConfiguration.addAllowedMethod("*");
        // 允许所有请求头
        corsConfiguration.addAllowedHeader("*");
        // 不携带Cookie，避免"*"与凭证冲突
        corsConfiguration.setAllowCredentials(false);
        // 预检请求缓存时间（秒）
        corsConfiguration.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}
