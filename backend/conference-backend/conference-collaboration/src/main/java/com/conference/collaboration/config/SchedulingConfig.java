package com.conference.collaboration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 启用Spring定时任务调度
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
