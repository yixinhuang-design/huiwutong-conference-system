package com.conference.seating.service;

import com.conference.seating.dto.AssignedAttendeeResponse;
import com.conference.seating.dto.DiningCreateRequest;
import com.conference.seating.dto.DiningDetailResponse;
import com.conference.seating.dto.DiningUpdateRequest;

import java.util.List;

public interface SeatingDiningService {
    
    List<DiningDetailResponse> getDiningsByConference(Long conferenceId);
    
    DiningDetailResponse getDiningDetail(Long diningId);
    
    DiningDetailResponse createDining(DiningCreateRequest request);
    
    DiningDetailResponse updateDining(Long diningId, DiningUpdateRequest request);
    
    void deleteDining(Long diningId);
    
    void assignAttendeeToDining(Long diningId, Long attendeeId, String attendeeName, String department);
    
    void unassignAttendeeFromDining(Long diningId, Long attendeeId);
    
    List<DiningDetailResponse> getAvailableDinings(Long conferenceId);

    List<AssignedAttendeeResponse> getAssignedAttendees(Long diningId);
}
