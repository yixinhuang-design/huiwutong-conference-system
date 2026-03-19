package com.conference.seating.service;

import com.conference.seating.dto.AccommodationCreateRequest;
import com.conference.seating.dto.AccommodationDetailResponse;
import com.conference.seating.dto.AccommodationUpdateRequest;

import java.util.List;

/**
 * 住宿安排服务接口
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingAccommodationService {
    
    List<AccommodationDetailResponse> getAccommodationsByConference(Long conferenceId);
    
    AccommodationDetailResponse getAccommodationDetail(Long accommodationId);
    
    AccommodationDetailResponse createAccommodation(AccommodationCreateRequest request);
    
    AccommodationDetailResponse updateAccommodation(Long accommodationId, AccommodationUpdateRequest request);
    
    void deleteAccommodation(Long accommodationId);
    
    void assignAttendeeToAccommodation(Long accommodationId, Long attendeeId);
    
    void unassignAttendeeFromAccommodation(Long accommodationId, Long attendeeId);
    
    List<AccommodationDetailResponse> getAvailableAccommodations(Long conferenceId);
}
