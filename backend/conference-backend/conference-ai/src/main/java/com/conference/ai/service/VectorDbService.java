package com.conference.ai.service;

import com.conference.ai.config.VectorDbProperties;
import com.conference.ai.entity.AiKnowledge;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量数据库服务 (基于内存的简化实现)
 * @author AI Executive
 * @date 2026-04-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorDbService {

    private final VectorDbProperties config;
    private final EmbeddingService embeddingService;
    
    // 内存向量存储 (生产环境应使用Milvus/Pinecone)
    private final Map<String, float[]> vectorStore = new HashMap<>();
    private final Map<String, AiKnowledge> knowledgeStore = new HashMap<>();
    
    /**
     * 添加知识向量
     */
    public void addKnowledge(AiKnowledge knowledge, String text) {
        try {
            // 生成文本向量
            float[] embedding = embeddingService.embed(text);
            
            // 存储向量和知识
            String id = String.valueOf(knowledge.getId());
            vectorStore.put(id, embedding);
            knowledgeStore.put(id, knowledge);
            
            log.info("知识向量已添加: id={}, title={}", id, knowledge.getTitle());
        } catch (Exception e) {
            log.error("添加知识向量失败: id={}", knowledge.getId(), e);
        }
    }
    
    /**
     * 检索相似知识
     * @param query 查询文本
     * @param topK 返回前K个结果
     * @return 相似知识列表
     */
    public List<AiKnowledge> search(String query, int topK) {
        try {
            // 生成查询向量
            float[] queryEmbedding = embeddingService.embed(query);
            
            // 计算相似度
            List<Map.Entry<String, Double>> similarities = new ArrayList<>();
            
            for (Map.Entry<String, float[]> entry : vectorStore.entrySet()) {
                String id = entry.getKey();
                float[] vector = entry.getValue();
                double similarity = cosineSimilarity(queryEmbedding, vector);
                similarities.add(new AbstractMap.SimpleEntry<>(id, similarity));
            }
            
            // 按相似度降序排序
            similarities.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            
            // 返回TopK结果
            return similarities.stream()
                .limit(topK)
                .map(entry -> knowledgeStore.get(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("向量检索失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 删除知识向量
     */
    public void deleteKnowledge(String id) {
        vectorStore.remove(id);
        knowledgeStore.remove(id);
        log.info("知识向量已删除: id={}", id);
    }
    
    /**
     * 清空所有向量
     */
    public void clear() {
        vectorStore.clear();
        knowledgeStore.clear();
        log.info("向量数据库已清空");
    }
    
    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVectors", vectorStore.size());
        stats.put("dimension", config.getDimension());
        stats.put("type", config.getType());
        return stats;
    }

    // ===== 文件持久化支持 =====

    private static final String PERSIST_DIR = "./ai-vector-store";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 启动时尝试从文件恢复向量数据
     */
    @PostConstruct
    public void loadFromDisk() {
        try {
            File knowledgeFile = new File(PERSIST_DIR, "knowledge_store.json");
            File vectorFile = new File(PERSIST_DIR, "vector_store.json");
            if (knowledgeFile.exists() && vectorFile.exists()) {
                Map<String, AiKnowledge> savedKnowledge = objectMapper.readValue(
                    knowledgeFile, new TypeReference<Map<String, AiKnowledge>>() {});
                Map<String, float[]> savedVectors = objectMapper.readValue(
                    vectorFile, new TypeReference<Map<String, float[]>>() {});
                knowledgeStore.putAll(savedKnowledge);
                vectorStore.putAll(savedVectors);
                log.info("从磁盘恢复向量数据: {}条记录", knowledgeStore.size());
            }
        } catch (Exception e) {
            log.warn("从磁盘恢复向量数据失败（首次启动属正常）: {}", e.getMessage());
        }
    }

    /**
     * 关闭时持久化向量数据到文件
     */
    @PreDestroy
    public void saveToDisk() {
        try {
            File dir = new File(PERSIST_DIR);
            if (!dir.exists()) dir.mkdirs();
            objectMapper.writeValue(new File(dir, "knowledge_store.json"), knowledgeStore);
            objectMapper.writeValue(new File(dir, "vector_store.json"), vectorStore);
            log.info("向量数据已持久化到磁盘: {}条记录", knowledgeStore.size());
        } catch (Exception e) {
            log.error("持久化向量数据失败", e);
        }
    }
}
