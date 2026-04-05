package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskDependency;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 任务依赖服务接口
 * @author AI Executive
 * @date 2026-04-01
 */
public interface TaskDependencyService extends IService<TaskDependency> {

    /**
     * 添加任务依赖
     * @param taskId 后置任务ID
     * @param predecessorTaskId 前置任务ID
     * @param dependencyType 依赖类型
     * @return 创建的依赖关系
     */
    TaskDependency addDependency(Long taskId, Long predecessorTaskId, String dependencyType);

    /**
     * 批量添加任务依赖
     * @param taskId 后置任务ID
     * @param predecessorTaskIds 前置任务ID列表
     * @return 创建的依赖关系列表
     */
    List<TaskDependency> addDependencies(Long taskId, List<Long> predecessorTaskIds);

    /**
     * 移除任务依赖
     * @param taskId 任务ID
     * @param predecessorTaskId 前置任务ID
     */
    void removeDependency(Long taskId, Long predecessorTaskId);

    /**
     * 获取任务的所有依赖
     * @param taskId 任务ID
     * @return 依赖关系列表
     */
    List<TaskDependency> getTaskDependencies(Long taskId);

    /**
     * 检查任务是否可以开始
     * @param taskId 任务ID
     * @return 是否可以开始
     */
    boolean canStartTask(Long taskId);

    /**
     * 获取任务的依赖链路图
     * @param taskId 任务ID
     * @return 依赖链路图数据
     */
    Map<String, Object> getDependencyGraph(Long taskId);

    /**
     * 前置任务完成后触发
     * @param predecessorTaskId 前置任务ID
     */
    void onPredecessorCompleted(Long predecessorTaskId);

    /**
     * 检测循环依赖
     * @param taskId 后置任务ID
     * @param predecessorTaskId 前置任务ID
     * @return 是否存在循环依赖
     */
    boolean hasCyclicDependency(Long taskId, Long predecessorTaskId);
}
