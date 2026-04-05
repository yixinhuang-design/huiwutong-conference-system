package com.conference.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.collaboration.entity.TaskDependency;
import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.mapper.TaskDependencyMapper;
import com.conference.collaboration.service.TaskDependencyService;
import com.conference.collaboration.service.TaskService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 任务依赖服务实现
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDependencyServiceImpl extends ServiceImpl<TaskDependencyMapper, TaskDependency>
        implements TaskDependencyService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final TaskService taskService;

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
    public TaskDependency addDependency(Long taskId, Long predecessorTaskId, String dependencyType) {
        // 检测循环依赖
        if (hasCyclicDependency(taskId, predecessorTaskId)) {
            throw new RuntimeException("存在循环依赖，无法添加");
        }

        TaskDependency dependency = new TaskDependency();
        dependency.setTenantId(getTenantId());
        dependency.setTaskId(taskId);
        dependency.setPredecessorTaskId(predecessorTaskId);
        dependency.setDependencyType(dependencyType != null ? dependencyType : "finish_to_start");
        dependency.setTimeOffsetMinutes(0);
        dependency.setIsBlocking(true);
        dependency.setCreateTime(LocalDateTime.now());
        dependency.setUpdateTime(LocalDateTime.now());

        this.save(dependency);
        log.info("任务依赖关系已添加: taskId={}, predecessorId={}", taskId, predecessorTaskId);
        return dependency;
    }

    @Override
    @Transactional
    public List<TaskDependency> addDependencies(Long taskId, List<Long> predecessorTaskIds) {
        List<TaskDependency> dependencies = new ArrayList<>();
        for (Long predecessorId : predecessorTaskIds) {
            try {
                TaskDependency dep = addDependency(taskId, predecessorId, "finish_to_start");
                dependencies.add(dep);
            } catch (Exception e) {
                log.warn("添加依赖失败: taskId={}, predecessorId={}", taskId, predecessorId);
            }
        }
        return dependencies;
    }

    @Override
    @Transactional
    public void removeDependency(Long taskId, Long predecessorTaskId) {
        LambdaQueryWrapper<TaskDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDependency::getTaskId, taskId)
               .eq(TaskDependency::getPredecessorTaskId, predecessorTaskId);
        this.remove(wrapper);
        log.info("任务依赖关系已移除: taskId={}, predecessorId={}", taskId, predecessorTaskId);
    }

    @Override
    public List<TaskDependency> getTaskDependencies(Long taskId) {
        LambdaQueryWrapper<TaskDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDependency::getTaskId, taskId);
        return this.list(wrapper);
    }

    @Override
    public boolean canStartTask(Long taskId) {
        List<TaskDependency> dependencies = getTaskDependencies(taskId);
        
        for (TaskDependency dep : dependencies) {
            if (!dep.getIsBlocking()) {
                continue;
            }

            TaskInfo predecessor = taskService.getById(dep.getPredecessorTaskId());
            if (predecessor == null) {
                continue;
            }

            // 检查前置任务是否完成
            if (!"completed".equals(predecessor.getStatus())) {
                log.info("任务{}无法开始，前置任务{}未完成", taskId, dep.getPredecessorTaskId());
                return false;
            }

            // 检查时间偏移
            if (dep.getTimeOffsetMinutes() != null && dep.getTimeOffsetMinutes() > 0) {
                LocalDateTime predecessorCompletedTime = predecessor.getUpdateTime();
                LocalDateTime canStartTime = predecessorCompletedTime.plusMinutes(dep.getTimeOffsetMinutes());
                if (LocalDateTime.now().isBefore(canStartTime)) {
                    log.info("任务{}无法开始，需等待时间偏移", taskId);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Map<String, Object> getDependencyGraph(Long taskId) {
        Map<String, Object> graph = new LinkedHashMap<>();
        
        // 获取当前任务
        TaskInfo task = taskService.getById(taskId);
        
        // 获取前置任务
        List<TaskDependency> predecessors = getTaskDependencies(taskId);
        List<Map<String, Object>> predecessorNodes = new ArrayList<>();
        for (TaskDependency dep : predecessors) {
            TaskInfo predTask = taskService.getById(dep.getPredecessorTaskId());
            if (predTask != null) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", predTask.getId());
                node.put("name", predTask.getTaskName());
                node.put("status", predTask.getStatus());
                node.put("type", dep.getDependencyType());
                predecessorNodes.add(node);
            }
        }

        // 获取后续任务
        LambdaQueryWrapper<TaskDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDependency::getPredecessorTaskId, taskId);
        List<TaskDependency> successors = this.list(wrapper);
        List<Map<String, Object>> successorNodes = new ArrayList<>();
        for (TaskDependency dep : successors) {
            TaskInfo succTask = taskService.getById(dep.getTaskId());
            if (succTask != null) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", succTask.getId());
                node.put("name", succTask.getTaskName());
                node.put("status", succTask.getStatus());
                node.put("type", dep.getDependencyType());
                successorNodes.add(node);
            }
        }

        graph.put("currentTask", task);
        graph.put("predecessors", predecessorNodes);
        graph.put("successors", successorNodes);
        graph.put("canStart", canStartTask(taskId));

        return graph;
    }

    @Override
    @Transactional
    public void onPredecessorCompleted(Long predecessorTaskId) {
        // 查找所有依赖此任务的后置任务
        LambdaQueryWrapper<TaskDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDependency::getPredecessorTaskId, predecessorTaskId);
        List<TaskDependency> dependencies = this.list(wrapper);

        for (TaskDependency dep : dependencies) {
            Long taskId = dep.getTaskId();
            
            // 检查后置任务是否可以开始
            if (canStartTask(taskId)) {
                TaskInfo task = taskService.getById(taskId);
                if (task != null && "pending".equals(task.getStatus())) {
                    // 更新任务状态为进行中
                    task.setStatus("in_progress");
                    task.setUpdateTime(LocalDateTime.now());
                    taskService.updateById(task);
                    log.info("任务{}已自动开始(前置任务完成)", taskId);
                }
            }
        }
    }

    @Override
    public boolean hasCyclicDependency(Long taskId, Long predecessorTaskId) {
        // 使用DFS检测循环依赖
        Set<Long> visited = new HashSet<>();
        return hasCycleDFS(predecessorTaskId, taskId, visited);
    }

    private boolean hasCycleDFS(Long currentId, Long targetId, Set<Long> visited) {
        if (currentId.equals(targetId)) {
            return true; // 找到循环
        }

        if (visited.contains(currentId)) {
            return false; // 已访问过，避免重复
        }

        visited.add(currentId);

        // 递归检查当前任务的所有前置任务
        LambdaQueryWrapper<TaskDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskDependency::getTaskId, currentId);
        List<TaskDependency> dependencies = this.list(wrapper);

        for (TaskDependency dep : dependencies) {
            if (hasCycleDFS(dep.getPredecessorTaskId(), targetId, visited)) {
                return true;
            }
        }

        return false;
    }
}
