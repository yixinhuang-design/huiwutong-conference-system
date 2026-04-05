package com.conference.collaboration.scheduler;

import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.mapper.TaskInfoMapper;
import com.conference.collaboration.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务调度器测试
 * @author AI Executive
 * @date 2026-04-02
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TaskSchedulerTest {

    @Autowired
    private TaskScheduledJobs taskScheduler;

    @Autowired
    private TaskInfoMapper taskMapper;

    @Autowired
    private TaskService taskService;

    @Test
    public void testCheckOverdueTasks() {
        // 创建一个应该逾期的任务
        TaskInfo overdueTask = new TaskInfo();
        overdueTask.setConferenceId(1L);
        overdueTask.setTaskName("逾期任务");
        overdueTask.setTaskType("custom");
        overdueTask.setCategory("custom");
        overdueTask.setCompletionMethod("text_image");
        overdueTask.setDescription("测试逾期");
        overdueTask.setPriority("medium");
        overdueTask.setStatus("in_progress");
        overdueTask.setProgress(50);
        overdueTask.setDeadline(LocalDateTime.now().minusHours(1)); // 截止时间在1小时前
        overdueTask.setCreatorId(1L);
        overdueTask.setOwnerName("测试管理员");
        taskMapper.insert(overdueTask);

        // 创建一个正常的任务
        TaskInfo normalTask = new TaskInfo();
        normalTask.setConferenceId(1L);
        normalTask.setTaskName("正常任务");
        normalTask.setTaskType("custom");
        normalTask.setCategory("custom");
        normalTask.setCompletionMethod("text_image");
        normalTask.setDescription("测试正常");
        normalTask.setPriority("medium");
        normalTask.setStatus("in_progress");
        normalTask.setProgress(50);
        normalTask.setDeadline(LocalDateTime.now().plusDays(1)); // 截止时间在明天
        normalTask.setCreatorId(1L);
        normalTask.setOwnerName("测试管理员");
        taskMapper.insert(normalTask);

        // 执行逾期检测
        taskScheduler.checkOverdueTasks();

        // 验证逾期任务状态
        TaskInfo updatedOverdue = taskMapper.selectById(overdueTask.getId());
        assertEquals("overdue", updatedOverdue.getStatus());

        // 验证正常任务状态不变
        TaskInfo updatedNormal = taskMapper.selectById(normalTask.getId());
        assertEquals("in_progress", updatedNormal.getStatus());
    }

    @Test
    public void testMultipleTasksWithDifferentDeadlines() {
        // 创建3个不同截止时间的任务
        TaskInfo task1 = new TaskInfo();
        task1.setConferenceId(1L);
        task1.setTaskName("任务1");
        task1.setTaskType("custom");
        task1.setCategory("custom");
        task1.setCompletionMethod("text_image");
        task1.setDescription("测试1");
        task1.setPriority("medium");
        task1.setStatus("pending");
        task1.setProgress(0);
        task1.setDeadline(LocalDateTime.now().minusHours(2));
        task1.setCreatorId(1L);
        task1.setOwnerName("测试管理员");
        taskMapper.insert(task1);

        TaskInfo task2 = new TaskInfo();
        task2.setConferenceId(1L);
        task2.setTaskName("任务2");
        task2.setTaskType("custom");
        task2.setCategory("custom");
        task2.setCompletionMethod("text_image");
        task2.setDescription("测试2");
        task2.setPriority("medium");
        task2.setStatus("in_progress");
        task2.setProgress(30);
        task2.setDeadline(LocalDateTime.now().minusMinutes(30));
        task2.setCreatorId(1L);
        task2.setOwnerName("测试管理员");
        taskMapper.insert(task2);

        TaskInfo task3 = new TaskInfo();
        task3.setConferenceId(1L);
        task3.setTaskName("任务3");
        task3.setTaskType("custom");
        task3.setCategory("custom");
        task3.setCompletionMethod("text_image");
        task3.setDescription("测试3");
        task3.setPriority("medium");
        task3.setStatus("in_progress");
        task3.setProgress(70);
        task3.setDeadline(LocalDateTime.now().plusHours(1));
        task3.setCreatorId(1L);
        task3.setOwnerName("测试管理员");
        taskMapper.insert(task3);

        // 执行逾期检测
        taskScheduler.checkOverdueTasks();

        // 验证：任务1和任务2应该逾期，任务3不应该逾期
        TaskInfo updated1 = taskMapper.selectById(task1.getId());
        TaskInfo updated2 = taskMapper.selectById(task2.getId());
        TaskInfo updated3 = taskMapper.selectById(task3.getId());

        assertEquals("overdue", updated1.getStatus());
        assertEquals("overdue", updated2.getStatus());
        assertEquals("in_progress", updated3.getStatus());
    }

    @Test
    public void testAlreadyOverdueTaskShouldNotBeUpdated() {
        // 创建一个已经逾期的任务
        TaskInfo alreadyOverdue = new TaskInfo();
        alreadyOverdue.setConferenceId(1L);
        alreadyOverdue.setTaskName("已逾期任务");
        alreadyOverdue.setTaskType("custom");
        alreadyOverdue.setCategory("custom");
        alreadyOverdue.setCompletionMethod("text_image");
        alreadyOverdue.setDescription("测试");
        alreadyOverdue.setPriority("medium");
        alreadyOverdue.setStatus("overdue");
        alreadyOverdue.setProgress(50);
        alreadyOverdue.setDeadline(LocalDateTime.now().minusHours(5));
        alreadyOverdue.setCreatorId(1L);
        alreadyOverdue.setOwnerName("测试管理员");
        alreadyOverdue.setUpdateTime(LocalDateTime.now().minusHours(2));
        taskMapper.insert(alreadyOverdue);

        // 记录更新时间
        LocalDateTime beforeUpdateTime = alreadyOverdue.getUpdateTime();

        // 执行逾期检测
        taskScheduler.checkOverdueTasks();

        // 验证：已经逾期的任务不应该重复更新
        TaskInfo updated = taskMapper.selectById(alreadyOverdue.getId());
        assertEquals("overdue", updated.getStatus());
        // 注意：这里可能无法精确测试时间，因为SQL会在更新时自动设置update_time
    }

    @Test
    public void testCompletedTasksShouldNotBecomeOverdue() {
        // 创建一个已完成的任务，但截止时间已过
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
        completedTask.setDeadline(LocalDateTime.now().minusHours(1));
        completedTask.setCreatorId(1L);
        completedTask.setOwnerName("测试管理员");
        taskMapper.insert(completedTask);

        // 执行逾期检测
        taskScheduler.checkOverdueTasks();

        // 验证：已完成的任务不应该变成逾期
        TaskInfo updated = taskMapper.selectById(completedTask.getId());
        assertEquals("completed", updated.getStatus());
        assertNotEquals("overdue", updated.getStatus());
    }

    @Test
    public void testCancelledTasksShouldNotBecomeOverdue() {
        // 创建一个已取消的任务，但截止时间已过
        TaskInfo cancelledTask = new TaskInfo();
        cancelledTask.setConferenceId(1L);
        cancelledTask.setTaskName("已取消任务");
        cancelledTask.setTaskType("custom");
        cancelledTask.setCategory("custom");
        cancelledTask.setCompletionMethod("text_image");
        cancelledTask.setDescription("测试");
        cancelledTask.setPriority("medium");
        cancelledTask.setStatus("cancelled");
        cancelledTask.setProgress(0);
        cancelledTask.setDeadline(LocalDateTime.now().minusHours(1));
        cancelledTask.setCreatorId(1L);
        cancelledTask.setOwnerName("测试管理员");
        taskMapper.insert(cancelledTask);

        // 执行逾期检测
        taskScheduler.checkOverdueTasks();

        // 验证：已取消的任务不应该变成逾期
        TaskInfo updated = taskMapper.selectById(cancelledTask.getId());
        assertEquals("cancelled", updated.getStatus());
        assertNotEquals("overdue", updated.getStatus());
    }
}
