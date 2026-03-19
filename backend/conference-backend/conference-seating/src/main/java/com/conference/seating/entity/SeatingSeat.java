package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 座位实体类
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_seat")
public class SeatingSeat {
    
    private Long id;
    
    private Long conferenceId;
    
    private Long tenantId;
    
    private Long venueId;
    
    private String seatNumber;
    
    /** 行号，对应DB列 row_num */
    private Integer rowNum;
    
    /** 列号，对应DB列 column_num */
    private Integer columnNum;
    
    private String seatType;
    
    private String status;
    
    /** 分配人员ID，对应DB列 assigned_user_id */
    private Long assignedUserId;
    
    /** 分配人员名称，对应DB列 assigned_user_name */
    private String assignedUserName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
