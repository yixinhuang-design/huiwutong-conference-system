package com.conference.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 系统指标快照
 */
@Data
@TableName("data_system_metrics")
public class SystemMetrics implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private Long memoryTotal;
    private Long memoryUsed;
    private BigDecimal heapUsage;
    private Long heapTotal;
    private Long heapUsed;
    private BigDecimal diskUsage;
    private Long diskTotal;
    private Long diskUsed;
    private Integer threadCount;
    private Integer activeConnections;
    private Long gcCount;
    private Long gcTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
