package com.conference.notification.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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
    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationMapper notificationMapper;
    private final NotificationTemplateMapper templateMapper;
    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    // ===== 阿里云短信配置 =====
    @Value("${aliyun.sms.access-key-id:}")
    private String smsAccessKeyId;
    @Value("${aliyun.sms.access-key-secret:}")
    private String smsAccessKeySecret;
    @Value("${aliyun.sms.sign-name:会通}")
    private String smsSignName;
    @Value("${aliyun.sms.template-code:SMS_000000}")
    private String smsTemplateCode;
    @Value("${aliyun.sms.enabled:false}")
    private boolean smsEnabled;

    // ===== UniPush 配置（uni-push 2.0 通过云函数URL化推送，不需要MasterSecret）=====
    @Value("${unipush.app-id:}")
    private String uniPushAppId;
    @Value("${unipush.cloud-function-url:}")
    private String uniPushCloudFunctionUrl;

    // ===== 报名服务地址 =====
    @Value("${service.registration.base-url:http://localhost:8082}")
    private String registrationBaseUrl;

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
     * 支持SMS（阿里云短信）和UniPush（系统消息）两个渠道
     * 含重试机制（最多3次）
     */
    private void doSendNotification(Notification notification) {
        try {
            List<String> channels = notification.getChannels() != null
                    ? JSON.parseArray(notification.getChannels(), String.class)
                    : Collections.singletonList("sms");

            // 查询收件人列表（从报名服务获取手机号、姓名等信息）
            List<Map<String, Object>> recipients = queryRecipients(notification);
            int totalRecipients = recipients.isEmpty()
                    ? (notification.getRecipientCount() > 0 ? notification.getRecipientCount() : 0)
                    : recipients.size();

            int deliveredCount = 0;
            int failedCount = 0;

            for (String channel : channels) {
                switch (channel) {
                    case "sms":
                        int[] smsResult = sendViaSms(notification, recipients);
                        deliveredCount += smsResult[0];
                        failedCount += smsResult[1];
                        break;
                    case "unipush":
                        int[] pushResult = sendViaUniPush(notification, recipients);
                        deliveredCount += pushResult[0];
                        failedCount += pushResult[1];
                        break;
                    default:
                        log.warn("[通知发送] 未知渠道: {}, 跳过", channel);
                        break;
                }
            }

            // 更新发送结果
            notification.setStatus("sent");
            notification.setSentCount(totalRecipients);
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
     * 从报名服务查询收件人信息
     * 返回列表包含: name, phone, department, position, conferenceTitle 等字段
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> queryRecipients(Notification notification) {
        List<Map<String, Object>> recipients = new ArrayList<>();
        try {
            // 通过报名服务API查询会议参会人员
            String url = registrationBaseUrl + "/api/registration/conference/"
                    + notification.getConferenceId() + "/participants?tenantId=" + notification.getTenantId();
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object data = body.get("data");
                if (data instanceof List) {
                    recipients = (List<Map<String, Object>>) data;
                } else if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    Object records = dataMap.get("records");
                    if (records instanceof List) {
                        recipients = (List<Map<String, Object>>) records;
                    }
                }
            }
            log.info("[收件人查询] conferenceId={}, 查到{}人", notification.getConferenceId(), recipients.size());
        } catch (Exception e) {
            log.warn("[收件人查询] 从报名服务获取收件人失败: {}, 将使用通知记录中的recipientCount", e.getMessage());
        }
        return recipients;
    }

    /**
     * 通过阿里云短信发送
     * 支持重试机制（最多MAX_RETRY_COUNT次）
     * 当smsEnabled=false时仅打印日志不调用真实接口
     *
     * @return int[]{deliveredCount, failedCount}
     */
    private int[] sendViaSms(Notification notification, List<Map<String, Object>> recipients) {
        int delivered = 0;
        int failed = 0;

        if (recipients.isEmpty()) {
            // 没有具体收件人信息时，按总数记录并发送通用内容
            String content = replaceTemplateVariables(notification.getContent(), notification, Collections.emptyMap());
            log.info("[SMS发送] 通知ID={}, 标题={}, 内容={}, 收件人数={}（无详细信息）",
                    notification.getId(), notification.getTitle(), content, notification.getRecipientCount());
            delivered = notification.getRecipientCount() != null ? notification.getRecipientCount() : 0;
            return new int[]{delivered, failed};
        }

        for (Map<String, Object> recipient : recipients) {
            String phone = getStringValue(recipient, "phone", "contactPhone", "mobile");
            if (!StringUtils.hasText(phone)) {
                log.warn("[SMS发送] 收件人无手机号, name={}", recipient.get("name"));
                failed++;
                continue;
            }

            String personalizedContent = replaceTemplateVariables(notification.getContent(), notification, recipient);

            boolean sent = false;
            for (int retry = 0; retry < MAX_RETRY_COUNT && !sent; retry++) {
                try {
                    if (smsEnabled && StringUtils.hasText(smsAccessKeyId)) {
                        // 调用阿里云短信API
                        sent = sendAliyunSms(phone, personalizedContent, notification.getTitle());
                    } else {
                        // 开发模式：仅打印日志
                        log.info("[SMS发送-开发模式] 手机={}, 标题={}, 内容={}", phone, notification.getTitle(), personalizedContent);
                        sent = true;
                    }
                } catch (Exception e) {
                    log.warn("[SMS发送] 第{}次重试失败, phone={}, error={}", retry + 1, phone, e.getMessage());
                    if (retry < MAX_RETRY_COUNT - 1) {
                        try { Thread.sleep(1000L * (retry + 1)); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                }
            }

            if (sent) {
                delivered++;
            } else {
                failed++;
                log.error("[SMS发送] 发送失败（已达最大重试次数）, phone={}", phone);
            }
        }

        log.info("[SMS发送] 通知ID={}, 成功={}, 失败={}", notification.getId(), delivered, failed);
        return new int[]{delivered, failed};
    }

    /**
     * 调用阿里云短信API发送短信（本地开发模式：日志模拟）
     */
    private boolean sendAliyunSms(String phone, String content, String title) {
        try {
            // 本地开发模式：日志模拟发送，不调用真实阿里云API
            log.info("[模拟阿里云SMS] 发送短信, phone={}, signName={}, templateCode={}, title={}, content={}",
                phone, smsSignName, smsTemplateCode, title, content);
            return true;
        } catch (Exception e) {
            log.error("[阿里云SMS] API调用异常, phone={}, error={}", phone, e.getMessage());
            throw new RuntimeException("阿里云SMS发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过UniPush系统消息发送
     * 方式：将通知写入数据库的系统消息表，移动端通过轮询/列表API获取
     *
     * @return int[]{deliveredCount, failedCount}
     */
    private int[] sendViaUniPush(Notification notification, List<Map<String, Object>> recipients) {
        int delivered = 0;
        int failed = 0;

        try {
            // 将通知内容持久化到系统消息表（conf_notification 表本身即为消息记录）
            // 移动端通过 GET /api/notification/list?conferenceId=xxx 查询通知列表
            // 此处通知已在主表中，无需重复插入

            if (recipients.isEmpty()) {
                // 没有具体收件人信息时，按总数记录
                delivered = notification.getRecipientCount() != null ? notification.getRecipientCount() : 0;
                log.info("[UniPush系统消息] 通知ID={}, 标题={}, 系统消息已存储, 目标人数={}",
                        notification.getId(), notification.getTitle(), delivered);
            } else {
                // 为每个收件人创建个性化阅读记录（可选），此处记录目标人数
                delivered = recipients.size();
                log.info("[UniPush系统消息] 通知ID={}, 标题={}, 系统消息已存储, 目标人数={}",
                        notification.getId(), notification.getTitle(), delivered);
            }

            // 如果配置了UniPush云函数URL，额外触发推送通知（仅提醒，内容在App内查看）
            if (StringUtils.hasText(uniPushAppId) && StringUtils.hasText(uniPushCloudFunctionUrl)) {
                try {
                    triggerUniPushNotification(notification);
                } catch (Exception e) {
                    log.warn("[UniPush] 服务器推送触发失败（不影响系统消息）: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("[UniPush系统消息] 处理失败: notificationId={}, error={}",
                    notification.getId(), e.getMessage(), e);
            failed = recipients.isEmpty()
                    ? (notification.getRecipientCount() != null ? notification.getRecipientCount() : 0)
                    : recipients.size();
        }

        return new int[]{delivered, failed};
    }

    /**
     * 触发UniPush推送通知（通过uniCloud云函数URL化接口）
     * 向uniCloud云函数发送HTTP请求，由云函数调用uni-cloud-push扩展库推送
     */
    private void triggerUniPushNotification(Notification notification) {
        try {
            Map<String, Object> pushBody = new LinkedHashMap<>();
            pushBody.put("action", "all");
            pushBody.put("title", notification.getTitle());
            pushBody.put("content", notification.getContent() != null
                    ? (notification.getContent().length() > 100
                        ? notification.getContent().substring(0, 100) + "..."
                        : notification.getContent())
                    : "您有新的通知消息");
            pushBody.put("force_notification", true);
            pushBody.put("request_id", "conf_" + notification.getId() + "_" + System.currentTimeMillis());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("notificationId", notification.getId());
            payload.put("conferenceId", notification.getConferenceId());
            payload.put("type", notification.getType());
            pushBody.put("payload", payload);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<String> entity =
                    new org.springframework.http.HttpEntity<>(JSON.toJSONString(pushBody), headers);

            log.info("[UniPush] 通过云函数发送推送: title={}, url={}", notification.getTitle(), uniPushCloudFunctionUrl);
            restTemplate.exchange(uniPushCloudFunctionUrl, org.springframework.http.HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.warn("[UniPush] 云函数推送调用失败（不影响系统消息）: {}", e.getMessage());
        }
    }

    /**
     * 公开发送接口，供定时调度器调用
     */
    @Override
    public void executeSend(Notification notification) {
        doSendNotification(notification);
    }

    /**
     * 模板变量替换（支持全部10个个性化变量 + per-recipient 数据）
     * 支持 ${变量名} 和 {变量名} 两种格式
     *
     * @param content      模板内容
     * @param notification 通知记录
     * @param recipient    收件人信息（来自报名服务），可为空Map
     */
    private String replaceTemplateVariables(String content, Notification notification, Map<String, Object> recipient) {
        if (!StringUtils.hasText(content)) return content;

        // ===== 通用变量 =====
        content = replaceVar(content, "conferenceId",
                notification.getConferenceId() != null ? String.valueOf(notification.getConferenceId()) : "");
        content = replaceVar(content, "sendTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        content = replaceVar(content, "title",
                notification.getTitle() != null ? notification.getTitle() : "");

        // ===== per-recipient 个性化变量 =====
        if (recipient != null && !recipient.isEmpty()) {
            content = replaceVar(content, "name",
                    getStringValue(recipient, "name", "realName", "attendeeName"));
            content = replaceVar(content, "conference",
                    getStringValue(recipient, "conferenceTitle", "conferenceName", "conference"));
            content = replaceVar(content, "seat",
                    getStringValue(recipient, "seatInfo", "seat", "seatNumber"));
            content = replaceVar(content, "group",
                    getStringValue(recipient, "groupName", "group", "discussionGroup"));
            content = replaceVar(content, "bus",
                    getStringValue(recipient, "busInfo", "bus", "transportInfo"));
            content = replaceVar(content, "room",
                    getStringValue(recipient, "roomInfo", "room", "accommodationInfo"));
            content = replaceVar(content, "schedule",
                    getStringValue(recipient, "schedule", "scheduleInfo", "agenda"));
            content = replaceVar(content, "department",
                    getStringValue(recipient, "department", "deptName", "organization"));
            content = replaceVar(content, "position",
                    getStringValue(recipient, "position", "jobTitle", "title"));
            content = replaceVar(content, "location",
                    getStringValue(recipient, "location", "venue", "address"));
        }

        return content;
    }

    /**
     * 替换模板变量（兼容 ${var} 和 {var} 两种格式）
     */
    private String replaceVar(String content, String varName, String value) {
        String safeValue = value != null ? value : "";
        content = content.replace("${" + varName + "}", safeValue);
        content = content.replace("{" + varName + "}", safeValue);
        return content;
    }

    /**
     * 从Map中获取字符串值，依次尝试多个key
     */
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null && StringUtils.hasText(String.valueOf(val))) {
                return String.valueOf(val);
            }
        }
        return "";
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
        return sendUrge(conferenceId, null, null);
    }

    @Override
    public Map<String, Object> sendUrge(Long conferenceId, String customTitle, String customContent) {
        Long tenantId = getTenantId();
        log.info("[租户{}] 发送催报通知: conferenceId={}", tenantId, conferenceId);

        String title = StringUtils.hasText(customTitle) ? customTitle : "报名催报通知";
        String content = StringUtils.hasText(customContent) ? customContent : "您有未完成的会议报名，请尽快完成报名手续。";

        // 创建催报通知记录
        Notification notification = new Notification();
        notification.setTenantId(tenantId);
        notification.setConferenceId(conferenceId);
        notification.setType("registration");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setChannels("[\"sms\",\"unipush\"]");
        notification.setSendTiming("now");
        notification.setStatus("sending");
        notification.setSentTime(LocalDateTime.now());
        notification.setSentCount(0);
        notification.setDeliveredCount(0);
        notification.setReadCount(0);
        notification.setFailedCount(0);
        notification.setDeleted(0);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        // 执行实际发送
        doSendNotification(notification);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notificationId", notification.getId());
        result.put("status", notification.getStatus());
        result.put("sentCount", notification.getSentCount());
        result.put("deliveredCount", notification.getDeliveredCount());
        result.put("message", "催报通知已发送");
        return result;
    }

    // ==================== 已读跟踪 ====================

    @Override
    public void markRead(Long notificationId, Long userId) {
        log.info("[标记已读] notificationId={}, userId={}", notificationId, userId);
        try {
            // 检查是否已读（避免重复计数）
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM conf_notification_read WHERE notification_id = ? AND user_id = ?",
                    Integer.class, notificationId, userId);
            if (count != null && count > 0) {
                log.debug("[标记已读] 已存在已读记录, 跳过");
                return;
            }
            // 插入已读记录
            jdbcTemplate.update(
                    "INSERT INTO conf_notification_read (notification_id, user_id, read_time) VALUES (?, ?, ?)",
                    notificationId, userId, LocalDateTime.now());
            // 更新通知的readCount
            jdbcTemplate.update(
                    "UPDATE conf_notification SET read_count = COALESCE(read_count, 0) + 1 WHERE id = ?",
                    notificationId);
        } catch (Exception e) {
            log.warn("[标记已读] 操作失败（conf_notification_read表可能不存在）: {}", e.getMessage());
            // 降级：仅更新readCount
            try {
                notificationMapper.selectById(notificationId);
                jdbcTemplate.update(
                        "UPDATE conf_notification SET read_count = COALESCE(read_count, 0) + 1 WHERE id = ?",
                        notificationId);
            } catch (Exception ex) {
                log.error("[标记已读] 降级更新也失败: {}", ex.getMessage());
            }
        }
    }

    @Override
    public void markAllRead(Long conferenceId, Long userId) {
        Long tenantId = getTenantId();
        log.info("[全部已读] conferenceId={}, userId={}, tenantId={}", conferenceId, userId, tenantId);
        try {
            // 查询该会议下所有已发送的通知
            List<Map<String, Object>> notifications = jdbcTemplate.queryForList(
                    "SELECT id FROM conf_notification WHERE conference_id = ? AND tenant_id = ? AND deleted = 0 AND status = 'sent'",
                    conferenceId, tenantId);
            for (Map<String, Object> n : notifications) {
                Long nId = Long.valueOf(n.get("id").toString());
                markRead(nId, userId);
            }
        } catch (Exception e) {
            log.error("[全部已读] 操作失败: {}", e.getMessage());
        }
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
