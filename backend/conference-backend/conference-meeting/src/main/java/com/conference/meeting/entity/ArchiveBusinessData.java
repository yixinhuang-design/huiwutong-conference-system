package com.conference.meeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_archive_business_data")
public class ArchiveBusinessData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("meeting_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long meetingId;

    @TableField("tenant_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    /** 数据类型: report_rate/checkin_rate/dormitory_rate */
    @TableField("data_type")
    private String dataType;

    /** 数据标签（部门名/日程名/日期） */
    @TableField("data_label")
    private String dataLabel;

    /** 时间描述 */
    @TableField("data_datetime")
    private String dataDatetime;

    @TableField("expected_count")
    private Integer expectedCount;

    @TableField("actual_count")
    private Integer actualCount;

    private BigDecimal rate;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer deleted;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
