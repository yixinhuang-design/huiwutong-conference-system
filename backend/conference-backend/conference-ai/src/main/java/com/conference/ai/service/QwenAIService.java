package com.conference.ai.service;

import com.conference.ai.config.QwenProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通义千问AI服务
 * @author AI Executive
 * @date 2026-04-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QwenAIService {

    private final QwenProperties qwenProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 聊天对话
     * @param message 用户消息
     * @param conversationId 对话ID (可选，用于多轮对话)
     * @param context 上下文信息 (可选)
     * @return AI回复
     */
    public String chat(String message, String conversationId, Map<String, Object> context) {
        // 检查API Key是否已配置
        if (!isApiKeyConfigured()) {
            log.warn("通义千问API Key未配置，使用本地回答模式");
            return null; // 返回null让调用方使用本地回答
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", qwenProperties.getModel());
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示
            messages.add(Map.of("role", "system", "content", buildSystemPrompt(context)));
            
            // TODO: 如果有conversationId，从数据库加载历史消息
            // List<ChatMessage> history = loadConversationHistory(conversationId);
            // for (ChatMessage msg : history) {
            //     messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            // }
            
            // 添加当前用户消息
            messages.add(Map.of("role", "user", "content", message));
            
            requestBody.put("input", Map.of("messages", messages));
            
            // 设置参数
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("max_tokens", qwenProperties.getMaxTokens());
            parameters.put("temperature", qwenProperties.getTemperature());
            parameters.put("top_p", qwenProperties.getTopP());
            requestBody.put("parameters", parameters);
            
            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(qwenProperties.getApiKey());
            headers.set("X-DashScope-SSE", "disable"); // 禁用SSE流式返回
            
            HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), 
                headers
            );
            
            ResponseEntity<String> response = restTemplate.exchange(
                qwenProperties.getEndpoint(),
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // 解析响应
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode output = root.path("output");
            String reply = output.path("text").asText();
            
            if (reply == null || reply.isEmpty()) {
                log.warn("通义千问返回空响应: {}", response.getBody());
                return "抱歉，我暂时无法回答这个问题。";
            }
            
            return reply;
            
        } catch (Exception e) {
            log.error("通义千问调用失败", e);
            return "抱歉，AI服务暂时不可用，请稍后重试。错误: " + e.getMessage();
        }
    }
    
    /**
     * 构建系统提示词 - 整合会议全维度知识
     */
    private String buildSystemPrompt(Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的智能会议助手（AI助教），负责回答用户关于会议的各种问题。\n");
        prompt.append("你可以精准回答会议日程、座位、报名、住宿、餐饮、交通、分组、资料等所有与会议相关的问题。\n\n");
        
        // 添加用户上下文信息
        if (context != null) {
            if (context.containsKey("conferenceName")) {
                prompt.append("【当前会议】").append(context.get("conferenceName")).append("\n");
            }
            if (context.containsKey("userName")) {
                prompt.append("【提问用户】").append(context.get("userName")).append("\n");
            }
            if (context.containsKey("userRole")) {
                prompt.append("【用户角色】").append(context.get("userRole")).append("\n");
            }
            if (context.containsKey("userPersonalInfo")) {
                prompt.append("\n").append(context.get("userPersonalInfo")).append("\n");
            }

            // 嵌入会议知识库（核心增强部分）
            if (context.containsKey("conferenceKnowledge")) {
                prompt.append("\n").append(context.get("conferenceKnowledge")).append("\n");
            }
        }
        
        prompt.append("\n【回答规范】\n");
        prompt.append("1. 请基于上述知识库信息准确回答，优先使用知识库中的真实数据。\n");
        prompt.append("2. 回答时使用Markdown格式，善用emoji、加粗、列表等增强可读性。\n");
        prompt.append("3. 如果用户问的是具体日程，按时间顺序列出，标注当前正在进行和下一个日程。\n");
        prompt.append("4. 如果用户问座位，给出具体的座位号和会场位置。\n");
        prompt.append("5. 如果用户问交通/住宿/餐饮，给出具体的时间地点联系方式等细节。\n");
        prompt.append("6. 如果知识库中没有相关信息，诚实告知并建议用户联系会务组或查看相关管理页面。\n");
        prompt.append("7. 回答简洁明了，避免过于冗长，但信息要完整。\n");
        prompt.append("8. 如果用户提到具体人名，尝试从报名人员或分组信息中查找相关人员信息。\n");
        
        return prompt.toString();
    }
    
    /**
     * 生成文本摘要
     */
    public String summarize(String text, int maxLength) {
        try {
            String prompt = String.format(
                "请将以下内容总结为不超过%d字的摘要：\n\n%s",
                maxLength,
                text
            );
            
            return chat(prompt, null, null);
        } catch (Exception e) {
            log.error("文本摘要生成失败", e);
            return text.substring(0, Math.min(maxLength, text.length()));
        }
    }
    
    /**
     * 翻译文本
     */
    public String translate(String text, String targetLanguage) {
        try {
            String prompt = String.format(
                "请将以下内容翻译为%s：\n\n%s",
                targetLanguage,
                text
            );
            
            return chat(prompt, null, null);
        } catch (Exception e) {
            log.error("文本翻译失败", e);
            return text;
        }
    }
    
    /**
     * 提取关键词
     */
    public List<String> extractKeywords(String text) {
        // 简化实现：使用分词
        // 实际应该调用AI或使用NLP工具
        return List.of(text.split("\\s+"));
    }

    /**
     * 检查API Key是否已真正配置
     */
    public boolean isApiKeyConfigured() {
        String key = qwenProperties.getApiKey();
        return key != null
                && !key.isBlank()
                && !key.contains("your-")
                && !key.equals("test-key")
                && !key.equals("placeholder");
    }
}
