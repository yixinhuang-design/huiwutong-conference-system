package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 住宿安排实体类
 * 对应数据库表: conf_seating_accommodation
 * 字段: id, conference_id, tenant_id, hotel_name, room_number, room_type,
 *       capacity, assigned_count, check_in_time, check_out_time, created_at, updated_at
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_accommodation")
public class SeatingAccommodation {

    private Long id;

    private Long conferenceId;

    private Long tenantId;

    /** 酒店名称 - DB列: hotel_name */
    private String hotelName;

    private String roomNumber;

    private String roomType;

    private Integer capacity;

    private Integer assignedCount;

    /** 入住时间 - DB列: check_in_time */
    private LocalDateTime checkInTime;

    /** 退房时间 - DB列: check_out_time */
    private LocalDateTime checkOutTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
