package com.conference.seating.service;

import com.conference.seating.dto.AccommodationCreateRequest;
import com.conference.seating.dto.AccommodationDetailResponse;
import com.conference.seating.dto.AccommodationUpdateRequest;
import com.conference.seating.dto.AssignedAttendeeResponse;

import java.util.List;

public interface SeatingAccommodationService {
    
    List<AccommodationDetailResponse> getAccommodationsByConference(Long conferenceId);
    
    AccommodationDetailResponse getAccommodationDetail(Long accommodationId);
    
    AccommodationDetailResponse createAccommodation(AccommodationCreateRequest request);
    
    AccommodationDetailResponse updateAccommodation(Long accommodationId, AccommodationUpdateRequest request);
    
    void deleteAccommodation(Long accommodationId);
    
    void assignAttendeeToAccommodation(Long accommodationId, Long attendeeId, String attendeeName, String department);
    
    void unassignAttendeeFromAccommodation(Long accommodationId, Long attendeeId);
    
    List<AccommodationDetailResponse> getAvailableAccommodations(Long conferenceId);

    List<AssignedAttendeeResponse> getAssignedAttendees(Long accommodationId);
}
