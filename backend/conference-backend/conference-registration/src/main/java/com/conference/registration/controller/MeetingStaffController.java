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
 * 会议工作人员管理 Controller
 * 
 * 数据来源：conf_meeting_staff 表
 * 端口: 8082 (conference-registration 服务)
 * 路由: /api/registration/staff/**
 */
@Slf4j
@RestController
@RequestMapping("/api/registration/staff")
public class MeetingStaffController {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long currentTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
    }

    /**
     * 批量保存会议工作人员
     * POST /api/registration/staff/save
     * 
     * 请求体:
     * {
     *   "meetingId": 123,
     *   "staffList": [
     *     { "staffId": 1001, "staffName": "张三", "staffPhone": "13800138000", "role": 0, "permissions": {...} }
     *   ]
     * }
     */
    @PostMapping("/save")
    @Transactional
    public Result<Map<String, Object>> saveStaff(@RequestBody Map<String, Object> request) {
        Long tenantId = currentTenantId();
        Long meetingId = Long.valueOf(request.get("meetingId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> staffList = (List<Map<String, Object>>) request.get("staffList");

        log.info("[MeetingStaff] 保存工作人员, meetingId={}, tenantId={}, 人数={}", 
                meetingId, tenantId, staffList != null ? staffList.size() : 0);

        try {
            // 第一步：先将该会议所有现有工作人员标记为删除（全量替换策略）
            int deletedCount = jdbcTemplate.update(
                "UPDATE conf_meeting_staff SET deleted = 1, update_time = NOW() " +
                "WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0",
                meetingId, tenantId
            );
            log.info("[MeetingStaff] 已软删除 {} 条旧记录, meetingId={}", deletedCount, meetingId);

            // 如果新列表为空，说明用户移除了所有工作人员，直接返回成功
            if (staffList == null || staffList.isEmpty()) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("success", true);
                result.put("addedCount", 0);
                result.put("removedCount", deletedCount);
                result.put("meetingId", meetingId);
                return Result.success("工作人员已全部移除", result);
            }

            // 第二步：重新插入/恢复工作人员列表
            int addedCount = 0;
            for (Map<String, Object> staff : staffList) {
                Long staffId = staff.get("staffId") != null ? Long.valueOf(staff.get("staffId").toString()) : null;
                String staffName = staff.get("staffName") != null ? staff.get("staffName").toString() : "";
                String staffPhone = staff.get("staffPhone") != null ? staff.get("staffPhone").toString() : "";
                Integer role = staff.get("role") != null ? Integer.valueOf(staff.get("role").toString()) : 0;
                String permissions = staff.get("permissions") != null ? 
                        com.alibaba.fastjson2.JSON.toJSONString(staff.get("permissions")) : null;

                if (staffId == null) {
                    // 如果没有staffId，用手机号哈希生成一个
                    staffId = (long) Math.abs(staffPhone.hashCode()) + 100000L;
                }

                // 使用 INSERT ... ON DUPLICATE KEY UPDATE 避免重复
                try {
                    jdbcTemplate.update(
                        "INSERT INTO conf_meeting_staff (meeting_id, staff_id, staff_name, staff_phone, role, permissions, tenant_id, deleted, create_time, update_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE staff_name=VALUES(staff_name), staff_phone=VALUES(staff_phone), role=VALUES(role), permissions=VALUES(permissions), deleted=0, update_time=NOW()",
                        meetingId, staffId, staffName, staffPhone, role, permissions, tenantId
                    );
                    addedCount++;
                } catch (Exception e) {
                    log.warn("[MeetingStaff] 保存工作人员失败: staffId={}, error={}", staffId, e.getMessage());
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("addedCount", addedCount);
            result.put("meetingId", meetingId);
            return Result.success("工作人员保存成功", result);
        } catch (Exception e) {
            log.error("[MeetingStaff] 保存工作人员异常: {}", e.getMessage(), e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取会议的所有工作人员
     * GET /api/registration/staff/list?meetingId=xxx
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listStaff(@RequestParam Long meetingId) {
        Long tenantId = currentTenantId();
        log.info("[MeetingStaff] 查询工作人员, meetingId={}, tenantId={}", meetingId, tenantId);

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, staff_id, staff_name, staff_phone, role, permissions, create_time " +
                "FROM conf_meeting_staff WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0 " +
                "ORDER BY role DESC, create_time ASC",
                meetingId, tenantId
            );

            List<Map<String, Object>> staffList = rows.stream().map(row -> {
                Map<String, Object> staff = new LinkedHashMap<>();
                staff.put("id", row.get("id"));
                staff.put("staffId", row.get("staff_id"));
                staff.put("staffName", row.get("staff_name"));
                staff.put("staffPhone", row.get("staff_phone"));
                staff.put("role", row.get("role"));
                // 将 permissions JSON 字符串解析为对象，前端可直接使用
                Object permRaw = row.get("permissions");
                if (permRaw instanceof String && ((String) permRaw).startsWith("{")) {
                    try {
                        staff.put("permissions", com.alibaba.fastjson2.JSON.parseObject((String) permRaw));
                    } catch (Exception e) {
                        staff.put("permissions", permRaw);
                    }
                } else {
                    staff.put("permissions", permRaw);
                }
                staff.put("createTime", row.get("create_time"));
                return staff;
            }).collect(Collectors.toList());

            return Result.success(staffList);
        } catch (Exception e) {
            log.error("[MeetingStaff] 查询工作人员失败: {}", e.getMessage(), e);
            return Result.success(new ArrayList<>());
        }
    }

    /**
     * 删除工作人员
     * DELETE /api/registration/staff/remove?meetingId=xxx&staffId=xxx
     */
    @DeleteMapping("/remove")
    public Result<String> removeStaff(@RequestParam Long meetingId, @RequestParam Long staffId) {
        Long tenantId = currentTenantId();
        log.info("[MeetingStaff] 删除工作人员, meetingId={}, staffId={}", meetingId, staffId);

        try {
            int rows = jdbcTemplate.update(
                "UPDATE conf_meeting_staff SET deleted = 1, update_time = NOW() " +
                "WHERE meeting_id = ? AND staff_id = ? AND tenant_id = ? AND deleted = 0",
                meetingId, staffId, tenantId
            );
            if (rows > 0) {
                return Result.success("工作人员已移除");
            }
            return Result.error("未找到该工作人员");
        } catch (Exception e) {
            log.error("[MeetingStaff] 删除工作人员失败: {}", e.getMessage());
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
