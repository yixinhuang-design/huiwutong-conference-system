package com.conference.seating.service;

import com.conference.seating.dto.AssignedAttendeeResponse;
import com.conference.seating.dto.TransportCreateRequest;
import com.conference.seating.dto.TransportDetailResponse;
import com.conference.seating.dto.TransportUpdateRequest;

import java.util.List;

public interface SeatingTransportService {
    
    List<TransportDetailResponse> getTransportsByConference(Long conferenceId);
    
    TransportDetailResponse getTransportDetail(Long transportId);
    
    TransportDetailResponse createTransport(TransportCreateRequest request);
    
    TransportDetailResponse updateTransport(Long transportId, TransportUpdateRequest request);
    
    void deleteTransport(Long transportId);
    
    void assignAttendeeToTransport(Long transportId, Long attendeeId, String attendeeName, String department);
    
    void unassignAttendeeFromTransport(Long transportId, Long attendeeId);
    
    List<TransportDetailResponse> getAvailableTransports(Long conferenceId);

    List<AssignedAttendeeResponse> getAssignedAttendees(Long transportId);
}
