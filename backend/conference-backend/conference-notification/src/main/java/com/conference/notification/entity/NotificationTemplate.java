package com.conference.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知模板实体
 */
@Data
@TableName("conf_notification_template")
public class NotificationTemplate {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long conferenceId;

    private String name;

    /** 通知类型: conference/registration/checkin/schedule/seat/bus/accommodation/full/custom */
    private String type;

    private String title;

    /** 模板内容(支持{变量}) */
    private String content;

    /** 变量列表(逗号分隔) */
    private String variables;

    /** 状态: 1=启用, 0=禁用 */
    private Integer status;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer deleted;
}
