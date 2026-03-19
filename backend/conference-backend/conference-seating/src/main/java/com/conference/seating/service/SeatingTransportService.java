package com.conference.seating.service;

import com.conference.seating.dto.TransportCreateRequest;
import com.conference.seating.dto.TransportDetailResponse;
import com.conference.seating.dto.TransportUpdateRequest;

import java.util.List;

/**
 * 车辆运输服务接口
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingTransportService {
    
    List<TransportDetailResponse> getTransportsByConference(Long conferenceId);
    
    TransportDetailResponse getTransportDetail(Long transportId);
    
    TransportDetailResponse createTransport(TransportCreateRequest request);
    
    TransportDetailResponse updateTransport(Long transportId, TransportUpdateRequest request);
    
    void deleteTransport(Long transportId);
    
    void assignAttendeeToTransport(Long transportId, Long attendeeId);
    
    void unassignAttendeeFromTransport(Long transportId, Long attendeeId);
    
    List<TransportDetailResponse> getAvailableTransports(Long conferenceId);
}
