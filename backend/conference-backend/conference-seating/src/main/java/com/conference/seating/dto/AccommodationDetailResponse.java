package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 住宿安排详情响应 DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "住宿安排详情响应")
public class AccommodationDetailResponse {
    
    @Schema(description = "ID", example = "1")
    private Long id;
    
    @Schema(description = "会议ID", example = "1")
    private Long conferenceId;
    
    @Schema(description = "房间号", example = "101")
    private String roomNumber;
    
    @Schema(description = "房间类型", example = "DOUBLE")
    private String roomType;
    
    @Schema(description = "房间容量", example = "2")
    private Integer capacity;
    
    @Schema(description = "已分配床位数", example = "1")
    private Integer assignedCount;
    
    @Schema(description = "可用床位数", example = "1")
    private Integer availableBeds;
    
    @Schema(description = "房间地址", example = "五楼西侧")
    private String address;
    
    @Schema(description = "联系电话", example = "0571-28888888")
    private String phone;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
