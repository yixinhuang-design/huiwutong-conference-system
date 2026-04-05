package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskTemplate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务模板服务测试
 * @author AI Executive
 * @date 2026-04-02
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TaskTemplateServiceTest {

    @Autowired
    private TaskTemplateService templateService;

    @Autowired
    private TaskService taskService;

    @Test
    public void testGetSystemTemplates() {
        // 测试获取系统预设模板
        List<TaskTemplate> venueTemplates = templateService.getSystemTemplates("venue");
        assertNotNull(venueTemplates);
        assertTrue(venueTemplates.size() >= 2, "至少应有2个会场任务模板");

        List<TaskTemplate> studentTemplates = templateService.getSystemTemplates("student");
        assertNotNull(studentTemplates);
        assertTrue(studentTemplates.size() >= 3, "至少应有3个学员任务模板");

        // 验证系统模板标识
        for (TaskTemplate template : venueTemplates) {
            assertEquals(1, template.getIsSystem(), "应该是系统模板");
            assertNotNull(template.getTemplateName(), "模板名称不能为空");
            assertNotNull(template.getTaskType(), "任务类型不能为空");
            assertNotNull(template.getCompletionMethod(), "完成方式不能为空");
        }
    }

    @Test
    public void testCreateTemplate() {
        // 测试创建自定义模板
        TaskTemplate template = new TaskTemplate();
        template.setTemplateName("测试模板");
        template.setTaskType("custom");
        template.setCategory("custom");
        template.setCompletionMethod("text_image");
        template.setDescription("测试描述");
        template.setPriority("medium");
        template.setConfig("{\"requireText\":true,\"minTextLength\":10}");
        template.setIcon("fa-test");

        TaskTemplate created = templateService.createTemplate(template);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("测试模板", created.getTemplateName());
        assertEquals(0, created.getIsSystem(), "自定义模板isSystem应为0");
    }

    @Test
    public void testUpdateTemplate() {
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
        created.setTemplateName("更新后名称");
        created.setDescription("更新后描述");
        created.setPriority("high");

        TaskTemplate updated = templateService.updateTemplate(created);

        assertNotNull(updated);
        assertEquals("更新后名称", updated.getTemplateName());
        assertEquals("更新后描述", updated.getDescription());
        assertEquals("high", updated.getPriority());
    }

    @Test
    public void testDeleteTemplate() {
        // 创建模板
        TaskTemplate template = new TaskTemplate();
        template.setTemplateName("待删除模板");
        template.setTaskType("custom");
        template.setCategory("custom");
        template.setCompletionMethod("text_image");
        template.setDescription("测试");
        template.setPriority("medium");
        TaskTemplate created = templateService.createTemplate(template);

        // 删除模板
        templateService.deleteTemplate(created.getId());

        // 验证删除
        TaskTemplate deleted = templateService.getById(created.getId());
        assertNull(deleted, "模板应已被删除");
    }

    @Test
    public void testDeleteSystemTemplateShouldFail() {
        // 尝试删除系统模板
        List<TaskTemplate> systemTemplates = templateService.getSystemTemplates(null);
        if (!systemTemplates.isEmpty()) {
            TaskTemplate firstSystemTemplate = systemTemplates.get(0);

            // 应该抛出异常
            Exception exception = assertThrows(RuntimeException.class, () -> {
                templateService.deleteTemplate(firstSystemTemplate.getId());
            });

            assertTrue(exception.getMessage().contains("系统模板不允许删除"));
        }
    }

    @Test
    public void testApplyTemplate() {
        // 获取系统模板
        List<TaskTemplate> templates = templateService.getSystemTemplates("venue");
        if (!templates.isEmpty()) {
            TaskTemplate template = templates.get(0);

            // 准备任务数据
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("conferenceId", 1L);
            taskData.put("taskName", "应用模板创建的任务");
            taskData.put("deadline", "2026-12-31 23:59:59");
            taskData.put("creatorId", 1L);
            taskData.put("ownerName", "测试管理员");

            // 准备执行人
            List<Map<String, Object>> assigneesData = new ArrayList<>();
            Map<String, Object> assignee1 = new HashMap<>();
            assignee1.put("userId", 100L);
            assignee1.put("userName", "张三");
            assignee1.put("role", "executor");
            assignee1.put("isMain", 1);
            assigneesData.add(assignee1);

            Map<String, Object> assignee2 = new HashMap<>();
            assignee2.put("userId", 101L);
            assignee2.put("userName", "李四");
            assignee2.put("role", "assistant");
            assignee2.put("isMain", 0);
            assigneesData.add(assignee2);

            taskData.put("assignees", assigneesData);

            // 应用模板
            Map<String, Object> result = templateService.applyTemplate(template.getId(), taskData);

            assertNotNull(result);
            assertNotNull(result.get("task"));
            assertEquals(template.getTemplateName(), result.get("templateName"));

            TaskInfo task = (TaskInfo) result.get("task");
            assertEquals("应用模板创建的任务", task.getTaskName());
            assertEquals(template.getTaskType(), task.getTaskType());
            assertEquals(template.getCategory(), task.getCategory());
            assertEquals(template.getCompletionMethod(), task.getCompletionMethod());
            assertEquals(template.getPriority(), task.getPriority());
            assertEquals(template.getDescription(), task.getDescription());
        }
    }

    @Test
    public void testListTemplatesWithPagination() {
        // 测试分页查询
        Page<TaskTemplate> page1 = templateService.listTemplates(null, null, 1, 5);
        assertNotNull(page1);
        assertTrue(page1.getSize() <= 5);

        // 如果有第二页
        if (page1.getTotal() > 5) {
            Page<TaskTemplate> page2 = templateService.listTemplates(null, null, 2, 5);
            assertNotNull(page2);
            assertTrue(page2.getCurrent() == 2);
        }
    }

    @Test
    public void testListTemplatesByCategory() {
        // 测试按类别查询
        Page<TaskTemplate> venuePage = templateService.listTemplates("venue", null, 1, 10);
        assertNotNull(venuePage);

        for (TaskTemplate template : venuePage.getRecords()) {
            assertEquals("venue", template.getCategory());
        }

        Page<TaskTemplate> studentPage = templateService.listTemplates("student", null, 1, 10);
        assertNotNull(studentPage);

        for (TaskTemplate template : studentPage.getRecords()) {
            assertEquals("student", template.getCategory());
        }
    }

    @Test
    public void testListSystemTemplatesOnly() {
        // 测试只查询系统模板
        Page<TaskTemplate> systemPage = templateService.listTemplates(null, 1, 1, 10);
        assertNotNull(systemPage);

        for (TaskTemplate template : systemPage.getRecords()) {
            assertEquals(1, template.getIsSystem());
        }
    }
}
