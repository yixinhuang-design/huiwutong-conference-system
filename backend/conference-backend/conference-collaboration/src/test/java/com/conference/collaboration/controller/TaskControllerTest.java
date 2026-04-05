package com.conference.collaboration.controller;

import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 任务 Controller 完整 API 测试
 * @author AI Executive
 * @date 2026-04-02
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskInfo testTask;
    private List<TaskAssignee> testAssignees;

    @BeforeEach
    public void setUp() {
        testTask = new TaskInfo();
        testTask.setConferenceId(1L);
        testTask.setTaskName("测试任务");
        testTask.setTaskType("custom");
        testTask.setCategory("custom");
        testTask.setCompletionMethod("text_image");
        testTask.setDescription("测试描述");
        testTask.setPriority("high");
        testTask.setStatus("pending");
        testTask.setProgress(0);
        testTask.setDeadline(LocalDateTime.now().plusDays(7));
        testTask.setCreatorId(1L);
        testTask.setOwnerName("测试管理员");

        testAssignees = new ArrayList<>();
        TaskAssignee assignee1 = new TaskAssignee();
        assignee1.setUserId(100L);
        assignee1.setUserName("张三");
        assignee1.setRole("executor");
        assignee1.setIsMain(1);
        testAssignees.add(assignee1);

        TaskAssignee assignee2 = new TaskAssignee();
        assignee2.setUserId(101L);
        assignee2.setUserName("李四");
        assignee2.setRole("assistant");
        assignee2.setIsMain(0);
        testAssignees.add(assignee2);
    }

    @Test
    public void testCreateTask() throws Exception {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("conferenceId", 1);
        taskData.put("taskName", "API测试任务");
        taskData.put("taskType", "custom");
        taskData.put("category", "custom");
        taskData.put("completionMethod", "text_image");
        taskData.put("description", "通过API创建的任务");
        taskData.put("priority", "medium");
        taskData.put("deadline", "2026-12-31 23:59:59");
        taskData.put("creatorId", 1);
        taskData.put("ownerName", "API测试");
        taskData.put("assignees", testAssignees);

        mockMvc.perform(post("/api/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskName").value("API测试任务"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    public void testGetTaskList() throws Exception {
        // 先创建一些任务
        taskService.createTask(testTask, testAssignees);

        mockMvc.perform(get("/api/task/list")
                .param("conferenceId", "1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    public void testGetTaskDetail() throws Exception {
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        mockMvc.perform(get("/api/task/{taskId}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.task").isMap())
                .andExpect(jsonPath("$.data.assignees").isArray())
                .andExpect(jsonPath("$.data.logs").isArray());
    }

    @Test
    public void testUpdateTask() throws Exception {
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("taskName", "更新后的任务名");
        updateData.put("priority", "urgent");
        updateData.put("description", "更新后的描述");

        mockMvc.perform(put("/api/task/{taskId}", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskName").value("更新后的任务名"))
                .andExpect(jsonPath("$.data.priority").value("urgent"));
    }

    @Test
    public void testAssignTask() throws Exception {
        TaskInfo created = taskService.createTask(testTask, new ArrayList<>());

        mockMvc.perform(post("/api/task/{taskId}/assign", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAssignees)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(containsString("分配")));
    }

    @Test
    public void testSubmitTask() throws Exception {
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        Map<String, Object> submitData = new HashMap<>();
        submitData.put("userId", 100);
        submitData.put("content", "我已完成任务");
        submitData.put("images", null);
        submitData.put("location", null);

        mockMvc.perform(post("/api/task/{taskId}/submit", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(containsString("提交")));
    }

    @Test
    public void testCompleteTask() throws Exception {
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        Map<String, Object> completeData = new HashMap<>();
        completeData.put("operatorId", 1);
        completeData.put("operatorName", "测试管理员");
        completeData.put("remark", "测试完成");

        mockMvc.perform(post("/api/task/{taskId}/complete", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(containsString("完成")));
    }

    @Test
    public void testCancelTask() throws Exception {
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("operatorId", 1);
        cancelData.put("operatorName", "测试管理员");
        cancelData.put("remark", "测试取消");

        mockMvc.perform(post("/api/task/{taskId}/cancel", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(containsString("取消")));
    }

    @Test
    public void testUrgeTask() throws Exception {
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        Map<String, Object> urgeData = new HashMap<>();
        urgeData.put("operatorId", 1);
        urgeData.put("operatorName", "测试管理员");

        mockMvc.perform(post("/api/task/{taskId}/urge", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(urgeData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(containsString("催办")));
    }

    @Test
    public void testGetTaskLogs() throws Exception {
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        mockMvc.perform(get("/api/task/{taskId}/logs", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetTaskStats() throws Exception {
        mockMvc.perform(get("/api/task/stats")
                .param("conferenceId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.pending").isNumber())
                .andExpect(jsonPath("$.data.completed").isNumber());
    }

    @Test
    public void testDeleteTask() throws Exception {
        TaskInfo created = taskService.createTask(testTask, testAssignees);

        mockMvc.perform(delete("/api/task/{taskId}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(containsString("删除")));
    }

    @Test
    public void testFilterTasksByStatus() throws Exception {
        taskService.createTask(testTask, testAssignees);

        mockMvc.perform(get("/api/task/list")
                .param("conferenceId", "1")
                .param("status", "pending")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    public void testFilterTasksByCategory() throws Exception {
        mockMvc.perform(get("/api/task/list")
                .param("conferenceId", "1")
                .param("category", "custom")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }
}
