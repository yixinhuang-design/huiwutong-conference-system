package com.conference.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.ai.entity.AiFaq;

import java.util.List;
import java.util.Map;

/**
 * AI FAQ服务接口
 */
public interface AiFaqService extends IService<AiFaq> {

    List<AiFaq> listByConference(Long conferenceId);

    AiFaq addFaq(AiFaq faq);

    AiFaq updateFaq(AiFaq faq);

    void deleteFaq(Long id);

    /**
     * 根据问题匹配FAQ
     */
    AiFaq matchFaq(String question, Long conferenceId);

    Map<String, Object> getFaqStats(Long conferenceId);
}
