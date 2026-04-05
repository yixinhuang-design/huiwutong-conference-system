package com.conference.collaboration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.collaboration.entity.TaskTemplate;
import com.conference.collaboration.service.TaskTemplateService;
import com.conference.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务模板 Controller
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@RestController
@RequestMapping("/api/task-template")
@RequiredArgsConstructor
public class TaskTemplateController {

    private final TaskTemplateService templateService;

    /** 获取模板列表(分页) */
    @GetMapping("/list")
    public Result<Page<TaskTemplate>> listTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer isSystem,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TaskTemplate> result = templateService.listTemplates(category, isSystem, page, size);
        return Result.ok(result);
    }

    /** 获取系统预设模板 */
    @GetMapping("/system")
    public Result<List<TaskTemplate>> getSystemTemplates(
            @RequestParam(required = false) String category) {
        List<TaskTemplate> templates = templateService.getSystemTemplates(category);
        return Result.ok(templates);
    }

    /** 获取模板详情 */
    @GetMapping("/{templateId}")
    public Result<Map<String, Object>> getTemplate(@PathVariable Long templateId) {
        Map<String, Object> detail = templateService.getTemplateDetail(templateId);
        return Result.ok(detail);
    }

    /** 创建模板 */
    @PostMapping("/create")
    public Result<TaskTemplate> createTemplate(@RequestBody TaskTemplate template) {
        TaskTemplate created = templateService.createTemplate(template);
        return Result.ok("创建模板成功", created);
    }

    /** 更新模板 */
    @PutMapping("/{templateId}")
    public Result<TaskTemplate> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody TaskTemplate template) {
        template.setId(templateId);
        TaskTemplate updated = templateService.updateTemplate(template);
        return Result.ok("更新模板成功", updated);
    }

    /** 删除模板 */
    @DeleteMapping("/{templateId}")
    public Result<String> deleteTemplate(@PathVariable Long templateId) {
        templateService.deleteTemplate(templateId);
        return Result.ok("删除模板成功");
    }

    /** 应用模板创建任务 */
    @PostMapping("/{templateId}/apply")
    public Result<Map<String, Object>> applyTemplate(
            @PathVariable Long templateId,
            @RequestBody Map<String, Object> taskData) {
        Map<String, Object> result = templateService.applyTemplate(templateId, taskData);
        return Result.ok(result);
    }
}
