package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 座位交换请求DTO
 * @author AI Assistant
 * @date 2026-03-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "座位交换请求")
public class SeatSwapRequest {
    
    @NotNull(message = "座位ID1不能为空")
    @Schema(description = "座位ID 1")
    private Long seatId1;
    
    @NotNull(message = "座位ID2不能为空")
    @Schema(description = "座位ID 2")
    private Long seatId2;
    
    @Schema(description = "交换原因/说明（可选）")
    private String reason;
}

