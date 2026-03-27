package com.conference.seating.service;

import com.conference.seating.entity.SeatingAttendee;

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
    List<SeatingAttendee> getAttendeesByConference(Long conferenceId);
    
    /**
     * 获取参会人员详情
     */
    SeatingAttendee getAttendeeDetail(Long attendeeId);
    
    /**
     * 创建参会人员
     */
    SeatingAttendee createAttendee(SeatingAttendee attendee);

    /**
     * 分配座位给参会人员
     */
    void assignSeatToAttendee(Long attendeeId, Long seatId);

    /**
     * 从报名服务同步参会人员数据
     * @param conferenceId 会议ID
     * @return 同步的人数
     */
    int syncFromRegistration(Long conferenceId);
}
