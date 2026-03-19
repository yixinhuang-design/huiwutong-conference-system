package com.conference.collaboration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.collaboration.entity.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatMessageService extends IService<ChatMessage> {

    /** 发送消息(持久化+广播) */
    ChatMessage sendMessage(ChatMessage message);

    /** 获取群组消息历史(分页) */
    Page<ChatMessage> getGroupMessages(Long groupId, int page, int size);

    /** 获取私聊消息 */
    Page<ChatMessage> getPrivateMessages(Long userId, Long targetId, int page, int size);

    /** 搜索消息 */
    List<ChatMessage> searchMessages(Long groupId, String keyword);

    /** 获取群组消息统计 */
    Map<String, Object> getMessageStats(Long groupId);

    /** 获取今日消息总数(按会议下所有群组) */
    long getTodayMessageCount(List<Long> groupIds);

    /** 获取每个群组的今日消息数 */
    Map<Long, Long> getTodayMessageCountByGroup(List<Long> groupIds);
}
