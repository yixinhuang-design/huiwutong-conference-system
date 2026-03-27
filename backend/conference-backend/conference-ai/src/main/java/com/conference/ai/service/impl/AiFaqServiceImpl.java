package com.conference.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.ai.entity.AiFaq;
import com.conference.ai.mapper.AiFaqMapper;
import com.conference.ai.service.AiFaqService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI FAQ服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFaqServiceImpl extends ServiceImpl<AiFaqMapper, AiFaq>
        implements AiFaqService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;
    private final AiFaqMapper faqMapper;

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    public List<AiFaq> listByConference(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiFaq> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiFaq::getTenantId, tenantId)
                .eq(AiFaq::getDeleted, 0)
                .eq(AiFaq::getStatus, "active");
        if (conferenceId != null) wrapper.eq(AiFaq::getConferenceId, conferenceId);
        wrapper.orderByAsc(AiFaq::getSortOrder).orderByDesc(AiFaq::getViews);
        return faqMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public AiFaq addFaq(AiFaq faq) {
        Long tenantId = getTenantId();
        faq.setTenantId(tenantId);
        faq.setViews(0);
        faq.setRatingCount(0);
        faq.setStatus("active");
        faq.setDeleted(0);
        faq.setCreateTime(LocalDateTime.now());
        faq.setUpdateTime(LocalDateTime.now());
        faqMapper.insert(faq);
        log.info("[租户{}] 新增FAQ: {}", tenantId, faq.getQuestion());
        return faq;
    }

    @Override
    @Transactional
    public AiFaq updateFaq(AiFaq faq) {
        faq.setUpdateTime(LocalDateTime.now());
        faqMapper.updateById(faq);
        log.info("FAQ{}已更新", faq.getId());
        return faq;
    }

    @Override
    @Transactional
    public void deleteFaq(Long id) {
        faqMapper.deleteById(id);
        log.info("FAQ{}已删除", id);
    }

    @Override
    public AiFaq matchFaq(String question, Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiFaq> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiFaq::getTenantId, tenantId)
                .eq(AiFaq::getDeleted, 0)
                .eq(AiFaq::getStatus, "active");
        if (conferenceId != null) wrapper.eq(AiFaq::getConferenceId, conferenceId);
        List<AiFaq> faqList = faqMapper.selectList(wrapper);

        if (faqList.isEmpty() || question == null) return null;

        String q = question.toLowerCase();

        // 1. 精确匹配
        for (AiFaq faq : faqList) {
            if (faq.getQuestion() != null && faq.getQuestion().toLowerCase().equals(q)) {
                incrementViews(faq);
                return faq;
            }
        }

        // 2. 关键词匹配
        AiFaq bestMatch = null;
        int bestScore = 0;
        for (AiFaq faq : faqList) {
            int score = 0;
            // 检查问题相似度
            if (faq.getQuestion() != null && faq.getQuestion().toLowerCase().contains(q)) score += 10;
            if (faq.getQuestion() != null && q.contains(faq.getQuestion().toLowerCase())) score += 8;

            // 检查关键词匹配
            if (faq.getKeywords() != null) {
                String[] keywords = faq.getKeywords().split("[,，\\s]+");
                for (String kw : keywords) {
                    if (q.contains(kw.trim().toLowerCase())) score += 3;
                }
            }

            // 检查分类匹配
            if (faq.getCategory() != null && q.contains(faq.getCategory().toLowerCase())) score += 2;

            if (score > bestScore) {
                bestScore = score;
                bestMatch = faq;
            }
        }

        // 匹配分数阈值
        if (bestMatch != null && bestScore >= 3) {
            incrementViews(bestMatch);
            return bestMatch;
        }

        return null;
    }

    private void incrementViews(AiFaq faq) {
        try {
            faq.setViews(faq.getViews() + 1);
            faqMapper.updateById(faq);
        } catch (Exception e) {
            log.warn("更新FAQ查看次数失败", e);
        }
    }

    @Override
    public Map<String, Object> getFaqStats(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiFaq> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiFaq::getTenantId, tenantId)
                .eq(AiFaq::getDeleted, 0);
        if (conferenceId != null) wrapper.eq(AiFaq::getConferenceId, conferenceId);
        List<AiFaq> list = faqMapper.selectList(wrapper);

        long total = list.size();
        long resolved = list.stream().filter(f -> f.getRatingCount() > 0).count();
        double avgRating = list.stream()
                .filter(f -> f.getRating() != null && f.getRating().doubleValue() > 0)
                .mapToDouble(f -> f.getRating().doubleValue())
                .average().orElse(0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("resolved", resolved);
        stats.put("avgRating", Math.round(avgRating * 10) / 10.0);
        return stats;
    }
}
