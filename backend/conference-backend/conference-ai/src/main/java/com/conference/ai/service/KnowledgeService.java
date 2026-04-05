package com.conference.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.conference.ai.entity.AiKnowledge;
import com.conference.ai.mapper.AiKnowledgeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识服务（供RAGService使用）
 * @author AI Executive
 * @date 2026-04-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final AiKnowledgeMapper knowledgeMapper;

    /**
     * 增加浏览次数
     */
    public void incrementViews(Long id) {
        knowledgeMapper.update(null,
            new LambdaUpdateWrapper<AiKnowledge>()
                .eq(AiKnowledge::getId, id)
                .setSql("views = IFNULL(views, 0) + 1"));
    }

    /**
     * 获取所有活跃知识
     */
    public List<AiKnowledge> getAll() {
        return knowledgeMapper.selectList(
            new LambdaQueryWrapper<AiKnowledge>()
                .eq(AiKnowledge::getStatus, "active")
                .eq(AiKnowledge::getDeleted, 0)
                .orderByDesc(AiKnowledge::getUpdateTime));
    }
}
