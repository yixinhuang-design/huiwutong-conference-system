package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.dto.*;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.entity.SeatingDiscussion;
import com.conference.seating.entity.SeatingDiscussionAssign;
import com.conference.seating.mapper.SeatingDiscussionAssignMapper;
import com.conference.seating.mapper.SeatingDiscussionMapper;
import com.conference.seating.service.SeatingAttendeeService;
import com.conference.seating.service.SeatingDiscussionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 讨论室安排服务实现
 */
@Slf4j
@Service
public class SeatingDiscussionServiceImpl implements SeatingDiscussionService {

    private final SeatingDiscussionMapper discussionMapper;
    private final SeatingDiscussionAssignMapper assignMapper;
    private final SeatingAttendeeService attendeeService;

    public SeatingDiscussionServiceImpl(SeatingDiscussionMapper discussionMapper,
                                        SeatingDiscussionAssignMapper assignMapper,
                                        SeatingAttendeeService attendeeService) {
        this.discussionMapper = discussionMapper;
        this.assignMapper = assignMapper;
        this.attendeeService = attendeeService;
    }

    @Override
    public List<DiscussionDetailResponse> getDiscussionsByConference(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingDiscussion> discussions = discussionMapper.selectByConferenceId(conferenceId, tenantId);
        return discussions.stream()
                .map(d -> buildDetailResponse(d, true))
                .collect(Collectors.toList());
    }

