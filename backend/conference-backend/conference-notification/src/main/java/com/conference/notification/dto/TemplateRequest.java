package com.conference.notification.dto;

import lombok.Data;

/**
 * 通知模板请求DTO
 */
@Data
public class TemplateRequest {

    /** 模板ID(更新时提供) */
    private Long id;

    /** 会议ID(可选,null=全局模板) */
    private Long conferenceId;

    /** 模板名称 */
    private String name;

    /** 通知类型 */
    private String type;

    /** 模板标题 */
    private String title;

    /** 模板内容 */
    private String content;

    /** 变量列表(逗号分隔) */
    private String variables;
}
