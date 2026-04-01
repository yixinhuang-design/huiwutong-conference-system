package com.conference.seating.service;

import com.conference.seating.dto.*;

import java.util.List;

/**
 * 讨论室安排服务接口
 */
public interface SeatingDiscussionService {

    List<DiscussionDetailResponse> getDiscussionsByConference(Long conferenceId);

    DiscussionDetailResponse getDiscussionDetail(Long discussionId);

    DiscussionDetailResponse createDiscussion(DiscussionCreateRequest request);

    DiscussionDetailResponse updateDiscussion(Long discussionId, DiscussionUpdateRequest request);

    void deleteDiscussion(Long discussionId);

    void assignAttendeeToDiscussion(Long discussionId, Long attendeeId, String attendeeName, String department);

    void unassignAttendeeFromDiscussion(Long discussionId, Long attendeeId);

    List<DiscussionDetailResponse> getAvailableDiscussions(Long conferenceId);

    List<AssignedAttendeeResponse> getAssignedAttendees(Long discussionId);
}
