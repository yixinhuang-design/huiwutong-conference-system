package com.conference.collaboration.controller;

import com.conference.collaboration.entity.TaskTemplate;
import com.conference.collaboration.service.TaskTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 任务模板 Controller 测试
 * @author AI Executive
 * @date 2026-04-02
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskTemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetSystemTemplates() throws Exception {
        mockMvc.perform(get("/api/task-template/system")
                .param("category", "venue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetTemplateList() throws Exception {
        mockMvc.perform(get("/api/task-template/list")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    public void testCreateTemplate() throws Exception {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("templateName", "测试模板");
        templateData.put("taskType", "custom");
        templateData.put("category", "custom");
        templateData.put("completionMethod", "text_image");
        templateData.put("description", "测试描述");
        templateData.put("priority", "medium");
        templateData.put("config", "{\"requireText\":true}");
        templateData.put("icon", "fa-test");

        mockMvc.perform(post("/api/task-template/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(templateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateName").value("测试模板"));
    }

    @Test
    public void testUpdateTemplate() throws Exception {
        // 先创建模板
        TaskTemplate template = new TaskTemplate();
        template.setTemplateName("原始名称");
        template.setTaskType("custom");
        template.setCategory("custom");
        template.setCompletionMethod("text_image");
        template.setDescription("原始描述");
        template.setPriority("medium");
        TaskTemplate created = templateService.createTemplate(template);

        // 更新模板
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("templateName", "更新后名称");
        updateData.put("description", "更新后描述");
        updateData.put("priority", "high");

        mockMvc.perform(put("/api/task-template/{templateId}", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateName").value("更新后名称"));
    }

    @Test
    public void testDeleteTemplate() throws Exception {
        // 创建自定义模板
        TaskTemplate template = new TaskTemplate();
        template.setTemplateName("待删除模板");
        template.setTaskType("custom");
        template.setCategory("custom");
        template.setCompletionMethod("text_image");
        template.setDescription("测试");
        template.setPriority("medium");
        TaskTemplate created = templateService.createTemplate(template);

        // 删除模板
        mockMvc.perform(delete("/api/task-template/{templateId}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(containsString("删除")));
    }

    @Test
    public void testApplyTemplate() throws Exception {
        // 获取系统模板
        List<TaskTemplate> templates = templateService.getSystemTemplates("venue");
        if (!templates.isEmpty()) {
            TaskTemplate template = templates.get(0);

            Map<String, Object> taskData = new HashMap<>();
            taskData.put("conferenceId", 1);
            taskData.put("taskName", "应用模板的任务");
            taskData.put("deadline", "2026-12-31 23:59:59");
            taskData.put("creatorId", 1);
            taskData.put("ownerName", "测试管理员");

            Map<String, Object> assignee1 = new HashMap<>();
            assignee1.put("userId", 100);
            assignee1.put("userName", "张三");
            assignee1.put("role", "executor");
            assignee1.put("isMain", 1);

            taskData.put("assignees", List.of(assignee1));

            mockMvc.perform(post("/api/task-template/{templateId}/apply", template.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(taskData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.task").isMap())
                    .andExpect(jsonPath("$.data.templateName").value(template.getTemplateName()));
        }
    }

    @Test
    public void testGetTemplateDetail() throws Exception {
        // 创建模板
        TaskTemplate template = new TaskTemplate();
        template.setTemplateName("详情测试模板");
        template.setTaskType("custom");
        template.setCategory("custom");
        template.setCompletionMethod("text_image");
        template.setDescription("测试详情");
        template.setPriority("medium");
        TaskTemplate created = templateService.createTemplate(template);

        mockMvc.perform(get("/api/task-template/{templateId}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.template").isMap())
                .andExpect(jsonPath("$.data.template.templateName").value("详情测试模板"));
    }
}
