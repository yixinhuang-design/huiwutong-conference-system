package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.DiningCreateRequest;
import com.conference.seating.dto.DiningDetailResponse;
import com.conference.seating.dto.DiningUpdateRequest;
import com.conference.seating.entity.SeatingDining;
import com.conference.seating.mapper.SeatingDiningMapper;
import com.conference.seating.service.SeatingDiningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用餐安排服务实现
 * @author AI Assistant
 * @date 2026-03-12
 */
@Slf4j
@Service
public class SeatingDiningServiceImpl implements SeatingDiningService {
    
    private final SeatingDiningMapper seatingDiningMapper;

    public SeatingDiningServiceImpl(SeatingDiningMapper seatingDiningMapper) {
        this.seatingDiningMapper = seatingDiningMapper;
    }

    @Override
    public List<DiningDetailResponse> getDiningsByConference(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingDining> dinings = seatingDiningMapper.selectByConferenceId(conferenceId, tenantId);
        return dinings.stream()
                .map(this::buildDiningDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DiningDetailResponse getDiningDetail(Long diningId) {
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在或无权限访问");
        }
        return buildDiningDetailResponse(dining);
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
        dining.setLocation(request.getLocation());
        dining.setMealTime(request.getMealTime());
        dining.setRemarks(request.getRemarks());
        dining.setCreatedAt(LocalDateTime.now());
        
        seatingDiningMapper.insert(dining);
        log.info("[租户{}] 创建用餐: {}", tenantId, request.getRestaurantName());
        
        return buildDiningDetailResponse(dining);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiningDetailResponse updateDining(Long diningId, DiningUpdateRequest request) {
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在");
        }
        
        if (request.getRestaurantName() != null) {
            dining.setRestaurantName(request.getRestaurantName());
        }
        if (request.getMealType() != null) {
            dining.setMealType(request.getMealType());
        }
        if (request.getCapacity() != null) {
            dining.setCapacity(request.getCapacity());
        }
        if (request.getLocation() != null) {
            dining.setLocation(request.getLocation());
        }
        if (request.getMealTime() != null) {
            dining.setMealTime(request.getMealTime());
        }
        if (request.getRemarks() != null) {
            dining.setRemarks(request.getRemarks());
        }
        dining.setUpdatedAt(LocalDateTime.now());
        
        seatingDiningMapper.updateById(dining);
        log.info("[租户{}] 更新用餐: {}", TenantContextHolder.getTenantId(), diningId);
        
        return buildDiningDetailResponse(dining);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDining(Long diningId) {
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在");
        }
        
        seatingDiningMapper.deleteById(diningId);
        log.info("[租户{}] 删除用餐: {}", TenantContextHolder.getTenantId(), diningId);
    }

    @Override
    public void assignAttendeeToDining(Long diningId, Long attendeeId) {
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在");
        }
        
        Integer assignedCount = dining.getAssignedCount() != null ? dining.getAssignedCount() : 0;
        if (assignedCount >= dining.getCapacity()) {
            throw new BusinessException("用餐已满，无法分配");
        }
        
        dining.setAssignedCount(assignedCount + 1);
        seatingDiningMapper.updateById(dining);
        log.info("[租户{}] 分配参会人员到用餐: {}", TenantContextHolder.getTenantId(), diningId);
    }

    @Override
    public void unassignAttendeeFromDining(Long diningId, Long attendeeId) {
        SeatingDining dining = getDiningByIdAndTenant(diningId);
        if (dining == null) {
            throw new BusinessException("用餐不存在");
        }
        
        Integer assignedCount = dining.getAssignedCount() != null ? dining.getAssignedCount() : 0;
        if (assignedCount > 0) {
            dining.setAssignedCount(assignedCount - 1);
            seatingDiningMapper.updateById(dining);
        }
        log.info("[租户{}] 取消分配参会人员: {}", TenantContextHolder.getTenantId(), diningId);
    }

    @Override
    public List<DiningDetailResponse> getAvailableDinings(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingDining> dinings = seatingDiningMapper.selectAvailableDinings(conferenceId, tenantId);
        return dinings.stream()
                .map(this::buildDiningDetailResponse)
                .collect(Collectors.toList());
    }

    private SeatingDining getDiningByIdAndTenant(Long diningId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingDining> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingDining::getId, diningId)
               .eq(SeatingDining::getTenantId, tenantId);
        return seatingDiningMapper.selectOne(wrapper);
    }

    private DiningDetailResponse buildDiningDetailResponse(SeatingDining dining) {
        Integer assignedCount = dining.getAssignedCount() != null ? dining.getAssignedCount() : 0;
        Integer availableSeats = dining.getCapacity() - assignedCount;
        
        return DiningDetailResponse.builder()
                .id(dining.getId())
                .conferenceId(dining.getConferenceId())
                .restaurantName(dining.getRestaurantName())
                .mealType(dining.getMealType())
                .capacity(dining.getCapacity())
                .assignedCount(assignedCount)
                .availableSeats(availableSeats)
                .location(dining.getLocation())
                .mealTime(dining.getMealTime())
                .remarks(dining.getRemarks())
                .createdAt(dining.getCreatedAt())
                .updatedAt(dining.getUpdatedAt())
                .build();
    }
}
