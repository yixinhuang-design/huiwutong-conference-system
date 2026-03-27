package com.conference.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponse {

    /**
     * 日程ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 会议ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long meetingId;

    /**
     * 日程主题
     */
    private String title;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 详细地点
     */
    private String location;

    /**
     * 主持人
     */
    private String host;

    /**
     * 主讲人
     */
    private String speaker;

    /**
     * 主讲人介绍
     */
    private String speakerIntro;

    /**
     * 备注说明
     */
    private String notes;

    /**
     * 日程状态（0-待发布, 1-已发布, 2-进行中, 3-已结束, 4-已取消）
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 优先级（1=正常，2=重要，3=特别重要）
     */
    private Integer priority;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 日程设置
     */
    private ScheduleSettingsResponse settings;

    /**
     * 附件列表
     */
    private List<ScheduleAttachmentResponse> attachments;

    /**
     * 创建人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;
}
