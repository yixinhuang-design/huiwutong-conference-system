package com.conference.seating.controller;

import com.conference.seating.dto.*;
import com.conference.seating.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排座管理 Controller
 * @author AI Assistant
 * @date 2026-03-12
 */
@Slf4j
@RestController
@RequestMapping("/api/seating")
@Tag(name = "排座管理", description = "排座相关接口")
public class SeatingController {
    
    @Resource
    private SeatingVenueService seatingVenueService;
    
    @Resource
    private SeatingDiningService seatingDiningService;
    
    @Resource
    private SeatingAccommodationService seatingAccommodationService;
    
    @Resource
    private SeatingTransportService seatingTransportService;
    
    @Resource
    private SeatingSeatService seatingSeatService;
    
    // ==================== 会场 ====================
    
    @GetMapping("/venues/{conferenceId}")
    @Operation(summary = "获取会场列表")
    public List<VenueDetailResponse> getVenues(@PathVariable Long conferenceId) {
        return seatingVenueService.getVenuesByConference(conferenceId);
    }
    
    @GetMapping("/venues/detail/{venueId}")
    @Operation(summary = "获取会场详情")
    public VenueDetailResponse getVenueDetail(@PathVariable Long venueId) {
        return seatingVenueService.getVenueDetail(venueId);
    }
    
    @PostMapping("/venues")
    @Operation(summary = "创建会场")
    public VenueDetailResponse createVenue(@Valid @RequestBody VenueCreateRequest request) {
        return seatingVenueService.createVenue(request);
    }
    
    @PutMapping("/venues/{venueId}")
    @Operation(summary = "更新会场")
    public VenueDetailResponse updateVenue(@PathVariable Long venueId, @Valid @RequestBody VenueUpdateRequest request) {
        return seatingVenueService.updateVenue(venueId, request);
    }
    
    @DeleteMapping("/venues/{venueId}")
    @Operation(summary = "删除会场")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long venueId) {
        seatingVenueService.deleteVenue(venueId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/venues/stats/{venueId}")
    @Operation(summary = "获取会场座位统计（venue维度）")
    public VenueSeatStatsDto getVenueStats(@PathVariable Long venueId) {
        return seatingVenueService.getSeatStats(venueId);
    }
    
    // ==================== 座位 ====================
    
    @GetMapping("/seats")
    @Operation(summary = "获取座位列表")
    public List<SeatListResponse> getSeats(@RequestParam Long venueId) {
        return seatingSeatService.getSeatsByVenue(venueId);
    }
    
    @PostMapping("/seats/assign")
    @Operation(summary = "分配座位")
    public ResponseEntity<Map<String, Object>> assignSeat(@Valid @RequestBody SeatAssignRequest request) {
        seatingSeatService.assignSeat(request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "座位分配成功");
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/seats/swap")
    @Operation(summary = "交换座位")
    public ResponseEntity<Map<String, Object>> swapSeats(@Valid @RequestBody SeatSwapRequest request) {
        seatingSeatService.swapSeats(request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "座位交换成功");
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/seats/stats/{venueId}")
    @Operation(summary = "获取座位统计")
    public VenueSeatStatsDto getSeatStats(@PathVariable Long venueId) {
        return seatingSeatService.getSeatStats(venueId);
    }
    
    // ==================== 整体布局保存/加载 ====================
    
    @PostMapping("/layout/save")
    @Operation(summary = "保存座位布局（全量保存所有座位区+座位+分配）")
    public ResponseEntity<Map<String, Object>> saveLayout(@Valid @RequestBody LayoutSaveRequest request) {
        seatingSeatService.saveLayout(request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "座位布局保存成功");
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/layout/load")
    @Operation(summary = "加载座位布局")
    public LayoutLoadResponse loadLayout(@RequestParam Long conferenceId,
                                          @RequestParam(required = false) Long scheduleId) {
        return seatingSeatService.loadLayout(conferenceId, scheduleId);
    }
    
    // ==================== 用餐 ====================
    
    @GetMapping("/dinings/{conferenceId}")
    @Operation(summary = "获取用餐列表")
    public List<DiningDetailResponse> getDinings(@PathVariable Long conferenceId) {
        return seatingDiningService.getDiningsByConference(conferenceId);
    }
    
    @PostMapping("/dinings")
    @Operation(summary = "创建用餐")
    public DiningDetailResponse createDining(@Valid @RequestBody DiningCreateRequest request) {
        return seatingDiningService.createDining(request);
    }
    
    // ==================== 住宿 ====================
    
    @GetMapping("/accommodations/{conferenceId}")
    @Operation(summary = "获取住宿列表")
    public List<AccommodationDetailResponse> getAccommodations(@PathVariable Long conferenceId) {
        return seatingAccommodationService.getAccommodationsByConference(conferenceId);
    }
    
    @PostMapping("/accommodations")
    @Operation(summary = "创建住宿")
    public AccommodationDetailResponse createAccommodation(@Valid @RequestBody AccommodationCreateRequest request) {
        return seatingAccommodationService.createAccommodation(request);
    }
    
    // ==================== 车辆 ====================
    
    @GetMapping("/transports/{conferenceId}")
    @Operation(summary = "获取车辆列表")
    public List<TransportDetailResponse> getTransports(@PathVariable Long conferenceId) {
        return seatingTransportService.getTransportsByConference(conferenceId);
    }
    
    @PostMapping("/transports")
    @Operation(summary = "创建车辆")
    public TransportDetailResponse createTransport(@Valid @RequestBody TransportCreateRequest request) {
        return seatingTransportService.createTransport(request);
    }
}
