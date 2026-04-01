package com.conference.seating.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人员分配请求 DTO（所有辅助安排通用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "人员分配请求")
public class AssignRequest {

    @NotNull(message = "参会人员ID不能为空")
    @Schema(description = "参会人员ID", example = "1")
    private Long attendeeId;

    @Schema(description = "参会人员姓名（可选，用于直接记录）", example = "张三")
    private String attendeeName;

    @Schema(description = "部门（可选，用于直接记录）", example = "技术部")
    private String department;
}
