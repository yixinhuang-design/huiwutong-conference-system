package com.conference.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.notification.dto.NotificationSendRequest;
import com.conference.notification.dto.TemplateRequest;
import com.conference.notification.entity.Notification;
import com.conference.notification.entity.NotificationTemplate;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /** 发送通知 */
    Notification sendNotification(NotificationSendRequest request);

    /** 获取通知列表(分页) */
    Page<Notification> listNotifications(Long conferenceId, String status, int page, int pageSize);

    /** 获取通知详情 */
    Notification getNotificationDetail(Long id);

    /** 保存草稿 */
    Notification saveDraft(NotificationSendRequest request);

    /** 删除通知 */
    void deleteNotification(Long id);

    /** 获取发送统计 */
    Map<String, Object> getStatistics(Long conferenceId);

    /** 催报通知 */
    Map<String, Object> sendUrge(Long conferenceId);

    // === 模板相关 ===

    /** 获取模板列表 */
    List<NotificationTemplate> listTemplates(Long conferenceId, String type);

    /** 保存/更新模板 */
    NotificationTemplate saveTemplate(TemplateRequest request);

    /** 删除模板 */
    void deleteTemplate(Long id);

    /** 获取模板详情 */
    NotificationTemplate getTemplate(Long id);

    /** 执行实际发送（供定时调度器调用） */
    void executeSend(Notification notification);
}
