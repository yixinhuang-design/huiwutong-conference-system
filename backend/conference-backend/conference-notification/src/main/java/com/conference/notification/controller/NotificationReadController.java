package com.conference.notification.controller;

import com.conference.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知已读状态控制器
 * 提供已读/未读状态查询和标记功能
 *
 * @author AI Executive
 * @since 2026-04-02
 */
@Slf4j
@RestController
@RequestMapping("/api/notification/read")
@RequiredArgsConstructor
public class NotificationReadController {

    private final NotificationService notificationService;

    /**
     * 标记单条通知为已读
     *
     * @param notificationId 通知ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{notificationId}/mark-read")
    public ResponseEntity<Map<String, Object>> markRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {

        try {
            notificationService.markRead(notificationId, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "已标记为已读");
            result.put("notificationId", notificationId);
            result.put("userId", userId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[标记已读] 操作失败: notificationId={}, userId={}, error={}",
                notificationId, userId, e.getMessage(), e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "标记失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 标记会议下所有通知为已读
     *
     * @param conferenceId 会议ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllRead(
            @RequestParam Long conferenceId,
            @RequestParam Long userId) {

        try {
            notificationService.markAllRead(conferenceId, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "已标记所有通知为已读");
            result.put("conferenceId", conferenceId);
            result.put("userId", userId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[全部已读] 操作失败: conferenceId={}, userId={}, error={}",
                conferenceId, userId, e.getMessage(), e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "标记失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}
