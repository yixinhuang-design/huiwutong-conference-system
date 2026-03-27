package com.conference.seating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.entity.SeatingSeat;
import com.conference.seating.mapper.SeatingAttendeeMapper;
import com.conference.seating.mapper.SeatingSeatMapper;
import com.conference.seating.service.SeatingAttendeeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 参会人员管理服务实现
 * 负责管理排座系统的参会人员数据，支持从报名服务同步
 *
 * @author AI Assistant
 * @date 2026-03-27
 */
@Slf4j
@Service
public class SeatingAttendeeServiceImpl implements SeatingAttendeeService {

    @Resource
    private SeatingAttendeeMapper attendeeMapper;

    @Resource
    private SeatingSeatMapper seatMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 报名服务地址（通过网关转发）
     */
    private static final String REGISTRATION_SERVICE = "http://localhost:8082";

    @Override
    public List<SeatingAttendee> getAttendeesByConference(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            log.warn("获取参会人员列表: tenantId为空, conferenceId={}", conferenceId);
            return Collections.emptyList();
        }
        return attendeeMapper.selectByConferenceId(conferenceId, tenantId);
    }

    @Override
    public SeatingAttendee getAttendeeDetail(Long attendeeId) {
        return attendeeMapper.selectById(attendeeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeatingAttendee createAttendee(SeatingAttendee attendee) {
        Long tenantId = TenantContextHolder.getTenantId();
        attendee.setTenantId(tenantId);
        attendee.setCreatedAt(LocalDateTime.now());
        attendee.setUpdatedAt(LocalDateTime.now());
        attendeeMapper.insert(attendee);
        return attendee;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignSeatToAttendee(Long attendeeId, Long seatId) {
        SeatingAttendee attendee = attendeeMapper.selectById(attendeeId);
        if (attendee == null) {
            log.warn("分配座位失败: 参会人员不存在, attendeeId={}", attendeeId);
            return;
        }

        // 更新参会人员的座位分配
        attendee.setAssignedSeatId(seatId);
        attendee.setAssignedAt(LocalDateTime.now());
        attendee.setUpdatedAt(LocalDateTime.now());
        attendeeMapper.updateById(attendee);

        // 更新座位的分配状态
        SeatingSeat seat = seatMapper.selectById(seatId);
        if (seat != null) {
            seat.setAssignedUserId(attendee.getUserId());
            seat.setAssignedUserName(attendee.getAttendeeName());
            seat.setStatus("assigned");
            seat.setUpdatedAt(LocalDateTime.now());
            seatMapper.updateById(seat);
        }

        log.info("分配座位成功: attendeeId={}, seatId={}", attendeeId, seatId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromRegistration(Long conferenceId) {
        Long tenantId = TenantContextHolder.getTenantId();
        log.info("[租户{}] 开始从报名服务同步参会人员, conferenceId={}", tenantId, conferenceId);

        try {
            // 调用报名服务获取已审核通过的报名人员列表
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (tenantId != null) {
                headers.set("X-Tenant-Id", String.valueOf(tenantId));
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = REGISTRATION_SERVICE + "/api/registration/list?conferenceId=" + conferenceId + "&status=1&page=1&pageSize=9999";
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.warn("调用报名服务失败: status={}", resp.getStatusCode());
                return 0;
            }

            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode dataNode = root.has("data") ? root.get("data") : null;
            if (dataNode == null) return 0;

            // 支持分页和非分页两种格式
            JsonNode records = dataNode.has("records") ? dataNode.get("records") : dataNode;
            if (!records.isArray()) return 0;

            // 获取现有参会人员列表（避免重复同步）
            List<SeatingAttendee> existing = attendeeMapper.selectByConferenceId(conferenceId, tenantId);
            List<Long> existingUserIds = existing.stream()
                    .map(SeatingAttendee::getUserId)
                    .filter(id -> id != null)
                    .toList();

            int syncedCount = 0;
            for (JsonNode record : records) {
                Long userId = record.has("id") ? record.get("id").asLong() : null;
                if (userId == null) continue;
                if (existingUserIds.contains(userId)) continue; // 已存在则跳过

                SeatingAttendee attendee = SeatingAttendee.builder()
                        .conferenceId(conferenceId)
                        .tenantId(tenantId)
                        .userId(userId)
                        .attendeeName(getJsonText(record, "realName", "name"))
                        .department(getJsonText(record, "department", "organization", "company"))
                        .position(getJsonText(record, "position", "jobTitle"))
                        .attendeePhone(getJsonText(record, "phone", "mobile"))
                        .email(getJsonText(record, "email"))
                        .isVip(false)
                        .isReserved(false)
                        .confirmed(false)
                        .attendanceStatus("registered")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                attendeeMapper.insert(attendee);
                syncedCount++;
            }

            log.info("[租户{}] 同步完成: conferenceId={}, synced={}", tenantId, conferenceId, syncedCount);
            return syncedCount;

        } catch (Exception e) {
            log.error("[租户{}] 同步报名人员失败: conferenceId={}, error={}", tenantId, conferenceId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 从JSON节点中安全获取文本值，支持多个候选字段名
     */
    private String getJsonText(JsonNode node, String... fieldNames) {
        for (String field : fieldNames) {
            if (node.has(field) && !node.get(field).isNull()) {
                String val = node.get(field).asText();
                if (val != null && !val.isEmpty() && !"null".equals(val)) {
                    return val;
                }
            }
        }
        return null;
    }
}
