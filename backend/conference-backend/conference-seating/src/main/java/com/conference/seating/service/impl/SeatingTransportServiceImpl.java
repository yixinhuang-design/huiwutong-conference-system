package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.TransportCreateRequest;
import com.conference.seating.dto.TransportDetailResponse;
import com.conference.seating.dto.TransportUpdateRequest;
import com.conference.seating.entity.SeatingTransport;
import com.conference.seating.mapper.SeatingTransportMapper;
import com.conference.seating.service.SeatingTransportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 车辆运输服务实现
 * @author AI Assistant
 * @date 2026-03-12
 */
@Slf4j
@Service
public class SeatingTransportServiceImpl implements SeatingTransportService {
    
    private final SeatingTransportMapper seatingTransportMapper;

    public SeatingTransportServiceImpl(SeatingTransportMapper seatingTransportMapper) {
        this.seatingTransportMapper = seatingTransportMapper;
    }

    @Override
    public List<TransportDetailResponse> getTransportsByConference(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingTransport> transports = seatingTransportMapper.selectByConferenceId(conferenceId, tenantId);
        return transports.stream()
                .map(this::buildTransportDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransportDetailResponse getTransportDetail(Long transportId) {
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在或无权限访问");
        }
        return buildTransportDetailResponse(transport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransportDetailResponse createTransport(TransportCreateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        SeatingTransport transport = new SeatingTransport();
        transport.setConferenceId(request.getConferenceId());
        transport.setTenantId(tenantId);
        transport.setVehicleName(request.getVehicleName());
        transport.setLicensePlate(request.getLicensePlate());
        transport.setVehicleType(request.getVehicleType());
        transport.setCapacity(request.getCapacity());
        transport.setDeparture(request.getDeparture());
        transport.setDestination(request.getDestination());
        transport.setDepartureTime(request.getDepartureTime());
        transport.setDriver(request.getDriver());
        transport.setDriverPhone(request.getDriverPhone());
        transport.setCreatedAt(LocalDateTime.now());
        
        seatingTransportMapper.insert(transport);
        log.info("[租户{}] 创建车辆: {}", tenantId, request.getLicensePlate());
        
        return buildTransportDetailResponse(transport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransportDetailResponse updateTransport(Long transportId, TransportUpdateRequest request) {
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在");
        }
        
        if (request.getVehicleName() != null) {
            transport.setVehicleName(request.getVehicleName());
        }
        if (request.getLicensePlate() != null) {
            transport.setLicensePlate(request.getLicensePlate());
        }
        if (request.getVehicleType() != null) {
            transport.setVehicleType(request.getVehicleType());
        }
        if (request.getCapacity() != null) {
            transport.setCapacity(request.getCapacity());
        }
        if (request.getDeparture() != null) {
            transport.setDeparture(request.getDeparture());
        }
        if (request.getDestination() != null) {
            transport.setDestination(request.getDestination());
        }
        if (request.getDepartureTime() != null) {
            transport.setDepartureTime(request.getDepartureTime());
        }
        if (request.getDriver() != null) {
            transport.setDriver(request.getDriver());
        }
        if (request.getDriverPhone() != null) {
            transport.setDriverPhone(request.getDriverPhone());
        }
        transport.setUpdatedAt(LocalDateTime.now());
        
        seatingTransportMapper.updateById(transport);
        log.info("[租户{}] 更新车辆: {}", TenantContextHolder.getTenantId(), transportId);
        
        return buildTransportDetailResponse(transport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransport(Long transportId) {
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在");
        }
        
        seatingTransportMapper.deleteById(transportId);
        log.info("[租户{}] 删除车辆: {}", TenantContextHolder.getTenantId(), transportId);
    }

    @Override
    public void assignAttendeeToTransport(Long transportId, Long attendeeId) {
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在");
        }
        
        Integer assignedCount = transport.getAssignedCount() != null ? transport.getAssignedCount() : 0;
        if (assignedCount >= transport.getCapacity()) {
            throw new BusinessException("车辆已满，无法分配");
        }
        
        transport.setAssignedCount(assignedCount + 1);
        seatingTransportMapper.updateById(transport);
        log.info("[租户{}] 分配参会人员到车辆: {}", TenantContextHolder.getTenantId(), transportId);
    }

    @Override
    public void unassignAttendeeFromTransport(Long transportId, Long attendeeId) {
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在");
        }
        
        Integer assignedCount = transport.getAssignedCount() != null ? transport.getAssignedCount() : 0;
        if (assignedCount > 0) {
            transport.setAssignedCount(assignedCount - 1);
            seatingTransportMapper.updateById(transport);
        }
        log.info("[租户{}] 取消分配参会人员: {}", TenantContextHolder.getTenantId(), transportId);
    }

    @Override
    public List<TransportDetailResponse> getAvailableTransports(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingTransport> transports = seatingTransportMapper.selectAvailableTransports(conferenceId, tenantId);
        return transports.stream()
                .map(this::buildTransportDetailResponse)
                .collect(Collectors.toList());
    }

    private SeatingTransport getTransportByIdAndTenant(Long transportId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingTransport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingTransport::getId, transportId)
               .eq(SeatingTransport::getTenantId, tenantId);
        return seatingTransportMapper.selectOne(wrapper);
    }

    private TransportDetailResponse buildTransportDetailResponse(SeatingTransport transport) {
        Integer assignedCount = transport.getAssignedCount() != null ? transport.getAssignedCount() : 0;
        Integer availableSeats = transport.getCapacity() - assignedCount;
        
        return TransportDetailResponse.builder()
                .id(transport.getId())
                .conferenceId(transport.getConferenceId())
                .vehicleName(transport.getVehicleName())
                .licensePlate(transport.getLicensePlate())
                .vehicleType(transport.getVehicleType())
                .capacity(transport.getCapacity())
                .assignedCount(assignedCount)
                .availableSeats(availableSeats)
                .departure(transport.getDeparture())
                .destination(transport.getDestination())
                .departureTime(transport.getDepartureTime())
                .driver(transport.getDriver())
                .driverPhone(transport.getDriverPhone())
                .createdAt(transport.getCreatedAt())
                .updatedAt(transport.getUpdatedAt())
                .build();
    }
}
