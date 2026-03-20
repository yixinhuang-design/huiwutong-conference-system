package com.conference.data.config;

import com.conference.data.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动事件监听
 * 服务启动时记录初始事件、执行初始健康检查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppStartupListener {

    private final SystemMonitorService systemMonitorService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("====== 数据监控服务启动完成 (端口: 8088) ======");

        // 记录服务启动事件
        systemMonitorService.logSystemEvent("success", "conference-data",
                "数据监控服务启动完成 (端口: 8088)");

        // 记录初始系统指标
        systemMonitorService.logSystemEvent("info", "conference-data",
                "开始采集系统指标和服务健康状态");

        log.info("====== 系统监控定时任务已启动 ======");
        log.info("  - 系统指标采集: 每30秒");
        log.info("  - 服务健康检查: 每60秒");
    }
}
