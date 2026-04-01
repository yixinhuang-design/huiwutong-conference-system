package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 住宿人员分配记录实体
 * 对应数据库表: conf_seating_accommodation_assign
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_accommodation_assign")
public class SeatingAccommodationAssign {

    private Long id;

    private Long accommodationId;

    private Long attendeeId;

    private String attendeeName;

    private String department;

    private Long conferenceId;

    private Long tenantId;

    private LocalDateTime createdAt;
}
