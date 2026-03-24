package com.conference.collaboration.controller;

import com.conference.collaboration.entity.ChatGroup;
import com.conference.collaboration.entity.ChatGroupMember;
import com.conference.collaboration.service.ChatGroupService;
import com.conference.collaboration.service.ChatMessageService;
import com.conference.collaboration.websocket.ChatWebSocketHandler;
import com.conference.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 群组管理 Controller
 * 支持群组CRUD、成员管理、群设置、在线状态等完整功能
 */
@Slf4j
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final ChatGroupService groupService;
    private final ChatMessageService messageService;
    private final ChatWebSocketHandler webSocketHandler;

    // ==================== 群组CRUD ====================

    /** 创建群组 */
    @PostMapping("/create")
    public Result<ChatGroup> createGroup(@RequestBody ChatGroup group) {
        ChatGroup created = groupService.createGroup(group);
        return Result.ok("群组创建成功", created);
    }

    /** 获取会议下所有群组 */
    @GetMapping("/list")
    public Result<List<ChatGroup>> listGroups(
            @RequestParam(required = false) Long conferenceId) {
        List<ChatGroup> groups = groupService.listByConference(conferenceId);
        return Result.ok(groups);
    }

    /** 获取我加入的群组 */
    @GetMapping("/my-groups")
    public Result<List<Map<String, Object>>> getMyGroups(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long conferenceId) {
        // 如果未传userId使用默认
        Long uid = userId != null ? userId : 1001L;
        List<Map<String, Object>> groups = groupService.listMyGroups(uid, conferenceId);
        return Result.ok(groups);
    }

    /** 获取群组详情(含成员) */
    @GetMapping("/{groupId}")
    public Result<Map<String, Object>> getGroupDetail(@PathVariable Long groupId) {
        Map<String, Object> detail = groupService.getGroupDetail(groupId);
        if (detail == null) {
            return Result.fail("群组不存在");
        }
        // 附加在线信息
        Set<String> onlineUsers = webSocketHandler.getGroupOnlineUsers(groupId.toString());
        detail.put("onlineCount", onlineUsers.size());
        detail.put("onlineUsers", onlineUsers);
        return Result.ok(detail);
    }

    // ==================== 成员管理 ====================

    /** 获取群成员列表 */
    @GetMapping("/{groupId}/members")
    public Result<List<ChatGroupMember>> getMembers(@PathVariable Long groupId) {
        List<ChatGroupMember> members = groupService.getMembers(groupId);
        return Result.ok(members);
    }

    /** 添加群成员 */
    @PostMapping("/{groupId}/members")
    public Result<String> addMember(@PathVariable Long groupId, @RequestBody ChatGroupMember member) {
        groupService.addMember(groupId, member);
        return Result.ok("成员添加成功");
    }

    /** 批量添加群成员 */
    @PostMapping("/{groupId}/members/batch")
    public Result<String> batchAddMembers(@PathVariable Long groupId, @RequestBody List<ChatGroupMember> members) {
        for (ChatGroupMember member : members) {
            groupService.addMember(groupId, member);
        }
        return Result.ok("批量添加成功，共添加" + members.size() + "人");
    }

    /** 移除群成员 */
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<String> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return Result.ok("成员已移除");
    }

    /** 更新成员角色 */
    @PutMapping("/{groupId}/members/{userId}/role")
    public Result<String> updateMemberRole(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || role.isEmpty()) {
            return Result.fail("角色不能为空");
        }
        groupService.updateMemberRole(groupId, userId, role);
        return Result.ok("成员角色已更新");
    }

    // ==================== 群设置 ====================

    /** 删除群组 */
    @DeleteMapping("/{groupId}")
    public Result<String> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return Result.ok("群组已删除");
    }

    /** 更新群设置 */
    @PutMapping("/{groupId}/settings")
    public Result<String> updateSettings(@PathVariable Long groupId, @RequestBody Map<String, Object> settings) {
        groupService.updateSettings(groupId, settings);
        return Result.ok("群设置已更新");
    }

    // ==================== 统计 ====================

    /** 群组统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestParam(required = false) Long conferenceId) {
        try {
            Map<String, Object> stats = groupService.getStats(conferenceId);
            stats.put("onlineTotal", webSocketHandler.getOnlineCount());

            // 获取该会议下所有群组的今日消息统计
            List<ChatGroup> groups = groupService.listByConference(conferenceId);
            List<Long> groupIds = groups.stream().map(ChatGroup::getId).collect(Collectors.toList());
            if (!groupIds.isEmpty()) {
                long todayTotal = messageService.getTodayMessageCount(groupIds);
                Map<Long, Long> todayByGroup = messageService.getTodayMessageCountByGroup(groupIds);
                stats.put("todayMessages", todayTotal);
                // 转为String key以便JSON序列化
                Map<String, Long> todayByGroupStr = new java.util.LinkedHashMap<>();
                todayByGroup.forEach((k, v) -> todayByGroupStr.put(k.toString(), v));
                stats.put("todayMessagesByGroup", todayByGroupStr);
            } else {
                stats.put("todayMessages", 0L);
                stats.put("todayMessagesByGroup", new java.util.LinkedHashMap<>());
            }
            return Result.ok(stats);
        } catch (Exception e) {
            log.error("获取群组统计失败", e);
            return Result.fail("获取群组统计失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
