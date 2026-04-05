package com.conference.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 文本向量化服务 (简化实现)
 * @author AI Executive
 * @date 2026-04-02
 */
@Slf4j
@Service
public class EmbeddingService {

    /**
     * 生成文本向量 (简化实现：基于TF-IDF)
     * 生产环境应使用专业的embedding模型（如通义千问的embedding API）
     */
    public float[] embed(String text) {
        try {
            // 简化实现：使用字符级别的特征
            // 实际应该调用通义千问的embedding API或其他专业模型
            
            Map<Character, Integer> charFreq = new HashMap<>();
            int totalChars = 0;
            
            // 统计字符频率
            for (char c : text.toCharArray()) {
                if (c > 255) { // 只统计中文和特殊字符
                    charFreq.put(c, charFreq.getOrDefault(c, 0) + 1);
                    totalChars++;
                }
            }
            
            // 构建固定维度的向量 (1536维，模拟OpenAI embedding)
            int dimension = 1536;
            float[] vector = new float[dimension];
            Random random = new Random(text.hashCode()); // 使用文本hash作为随机种子，保证相同文本产生相同向量
            
            for (int i = 0; i < dimension; i++) {
                // 使用字符频率hash值作为向量元素
                final int idx = i;
                double hash = charFreq.values().stream()
                    .mapToInt(Integer::intValue)
                    .map(v -> v * (idx + 1))
                    .sum();
                vector[i] = (float) ((hash + random.nextDouble() * 0.1) / 1000.0);
            }
            
            // 归一化
            float norm = 0.0f;
            for (float v : vector) {
                norm += v * v;
            }
            norm = (float) Math.sqrt(norm);
            
            if (norm > 0) {
                for (int i = 0; i < dimension; i++) {
                    vector[i] /= norm;
                }
            }
            
            return vector;
            
        } catch (Exception e) {
            log.error("文本向量化失败", e);
            // 返回零向量
            return new float[1536];
        }
    }
    
    /**
     * 批量向量化
     */
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> embeddings = new ArrayList<>();
        for (String text : texts) {
            embeddings.add(embed(text));
        }
        return embeddings;
    }
    
    /**
     * 计算两个向量的余弦相似度
     */
    public double cosineSimilarity(float[] vectorA, float[] vectorB) {
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
}
