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
     * 优化：批量插入座位、精确删除assignment、记录occupantId
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

        // 4. 删除旧分配记录（精确匹配 scheduleId，null 也要精确匹配）
        LambdaQueryWrapper<SeatingAssignment> assignDelWrapper = new LambdaQueryWrapper<>();
        assignDelWrapper.eq(SeatingAssignment::getConferenceId, conferenceId)
                .eq(SeatingAssignment::getTenantId, tenantId);
        if (scheduleId != null) {
            assignDelWrapper.eq(SeatingAssignment::getScheduleId, scheduleId);
        } else {
            assignDelWrapper.isNull(SeatingAssignment::getScheduleId);
        }
        assignmentMapper.delete(assignDelWrapper);

        // 5. 遍历每个座位区，创建会场 + 座位
        if (request.getAreas() == null || request.getAreas().isEmpty()) {
            log.info("[租户{}] 无座位区数据，仅清除旧数据", tenantId);
            return;
        }

        // 构建参会人员ID→名称映射（用于将前端att_xxx与名称关联）
        Map<String, String> attendeeIdToName = new HashMap<>();
        if (request.getAttendees() != null) {
            for (LayoutSaveRequest.AttendeeData att : request.getAttendees()) {
                if (att.getAttendeeId() != null && att.getName() != null) {
                    attendeeIdToName.put(att.getAttendeeId(), att.getName());
                }
            }
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

            // 创建座位记录（批量收集后一次性插入）
            if (area.getSeats() != null && !area.getSeats().isEmpty()) {
                int assignedCount = 0;
                List<SeatingSeat> seatBatch = new ArrayList<>();
                for (LayoutSaveRequest.SeatData seatData : area.getSeats()) {
                    String status = seatData.getStatus();
                    if ("occupied".equals(status)) {
                        status = "assigned";
                    }

                    // 尝试获取occupantName：优先用seatData自带的，其次从attendees映射查
                    String occupantName = seatData.getOccupantName();
                    String occupantId = seatData.getOccupantId();
                    if (occupantName == null && occupantId != null) {
                        occupantName = attendeeIdToName.get(occupantId);
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
                            .assignedUserName(occupantName)
                            .createdAt(LocalDateTime.now())
                            .build();
                    seatBatch.add(seat);

                    if ("assigned".equals(status) || occupantName != null) {
                        assignedCount++;
                    }
                }

                // 批量插入座位（每500条一批）
                int batchSize = 500;
                for (int i = 0; i < seatBatch.size(); i += batchSize) {
                    List<SeatingSeat> subList = seatBatch.subList(i, Math.min(i + batchSize, seatBatch.size()));
                    for (SeatingSeat s : subList) {
                        seatMapper.insert(s);
                    }
                }

                // 更新会场的已分配数
                venue.setAssignedCount(assignedCount);
                venueMapper.updateById(venue);

                // 注意：不在 saveLayout 中写入 conf_seating_assignment 表
                // 原因：该表 attendee_id 有 NOT NULL + FK 约束指向 conf_seating_attendee 表，
                // 而前端参会人员来自报名服务（att_xxx格式ID），conf_seating_attendee 表无对应记录。
                // 座位分配信息已完整保存在 conf_seating_seat.assigned_user_name 字段中，
                // loadLayout 可正确还原。assignment 表仅由 assignSeat/autoAssign 等精确分配接口写入。
            }
        }

        log.info("[租户{}] 座位布局保存完成: conferenceId={}, scheduleId={}, 共{}个座位区",
                tenantId, conferenceId, scheduleId, request.getAreas().size());
    }

    /**
     * 整体加载座位布局
     * 优先从 layoutData JSON 还原完整区域结构
     * 同时返回 occupantId 和 attendees 列表
     */
    @Override
    public LayoutLoadResponse loadLayout(Long conferenceId, Long scheduleId) {
        Long tenantId = TenantContextHolder.getTenantId();

        // 加载会场（按日程隔离）
        List<SeatingVenue> venues = venueMapper.selectByConferenceAndSchedule(conferenceId, scheduleId, tenantId);

        log.info("[租户{}] 加载座位布局: conferenceId={}, scheduleId={}, 找到{}个会场",
                tenantId, conferenceId, scheduleId, venues.size());

        List<LayoutSaveRequest.AreaData> areas = new ArrayList<>();
        // 收集所有已分配座位的人员信息用于构建attendees列表
        Map<String, LayoutSaveRequest.AttendeeData> attendeeMap = new LinkedHashMap<>();

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

                // 构建 occupantId：如果有 assignedUserName，生成前端兼容的 att_ 格式ID
                String occupantId = null;
                String occupantName = seat.getAssignedUserName();
                if (occupantName != null && !occupantName.isEmpty()) {
                    occupantId = "att_db_" + seat.getId();
                    // 收集到 attendees 列表
                    attendeeMap.putIfAbsent(occupantId, LayoutSaveRequest.AttendeeData.builder()
                            .attendeeId(occupantId)
                            .name(occupantName)
                            .department("")
                            .position("")
                            .company("")
                            .isVip("vip".equals(seat.getSeatType()))
                            .phone("")
                            .build());
                }

                return LayoutSaveRequest.SeatData.builder()
                        .seatId(String.valueOf(seat.getId()))
                        .row(seat.getRowNum())
                        .col(seat.getColumnNum())
                        .seatNumber(seat.getSeatNumber())
                        .seatType(seat.getSeatType())
                        .status(status)
                        .occupantId(occupantId)
                        .occupantName(occupantName)
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
                .attendees(new ArrayList<>(attendeeMap.values()))
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

    @Override
    public List<SeatingSeat> getAvailableSeatsByConference(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        // 获取会议下所有会场
        LambdaQueryWrapper<SeatingVenue> venueWrapper = new LambdaQueryWrapper<>();
        venueWrapper.eq(SeatingVenue::getConferenceId, conferenceId);
        if (tenantId != null) {
            venueWrapper.eq(SeatingVenue::getTenantId, tenantId);
        }
        List<SeatingVenue> venues = venueMapper.selectList(venueWrapper);
        if (venues.isEmpty()) return Collections.emptyList();

        List<Long> venueIds = venues.stream().map(SeatingVenue::getId).collect(Collectors.toList());
        
        // 获取所有未分配的座位
        LambdaQueryWrapper<SeatingSeat> seatWrapper = new LambdaQueryWrapper<>();
        seatWrapper.in(SeatingSeat::getVenueId, venueIds)
                   .isNull(SeatingSeat::getAssignedUserId);
        return seatMapper.selectList(seatWrapper);
    }
}
