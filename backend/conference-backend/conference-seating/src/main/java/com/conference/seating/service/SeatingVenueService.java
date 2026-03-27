package com.conference.seating.service;

import com.conference.seating.dto.VenueCreateRequest;
import com.conference.seating.dto.VenueDetailResponse;
import com.conference.seating.dto.VenueUpdateRequest;
import com.conference.seating.dto.VenueSeatStatsDto;

import java.util.List;

/**
 * 会场管理服务接口
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingVenueService {
    
    List<VenueDetailResponse> getVenuesByConference(Long conferenceId);
    
    VenueDetailResponse getVenueDetail(Long venueId);
    
    VenueDetailResponse createVenue(VenueCreateRequest request);
    
    VenueDetailResponse updateVenue(Long venueId, VenueUpdateRequest request);
    
    void deleteVenue(Long venueId);
    
    boolean venueExists(Long venueId);
    
    VenueSeatStatsDto getSeatStats(Long venueId);
}
