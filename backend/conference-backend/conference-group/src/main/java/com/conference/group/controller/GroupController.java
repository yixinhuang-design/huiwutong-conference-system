package com.conference.group.controller;

import com.conference.common.result.Result;
import com.conference.group.dto.GroupCreateRequest;
import com.conference.group.dto.GroupUpdateRequest;
import com.conference.group.entity.ConferenceGroup;
import com.conference.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 群组管理 Controller
 * @author AI Executive
 * @date 2026-03-24
 */
@Slf4j
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {
    
    private final GroupService groupService;
    
    /**
     * 创建群组
     */
    @PostMapping("/create")
    public Result<ConferenceGroup> createGroup(@RequestBody GroupCreateRequest request) {
        ConferenceGroup group = groupService.createGroup(request);
        return Result.ok("群组创建成功", group);
    }
    
    /**
     * 更新群组
     */
    @PutMapping("/{webKey}")
    public Result<ConferenceGroup> updateGroup(
            @PathVariable String webKey,
            @RequestBody GroupUpdateRequest request) {
        ConferenceGroup group = groupService.updateGroup(webKey, request);
        return Result.ok("群组更新成功", group);
    }
    
    /**
     * 删除群组
     */
    @DeleteMapping("/{webKey}")
    public Result<Void> deleteGroup(@PathVariable String webKey) {
        groupService.deleteGroup(webKey);
        return Result.ok("群组删除成功", null);
    }
    
    /**
     * 获取群组详情
     */
    @GetMapping("/{webKey}")
    public Result<Map<String, Object>> getGroupDetail(@PathVariable String webKey) {
        Map<String, Object> detail = groupService.getGroupDetail(webKey);
        return Result.ok(detail);
    }
    
    /**
     * 获取会议的所有群组
     */
    @GetMapping("/list")
    public Result<List<ConferenceGroup>> getGroups(@RequestParam Long conferenceId) {
        List<ConferenceGroup> groups = groupService.getGroupsByConference(conferenceId);
        return Result.ok(groups);
    }
    
    /**
     * 添加成员
     */
    @PostMapping("/{webKey}/members")
    public Result<Void> addMembers(
            @PathVariable String webKey,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> memberIds = (List<Long>) request.get("memberIds");
        
        groupService.addMembers(webKey, memberIds);
        return Result.ok("成员添加成功", null);
    }
    
    /**
     * 移除成员
     */
    @DeleteMapping("/{webKey}/members/{memberWebKey}")
    public Result<Void> removeMember(
            @PathVariable String webKey,
            @PathVariable String memberWebKey) {
        groupService.removeMember(webKey, memberWebKey);
        return Result.ok("成员移除成功", null);
    }
    
    /**
     * 设置管理员
     */
    @PutMapping("/{webKey}/admins")
    public Result<Void> setAdmins(
            @PathVariable String webKey,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> adminWebKeys = (List<String>) request.get("adminWebKeys");
        
        groupService.setAdmins(webKey, adminWebKeys);
        return Result.ok("管理员设置成功", null);
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/{webKey}/messages")
    public Result<Map<String, Object>> sendMessage(
            @PathVariable String webKey,
            @RequestBody Map<String, Object> request) {
        
        String content = (String) request.get("content");
        String messageType = (String) request.getOrDefault("messageType", "text");
        
        Map<String, Object> result = groupService.sendMessage(webKey, content, messageType);
        return Result.ok("消息发送成功", result);
    }
    
    /**
     * 获取消息列表
     */
    @GetMapping("/{webKey}/messages")
    public Result<List<com.conference.group.entity.GroupMessage>> getMessages(
            @PathVariable String webKey,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        List<com.conference.group.entity.GroupMessage> messages = 
            groupService.getMessages(webKey, page, pageSize);
        return Result.ok(messages);
    }
    
    /**
     * 创建任务
     */
    @PostMapping("/{webKey}/tasks")
    public Result<com.conference.group.entity.GroupTask> createTask(
            @PathVariable String webKey,
            @RequestBody com.conference.group.dto.GroupTaskRequest request) {
        com.conference.group.entity.GroupTask task = groupService.createTask(webKey, request);
        return Result.ok("任务创建成功", task);
    }
    
    /**
     * 更新任务状态
     */
    @PutMapping("/{webKey}/tasks/{taskWebKey}")
    public Result<com.conference.group.entity.GroupTask> updateTaskStatus(
            @PathVariable String webKey,
            @PathVariable String taskWebKey,
            @RequestBody Map<String, Object> request) {
        
        String status = (String) request.get("status");
        Integer progress = (Integer) request.get("progress");
        Integer completedCount = (Integer) request.get("completedCount");
        
        com.conference.group.entity.GroupTask task = groupService.updateTaskStatus(
            webKey, taskWebKey, status, progress, completedCount);
        
        return Result.ok("任务更新成功", task);
    }
    
    /**
     * 获取任务列表
     */
    @GetMapping("/{webKey}/tasks")
    public Result<List<com.conference.group.entity.GroupTask>> getTasks(@PathVariable String webKey) {
        List<com.conference.group.entity.GroupTask> tasks = groupService.getTasksByGroup(webKey);
        return Result.ok(tasks);
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/{webKey}/stats")
    public Result<Map<String, Object>> getStats(@PathVariable String webKey) {
        Map<String, Object> stats = groupService.getGroupStats(webKey);
        return Result.ok(stats);
    }
}
