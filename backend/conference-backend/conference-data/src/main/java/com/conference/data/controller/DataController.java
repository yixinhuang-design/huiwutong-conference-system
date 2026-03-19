package com.conference.data.controller;

import com.conference.common.result.Result;
import com.conference.common.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据指挥 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/data")
public class DataController {
    
    /**
     * 获取数据大屏数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData(@RequestParam Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        log.info("[租户{}] 获取仪表盘数据", tenantId);
        
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("tenantId", tenantId);
        dashboardData.put("registeredCount", 500);
        dashboardData.put("checkinCount", 480);
        dashboardData.put("checkinRate", 96.0);
        dashboardData.put("realTimeParticipants", 450);
        return Result.ok(dashboardData);
    }
    
    /**
     * 获取预警数据
     */
    @GetMapping("/warning")
    public Result<Map<String, Object>> getWarningData(@RequestParam Long conferenceId) {
        Map<String, Object> warningData = new HashMap<>();
        warningData.put("warnings", 5);
        warningData.put("critical", 0);
        warningData.put("lastUpdated", System.currentTimeMillis());
        return Result.ok(warningData);
    }
    
    /**
     * 获取实时统计
     */
    @GetMapping("/realtime-stats")
    public Result<Map<String, Object>> getRealtimeStats(@RequestParam Long conferenceId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("onlineCount", 450);
        stats.put("offlineCount", 50);
        stats.put("timestamp", System.currentTimeMillis());
        return Result.ok(stats);
    }
    
    /**
     * 获取数据分析报告
     */
    @GetMapping("/analysis-report")
    public Result<Map<String, Object>> getAnalysisReport(@RequestParam Long conferenceId) {
        Map<String, Object> report = new HashMap<>();
        report.put("reportId", 1L);
        report.put("generatedAt", System.currentTimeMillis());
        report.put("insights", "数据分析中...");
        return Result.ok(report);
    }
}
