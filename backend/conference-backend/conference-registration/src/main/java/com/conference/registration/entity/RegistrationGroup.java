package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名分组实体
 */
@Data
@TableName("registration_group")
public class RegistrationGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 会议ID */
    private Long conferenceId;
    
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
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 创建人ID */
    private Long creatorId;
    
    /** 是否删除 */
    private Boolean deleted;
}
