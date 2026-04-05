package com.conference.registration.controller;

import com.conference.common.result.Result;
import com.conference.registration.dto.GroupRequest;
import com.conference.registration.entity.RegistrationGroup;
import com.conference.registration.service.RegistrationGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分组管理 Controller
 * @author AI Executive
 * @date 2026-03-24
 */
@Slf4j
@RestController
@RequestMapping("/api/registration/groups")
@RequiredArgsConstructor
public class RegistrationGroupController {
    
    private final RegistrationGroupService groupService;
    
    /**
     * 获取会议的所有分组
     */
    @GetMapping
    public Result<List<RegistrationGroup>> getGroups(@RequestParam Long conferenceId) {
        List<RegistrationGroup> groups = groupService.getGroupsByConference(conferenceId);
        return Result.ok(groups);
    }
    
    /**
     * 获取分组详情（包含成员）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getGroupDetail(@PathVariable Long id) {
        Map<String, Object> detail = groupService.getGroupDetail(id);
        return Result.ok(detail);
    }
    
    /**
     * 创建分组
     */
    @PostMapping
    public Result<RegistrationGroup> createGroup(@RequestBody GroupRequest request) {
        RegistrationGroup group = groupService.createGroup(request);
        return Result.ok("分组创建成功", group);
    }
    
    /**
     * 更新分组
     */
    @PutMapping("/{id}")
    public Result<RegistrationGroup> updateGroup(
            @PathVariable Long id,
            @RequestBody GroupRequest request) {
        RegistrationGroup group = groupService.updateGroup(id, request);
        return Result.ok("分组更新成功", group);
    }
    
    /**
     * 删除分组
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return Result.ok("分组删除成功", null);
    }
    
    /**
     * 分配成员到分组
     */
    @PostMapping("/{id}/members")
    public Result<Void> assignMembers(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<Long> memberIds = (List<Long>) request.get("memberIds");
        
        groupService.assignMembers(id, memberIds);
        return Result.ok("成员分配成功", null);
    }
    
    /**
     * 移除分组成员
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public Result<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        groupService.removeMember(groupId, memberId);
        return Result.ok("成员移除成功", null);
    }
    
    /**
     * 指定工作人员
     */
    @PutMapping("/{id}/staff")
    public Result<Void> assignStaff(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<Long> staffIds = (List<Long>) request.get("staffIds");
        
        groupService.assignStaff(id, staffIds);
        return Result.ok("工作人员指定成功", null);
    }
    
    /**
     * 自动分组
     */
    @PostMapping("/auto-group")
    public Result<List<RegistrationGroup>> autoGroup(@RequestBody Map<String, Object> request) {
        Long conferenceId = Long.valueOf(request.get("conferenceId").toString());
        String criteria = (String) request.get("criteria"); // department, position, etc.
        
        List<RegistrationGroup> groups = groupService.autoGroup(conferenceId, criteria);
        return Result.ok("自动分组完成", groups);
    }
}
