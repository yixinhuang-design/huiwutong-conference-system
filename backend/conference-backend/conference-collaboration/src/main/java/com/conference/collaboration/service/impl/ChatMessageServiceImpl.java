package com.conference.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.collaboration.entity.ChatMessage;
import com.conference.collaboration.mapper.ChatMessageMapper;
import com.conference.collaboration.service.ChatMessageService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final ChatMessageMapper messageMapper;

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        Long tenantId = getTenantId();
        message.setTenantId(tenantId);
        message.setCreateTime(LocalDateTime.now());
        message.setDeleted(0);
        messageMapper.insert(message);
        log.info("[租户{}] 消息已发送: groupId={}, sender={}, type={}",
                tenantId, message.getGroupId(), message.getSenderName(), message.getMsgType());
        return message;
    }

    @Override
    public Page<ChatMessage> getGroupMessages(Long groupId, int page, int size) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getGroupId, groupId)
                .eq(ChatMessage::getDeleted, 0)
                .isNull(ChatMessage::getReceiverId)    // 群消息没有receiverId
                .orderByDesc(ChatMessage::getCreateTime);
        return messageMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<ChatMessage> getPrivateMessages(Long userId, Long targetId, int page, int size) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getDeleted, 0)
                .and(w -> w
                        .and(inner -> inner.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, targetId))
                        .or(inner -> inner.eq(ChatMessage::getSenderId, targetId).eq(ChatMessage::getReceiverId, userId))
                )
                .orderByDesc(ChatMessage::getCreateTime);
        return messageMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<ChatMessage> searchMessages(Long groupId, String keyword) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getGroupId, groupId)
                .eq(ChatMessage::getDeleted, 0)
                .like(ChatMessage::getContent, keyword)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT 50");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getMessageStats(Long groupId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getGroupId, groupId)
                .eq(ChatMessage::getDeleted, 0);
        long total = messageMapper.selectCount(wrapper);

        // 按类型统计
        LambdaQueryWrapper<ChatMessage> textWrapper = new LambdaQueryWrapper<>();
        textWrapper.eq(ChatMessage::getGroupId, groupId)
                .eq(ChatMessage::getDeleted, 0)
                .eq(ChatMessage::getMsgType, "text");
        long textCount = messageMapper.selectCount(textWrapper);

        LambdaQueryWrapper<ChatMessage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ChatMessage::getGroupId, groupId)
                .eq(ChatMessage::getDeleted, 0)
                .eq(ChatMessage::getMsgType, "image");
        long imageCount = messageMapper.selectCount(imageWrapper);

        LambdaQueryWrapper<ChatMessage> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(ChatMessage::getGroupId, groupId)
                .eq(ChatMessage::getDeleted, 0)
                .eq(ChatMessage::getMsgType, "file");
        long fileCount = messageMapper.selectCount(fileWrapper);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("textCount", textCount);
        stats.put("imageCount", imageCount);
        stats.put("fileCount", fileCount);
        return stats;
    }

    @Override
    public long getTodayMessageCount(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return 0;
        }
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ChatMessage::getGroupId, groupIds)
                .eq(ChatMessage::getDeleted, 0)
                .ge(ChatMessage::getCreateTime, todayStart);
        return messageMapper.selectCount(wrapper);
    }

    @Override
    public Map<Long, Long> getTodayMessageCountByGroup(List<Long> groupIds) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (groupIds == null || groupIds.isEmpty()) {
            return result;
        }
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        // 逐个群组统计今日消息数
        for (Long groupId : groupIds) {
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ChatMessage::getGroupId, groupId)
                    .eq(ChatMessage::getDeleted, 0)
                    .ge(ChatMessage::getCreateTime, todayStart);
            long count = messageMapper.selectCount(wrapper);
            result.put(groupId, count);
        }
        return result;
    }
}
