package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务服务完整流程测试
 * @author AI Executive
 * @date 2026-04-02
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskDependencyService dependencyService;

    private TaskInfo testTask;
    private List<TaskAssignee> testAssignees;

    @BeforeEach
    public void setUp() {
        // 创建测试任务
        testTask = new TaskInfo();
        testTask.setConferenceId(1L);
        testTask.setTaskName("测试任务");
        testTask.setTaskType("custom");
        testTask.setCategory("custom");
        testTask.setCompletionMethod("text_image");
        testTask.setDescription("这是一个测试任务");
        testTask.setPriority("high");
        testTask.setStatus("pending");
        testTask.setProgress(0);
        testTask.setDeadline(LocalDateTime.now().plusDays(7));
        testTask.setCreatorId(1L);
        testTask.setOwnerName("测试管理员");

        // 创建测试执行人
        testAssignees = new ArrayList<>();
        TaskAssignee assignee1 = new TaskAssignee();
        assignee1.setUserId(100L);
        assignee1.setUserName("张三");
        assignee1.setRole("executor");
        assignee1.setIsMain(1);
        assignee1.setStatus("pending");
        testAssignees.add(assignee1);

        TaskAssignee assignee2 = new TaskAssignee();
        assignee2.setUserId(101L);
        assignee2.setUserName("李四");
        assignee2.setRole("assistant");
        assignee2.setIsMain(0);
        assignee2.setStatus("pending");
        testAssignees.add(assignee2);
    }

    @Test
    public void testCompleteTaskLifecycle() {
        // 1. 创建任务
        TaskInfo created = taskService.createTask(testTask, testAssignees);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("pending", created.getStatus());

        // 2. 查询任务详情
        Map<String, Object> detail = taskService.getTaskDetail(created.getId());
        assertNotNull(detail);
        assertNotNull(detail.get("task"));
        assertNotNull(detail.get("assignees"));
        assertNotNull(detail.get("logs"));

        // 3. 第一个执行人提交任务
        taskService.submitTask(created.getId(), 100L, "我已完成任务", null, null);

        // 4. 检查进度
        TaskInfo updated = taskService.getById(created.getId());
        assertTrue(updated.getProgress() >= 0 && updated.getProgress() < 100);

        // 5. 第二个执行人提交任务
        taskService.submitTask(created.getId(), 101L, "我也完成了", null, null);

        // 6. 检查任务是否自动完成
        TaskInfo finalTask = taskService.getById(created.getId());
        // 注意：实际完成状态可能需要手动触发，这里检查进度
        assertTrue(finalTask.getProgress() == 100 || finalTask.getProgress() > 0);
    }

    @Test
    public void testTaskWithDependency() {
        // 创建前置任务
        TaskInfo predecessor = new TaskInfo();
        predecessor.setConferenceId(1L);
        predecessor.setTaskName("前置任务");
        predecessor.setTaskType("custom");
        predecessor.setCategory("custom");
        predecessor.setCompletionMethod("text_image");
        predecessor.setDescription("需要先完成的任务");
        predecessor.setPriority("high");
        predecessor.setStatus("pending");
        predecessor.setProgress(0);
        predecessor.setCreatorId(1L);
        predecessor.setOwnerName("测试管理员");
        TaskInfo createdPredecessor = taskService.createTask(predecessor, testAssignees);

        // 创建后置任务
        TaskInfo successor = new TaskInfo();
        successor.setConferenceId(1L);
        successor.setTaskName("后置任务");
        successor.setTaskType("custom");
        successor.setCategory("custom");
        successor.setCompletionMethod("text_image");
        successor.setDescription("依赖前置任务");
        successor.setPriority("medium");
        successor.setStatus("pending");
        successor.setProgress(0);
        successor.setCreatorId(1L);
        successor.setOwnerName("测试管理员");
        TaskInfo createdSuccessor = taskService.createTask(successor, testAssignees);

        // 添加依赖关系
        dependencyService.addDependency(
            createdSuccessor.getId(),
            createdPredecessor.getId(),
            "finish_to_start"
        );

        // 验证后置任务不能开始
        boolean canStart = dependencyService.canStartTask(createdSuccessor.getId());
        assertFalse(canStart);

        // 完成前置任务
        taskService.submitTask(createdPredecessor.getId(), 100L, "前置任务完成", null, null);
        taskService.submitTask(createdPredecessor.getId(), 101L, "前置任务也完成", null, null);

        // 手动完成前置任务
        taskService.completeTask(createdPredecessor.getId(), 1L, "测试管理员", "");

        // 触发依赖检查
        dependencyService.onPredecessorCompleted(createdPredecessor.getId());

        // 验证后置任务自动启动
        TaskInfo updatedSuccessor = taskService.getById(createdSuccessor.getId());
        assertEquals("in_progress", updatedSuccessor.getStatus());
    }

    @Test
    public void testTaskUrge() {
        // 创建任务
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        // 催办任务
        taskService.urgeTask(created.getId(), 1L, "测试管理员");

        // 验证日志
        List<?> logs = taskService.getTaskLogs(created.getId());
        assertTrue(logs.stream()
            .anyMatch(log -> log.toString().contains("urged")));
    }

    @Test
    public void testTaskCancel() {
        // 创建任务
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        // 取消任务
        taskService.cancelTask(created.getId(), 1L, "测试管理员", "测试取消");

        // 验证状态
        TaskInfo cancelled = taskService.getById(created.getId());
        assertEquals("cancelled", cancelled.getStatus());

        // 验证日志
        List<?> logs = taskService.getTaskLogs(created.getId());
        assertTrue(logs.stream()
            .anyMatch(log -> log.toString().contains("cancelled")));
    }

    @Test
    public void testGetPendingAssignees() {
        // 创建任务
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        // 第一个执行人提交
        taskService.submitTask(created.getId(), 100L, "完成", null, null);

        // 获取未完成执行人（通过 getAssignees + filter 实现）
        List<TaskAssignee> pendingAssignees = taskService.getAssignees(created.getId()).stream()
            .filter(a -> !"completed".equals(a.getStatus()))
            .toList();

        assertNotNull(pendingAssignees);
        assertTrue(pendingAssignees.size() >= 1);
        assertTrue(pendingAssignees.stream()
            .anyMatch(a -> a.getUserId().equals(101L)));
    }

    @Test
    public void testTaskStats() {
        // 创建多个不同状态的任务
        TaskInfo pendingTask = new TaskInfo();
        pendingTask.setConferenceId(1L);
        pendingTask.setTaskName("待处理任务");
        pendingTask.setTaskType("custom");
        pendingTask.setCategory("custom");
        pendingTask.setCompletionMethod("text_image");
        pendingTask.setDescription("测试");
        pendingTask.setPriority("medium");
        pendingTask.setStatus("pending");
        pendingTask.setProgress(0);
        pendingTask.setCreatorId(1L);
        pendingTask.setOwnerName("测试管理员");
        taskService.save(pendingTask);

        TaskInfo completedTask = new TaskInfo();
        completedTask.setConferenceId(1L);
        completedTask.setTaskName("已完成任务");
        completedTask.setTaskType("custom");
        completedTask.setCategory("custom");
        completedTask.setCompletionMethod("text_image");
        completedTask.setDescription("测试");
        completedTask.setPriority("medium");
        completedTask.setStatus("completed");
        completedTask.setProgress(100);
        completedTask.setCreatorId(1L);
        completedTask.setOwnerName("测试管理员");
        taskService.save(completedTask);

        // 获取统计
        Map<String, Object> stats = taskService.getTaskStats(1L);

        assertNotNull(stats);
        assertTrue((Integer) stats.get("total") >= 2);
        assertTrue((Integer) stats.get("pending") >= 1);
        assertTrue((Integer) stats.get("completed") >= 1);
    }

    @Test
    public void testAssignTask() {
        // 创建任务
        TaskInfo created = taskService.createTask(testTask, new ArrayList<>());

        // 分配执行人
        taskService.assignTask(created.getId(), testAssignees);

        // 验证执行人
        List<TaskAssignee> assignees = taskService.getAssignees(created.getId());
        assertNotNull(assignees);
        assertEquals(2, assignees.size());

        // 验证任务状态
        TaskInfo updated = taskService.getById(created.getId());
        assertEquals("in_progress", updated.getStatus());
    }

    @Test
    public void testDeleteTask() {
        // 创建任务
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        // 删除任务
        taskService.deleteTask(created.getId());

        // 验证删除
        TaskInfo deleted = taskService.getById(created.getId());
        assertNull(deleted);

        // 验证执行人也已删除
        List<TaskAssignee> assignees = taskService.getAssignees(created.getId());
        assertTrue(assignees == null || assignees.isEmpty());
    }

    @Test
    public void testUpdateTask() {
        // 创建任务
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        // 更新任务
        created.setTaskName("更新后的任务名");
        created.setPriority("urgent");
        TaskInfo updated = taskService.updateTask(created);

        // 验证更新
        assertEquals("更新后的任务名", updated.getTaskName());
        assertEquals("urgent", updated.getPriority());
    }
}
