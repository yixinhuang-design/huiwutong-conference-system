package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.*;
import com.conference.seating.entity.SeatingAssignment;
import com.conference.seating.entity.SeatingSeat;
import com.conference.seating.entity.SeatingVenue;
import com.conference.seating.mapper.SeatingAssignmentMapper;
import com.conference.seating.mapper.SeatingSeatMapper;
import com.conference.seating.mapper.SeatingVenueMapper;
import com.conference.seating.service.SeatingSeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 座位管理服务实现
 */
@Slf4j
@Service
public class SeatingSeatServiceImpl implements SeatingSeatService {

    @Resource
    private SeatingSeatMapper seatMapper;

    @Resource
    private SeatingVenueMapper venueMapper;

    @Resource
    private SeatingAssignmentMapper assignmentMapper;

    @Override
    public List<SeatListResponse> getSeatsByVenue(Long venueId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingSeat> seats = seatMapper.selectByVenueId(venueId, tenantId);

        // 按 venue 分组返回（这里只有一个 venue）
        SeatListResponse response = SeatListResponse.builder()
                .venueId(venueId)
                .totalSeats(seats.size())
                .assignedSeats((int) seats.stream().filter(s -> "assigned".equals(s.getStatus())).count())
                .build();
        return Collections.singletonList(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignSeat(SeatAssignRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingSeat seat = seatMapper.selectById(request.getSeatId());
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }

        seat.setAssignedUserId(request.getAttendeeId());
        seat.setStatus("assigned");
        seat.setUpdatedAt(LocalDateTime.now());
        seatMapper.updateById(seat);

        // 创建分配记录
        SeatingAssignment assignment = SeatingAssignment.builder()
                .conferenceId(seat.getConferenceId())
                .seatId(seat.getId())
                .attendeeId(request.getAttendeeId())
                .tenantId(tenantId)
                .assignType(request.getAssignType() != null ? request.getAssignType() : "manual")
                .assignTime(LocalDateTime.now())
                .status("assigned")
                .build();
        assignmentMapper.insert(assignment);

        log.info("[租户{}] 分配座位: seatId={}, attendeeId={}", tenantId, request.getSeatId(), request.getAttendeeId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void swapSeats(SeatSwapRequest request) {
        SeatingSeat seat1 = seatMapper.selectById(request.getSeatId1());
        SeatingSeat seat2 = seatMapper.selectById(request.getSeatId2());
        if (seat1 == null || seat2 == null) {
            throw new BusinessException("座位不存在");
        }

        // 交换分配信息
        Long tempUserId = seat1.getAssignedUserId();
        String tempUserName = seat1.getAssignedUserName();
        seat1.setAssignedUserId(seat2.getAssignedUserId());
        seat1.setAssignedUserName(seat2.getAssignedUserName());
        seat2.setAssignedUserId(tempUserId);
        seat2.setAssignedUserName(tempUserName);

        seat1.setUpdatedAt(LocalDateTime.now());
        seat2.setUpdatedAt(LocalDateTime.now());
        seatMapper.updateById(seat1);
        seatMapper.updateById(seat2);

        log.info("[租户{}] 交换座位: {} <-> {}", TenantContextHolder.getTenantId(), request.getSeatId1(), request.getSeatId2());
    }

    @Override
    public VenueSeatStatsDto getSeatStats(Long venueId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingSeat> seats = seatMapper.selectByVenueId(venueId, tenantId);

        int total = seats.size();
        int assigned = (int) seats.stream().filter(s -> "assigned".equals(s.getStatus())).count();
        int available = (int) seats.stream().filter(s -> "available".equals(s.getStatus())).count();
        int reserved = (int) seats.stream().filter(s -> "reserved".equals(s.getStatus())).count();
        int aisle = (int) seats.stream().filter(s -> "aisle".equals(s.getSeatType())).count();
        int vip = (int) seats.stream().filter(s -> "vip".equals(s.getSeatType())).count();

        return VenueSeatStatsDto.builder()
                .venueId(venueId)
                .totalSeats(total)
                .assignedSeats(assigned)
                .availableSeats(available)
                .reservedSeats(reserved)
                .aisleCount(aisle)
                .vipSeats(vip)
                .build();
    }

    /**
     * 整体保存座位布局
     * 策略：删除该会议+日程的旧数据，全量插入新数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLayout(LayoutSaveRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        Long conferenceId = request.getConferenceId();
        Long scheduleId = request.getScheduleId();

        log.info("[租户{}] 保存座位布局: conferenceId={}, scheduleId={}, areas={}",
                tenantId, conferenceId, scheduleId,
                request.getAreas() != null ? request.getAreas().size() : 0);

        // 1. 查出该会议+日程的旧会场ID列表（用于精确删除座位）
        List<SeatingVenue> oldVenues = venueMapper.selectByConferenceAndSchedule(conferenceId, scheduleId, tenantId);
        List<Long> oldVenueIds = oldVenues.stream().map(SeatingVenue::getId).collect(Collectors.toList());

        // 2. 删除旧座位（按 venueId 精确删除）
        if (!oldVenueIds.isEmpty()) {
            for (Long venueId : oldVenueIds) {
                LambdaQueryWrapper<SeatingSeat> seatDelWrapper = new LambdaQueryWrapper<>();
                seatDelWrapper.eq(SeatingSeat::getVenueId, venueId)
                        .eq(SeatingSeat::getTenantId, tenantId);
                seatMapper.delete(seatDelWrapper);
            }
        }

        // 3. 删除旧会场记录
        if (!oldVenueIds.isEmpty()) {
            for (Long venueId : oldVenueIds) {
                venueMapper.deleteById(venueId);
            }
        }

        // 4. 删除旧分配记录
        LambdaQueryWrapper<SeatingAssignment> assignDelWrapper = new LambdaQueryWrapper<>();
        assignDelWrapper.eq(SeatingAssignment::getConferenceId, conferenceId)
                .eq(SeatingAssignment::getTenantId, tenantId);
        if (scheduleId != null) {
            assignDelWrapper.eq(SeatingAssignment::getScheduleId, scheduleId);
        }
        assignmentMapper.delete(assignDelWrapper);

        // 5. 遍历每个座位区，创建会场 + 座位
        if (request.getAreas() == null || request.getAreas().isEmpty()) {
            log.info("[租户{}] 无座位区数据，仅清除旧数据", tenantId);
            return;
        }

        for (LayoutSaveRequest.AreaData area : request.getAreas()) {
            // 创建会场记录
            SeatingVenue venue = SeatingVenue.builder()
                    .conferenceId(conferenceId)
                    .scheduleId(scheduleId)
                    .tenantId(tenantId)
                    .venueName(area.getName())
                    .venueType(area.getVenueType() != null ? area.getVenueType() : "normal")
                    .totalRows(area.getRows() != null ? area.getRows() : 10)
                    .totalColumns(area.getCols() != null ? area.getCols() : 15)
                    .capacity((area.getRows() != null ? area.getRows() : 10) * (area.getCols() != null ? area.getCols() : 15))
                    .layoutData(area.getLayoutJson())
                    .status("active")
                    .createdBy(tenantId)
                    .createdAt(LocalDateTime.now())
                    .build();
            venueMapper.insert(venue);
            Long venueId = venue.getId();

            // 创建座位记录
            if (area.getSeats() != null) {
                int assignedCount = 0;
                for (LayoutSaveRequest.SeatData seatData : area.getSeats()) {
                    String status = seatData.getStatus();
                    if ("occupied".equals(status)) {
                        status = "assigned";
                    }
                    SeatingSeat seat = SeatingSeat.builder()
                            .conferenceId(conferenceId)
                            .venueId(venueId)
                            .tenantId(tenantId)
                            .seatNumber(seatData.getSeatNumber() != null ? seatData.getSeatNumber() : (seatData.getRow() + "-" + seatData.getCol()))
                            .rowNum(seatData.getRow())
                            .columnNum(seatData.getCol())
                            .seatType(seatData.getSeatType() != null ? seatData.getSeatType() : "normal")
                            .status(status != null ? status : "available")
                            .assignedUserId(null)
                            .assignedUserName(seatData.getOccupantName())
                            .createdAt(LocalDateTime.now())
                            .build();
                    seatMapper.insert(seat);

                    // 统计已分配座位数（通过座位状态判断，不再插入assignment记录）
                    // 原因：前端 occupantId 来自报名服务(att_xxx)，不是 conf_seating_attendee 表的ID，
                    // 而 conf_seating_attendee 表目前无数据，插入 assignment 会触发外键约束失败。
                    // 分配人名已记录在 conf_seating_seat.assigned_user_name 中，可完整还原。
                    if ("assigned".equals(status) || seatData.getOccupantName() != null) {
                        assignedCount++;
                    }
                }
                // 更新会场的已分配数
                venue.setAssignedCount(assignedCount);
                venueMapper.updateById(venue);
            }
        }

        log.info("[租户{}] 座位布局保存完成: conferenceId={}, scheduleId={}, 共{}个座位区",
                tenantId, conferenceId, scheduleId, request.getAreas().size());
    }

    /**
     * 整体加载座位布局
     * 优先从 layoutData JSON 还原完整区域结构
     */
    @Override
    public LayoutLoadResponse loadLayout(Long conferenceId, Long scheduleId) {
        Long tenantId = TenantContextHolder.getTenantId();

        // 加载会场（按日程隔离）
        List<SeatingVenue> venues = venueMapper.selectByConferenceAndSchedule(conferenceId, scheduleId, tenantId);

        log.info("[租户{}] 加载座位布局: conferenceId={}, scheduleId={}, 找到{}个会场",
                tenantId, conferenceId, scheduleId, venues.size());

        List<LayoutSaveRequest.AreaData> areas = new ArrayList<>();
        for (SeatingVenue venue : venues) {
            // 优先使用 layoutData（前端原始JSON）来还原区域
            LayoutSaveRequest.AreaData areaData = LayoutSaveRequest.AreaData.builder()
                    .areaId("area_" + venue.getId())
                    .name(venue.getVenueName())
                    .type(mapVenueTypeToAreaType(venue.getVenueType()))
                    .venueType(venue.getVenueType())
                    .rows(venue.getTotalRows())
                    .cols(venue.getTotalColumns())
                    .angle(0)
                    .layoutJson(venue.getLayoutData())
                    .build();

            // 同时加载座位记录以获取最新分配状态
            List<SeatingSeat> seats = seatMapper.selectByVenueId(venue.getId(), tenantId);
            List<LayoutSaveRequest.SeatData> seatDataList = seats.stream().map(seat -> {
                String status = seat.getStatus();
                if ("assigned".equals(status)) {
                    status = "occupied";
                }
                return LayoutSaveRequest.SeatData.builder()
                        .seatId(String.valueOf(seat.getId()))
                        .row(seat.getRowNum())
                        .col(seat.getColumnNum())
                        .seatNumber(seat.getSeatNumber())
                        .seatType(seat.getSeatType())
                        .status(status)
                        .occupantName(seat.getAssignedUserName())
                        .build();
            }).collect(Collectors.toList());
            areaData.setSeats(seatDataList);

            areas.add(areaData);
        }

        String savedAt = venues.isEmpty() ? null :
                venues.stream()
                        .map(SeatingVenue::getCreatedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .map(LocalDateTime::toString)
                        .orElse(null);

        return LayoutLoadResponse.builder()
                .conferenceId(conferenceId)
                .scheduleId(scheduleId)
                .areas(areas)
                .savedAt(savedAt)
                .build();
    }

    private String mapVenueTypeToAreaType(String venueType) {
        if (venueType == null) return "regular";
        switch (venueType.toLowerCase()) {
            case "stage": return "stage";
            case "vip": return "vip";
            default: return "regular";
        }
    }
}
