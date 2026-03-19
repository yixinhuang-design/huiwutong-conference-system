package com.conference.seating.service;

import java.util.List;

/**
 * 参会人员管理服务接口
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingAttendeeService {
    
    /**
     * 获取会议参会人员列表
     */
    List<?> getAttendeesByConference(Long conferenceId);
    
    /**
     * 获取参会人员详情
     */
    Object getAttendeeDetail(Long attendeeId);
    
    /**
     * 创建参会人员
     */
    Object createAttendee(Object request);
}
