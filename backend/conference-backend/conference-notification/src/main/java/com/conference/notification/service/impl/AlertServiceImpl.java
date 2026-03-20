package com.conference.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.notification.dto.AlertRuleRequest;
import com.conference.notification.entity.AlertEvent;
import com.conference.notification.entity.AlertRule;
import com.conference.notification.mapper.AlertEventMapper;
import com.conference.notification.mapper.AlertRuleMapper;
import com.conference.notification.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
        AlertRule rule = ruleMapper.selectById(id);
        if (rule != null) {
            rule.setDeleted(1);
            rule.setUpdateTime(LocalDateTime.now());
            ruleMapper.updateById(rule);
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

            // 任务完成率/API响应时间暂不可跨服务获取
            metricValues.put("taskCompletionRate", 0.0);
            metricValues.put("apiResponseTime", 0.0);
            metrics.put("taskCompletionRate", buildMetric(0, "任务完成率", "%", "stable"));
            metrics.put("apiResponseTime", buildMetric(0, "API响应时间", "ms", "stable"));

        } catch (Exception e) {
            log.error("获取实时指标异常: {}", e.getMessage(), e);
            metrics.put("registrationRate", buildMetric(0, "报到率", "%", "stable"));
            metrics.put("checkinRate", buildMetric(0, "签到率", "%", "stable"));
            metrics.put("taskCompletionRate", buildMetric(0, "任务完成率", "%", "stable"));
            metrics.put("apiResponseTime", buildMetric(0, "API响应时间", "ms", "stable"));
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
            if (currentValue == null || currentValue == 0) continue;

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
        eventMapper.updateById(event);
        log.info("预警事件开始处理: id={}", id);
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
}
