package com.conference.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskLog;
import com.conference.collaboration.mapper.TaskAssigneeMapper;
import com.conference.collaboration.mapper.TaskInfoMapper;
import com.conference.collaboration.mapper.TaskLogMapper;
import com.conference.collaboration.service.TaskService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo>
        implements TaskService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final TaskInfoMapper taskMapper;
    private final TaskAssigneeMapper assigneeMapper;
    private final TaskLogMapper logMapper;

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    @Transactional
    public TaskInfo createTask(TaskInfo task, List<TaskAssignee> assignees) {
        Long tenantId = getTenantId();
        task.setTenantId(tenantId);
        task.setStatus(task.getStatus() != null ? task.getStatus() : "pending");
        task.setProgress(0);
        task.setDeleted(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.insert(task);

        // 保存执行人
        if (assignees != null && !assignees.isEmpty()) {
            for (TaskAssignee assignee : assignees) {
                assignee.setTaskId(task.getId());
                assignee.setStatus("pending");
                assignee.setCreateTime(LocalDateTime.now());
                assigneeMapper.insert(assignee);
            }
        }

        // 记录日志
        addLog(task.getId(), task.getCreatorId(), task.getOwnerName(), "created", "任务已创建");
        log.info("[租户{}] 任务创建成功: id={}, name={}", tenantId, task.getId(), task.getTaskName());
        return task;
    }

    @Override
    @Transactional
    public TaskInfo updateTask(TaskInfo task) {
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        log.info("任务{}已更新", task.getId());
        return task;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        // 先删除执行人
        assigneeMapper.delete(new LambdaQueryWrapper<TaskAssignee>()
                .eq(TaskAssignee::getTaskId, taskId));
        // 再删除日志
        logMapper.delete(new LambdaQueryWrapper<TaskLog>()
                .eq(TaskLog::getTaskId, taskId));
        // 最后删除任务本身
        taskMapper.deleteById(taskId);
        log.info("任务{}已彻底删除", taskId);
    }

    @Override
    public Map<String, Object> getTaskDetail(Long taskId) {
        TaskInfo task = taskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }

        List<TaskAssignee> assignees = getAssignees(taskId);
        List<TaskLog> logs = getTaskLogs(taskId);

        // 计算完成进度
        long totalAssignees = assignees.size();
        long completedAssignees = assignees.stream()
                .filter(a -> "completed".equals(a.getStatus()))
                .count();

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("task", task);
        detail.put("assignees", assignees);
        detail.put("logs", logs);
        detail.put("totalAssignees", totalAssignees);
        detail.put("completedAssignees", completedAssignees);
        detail.put("progress", totalAssignees > 0 ? (int) (completedAssignees * 100 / totalAssignees) : 0);
        return detail;
    }

    @Override
    public Page<TaskInfo> listByConference(Long conferenceId, String status, String category, int page, int size) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskInfo::getTenantId, tenantId)
                .eq(TaskInfo::getDeleted, 0);
        if (conferenceId != null) {
            wrapper.eq(TaskInfo::getConferenceId, conferenceId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TaskInfo::getStatus, status);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(TaskInfo::getCategory, category);
        }
        wrapper.orderByDesc(TaskInfo::getCreateTime);
        return taskMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Map<String, Object>> listMyTasks(Long userId, String status, int page, int size) {
        // 先查我负责的任务ID
        LambdaQueryWrapper<TaskAssignee> assigneeWrapper = new LambdaQueryWrapper<>();
        assigneeWrapper.eq(TaskAssignee::getUserId, userId);
        if (StringUtils.hasText(status)) {
            assigneeWrapper.eq(TaskAssignee::getStatus, status);
        }
        List<TaskAssignee> myAssignments = assigneeMapper.selectList(assigneeWrapper);

        if (myAssignments.isEmpty()) {
            return new Page<>(page, size);
        }

        List<Long> taskIds = myAssignments.stream()
                .map(TaskAssignee::getTaskId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, TaskAssignee> assignmentMap = myAssignments.stream()
                .collect(Collectors.toMap(TaskAssignee::getTaskId, a -> a, (a, b) -> a));

        LambdaQueryWrapper<TaskInfo> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.in(TaskInfo::getId, taskIds)
                .eq(TaskInfo::getDeleted, 0)
                .orderByDesc(TaskInfo::getCreateTime);

        Page<TaskInfo> taskPage = taskMapper.selectPage(new Page<>(page, size), taskWrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page, size);
        resultPage.setTotal(taskPage.getTotal());
        resultPage.setRecords(taskPage.getRecords().stream().map(task -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("task", task);
            map.put("myAssignment", assignmentMap.get(task.getId()));
            return map;
        }).collect(Collectors.toList()));
        return resultPage;
    }

    @Override
    @Transactional
    public void assignTask(Long taskId, List<TaskAssignee> assignees) {
        for (TaskAssignee assignee : assignees) {
            assignee.setTaskId(taskId);
            assignee.setStatus("pending");
            assignee.setCreateTime(LocalDateTime.now());
            assigneeMapper.insert(assignee);
        }

        // 更新任务状态为进行中
        TaskInfo task = taskMapper.selectById(taskId);
        if (task != null && "pending".equals(task.getStatus())) {
            task.setStatus("in_progress");
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }

        addLog(taskId, null, "系统", "assigned",
                "分配了" + assignees.size() + "个执行人");
        log.info("任务{}已分配{}个执行人", taskId, assignees.size());
    }

    @Override
    @Transactional
    public void submitTask(Long taskId, Long userId, String content, String images, String location) {
        LambdaQueryWrapper<TaskAssignee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAssignee::getTaskId, taskId)
                .eq(TaskAssignee::getUserId, userId);
        TaskAssignee assignee = assigneeMapper.selectOne(wrapper);
        if (assignee == null) {
            throw new RuntimeException("您不是此任务的执行人");
        }

        assignee.setStatus("completed");
        assignee.setSubmitContent(content);
        assignee.setSubmitImages(images);
        assignee.setSubmitLocation(location);
        assignee.setSubmitTime(LocalDateTime.now());
        assigneeMapper.updateById(assignee);

        // 检查是否所有人都已完成
        updateTaskProgress(taskId);
        addLog(taskId, userId, assignee.getUserName(), "submitted", "提交了任务反馈");
        log.info("用户{}提交了任务{}的反馈", userId, taskId);
    }

    @Override
    @Transactional
    public void cancelTask(Long taskId, Long operatorId, String operatorName, String remark) {
        TaskInfo task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        task.setStatus("cancelled");
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        addLog(taskId, operatorId, operatorName, "cancelled",
                StringUtils.hasText(remark) ? remark : "任务已取消");
        log.info("任务{}已取消", taskId);
    }

    @Override
    @Transactional
    public void completeTask(Long taskId, Long operatorId, String operatorName, String remark) {
        TaskInfo task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        task.setStatus("completed");
        task.setProgress(100);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        addLog(taskId, operatorId, operatorName, "completed",
                StringUtils.hasText(remark) ? remark : "任务已完成");
        log.info("任务{}已完成", taskId);
    }

    @Override
    public List<TaskAssignee> getAssignees(Long taskId) {
        LambdaQueryWrapper<TaskAssignee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskAssignee::getTaskId, taskId)
                .orderByAsc(TaskAssignee::getCreateTime);
        return assigneeMapper.selectList(wrapper);
    }

    @Override
    public List<TaskLog> getTaskLogs(Long taskId) {
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskId, taskId)
                .orderByDesc(TaskLog::getCreateTime);
        return logMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getTaskStats(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskInfo::getTenantId, tenantId)
                .eq(TaskInfo::getDeleted, 0);
        if (conferenceId != null) {
            wrapper.eq(TaskInfo::getConferenceId, conferenceId);
        }

        List<TaskInfo> tasks = taskMapper.selectList(wrapper);
        long total = tasks.size();
        long pending = tasks.stream().filter(t -> "pending".equals(t.getStatus())).count();
        long inProgress = tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();
        long completed = tasks.stream().filter(t -> "completed".equals(t.getStatus())).count();
        long overdue = tasks.stream().filter(t -> "overdue".equals(t.getStatus())).count();
        long cancelled = tasks.stream().filter(t -> "cancelled".equals(t.getStatus())).count();

        // 按优先级统计
        Map<String, Long> byPriority = tasks.stream()
                .filter(t -> t.getPriority() != null)
                .collect(Collectors.groupingBy(TaskInfo::getPriority, Collectors.counting()));

        // 按类别统计
        Map<String, Long> byCategory = tasks.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(TaskInfo::getCategory, Collectors.counting()));

        // 计算平均进度
        double avgProgress = tasks.stream()
                .filter(t -> t.getProgress() != null)
                .mapToInt(TaskInfo::getProgress)
                .average()
                .orElse(0);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("completed", completed);
        stats.put("overdue", overdue);
        stats.put("cancelled", cancelled);
        stats.put("completionRate", total > 0 ? Math.round(completed * 100.0 / total) : 0);
        stats.put("avgProgress", Math.round(avgProgress));
        stats.put("byPriority", byPriority);
        stats.put("byCategory", byCategory);
        return stats;
    }

    @Override
    @Transactional
    public void urgeTask(Long taskId, Long operatorId, String operatorName) {
        TaskInfo task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        addLog(taskId, operatorId, operatorName, "urged", "催办了此任务");
        log.info("任务{}已被催办", taskId);
    }

    /** 更新任务进度 */
    private void updateTaskProgress(Long taskId) {
        List<TaskAssignee> allAssignees = getAssignees(taskId);
        long total = allAssignees.size();
        long completed = allAssignees.stream()
                .filter(a -> "completed".equals(a.getStatus()))
                .count();
        int progress = total > 0 ? (int) (completed * 100 / total) : 0;

        TaskInfo task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setProgress(progress);
            if (progress >= 100) {
                task.setStatus("completed");
            } else if (progress > 0) {
                task.setStatus("in_progress");
            }
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    /** 添加任务日志 */
    private void addLog(Long taskId, Long operatorId, String operatorName, String action, String remark) {
        TaskLog taskLog = new TaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setOperatorId(operatorId);
        taskLog.setOperatorName(operatorName);
        taskLog.setAction(action);
        taskLog.setRemark(remark);
        taskLog.setCreateTime(LocalDateTime.now());
        logMapper.insert(taskLog);
    }
}
