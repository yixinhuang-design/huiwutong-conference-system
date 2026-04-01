package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.AccommodationCreateRequest;
import com.conference.seating.dto.AccommodationDetailResponse;
import com.conference.seating.dto.AccommodationUpdateRequest;
import com.conference.seating.dto.AssignedAttendeeResponse;
import com.conference.seating.entity.SeatingAccommodation;
import com.conference.seating.entity.SeatingAccommodationAssign;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.mapper.SeatingAccommodationAssignMapper;
import com.conference.seating.mapper.SeatingAccommodationMapper;
import com.conference.seating.service.SeatingAccommodationService;
import com.conference.seating.service.SeatingAttendeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 住宿安排服务实现
 */
@Slf4j
@Service
public class SeatingAccommodationServiceImpl implements SeatingAccommodationService {

    private final SeatingAccommodationMapper accommodationMapper;
    private final SeatingAccommodationAssignMapper assignMapper;
    private final SeatingAttendeeService attendeeService;

    public SeatingAccommodationServiceImpl(SeatingAccommodationMapper accommodationMapper,
                                            SeatingAccommodationAssignMapper assignMapper,
                                            SeatingAttendeeService attendeeService) {
        this.accommodationMapper = accommodationMapper;
        this.assignMapper = assignMapper;
        this.attendeeService = attendeeService;
    }

    @Override
    public List<AccommodationDetailResponse> getAccommodationsByConference(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingAccommodation> accommodations = accommodationMapper.selectByConferenceId(conferenceId, tenantId);
        return accommodations.stream()
                .map(a -> buildDetailResponse(a, true))
                .collect(Collectors.toList());
    }

