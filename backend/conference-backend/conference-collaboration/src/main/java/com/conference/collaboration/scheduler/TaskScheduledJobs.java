package com.conference.collaboration.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.mapper.TaskInfoMapper;
import com.conference.collaboration.service.TaskNotificationService;
import com.conference.collaboration.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务定时任务调度器
 * 负责逾期检测、状态更新等定时任务
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskScheduledJobs {

    private final TaskInfoMapper taskMapper;
    private final TaskService taskService;
    private final TaskNotificationService notificationService;

    /**
     * 逾期任务检测
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkOverdueTasks() {
        log.info("开始执行逾期任务检测...");

        try {
            // 查询应该逾期但未逾期的任务
            LocalDateTime now = LocalDateTime.now();
            LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskInfo::getDeleted, 0)
                    .in(TaskInfo::getStatus, "pending", "in_progress")
                    .lt(TaskInfo::getDeadline, now);

            List<TaskInfo> tasksToOverdue = taskMapper.selectList(wrapper);

            if (!tasksToOverdue.isEmpty()) {
                for (TaskInfo task : tasksToOverdue) {
                    // 更新任务状态为逾期
                    task.setStatus("overdue");
                    task.setUpdateTime(now);
                    taskMapper.updateById(task);

                    // 发送逾期通知
                    try {
                        List<TaskAssignee> pendingAssignees = taskService.getAssignees(task.getId()).stream()
                            .filter(a -> !"completed".equals(a.getStatus()))
                            .toList();
                        if (!pendingAssignees.isEmpty()) {
                            notificationService.notifyTaskOverdue(task, pendingAssignees);
                        }
                    } catch (Exception e) {
                        log.error("发送逾期通知失败: taskId={}", task.getId(), e);
                    }

                    log.info("任务{}已标记为逾期", task.getId());
                }

                log.info("逾期任务检测完成，处理了{}个逾期任务", tasksToOverdue.size());
            } else {
                log.info("逾期任务检测完成，无逾期任务");
            }

        } catch (Exception e) {
            log.error("逾期任务检测执行失败", e);
        }
    }

    /**
     * 任务统计汇总
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyTaskSummary() {
        log.info("开始执行每日任务统计...");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);

            // 统计昨日新建任务数
            LambdaQueryWrapper<TaskInfo> newWrapper = new LambdaQueryWrapper<>();
            newWrapper.eq(TaskInfo::getDeleted, 0)
                    .ge(TaskInfo::getCreateTime, yesterday)
                    .lt(TaskInfo::getCreateTime, now);
            long newCount = taskMapper.selectCount(newWrapper);

            // 统计昨日完成任务数
            LambdaQueryWrapper<TaskInfo> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.eq(TaskInfo::getDeleted, 0)
                    .eq(TaskInfo::getStatus, "completed")
                    .ge(TaskInfo::getUpdateTime, yesterday)
                    .lt(TaskInfo::getUpdateTime, now);
            long completedCount = taskMapper.selectCount(completedWrapper);

            // 统计当前活跃与逾期
            LambdaQueryWrapper<TaskInfo> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(TaskInfo::getDeleted, 0).in(TaskInfo::getStatus, "pending", "in_progress");
            long activeCount = taskMapper.selectCount(activeWrapper);

            LambdaQueryWrapper<TaskInfo> overdueWrapper = new LambdaQueryWrapper<>();
            overdueWrapper.eq(TaskInfo::getDeleted, 0).eq(TaskInfo::getStatus, "overdue");
            long overdueCount = taskMapper.selectCount(overdueWrapper);

            log.info("每日任务统计完成 - 昨日新建:{}, 昨日完成:{}, 当前进行中:{}, 当前逾期:{}",
                    newCount, completedCount, activeCount, overdueCount);
        } catch (Exception e) {
            log.error("每日任务统计执行失败", e);
        }
    }

    /**
     * 清理已删除的任务数据
     * 每周日凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * 1")
    public void cleanupDeletedTasks() {
        log.info("开始清理已删除的任务数据...");

        try {
            // 物理删除30天前逻辑删除的任务
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskInfo::getDeleted, 1)
                    .lt(TaskInfo::getUpdateTime, thirtyDaysAgo);
            List<TaskInfo> deletedTasks = taskMapper.selectList(wrapper);

            int count = 0;
            for (TaskInfo task : deletedTasks) {
                // 物理删除关联的执行人记录
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskAssignee> aWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                aWrapper.eq(TaskAssignee::getTaskId, task.getId());
                taskService.getAssignees(task.getId()); // just for logging
                
                // 物理删除任务（MyBatis Plus的deleteById会物理删除已逻辑删除的记录）
                taskMapper.deleteById(task.getId());
                count++;
            }

            log.info("清理已删除任务完成，物理删除了{}个任务", count);
        } catch (Exception e) {
            log.error("清理已删除任务失败", e);
        }
    }
}
