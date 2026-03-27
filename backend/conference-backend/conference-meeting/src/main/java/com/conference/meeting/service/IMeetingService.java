package com.conference.meeting.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.meeting.entity.Meeting;

import java.util.List;

public interface IMeetingService extends IService<Meeting> {

    Meeting createMeeting(Meeting meeting);

    Meeting getMeetingDetail(Long id);

    IPage<Meeting> getMeetingList(Page<Meeting> page, Long tenantId, String keyword, Integer status);

    Boolean updateMeeting(Meeting meeting);

    Boolean deleteMeeting(Long id);

    Boolean updateMeetingStatus(Long id, Integer status);

    MeetingStatistics getMeetingStatistics(Long tenantId);

    List<Meeting> getOngoingMeetings(Long tenantId);

    Boolean validateMeetingCodeUnique(Long tenantId, String code, Long excludeId);

    class MeetingStatistics {
        public Integer totalMeetings;
        public Integer draftMeetings;
        public Integer registrationOpenMeetings;
        public Integer ongoingMeetings;
        public Integer completedMeetings;
        
        public MeetingStatistics(Integer total, Integer draft, Integer regOpen, Integer ongoing, Integer completed) {
            this.totalMeetings = total;
            this.draftMeetings = draft;
            this.registrationOpenMeetings = regOpen;
            this.ongoingMeetings = ongoing;
            this.completedMeetings = completed;
        }
    }
}

