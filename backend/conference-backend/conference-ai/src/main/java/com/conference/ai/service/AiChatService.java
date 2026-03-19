package com.conference.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.ai.entity.AiConversation;
import com.conference.ai.entity.AiMessage;

import java.util.List;
import java.util.Map;

/**
 * AI聊天服务接口
 */
public interface AiChatService extends IService<AiConversation> {

    /**
     * 发送消息并获取AI回复
     */
    Map<String, Object> chat(Long conversationId, String message, Long userId, String userName, Long conferenceId);

    /**
     * 创建新对话
     */
    AiConversation createConversation(Long userId, String userName, Long conferenceId, String title);

    /**
     * 获取对话列表
     */
    List<AiConversation> listConversations(Long userId, Long conferenceId);

    /**
     * 获取对话的消息列表
     */
    List<AiMessage> getConversationMessages(Long conversationId);

    /**
     * 删除对话
     */
    void deleteConversation(Long conversationId);

    /**
     * 清空对话消息
     */
    void clearConversation(Long conversationId);

    /**
     * 评价消息
     */
    void rateMessage(Long messageId, String rating);

    /**
     * 重新生成回复
     */
    Map<String, Object> regenerateResponse(Long messageId);

    /**
     * 获取AI统计信息
     */
    Map<String, Object> getAiStats(Long conferenceId);
}
