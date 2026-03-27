package com.conference.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.ai.entity.AiFeedback;
import com.conference.ai.mapper.AiFeedbackMapper;
import com.conference.ai.service.AiFeedbackService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI反馈服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackServiceImpl extends ServiceImpl<AiFeedbackMapper, AiFeedback>
        implements AiFeedbackService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;
    private final AiFeedbackMapper feedbackMapper;

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    @Transactional
    public AiFeedback addFeedback(AiFeedback feedback) {
        Long tenantId = getTenantId();
        feedback.setTenantId(tenantId);
        feedback.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        log.info("[租户{}] 新增反馈: {}", tenantId, feedback.getFeedback());
        return feedback;
    }

    @Override
    public List<AiFeedback> listFeedback(Long conferenceId, int page, int size) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiFeedback::getTenantId, tenantId)
                .orderByDesc(AiFeedback::getCreateTime);
        // 简单分页
        wrapper.last("LIMIT " + size + " OFFSET " + ((page - 1) * size));
        return feedbackMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getFeedbackStats(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiFeedback::getTenantId, tenantId);
        List<AiFeedback> list = feedbackMapper.selectList(wrapper);

        long positive = list.stream().filter(f -> "good".equals(f.getFeedback())).count();
        long negative = list.stream().filter(f -> "bad".equals(f.getFeedback())).count();
        long neutral = list.stream().filter(f -> "neutral".equals(f.getFeedback())).count();
        long total = positive + negative + neutral;
        int rate = total > 0 ? (int) (positive * 100 / total) : 95;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("positive", positive);
        stats.put("negative", negative);
        stats.put("neutral", neutral);
        stats.put("rate", rate);
        return stats;
    }
}
