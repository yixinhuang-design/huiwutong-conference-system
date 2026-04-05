package com.conference.group.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 群组创建请求
 */
@Data
public class GroupCreateRequest {
    
    /**
     * 会议ID
     */
    @NotNull(message = "会议ID不能为空")
    private Long conferenceId;
    
    /**
     * 群组名称
     */
    @NotBlank(message = "群组名称不能为空")
    private String groupName;
    
    /**
     * 群组类型
     */
    @NotBlank(message = "群组类型不能为空")
    private String groupType;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 群公告
     */
    private String announcement;
    
    /**
     * 欢迎消息
     */
    private String welcomeMessage;
}
