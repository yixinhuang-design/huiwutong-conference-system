package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.AccommodationCreateRequest;
import com.conference.seating.dto.AccommodationDetailResponse;
import com.conference.seating.dto.AccommodationUpdateRequest;
import com.conference.seating.entity.SeatingAccommodation;
import com.conference.seating.mapper.SeatingAccommodationMapper;
import com.conference.seating.service.SeatingAccommodationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 住宿安排服务实现
 *
 * 实现住宿管理的业务逻辑，包括创建、更新、删除、查询等
 *
 * @author AI Assistant
 * @version 1.0.0
 */
@Slf4j
@Service
public class SeatingAccommodationServiceImpl extends ServiceImpl<SeatingAccommodationMapper, SeatingAccommodation> implements SeatingAccommodationService {

    private final SeatingAccommodationMapper accommodationMapper;

    public SeatingAccommodationServiceImpl(SeatingAccommodationMapper accommodationMapper) {
        this.accommodationMapper = accommodationMapper;
    }

    @Override
    public List<AccommodationDetailResponse> getAccommodationsByConference(Long conferenceId) {
        if (conferenceId == null) {
            throw new IllegalArgumentException("会议ID不能为空");
        }
        
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingAccommodation> accommodations = accommodationMapper.selectByConferenceId(conferenceId, tenantId);
        
        return accommodations.stream()
                .map(this::buildAccommodationDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccommodationDetailResponse getAccommodationDetail(Long accommodationId) {
        if (accommodationId == null) {
            throw new IllegalArgumentException("住宿ID不能为空");
        }
        
        SeatingAccommodation accommodation = getAccommodationByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在或无权限访问");
        }
        
        return buildAccommodationDetailResponse(accommodation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccommodationDetailResponse createAccommodation(AccommodationCreateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        SeatingAccommodation accommodation = new SeatingAccommodation();
        accommodation.setConferenceId(request.getConferenceId());
        accommodation.setTenantId(tenantId);
        accommodation.setRoomNumber(request.getRoomNumber());
        accommodation.setRoomType(request.getRoomType());
        accommodation.setCapacity(request.getCapacity());
        accommodation.setAddress(request.getAddress());
        accommodation.setPhone(request.getPhone());
        accommodation.setCreatedAt(LocalDateTime.now());
        
        accommodationMapper.insert(accommodation);
        log.info("[租户{}] 创建住宿: {}", tenantId, request.getRoomNumber());
        
        return buildAccommodationDetailResponse(accommodation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccommodationDetailResponse updateAccommodation(Long accommodationId, AccommodationUpdateRequest request) {
        SeatingAccommodation accommodation = getAccommodationByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在");
        }
        
        if (request.getRoomNumber() != null) {
            accommodation.setRoomNumber(request.getRoomNumber());
        }
        if (request.getRoomType() != null) {
            accommodation.setRoomType(request.getRoomType());
        }
        if (request.getCapacity() != null) {
            accommodation.setCapacity(request.getCapacity());
        }
        if (request.getAddress() != null) {
            accommodation.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            accommodation.setPhone(request.getPhone());
        }
        accommodation.setUpdatedAt(LocalDateTime.now());
        
        accommodationMapper.updateById(accommodation);
        log.info("[租户{}] 更新住宿: {}", TenantContextHolder.getTenantId(), accommodationId);
        
        return buildAccommodationDetailResponse(accommodation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccommodation(Long accommodationId) {
        SeatingAccommodation accommodation = getAccommodationByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在");
        }
        
        accommodationMapper.deleteById(accommodationId);
        log.info("[租户{}] 删除住宿: {}", TenantContextHolder.getTenantId(), accommodationId);
    }

    @Override
    public void assignAttendeeToAccommodation(Long accommodationId, Long attendeeId) {
        SeatingAccommodation accommodation = getAccommodationByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在");
        }
        
        Integer assignedCount = accommodation.getAssignedCount() != null ? accommodation.getAssignedCount() : 0;
        if (assignedCount >= accommodation.getCapacity()) {
            throw new BusinessException("住宿已满，无法分配");
        }
        
        accommodation.setAssignedCount(assignedCount + 1);
        accommodationMapper.updateById(accommodation);
        log.info("[租户{}] 分配参会人员到住宿: {}", TenantContextHolder.getTenantId(), accommodationId);
    }

    @Override
    public void unassignAttendeeFromAccommodation(Long accommodationId, Long attendeeId) {
        SeatingAccommodation accommodation = getAccommodationByIdAndTenant(accommodationId);
        if (accommodation == null) {
            throw new BusinessException("住宿不存在");
        }
        
        Integer assignedCount = accommodation.getAssignedCount() != null ? accommodation.getAssignedCount() : 0;
        if (assignedCount > 0) {
            accommodation.setAssignedCount(assignedCount - 1);
            accommodationMapper.updateById(accommodation);
        }
        log.info("[租户{}] 取消分配参会人员: {}", TenantContextHolder.getTenantId(), accommodationId);
    }

    @Override
    public List<AccommodationDetailResponse> getAvailableAccommodations(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingAccommodation> accommodations = accommodationMapper.selectAvailableAccommodations(conferenceId, tenantId);
        return accommodations.stream()
                .map(this::buildAccommodationDetailResponse)
                .collect(Collectors.toList());
    }

    private SeatingAccommodation getAccommodationByIdAndTenant(Long accommodationId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingAccommodation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingAccommodation::getId, accommodationId)
               .eq(SeatingAccommodation::getTenantId, tenantId);
        return accommodationMapper.selectOne(wrapper);
    }

    private AccommodationDetailResponse buildAccommodationDetailResponse(SeatingAccommodation accommodation) {
        Integer assignedCount = accommodation.getAssignedCount() != null ? accommodation.getAssignedCount() : 0;
        Integer availableBeds = accommodation.getCapacity() - assignedCount;
        
        return AccommodationDetailResponse.builder()
                .id(accommodation.getId())
                .conferenceId(accommodation.getConferenceId())
                .roomNumber(accommodation.getRoomNumber())
                .roomType(accommodation.getRoomType())
                .capacity(accommodation.getCapacity())
                .assignedCount(assignedCount)
                .availableBeds(availableBeds)
                .address(accommodation.getAddress())
                .phone(accommodation.getPhone())
                .createdAt(accommodation.getCreatedAt())
                .updatedAt(accommodation.getUpdatedAt())
                .build();
    }
}
