package com.conference.seating.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 座位分配实体类
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("conf_seating_assignment")
public class SeatingAssignment {
    
    private Long id;
    
    private Long conferenceId;
    
    private Long seatId;
    
    private Long attendeeId;
    
    private Long tenantId;
    
    /** 日程ID（多日程隔离） */
    private Long scheduleId;
    
    /** 分配类型：auto/manual/swap */
    private String assignType;
    
    /** 使用的算法名称 */
    private String algorithmName;
    
    /** 分配时间 */
    private LocalDateTime assignTime;
    
    /** 分配人ID */
    private Long assignedBy;
    
    /** 状态：assigned/confirmed/cancelled */
    private String status;
    
    /** 确认时间 */
    private LocalDateTime confirmTime;
    
    /** 取消原因 */
    private String cancelReason;
    
    private LocalDateTime createdAt;
}
