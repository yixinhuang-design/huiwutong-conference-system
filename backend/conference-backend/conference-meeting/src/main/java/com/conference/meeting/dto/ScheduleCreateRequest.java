package com.conference.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCreateRequest {

    /**
     * 会议ID
     */
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
     * 优先级
     */
    private Integer priority;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 日程设置
     */
    private ScheduleSettingsRequest settings;

    /**
     * 附件列表（文件ID列表）
     */
    private List<String> attachments;
}
