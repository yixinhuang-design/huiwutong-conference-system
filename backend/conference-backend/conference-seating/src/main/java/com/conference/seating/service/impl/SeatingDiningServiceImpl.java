package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.AssignedAttendeeResponse;
import com.conference.seating.dto.DiningCreateRequest;
import com.conference.seating.dto.DiningDetailResponse;
import com.conference.seating.dto.DiningUpdateRequest;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.entity.SeatingDining;
import com.conference.seating.entity.SeatingDiningAssign;
import com.conference.seating.mapper.SeatingDiningAssignMapper;
import com.conference.seating.mapper.SeatingDiningMapper;
import com.conference.seating.service.SeatingAttendeeService;
import com.conference.seating.service.SeatingDiningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用餐安排服务实现
 */
@Slf4j
@Service
public class SeatingDiningServiceImpl implements SeatingDiningService {

    private final SeatingDiningMapper seatingDiningMapper;
    private final SeatingDiningAssignMapper assignMapper;
    private final SeatingAttendeeService attendeeService;

    public SeatingDiningServiceImpl(SeatingDiningMapper seatingDiningMapper,
                                     SeatingDiningAssignMapper assignMapper,
                                     SeatingAttendeeService attendeeService) {
        this.seatingDiningMapper = seatingDiningMapper;
        this.assignMapper = assignMapper;
        this.attendeeService = attendeeService;
    }

    @Override
    public List<DiningDetailResponse> getDiningsByConference(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingDining> dinings = seatingDiningMapper.selectByConferenceId(conferenceId, tenantId);
        return dinings.stream()
                .map(d -> buildDiningDetailResponse(d, true))
                .collect(Collectors.toList());
    }

