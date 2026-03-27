package com.conference.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.common.result.Result;
import com.conference.notification.dto.NotificationSendRequest;
import com.conference.notification.dto.TemplateRequest;
import com.conference.notification.entity.Notification;
import com.conference.notification.entity.NotificationTemplate;
import com.conference.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 精准通知管理 Controller
 * 提供通知发送、模板管理、草稿保存、统计查询、催报等完整API
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ==================== 通知发送 ====================

    /**
     * 发送通知（支持立即/延迟/定时发送）
     */
    @PostMapping("/send")
    public Result<Notification> sendNotification(@RequestBody NotificationSendRequest request) {
        Notification notification = notificationService.sendNotification(request);
        return Result.ok("通知发送成功", notification);
    }

    /**
     * 保存草稿
     */
    @PostMapping("/draft")
    public Result<Notification> saveDraft(@RequestBody NotificationSendRequest request) {
        Notification draft = notificationService.saveDraft(request);
        return Result.ok("草稿已保存", draft);
    }

    /**
     * 催报通知
     */
    @PostMapping("/urge")
    public Result<Map<String, Object>> sendUrge(@RequestBody Map<String, Object> body) {
        Long conferenceId = Long.valueOf(body.getOrDefault("conferenceId", "0").toString());
        Map<String, Object> result = notificationService.sendUrge(conferenceId);
        return Result.ok("催报通知已发送", result);
    }

    // ==================== 通知查询 ====================

    /**
     * 获取通知列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<Notification>> listNotifications(
            @RequestParam(required = false) Long conferenceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Notification> result = notificationService.listNotifications(conferenceId, status, page, pageSize);
        return Result.ok(result);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    public Result<Notification> getNotification(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationDetail(id);
        if (notification == null) {
            return Result.fail("通知不存在");
        }
        return Result.ok(notification);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.ok("通知已删除", null);
    }

    /**
     * 获取发送统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(
            @RequestParam(required = false) Long conferenceId) {
        Map<String, Object> stats = notificationService.getStatistics(conferenceId);
        return Result.ok(stats);
    }

    // ==================== 模板管理 ====================

    /**
     * 获取模板列表
     */
    @GetMapping("/template/list")
    public Result<List<NotificationTemplate>> listTemplates(
            @RequestParam(required = false) Long conferenceId,
            @RequestParam(required = false) String type) {
        List<NotificationTemplate> templates = notificationService.listTemplates(conferenceId, type);
        return Result.ok(templates);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/template/{id}")
    public Result<NotificationTemplate> getTemplate(@PathVariable Long id) {
        NotificationTemplate template = notificationService.getTemplate(id);
        if (template == null) {
            return Result.fail("模板不存在");
        }
        return Result.ok(template);
    }

    /**
     * 保存/更新模板
     */
    @PostMapping("/template/save")
    public Result<NotificationTemplate> saveTemplate(@RequestBody TemplateRequest request) {
        NotificationTemplate template = notificationService.saveTemplate(request);
        return Result.ok("模板已保存", template);
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/template/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        notificationService.deleteTemplate(id);
        return Result.ok("模板已删除", null);
    }
}
