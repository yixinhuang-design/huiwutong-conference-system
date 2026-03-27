package com.conference.seating.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 座位分配请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAssignRequest {
    
    /**
     * 座位ID
     */
    private Long seatId;
    
    /**
     * 参会人员ID
     */
    private Long attendeeId;
    
    /**
     * 分配类型：auto/manual
     */
    private String assignType;
}

