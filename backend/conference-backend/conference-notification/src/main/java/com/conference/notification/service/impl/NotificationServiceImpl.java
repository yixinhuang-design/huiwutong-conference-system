package com.conference.notification.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.notification.dto.NotificationSendRequest;
import com.conference.notification.dto.TemplateRequest;
import com.conference.notification.entity.Notification;
import com.conference.notification.entity.NotificationTemplate;
import com.conference.notification.mapper.NotificationMapper;
import com.conference.notification.mapper.NotificationTemplateMapper;
import com.conference.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final NotificationMapper notificationMapper;
    private final NotificationTemplateMapper templateMapper;

    private Long getTenantId() {
        try {
            Long tenantId = com.conference.common.tenant.TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    public Notification sendNotification(NotificationSendRequest request) {
        Long tenantId = getTenantId();
        log.info("[租户{}] 发送通知: type={}, conferenceId={}", tenantId, request.getType(), request.getConferenceId());

        Notification notification = new Notification();
        notification.setTenantId(tenantId);
        notification.setConferenceId(request.getConferenceId());
        notification.setTemplateId(request.getTemplateId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setChannels(request.getChannels() != null ? JSON.toJSONString(request.getChannels()) : null);
        notification.setRecipientFilter(request.getFilters() != null ? JSON.toJSONString(request.getFilters()) : null);
        notification.setRecipientCount(request.getRecipientCount() != null ? request.getRecipientCount() : 0);
        notification.setSendTiming(request.getSendTiming() != null ? request.getSendTiming() : "now");
        notification.setOptions(request.getOptions() != null ? JSON.toJSONString(request.getOptions()) : null);
        notification.setDeleted(0);

        // 处理发送时机
        String timing = notification.getSendTiming();
        if ("scheduled".equals(timing) && StringUtils.hasText(request.getScheduledTime())) {
            notification.setScheduledTime(
                    LocalDateTime.parse(request.getScheduledTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
            notification.setStatus("pending");
        } else if ("delayed".equals(timing) && request.getDelayedMinutes() != null) {
            int minutes = request.getDelayedMinutes();
            if ("hours".equals(request.getDelayedUnit())) {
                minutes = minutes * 60;
            }
            notification.setScheduledTime(LocalDateTime.now().plusMinutes(minutes));
            notification.setStatus("pending");
        } else {
            // 立即发送
            notification.setStatus("sending");
            notification.setSentTime(LocalDateTime.now());
            int total = notification.getRecipientCount() > 0 ? notification.getRecipientCount() : 0;
            notification.setSentCount(total);
            notification.setDeliveredCount(0);
            notification.setReadCount(0);
            notification.setFailedCount(0);
        }

        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        // 立即发送的通知，执行实际发送逻辑
        if ("sending".equals(notification.getStatus())) {
            doSendNotification(notification);
        }

        log.info("[租户{}] 通知已创建: id={}, status={}", tenantId, notification.getId(), notification.getStatus());
        return notification;
    }

    /**
     * 执行实际发送逻辑
     * 当前仅支持SMS渠道，后续可扩展UniPush等
     */
    private void doSendNotification(Notification notification) {
        try {
            List<String> channels = notification.getChannels() != null
                    ? JSON.parseArray(notification.getChannels(), String.class)
                    : Collections.singletonList("sms");

            String content = replaceTemplateVariables(notification.getContent(), notification);

            int sentCount = notification.getSentCount();
            int deliveredCount = 0;
            int failedCount = 0;

            for (String channel : channels) {
                switch (channel) {
                    case "sms":
                        // TODO: 接入实际短信服务SDK（如阿里云短信、腾讯云短信）
                        log.info("[通知发送] SMS渠道 - 通知ID={}, 标题={}, 接收人数={}, 内容={}",
                                notification.getId(), notification.getTitle(), sentCount, content);
                        deliveredCount = sentCount; // 标记为已投递，实际接入SDK后由回调更新
                        break;
                    case "unipush":
                        // TODO: 接入UniPush 2.0推送SDK
                        log.info("[通知发送] UniPush渠道 - 通知ID={}, 标题={}", notification.getId(), notification.getTitle());
                        break;
                    default:
                        log.warn("[通知发送] 未知渠道: {}, 跳过", channel);
                        break;
                }
            }

            // 更新发送结果
            notification.setStatus("sent");
            notification.setDeliveredCount(deliveredCount);
            notification.setFailedCount(failedCount);
            notification.setUpdateTime(LocalDateTime.now());
            notificationMapper.updateById(notification);

        } catch (Exception e) {
            log.error("[通知发送] 发送失败: notificationId={}, error={}", notification.getId(), e.getMessage(), e);
            notification.setStatus("failed");
            notification.setUpdateTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }

    /**
     * 模板变量替换
     * 支持 ${变量名} 格式的变量替换
     */
    private String replaceTemplateVariables(String content, Notification notification) {
        if (!StringUtils.hasText(content)) return content;

        // 替换通用变量
        content = content.replace("${conferenceId}", notification.getConferenceId() != null ? String.valueOf(notification.getConferenceId()) : "");
        content = content.replace("${sendTime}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        content = content.replace("${title}", notification.getTitle() != null ? notification.getTitle() : "");

        return content;
    }

    @Override
    public Page<Notification> listNotifications(Long conferenceId, String status, int page, int pageSize) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getTenantId, tenantId)
                .eq(Notification::getDeleted, 0);

        if (conferenceId != null) {
            wrapper.eq(Notification::getConferenceId, conferenceId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Notification::getStatus, status);
        }
        wrapper.orderByDesc(Notification::getCreateTime);

        return notificationMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public Notification getNotificationDetail(Long id) {
        return notificationMapper.selectById(id);
    }

    @Override
    public Notification saveDraft(NotificationSendRequest request) {
        Long tenantId = getTenantId();
        Notification notification = new Notification();
        notification.setTenantId(tenantId);
        notification.setConferenceId(request.getConferenceId());
        notification.setTemplateId(request.getTemplateId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setChannels(request.getChannels() != null ? JSON.toJSONString(request.getChannels()) : null);
        notification.setRecipientFilter(request.getFilters() != null ? JSON.toJSONString(request.getFilters()) : null);
        notification.setRecipientCount(request.getRecipientCount() != null ? request.getRecipientCount() : 0);
        notification.setSendTiming(request.getSendTiming());
        notification.setOptions(request.getOptions() != null ? JSON.toJSONString(request.getOptions()) : null);
        notification.setStatus("draft");
        notification.setDeleted(0);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());

        notificationMapper.insert(notification);
        log.info("[租户{}] 草稿已保存: id={}", tenantId, notification.getId());
        return notification;
    }

    @Override
    public void deleteNotification(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification != null) {
            notification.setDeleted(1);
            notification.setUpdateTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public Map<String, Object> getStatistics(Long conferenceId) {
        Long tenantId = getTenantId();
        Map<String, Object> stats = new LinkedHashMap<>();

        // 发送汇总
        Map<String, Object> sendStats = notificationMapper.getSendStats(conferenceId, tenantId);
        if (sendStats != null) {
            stats.putAll(sendStats);
        } else {
            stats.put("totalSent", 0);
            stats.put("delivered", 0);
            stats.put("readCount", 0);
            stats.put("failed", 0);
        }

        // 状态分布
        List<Map<String, Object>> statusStats = notificationMapper.getStatusStats(conferenceId, tenantId);
        Map<String, Object> statusMap = new LinkedHashMap<>();
        for (Map<String, Object> s : statusStats) {
            statusMap.put(String.valueOf(s.get("status")), s.get("count"));
        }
        stats.put("statusDistribution", statusMap);

        // 通知总数
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getTenantId, tenantId)
                .eq(Notification::getDeleted, 0);
        if (conferenceId != null) {
            wrapper.eq(Notification::getConferenceId, conferenceId);
        }
        stats.put("total", notificationMapper.selectCount(wrapper));

        return stats;
    }

    @Override
    public Map<String, Object> sendUrge(Long conferenceId) {
        Long tenantId = getTenantId();
        log.info("[租户{}] 发送催报通知: conferenceId={}", tenantId, conferenceId);

        // 创建催报通知记录
        Notification notification = new Notification();
        notification.setTenantId(tenantId);
        notification.setConferenceId(conferenceId);
        notification.setType("registration");
        notification.setTitle("报名催报通知");
        notification.setContent("您有未完成的会议报名，请尽快完成报名手续。");
        notification.setChannels("[\"sms\"]");
        notification.setSendTiming("now");
        notification.setStatus("sent");
        notification.setSentTime(LocalDateTime.now());
        notification.setSentCount(0);
        notification.setDeliveredCount(0);
        notification.setReadCount(0);
        notification.setFailedCount(0);
        notification.setDeleted(0);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notificationId", notification.getId());
        result.put("status", "sent");
        result.put("message", "催报通知已发送");
        return result;
    }

    // === 模板操作 ===

    @Override
    public List<NotificationTemplate> listTemplates(Long conferenceId, String type) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationTemplate::getTenantId, tenantId)
                .eq(NotificationTemplate::getDeleted, 0)
                .eq(NotificationTemplate::getStatus, 1);

        if (conferenceId != null) {
            // 查询全局模板 + 指定会议模板
            wrapper.and(w -> w.isNull(NotificationTemplate::getConferenceId)
                    .or().eq(NotificationTemplate::getConferenceId, conferenceId));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(NotificationTemplate::getType, type);
        }
        wrapper.orderByAsc(NotificationTemplate::getType);

        return templateMapper.selectList(wrapper);
    }

    @Override
    public NotificationTemplate saveTemplate(TemplateRequest request) {
        Long tenantId = getTenantId();
        NotificationTemplate template;

        if (request.getId() != null) {
            template = templateMapper.selectById(request.getId());
            if (template == null) {
                throw new RuntimeException("模板不存在: " + request.getId());
            }
        } else {
            template = new NotificationTemplate();
            template.setTenantId(tenantId);
            template.setStatus(1);
            template.setDeleted(0);
            template.setCreateTime(LocalDateTime.now());
        }

        template.setConferenceId(request.getConferenceId());
        template.setName(request.getName());
        template.setType(request.getType());
        template.setTitle(request.getTitle());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setUpdateTime(LocalDateTime.now());

        if (request.getId() != null) {
            templateMapper.updateById(template);
        } else {
            templateMapper.insert(template);
        }

        log.info("[租户{}] 模板已保存: id={}, name={}", tenantId, template.getId(), template.getName());
        return template;
    }

    @Override
    public void deleteTemplate(Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template != null) {
            template.setDeleted(1);
            template.setUpdateTime(LocalDateTime.now());
            templateMapper.updateById(template);
        }
    }

    @Override
    public NotificationTemplate getTemplate(Long id) {
        return templateMapper.selectById(id);
    }
}