    @Override
    public DiscussionDetailResponse getDiscussionDetail(Long discussionId) {
        SeatingDiscussion discussion = getByIdAndTenant(discussionId);
        if (discussion == null) {
            throw new BusinessException("讨论室不存在或无权限访问");
        }
        return buildDetailResponse(discussion, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiscussionDetailResponse createDiscussion(DiscussionCreateRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();

        SeatingDiscussion discussion = new SeatingDiscussion();
        discussion.setConferenceId(request.getConferenceId());
        discussion.setTenantId(tenantId);
        discussion.setRoomName(request.getRoomName());
        discussion.setLocation(request.getLocation());
        discussion.setCapacity(request.getCapacity());
        discussion.setAssignedCount(0);
        discussion.setStartTime(request.getStartTime());
        discussion.setEndTime(request.getEndTime());
        discussion.setDescription(request.getDescription());
        discussion.setCreatedAt(LocalDateTime.now());

        discussionMapper.insert(discussion);
        log.info("[租户{}] 创建讨论室: {}", tenantId, request.getRoomName());

        return buildDetailResponse(discussion, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiscussionDetailResponse updateDiscussion(Long discussionId, DiscussionUpdateRequest request) {
        SeatingDiscussion discussion = getByIdAndTenant(discussionId);
        if (discussion == null) {
            throw new BusinessException("讨论室不存在");
        }

        if (request.getRoomName() != null) discussion.setRoomName(request.getRoomName());
        if (request.getLocation() != null) discussion.setLocation(request.getLocation());
        if (request.getCapacity() != null) discussion.setCapacity(request.getCapacity());
        if (request.getStartTime() != null) discussion.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) discussion.setEndTime(request.getEndTime());
        if (request.getDescription() != null) discussion.setDescription(request.getDescription());
        discussion.setUpdatedAt(LocalDateTime.now());

        discussionMapper.updateById(discussion);
        log.info("[租户{}] 更新讨论室: {}", TenantContextHolder.getTenantId(), discussionId);

        return buildDetailResponse(discussion, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscussion(Long discussionId) {
        SeatingDiscussion discussion = getByIdAndTenant(discussionId);
        if (discussion == null) {
            throw new BusinessException("讨论室不存在");
        }

        // 同时删除所有分配记录
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingDiscussionAssign> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingDiscussionAssign::getDiscussionId, discussionId)
               .eq(SeatingDiscussionAssign::getTenantId, tenantId);
        assignMapper.delete(wrapper);

        discussionMapper.deleteById(discussionId);
        log.info("[租户{}] 删除讨论室: {}", tenantId, discussionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignAttendeeToDiscussion(Long discussionId, Long attendeeId, String attendeeName, String department) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingDiscussion discussion = getByIdAndTenant(discussionId);
        if (discussion == null) {
            throw new BusinessException("讨论室不存在");
        }

        Integer assignedCount = discussion.getAssignedCount() != null ? discussion.getAssignedCount() : 0;
        if (assignedCount >= discussion.getCapacity()) {
            throw new BusinessException("讨论室已满，无法分配");
        }

        // 检查是否已分配
        SeatingDiscussionAssign existing = assignMapper.selectByDiscussionAndAttendee(discussionId, attendeeId);
        if (existing != null) {
            throw new BusinessException("该人员已分配到此讨论室");
        }

        // 优先使用前端传来的人员信息
        String finalName = (attendeeName != null && !attendeeName.isEmpty()) ? attendeeName : "";
        String finalDept = (department != null && !department.isEmpty()) ? department : "";
        if (finalName.isEmpty()) {
            try {
                List<SeatingAttendee> attendees = attendeeService.getAttendeesByConference(discussion.getConferenceId());
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
        SeatingDiscussionAssign assign = SeatingDiscussionAssign.builder()
                .discussionId(discussionId)
                .attendeeId(attendeeId)
                .attendeeName(finalName)
                .department(finalDept)
                .conferenceId(discussion.getConferenceId())
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build();
        assignMapper.insert(assign);

        // 更新计数
        discussion.setAssignedCount(assignedCount + 1);
        discussionMapper.updateById(discussion);

        log.info("[租户{}] 分配人员{}({}）到讨论室{}", tenantId, attendeeId, finalName, discussionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unassignAttendeeFromDiscussion(Long discussionId, Long attendeeId) {
        Long tenantId = TenantContextHolder.getTenantId();
        SeatingDiscussion discussion = getByIdAndTenant(discussionId);
        if (discussion == null) {
            throw new BusinessException("讨论室不存在");
        }

        // 删除分配记录
        SeatingDiscussionAssign existing = assignMapper.selectByDiscussionAndAttendee(discussionId, attendeeId);
        if (existing != null) {
            assignMapper.deleteById(existing.getId());
        }

        // 更新计数
        Integer assignedCount = discussion.getAssignedCount() != null ? discussion.getAssignedCount() : 0;
        if (assignedCount > 0) {
            discussion.setAssignedCount(assignedCount - 1);
            discussionMapper.updateById(discussion);
        }

        log.info("[租户{}] 取消分配人员{}从讨论室{}", tenantId, attendeeId, discussionId);
    }

    @Override
    public List<DiscussionDetailResponse> getAvailableDiscussions(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingDiscussion> discussions = discussionMapper.selectAvailableDiscussions(conferenceId, tenantId);
        return discussions.stream()
                .map(d -> buildDetailResponse(d, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignedAttendeeResponse> getAssignedAttendees(Long discussionId) {
        Long tenantId = TenantContextHolder.getTenantId();
        List<SeatingDiscussionAssign> assigns = assignMapper.selectByDiscussionId(discussionId, tenantId);
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

    private SeatingDiscussion getByIdAndTenant(Long discussionId) {
        Long tenantId = TenantContextHolder.getTenantId();
        LambdaQueryWrapper<SeatingDiscussion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatingDiscussion::getId, discussionId)
               .eq(SeatingDiscussion::getTenantId, tenantId);
        return discussionMapper.selectOne(wrapper);
    }

    private DiscussionDetailResponse buildDetailResponse(SeatingDiscussion discussion, boolean includeAssignees) {
        Integer assignedCount = discussion.getAssignedCount() != null ? discussion.getAssignedCount() : 0;
        Integer availableSeats = discussion.getCapacity() - assignedCount;

        List<AssignedAttendeeResponse> assignees = Collections.emptyList();
        if (includeAssignees) {
            assignees = getAssignedAttendees(discussion.getId());
        }

        return DiscussionDetailResponse.builder()
                .id(discussion.getId())
                .conferenceId(discussion.getConferenceId())
                .roomName(discussion.getRoomName())
                .location(discussion.getLocation())
                .capacity(discussion.getCapacity())
                .assignedCount(assignedCount)
                .availableSeats(availableSeats)
                .startTime(discussion.getStartTime())
                .endTime(discussion.getEndTime())
                .description(discussion.getDescription())
                .assignees(assignees)
                .createdAt(discussion.getCreatedAt())
                .updatedAt(discussion.getUpdatedAt())
                .build();
    }
}
