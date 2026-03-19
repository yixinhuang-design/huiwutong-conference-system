package com.conference.collaboration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskLog;

import java.util.List;
import java.util.Map;

public interface TaskService extends IService<TaskInfo> {

    /** 创建任务 */
    TaskInfo createTask(TaskInfo task, List<TaskAssignee> assignees);

    /** 更新任务 */
    TaskInfo updateTask(TaskInfo task);

    /** 获取任务详情(含执行人) */
    Map<String, Object> getTaskDetail(Long taskId);

    /** 按会议查询任务列表(分页) */
    Page<TaskInfo> listByConference(Long conferenceId, String status, String category, int page, int size);

    /** 获取我的任务(作为执行人) */
    Page<Map<String, Object>> listMyTasks(Long userId, String status, int page, int size);

    /** 分配任务执行人 */
    void assignTask(Long taskId, List<TaskAssignee> assignees);

    /** 提交任务 */
    void submitTask(Long taskId, Long userId, String content, String images, String location);

    /** 删除任务(含执行人和日志) */
    void deleteTask(Long taskId);

    /** 取消任务 */
    void cancelTask(Long taskId, Long operatorId, String operatorName, String remark);

    /** 完成任务 */
    void completeTask(Long taskId, Long operatorId, String operatorName, String remark);

    /** 获取任务执行人列表 */
    List<TaskAssignee> getAssignees(Long taskId);

    /** 获取任务日志 */
    List<TaskLog> getTaskLogs(Long taskId);

    /** 获取任务统计 */
    Map<String, Object> getTaskStats(Long conferenceId);

    /** 催办任务 */
    void urgeTask(Long taskId, Long operatorId, String operatorName);
}
