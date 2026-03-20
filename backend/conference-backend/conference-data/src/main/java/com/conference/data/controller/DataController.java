package com.conference.data.controller;

import com.conference.common.result.Result;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.data.entity.ServiceHealth;
import com.conference.data.entity.SystemEvent;
import com.conference.data.entity.UserActivity;
import com.conference.data.service.BusinessDataService;
import com.conference.data.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 数据监控 Controller
 * 提供系统监控和业务数据的完整API
 */
@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final SystemMonitorService systemMonitorService;
    private final BusinessDataService businessDataService;

    // ==================== 系统监控API ====================

    /**
     * 获取系统资源指标（CPU/内存/磁盘/网络）
     */
    @GetMapping("/system/metrics")
    public Result<Map<String, Object>> getSystemMetrics() {
        log.debug("获取系统资源指标");
        Map<String, Object> metrics = systemMonitorService.collectCurrentMetrics();
        return Result.ok(metrics);
    }

    /**
     * 获取系统指标历史数据（用于图表）
     */
    @GetMapping("/system/metrics/history")
    public Result<Object> getMetricsHistory(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(systemMonitorService.getMetricsHistory(Math.min(limit, 100)));
    }

    /**
     * 获取服务健康状态
     */
    @GetMapping("/system/services")
    public Result<List<ServiceHealth>> getServiceHealth() {
        log.debug("获取服务健康状态");
        return Result.ok(systemMonitorService.getAllServiceHealth());
    }

    /**
     * 获取数据库监控指标
     */
    @GetMapping("/system/db-metrics")
    public Result<Map<String, Object>> getDbMetrics() {
        log.debug("获取数据库监控指标");
        return Result.ok(systemMonitorService.getDbMetrics());
    }

    /**
     * 获取API访问统计
     */
    @GetMapping("/system/api-metrics")
    public Result<Map<String, Object>> getApiMetrics() {
        log.debug("获取API访问统计");
        return Result.ok(systemMonitorService.getApiMetrics());
    }

    /**
     * 获取热门接口
     */
    @GetMapping("/system/top-endpoints")
    public Result<List<Map<String, Object>>> getTopEndpoints() {
        return Result.ok(systemMonitorService.getTopEndpoints());
    }

    /**
     * 获取响应时间趋势（按小时）
     */
    @GetMapping("/system/response-time-trend")
    public Result<List<Map<String, Object>>> getResponseTimeTrend() {
        return Result.ok(systemMonitorService.getHourlyResponseTime());
    }

    /**
     * 获取错误率趋势（按天）
     */
    @GetMapping("/system/error-rate-trend")
    public Result<List<Map<String, Object>>> getErrorRateTrend() {
        return Result.ok(systemMonitorService.getDailyErrorRate());
    }

    /**
     * 获取系统事件列表
     */
    @GetMapping("/system/events")
    public Result<List<SystemEvent>> getSystemEvents(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(systemMonitorService.getRecentSystemEvents(Math.min(limit, 100)));
    }

    /**
     * 获取用户活动列表
     */
    @GetMapping("/system/user-activities")
    public Result<List<UserActivity>> getUserActivities(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.ok(systemMonitorService.getRecentUserActivities(Math.min(limit, 100)));
    }

    /**
     * 记录API访问日志（供其他服务调用）
     */
    @PostMapping("/system/log-access")
    public Result<Void> logAccess(@RequestBody Map<String, Object> logData) {
        try {
            systemMonitorService.logApiAccess(
                    (String) logData.getOrDefault("serviceName", "unknown"),
                    (String) logData.getOrDefault("method", "GET"),
                    (String) logData.getOrDefault("path", "/"),
                    (int) logData.getOrDefault("statusCode", 200),
                    ((Number) logData.getOrDefault("responseTime", 0)).longValue()
            );
            return Result.ok();
        } catch (Exception e) {
            log.warn("记录API日志异常: {}", e.getMessage());
            return Result.fail("记录失败");
        }
    }

    /**
     * 记录系统事件
     */
    @PostMapping("/system/log-event")
    public Result<Void> logEvent(@RequestBody Map<String, String> eventData) {
        try {
            systemMonitorService.logSystemEvent(
                    eventData.getOrDefault("type", "info"),
                    eventData.getOrDefault("source", "unknown"),
                    eventData.getOrDefault("message", "")
            );
            return Result.ok();
        } catch (Exception e) {
            return Result.fail("记录失败");
        }
    }

    // ==================== 业务数据API ====================

    /**
     * 获取报到率数据
     */
    @GetMapping("/business/report-rate")
    public Result<Map<String, Object>> getReportRate(
            @RequestParam(required = false) Long conferenceId) {
        log.debug("获取报到率数据, conferenceId={}", conferenceId);
        return Result.ok(businessDataService.getReportRate(conferenceId));
    }

    /**
     * 获取签到率数据（按日程维度）
     */
    @GetMapping("/business/checkin-rate")
    public Result<List<Map<String, Object>>> getCheckinRate(
            @RequestParam(required = false) Long conferenceId) {
        log.debug("获取签到率数据, conferenceId={}", conferenceId);
        return Result.ok(businessDataService.getCheckinRate(conferenceId));
    }

    /**
     * 获取查寝率数据
     */
    @GetMapping("/business/dormitory-rate")
    public Result<List<Map<String, Object>>> getDormitoryRate(
            @RequestParam(required = false) Long conferenceId) {
        log.debug("获取查寝率数据, conferenceId={}", conferenceId);
        return Result.ok(businessDataService.getDormitoryRate(conferenceId));
    }

    /**
     * 获取会议列表（供数据监控选择会议）
     */
    @GetMapping("/business/meetings")
    public Result<List<Map<String, Object>>> getMeetingList() {
        return Result.ok(businessDataService.getMeetingList());
    }

    /**
     * 获取数据大屏数据（综合汇总）
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData(
            @RequestParam(required = false) Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        log.info("[租户{}] 获取仪表盘数据, conferenceId={}", tenantId, conferenceId);

        Map<String, Object> dashboardData = new LinkedHashMap<>();
        dashboardData.put("tenantId", tenantId);

        // 报到率数据
        Map<String, Object> reportRate = businessDataService.getReportRate(conferenceId);
        dashboardData.put("reportRate", reportRate);

        // 系统指标
        Map<String, Object> systemMetrics = systemMonitorService.collectCurrentMetrics();
        dashboardData.put("systemMetrics", systemMetrics);

        // API指标
        Map<String, Object> apiMetrics = systemMonitorService.getApiMetrics();
        dashboardData.put("apiMetrics", apiMetrics);

        return Result.ok(dashboardData);
    }

    /**
     * 获取预警数据
     */
    @GetMapping("/warning")
    public Result<Map<String, Object>> getWarningData(
            @RequestParam(required = false) Long conferenceId) {
        Map<String, Object> warningData = new LinkedHashMap<>();

        // 从系统事件中统计warning和error类型
        List<SystemEvent> events = systemMonitorService.getRecentSystemEvents(100);
        long warnings = events.stream().filter(e -> "warning".equals(e.getEventType())).count();
        long critical = events.stream().filter(e -> "error".equals(e.getEventType())).count();

        warningData.put("warnings", warnings);
        warningData.put("critical", critical);
        warningData.put("lastUpdated", System.currentTimeMillis());
        return Result.ok(warningData);
    }

    /**
     * 获取实时统计
     */
    @GetMapping("/realtime-stats")
    public Result<Map<String, Object>> getRealtimeStats(
            @RequestParam(required = false) Long conferenceId) {
        Map<String, Object> stats = new LinkedHashMap<>();

        // 获取报到数据作为在线/离线人数基础
        Map<String, Object> reportRate = businessDataService.getReportRate(conferenceId);
        stats.put("onlineCount", reportRate.getOrDefault("actual", 0));
        stats.put("totalCount", reportRate.getOrDefault("expected", 0));
        stats.put("timestamp", System.currentTimeMillis());
        return Result.ok(stats);
    }

    /**
     * 数据导出接口
     */
    @GetMapping("/business/export")
    public Result<Map<String, Object>> exportData(
            @RequestParam String type,
            @RequestParam(required = false) Long conferenceId) {
        log.info("导出数据: type={}, conferenceId={}", type, conferenceId);
        Map<String, Object> exportResult = new LinkedHashMap<>();
        exportResult.put("type", type);
        exportResult.put("conferenceId", conferenceId);
        exportResult.put("status", "success");
        exportResult.put("message", "数据导出功能准备就绪");
        exportResult.put("timestamp", System.currentTimeMillis());
        return Result.ok(exportResult);
    }
}
