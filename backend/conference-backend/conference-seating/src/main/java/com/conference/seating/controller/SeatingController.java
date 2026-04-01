package com.conference.seating.controller;

import com.conference.common.result.Result;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.*;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.service.*;
import com.conference.seating.service.algorithm.AlgorithmFactory;
import com.conference.seating.service.algorithm.SeatingAlgorithm;
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

    @Resource
    private SeatingAttendeeService seatingAttendeeService;

    @Resource
    private SeatingDiscussionService seatingDiscussionService;
    
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

    // ==================== 智能排座算法 ====================

    @PostMapping("/auto-assign")
    @Operation(summary = "智能自动排座")
    public Result<Map<String, Object>> autoAssign(@RequestBody Map<String, Object> params) {
        Long conferenceId = Long.parseLong(String.valueOf(params.get("conferenceId")));
        String algorithm = params.get("algorithm") != null ? String.valueOf(params.get("algorithm")) : "priority";

        log.info("执行智能排座: conferenceId={}, algorithm={}", conferenceId, algorithm);

        // 获取参会人员列表
        List<SeatingAttendee> attendees = seatingAttendeeService.getAttendeesByConference(conferenceId);
        if (attendees.isEmpty()) {
            return Result.success(Map.of("assigned", 0, "message", "暂无参会人员数据，请先同步报名数据"));
        }

        // 获取空闲座位
        List<com.conference.seating.entity.SeatingSeat> seats = seatingSeatService.getAvailableSeatsByConference(conferenceId);
        if (seats.isEmpty()) {
            return Result.success(Map.of("assigned", 0, "message", "暂无可用座位，请先配置座位布局"));
        }

        // 执行算法
        SeatingAlgorithm algo = AlgorithmFactory.createAlgorithm(algorithm);
        Map<Long, Long> assignments = algo.execute(attendees, seats);

        // 保存分配结果
        int assignedCount = 0;
        for (Map.Entry<Long, Long> entry : assignments.entrySet()) {
            try {
                seatingAttendeeService.assignSeatToAttendee(entry.getKey(), entry.getValue());
                assignedCount++;
            } catch (Exception e) {
                log.warn("分配座位失败: attendeeId={}, seatId={}, error={}", entry.getKey(), entry.getValue(), e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("assigned", assignedCount);
        result.put("total", attendees.size());
        result.put("algorithm", algo.getAlgorithmName());
        result.put("message", String.format("成功分配 %d/%d 人", assignedCount, attendees.size()));
        return Result.success(result);
    }

    @GetMapping("/algorithms")
    @Operation(summary = "获取可用排座算法列表")
    public Result<List<Map<String, String>>> getAlgorithms() {
        return Result.success(List.of(
            Map.of("type", "priority", "name", "优先级排座", "description", "按照VIP优先、领导优先等优先级分配座位"),
            Map.of("type", "vip_optimization", "name", "VIP优化排座", "description", "优化VIP人员的座位体验，前排居中分配"),
            Map.of("type", "department_grouping", "name", "部门分组排座", "description", "同部门人员集中就座，方便交流")
        ));
    }

    // ==================== 参会人员 ====================

    @GetMapping("/attendees/{conferenceId}")
    @Operation(summary = "获取参会人员列表")
    public Result<List<SeatingAttendee>> getAttendees(@PathVariable Long conferenceId) {
        return Result.success(seatingAttendeeService.getAttendeesByConference(conferenceId));
    }

    @PostMapping("/attendees/sync")
    @Operation(summary = "从报名服务同步参会人员数据")
    public Result<Map<String, Object>> syncAttendees(@RequestBody Map<String, Object> params) {
        Long conferenceId = Long.parseLong(String.valueOf(params.get("conferenceId")));
        int synced = seatingAttendeeService.syncFromRegistration(conferenceId);
        return Result.success(Map.of("synced", synced, "message", String.format("成功同步 %d 人", synced)));
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

    @PutMapping("/dinings/{diningId}")
    @Operation(summary = "更新用餐")
    public DiningDetailResponse updateDining(@PathVariable Long diningId, @Valid @RequestBody DiningUpdateRequest request) {
        return seatingDiningService.updateDining(diningId, request);
    }

    @DeleteMapping("/dinings/{diningId}")
    @Operation(summary = "删除用餐")
    public ResponseEntity<Void> deleteDining(@PathVariable Long diningId) {
        seatingDiningService.deleteDining(diningId);
        return ResponseEntity.ok().build();
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

    @PutMapping("/accommodations/{accommodationId}")
    @Operation(summary = "更新住宿")
    public AccommodationDetailResponse updateAccommodation(@PathVariable Long accommodationId, @Valid @RequestBody AccommodationUpdateRequest request) {
        return seatingAccommodationService.updateAccommodation(accommodationId, request);
    }

    @DeleteMapping("/accommodations/{accommodationId}")
    @Operation(summary = "删除住宿")
    public ResponseEntity<Void> deleteAccommodation(@PathVariable Long accommodationId) {
        seatingAccommodationService.deleteAccommodation(accommodationId);
        return ResponseEntity.ok().build();
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

    @PutMapping("/transports/{transportId}")
    @Operation(summary = "更新车辆")
    public TransportDetailResponse updateTransport(@PathVariable Long transportId, @Valid @RequestBody TransportUpdateRequest request) {
        return seatingTransportService.updateTransport(transportId, request);
    }

    @DeleteMapping("/transports/{transportId}")
    @Operation(summary = "删除车辆")
    public ResponseEntity<Void> deleteTransport(@PathVariable Long transportId) {
        seatingTransportService.deleteTransport(transportId);
        return ResponseEntity.ok().build();
    }

    // ==================== 讨论室 ====================

    @GetMapping("/discussions/{conferenceId}")
    @Operation(summary = "获取讨论室列表")
    public List<DiscussionDetailResponse> getDiscussions(@PathVariable Long conferenceId) {
        return seatingDiscussionService.getDiscussionsByConference(conferenceId);
    }

    @PostMapping("/discussions")
    @Operation(summary = "创建讨论室")
    public DiscussionDetailResponse createDiscussion(@Valid @RequestBody DiscussionCreateRequest request) {
        return seatingDiscussionService.createDiscussion(request);
    }

    @PutMapping("/discussions/{discussionId}")
    @Operation(summary = "更新讨论室")
    public DiscussionDetailResponse updateDiscussion(@PathVariable Long discussionId, @Valid @RequestBody DiscussionUpdateRequest request) {
        return seatingDiscussionService.updateDiscussion(discussionId, request);
    }

    @DeleteMapping("/discussions/{discussionId}")
    @Operation(summary = "删除讨论室")
    public ResponseEntity<Void> deleteDiscussion(@PathVariable Long discussionId) {
        seatingDiscussionService.deleteDiscussion(discussionId);
        return ResponseEntity.ok().build();
    }

    // ==================== 人员分配（乘车） ====================

    @GetMapping("/transports/{transportId}/assignees")
    @Operation(summary = "获取车辆已分配人员列表")
    public List<AssignedAttendeeResponse> getTransportAssignees(@PathVariable Long transportId) {
        return seatingTransportService.getAssignedAttendees(transportId);
    }

    @PostMapping("/transports/{transportId}/assign")
    @Operation(summary = "分配人员到车辆")
    public ResponseEntity<Map<String, Object>> assignToTransport(@PathVariable Long transportId, @Valid @RequestBody AssignRequest request) {
        seatingTransportService.assignAttendeeToTransport(transportId, request.getAttendeeId(), request.getAttendeeName(), request.getDepartment());
        return ResponseEntity.ok(Map.of("success", true, "message", "分配成功"));
    }

    @DeleteMapping("/transports/{transportId}/assign/{attendeeId}")
    @Operation(summary = "取消车辆人员分配")
    public ResponseEntity<Map<String, Object>> unassignFromTransport(@PathVariable Long transportId, @PathVariable Long attendeeId) {
        seatingTransportService.unassignAttendeeFromTransport(transportId, attendeeId);
        return ResponseEntity.ok(Map.of("success", true, "message", "取消分配成功"));
    }

    // ==================== 人员分配（住宿） ====================

    @GetMapping("/accommodations/{accommodationId}/assignees")
    @Operation(summary = "获取住宿已分配人员列表")
    public List<AssignedAttendeeResponse> getAccommodationAssignees(@PathVariable Long accommodationId) {
        return seatingAccommodationService.getAssignedAttendees(accommodationId);
    }

    @PostMapping("/accommodations/{accommodationId}/assign")
    @Operation(summary = "分配人员到住宿")
    public ResponseEntity<Map<String, Object>> assignToAccommodation(@PathVariable Long accommodationId, @Valid @RequestBody AssignRequest request) {
        seatingAccommodationService.assignAttendeeToAccommodation(accommodationId, request.getAttendeeId(), request.getAttendeeName(), request.getDepartment());
        return ResponseEntity.ok(Map.of("success", true, "message", "分配成功"));
    }

    @DeleteMapping("/accommodations/{accommodationId}/assign/{attendeeId}")
    @Operation(summary = "取消住宿人员分配")
    public ResponseEntity<Map<String, Object>> unassignFromAccommodation(@PathVariable Long accommodationId, @PathVariable Long attendeeId) {
        seatingAccommodationService.unassignAttendeeFromAccommodation(accommodationId, attendeeId);
        return ResponseEntity.ok(Map.of("success", true, "message", "取消分配成功"));
    }

    // ==================== 人员分配（讨论室） ====================

    @GetMapping("/discussions/{discussionId}/assignees")
    @Operation(summary = "获取讨论室已分配人员列表")
    public List<AssignedAttendeeResponse> getDiscussionAssignees(@PathVariable Long discussionId) {
        return seatingDiscussionService.getAssignedAttendees(discussionId);
    }

    @PostMapping("/discussions/{discussionId}/assign")
    @Operation(summary = "分配人员到讨论室")
    public ResponseEntity<Map<String, Object>> assignToDiscussion(@PathVariable Long discussionId, @Valid @RequestBody AssignRequest request) {
        seatingDiscussionService.assignAttendeeToDiscussion(discussionId, request.getAttendeeId(), request.getAttendeeName(), request.getDepartment());
        return ResponseEntity.ok(Map.of("success", true, "message", "分配成功"));
    }

    @DeleteMapping("/discussions/{discussionId}/assign/{attendeeId}")
    @Operation(summary = "取消讨论室人员分配")
    public ResponseEntity<Map<String, Object>> unassignFromDiscussion(@PathVariable Long discussionId, @PathVariable Long attendeeId) {
        seatingDiscussionService.unassignAttendeeFromDiscussion(discussionId, attendeeId);
        return ResponseEntity.ok(Map.of("success", true, "message", "取消分配成功"));
    }

    // ==================== 人员分配（用餐） ====================

    @GetMapping("/dinings/{diningId}/assignees")
    @Operation(summary = "获取用餐已分配人员列表")
    public List<AssignedAttendeeResponse> getDiningAssignees(@PathVariable Long diningId) {
        return seatingDiningService.getAssignedAttendees(diningId);
    }

    @PostMapping("/dinings/{diningId}/assign")
    @Operation(summary = "分配人员到用餐")
    public ResponseEntity<Map<String, Object>> assignToDining(@PathVariable Long diningId, @Valid @RequestBody AssignRequest request) {
        seatingDiningService.assignAttendeeToDining(diningId, request.getAttendeeId(), request.getAttendeeName(), request.getDepartment());
        return ResponseEntity.ok(Map.of("success", true, "message", "分配成功"));
    }

    @DeleteMapping("/dinings/{diningId}/assign/{attendeeId}")
    @Operation(summary = "取消用餐人员分配")
    public ResponseEntity<Map<String, Object>> unassignFromDining(@PathVariable Long diningId, @PathVariable Long attendeeId) {
        seatingDiningService.unassignAttendeeFromDining(diningId, attendeeId);
        return ResponseEntity.ok(Map.of("success", true, "message", "取消分配成功"));
    }
}
