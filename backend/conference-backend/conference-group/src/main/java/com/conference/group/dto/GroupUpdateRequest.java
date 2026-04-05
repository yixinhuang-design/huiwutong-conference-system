package com.conference.group.dto;

import lombok.Data;

/**
 * 群组更新请求
 */
@Data
public class GroupUpdateRequest {
    
    /**
     * 群组名称
     */
    private String groupName;
    
    /**
     * 群公告
     */
    private String announcement;
    
    /**
     * 群头像URL
     */
    private String avatarUrl;
    
    /**
     * 欢迎消息
     */
    private String welcomeMessage;
    
    /**
     * 群二维码URL
     */
    private String qrCodeUrl;
}
