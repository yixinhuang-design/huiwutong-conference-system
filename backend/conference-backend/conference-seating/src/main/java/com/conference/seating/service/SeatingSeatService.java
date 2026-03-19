package com.conference.seating.service;

import com.conference.seating.dto.*;

import java.util.List;

/**
 * 座位管理服务接口
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingSeatService {
    
    /**
     * 获取会场座位列表
     */
    List<SeatListResponse> getSeatsByVenue(Long venueId);
    
    /**
     * 分配座位
     */
    void assignSeat(SeatAssignRequest request);
    
    /**
     * 交换座位
     */
    void swapSeats(SeatSwapRequest request);
    
    /**
     * 获取座位统计
     */
    VenueSeatStatsDto getSeatStats(Long venueId);
    
    /**
     * 整体保存座位布局（前端一次性提交全部座位区+座位+分配）
     */
    void saveLayout(LayoutSaveRequest request);
    
    /**
     * 整体加载座位布局
     */
    LayoutLoadResponse loadLayout(Long conferenceId, Long scheduleId);
}
