package com.conference.seating.service;

import com.conference.seating.dto.DiningCreateRequest;
import com.conference.seating.dto.DiningDetailResponse;
import com.conference.seating.dto.DiningUpdateRequest;

import java.util.List;

/**
 * 用餐安排服务接口
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingDiningService {
    
    List<DiningDetailResponse> getDiningsByConference(Long conferenceId);
    
    DiningDetailResponse getDiningDetail(Long diningId);
    
    DiningDetailResponse createDining(DiningCreateRequest request);
    
    DiningDetailResponse updateDining(Long diningId, DiningUpdateRequest request);
    
    void deleteDining(Long diningId);
    
    void assignAttendeeToDining(Long diningId, Long attendeeId);
    
    void unassignAttendeeFromDining(Long diningId, Long attendeeId);
    
    List<DiningDetailResponse> getAvailableDinings(Long conferenceId);
}
