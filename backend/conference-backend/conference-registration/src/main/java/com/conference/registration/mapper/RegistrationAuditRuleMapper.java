package com.conference.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.registration.entity.RegistrationAuditRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审核规则 Mapper 接口
 */
@Mapper
public interface RegistrationAuditRuleMapper extends BaseMapper<RegistrationAuditRule> {
    
    /**
     * 获取会议的所有审核规则
     */
    List<RegistrationAuditRule> getByConferenceId(@Param("conferenceId") Long conferenceId);
    
    /**
     * 获取启用的审核规则
     */
    List<RegistrationAuditRule> getEnabledRules(@Param("conferenceId") Long conferenceId);
}