    @Override
    public DiningDetailResponse getDiningDetail(Long diningId) {
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在或无权限访问");
        }
        return buildDiningDetailResponse(dining, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningDetailResponse createDining(DiningCreateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();

        SeatingDining dining = new SeatingDining();
        dining.setConferenceId(request.getConferenceId());
        dining.setTenantId(tenantId);
        dining.setRestaurantName(request.getRestaurantName());
        dining.setMealType(request.getMealType());
        dining.setCapacity(request.getCapacity());
        dining.setAssignedCount(0);
        dining.setLocation(request.getLocation());
        dining.setDiningDate(request.getDiningDate());
        dining.setDiningTime(request.getDiningTime());
        dining.setMealTime(request.getMealTime());
        dining.setRemarks(request.getRemarks());
        dining.setCreatedAt(LocalDateTime.now());

        seatingDiningMapper.insert(dining);
        log.info("[租户{}] 创建用餐: {}", tenantId, request.getRestaurantName());

        return buildDiningDetailResponse(dining, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningDetailResponse updateDining(Long diningId, DiningUpdateRequest request) {
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在");
        }

        if (request.getRestaurantName() != null) dining.setRestaurantName(request.getRestaurantName());
        if (request.getMealType() != null) dining.setMealType(request.getMealType());
        if (request.getCapacity() != null) dining.setCapacity(request.getCapacity());
        if (request.getLocation() != null) dining.setLocation(request.getLocation());
        if (request.getDiningDate() != null) dining.setDiningDate(request.getDiningDate());
        if (request.getDiningTime() != null) dining.setDiningTime(request.getDiningTime());
        if (request.getMealTime() != null) dining.setMealTime(request.getMealTime());
        if (request.getRemarks() != null) dining.setRemarks(request.getRemarks());
        dining.setUpdatedAt(LocalDateTime.now());

        seatingDiningMapper.updateById(dining);
        log.info("[租户{}] 更新用餐: {}", TenantContextHolder.getTenantId(), diningId);

        return buildDiningDetailResponse(dining, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDining(Long diningId) {
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在");
        }

        // 级联删除分配记录
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingDiningAssign> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingDiningAssign::getDiningId, diningId)
               .eq(SeatingDiningAssign::getTenantId, tenantId);
        assignMapper.delete(wrapper);

        seatingDiningMapper.deleteById(diningId);
        log.info("[租户{}] 删除用餐: {}", tenantId, diningId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignAttendeeToDining(Long diningId, Long attendeeId, String attendeeName, String department) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在");
        }

        Integer assignedCount = dining.getAssignedCount() != null ? dining.getAssignedCount() : 0;
        if (assignedCount >= dining.getCapacity()) {
            throw new BusinessException("用餐已满，无法分配");
        }

        // 检查重复分配
        SeatingDiningAssign existing = assignMapper.selectByDiningAndAttendee(diningId, attendeeId);
        if (existing != null) {
            throw new BusinessException("该人员已分配到此用餐");
        }

        // 优先使用前端传来的人员信息
        String finalName = (attendeeName != null && !attendeeName.isEmpty()) ? attendeeName : "";
        String finalDept = (department != null && !department.isEmpty()) ? department : "";
        if (finalName.isEmpty()) {
            try {
                List<SeatingAttendee> attendees = attendeeService.getAttendeesByConference(dining.getConferenceId());
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
        SeatingDiningAssign assign = SeatingDiningAssign.builder()
                .diningId(diningId)
                .attendeeId(attendeeId)
                .attendeeName(finalName)
                .department(finalDept)
                .conferenceId(dining.getConferenceId())
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build();
        assignMapper.insert(assign);

        // 更新计数
        dining.setAssignedCount(assignedCount + 1);
        seatingDiningMapper.updateById(dining);

        log.info("[租户{}] 分配人员{}({})到用餐{}", tenantId, attendeeId, finalName, diningId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unassignAttendeeFromDining(Long diningId, Long attendeeId) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在");
        }

        // 删除分配记录
        SeatingDiningAssign existing = assignMapper.selectByDiningAndAttendee(diningId, attendeeId);
        if (existing != null) {
            assignMapper.deleteById(existing.getId());
        }

        // 更新计数
        Integer assignedCount = dining.getAssignedCount() != null ? dining.getAssignedCount() : 0;
        if (assignedCount > 0) {
            dining.setAssignedCount(assignedCount - 1);
            seatingDiningMapper.updateById(dining);
        }

        log.info("[租户{}] 取消分配人员{}从用餐{}", tenantId, attendeeId, diningId);
    }

    @Override
    public List<DiningDetailResponse> getAvailableDinings(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingDining> dinings = seatingDiningMapper.selectAvailableDinings(conferenceId, tenantId);
        return dinings.stream()
                .map(d -> buildDiningDetailResponse(d, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignedAttendeeResponse> getAssignedAttendees(Long diningId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingDiningAssign> assigns = assignMapper.selectByDiningId(diningId, tenantId);
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

    private SeatingDining getDiningByIdAndTenant(Long diningId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingDining> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingDining::getId, diningId)
               .eq(SeatingDining::getTenantId, tenantId);
        return seatingDiningMapper.selectOne(wrapper);
    }

    private DiningDetailResponse buildDiningDetailResponse(SeatingDining dining, boolean includeAssignees) {
        Integer assignedCount = dining.getAssignedCount() != null ? dining.getAssignedCount() : 0;
        Integer availableSeats = dining.getCapacity() - assignedCount;

        List<AssignedAttendeeResponse> assignees = Collections.emptyList();
        if (includeAssignees) {
            assignees = getAssignedAttendees(dining.getId());
        }

        return DiningDetailResponse.builder()
                .id(dining.getId())
                .conferenceId(dining.getConferenceId())
                .restaurantName(dining.getRestaurantName())
                .mealType(dining.getMealType())
                .capacity(dining.getCapacity())
                .assignedCount(assignedCount)
                .availableSeats(availableSeats)
                .diningDate(dining.getDiningDate())
                .diningTime(dining.getDiningTime())
                .location(dining.getLocation())
                .mealTime(dining.getMealTime())
                .remarks(dining.getRemarks())
                .assignees(assignees)
                .createdAt(dining.getCreatedAt())
                .updatedAt(dining.getUpdatedAt())
                .build();
    }
}
