package com.conference.ai.controller;

import com.conference.ai.entity.*;
import com.conference.ai.service.*;
import com.conference.common.result.Result;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * AI智能咨询 Controller
 * 提供: 智能对话、知识库管理、FAQ管理、反馈统计、上下文管理、功能配置
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService chatService;
    private final AiKnowledgeService knowledgeService;
    private final AiFaqService faqService;
    private final AiFeedbackService feedbackService;
    private final DocumentParseService documentParseService;

    // ===== 聊天对话 API =====

    /**
     * 发送消息 - 核心对话接口
     * POST /api/ai/chat
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.fail("消息内容不能为空");
        }
        Long conversationId = toLong(request.get("conversationId"));
        Long userId = toLong(request.get("userId"));
        String userName = (String) request.get("userName");
        Long conferenceId = toLong(request.get("conferenceId"));

        Map<String, Object> result = chatService.chat(conversationId, message.trim(), userId, userName, conferenceId);
        return Result.ok(result);
    }

    /**
     * 兼容APP端接口 - POST /api/assistant/chat
     * UniApp调用的是 {message} -> {data:{reply}}
     */
    @PostMapping("/assistant/chat")
    public Result<Map<String, Object>> assistantChat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.fail("消息内容不能为空");
        }

        Map<String, Object> chatResult = chatService.chat(null, message.trim(), null, "APP用户", null);

        // 兼容APP端期望的格式
        Map<String, Object> result = new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> aiMessage = (Map<String, Object>) chatResult.get("aiMessage");
        result.put("reply", aiMessage != null ? aiMessage.get("content") : "抱歉，处理出现问题");
        result.put("conversationId", chatResult.get("conversationId"));
        return Result.ok(result);
    }

    /**
     * 创建新对话
     */
    @PostMapping("/conversations")
    public Result<AiConversation> createConversation(@RequestBody Map<String, Object> request) {
        Long userId = toLong(request.get("userId"));
        String userName = (String) request.get("userName");
        Long conferenceId = toLong(request.get("conferenceId"));
        String title = (String) request.get("title");
        AiConversation conv = chatService.createConversation(userId, userName, conferenceId, title);
        return Result.ok(conv);
    }

    /**
     * 获取对话列表
     */
    @GetMapping("/conversations")
    public Result<List<AiConversation>> listConversations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long conferenceId) {
        return Result.ok(chatService.listConversations(userId, conferenceId));
    }

    /**
     * 获取对话消息列表
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public Result<List<AiMessage>> getConversationMessages(@PathVariable Long conversationId) {
        return Result.ok(chatService.getConversationMessages(conversationId));
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/conversations/{conversationId}")
    public Result<String> deleteConversation(@PathVariable Long conversationId) {
        chatService.deleteConversation(conversationId);
        return Result.ok("对话已删除");
    }

    /**
     * 清空对话消息
     */
    @PostMapping("/conversations/{conversationId}/clear")
    public Result<String> clearConversation(@PathVariable Long conversationId) {
        chatService.clearConversation(conversationId);
        return Result.ok("对话消息已清空");
    }

    /**
     * 评价消息
     */
    @PostMapping("/messages/{messageId}/rate")
    public Result<String> rateMessage(@PathVariable Long messageId, @RequestBody Map<String, String> request) {
        String rating = request.get("rating");
        chatService.rateMessage(messageId, rating);
        return Result.ok("评价成功");
    }

    /**
     * 重新生成回复
     */
    @PostMapping("/messages/{messageId}/regenerate")
    public Result<Map<String, Object>> regenerateResponse(@PathVariable Long messageId) {
        return Result.ok(chatService.regenerateResponse(messageId));
    }

    // ===== 知识库管理 API =====

    /**
     * 获取知识库列表
     */
    @GetMapping("/knowledge")
    public Result<List<AiKnowledge>> listKnowledge(@RequestParam(required = false) Long conferenceId) {
        return Result.ok(knowledgeService.listByConference(conferenceId));
    }

    /**
     * 添加知识
     */
    @PostMapping("/knowledge")
    public Result<AiKnowledge> addKnowledge(@RequestBody AiKnowledge knowledge) {
        return Result.ok("添加成功", knowledgeService.addKnowledge(knowledge));
    }

    /**
     * 更新知识
     */
    @PutMapping("/knowledge/{id}")
    public Result<AiKnowledge> updateKnowledge(@PathVariable Long id, @RequestBody AiKnowledge knowledge) {
        knowledge.setId(id);
        return Result.ok("更新成功", knowledgeService.updateKnowledge(knowledge));
    }

    /**
     * 删除知识
     */
    @DeleteMapping("/knowledge/{id}")
    public Result<String> deleteKnowledge(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return Result.ok("删除成功");
    }

    /**
     * 知识库统计
     */
    @GetMapping("/knowledge/stats")
    public Result<Map<String, Object>> getKnowledgeStats(@RequestParam(required = false) Long conferenceId) {
        return Result.ok(knowledgeService.getKnowledgeStats(conferenceId));
    }

    /**
     * 上传文档到知识库 - 自动解析PDF/Word/Excel/TXT文档内容并存入知识库
     * POST /api/ai/knowledge/upload
     */
    @PostMapping("/knowledge/upload")
    public Result<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "conferenceId", required = false) Long conferenceId,
            @RequestParam(value = "tags", required = false) String tags) {
        if (file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }
        log.info("收到文档上传请求: {}, 大小: {}KB", file.getOriginalFilename(), file.getSize() / 1024);
        Map<String, Object> result = documentParseService.uploadAndParse(file, category, conferenceId, tags);
        boolean success = (boolean) result.getOrDefault("success", false);
        return success ? Result.ok("文档解析成功", result) : Result.fail((String) result.get("message"));
    }

    // ===== FAQ管理 API =====

    /**
     * 获取FAQ列表
     */
    @GetMapping("/faq")
    public Result<List<AiFaq>> listFaq(@RequestParam(required = false) Long conferenceId) {
        return Result.ok(faqService.listByConference(conferenceId));
    }

    /**
     * 添加FAQ
     */
    @PostMapping("/faq")
    public Result<AiFaq> addFaq(@RequestBody AiFaq faq) {
        return Result.ok("添加成功", faqService.addFaq(faq));
    }

    /**
     * 更新FAQ
     */
    @PutMapping("/faq/{id}")
    public Result<AiFaq> updateFaq(@PathVariable Long id, @RequestBody AiFaq faq) {
        faq.setId(id);
        return Result.ok("更新成功", faqService.updateFaq(faq));
    }

    /**
     * 删除FAQ
     */
    @DeleteMapping("/faq/{id}")
    public Result<String> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return Result.ok("删除成功");
    }

    /**
     * FAQ统计
     */
    @GetMapping("/faq/stats")
    public Result<Map<String, Object>> getFaqStats(@RequestParam(required = false) Long conferenceId) {
        return Result.ok(faqService.getFaqStats(conferenceId));
    }

    // ===== 反馈 API =====

    /**
     * 提交反馈
     */
    @PostMapping("/feedback")
    public Result<AiFeedback> addFeedback(@RequestBody AiFeedback feedback) {
        return Result.ok("反馈已提交", feedbackService.addFeedback(feedback));
    }

    /**
     * 获取反馈列表
     */
    @GetMapping("/feedback")
    public Result<List<AiFeedback>> listFeedback(
            @RequestParam(required = false) Long conferenceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(feedbackService.listFeedback(conferenceId, page, size));
    }

    /**
     * 反馈统计
     */
    @GetMapping("/feedback/stats")
    public Result<Map<String, Object>> getFeedbackStats(@RequestParam(required = false) Long conferenceId) {
        return Result.ok(feedbackService.getFeedbackStats(conferenceId));
    }

    // ===== AI统计 API =====

    /**
     * AI总体统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getAiStats(@RequestParam(required = false) Long conferenceId) {
        return Result.ok(chatService.getAiStats(conferenceId));
    }

    // ===== OCR & 语音 (保留原有接口) =====

    @PostMapping("/ocr")
    public Result<Map<String, Object>> ocrRecognition(
            @RequestParam String imageUrl,
            @RequestParam String type) {
        Long tenantId = TenantContextHolder.getTenantId();
        log.info("[租户{}] 执行OCR识别, 类型: {}", tenantId, type);
        Map<String, Object> result = new HashMap<>();
        result.put("tenantId", tenantId != null ? tenantId.toString() : null);
        result.put("recognitionId", "1");
        result.put("type", type);
        result.put("confidence", 0.95);
        return Result.ok("OCR识别完成", result);
    }

    @PostMapping("/speech-recognition")
    public Result<Map<String, String>> speechRecognition(@RequestParam String audioUrl) {
        Map<String, String> result = new HashMap<>();
        result.put("text", "识别的文本内容");
        result.put("language", "zh-CN");
        return Result.ok(result);
    }

    // ===== 工具方法 =====

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); } catch (Exception e) { return null; }
    }
}
