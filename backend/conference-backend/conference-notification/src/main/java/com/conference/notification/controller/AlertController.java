package com.conference.notification.controller;

import com.conference.common.result.Result;
import com.conference.notification.dto.AlertRuleRequest;
import com.conference.notification.entity.AlertEvent;
import com.conference.notification.entity.AlertRule;
import com.conference.notification.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 预警管理 Controller
 * 提供预警规则CRUD、实时监控指标、预警事件处理、统计分析等完整API
 */
@Slf4j
@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // ==================== 预警规则 ====================

    /**
     * 获取预警规则列表
     */
    @GetMapping("/rule/list")
    public Result<List<AlertRule>> listRules(
            @RequestParam(required = false) Long conferenceId) {
        List<AlertRule> rules = alertService.listRules(conferenceId);
        return Result.ok(rules);
    }

    /**
     * 添加预警规则
     */
    @PostMapping("/rule/save")
    public Result<AlertRule> saveRule(@RequestBody AlertRuleRequest request) {
        AlertRule rule = alertService.saveRule(request);
        return Result.ok("规则已添加", rule);
    }

    /**
     * 更新预警规则
     */
    @PutMapping("/rule/{id}")
    public Result<AlertRule> updateRule(@PathVariable Long id, @RequestBody AlertRuleRequest request) {
        AlertRule rule = alertService.updateRule(id, request);
        return Result.ok("规则已更新", rule);
    }

    /**
     * 删除预警规则
     */
    @DeleteMapping("/rule/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        alertService.deleteRule(id);
        return Result.ok("规则已删除", null);
    }

    /**
     * 启用/禁用预警规则
     */
    @PutMapping("/rule/{id}/toggle")
    public Result<AlertRule> toggleRule(@PathVariable Long id) {
        AlertRule rule = alertService.toggleRule(id);
        return Result.ok("规则状态已切换", rule);
    }

    // ==================== 实时监控 ====================

    /**
     * 获取实时监控指标
     */
    @GetMapping("/metrics")
    public Result<Map<String, Object>> getMetrics(
            @RequestParam(required = false) Long conferenceId) {
        Map<String, Object> metrics = alertService.getMetrics(conferenceId);
        return Result.ok(metrics);
    }

    // ==================== 预警事件 ====================

    /**
     * 获取预警事件列表
     */
    @GetMapping("/list")
    public Result<List<AlertEvent>> listAlerts(
            @RequestParam(required = false) Long conferenceId,
            @RequestParam(required = false) String status) {
        List<AlertEvent> alerts = alertService.listAlerts(conferenceId, status);
        return Result.ok(alerts);
    }

    /**
     * 开始处理预警
     */
    @PutMapping("/{id}/process")
    public Result<AlertEvent> processAlert(@PathVariable Long id) {
        AlertEvent event = alertService.processAlert(id);
        return Result.ok("预警已开始处理", event);
    }

    /**
     * 解决预警
     */
    @PutMapping("/{id}/resolve")
    public Result<AlertEvent> resolveAlert(
            @PathVariable Long id,
            @RequestParam(required = false) String remark) {
        AlertEvent event = alertService.resolveAlert(id, remark);
        return Result.ok("预警已解决", event);
    }

    // ==================== 统计分析 ====================

    /**
     * 获取预警统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getAlertStats(
            @RequestParam(required = false) Long conferenceId) {
        Map<String, Object> stats = alertService.getAlertStatistics(conferenceId);
        return Result.ok(stats);
    }
}
