package com.conference.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.registration.entity.RegistrationAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审核日志 Mapper 接口
 */
@Mapper
public interface RegistrationAuditLogMapper extends BaseMapper<RegistrationAuditLog> {
    
    /**
     * 获取报名的审核历史
     */
    List<RegistrationAuditLog> getByRegistrationId(@Param("registrationId") Long registrationId);
    
    /**
     * 获取审核人的审核记录
     */
    List<RegistrationAuditLog> getByAuditorId(@Param("auditorId") Long auditorId);
}
