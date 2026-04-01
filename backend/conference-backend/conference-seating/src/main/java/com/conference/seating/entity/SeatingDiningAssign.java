package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用餐人员分配记录实体
 * 对应数据库表: conf_seating_dining_assign
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_dining_assign")
public class SeatingDiningAssign {

    private Long id;

    private Long diningId;

    private Long attendeeId;

    private String attendeeName;

    private String department;

    private Long conferenceId;

    private Long tenantId;

    private LocalDateTime createdAt;
}
