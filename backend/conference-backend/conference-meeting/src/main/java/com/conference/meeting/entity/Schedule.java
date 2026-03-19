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
import java.time.LocalDateTime;

/**
 * 日程管理实体类
 * 对应表: conf_schedule
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_schedule")
public class Schedule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日程ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 会议ID
     */
    @TableField("meeting_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long meetingId;

    /**
     * 租户ID（多租户隔离）
     */
    @TableField("tenant_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    /**
     * 日程主题
     */
    @TableField("title")
    private String title;

    /**
     * 开始时间
     */
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 详细地点（如：主会场A厅）
     */
    @TableField("location")
    private String location;

    /**
     * 主持人姓名
     */
    @TableField("host")
    private String host;

    /**
     * 主讲人姓名
     */
    @TableField("speaker")
    private String speaker;

    /**
     * 主讲人介绍（职称、研究方向等）
     */
    @TableField("speaker_intro")
    private String speakerIntro;

    /**
     * 备注说明
     */
    @TableField("notes")
    private String notes;

    /**
     * 日程状态：0-待发布, 1-已发布, 2-进行中, 3-已结束, 4-已取消
     */
    @TableField("status")
    private Integer status;

    /**
     * 优先级（1=正常，2=重要，3=特别重要）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField("created_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 最后修改人ID
     */
    @TableField("updated_by")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updatedBy;

    /**
     * 最后修改时间
     */
    @TableField("updated_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;

    /**
     * 删除标志（0=正常，1=删除）
     */
    @TableField("deleted")
    private Integer deleted;
}
