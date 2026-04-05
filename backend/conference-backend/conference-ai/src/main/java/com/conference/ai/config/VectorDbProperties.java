package com.conference.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 向量数据库配置
 * @author AI Executive
 * @date 2026-04-02
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "vectordb")
public class VectorDbProperties {
    
    /**
     * 向量数据库类型 (milvus/pinecone/memory)
     */
    private String type = "memory";
    
    /**
     * Milvus配置
     */
    private MilvusConfig milvus = new MilvusConfig();
    
    /**
     * Pinecone配置
     */
    private PineconeConfig pinecone = new PineconeConfig();
    
    /**
     * 向量维度
     */
    private Integer dimension = 1536;
    
    @Data
    public static class MilvusConfig {
        private String host = "localhost";
        private Integer port = 19530;
        private String collectionName = "conference_knowledge";
    }
    
    @Data
    public static class PineconeConfig {
        private String apiKey;
        private String environment;
        private String indexName;
    }
}
