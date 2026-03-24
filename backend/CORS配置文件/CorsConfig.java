package com.huiwutong.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS 跨域配置
 * 解决前端 H5 开发环境访问后端 API 的跨域问题
 *
 * 使用方法：
 * 1. 将此文件放到 conference-gateway/src/main/java/com/huiwutong/gateway/config/ 目录下
 * 2. 确保依赖中有 spring-boot-starter-webflux
 * 3. 重启 Gateway 服务
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源（开发环境）
        // 生产环境应该指定具体的域名，例如：
        // config.addAllowedOrigin("https://yourdomain.com");
        config.addAllowedOriginPattern("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许的 HTTP 方法
        config.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS",
            "PATCH"
        ));

        // 允许携带凭证（Cookie、Authorization 等）
        config.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        // 暴露的响应头（如果前端需要访问自定义响应头，在这里添加）
        // config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 对所有路径应用 CORS 配置
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
