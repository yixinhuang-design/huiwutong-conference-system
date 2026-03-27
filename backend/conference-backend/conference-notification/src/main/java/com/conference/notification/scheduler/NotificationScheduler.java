package com.conference.notification.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.notification.entity.Notification;
import com.conference.notification.mapper.NotificationMapper;
import com.conference.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时通知调度器
 * 负责检测并发送 pending 状态的定时/延迟通知
 *
 * @author AI Assistant
 * @date 2026-03-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationMapper notificationMapper;
    private final NotificationService notificationService;

    /**
     * 每30秒检查一次是否有需要发送的定时/延迟通知
     */
    @Scheduled(fixedRate = 30000)
    public void processPendingNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 查询状态为 pending 且 scheduledTime <= now 的通知
            LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Notification::getStatus, "pending")
                   .le(Notification::getScheduledTime, now)
                   .eq(Notification::getDeleted, 0);
            
            List<Notification> pendingList = notificationMapper.selectList(wrapper);
            
            if (pendingList.isEmpty()) {
                return;
            }
            
            log.info("[定时调度] 发现 {} 条待发送通知", pendingList.size());
            
            for (Notification notification : pendingList) {
                try {
                    // 更新状态为 sending
                    notification.setStatus("sending");
                    notification.setSentTime(LocalDateTime.now());
                    notification.setUpdateTime(LocalDateTime.now());
                    
                    int total = notification.getRecipientCount() != null ? notification.getRecipientCount() : 0;
                    notification.setSentCount(total);
                    notification.setDeliveredCount(0);
                    notification.setReadCount(0);
                    notification.setFailedCount(0);
                    
                    notificationMapper.updateById(notification);
                    
                    // 执行发送逻辑（复用 service 的发送逻辑）
                    notificationService.executeSend(notification);
                    
                    log.info("[定时调度] 通知已发送: id={}, title={}", notification.getId(), notification.getTitle());
                    
                } catch (Exception e) {
                    log.error("[定时调度] 发送通知失败: id={}, error={}", notification.getId(), e.getMessage(), e);
                    notification.setStatus("failed");
                    notification.setUpdateTime(LocalDateTime.now());
                    notificationMapper.updateById(notification);
                }
            }
            
        } catch (Exception e) {
            log.error("[定时调度] 处理待发送通知异常: {}", e.getMessage(), e);
        }
    }
}
