package com.conference.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskTemplate;
import com.conference.collaboration.mapper.TaskTemplateMapper;
import com.conference.collaboration.service.TaskService;
import com.conference.collaboration.service.TaskTemplateService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 任务模板服务实现
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskTemplateServiceImpl extends ServiceImpl<TaskTemplateMapper, TaskTemplate>
        implements TaskTemplateService {

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
    public Page<TaskTemplate> listTemplates(String category, Integer isSystem, int page, int size) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<TaskTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTemplate::getTenantId, tenantId)
                .eq(TaskTemplate::getDeleted, 0);

        if (category != null && !category.isEmpty()) {
            wrapper.eq(TaskTemplate::getCategory, category);
        }
        if (isSystem != null) {
            wrapper.eq(TaskTemplate::getIsSystem, isSystem);
        }

        wrapper.orderByAsc(TaskTemplate::getSortOrder)
               .orderByDesc(TaskTemplate::getCreateTime);

        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public Map<String, Object> getTemplateDetail(Long templateId) {
        TaskTemplate template = this.getById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("template", template);

        // 解析配置JSON
        try {
            if (template.getConfig() != null && !template.getConfig().isEmpty()) {
                // 简单的JSON解析展示
                detail.put("configPreview", template.getConfig());
            }
        } catch (Exception e) {
            log.warn("解析模板配置失败: templateId={}", templateId);
        }

        return detail;
    }

    @Override
    @Transactional
    public TaskTemplate createTemplate(TaskTemplate template) {
        Long tenantId = getTenantId();
        template.setTenantId(tenantId);
        template.setIsSystem(template.getIsSystem() != null ? template.getIsSystem() : 0);
        template.setDeleted(0);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());

        // 自动设置排序号
        if (template.getSortOrder() == null) {
            LambdaQueryWrapper<TaskTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskTemplate::getTenantId, tenantId)
                    .eq(TaskTemplate::getDeleted, 0);
            long count = this.count(wrapper);
            template.setSortOrder((int) count + 1);
        }

        this.save(template);
        log.info("[租户{}] 模板创建成功: id={}, name={}", tenantId, template.getId(), template.getTemplateName());
        return template;
    }

    @Override
    @Transactional
    public TaskTemplate updateTemplate(TaskTemplate template) {
        template.setUpdateTime(LocalDateTime.now());
        this.updateById(template);
        log.info("模板{}已更新", template.getId());
        return template;
    }

    @Override
    @Transactional
    public void deleteTemplate(Long templateId) {
        // 系统模板不允许删除
        TaskTemplate template = this.getById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        if (template.getIsSystem() == 1) {
            throw new RuntimeException("系统模板不允许删除");
        }

        this.removeById(templateId);
        log.info("模板{}已删除", templateId);
    }

    @Override
    @Transactional
    public Map<String, Object> applyTemplate(Long templateId, Map<String, Object> taskData) {
        TaskTemplate template = this.getById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        // 构建任务信息
        TaskInfo task = new TaskInfo();
        task.setConferenceId(getLong(taskData, "conferenceId"));
        task.setTaskName(getString(taskData, "taskName"));
        task.setTaskType(template.getTaskType());
        task.setCategory(template.getCategory());
        task.setCompletionMethod(template.getCompletionMethod());
        task.setDescription(template.getDescription());
        task.setPriority(template.getPriority() != null ? template.getPriority() : "medium");
        task.setConfig(template.getConfig());

        // 解析截止时间
        String deadline = getString(taskData, "deadline");
        if (deadline != null && !deadline.isEmpty()) {
            try {
                task.setDeadline(LocalDateTime.parse(deadline, 
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                log.warn("截止时间格式错误: {}", deadline);
            }
        }

        task.setCreatorId(getLong(taskData, "creatorId"));
        task.setOwnerName(getString(taskData, "ownerName"));

        // 解析执行人
        List<TaskAssignee> assignees = new ArrayList<>();
        Object assigneesObj = taskData.get("assignees");
        if (assigneesObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> assigneeMap = (Map<String, Object>) map;
                    TaskAssignee assignee = new TaskAssignee();
                    assignee.setUserId(getLong(assigneeMap, "userId"));
                    assignee.setUserName(getString(assigneeMap, "userName"));
                    assignee.setRole(getString(assigneeMap, "role"));
                    assignee.setIsMain(getInt(assigneeMap, "isMain"));
                    assignees.add(assignee);
                }
            }
        }

        // 创建任务
        TaskInfo createdTask = taskService.createTask(task, assignees);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task", createdTask);
        result.put("templateName", template.getTemplateName());
        result.put("message", "应用模板成功");

        log.info("模板{}已应用，创建任务{}", templateId, createdTask.getId());
        return result;
    }

    @Override
    public List<TaskTemplate> getSystemTemplates(String category) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<TaskTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTemplate::getTenantId, tenantId)
                .eq(TaskTemplate::getIsSystem, 1)
                .eq(TaskTemplate::getDeleted, 0);

        if (category != null && !category.isEmpty()) {
            wrapper.eq(TaskTemplate::getCategory, category);
        }

        wrapper.orderByAsc(TaskTemplate::getSortOrder);
        return this.list(wrapper);
    }

    // 辅助方法
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
