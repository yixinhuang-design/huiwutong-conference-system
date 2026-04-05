package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 任务通知服务
 * 负责任务相关的所有通知发送
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskNotificationService {

    private final TaskAssigneeService assigneeService;
    private final AliyunSmsService aliyunSmsService;
    private final UniPushService uniPushService;

    /**
     * 通知任务分派
     * @param task 任务信息
     * @param assignees 执行人列表
     */
    public void notifyTaskAssigned(TaskInfo task, List<TaskAssignee> assignees) {
        String title = "新任务通知";
        String content = String.format(
            "您有一个新任务：%s，截止时间：%s，优先级：%s",
            task.getTaskName(),
            task.getDeadline() != null ? task.getDeadline().toString() : "无",
            getPriorityText(task.getPriority())
        );

        List<Map<String, String>> contacts = assigneeService.getAssigneeContacts(assignees);
        sendToContacts(contacts, title, content);

        log.info("任务分派通知已发送: taskId={}, 执行人数={}", task.getId(), assignees.size());
    }

    /**
     * 通知任务催办
     * @param task 任务信息
     * @param pendingAssignees 未完成的执行人
     */
    public void notifyTaskUrge(TaskInfo task, List<TaskAssignee> pendingAssignees) {
        String title = "任务催办提醒";
        String content = String.format(
            "您的任务【%s】即将到期，请尽快完成！截止时间：%s",
            task.getTaskName(),
            task.getDeadline() != null ? task.getDeadline().toString() : "无"
        );

        List<Map<String, String>> contacts = assigneeService.getAssigneeContacts(pendingAssignees);
        sendToContacts(contacts, title, content);

        log.info("任务催办通知已发送: taskId={}, 催办人数={}", task.getId(), pendingAssignees.size());
    }

    /**
     * 通知任务完成
     * @param task 任务信息
     * @param assignee 完成者
     */
    public void notifyTaskCompleted(TaskInfo task, TaskAssignee assignee) {
        String title = "任务完成通知";
        String content = String.format(
            "%s已完成任务【%s】",
            assignee.getUserName(),
            task.getTaskName()
        );

        if (task.getCreatorId() != null) {
            Map<String, String> ownerContact = assigneeService.getUserContact(task.getCreatorId());
            if (ownerContact.containsKey("phone")) {
                aliyunSmsService.sendSms(ownerContact.get("phone"), content);
            }
            if (ownerContact.containsKey("clientId")) {
                uniPushService.pushToSingle(ownerContact.get("clientId"), title, content);
            }
        }

        log.info("任务完成通知已发送: taskId={}, 完成者={}", task.getId(), assignee.getUserName());
    }

    /**
     * 通知任务变更
     * @param task 任务信息
     * @param changeType 变更类型 (deadline_update, priority_update, etc.)
     * @param changeDetail 变更详情
     */
    public void notifyTaskChanged(TaskInfo task, List<TaskAssignee> allAssignees, String changeType, String changeDetail) {
        String title = "任务变更通知";
        String content = String.format(
            "您的任务【%s】有变更：%s",
            task.getTaskName(),
            changeDetail
        );

        List<Map<String, String>> contacts = assigneeService.getAssigneeContacts(allAssignees);

        contacts.stream()
            .filter(m -> m.containsKey("clientId"))
            .forEach(m -> uniPushService.pushToSingle(m.get("clientId"), title, content));

        log.info("任务变更通知已发送: taskId={}, 变更类型={}", task.getId(), changeType);
    }

    /**
     * 通知任务取消
     * @param task 任务信息
     * @param reason 取消原因
     */
    public void notifyTaskCancelled(TaskInfo task, List<TaskAssignee> allAssignees, String reason) {
        String title = "任务取消通知";
        String content = String.format(
            "您的任务【%s】已被取消。原因：%s",
            task.getTaskName(),
            reason != null && !reason.isEmpty() ? reason : "无"
        );

        List<Map<String, String>> contacts = assigneeService.getAssigneeContacts(allAssignees);
        sendToContacts(contacts, title, content);
        log.info("任务取消通知已发送: taskId={}", task.getId());
    }

    /**
     * 通知任务逾期
     * @param task 任务信息
     */
    public void notifyTaskOverdue(TaskInfo task, List<TaskAssignee> pendingAssignees) {
        String title = "任务逾期提醒";
        String content = String.format(
            "您的任务【%s】已逾期，请立即处理！",
            task.getTaskName()
        );

        List<Map<String, String>> contacts = assigneeService.getAssigneeContacts(pendingAssignees);
        sendToContacts(contacts, title, content);
        log.info("任务逾期通知已发送: taskId={}", task.getId());
    }

    /**
     * 获取优先级文本
     */
    private String getPriorityText(String priority) {
        if (priority == null) return "中";
        return switch (priority) {
            case "low" -> "低";
            case "medium" -> "中";
            case "high" -> "高";
            case "urgent" -> "紧急";
            default -> "中";
        };
    }

    // ==================== 统一发送方法 ====================

    private void sendToContacts(List<Map<String, String>> contacts, String title, String content) {
        contacts.stream()
            .filter(m -> m.containsKey("phone"))
            .forEach(m -> {
                try {
                    aliyunSmsService.sendSms(m.get("phone"), content);
                } catch (Exception e) {
                    log.error("短信发送失败: phone={}", m.get("phone"), e);
                }
            });

        contacts.stream()
            .filter(m -> m.containsKey("clientId"))
            .forEach(m -> {
                try {
                    uniPushService.pushToSingle(m.get("clientId"), title, content);
                } catch (Exception e) {
                    log.error("推送发送失败: clientId={}", m.get("clientId"), e);
                }
            });
    }
}
