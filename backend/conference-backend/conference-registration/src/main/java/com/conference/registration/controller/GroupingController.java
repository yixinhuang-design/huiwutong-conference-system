package com.conference.registration.controller;

import com.conference.common.result.Result;
import com.conference.common.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能分组管理 Controller
 * 
 * 数据来源：
 *   - 分组表: conf_group (带 tenant_id, meeting_id, deleted 字段)
 *   - 组员表: conf_group_member (带 group_id → conf_group.id, registration_id → conf_registration.id)
 *   - 报名表: conf_registration (获取姓名、单位等信息)
 * 
 * 端口: 8082 (conference-registration 服务)
 */
@Slf4j
@RestController
@RequestMapping("/api/grouping")
public class GroupingController {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取当前租户ID
     */
    private Long currentTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }

    // ===================== 查询 =====================

    /**
     * 获取会议的所有分组数据（含组员详情）
     * GET /api/registration/group/list?meetingId=xxx
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> listGroups(@RequestParam Long meetingId) {
        Long tenantId = currentTenantId();
        log.info("[Grouping] 查询分组列表, meetingId={}, tenantId={}", meetingId, tenantId);

        List<Map<String, Object>> groups = new ArrayList<>();
        try {
            List<Map<String, Object>> groupRows = jdbcTemplate.queryForList(
                    "SELECT id, group_name, group_type, max_members, current_members, leader_id, sort, remark " +
                    "FROM conf_group WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0 ORDER BY sort",
                    meetingId, tenantId
            );

            for (Map<String, Object> gRow : groupRows) {
                Map<String, Object> group = new LinkedHashMap<>();
                Long groupId = ((Number) gRow.get("id")).longValue();
                group.put("id", String.valueOf(groupId));
                group.put("name", gRow.get("group_name"));
                group.put("groupType", gRow.get("group_type"));
                group.put("maxMembers", gRow.get("max_members"));
                group.put("currentMembers", gRow.get("current_members"));
                group.put("sort", gRow.get("sort"));
                group.put("remark", gRow.get("remark"));

                // 查询组长姓名
                Long leaderId = gRow.get("leader_id") != null ? ((Number) gRow.get("leader_id")).longValue() : null;
                group.put("leaderId", leaderId != null ? String.valueOf(leaderId) : "");
                if (leaderId != null) {
                    try {
                        Map<String, Object> leader = jdbcTemplate.queryForMap(
                                "SELECT name, organization FROM conf_registration WHERE id = ?", leaderId);
                        group.put("leader", leader.get("name"));
                        group.put("leaderUnit", leader.get("organization"));
                    } catch (Exception e) {
                        group.put("leader", "");
                        group.put("leaderUnit", "");
                    }
                } else {
                    group.put("leader", "");
                    group.put("leaderUnit", "");
                }

                // 查询组员列表
                List<Map<String, Object>> memberRows = jdbcTemplate.queryForList(
                        "SELECT r.id, r.name, r.organization, r.position, r.phone, r.gender, gm.is_leader " +
                        "FROM conf_group_member gm " +
                        "JOIN conf_registration r ON gm.registration_id = r.id " +
                        "WHERE gm.group_id = ? AND gm.deleted = 0 " +
                        "ORDER BY gm.is_leader DESC, r.name",
                        groupId
                );
                List<Map<String, Object>> members = memberRows.stream().map(mr -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", String.valueOf(mr.get("id")));
                    m.put("name", mr.get("name"));
                    m.put("unit", mr.get("organization") != null ? mr.get("organization").toString() : "");
                    m.put("department", mr.get("organization") != null ? mr.get("organization").toString() : "");
                    m.put("position", mr.get("position") != null ? mr.get("position").toString() : "");
                    m.put("phone", mr.get("phone") != null ? mr.get("phone").toString() : "");
                    m.put("isLeader", Objects.equals(mr.get("is_leader"), 1) || Objects.equals(mr.get("is_leader"), true));
                    return m;
                }).collect(Collectors.toList());

                group.put("memberCount", members.size());
                group.put("members", members);
                groups.add(group);
            }
        } catch (Exception e) {
            log.error("[Grouping] 查询分组数据失败: {}", e.getMessage(), e);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groups", groups);
        result.put("total", groups.size());
        return Result.success(result);
    }

    // ===================== 执行智能分组（保存到数据库） =====================

    /**
     * 执行智能分组 - 接收前端计算好的分组结果，持久化到数据库
     * POST /api/registration/group/save
     * 
     * 请求体格式:
     * {
     *   "meetingId": 123,
     *   "criteria": "department",
     *   "groupSize": 25,
     *   "groups": [
     *     {
     *       "name": "第1学习小组",
     *       "leader": "",
     *       "staff": [],
     *       "members": [
     *         { "id": "123", "name": "张三", "unit": "市教育局", "department": "办公室", "position": "科长" }
     *       ]
     *     }
     *   ]
     * }
     */
    @PostMapping("/save")
    @Transactional
    public Result<Map<String, Object>> saveGroups(@RequestBody Map<String, Object> request) {
        Long tenantId = currentTenantId();
        Long meetingId = Long.valueOf(request.get("meetingId").toString());
        String criteria = request.get("criteria") != null ? request.get("criteria").toString() : "mixed";
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> groups = (List<Map<String, Object>>) request.get("groups");

        log.info("[Grouping] 保存分组, meetingId={}, tenantId={}, 组数={}", meetingId, tenantId, groups != null ? groups.size() : 0);

        if (groups == null || groups.isEmpty()) {
            return Result.error("分组数据不能为空");
        }

        try {
            // 1. 软删除该会议的旧分组及成员
            jdbcTemplate.update(
                    "UPDATE conf_group SET deleted = 1, update_time = NOW() WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0",
                    meetingId, tenantId
            );
            // 软删除旧成员（通过 group_id 关联）
            jdbcTemplate.update(
                    "UPDATE conf_group_member gm " +
                    "JOIN conf_group g ON gm.group_id = g.id " +
                    "SET gm.deleted = 1 " +
                    "WHERE g.meeting_id = ? AND g.tenant_id = ?",
                    meetingId, tenantId
            );

            // 2. 插入新分组
            List<Map<String, Object>> savedGroups = new ArrayList<>();
            for (int i = 0; i < groups.size(); i++) {
                Map<String, Object> groupData = groups.get(i);
                String groupName = groupData.get("name") != null ? groupData.get("name").toString() : "第" + (i + 1) + "学习小组";

                // 查找组长的 registration_id
                Long leaderId = null;
                String leaderName = groupData.get("leader") != null ? groupData.get("leader").toString() : "";
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> members = (List<Map<String, Object>>) groupData.get("members");
                int memberCount = members != null ? members.size() : 0;

                // 如果指定了组长名字，找到对应的 registration_id
                if (!leaderName.isEmpty() && members != null) {
                    for (Map<String, Object> member : members) {
                        if (leaderName.equals(member.get("name"))) {
                            try {
                                leaderId = Long.valueOf(member.get("id").toString());
                            } catch (Exception ignored) {}
                            break;
                        }
                    }
                }

                // 插入分组记录
                jdbcTemplate.update(
                        "INSERT INTO conf_group (tenant_id, meeting_id, group_name, group_type, max_members, current_members, leader_id, sort, remark, create_time, update_time, deleted) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), 0)",
                        tenantId, meetingId, groupName, criteria,
                        memberCount > 0 ? memberCount + 5 : 30, // max_members 留余量
                        memberCount,
                        leaderId,
                        i + 1, // sort
                        ""
                );

                // 获取刚插入的 group id
                Long groupId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

                // 插入组员
                if (members != null) {
                    for (Map<String, Object> member : members) {
                        Long registrationId;
                        try {
                            registrationId = Long.valueOf(member.get("id").toString());
                        } catch (Exception e) {
                            log.warn("[Grouping] 无效的成员ID: {}", member.get("id"));
                            continue;
                        }
                        int isLeader = (leaderId != null && leaderId.equals(registrationId)) ? 1 : 0;

                        jdbcTemplate.update(
                                "INSERT INTO conf_group_member (tenant_id, group_id, registration_id, is_leader, join_time, deleted) " +
                                "VALUES (?, ?, ?, ?, NOW(), 0)",
                                tenantId, groupId, registrationId, isLeader
                        );
                    }
                }

                Map<String, Object> savedGroup = new LinkedHashMap<>();
                savedGroup.put("id", String.valueOf(groupId));
                savedGroup.put("name", groupName);
                savedGroup.put("memberCount", memberCount);
                savedGroup.put("leader", leaderName);
                savedGroups.add(savedGroup);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("savedGroups", savedGroups);
            result.put("totalGroups", savedGroups.size());
            return Result.success(result);

        } catch (Exception e) {
            log.error("[Grouping] 保存分组失败: {}", e.getMessage(), e);
            return Result.error("保存分组失败: " + e.getMessage());
        }
    }

    // ===================== 更新单个组信息 =====================

    /**
     * 更新组名
     * PUT /api/registration/group/updateName?groupId=xxx&name=xxx
     */
    @PutMapping("/updateName")
    public Result<String> updateGroupName(@RequestParam Long groupId, @RequestParam String name) {
        Long tenantId = currentTenantId();
        log.info("[Grouping] 更新组名, groupId={}, name={}", groupId, name);
        try {
            int rows = jdbcTemplate.update(
                    "UPDATE conf_group SET group_name = ?, update_time = NOW() WHERE id = ? AND tenant_id = ? AND deleted = 0",
                    name, groupId, tenantId
            );
            if (rows > 0) {
                return Result.success("组名更新成功");
            }
            return Result.error("未找到该分组");
        } catch (Exception e) {
            log.error("[Grouping] 更新组名失败: {}", e.getMessage());
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新组长
     * PUT /api/registration/group/updateLeader?groupId=xxx&leaderId=xxx
     */
    @PutMapping("/updateLeader")
    public Result<String> updateGroupLeader(@RequestParam Long groupId, @RequestParam(required = false) Long leaderId) {
        Long tenantId = currentTenantId();
        log.info("[Grouping] 更新组长, groupId={}, leaderId={}", groupId, leaderId);
        try {
            // 先取消旧的组长标记
            jdbcTemplate.update(
                    "UPDATE conf_group_member SET is_leader = 0 WHERE group_id = ? AND is_leader = 1 AND deleted = 0",
                    groupId
            );
            // 更新 conf_group 的 leader_id
            jdbcTemplate.update(
                    "UPDATE conf_group SET leader_id = ?, update_time = NOW() WHERE id = ? AND tenant_id = ? AND deleted = 0",
                    leaderId, groupId, tenantId
            );
            // 设置新组长标记
            if (leaderId != null) {
                jdbcTemplate.update(
                        "UPDATE conf_group_member SET is_leader = 1 WHERE group_id = ? AND registration_id = ? AND deleted = 0",
                        groupId, leaderId
                );
            }
            return Result.success("组长更新成功");
        } catch (Exception e) {
            log.error("[Grouping] 更新组长失败: {}", e.getMessage());
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    // ===================== 成员调整 =====================

    /**
     * 调整组成员 - 接收完整的组-成员关系
     * POST /api/registration/group/adjustMembers
     * 
     * 请求体格式:
     * {
     *   "meetingId": 123,
     *   "groups": [
     *     { "id": "1", "name": "第1组", "members": [{"id":"101"}, {"id":"102"}] },
     *     { "id": "2", "name": "第2组", "members": [{"id":"103"}] }
     *   ]
     * }
     */
    @PostMapping("/adjustMembers")
    @Transactional
    public Result<String> adjustMembers(@RequestBody Map<String, Object> request) {
        Long tenantId = currentTenantId();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> groups = (List<Map<String, Object>>) request.get("groups");

        log.info("[Grouping] 调整成员, 组数={}", groups != null ? groups.size() : 0);

        if (groups == null || groups.isEmpty()) {
            return Result.error("分组数据不能为空");
        }

        try {
            for (Map<String, Object> groupData : groups) {
                Long groupId = Long.valueOf(groupData.get("id").toString());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> members = (List<Map<String, Object>>) groupData.get("members");

                // 删除该组的旧成员
                jdbcTemplate.update(
                        "DELETE FROM conf_group_member WHERE group_id = ?",
                        groupId
                );

                // 插入新成员
                if (members != null) {
                    for (Map<String, Object> member : members) {
                        Long registrationId = Long.valueOf(member.get("id").toString());
                        // 检查是否为组长
                        int isLeader = 0;
                        try {
                            Map<String, Object> groupRow = jdbcTemplate.queryForMap(
                                    "SELECT leader_id FROM conf_group WHERE id = ? AND deleted = 0", groupId);
                            Long existingLeaderId = groupRow.get("leader_id") != null ? ((Number) groupRow.get("leader_id")).longValue() : null;
                            if (existingLeaderId != null && existingLeaderId.equals(registrationId)) {
                                isLeader = 1;
                            }
                        } catch (Exception ignored) {}

                        jdbcTemplate.update(
                                "INSERT INTO conf_group_member (tenant_id, group_id, registration_id, is_leader, join_time, deleted) " +
                                "VALUES (?, ?, ?, ?, NOW(), 0)",
                                tenantId, groupId, registrationId, isLeader
                        );
                    }

                    // 更新 current_members 计数
                    jdbcTemplate.update(
                            "UPDATE conf_group SET current_members = ?, update_time = NOW() WHERE id = ? AND tenant_id = ? AND deleted = 0",
                            members.size(), groupId, tenantId
                    );
                }
            }

            return Result.success("成员调整保存成功");
        } catch (Exception e) {
            log.error("[Grouping] 调整成员失败: {}", e.getMessage(), e);
            return Result.error("调整成员失败: " + e.getMessage());
        }
    }

    /**
     * 从组中移除成员
     * DELETE /api/registration/group/removeMember?groupId=xxx&memberId=xxx
     */
    @DeleteMapping("/removeMember")
    @Transactional
    public Result<String> removeMember(@RequestParam Long groupId, @RequestParam Long memberId) {
        Long tenantId = currentTenantId();
        log.info("[Grouping] 移除成员, groupId={}, memberId={}", groupId, memberId);
        try {
            jdbcTemplate.update(
                    "DELETE FROM conf_group_member WHERE group_id = ? AND registration_id = ?",
                    groupId, memberId
            );
            // 更新 current_members 计数
            int count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM conf_group_member WHERE group_id = ? AND deleted = 0",
                    Integer.class, groupId
            );
            jdbcTemplate.update(
                    "UPDATE conf_group SET current_members = ?, update_time = NOW() WHERE id = ? AND tenant_id = ? AND deleted = 0",
                    count, groupId, tenantId
            );
            return Result.success("成员已移除");
        } catch (Exception e) {
            log.error("[Grouping] 移除成员失败: {}", e.getMessage());
            return Result.error("移除失败: " + e.getMessage());
        }
    }

    /**
     * 删除所有分组
     * DELETE /api/registration/group/deleteAll?meetingId=xxx
     */
    @DeleteMapping("/deleteAll")
    @Transactional
    public Result<String> deleteAllGroups(@RequestParam Long meetingId) {
        Long tenantId = currentTenantId();
        log.info("[Grouping] 删除所有分组, meetingId={}", meetingId);
        try {
            // 先删除成员
            jdbcTemplate.update(
                    "UPDATE conf_group_member gm " +
                    "JOIN conf_group g ON gm.group_id = g.id " +
                    "SET gm.deleted = 1 " +
                    "WHERE g.meeting_id = ? AND g.tenant_id = ?",
                    meetingId, tenantId
            );
            // 再删除分组
            jdbcTemplate.update(
                    "UPDATE conf_group SET deleted = 1, update_time = NOW() WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0",
                    meetingId, tenantId
            );
            return Result.success("已删除所有分组");
        } catch (Exception e) {
            log.error("[Grouping] 删除分组失败: {}", e.getMessage());
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
