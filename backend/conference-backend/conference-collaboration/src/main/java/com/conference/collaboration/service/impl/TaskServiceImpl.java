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
import com.conference.collaboration.service.TaskNotificationService;
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
    private final TaskNotificationService notificationService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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

        // 发送任务分派通知
        if (assignees != null && !assignees.isEmpty()) {
            try {
                notificationService.notifyTaskAssigned(task, assignees);
            } catch (Exception e) {
                log.error("任务分派通知发送失败: taskId={}", task.getId(), e);
            }
        }

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

        // 获取任务及其完成配置
        TaskInfo task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 校验完成配置
        validateSubmission(task, content, images, location);

        assignee.setStatus("completed");
        assignee.setSubmitContent(content);
        assignee.setSubmitImages(images);
        assignee.setSubmitLocation(location);
        assignee.setSubmitTime(LocalDateTime.now());
        assigneeMapper.updateById(assignee);

        // 检查是否所有人都已完成
        updateTaskProgress(taskId);
        addLog(taskId, userId, assignee.getUserName(), "submitted", "提交了任务反馈");

        // 发送完成通知给任务创建者
        try {
            notificationService.notifyTaskCompleted(task, assignee);
        } catch (Exception e) {
            log.error("任务完成通知发送失败: taskId={}", taskId, e);
        }

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

        // 发送任务取消通知
        try {
            List<TaskAssignee> allAssignees = getAssignees(taskId);
            notificationService.notifyTaskCancelled(task, allAssignees, remark);
        } catch (Exception e) {
            log.error("任务取消通知发送失败: taskId={}", taskId, e);
        }

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

        // 使用SQL聚合查询代替全量加载到内存
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) as total," +
            " SUM(CASE WHEN status='pending' THEN 1 ELSE 0 END) as pending_cnt," +
            " SUM(CASE WHEN status='in_progress' THEN 1 ELSE 0 END) as progress_cnt," +
            " SUM(CASE WHEN status='completed' THEN 1 ELSE 0 END) as completed_cnt," +
            " SUM(CASE WHEN status='overdue' THEN 1 ELSE 0 END) as overdue_cnt," +
            " SUM(CASE WHEN status='cancelled' THEN 1 ELSE 0 END) as cancelled_cnt," +
            " COALESCE(AVG(progress), 0) as avg_progress" +
            " FROM task_info WHERE tenant_id = ? AND deleted = 0"
        );

        List<Object> params = new java.util.ArrayList<>();
        params.add(tenantId);
        if (conferenceId != null) {
            sql.append(" AND conference_id = ?");
            params.add(conferenceId);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        jdbcTemplate.query(sql.toString(), rs -> {
            long total = rs.getLong("total");
            long completedCount = rs.getLong("completed_cnt");
            stats.put("total", total);
            stats.put("pending", rs.getLong("pending_cnt"));
            stats.put("inProgress", rs.getLong("progress_cnt"));
            stats.put("completed", completedCount);
            stats.put("overdue", rs.getLong("overdue_cnt"));
            stats.put("cancelled", rs.getLong("cancelled_cnt"));
            stats.put("completionRate", total > 0 ? Math.round(completedCount * 100.0 / total) : 0);
            stats.put("avgProgress", Math.round(rs.getDouble("avg_progress")));
        }, params.toArray());

        // 按优先级统计
        StringBuilder pSql = new StringBuilder(
            "SELECT priority, COUNT(*) as cnt FROM task_info WHERE tenant_id = ? AND deleted = 0 AND priority IS NOT NULL");
        List<Object> pParams = new java.util.ArrayList<>();
        pParams.add(tenantId);
        if (conferenceId != null) { pSql.append(" AND conference_id = ?"); pParams.add(conferenceId); }
        pSql.append(" GROUP BY priority");
        Map<String, Long> byPriority = new LinkedHashMap<>();
        jdbcTemplate.query(pSql.toString(), rs -> {
            byPriority.put(rs.getString("priority"), rs.getLong("cnt"));
        }, pParams.toArray());

        // 按类别统计
        StringBuilder cSql = new StringBuilder(
            "SELECT category, COUNT(*) as cnt FROM task_info WHERE tenant_id = ? AND deleted = 0 AND category IS NOT NULL");
        List<Object> cParams = new java.util.ArrayList<>();
        cParams.add(tenantId);
        if (conferenceId != null) { cSql.append(" AND conference_id = ?"); cParams.add(conferenceId); }
        cSql.append(" GROUP BY category");
        Map<String, Long> byCategory = new LinkedHashMap<>();
        jdbcTemplate.query(cSql.toString(), rs -> {
            byCategory.put(rs.getString("category"), rs.getLong("cnt"));
        }, cParams.toArray());

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

        // 获取未完成的执行人并发送催办通知
        List<TaskAssignee> pendingAssignees = getAssignees(taskId).stream()
                .filter(a -> !"completed".equals(a.getStatus()))
                .toList();
        if (!pendingAssignees.isEmpty()) {
            try {
                notificationService.notifyTaskUrge(task, pendingAssignees);
            } catch (Exception e) {
                log.error("催办通知发送失败: taskId={}", taskId, e);
            }
        }

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

    /**
     * 校验提交内容是否符合完成配置要求
     */
    @SuppressWarnings("unchecked")
    private void validateSubmission(TaskInfo task, String content, String images, String location) {
        if (task.getConfig() == null || task.getConfig().isEmpty()) {
            return;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> config = objectMapper.readValue(task.getConfig(), Map.class);
            String method = task.getCompletionMethod();

            // 文字/图片类型校验
            if ("text_image".equals(method)) {
                Integer minTextLength = getConfigInt(config, "minTextLength");
                if (minTextLength != null && minTextLength > 0) {
                    if (content == null || content.length() < minTextLength) {
                        throw new RuntimeException("文字内容至少需要" + minTextLength + "个字符");
                    }
                }
                Integer minImageCount = getConfigInt(config, "minImageCount");
                if (minImageCount != null && minImageCount > 0) {
                    int imageCount = 0;
                    if (images != null && !images.isEmpty()) {
                        try {
                            List<?> imageList = objectMapper.readValue(images, List.class);
                            imageCount = imageList.size();
                        } catch (Exception e) {
                            imageCount = images.split(",").length;
                        }
                    }
                    if (imageCount < minImageCount) {
                        throw new RuntimeException("至少需要上传" + minImageCount + "张图片");
                    }
                }
            }

            // 位置签到类型校验 (haversine距离)
            if ("location".equals(method) && location != null && !location.isEmpty()) {
                Object targetLocationObj = config.get("targetLocation");
                Integer maxDistance = getConfigInt(config, "maxDistance");
                if (targetLocationObj instanceof Map<?, ?> targetMap && maxDistance != null) {
                    double targetLat = toDouble(targetMap.get("lat"));
                    double targetLng = toDouble(targetMap.get("lng"));
                    if (targetLat != 0 && targetLng != 0) {
                        Map<String, Object> userLocation = objectMapper.readValue(location, Map.class);
                        double userLat = toDouble(userLocation.get("lat"));
                        double userLng = toDouble(userLocation.get("lng"));
                        double distance = haversineDistance(targetLat, targetLng, userLat, userLng);
                        if (distance > maxDistance) {
                            throw new RuntimeException(String.format(
                                "签到位置超出范围，距离目标%.0f米，最大允许%d米", distance, maxDistance));
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("解析完成配置失败: {}", e.getMessage());
        }
    }

    /** Haversine公式计算两点间距离（米） */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Integer getConfigInt(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(value.toString()); } catch (Exception e) { return null; }
    }

    private double toDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0; }
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
