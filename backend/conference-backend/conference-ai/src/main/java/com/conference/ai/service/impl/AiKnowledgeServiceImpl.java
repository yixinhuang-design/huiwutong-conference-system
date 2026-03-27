package com.conference.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.ai.entity.AiKnowledge;
import com.conference.ai.mapper.AiKnowledgeMapper;
import com.conference.ai.service.AiKnowledgeService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI知识库服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl extends ServiceImpl<AiKnowledgeMapper, AiKnowledge>
        implements AiKnowledgeService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;
    private final AiKnowledgeMapper knowledgeMapper;

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    public List<AiKnowledge> listByConference(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledge::getTenantId, tenantId)
                .eq(AiKnowledge::getDeleted, 0)
                .eq(AiKnowledge::getStatus, "active");
        if (conferenceId != null) wrapper.eq(AiKnowledge::getConferenceId, conferenceId);
        wrapper.orderByAsc(AiKnowledge::getSortOrder).orderByDesc(AiKnowledge::getCreateTime);
        return knowledgeMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public AiKnowledge addKnowledge(AiKnowledge knowledge) {
        Long tenantId = getTenantId();
        knowledge.setTenantId(tenantId);
        knowledge.setViews(0);
        knowledge.setStatus("active");
        knowledge.setDeleted(0);
        knowledge.setCreateTime(LocalDateTime.now());
        knowledge.setUpdateTime(LocalDateTime.now());
        knowledgeMapper.insert(knowledge);
        log.info("[租户{}] 新增知识: {}", tenantId, knowledge.getTitle());
        return knowledge;
    }

    @Override
    @Transactional
    public AiKnowledge updateKnowledge(AiKnowledge knowledge) {
        knowledge.setUpdateTime(LocalDateTime.now());
        knowledgeMapper.updateById(knowledge);
        log.info("知识{}已更新", knowledge.getId());
        return knowledge;
    }

    @Override
    @Transactional
    public void deleteKnowledge(Long id) {
        knowledgeMapper.deleteById(id);
        log.info("知识{}已删除", id);
    }

    @Override
    public List<AiKnowledge> searchKnowledge(String query, Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledge::getTenantId, tenantId)
                .eq(AiKnowledge::getDeleted, 0)
                .eq(AiKnowledge::getStatus, "active");
        if (conferenceId != null) wrapper.eq(AiKnowledge::getConferenceId, conferenceId);
        List<AiKnowledge> allKnowledge = knowledgeMapper.selectList(wrapper);

        if (allKnowledge.isEmpty() || query == null) return Collections.emptyList();

        // 简单关键词匹配排序
        String q = query.toLowerCase();
        return allKnowledge.stream()
                .filter(k -> {
                    String text = ((k.getTitle() != null ? k.getTitle() : "") + " " +
                            (k.getSummary() != null ? k.getSummary() : "") + " " +
                            (k.getContent() != null ? k.getContent() : "") + " " +
                            (k.getTags() != null ? k.getTags() : "") + " " +
                            (k.getCategory() != null ? k.getCategory() : "")).toLowerCase();
                    return text.contains(q) || Arrays.stream(q.split("\\s+"))
                            .anyMatch(text::contains);
                })
                .sorted(Comparator.comparingInt(k -> {
                    String title = k.getTitle() != null ? k.getTitle().toLowerCase() : "";
                    return title.contains(q) ? 0 : 1;
                }))
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getKnowledgeStats(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledge::getTenantId, tenantId)
                .eq(AiKnowledge::getDeleted, 0);
        if (conferenceId != null) wrapper.eq(AiKnowledge::getConferenceId, conferenceId);
        List<AiKnowledge> list = knowledgeMapper.selectList(wrapper);

        long total = list.size();
        long categories = list.stream()
                .map(AiKnowledge::getCategory)
                .filter(Objects::nonNull)
                .distinct().count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("categories", categories);
        stats.put("updated", total > 0 ? "刚刚" : "暂无");
        return stats;
    }
}
