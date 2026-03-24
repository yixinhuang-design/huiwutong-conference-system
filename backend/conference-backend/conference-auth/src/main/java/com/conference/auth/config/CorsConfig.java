package com.conference.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置
 * 注意：当通过网关访问时，应该禁用此配置，由网关统一处理CORS
 * 只有直接访问服务时才需要此配置
 *
 * 当前已禁用（通过Gateway网关访问时）
 * 如需直连服务调试，请取消下面的注释
 */
// @Configuration  // 已禁用：使用Gateway的统一CORS配置
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 允许所有来源（开发环境）
        corsConfiguration.addAllowedOrigin("*");
        // 允许所有HTTP方法
        corsConfiguration.addAllowedMethod("*");
        // 允许所有请求头
        corsConfiguration.addAllowedHeader("*");
        // 允许跨域请求携带认证信息
        corsConfiguration.setAllowCredentials(false);
        // 预检请求缓存时间（秒）
        corsConfiguration.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}
