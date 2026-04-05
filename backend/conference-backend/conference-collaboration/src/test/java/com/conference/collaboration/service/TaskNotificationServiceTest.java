package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TaskNotificationService 单元测试
 * 验证通知服务的各场景：分派、催办、完成、取消、逾期
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("任务通知服务测试")
class TaskNotificationServiceTest {

    @Mock
    private TaskAssigneeService assigneeService;

    @Mock
    private AliyunSmsService aliyunSmsService;

    @Mock
    private UniPushService uniPushService;

    @InjectMocks
    private TaskNotificationService notificationService;

    private TaskInfo sampleTask;
    private List<TaskAssignee> sampleAssignees;

    @BeforeEach
    void setUp() {
        sampleTask = new TaskInfo();
        sampleTask.setId(1001L);
        sampleTask.setTaskName("场地巡检");
        sampleTask.setDeadline(LocalDateTime.of(2026, 4, 10, 18, 0));
        sampleTask.setPriority("high");
        sampleTask.setCreatorId(100L);

        TaskAssignee a1 = new TaskAssignee();
        a1.setId(1L);
        a1.setTaskId(1001L);
        a1.setUserId(201L);
        a1.setUserName("张三");
        a1.setStatus("pending");

        TaskAssignee a2 = new TaskAssignee();
        a2.setId(2L);
        a2.setTaskId(1001L);
        a2.setUserId(202L);
        a2.setUserName("李四");
        a2.setStatus("pending");

        sampleAssignees = List.of(a1, a2);
    }

    @Test
    @DisplayName("S4: 任务分派通知 - 应调用SMS和Push发送")
    void notifyTaskAssigned_shouldSendSmsAndPush() {
        // Arrange
        List<Map<String, String>> contacts = List.of(
                Map.of("phone", "13800138001", "clientId", "cid_001"),
                Map.of("phone", "13800138002", "clientId", "cid_002")
        );
        when(assigneeService.getAssigneeContacts(sampleAssignees)).thenReturn(contacts);

        // Act
        notificationService.notifyTaskAssigned(sampleTask, sampleAssignees);

        // Assert
        verify(aliyunSmsService, times(2)).sendSms(anyString(), anyString());
        verify(uniPushService, times(2)).pushToSingle(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("S4: 催办通知 - 应只通知未完成的执行人")
    void notifyTaskUrge_shouldNotifyPendingAssigneesOnly() {
        // Arrange
        List<Map<String, String>> contacts = List.of(
                Map.of("phone", "13800138001", "clientId", "cid_001")
        );
        List<TaskAssignee> pending = List.of(sampleAssignees.get(0));
        when(assigneeService.getAssigneeContacts(pending)).thenReturn(contacts);

        // Act
        notificationService.notifyTaskUrge(sampleTask, pending);

        // Assert
        verify(aliyunSmsService, times(1)).sendSms(eq("13800138001"), contains("到期"));
        verify(uniPushService, times(1)).pushToSingle(eq("cid_001"), anyString(), anyString());
    }

    @Test
    @DisplayName("M1: 任务完成通知 - 应通知任务创建者")
    void notifyTaskCompleted_shouldNotifyCreator() {
        // Arrange
        Map<String, String> ownerContact = Map.of("phone", "13800100000", "clientId", "cid_owner");
        when(assigneeService.getUserContact(100L)).thenReturn(ownerContact);

        // Act
        notificationService.notifyTaskCompleted(sampleTask, sampleAssignees.get(0));

        // Assert
        verify(aliyunSmsService).sendSms(eq("13800100000"), contains("完成"));
        verify(uniPushService).pushToSingle(eq("cid_owner"), anyString(), anyString());
    }

    @Test
    @DisplayName("任务取消通知 - 应通知所有执行人")
    void notifyTaskCancelled_shouldNotifyAllAssignees() {
        // Arrange
        List<Map<String, String>> contacts = List.of(
                Map.of("phone", "13800138001"),
                Map.of("clientId", "cid_002")
        );
        when(assigneeService.getAssigneeContacts(sampleAssignees)).thenReturn(contacts);

        // Act
        notificationService.notifyTaskCancelled(sampleTask, sampleAssignees, "会议延期");

        // Assert
        verify(aliyunSmsService, times(1)).sendSms(eq("13800138001"), contains("取消"));
        verify(uniPushService, times(1)).pushToSingle(eq("cid_002"), anyString(), contains("取消"));
    }

    @Test
    @DisplayName("逾期通知 - 应通知待处理执行人")
    void notifyTaskOverdue_shouldNotifyPendingAssignees() {
        // Arrange
        List<Map<String, String>> contacts = List.of(
                Map.of("phone", "13800138001", "clientId", "cid_001")
        );
        when(assigneeService.getAssigneeContacts(sampleAssignees)).thenReturn(contacts);

        // Act
        notificationService.notifyTaskOverdue(sampleTask, sampleAssignees);

        // Assert
        verify(aliyunSmsService).sendSms(anyString(), contains("逾期"));
        verify(uniPushService).pushToSingle(anyString(), anyString(), contains("逾期"));
    }

    @Test
    @DisplayName("SMS发送异常不应中断整个通知流程")
    void sendToContacts_smsFailure_shouldNotBlockPush() {
        // Arrange
        List<Map<String, String>> contacts = List.of(
                Map.of("phone", "13800138001", "clientId", "cid_001")
        );
        when(assigneeService.getAssigneeContacts(sampleAssignees)).thenReturn(contacts);
        doThrow(new RuntimeException("SMS网络异常")).when(aliyunSmsService).sendSms(anyString(), anyString());

        // Act - should not throw
        notificationService.notifyTaskAssigned(sampleTask, sampleAssignees);

        // Assert - push should still be called
        verify(uniPushService).pushToSingle(eq("cid_001"), anyString(), anyString());
    }

    @Test
    @DisplayName("空联系方式列表 - 不应抛异常")
    void notifyWithEmptyContacts_shouldNotThrow() {
        // Arrange
        when(assigneeService.getAssigneeContacts(anyList())).thenReturn(Collections.emptyList());

        // Act & Assert - no exception
        notificationService.notifyTaskAssigned(sampleTask, sampleAssignees);

        verify(aliyunSmsService, never()).sendSms(anyString(), anyString());
        verify(uniPushService, never()).pushToSingle(anyString(), anyString(), anyString());
    }
}
