package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 参会人员实体类
 * 对应数据库表: conf_seating_attendee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_attendee")
public class SeatingAttendee {
    
    private Long id;
    
    private Long conferenceId;
    
    private Long tenantId;
    
    private Long userId;
    
    @TableField("name")
    private String attendeeName;
    
    private String department;
    
    private String position;
    
    @TableField("phone")
    private String attendeePhone;
    
    private String email;
    
    private Boolean isVip;
    
    private Boolean isReserved;
    
    private Long assignedSeatId;
    
    private LocalDateTime assignedAt;
    
    private Boolean confirmed;
    
    private String attendanceStatus;
    
    private String extraData;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
