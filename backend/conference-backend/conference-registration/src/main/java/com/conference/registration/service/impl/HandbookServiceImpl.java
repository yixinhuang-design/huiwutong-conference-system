package com.conference.registration.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.registration.dto.HandbookSaveRequest;
import com.conference.registration.entity.Handbook;
import com.conference.registration.entity.HandbookDiscussion;
import com.conference.registration.entity.Registration;
import com.conference.registration.mapper.HandbookDiscussionMapper;
import com.conference.registration.mapper.HandbookMapper;
import com.conference.registration.mapper.RegistrationMapper;
import com.conference.registration.service.HandbookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学员手册服务实现
 * 
 * 数据来源说明（不做单独存储，实时从源表获取）:
 * - 日程数据 → conf_schedule 表
 * - 资料/教案 → conf_schedule_attachment 表
 * - 学员名册 → conf_registration 表 (status=1已审核)
 * - 分组数据 → conf_group + conf_group_member 表
 * - 讨论题目 → conf_handbook_discussion 表（手册独有，需要存储）
 * - 手册配置 → conf_handbook 表（配置数据需要存储）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HandbookServiceImpl implements HandbookService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final HandbookMapper handbookMapper;
    private final HandbookDiscussionMapper discussionMapper;
    private final RegistrationMapper registrationMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public Map<String, Object> saveHandbook(HandbookSaveRequest request) {
        Long tenantId = currentTenantId();
        Long meetingId = request.getMeetingId();
        log.info("[Handbook] 保存手册配置, meetingId={}, tenantId={}", meetingId, tenantId);

        // 查找是否已有此会议的手册
        Handbook handbook = handbookMapper.selectOne(
                new LambdaQueryWrapper<Handbook>()
                        .eq(Handbook::getMeetingId, meetingId)
                        .eq(Handbook::getTenantId, tenantId)
                        .eq(Handbook::getDeleted, 0)
        );

        boolean isNew = (handbook == null);
        if (isNew) {
            handbook = new Handbook();
            handbook.setMeetingId(meetingId);
            handbook.setTenantId(tenantId);
            handbook.setStatus(0); // 草稿
            handbook.setDeleted(0);
            handbook.setCreateTime(LocalDateTime.now());
        }

        // 更新配置字段
        handbook.setTitle(request.getTitle());
        handbook.setUpdateTime(LocalDateTime.now());

        if (request.getCoverConfig() != null) {
            handbook.setCoverConfig(JSON.toJSONString(request.getCoverConfig()));
        }
        if (request.getTocConfig() != null) {
            handbook.setTocConfig(JSON.toJSONString(request.getTocConfig()));
        }
        if (request.getRosterConfig() != null) {
            handbook.setRosterConfig(JSON.toJSONString(request.getRosterConfig()));
        }
        if (request.getRosterFields() != null) {
            handbook.setRosterFields(JSON.toJSONString(request.getRosterFields()));
        }
        if (request.getSeatingConfig() != null) {
            handbook.setSeatingConfig(JSON.toJSONString(request.getSeatingConfig()));
        }
        if (request.getTransportConfig() != null) {
            handbook.setTransportConfig(JSON.toJSONString(request.getTransportConfig()));
        }
        if (request.getHotelConfig() != null) {
            handbook.setHotelConfig(JSON.toJSONString(request.getHotelConfig()));
        }
        if (request.getMealConfig() != null) {
            handbook.setMealConfig(JSON.toJSONString(request.getMealConfig()));
        }
        if (request.getDiscussionConfig() != null) {
            handbook.setDiscussionConfig(JSON.toJSONString(request.getDiscussionConfig()));
        }
        if (request.getBackcoverConfig() != null) {
            handbook.setBackcoverConfig(JSON.toJSONString(request.getBackcoverConfig()));
        }
        handbook.setNotesContent(request.getNotesContent());

        if (request.getHandbookSections() != null) {
            handbook.setSectionsConfig(JSON.toJSONString(request.getHandbookSections()));
        }
        if (request.getGrouping() != null) {
            handbook.setGroupingConfig(JSON.toJSONString(request.getGrouping()));
        }

        if (isNew) {
            handbookMapper.insert(handbook);
            log.info("[Handbook] 创建新手册配置, id={}", handbook.getId());
        } else {
            handbookMapper.updateById(handbook);
            log.info("[Handbook] 更新手册配置, id={}", handbook.getId());
        }

        // 保存讨论题目（手册独有数据，需要存储）
        if (request.getDiscussionTopics() != null) {
            // 先删除旧的讨论题目
            discussionMapper.delete(
                    new LambdaQueryWrapper<HandbookDiscussion>()
                            .eq(HandbookDiscussion::getHandbookId, handbook.getId())
            );
            // 插入新的讨论题目
            for (int i = 0; i < request.getDiscussionTopics().size(); i++) {
                HandbookSaveRequest.DiscussionTopic topic = request.getDiscussionTopics().get(i);
                if (topic.getContent() != null && !topic.getContent().isBlank()) {
                    HandbookDiscussion discussion = new HandbookDiscussion();
                    discussion.setHandbookId(handbook.getId());
                    discussion.setTenantId(tenantId);
                    discussion.setContent(topic.getContent());
                    discussion.setReferenceText(topic.getReference());
                    discussion.setSortOrder(i);
                    discussion.setCreateTime(LocalDateTime.now());
                    discussionMapper.insert(discussion);
                }
            }
            log.info("[Handbook] 保存 {} 个讨论题目", request.getDiscussionTopics().size());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", handbook.getId());
        result.put("meetingId", meetingId);
        result.put("status", handbook.getStatus());
        result.put("message", isNew ? "手册创建成功" : "手册更新成功");
        return result;
    }

    @Override
    public Map<String, Object> getHandbook(Long meetingId) {
        Long tenantId = currentTenantId();
        log.info("[Handbook] 获取手册配置, meetingId={}, tenantId={}", meetingId, tenantId);

        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 查手册配置
        Handbook handbook = handbookMapper.selectOne(
                new LambdaQueryWrapper<Handbook>()
                        .eq(Handbook::getMeetingId, meetingId)
                        .eq(Handbook::getTenantId, tenantId)
                        .eq(Handbook::getDeleted, 0)
        );

        if (handbook != null) {
            result.put("id", handbook.getId());
            result.put("title", handbook.getTitle());
            result.put("status", handbook.getStatus());
            result.put("coverConfig", parseJson(handbook.getCoverConfig()));
            result.put("tocConfig", parseJson(handbook.getTocConfig()));
            result.put("rosterConfig", parseJson(handbook.getRosterConfig()));
            result.put("rosterFields", parseJsonArray(handbook.getRosterFields()));
            result.put("seatingConfig", parseJson(handbook.getSeatingConfig()));
            result.put("transportConfig", parseJson(handbook.getTransportConfig()));
            result.put("hotelConfig", parseJson(handbook.getHotelConfig()));
            result.put("mealConfig", parseJson(handbook.getMealConfig()));
            result.put("discussionConfig", parseJson(handbook.getDiscussionConfig()));
            result.put("backcoverConfig", parseJson(handbook.getBackcoverConfig()));
            result.put("notesContent", handbook.getNotesContent());
            result.put("handbookSections", parseJsonArray(handbook.getSectionsConfig()));
            result.put("grouping", parseJson(handbook.getGroupingConfig()));
            result.put("pdfUrl", handbook.getPdfUrl());

            // 讨论题目（从 conf_handbook_discussion 获取）
            List<HandbookDiscussion> discussions = discussionMapper.selectList(
                    new LambdaQueryWrapper<HandbookDiscussion>()
                            .eq(HandbookDiscussion::getHandbookId, handbook.getId())
                            .orderByAsc(HandbookDiscussion::getSortOrder)
            );
            List<Map<String, String>> topicList = discussions.stream().map(d -> {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("content", d.getContent());
                m.put("reference", d.getReferenceText());
                return m;
            }).collect(Collectors.toList());
            result.put("discussionTopics", topicList);
        } else {
            result.put("id", null);
            result.put("status", null);
        }

        // 2. 日程数据 - 从 conf_schedule 获取
        result.put("scheduleItems", getScheduleData(meetingId).get("scheduleItems"));

        // 3. 资料/教案 - 从 conf_schedule_attachment 获取
        result.put("materials", getMaterialsData(meetingId).get("materials"));

        // 4. 学员名册 - 从 conf_registration 获取
        Map<String, Object> rosterData = getRosterData(meetingId);
        result.put("allMembers", rosterData.get("members"));
        result.put("memberCount", rosterData.get("total"));

        // 5. 分组数据 - 从 conf_group + conf_group_member 获取
        result.put("groups", getGroupingData(meetingId).get("groups"));

        // 6. 会议基本信息
        result.put("conference", getConferenceInfo(meetingId, tenantId));

        return result;
    }

    @Override
    public Map<String, Object> getRosterData(Long meetingId) {
        Long tenantId = currentTenantId();
        log.info("[Handbook] 获取名册数据, meetingId={}", meetingId);

        // 查询已审核通过的学员 (status=1)
        List<Registration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getConferenceId, meetingId)
                        .eq(Registration::getTenantId, tenantId)
                        .eq(Registration::getStatus, 1) // 已审核通过
                        .eq(Registration::getDeleted, 0)
                        .orderByAsc(Registration::getDepartment)
                        .orderByAsc(Registration::getRealName)
        );

        List<Map<String, Object>> members = registrations.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", String.valueOf(r.getId())); // 转字符串避免JS精度丢失
            m.put("name", r.getRealName());
            m.put("gender", mapGender(r.getGender()));
            m.put("unit", r.getDepartment());
            m.put("position", r.getPosition());
            m.put("phone", r.getPhone());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("members", members);
        result.put("total", members.size());
        return result;
    }

    @Override
    public Map<String, Object> getScheduleData(Long meetingId) {
        log.info("[Handbook] 获取日程数据, meetingId={}", meetingId);

        List<Map<String, Object>> scheduleItems = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, title, start_time, end_time, location, host, speaker " +
                    "FROM conf_schedule WHERE meeting_id = ? AND deleted = 0 ORDER BY start_time",
                    meetingId
            );
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new LinkedHashMap<>();
                Object startTime = row.get("start_time");
                Object endTime = row.get("end_time");

                // 从 datetime 中拆出 date 和 time
                String startStr = startTime != null ? startTime.toString() : "";
                String endStr = endTime != null ? endTime.toString() : "";

                if (startStr.length() >= 10) {
                    item.put("date", startStr.substring(0, 10));
                }
                if (startStr.length() >= 16) {
                    item.put("startTime", startStr.substring(11, 16));
                }
                if (endStr.length() >= 16) {
                    item.put("endTime", endStr.substring(11, 16));
                }
                item.put("title", row.get("title"));
                item.put("location", row.get("location"));
                item.put("host", row.get("host"));
                item.put("speaker", row.get("speaker"));
                scheduleItems.add(item);
            }
        } catch (Exception e) {
            log.warn("[Handbook] 查询日程数据失败: {}", e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scheduleItems", scheduleItems);
        result.put("total", scheduleItems.size());
        return result;
    }

    @Override
    public Map<String, Object> getMaterialsData(Long meetingId) {
        log.info("[Handbook] 获取教案/资料数据, meetingId={}", meetingId);

        List<Map<String, Object>> materials = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT a.id, a.file_name, a.file_type, a.file_size, a.file_url, a.description, " +
                    "s.title as schedule_title " +
                    "FROM conf_schedule_attachment a " +
                    "LEFT JOIN conf_schedule s ON a.schedule_id = s.id " +
                    "WHERE a.meeting_id = ? AND a.deleted = 0 ORDER BY a.id",
                    meetingId
            );
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", row.get("id"));
                item.put("name", row.get("file_name"));
                item.put("type", row.get("file_type"));
                item.put("size", row.get("file_size"));
                item.put("url", row.get("file_url"));
                item.put("description", row.get("description"));
                item.put("fromSchedule", row.get("schedule_title"));
                materials.add(item);
            }
        } catch (Exception e) {
            log.warn("[Handbook] 查询资料数据失败: {}", e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("materials", materials);
        result.put("total", materials.size());
        return result;
    }

    @Override
    public Map<String, Object> getGroupingData(Long meetingId) {
        Long tenantId = currentTenantId();
        log.info("[Handbook] 获取分组数据, meetingId={}", meetingId);

        List<Map<String, Object>> groups = new ArrayList<>();
        try {
            // 查询分组
            List<Map<String, Object>> groupRows = jdbcTemplate.queryForList(
                    "SELECT id, group_name, leader_id, current_members, sort " +
                    "FROM conf_group WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0 ORDER BY sort",
                    meetingId, tenantId
            );

            for (Map<String, Object> gRow : groupRows) {
                Map<String, Object> group = new LinkedHashMap<>();
                Long groupId = ((Number) gRow.get("id")).longValue();
                group.put("id", groupId);
                group.put("name", gRow.get("group_name"));
                group.put("currentMembers", gRow.get("current_members"));

                // 查询组长信息
                Long leaderId = gRow.get("leader_id") != null ? ((Number) gRow.get("leader_id")).longValue() : null;
                if (leaderId != null) {
                    try {
                        Map<String, Object> leader = jdbcTemplate.queryForMap(
                                "SELECT name, phone, organization FROM conf_registration WHERE id = ?", leaderId);
                        group.put("leader", leader.get("name"));
                    } catch (Exception ignored) {
                        group.put("leader", "");
                    }
                } else {
                    group.put("leader", "");
                }

                // 查询组员
                List<Map<String, Object>> memberRows = jdbcTemplate.queryForList(
                        "SELECT r.id, r.name, r.phone, r.organization, r.position, r.gender, gm.is_leader " +
                        "FROM conf_group_member gm " +
                        "JOIN conf_registration r ON gm.registration_id = r.id " +
                        "WHERE gm.group_id = ? ORDER BY gm.is_leader DESC, r.name",
                        groupId
                );
                List<Map<String, Object>> members = memberRows.stream().map(mr -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", String.valueOf(mr.get("id")));
                    m.put("name", mr.get("name"));
                    m.put("gender", mapGender(mr.get("gender")));
                    m.put("unit", mr.get("organization"));
                    m.put("position", mr.get("position"));
                    m.put("phone", mr.get("phone"));
                    m.put("isLeader", Objects.equals(mr.get("is_leader"), 1));
                    return m;
                }).collect(Collectors.toList());
                group.put("members", members);
                groups.add(group);
            }
        } catch (Exception e) {
            log.warn("[Handbook] 查询分组数据失败: {}", e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groups", groups);
        result.put("total", groups.size());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> generateHandbook(Long meetingId) {
        Long tenantId = currentTenantId();
        log.info("[Handbook] 生成手册, meetingId={}", meetingId);

        Handbook handbook = handbookMapper.selectOne(
                new LambdaQueryWrapper<Handbook>()
                        .eq(Handbook::getMeetingId, meetingId)
                        .eq(Handbook::getTenantId, tenantId)
                        .eq(Handbook::getDeleted, 0)
        );

        if (handbook == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("success", false);
            err.put("message", "请先保存手册配置");
            return err;
        }

        // 更新状态为已生成
        handbook.setStatus(1);
        handbook.setUpdateTime(LocalDateTime.now());
        handbookMapper.updateById(handbook);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("id", handbook.getId());
        result.put("status", 1);
        result.put("message", "手册生成成功");
        return result;
    }

    // ===== 工具方法 =====

    private Map<String, Object> getConferenceInfo(Long meetingId, Long tenantId) {
        Map<String, Object> info = new LinkedHashMap<>();
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT id, meeting_name, meeting_type, start_time, end_time, venue_name, venue_address, description " +
                    "FROM conf_meeting WHERE id = ? AND tenant_id = ? AND deleted = 0",
                    meetingId, tenantId
            );
            info.put("id", String.valueOf(row.get("id")));
            info.put("name", row.get("meeting_name"));
            info.put("type", row.get("meeting_type"));
            info.put("startTime", row.get("start_time"));
            info.put("endTime", row.get("end_time"));
            info.put("venue", row.get("venue_name"));
            info.put("address", row.get("venue_address"));
            info.put("description", row.get("description"));
        } catch (Exception e) {
            log.warn("[Handbook] 查询会议信息失败: {}", e.getMessage());
            info.put("id", String.valueOf(meetingId));
            info.put("name", "未知会议");
        }
        return info;
    }

    private Object parseJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return JSON.parse(json);
        } catch (Exception e) {
            return json;
        }
    }

    private Object parseJsonArray(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return JSON.parseArray(json);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String mapGender(Object gender) {
        if (gender == null) return "男";
        if (gender instanceof Number) {
            return ((Number) gender).intValue() == 2 ? "女" : "男";
        }
        return gender.toString();
    }

    private Long currentTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId == null ? DEFAULT_TENANT_ID : tenantId;
    }
}
