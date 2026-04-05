package com.conference.registration.service;

import com.conference.registration.entity.RegistrationAuditLog;

import java.util.List;

/**
 * 审核日志服务接口
 */
public interface RegistrationAuditLogService {
    
    /**
     * 记录审核日志
     */
    void logAudit(Long registrationId, Long auditorId, String auditorName, String action, String comment);
    
    /**
     * 获取报名的审核历史
     */
    List<RegistrationAuditLog> getAuditHistory(Long registrationId);
    
    /**
     * 获取审核人的审核记录
     */
    List<RegistrationAuditLog> getAuditorRecords(Long auditorId);
}
