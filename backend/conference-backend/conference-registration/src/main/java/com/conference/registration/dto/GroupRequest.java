package com.conference.registration.dto;

import lombok.Data;

import java.util.List;

/**
 * 分组创建/更新请求
 */
@Data
public class GroupRequest {
    /** 分组ID（更新时需要） */
    private Long id;
    
    /** 分组名称 */
    private String name;
    
    /** 分组描述 */
    private String description;
    
    /** 分组颜色 */
    private String color;
    
    /** 分组图标 */
    private String icon;
    
    /** 分组类型（manual手动/auto自动） */
    private String type;
    
    /** 排序 */
    private Integer sort;
    
    /** 成员ID列表 */
    private List<Long> memberIds;
    
    /** 工作人员ID列表 */
    private List<Long> staffIds;
    
    /** 会议ID */
    private Long conferenceId;
}
