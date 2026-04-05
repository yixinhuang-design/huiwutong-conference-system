package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.mapper.TaskAssigneeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 任务执行人服务
 * 负责执行人相关信息查询
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAssigneeService {

    private final TaskAssigneeMapper assigneeMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 批量获取执行人联系方式
     * @param assignees 执行人列表
     * @return 联系方式列表，每个Map包含phone、clientId等信息
     */
    public List<Map<String, String>> getAssigneeContacts(List<TaskAssignee> assignees) {
        List<Map<String, String>> contacts = new ArrayList<>();

        for (TaskAssignee assignee : assignees) {
            Map<String, String> contact = getUserContact(assignee.getUserId());
            if (!contact.isEmpty()) {
                contacts.add(contact);
            }
        }

        return contacts;
    }

    /**
     * 获取单个用户联系方式
     * @param userId 用户ID
     * @return 联系方式Map (phone, clientId)
     */
    public Map<String, String> getUserContact(Long userId) {
        Map<String, String> contact = new HashMap<>();

        try {
            // 从用户表查询手机号和ClientId
            String sql = """
                SELECT phone, client_id 
                FROM sys_user 
                WHERE id = ? AND deleted = 0
                """;

            jdbcTemplate.query(sql, rs -> {
                String phone = rs.getString("phone");
                String clientId = rs.getString("client_id");

                if (phone != null && !phone.isEmpty()) {
                    contact.put("phone", phone);
                }
                if (clientId != null && !clientId.isEmpty()) {
                    contact.put("clientId", clientId);
                }
            }, userId);

        } catch (Exception e) {
            log.error("获取用户联系方式失败: userId={}, 错误={}", userId, e.getMessage());
        }

        return contact;
    }

    /**
     * 获取执行人详细信息（包含姓名、角色等）
     * @param assigneeIds 执行人ID列表
     * @return 用户信息列表
     */
    public List<Map<String, Object>> getAssigneeDetails(List<Long> assigneeIds) {
        List<Map<String, Object>> details = new ArrayList<>();

        if (assigneeIds == null || assigneeIds.isEmpty()) {
            return details;
        }

        try {
            String sql = "SELECT id, name, phone, role, unit, client_id "
                + "FROM sys_user "
                + "WHERE id IN (" + String.join(",", Collections.nCopies(assigneeIds.size(), "?")) + ") "
                + "AND deleted = 0";

            List<Object> params = new ArrayList<>(assigneeIds);

            jdbcTemplate.query(sql, rs -> {
                Map<String, Object> detail = new HashMap<>();
                detail.put("id", rs.getLong("id"));
                detail.put("name", rs.getString("name"));
                detail.put("phone", rs.getString("phone"));
                detail.put("role", rs.getString("role"));
                detail.put("unit", rs.getString("unit"));
                detail.put("clientId", rs.getString("client_id"));
                details.add(detail);
            }, params.toArray());

        } catch (Exception e) {
            log.error("获取执行人详细信息失败: 错误={}", e.getMessage());
        }

        return details;
    }
}
