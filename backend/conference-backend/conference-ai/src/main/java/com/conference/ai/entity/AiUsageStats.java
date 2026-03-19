package com.conference.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI使用统计实体
 */
@Data
@TableName("ai_usage_stats")
public class AiUsageStats {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate statDate;

    private Integer totalQueries;
    private Integer totalTokens;
    private Integer avgResponseMs;
    private Integer positiveCount;
    private Integer negativeCount;
    private Integer neutralCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
