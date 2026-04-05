package com.conference.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群组消息实体
 */
@Data
@TableName("conference_group_message")
public class GroupMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 消息类型：text文本/image图片/file文件/voice语音/video视频/card卡片
     */
    private String messageType;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者名称
     */
    private String senderName;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 媒体URL（图片/文件/音视频）
     */
    private String mediaUrl;
    
    /**
     * 卡片JSON
     */
    private String cardJson;
    
    /**
     * 是否为机器人消息
     */
    private Boolean isBot;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * Web Key（用于前端识别和访问）
     */
    private String webKey;
    
    /**
     * 消息序号
     */
    private Long msgId;
}
