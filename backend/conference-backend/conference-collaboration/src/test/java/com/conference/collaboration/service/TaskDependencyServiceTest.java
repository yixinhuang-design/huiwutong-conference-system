package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskDependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务依赖服务测试
 * @author AI Executive
 * @date 2026-04-02
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TaskDependencyServiceTest {

    @Autowired
    private TaskDependencyService dependencyService;

    @Autowired
    private TaskService taskService;

    private TaskInfo taskA;
    private TaskInfo taskB;
    private TaskInfo taskC;

    @BeforeEach
    public void setUp() {
        // 创建测试任务
        taskA = createTestTask("任务A", "pending");
        taskB = createTestTask("任务B", "pending");
        taskC = createTestTask("任务C", "pending");
    }

    @Test
    public void testAddDependency() {
        // 添加依赖: A -> B
        TaskDependency dependency = dependencyService.addDependency(
            taskB.getId(),
            taskA.getId(),
            "finish_to_start"
        );

        assertNotNull(dependency);
        assertEquals(taskB.getId(), dependency.getTaskId());
        assertEquals(taskA.getId(), dependency.getPredecessorTaskId());
        assertEquals("finish_to_start", dependency.getDependencyType());
        assertTrue(dependency.getIsBlocking());
    }

    @Test
    public void testAddMultipleDependencies() {
        // 添加多个依赖: A -> C, B -> C
        List<TaskDependency> dependencies = dependencyService.addDependencies(
            taskC.getId(),
            List.of(taskA.getId(), taskB.getId())
        );

        assertNotNull(dependencies);
        assertEquals(2, dependencies.size());

        for (TaskDependency dep : dependencies) {
            assertEquals(taskC.getId(), dep.getTaskId());
            assertEquals("finish_to_start", dep.getDependencyType());
        }
    }

    @Test
    public void testRemoveDependency() {
        // 先添加依赖
        dependencyService.addDependency(taskB.getId(), taskA.getId(), "finish_to_start");

        // 移除依赖
        dependencyService.removeDependency(taskB.getId(), taskA.getId());

        // 验证移除
        List<TaskDependency> dependencies = dependencyService.getTaskDependencies(taskB.getId());
        assertTrue(dependencies.isEmpty() || dependencies.stream()
            .noneMatch(d -> d.getPredecessorTaskId().equals(taskA.getId())));
    }

    @Test
    public void testGetTaskDependencies() {
        // 添加依赖: A -> C, B -> C
        dependencyService.addDependencies(taskC.getId(), List.of(taskA.getId(), taskB.getId()));

        // 获取C的所有依赖
        List<TaskDependency> dependencies = dependencyService.getTaskDependencies(taskC.getId());

        assertNotNull(dependencies);
        assertEquals(2, dependencies.size());
    }

    @Test
    public void testCanStartTaskWithoutDependencies() {
        // 任务A没有依赖，应该可以开始
        boolean canStart = dependencyService.canStartTask(taskA.getId());
        assertTrue(canStart);
    }

    @Test
    public void testCanStartTaskWithCompletedPredecessor() {
        // 添加依赖: A -> B
        dependencyService.addDependency(taskB.getId(), taskA.getId(), "finish_to_start");

        // 完成任务A
        taskA.setStatus("completed");
        taskService.updateById(taskA);

        // 任务B应该可以开始
        boolean canStart = dependencyService.canStartTask(taskB.getId());
        assertTrue(canStart);
    }

    @Test
    public void testCanStartTaskWithPendingPredecessor() {
        // 添加依赖: A -> B
        dependencyService.addDependency(taskB.getId(), taskA.getId(), "finish_to_start");

        // 任务A未完成，任务B不应该可以开始
        boolean canStart = dependencyService.canStartTask(taskB.getId());
        assertFalse(canStart);
    }

    @Test
    public void testHasCyclicDependency() {
        // 添加依赖: A -> B
        dependencyService.addDependency(taskB.getId(), taskA.getId(), "finish_to_start");

        // 尝试添加循环依赖: B -> A
        boolean hasCycle = dependencyService.hasCyclicDependency(taskA.getId(), taskB.getId());
        assertTrue(hasCycle);
    }

    @Test
    public void testNoCyclicDependency() {
        // A -> B, A -> C 不应该产生循环
        dependencyService.addDependency(taskB.getId(), taskA.getId(), "finish_to_start");
        dependencyService.addDependency(taskC.getId(), taskA.getId(), "finish_to_start");

        // B -> C 不应该产生循环
        boolean hasCycle = dependencyService.hasCyclicDependency(taskC.getId(), taskB.getId());
        assertFalse(hasCycle);
    }

    @Test
    public void testAddCyclicDependencyShouldFail() {
        // 添加依赖: A -> B
        dependencyService.addDependency(taskB.getId(), taskA.getId(), "finish_to_start");

        // 尝试添加循环依赖应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            dependencyService.addDependency(taskA.getId(), taskB.getId(), "finish_to_start");
        });
    }

    @Test
    public void testOnPredecessorCompleted() {
        // 添加依赖: A -> B
        dependencyService.addDependency(taskB.getId(), taskA.getId(), "finish_to_start");

        // 完成任务A
        taskA.setStatus("completed");
        taskService.updateById(taskA);

        // 触发前置任务完成事件
        dependencyService.onPredecessorCompleted(taskA.getId());

        // 任务B应该自动启动
        TaskInfo updatedTaskB = taskService.getById(taskB.getId());
        assertEquals("in_progress", updatedTaskB.getStatus());
    }

    @Test
    public void testGetDependencyGraph() {
        // 构建依赖链: A -> B -> C
        dependencyService.addDependency(taskB.getId(), taskA.getId(), "finish_to_start");
        dependencyService.addDependency(taskC.getId(), taskB.getId(), "finish_to_start");

        // 获取任务B的依赖图
        Map<String, Object> graph = dependencyService.getDependencyGraph(taskB.getId());

        assertNotNull(graph);
        assertNotNull(graph.get("currentTask"));
        assertNotNull(graph.get("predecessors"));
        assertNotNull(graph.get("successors"));
        assertNotNull(graph.get("canStart"));

        // 验证前置任务
        List<?> predecessors = (List<?>) graph.get("predecessors");
        assertEquals(1, predecessors.size());

        // 验证后续任务
        List<?> successors = (List<?>) graph.get("successors");
        assertEquals(1, successors.size());
    }

    @Test
    public void testComplexDependencyChain() {
        // 构建复杂依赖: A -> C, B -> C
        dependencyService.addDependency(taskC.getId(), taskA.getId(), "finish_to_start");
        dependencyService.addDependency(taskC.getId(), taskB.getId(), "finish_to_start");

        // 完成任务A
        taskA.setStatus("completed");
        taskService.updateById(taskA);

        // 任务C还不应该可以开始 (B未完成)
        boolean canStart = dependencyService.canStartTask(taskC.getId());
        assertFalse(canStart);

        // 完成任务B
        taskB.setStatus("completed");
        taskService.updateById(taskB);

        // 任务C现在应该可以开始
        canStart = dependencyService.canStartTask(taskC.getId());
        assertTrue(canStart);
    }

    // 辅助方法
    private TaskInfo createTestTask(String name, String status) {
        TaskInfo task = new TaskInfo();
        task.setTaskName(name);
        task.setTaskType("custom");
        task.setCategory("custom");
        task.setCompletionMethod("text_image");
        task.setDescription("测试任务");
        task.setPriority("medium");
        task.setStatus(status);
        task.setProgress(0);
        task.setCreatorId(1L);
        task.setOwnerName("测试管理员");
        taskService.save(task);
        return task;
    }
}
