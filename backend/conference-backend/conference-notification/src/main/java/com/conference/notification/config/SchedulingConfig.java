package com.conference.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置
 * 启用Spring定时任务调度器
 *
 * @author AI Executive
 * @since 2026-04-02
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 定时任务配置类
    // 实际的定时任务在@Scheduled注解的方法中实现
}
