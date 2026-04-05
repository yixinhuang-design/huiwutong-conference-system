package com.conference.collaboration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.collaboration.entity.TaskTemplate;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;

/**
 * 任务模板服务接口
 * @author AI Executive
 * @date 2026-04-01
 */
public interface TaskTemplateService extends IService<TaskTemplate> {

    /**
     * 分页查询模板列表
     * @param category 类别(可选)
     * @param isSystem 是否系统模板(可选)
     * @param page 页码
     * @param size 每页大小
     * @return 模板分页列表
     */
    Page<TaskTemplate> listTemplates(String category, Integer isSystem, int page, int size);

    /**
     * 获取模板详情
     * @param templateId 模板ID
     * @return 模板详情
     */
    Map<String, Object> getTemplateDetail(Long templateId);

    /**
     * 创建模板
     * @param template 模板信息
     * @return 创建的模板
     */
    TaskTemplate createTemplate(TaskTemplate template);

    /**
     * 更新模板
     * @param template 模板信息
     * @return 更新的模板
     */
    TaskTemplate updateTemplate(TaskTemplate template);

    /**
     * 删除模板
     * @param templateId 模板ID
     */
    void deleteTemplate(Long templateId);

    /**
     * 应用模板创建任务
     * @param templateId 模板ID
     * @param taskData 任务特定数据(标题、截止时间、执行人等)
     * @return 任务信息
     */
    Map<String, Object> applyTemplate(Long templateId, Map<String, Object> taskData);

    /**
     * 获取预设模板列表(系统模板)
     * @param category 类别(可选)
     * @return 系统模板列表
     */
    List<TaskTemplate> getSystemTemplates(String category);
}
