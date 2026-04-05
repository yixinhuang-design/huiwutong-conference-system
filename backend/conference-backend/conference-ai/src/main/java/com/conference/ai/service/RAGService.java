package com.conference.ai.service;

import com.conference.ai.entity.AiKnowledge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) 服务
 * 检索增强生成：结合知识库检索和LLM生成
 * @author AI Executive
 * @date 2026-04-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RAGService {

    private final QwenAIService aiService;
    private final VectorDbService vectorDbService;
    private final KnowledgeService knowledgeService;

    /**
     * 获取AI服务实例（供外部使用，如知识增强问答）
     */
    public QwenAIService getAiService() {
        return aiService;
    }

    /**
     * RAG聊天：检索相关知识 → 增强提示词 → 生成回答
     * @param query 用户查询
     * @param conversationId 对话ID
     * @param context 上下文
     * @return AI回复
     */
    public String chat(String query, String conversationId, Map<String, Object> context) {
        try {
            // Step 1: 检索相关知识 (Top-3)
            List<AiKnowledge> relevantKnowledge = vectorDbService.search(query, 3);
            
            if (!relevantKnowledge.isEmpty()) {
                log.info("检索到{}条相关知识", relevantKnowledge.size());
                
                // Step 2: 构建增强提示词
                String enhancedPrompt = buildRAGPrompt(query, relevantKnowledge, context);
                
                // Step 3: 调用LLM生成回答
                String response = aiService.chat(enhancedPrompt, conversationId, context);
                
                if (response != null) {
                    // Step 4: 记录知识使用情况
                    for (AiKnowledge k : relevantKnowledge) {
                        knowledgeService.incrementViews(k.getId());
                    }
                    return response;
                }
                // AI未配置时，返回知识库检索结果拼接
                StringBuilder sb = new StringBuilder("📋 **知识库检索结果**\n\n");
                for (AiKnowledge k : relevantKnowledge) {
                    sb.append("**").append(k.getTitle()).append("**\n");
                    sb.append(k.getContent() != null ? k.getContent() : k.getSummary()).append("\n\n");
                    knowledgeService.incrementViews(k.getId());
                }
                return sb.toString();
            } else {
                // 无相关知识，直接调用AI
                log.info("未检索到相关知识，使用直接回答");
                String response = aiService.chat(query, conversationId, context);
                return response != null ? response : "抱歉，AI服务暂未配置，且未找到相关知识库内容。请稍后重试。";
            }
            
        } catch (Exception e) {
            log.error("RAG聊天失败", e);
            return "抱歉，处理您的问题时出现错误。请稍后重试。";
        }
    }

    /**
     * 构建RAG增强提示词
     */
    private String buildRAGPrompt(String query, List<AiKnowledge> knowledge, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请根据以下知识库信息回答用户问题。如果知识库中有相关信息，请优先使用这些信息回答。\n\n");
        
        // 添加知识库内容
        prompt.append("【知识库信息】\n");
        for (int i = 0; i < knowledge.size(); i++) {
            AiKnowledge k = knowledge.get(i);
            prompt.append(String.format("%d. %s\n", i + 1, k.getTitle()));
            prompt.append(String.format("   %s\n\n", k.getContent()));
        }
        
        prompt.append("【用户问题】\n");
        prompt.append(query);
        
        prompt.append("\n\n请基于以上知识库信息回答问题。如果知识库信息不足以回答，请说明并尽力提供帮助。");
        
        return prompt.toString();
    }

    /**
     * 批量添加知识到向量库
     */
    public void indexKnowledge(List<AiKnowledge> knowledgeList) {
        for (AiKnowledge k : knowledgeList) {
            try {
                // 使用标题+内容作为索引文本
                String text = k.getTitle() + "\n" + k.getContent();
                vectorDbService.addKnowledge(k, text);
            } catch (Exception e) {
                log.error("索引知识失败: id={}", k.getId(), e);
            }
        }
        log.info("知识库索引完成: {}条", knowledgeList.size());
    }

    /**
     * 重新索引所有知识
     */
    public void reindexAll() {
        log.info("开始重新索引所有知识...");
        
        // 清空向量库
        vectorDbService.clear();
        
        // 加载所有知识
        List<AiKnowledge> allKnowledge = knowledgeService.getAll();
        
        // 重建索引
        indexKnowledge(allKnowledge);
        
        log.info("重新索引完成");
    }

    /**
     * 相似问题推荐
     */
    public List<String> getSimilarQuestions(String query, int topK) {
        try {
            List<AiKnowledge> similar = vectorDbService.search(query, topK);
            return similar.stream()
                .map(AiKnowledge::getTitle)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取相似问题失败", e);
            return Collections.emptyList();
        }
    }
}
