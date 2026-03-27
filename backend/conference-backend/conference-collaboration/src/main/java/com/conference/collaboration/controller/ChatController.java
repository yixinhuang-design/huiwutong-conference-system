package com.conference.collaboration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.collaboration.entity.ChatMessage;
import com.conference.collaboration.service.ChatMessageService;
import com.conference.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 聊天消息 Controller
 * 提供消息发送(HTTP方式)、历史记录查询、消息搜索等API
 * 实时消息收发通过WebSocket (/ws/chat) 实现
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService messageService;

    // ==================== 消息发送 ====================

    /** 通过HTTP发送消息(备用方案，推荐使用WebSocket) */
    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        ChatMessage saved = messageService.sendMessage(message);
        return Result.ok("消息发送成功", saved);
    }

    // ==================== 消息查询 ====================

    /** 获取群组聊天记录(分页) */
    @GetMapping("/group/{groupId}/messages")
    public Result<Page<ChatMessage>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ChatMessage> result = messageService.getGroupMessages(groupId, page, size);
        return Result.ok(result);
    }

    /** 获取私聊记录(分页) */
    @GetMapping("/private/messages")
    public Result<Page<ChatMessage>> getPrivateMessages(
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ChatMessage> result = messageService.getPrivateMessages(userId, targetId, page, size);
        return Result.ok(result);
    }

    /** 搜索群消息 */
    @GetMapping("/group/{groupId}/search")
    public Result<List<ChatMessage>> searchMessages(
            @PathVariable Long groupId,
            @RequestParam String keyword) {
        List<ChatMessage> messages = messageService.searchMessages(groupId, keyword);
        return Result.ok(messages);
    }

    // ==================== 统计 ====================

    /** 获取群消息统计 */
    @GetMapping("/group/{groupId}/stats")
    public Result<Map<String, Object>> getMessageStats(@PathVariable Long groupId) {
        Map<String, Object> stats = messageService.getMessageStats(groupId);
        return Result.ok(stats);
    }
}
