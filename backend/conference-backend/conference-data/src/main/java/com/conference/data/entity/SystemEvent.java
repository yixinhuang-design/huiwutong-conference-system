package com.conference.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统监控事件
 */
@Data
@TableName("data_system_event")
public class SystemEvent implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    /** info/success/warning/error */
    private String eventType;

    /** 事件来源服务 */
    private String eventSource;

    private String message;

    private String details;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
