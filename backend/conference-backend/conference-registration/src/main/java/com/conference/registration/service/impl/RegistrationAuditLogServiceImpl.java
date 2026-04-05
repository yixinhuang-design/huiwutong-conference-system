package com.conference.registration.service.impl;

import com.conference.registration.entity.RegistrationAuditLog;
import com.conference.registration.mapper.RegistrationAuditLogMapper;
import com.conference.registration.service.RegistrationAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationAuditLogServiceImpl implements RegistrationAuditLogService {
    
    private final RegistrationAuditLogMapper auditLogMapper;
    
    @Override
    public void logAudit(Long registrationId, Long auditorId, String auditorName, String action, String comment) {
        RegistrationAuditLog auditLog = new RegistrationAuditLog();
        auditLog.setRegistrationId(registrationId);
        auditLog.setAuditorId(auditorId);
        auditLog.setAuditorName(auditorName);
        auditLog.setAction(action);
        auditLog.setComment(comment);
        auditLog.setCreateTime(LocalDateTime.now());
        
        auditLogMapper.insert(auditLog);
        
        log.debug("记录审核日志: registrationId={}, action={}, auditor={}", registrationId, action, auditorName);
    }
    
    @Override
    public List<RegistrationAuditLog> getAuditHistory(Long registrationId) {
        return auditLogMapper.getByRegistrationId(registrationId);
    }
    
    @Override
    public List<RegistrationAuditLog> getAuditorRecords(Long auditorId) {
        return auditLogMapper.getByAuditorId(auditorId);
    }
}
