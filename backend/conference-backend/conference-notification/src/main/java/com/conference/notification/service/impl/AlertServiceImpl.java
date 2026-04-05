package com.conference.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.notification.dto.AlertRuleRequest;
import com.conference.notification.entity.AlertEvent;
import com.conference.notification.entity.AlertRule;
import com.conference.notification.mapper.AlertEventMapper;
import com.conference.notification.mapper.AlertRuleMapper;
import com.conference.notification.service.AlertService;
import com.conference.notification.service.NotificationChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 预警服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl extends ServiceImpl<AlertEventMapper, AlertEvent>
        implements AlertService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final AlertRuleMapper ruleMapper;
    private final AlertEventMapper eventMapper;
    private final NotificationChannelService notificationChannelService;

    @Value("${alert.gateway.url:http://localhost:8080}")
    private String gatewayBaseUrl;

    @Value("${alert.data-service.url:http://localhost:8088}")
    private String dataServiceUrl;

    private Long getTenantId() {
        try {
            Long tenantId = com.conference.common.tenant.TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    // === 预警规则 ===

    @Override
    public List<AlertRule> listRules(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getTenantId, tenantId)
                .eq(AlertRule::getDeleted, 0);
        if (conferenceId != null) {
            wrapper.eq(AlertRule::getConferenceId, conferenceId);
        }
        wrapper.orderByDesc(AlertRule::getCreateTime);
        return ruleMapper.selectList(wrapper);
    }

    @Override
    public AlertRule saveRule(AlertRuleRequest request) {
        Long tenantId = getTenantId();
        AlertRule rule = new AlertRule();
        rule.setTenantId(tenantId);
        rule.setConferenceId(request.getConferenceId());
        rule.setName(request.getName());
        rule.setMetric(request.getMetric());
        rule.setOperator(request.getOperator() != null ? request.getOperator() : "<");
        rule.setThreshold(request.getThreshold());
        rule.setSeverity(request.getSeverity() != null ? request.getSeverity() : "medium");
        rule.setNotifySms(Boolean.TRUE.equals(request.getNotifySms()) ? 1 : 0);
        rule.setNotifySystem(Boolean.TRUE.equals(request.getNotifySystem()) ? 1 : 0);
        rule.setEnabled(1);
        rule.setDeleted(0);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());

        ruleMapper.insert(rule);
        log.info("[租户{}] 预警规则已创建: id={}, metric={}", tenantId, rule.getId(), rule.getMetric());
        return rule;
    }

    @Override
    public AlertRule updateRule(Long id, AlertRuleRequest request) {
        AlertRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在: " + id);
        }

        if (request.getName() != null) rule.setName(request.getName());
        if (request.getMetric() != null) rule.setMetric(request.getMetric());
        if (request.getOperator() != null) rule.setOperator(request.getOperator());
        if (request.getThreshold() != null) rule.setThreshold(request.getThreshold());
        if (request.getSeverity() != null) rule.setSeverity(request.getSeverity());
        if (request.getNotifySms() != null) rule.setNotifySms(request.getNotifySms() ? 1 : 0);
        if (request.getNotifySystem() != null) rule.setNotifySystem(request.getNotifySystem() ? 1 : 0);
        rule.setUpdateTime(LocalDateTime.now());

        ruleMapper.updateById(rule);
        log.info("预警规则已更新: id={}", id);
        return rule;
    }

    @Override
    public void deleteRule(Long id) {
        // 1. 逻辑删除规则本身
        ruleMapper.deleteById(id);

        // 2. 同步逻辑删除该规则产生的所有预警事件（实时监控/预警通知/统计分析同步清理）
        LambdaQueryWrapper<AlertEvent> eventWrapper = new LambdaQueryWrapper<>();
        eventWrapper.eq(AlertEvent::getRuleId, id)
                .eq(AlertEvent::getDeleted, 0);
        List<AlertEvent> events = eventMapper.selectList(eventWrapper);
        for (AlertEvent event : events) {
            eventMapper.deleteById(event.getId());
        }
        if (!events.isEmpty()) {
            log.info("规则{}删除，同步清理{}条关联预警事件", id, events.size());
        }
    }

    @Override
    public AlertRule toggleRule(Long id) {
        AlertRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在: " + id);
        }
        rule.setEnabled(rule.getEnabled() == 1 ? 0 : 1);
        rule.setUpdateTime(LocalDateTime.now());
        ruleMapper.updateById(rule);
        log.info("预警规则状态切换: id={}, enabled={}", id, rule.getEnabled());
        return rule;
    }

    // === 实时指标 ===

    @Override
    public Map<String, Object> getMetrics(Long conferenceId) {
        Long tenantId = getTenantId();
        Map<String, Object> metrics = new LinkedHashMap<>();
        Map<String, Double> metricValues = new LinkedHashMap<>();

        try {
            // 报到率 - 从 conference_registration 真实数据获取
            double reportRate = 0;
            if (conferenceId != null) {
                Map<String, Object> regData = eventMapper.getRegistrationMetrics(conferenceId, tenantId);
                if (regData != null) {
                    long approved = toLong(regData.get("approved"));
                    long reported = toLong(regData.get("reported"));
                    reportRate = approved > 0 ? Math.round(reported * 1000.0 / approved) / 10.0 : 0;
                }
            }
            metricValues.put("registrationRate", reportRate);
            metrics.put("registrationRate", buildMetric(reportRate, "报到率", "%",
                    reportRate >= 80 ? "up" : reportRate >= 50 ? "stable" : "down"));

            // 签到率 - 从 conference_registration 真实数据获取
            double checkinRate = 0;
            if (conferenceId != null) {
                Map<String, Object> checkinData = eventMapper.getCheckinMetrics(conferenceId, tenantId);
                if (checkinData != null) {
                    long expected = toLong(checkinData.get("expected"));
                    long actual = toLong(checkinData.get("actual"));
                    checkinRate = expected > 0 ? Math.round(actual * 1000.0 / expected) / 10.0 : 0;
                }
            }
            metricValues.put("checkinRate", checkinRate);
            metrics.put("checkinRate", buildMetric(checkinRate, "签到率", "%",
                    checkinRate >= 80 ? "up" : checkinRate >= 50 ? "stable" : "down"));

            // 任务完成率 - 从 conference_registration 日程签到真实数据获取
            double taskRate = 0;
            if (conferenceId != null) {
                Map<String, Object> taskData = eventMapper.getTaskCompletionMetrics(conferenceId, tenantId);
                if (taskData != null) {
                    long totalSchedules = toLong(taskData.get("total"));
                    long completedSchedules = toLong(taskData.get("completed"));
                    taskRate = totalSchedules > 0 ? Math.round(completedSchedules * 1000.0 / totalSchedules) / 10.0 : 0;
                }
            }
            metricValues.put("taskCompletionRate", taskRate);
            metrics.put("taskCompletionRate", buildMetric(taskRate, "任务完成率", "%",
                    taskRate >= 80 ? "up" : taskRate >= 50 ? "stable" : "down"));

            // API响应时间 - 实际测量网关服务响应延迟
            double responseTime = measureGatewayResponseTime();
            metricValues.put("apiResponseTime", responseTime);
            metrics.put("apiResponseTime", buildMetric(responseTime, "API响应时间", "ms",
                    responseTime <= 500 ? "up" : responseTime <= 1000 ? "stable" : "down"));

            // 系统错误率 - 从dataService查询API访问日志统计
            double systemErrorRate = fetchSystemErrorRate();
            metricValues.put("systemErrorRate", systemErrorRate);
            metrics.put("systemErrorRate", buildMetric(systemErrorRate, "系统错误率", "%",
                    systemErrorRate <= 1 ? "up" : systemErrorRate <= 5 ? "stable" : "down"));

        } catch (Exception e) {
            log.error("获取实时指标异常: {}", e.getMessage(), e);
            metrics.put("registrationRate", buildMetric(0, "报到率", "%", "stable"));
            metrics.put("checkinRate", buildMetric(0, "签到率", "%", "stable"));
            metrics.put("taskCompletionRate", buildMetric(0, "任务完成率", "%", "stable"));
            metrics.put("apiResponseTime", buildMetric(0, "API响应时间", "ms", "stable"));
            metrics.put("systemErrorRate", buildMetric(0, "系统错误率", "%", "stable"));
        }

        // 自动预警检测：对比当前指标与启用的规则阈值，自动生成预警事件
        if (conferenceId != null && !metricValues.isEmpty()) {
            try {
                checkRulesAndCreateAlerts(conferenceId, tenantId, metricValues);
            } catch (Exception e) {
                log.warn("预警自动检测异常: {}", e.getMessage());
            }
        }

        // 当前触发的预警数量
        LambdaQueryWrapper<AlertEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getDeleted, 0)
                .ne(AlertEvent::getStatus, "resolved");
        if (conferenceId != null) {
            wrapper.eq(AlertEvent::getConferenceId, conferenceId);
        }
        long activeAlerts = eventMapper.selectCount(wrapper);
        metrics.put("activeAlertCount", activeAlerts);

        return metrics;
    }

    /**
     * 自动预警检测：比较实时指标与启用规则，触发阈值时自动创建预警事件
     * 同一会议+规则只在无未处理事件时创建新事件，避免重复
     */
    private void checkRulesAndCreateAlerts(Long conferenceId, Long tenantId, Map<String, Double> metricValues) {
        LambdaQueryWrapper<AlertRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(AlertRule::getTenantId, tenantId)
                .eq(AlertRule::getConferenceId, conferenceId)
                .eq(AlertRule::getEnabled, 1)
                .eq(AlertRule::getDeleted, 0);
        List<AlertRule> rules = ruleMapper.selectList(ruleWrapper);

        for (AlertRule rule : rules) {
            Double currentValue = metricValues.get(rule.getMetric());
            if (currentValue == null) continue; // null 表示无数据，跳过；0 是有效值，不跳过

            boolean breached = isThresholdBreached(currentValue, rule.getOperator(), rule.getThreshold().doubleValue());
            if (breached) {
                // 检查是否已有该规则的未处理预警事件
                LambdaQueryWrapper<AlertEvent> existCheck = new LambdaQueryWrapper<>();
                existCheck.eq(AlertEvent::getTenantId, tenantId)
                        .eq(AlertEvent::getConferenceId, conferenceId)
                        .eq(AlertEvent::getRuleId, rule.getId())
                        .in(AlertEvent::getStatus, "pending", "processing")
                        .eq(AlertEvent::getDeleted, 0);
                long existCount = eventMapper.selectCount(existCheck);
                if (existCount == 0) {
                    AlertEvent event = new AlertEvent();
                    event.setTenantId(tenantId);
                    event.setConferenceId(conferenceId);
                    event.setRuleId(rule.getId());
                    event.setMetric(rule.getMetric());
                    event.setMetricValue(java.math.BigDecimal.valueOf(currentValue));
                    event.setThreshold(rule.getThreshold());
                    event.setSeverity(rule.getSeverity());
                    event.setStatus("pending");
                    event.setCreateTime(LocalDateTime.now());
                    event.setUpdateTime(LocalDateTime.now());
                    event.setDeleted(0);
                    eventMapper.insert(event);
                    log.info("[预警触发] 规则={}, 指标={}, 当前值={}, 阈值={}", rule.getName(), rule.getMetric(), currentValue, rule.getThreshold());
                    // 发送通知（短信/系统通知）
                    sendAlertNotification(rule, event, currentValue);
                }
            }
        }
    }

    private boolean isThresholdBreached(double currentValue, String operator, double threshold) {
        if (operator == null) operator = "<";
        switch (operator) {
            case "<": return currentValue < threshold;
            case "<=": return currentValue <= threshold;
            case ">": return currentValue > threshold;
            case ">=": return currentValue >= threshold;
            case "==": return Math.abs(currentValue - threshold) < 0.01;
            default: return currentValue < threshold;
        }
    }

    /**
     * 实际测量网关服务的响应时间（ms）
     * 向 localhost:8080 发送健康检查请求并计时
     */
    private double measureGatewayResponseTime() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gatewayBaseUrl + "/api/meeting/list?page=1&size=1"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            long start = System.currentTimeMillis();
            client.send(request, HttpResponse.BodyHandlers.discarding());
            long elapsed = System.currentTimeMillis() - start;
            return elapsed;
        } catch (Exception e) {
            log.warn("网关响应时间测量失败: {}", e.getMessage());
            return 9999; // 超时或不可达
        }
    }

    private Map<String, Object> buildMetric(double value, String label, String unit, String trend) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("value", value);
        metric.put("label", label);
        metric.put("unit", unit);
        metric.put("trend", trend);
        return metric;
    }

    private long toLong(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try { return Long.parseLong(obj.toString()); } catch (Exception e) { return 0; }
    }

    // === 预警事件 ===

    @Override
    public List<AlertEvent> listAlerts(Long conferenceId, String status) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AlertEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getDeleted, 0);
        if (conferenceId != null) {
            wrapper.eq(AlertEvent::getConferenceId, conferenceId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AlertEvent::getStatus, status);
        }
        wrapper.orderByDesc(AlertEvent::getCreateTime);
        return eventMapper.selectList(wrapper);
    }

    @Override
    public AlertEvent processAlert(Long id) {
        AlertEvent event = eventMapper.selectById(id);
        if (event == null) {
            throw new RuntimeException("预警事件不存在: " + id);
        }
        event.setStatus("processing");
        event.setHandleTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());
        // 设置处理人信息
        try {
            Long currentUserId = com.conference.common.tenant.TenantContextHolder.getTenantId();
            event.setHandlerId(currentUserId);
            event.setHandlerName("系统管理员"); // 待集成用户服务后替换为真实用户名
        } catch (Exception e) {
            event.setHandlerName("系统自动处理");
        }
        eventMapper.updateById(event);
        log.info("预警事件开始处理: id={}, handler={}", id, event.getHandlerName());
        return event;
    }

    @Override
    public AlertEvent resolveAlert(Long id, String remark) {
        AlertEvent event = eventMapper.selectById(id);
        if (event == null) {
            throw new RuntimeException("预警事件不存在: " + id);
        }
        event.setStatus("resolved");
        event.setResolveTime(LocalDateTime.now());
        event.setRemark(remark);
        event.setUpdateTime(LocalDateTime.now());
        eventMapper.updateById(event);
        log.info("预警事件已解决: id={}", id);
        return event;
    }

    // === 统计 ===

    @Override
    public Map<String, Object> getAlertStatistics(Long conferenceId) {
        Long tenantId = getTenantId();
        Map<String, Object> stats = new LinkedHashMap<>();

        // 状态分布
        List<Map<String, Object>> statusStats = eventMapper.getStatusStats(conferenceId, tenantId);
        Map<String, Object> statusMap = new LinkedHashMap<>();
        long total = 0;
        for (Map<String, Object> s : statusStats) {
            String statusKey = String.valueOf(s.get("status"));
            Object count = s.get("count");
            statusMap.put(statusKey, count);
            total += ((Number) count).longValue();
        }
        stats.put("statusDistribution", statusMap);
        stats.put("total", total);

        // 级别分布
        List<Map<String, Object>> severityStats = eventMapper.getSeverityStats(conferenceId, tenantId);
        Map<String, Object> severityMap = new LinkedHashMap<>();
        for (Map<String, Object> s : severityStats) {
            severityMap.put(String.valueOf(s.get("severity")), s.get("count"));
        }
        stats.put("severityDistribution", severityMap);

        // 待处理数
        LambdaQueryWrapper<AlertEvent> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getDeleted, 0)
                .eq(AlertEvent::getStatus, "pending");
        if (conferenceId != null) {
            pendingWrapper.eq(AlertEvent::getConferenceId, conferenceId);
        }
        stats.put("pendingCount", eventMapper.selectCount(pendingWrapper));

        // 处理中数
        LambdaQueryWrapper<AlertEvent> processingWrapper = new LambdaQueryWrapper<>();
        processingWrapper.eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getDeleted, 0)
                .eq(AlertEvent::getStatus, "processing");
        if (conferenceId != null) {
            processingWrapper.eq(AlertEvent::getConferenceId, conferenceId);
        }
        stats.put("processingCount", eventMapper.selectCount(processingWrapper));

        // 已解决数
        LambdaQueryWrapper<AlertEvent> resolvedWrapper = new LambdaQueryWrapper<>();
        resolvedWrapper.eq(AlertEvent::getTenantId, tenantId)
                .eq(AlertEvent::getDeleted, 0)
                .eq(AlertEvent::getStatus, "resolved");
        if (conferenceId != null) {
            resolvedWrapper.eq(AlertEvent::getConferenceId, conferenceId);
        }
        stats.put("resolvedCount", eventMapper.selectCount(resolvedWrapper));

        // 最近7天预警趋势
        try {
            List<Map<String, Object>> trend = eventMapper.getAlertDailyTrend(tenantId);
            stats.put("dailyTrend", trend != null ? trend : Collections.emptyList());
        } catch (Exception e) {
            log.warn("获取预警趋势数据异常: {}", e.getMessage());
            stats.put("dailyTrend", Collections.emptyList());
        }

        return stats;
    }

    // === 定时预警检测 ===

    /**
     * 定时检测所有启用规则（每120秒）
     * 自动遍历所有有规则关联的会议，获取实时指标并检测是否超阈值
     */
    @Scheduled(fixedRate = 120000, initialDelay = 30000)
    public void scheduledAlertCheck() {
        try {
            Long tenantId = DEFAULT_TENANT_ID;
            // 获取所有启用的规则，按conferenceId分组
            LambdaQueryWrapper<AlertRule> ruleWrapper = new LambdaQueryWrapper<>();
            ruleWrapper.eq(AlertRule::getTenantId, tenantId)
                    .eq(AlertRule::getEnabled, 1)
                    .eq(AlertRule::getDeleted, 0);
            List<AlertRule> allRules = ruleMapper.selectList(ruleWrapper);

            Set<Long> conferenceIds = new HashSet<>();
            for (AlertRule rule : allRules) {
                if (rule.getConferenceId() != null) {
                    conferenceIds.add(rule.getConferenceId());
                }
            }

            for (Long confId : conferenceIds) {
                try {
                    // 收集指标
                    Map<String, Double> metricValues = collectMetricsForConference(confId, tenantId);
                    if (!metricValues.isEmpty()) {
                        checkRulesAndCreateAlerts(confId, tenantId, metricValues);
                    }
                } catch (Exception e) {
                    log.warn("[定时预警] 检测会议{}异常: {}", confId, e.getMessage());
                }
            }
            log.debug("[定时预警] 检测完成，共{}个会议", conferenceIds.size());
        } catch (Exception e) {
            log.warn("[定时预警] 执行异常: {}", e.getMessage());
        }
    }

    /**
     * 收集指定会议的指标值（用于定时检测）
     */
    private Map<String, Double> collectMetricsForConference(Long conferenceId, Long tenantId) {
        Map<String, Double> metricValues = new LinkedHashMap<>();
        try {
            Map<String, Object> regData = eventMapper.getRegistrationMetrics(conferenceId, tenantId);
            if (regData != null) {
                long approved = toLong(regData.get("approved"));
                long reported = toLong(regData.get("reported"));
                metricValues.put("registrationRate", approved > 0 ? Math.round(reported * 1000.0 / approved) / 10.0 : 0.0);
            }

            Map<String, Object> checkinData = eventMapper.getCheckinMetrics(conferenceId, tenantId);
            if (checkinData != null) {
                long expected = toLong(checkinData.get("expected"));
                long actual = toLong(checkinData.get("actual"));
                metricValues.put("checkinRate", expected > 0 ? Math.round(actual * 1000.0 / expected) / 10.0 : 0.0);
            }

            Map<String, Object> taskData = eventMapper.getTaskCompletionMetrics(conferenceId, tenantId);
            if (taskData != null) {
                long total = toLong(taskData.get("total"));
                long completed = toLong(taskData.get("completed"));
                metricValues.put("taskCompletionRate", total > 0 ? Math.round(completed * 1000.0 / total) / 10.0 : 0.0);
            }

            metricValues.put("apiResponseTime", measureGatewayResponseTime());
            metricValues.put("systemErrorRate", fetchSystemErrorRate());
        } catch (Exception e) {
            log.warn("收集会议{}指标异常: {}", conferenceId, e.getMessage());
        }
        return metricValues;
    }

    // === 辅助方法 ===

    /**
     * 从data服务获取系统错误率
     * 查询 /api/data/system/api-metrics 的 successRate 计算错误率
     */
    private double fetchSystemErrorRate() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(dataServiceUrl + "/api/data/system/api-metrics"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            // 简单解析 JSON 中的 successRate 字段
            int idx = body.indexOf("\"successRate\"");
            if (idx > 0) {
                int colonIdx = body.indexOf(":", idx);
                int endIdx = body.indexOf(",", colonIdx);
                if (endIdx < 0) endIdx = body.indexOf("}", colonIdx);
                String val = body.substring(colonIdx + 1, endIdx).trim();
                double successRate = Double.parseDouble(val);
                return Math.max(0, 100.0 - successRate);
            }
        } catch (Exception e) {
            log.debug("[systemErrorRate] 获取失败: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * 预警触发后发送通知（短信+系统通知）
     */
    private void sendAlertNotification(AlertRule rule, AlertEvent event, double currentValue) {
        try {
            String message = String.format("【预警通知】%s触发: 当前值%.1f, 阈值%.1f, 级别: %s",
                    rule.getName(), currentValue, rule.getThreshold().doubleValue(), rule.getSeverity());

            // 系统通知
            if (rule.getNotifySystem() != null && rule.getNotifySystem() == 1) {
                log.info("[预警系统通知] {}", message);
                // TODO: 集成系统内通知推送
            }

            // 短信通知
            if (rule.getNotifySms() != null && rule.getNotifySms() == 1) {
                notificationChannelService.sendSMS("管理员", message);
            }
        } catch (Exception e) {
            log.warn("发送预警通知异常: {}", e.getMessage());
        }
    }
}
