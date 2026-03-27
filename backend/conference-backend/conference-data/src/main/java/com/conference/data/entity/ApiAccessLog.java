package com.conference.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API访问日志
 */
@Data
@TableName("data_api_access_log")
public class ApiAccessLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;
    private String serviceName;
    private String method;
    private String path;
    private Integer statusCode;
    private Long responseTime;
    private Long requestSize;
    private Long responseSize;
    private String ipAddress;
    private String userAgent;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
