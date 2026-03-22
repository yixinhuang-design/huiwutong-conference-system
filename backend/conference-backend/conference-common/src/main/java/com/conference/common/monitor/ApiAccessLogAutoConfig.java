package com.conference.common.monitor;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API 访问日志自动配置
 * 各服务在 application.yml 中配置即可启用：
 *
 * monitor:
 *   access-log:
 *     enabled: true                          # 默认 true
 *     data-service-url: http://localhost:8088 # 数据服务地址
 *
 * data 服务自身无需启用（它直接本地记录），在其 yml 中设置 enabled: false 即可
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "monitor.access-log.enabled", havingValue = "true", matchIfMissing = true)
public class ApiAccessLogAutoConfig {

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Value("${monitor.access-log.data-service-url:http://localhost:8088}")
    private String dataServiceUrl;

    @Bean
    public FilterRegistrationBean<Filter> apiAccessLogFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiAccessLogFilter(serviceName, dataServiceUrl));
        registration.addUrlPatterns("/api/*");
        registration.setName("apiAccessLogFilter");
        registration.setOrder(Integer.MAX_VALUE); // 最低优先级，在其他 Filter 之后执行
        log.info("[ApiAccessLogAutoConfig] 注册 API 访问日志 Filter - 服务: {}, 上报到: {}", serviceName, dataServiceUrl);
        return registration;
    }
}
