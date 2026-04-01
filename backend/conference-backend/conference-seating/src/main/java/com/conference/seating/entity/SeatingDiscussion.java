package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 讨论室安排实体类
 * 对应数据库表: conf_seating_discussion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_discussion")
public class SeatingDiscussion {

    private Long id;

    private Long conferenceId;

    private Long tenantId;

    private String roomName;

    private String location;

    private Integer capacity;

    private Integer assignedCount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
