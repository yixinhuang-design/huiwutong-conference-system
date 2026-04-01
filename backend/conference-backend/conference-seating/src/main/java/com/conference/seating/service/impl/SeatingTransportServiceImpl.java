package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.AssignedAttendeeResponse;
import com.conference.seating.dto.TransportCreateRequest;
import com.conference.seating.dto.TransportDetailResponse;
import com.conference.seating.dto.TransportUpdateRequest;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.entity.SeatingTransport;
import com.conference.seating.entity.SeatingTransportAssign;
import com.conference.seating.mapper.SeatingTransportAssignMapper;
import com.conference.seating.mapper.SeatingTransportMapper;
import com.conference.seating.service.SeatingAttendeeService;
import com.conference.seating.service.SeatingTransportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 车辆运输服务实现
 */
@Slf4j
@Service
public class SeatingTransportServiceImpl implements SeatingTransportService {

    private final SeatingTransportMapper seatingTransportMapper;
    private final SeatingTransportAssignMapper assignMapper;
    private final SeatingAttendeeService attendeeService;

    public SeatingTransportServiceImpl(SeatingTransportMapper seatingTransportMapper,
                                        SeatingTransportAssignMapper assignMapper,
                                        SeatingAttendeeService attendeeService) {
        this.seatingTransportMapper = seatingTransportMapper;
        this.assignMapper = assignMapper;
        this.attendeeService = attendeeService;
    }

    @Override
    public List<TransportDetailResponse> getTransportsByConference(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingTransport> transports = seatingTransportMapper.selectByConferenceId(conferenceId, tenantId);
        return transports.stream()
                .map(t -> buildTransportDetailResponse(t, true))
                .collect(Collectors.toList());
    }

    @Override
    public TransportDetailResponse getTransportDetail(Long transportId) {
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在或无权限访问");
        }
        return buildTransportDetailResponse(transport, true);
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
        transport.setAssignedCount(0);
        transport.setDeparture(request.getDeparture());
        transport.setDestination(request.getDestination());
        transport.setDepartureTime(request.getDepartureTime());
        transport.setDriver(request.getDriver());
        transport.setDriverPhone(request.getDriverPhone());
        transport.setCreatedAt(LocalDateTime.now());

        seatingTransportMapper.insert(transport);
        log.info("[租户{}] 创建车辆: {}", tenantId, request.getLicensePlate());

        return buildTransportDetailResponse(transport, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransportDetailResponse updateTransport(Long transportId, TransportUpdateRequest request) {
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在");
        }

        if (request.getVehicleName() != null) transport.setVehicleName(request.getVehicleName());
        if (request.getLicensePlate() != null) transport.setLicensePlate(request.getLicensePlate());
        if (request.getVehicleType() != null) transport.setVehicleType(request.getVehicleType());
        if (request.getCapacity() != null) transport.setCapacity(request.getCapacity());
        if (request.getDeparture() != null) transport.setDeparture(request.getDeparture());
        if (request.getDestination() != null) transport.setDestination(request.getDestination());
        if (request.getDepartureTime() != null) transport.setDepartureTime(request.getDepartureTime());
        if (request.getDriver() != null) transport.setDriver(request.getDriver());
        if (request.getDriverPhone() != null) transport.setDriverPhone(request.getDriverPhone());
        transport.setUpdatedAt(LocalDateTime.now());

        seatingTransportMapper.updateById(transport);
        log.info("[租户{}] 更新车辆: {}", TenantContextHolder.getTenantId(), transportId);

        return buildTransportDetailResponse(transport, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransport(Long transportId) {
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在");
        }

        // 级联删除分配记录
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingTransportAssign> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingTransportAssign::getTransportId, transportId)
               .eq(SeatingTransportAssign::getTenantId, tenantId);
        assignMapper.delete(wrapper);

        seatingTransportMapper.deleteById(transportId);
        log.info("[租户{}] 删除车辆: {}", tenantId, transportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignAttendeeToTransport(Long transportId, Long attendeeId, String attendeeName, String department) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在");
        }

        Integer assignedCount = transport.getAssignedCount() != null ? transport.getAssignedCount() : 0;
        if (assignedCount >= transport.getCapacity()) {
            throw new BusinessException("车辆已满，无法分配");
        }

        // 检查重复分配
        SeatingTransportAssign existing = assignMapper.selectByTransportAndAttendee(transportId, attendeeId);
        if (existing != null) {
            throw new BusinessException("该人员已分配到此车辆");
        }

        // 优先使用前端传来的人员信息，若空则尝试从本地表查找
        String finalName = (attendeeName != null && !attendeeName.isEmpty()) ? attendeeName : "";
        String finalDept = (department != null && !department.isEmpty()) ? department : "";
        if (finalName.isEmpty()) {
            try {
                List<SeatingAttendee> attendees = attendeeService.getAttendeesByConference(transport.getConferenceId());
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
        SeatingTransportAssign assign = SeatingTransportAssign.builder()
                .transportId(transportId)
                .attendeeId(attendeeId)
                .attendeeName(finalName)
                .department(finalDept)
                .conferenceId(transport.getConferenceId())
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build();
        assignMapper.insert(assign);

        // 更新计数
        transport.setAssignedCount(assignedCount + 1);
        seatingTransportMapper.updateById(transport);

        log.info("[租户{}] 分配人员{}({})到车辆{}", tenantId, attendeeId, finalName, transportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unassignAttendeeFromTransport(Long transportId, Long attendeeId) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingTransport transport = getTransportByIdAndTenant(transportId);
        if (transport == null) {
            throw new BusinessException("车辆不存在");
        }

        // 删除分配记录
        SeatingTransportAssign existing = assignMapper.selectByTransportAndAttendee(transportId, attendeeId);
        if (existing != null) {
            assignMapper.deleteById(existing.getId());
        }

        // 更新计数
        Integer assignedCount = transport.getAssignedCount() != null ? transport.getAssignedCount() : 0;
        if (assignedCount > 0) {
            transport.setAssignedCount(assignedCount - 1);
            seatingTransportMapper.updateById(transport);
        }

        log.info("[租户{}] 取消分配人员{}从车辆{}", tenantId, attendeeId, transportId);
    }

    @Override
    public List<TransportDetailResponse> getAvailableTransports(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingTransport> transports = seatingTransportMapper.selectAvailableTransports(conferenceId, tenantId);
        return transports.stream()
                .map(t -> buildTransportDetailResponse(t, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignedAttendeeResponse> getAssignedAttendees(Long transportId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingTransportAssign> assigns = assignMapper.selectByTransportId(transportId, tenantId);
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

    private SeatingTransport getTransportByIdAndTenant(Long transportId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingTransport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingTransport::getId, transportId)
               .eq(SeatingTransport::getTenantId, tenantId);
        return seatingTransportMapper.selectOne(wrapper);
    }

    private TransportDetailResponse buildTransportDetailResponse(SeatingTransport transport, boolean includeAssignees) {
        Integer assignedCount = transport.getAssignedCount() != null ? transport.getAssignedCount() : 0;
        Integer availableSeats = transport.getCapacity() - assignedCount;

        List<AssignedAttendeeResponse> assignees = Collections.emptyList();
        if (includeAssignees) {
            assignees = getAssignedAttendees(transport.getId());
        }

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
                .assignees(assignees)
                .createdAt(transport.getCreatedAt())
                .updatedAt(transport.getUpdatedAt())
                .build();
    }
}
