package com.conference.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.ai.entity.AiFeedback;

import java.util.List;
import java.util.Map;

/**
 * AI反馈服务接口
 */
public interface AiFeedbackService extends IService<AiFeedback> {

    AiFeedback addFeedback(AiFeedback feedback);

    List<AiFeedback> listFeedback(Long conferenceId, int page, int size);

    Map<String, Object> getFeedbackStats(Long conferenceId);
}
