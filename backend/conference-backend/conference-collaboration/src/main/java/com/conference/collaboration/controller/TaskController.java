package com.conference.collaboration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskLog;
import com.conference.collaboration.service.TaskService;
import com.conference.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务分派 Controller
 * 支持任务CRUD、执行人分配、任务提交、进度追踪、催办等完整功能
 */
@Slf4j
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ==================== 任务CRUD ====================

    /** 创建任务 */
    @PostMapping("/create")
    public Result<TaskInfo> createTask(@RequestBody Map<String, Object> request) {
        TaskInfo task = new TaskInfo();
        task.setConferenceId(toLong(request.get("conferenceId")));
        task.setTaskName((String) request.get("taskName"));
        task.setTaskType((String) request.get("taskType"));
        task.setCategory((String) request.get("category"));
        task.setCompletionMethod((String) request.get("completionMethod"));
        task.setDescription((String) request.get("description"));
        task.setPriority((String) request.get("priority"));
        task.setCreatorId(toLong(request.get("creatorId")));
        task.setOwnerName((String) request.get("ownerName"));
        task.setTargetGroups(request.get("targetGroups") != null ? request.get("targetGroups").toString() : null);
        task.setConfig(request.get("config") != null ? request.get("config").toString() : null);
        task.setAttachments(request.get("attachments") != null ? request.get("attachments").toString() : null);

        // 解析deadline
        String deadline = (String) request.get("deadline");
        if (deadline != null && !deadline.isEmpty()) {
            task.setDeadline(java.time.LocalDateTime.parse(deadline, 
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        // 解析执行人列表
        List<TaskAssignee> assignees = new java.util.ArrayList<>();
        Object assigneesObj = request.get("assignees");
        if (assigneesObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    TaskAssignee assignee = new TaskAssignee();
                    assignee.setUserId(toLong(map.get("userId")));
                    assignee.setUserName((String) map.get("userName"));
                    assignee.setRole(map.get("role") != null ? (String) map.get("role") : "executor");
                    assignee.setIsMain(map.get("isMain") != null ? ((Number) map.get("isMain")).intValue() : 0);
                    assignees.add(assignee);
                }
            }
        }

        TaskInfo created = taskService.createTask(task, assignees);
        return Result.ok("任务创建成功", created);
    }

    /** 删除任务 */
    @DeleteMapping("/{taskId}")
    public Result<String> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return Result.ok("任务已删除");
    }

    /** 更新任务 */
    @PutMapping("/{taskId}")
    public Result<TaskInfo> updateTask(@PathVariable Long taskId, @RequestBody TaskInfo task) {
        task.setId(taskId);
        TaskInfo updated = taskService.updateTask(task);
        return Result.ok("任务更新成功", updated);
    }

    /** 获取任务详情 */
    @GetMapping("/{taskId}")
    public Result<Map<String, Object>> getTaskDetail(@PathVariable Long taskId) {
        Map<String, Object> detail = taskService.getTaskDetail(taskId);
        if (detail == null) {
            return Result.fail("任务不存在");
        }
        return Result.ok(detail);
    }

    // ==================== 任务列表 ====================

    /** 按会议获取任务列表(分页) */
    @GetMapping("/list")
    public Result<Page<TaskInfo>> listTasks(
            @RequestParam(required = false) Long conferenceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TaskInfo> result = taskService.listByConference(conferenceId, status, category, page, size);
        return Result.ok(result);
    }

    /** 获取我的任务(作为执行人) */
    @GetMapping("/my-tasks")
    public Result<Page<Map<String, Object>>> getMyTasks(
            @RequestParam Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Map<String, Object>> result = taskService.listMyTasks(userId, status, page, size);
        return Result.ok(result);
    }

    // ==================== 执行人管理 ====================

    /** 分配执行人 */
    @PostMapping("/{taskId}/assign")
    public Result<String> assignTask(@PathVariable Long taskId, @RequestBody List<TaskAssignee> assignees) {
        taskService.assignTask(taskId, assignees);
        return Result.ok("任务分配成功");
    }

    /** 获取任务执行人 */
    @GetMapping("/{taskId}/assignees")
    public Result<List<TaskAssignee>> getAssignees(@PathVariable Long taskId) {
        List<TaskAssignee> assignees = taskService.getAssignees(taskId);
        return Result.ok(assignees);
    }

    // ==================== 任务执行 ====================

    /** 提交任务反馈 */
    @PostMapping("/{taskId}/submit")
    public Result<String> submitTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> submitData) {
        Long userId = toLong(submitData.get("userId"));
        String content = (String) submitData.get("content");
        String images = submitData.get("images") != null ? submitData.get("images").toString() : null;
        String location = submitData.get("location") != null ? submitData.get("location").toString() : null;
        taskService.submitTask(taskId, userId, content, images, location);
        return Result.ok("任务提交成功");
    }

    /** 完成任务 */
    @PostMapping("/{taskId}/complete")
    public Result<String> completeTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> data) {
        Long operatorId = toLong(data.get("operatorId"));
        String operatorName = (String) data.get("operatorName");
        String remark = (String) data.get("remark");
        taskService.completeTask(taskId, operatorId, operatorName, remark);
        return Result.ok("任务已完成");
    }

    /** 取消任务 */
    @PostMapping("/{taskId}/cancel")
    public Result<String> cancelTask(
            @PathVariable Long taskId,
            @RequestBody(required = false) Map<String, Object> data) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        Long operatorId = toLong(data.get("operatorId"));
        String operatorName = (String) data.get("operatorName");
        String remark = (String) data.get("remark");
        taskService.cancelTask(taskId, operatorId, operatorName, remark);
        return Result.ok("任务已取消");
    }

    /** 催办任务 */
    @PostMapping("/{taskId}/urge")
    public Result<String> urgeTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> data) {
        Long operatorId = toLong(data.get("operatorId"));
        String operatorName = (String) data.get("operatorName");
        taskService.urgeTask(taskId, operatorId, operatorName);
        return Result.ok("催办已发送");
    }

    // ==================== 日志 ====================

    /** 获取任务日志 */
    @GetMapping("/{taskId}/logs")
    public Result<List<TaskLog>> getTaskLogs(@PathVariable Long taskId) {
        List<TaskLog> logs = taskService.getTaskLogs(taskId);
        return Result.ok(logs);
    }

    // ==================== 统计 ====================

    /** 任务统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getTaskStats(
            @RequestParam(required = false) Long conferenceId) {
        Map<String, Object> stats = taskService.getTaskStats(conferenceId);
        return Result.ok(stats);
    }

    /** Long类型转换辅助 */
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
