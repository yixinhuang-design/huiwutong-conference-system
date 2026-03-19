package com.conference.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.registration.entity.RegistrationAudit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报名审核 Mapper
 */
@Mapper
public interface RegistrationAuditMapper extends BaseMapper<RegistrationAudit> {
}
