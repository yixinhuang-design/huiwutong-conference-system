package com.conference.collaboration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_info")
public class TaskInfo {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long conferenceId;

    private String taskName;
    private String taskType;            // checkin/room_check/evaluation/collection/custom
    private String category;            // venue/student/custom
    private String completionMethod;    // text_image/location/questionnaire/collection
    private String description;
    private String priority;            // low/medium/high/urgent
    private String status;              // pending/in_progress/completed/overdue/cancelled

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    private Integer progress;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long creatorId;

    private String ownerName;
    private String targetGroups;        // JSON
    private String config;              // JSON
    private String attachments;         // JSON

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
