package com.conference.notification.service;

import com.conference.notification.entity.Notification;

import java.util.List;
import java.util.Map;

/**
 * UniPush推送服务
 * 负责向UniPush服务器发送推送通知
 *
 * @author AI Executive
 * @since 2026-04-02
 */
public interface UniPushService {

    /**
     * 发送推送给指定用户列表
     *
     * @param notification 通知对象
     * @param userIds 用户ID列表（UniPush的CID列表）
     * @return 发送结果 {success: 成功数, failed: 失败数}
     */
    Map<String, Integer> pushToUsers(Notification notification, List<String> userIds);

    /**
     * 发送推送给所有用户
     *
     * @param notification 通知对象
     * @return 发送结果 {success: 成功数, failed: 失败数}
     */
    Map<String, Integer> pushToAll(Notification notification);

    /**
     * 验证UniPush配置
     *
     * @return true如果UniPush已正确配置
     */
    boolean isConfigured();

    /**
     * 设置透传消息（不展示，仅传递数据）
     *
     * @param notification 通知对象
     * @param userIds 用户ID列表
     * @return 发送结果
     */
    Map<String, Integer> pushTransmission(Notification notification, List<String> userIds);
}
