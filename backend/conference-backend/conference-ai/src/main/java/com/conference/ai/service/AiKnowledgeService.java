package com.conference.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.ai.entity.AiKnowledge;

import java.util.List;
import java.util.Map;

/**
 * AI知识库服务接口
 */
public interface AiKnowledgeService extends IService<AiKnowledge> {

    List<AiKnowledge> listByConference(Long conferenceId);

    AiKnowledge addKnowledge(AiKnowledge knowledge);

    AiKnowledge updateKnowledge(AiKnowledge knowledge);

    void deleteKnowledge(Long id);

    /**
     * 根据问题关键词搜索知识库
     */
    List<AiKnowledge> searchKnowledge(String query, Long conferenceId);

    Map<String, Object> getKnowledgeStats(Long conferenceId);
}
