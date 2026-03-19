package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 参会人员实体类
 * @author AI Assistant
 * @date 2026-03-12
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
    
    private String attendeeName;
    
    private String attendeePhone;
    
    private String department;
    
    private String position;
    
    private String gender;
    
    private Boolean isVip;
    
    private String specialNeeds;
    
    private Boolean hasRegistered;
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    private LocalDateTime updatedAt;
}