    @Override
    public AccommodationDetailResponse getAccommodationDetail(Long accommodationId) {
        SeatingAccommodation accommodation = getByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在或无权限访问");
        }
        return buildDetailResponse(accommodation, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccommodationDetailResponse createAccommodation(AccommodationCreateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();

        SeatingAccommodation accommodation = new SeatingAccommodation();
        accommodation.setConferenceId(request.getConferenceId());
        accommodation.setTenantId(tenantId);
        accommodation.setHotelName(request.getHotelName());
        accommodation.setRoomNumber(request.getRoomNumber());
        accommodation.setRoomType(request.getRoomType());
        accommodation.setCapacity(request.getCapacity());
        accommodation.setAssignedCount(0);
        accommodation.setCheckInTime(request.getCheckInTime());
        accommodation.setCheckOutTime(request.getCheckOutTime());
        accommodation.setCreatedAt(LocalDateTime.now());

        accommodationMapper.insert(accommodation);
        log.info("[租户{}] 创建住宿: {} {}", tenantId, request.getHotelName(), request.getRoomNumber());

        return buildDetailResponse(accommodation, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccommodationDetailResponse updateAccommodation(Long accommodationId, AccommodationUpdateRequest request) {
        SeatingAccommodation accommodation = getByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在");
        }

        if (request.getHotelName() != null) accommodation.setHotelName(request.getHotelName());
        if (request.getRoomNumber() != null) accommodation.setRoomNumber(request.getRoomNumber());
        if (request.getRoomType() != null) accommodation.setRoomType(request.getRoomType());
        if (request.getCapacity() != null) accommodation.setCapacity(request.getCapacity());
        if (request.getCheckInTime() != null) accommodation.setCheckInTime(request.getCheckInTime());
        if (request.getCheckOutTime() != null) accommodation.setCheckOutTime(request.getCheckOutTime());
        accommodation.setUpdatedAt(LocalDateTime.now());

        accommodationMapper.updateById(accommodation);
        log.info("[租户{}] 更新住宿: {}", TenantContextHolder.getTenantId(), accommodationId);

        return buildDetailResponse(accommodation, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccommodation(Long accommodationId) {
        SeatingAccommodation accommodation = getByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在");
        }

        // 级联删除分配记录
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingAccommodationAssign> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingAccommodationAssign::getAccommodationId, accommodationId)
               .eq(SeatingAccommodationAssign::getTenantId, tenantId);
        assignMapper.delete(wrapper);

        accommodationMapper.deleteById(accommodationId);
        log.info("[租户{}] 删除住宿: {}", tenantId, accommodationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignAttendeeToAccommodation(Long accommodationId, Long attendeeId, String attendeeName, String department) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingAccommodation accommodation = getByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在");
        }

        Integer assignedCount = accommodation.getAssignedCount() != null ? accommodation.getAssignedCount() : 0;
        if (assignedCount >= accommodation.getCapacity()) {
            throw new BusinessException("住宿已满，无法分配");
        }

        // 检查重复分配
        SeatingAccommodationAssign existing = assignMapper.selectByAccommodationAndAttendee(accommodationId, attendeeId);
        if (existing != null) {
            throw new BusinessException("该人员已分配到此住宿");
        }

        // 优先使用前端传来的人员信息
        String finalName = (attendeeName != null && !attendeeName.isEmpty()) ? attendeeName : "";
        String finalDept = (department != null && !department.isEmpty()) ? department : "";
        if (finalName.isEmpty()) {
            try {
                List<SeatingAttendee> attendees = attendeeService.getAttendeesByConference(accommodation.getConferenceId());
                for (SeatingAttendee a : attendees) {
                    if (a.getId().equals(attendeeId) || (a.getUserId() != null && a.getUserId().equals(attendeeId))) {
                        finalName = a.getAttendeeName();
                        finalDept = a.getDepartment() != null ? a.getDepartment() : finalDept;
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("获取参会人员信息失败: {}", e.getMessage());
            }
        }

        // 插入分配记录
        SeatingAccommodationAssign assign = SeatingAccommodationAssign.builder()
                .accommodationId(accommodationId)
                .attendeeId(attendeeId)
                .attendeeName(finalName)
                .department(finalDept)
                .conferenceId(accommodation.getConferenceId())
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build();
        assignMapper.insert(assign);

        // 更新计数
        accommodation.setAssignedCount(assignedCount + 1);
        accommodationMapper.updateById(accommodation);

        log.info("[租户{}] 分配人员{}({}）到住宿{}", tenantId, attendeeId, finalName, accommodationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unassignAttendeeFromAccommodation(Long accommodationId, Long attendeeId) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingAccommodation accommodation = getByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在");
        }

        // 删除分配记录
        SeatingAccommodationAssign existing = assignMapper.selectByAccommodationAndAttendee(accommodationId, attendeeId);
        if (existing != null) {
            assignMapper.deleteById(existing.getId());
        }

        // 更新计数
        Integer assignedCount = accommodation.getAssignedCount() != null ? accommodation.getAssignedCount() : 0;
        if (assignedCount > 0) {
            accommodation.setAssignedCount(assignedCount - 1);
            accommodationMapper.updateById(accommodation);
        }

        log.info("[租户{}] 取消分配人员{}从住宿{}", tenantId, attendeeId, accommodationId);
    }

    @Override
    public List<AccommodationDetailResponse> getAvailableAccommodations(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingAccommodation> accommodations = accommodationMapper.selectAvailableAccommodations(conferenceId, tenantId);
        return accommodations.stream()
                .map(a -> buildDetailResponse(a, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignedAttendeeResponse> getAssignedAttendees(Long accommodationId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingAccommodationAssign> assigns = assignMapper.selectByAccommodationId(accommodationId, tenantId);
        return assigns.stream()
                .map(a -> AssignedAttendeeResponse.builder()
                        .assignId(a.getId())
                        .attendeeId(a.getAttendeeId())
                        .attendeeName(a.getAttendeeName())
                        .department(a.getDepartment())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private SeatingAccommodation getByIdAndTenant(Long accommodationId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingAccommodation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingAccommodation::getId, accommodationId)
               .eq(SeatingAccommodation::getTenantId, tenantId);
        return accommodationMapper.selectOne(wrapper);
    }

    private AccommodationDetailResponse buildDetailResponse(SeatingAccommodation accommodation, boolean includeAssignees) {
        Integer assignedCount = accommodation.getAssignedCount() != null ? accommodation.getAssignedCount() : 0;
        Integer availableBeds = accommodation.getCapacity() - assignedCount;

        List<AssignedAttendeeResponse> assignees = Collections.emptyList();
        if (includeAssignees) {
            assignees = getAssignedAttendees(accommodation.getId());
        }

        return AccommodationDetailResponse.builder()
                .id(accommodation.getId())
                .conferenceId(accommodation.getConferenceId())
                .hotelName(accommodation.getHotelName())
                .roomNumber(accommodation.getRoomNumber())
                .roomType(accommodation.getRoomType())
                .capacity(accommodation.getCapacity())
                .assignedCount(assignedCount)
                .availableBeds(availableBeds)
                .checkInTime(accommodation.getCheckInTime())
                .checkOutTime(accommodation.getCheckOutTime())
                .assignees(assignees)
                .createdAt(accommodation.getCreatedAt())
                .updatedAt(accommodation.getUpdatedAt())
                .build();
    }
}
