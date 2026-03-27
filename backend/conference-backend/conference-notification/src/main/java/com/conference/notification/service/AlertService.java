package com.conference.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.notification.dto.AlertRuleRequest;
import com.conference.notification.entity.AlertEvent;
import com.conference.notification.entity.AlertRule;

import java.util.List;
import java.util.Map;

/**
 * 预警服务接口
 */
public interface AlertService extends IService<AlertEvent> {

    // === 预警规则 ===

    /** 获取规则列表 */
    List<AlertRule> listRules(Long conferenceId);

    /** 保存规则 */
    AlertRule saveRule(AlertRuleRequest request);

    /** 更新规则 */
    AlertRule updateRule(Long id, AlertRuleRequest request);

    /** 删除规则 */
    void deleteRule(Long id);

    /** 切换启用/禁用 */
    AlertRule toggleRule(Long id);

    // === 实时指标 ===

    /** 获取实时监控指标 */
    Map<String, Object> getMetrics(Long conferenceId);

    // === 预警事件 ===

    /** 获取预警事件列表 */
    List<AlertEvent> listAlerts(Long conferenceId, String status);

    /** 开始处理预警 */
    AlertEvent processAlert(Long id);

    /** 解决预警 */
    AlertEvent resolveAlert(Long id, String remark);

    // === 统计 ===

    /** 获取预警统计 */
    Map<String, Object> getAlertStatistics(Long conferenceId);
}
