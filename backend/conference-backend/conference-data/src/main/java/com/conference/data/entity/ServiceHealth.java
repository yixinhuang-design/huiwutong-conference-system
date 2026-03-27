package com.conference.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务健康状态
 */
@Data
@TableName("data_service_health")
public class ServiceHealth implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String serviceName;
    private String serviceUrl;
    private Integer servicePort;
    private String serviceIcon;
    private String serviceDesc;

    /** healthy/warning/error/offline */
    private String status;

    /** 响应时间(ms) */
    private Long responseTime;

    /** 每秒查询数 */
    private Integer qps;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheckTime;

    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
