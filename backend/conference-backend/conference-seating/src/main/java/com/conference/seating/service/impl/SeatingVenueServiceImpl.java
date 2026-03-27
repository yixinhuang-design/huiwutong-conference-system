package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.VenueCreateRequest;
import com.conference.seating.dto.VenueDetailResponse;
import com.conference.seating.dto.VenueUpdateRequest;
import com.conference.seating.dto.VenueSeatStatsDto;
import com.conference.seating.entity.SeatingVenue;
import com.conference.seating.mapper.SeatingVenueMapper;
import com.conference.seating.service.SeatingVenueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会场管理服务实现
 *
 * 实现会场管理的业务逻辑，包括创建、更新、删除、查询等
 *
 * @author AI Assistant
 * @version 1.0.0
 */
@Slf4j
@Service
public class SeatingVenueServiceImpl extends ServiceImpl<SeatingVenueMapper, SeatingVenue> implements SeatingVenueService {

    private final SeatingVenueMapper venueMapper;

    public SeatingVenueServiceImpl(SeatingVenueMapper venueMapper) {
        this.venueMapper = venueMapper;
    }

    @Override
    public List<VenueDetailResponse> getVenuesByConference(Long conferenceId) {
        if (conferenceId == null) {
            throw new IllegalArgumentException("会议ID不能为空");
        }
        
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingVenue> venues = venueMapper.selectByConferenceId(conferenceId, tenantId);
        
        return venues.stream()
                .map(this::buildVenueDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VenueDetailResponse getVenueDetail(Long venueId) {
        if (venueId == null) {
            throw new IllegalArgumentException("会场ID不能为空");
        }
        
        SeatingVenue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new BusinessException("会场不存在或无权限访问");
        }
        
        return buildVenueDetailResponse(venue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueDetailResponse createVenue(VenueCreateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        SeatingVenue venue = new SeatingVenue();
        venue.setConferenceId(request.getConferenceId());
        venue.setTenantId(tenantId);
        venue.setVenueName(request.getVenueName());
        venue.setVenueType(request.getVenueType());
        venue.setTotalRows(request.getTotalRows());
        venue.setTotalColumns(request.getTotalColumns());
        venue.setCapacity(request.getTotalRows() * request.getTotalColumns());
        venue.setCreatedBy(tenantId);
        venue.setCreatedAt(LocalDateTime.now());
        
        venueMapper.insert(venue);
        log.info("[租户{}] 创建会场: {}", tenantId, request.getVenueName());
        
        return buildVenueDetailResponse(venue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueDetailResponse updateVenue(Long venueId, VenueUpdateRequest request) {
        SeatingVenue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new BusinessException("会场不存在");
        }
        
        if (request.getVenueName() != null) {
            venue.setVenueName(request.getVenueName());
        }
        if (request.getVenueType() != null) {
            venue.setVenueType(request.getVenueType());
        }
        if (request.getTotalRows() != null) {
            venue.setTotalRows(request.getTotalRows());
        }
        if (request.getTotalColumns() != null) {
            venue.setTotalColumns(request.getTotalColumns());
        }
        
        venue.setUpdatedBy(TenantContextHolder.getTenantId());
        venue.setUpdatedAt(LocalDateTime.now());
        
        venueMapper.updateById(venue);
        log.info("[租户{}] 更新会场: {}", TenantContextHolder.getTenantId(), venueId);
        
        return buildVenueDetailResponse(venue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVenue(Long venueId) {
        SeatingVenue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new BusinessException("会场不存在");
        }
        
        venueMapper.deleteById(venueId);
        log.info("[租户{}] 删除会场: {}", TenantContextHolder.getTenantId(), venueId);
    }

    @Override
    public boolean venueExists(Long venueId) {
        if (venueId == null) {
            return false;
        }
        return venueMapper.selectById(venueId) != null;
    }

    @Override
    public VenueSeatStatsDto getSeatStats(Long venueId) {
        SeatingVenue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new BusinessException("会场不存在");
        }
        
        return VenueSeatStatsDto.builder()
                .venueId(venueId)
                .totalSeats(venue.getCapacity())
                .assignedSeats(venue.getAssignedCount() != null ? venue.getAssignedCount() : 0)
                .availableSeats(venue.getCapacity() - (venue.getAssignedCount() != null ? venue.getAssignedCount() : 0))
                .build();
    }

    private VenueDetailResponse buildVenueDetailResponse(SeatingVenue venue) {
        Integer assignedSeats = venue.getAssignedCount() != null ? venue.getAssignedCount() : 0;
        Integer availableSeats = venue.getCapacity() - assignedSeats;
        
        return VenueDetailResponse.builder()
                .id(venue.getId())
                .conferenceId(venue.getConferenceId())
                .venueName(venue.getVenueName())
                .venueType(venue.getVenueType())
                .totalRows(venue.getTotalRows())
                .totalColumns(venue.getTotalColumns())
                .capacity(venue.getCapacity())
                .assignedSeats(assignedSeats)
                .availableSeats(availableSeats)
                .layoutData(venue.getLayoutData())
                .createdAt(venue.getCreatedAt())
                .updatedAt(venue.getUpdatedAt())
                .build();
    }
}
