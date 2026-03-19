package com.conference.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.conference.common.tenant.TenantInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus 配置
 * 注册多租户拦截器和分页插件
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 配置 MybatisPlus 拦截器
     * 包括：多租户隔离、分页等
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加多租户拦截器（必须在分页拦截器前）
        interceptor.addInnerInterceptor(new TenantInterceptor());
        
        // 添加分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        
        log.info("✓ MybatisPlus 拦截器配置成功");
        return interceptor;
    }
}
