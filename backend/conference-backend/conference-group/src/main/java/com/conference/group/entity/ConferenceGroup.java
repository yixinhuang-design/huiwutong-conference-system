package com.conference.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议群组实体
 */
@Data
@TableName("conference_group")
public class ConferenceGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 会议ID
     */
    private Long conferenceId;
    
    /**
     * 群组名称
     */
    private String groupName;
    
    /**
     * 群组类型：staff工作人员群/student学员群
     */
    private String groupType;
    
    /**
     * 微信企业群ID
     */
    private String wechatChatId;
    
    /**
     * 群主ID
     */
    private String ownerId;
    
    /**
     * 群主名称
     */
    private String ownerName;
    
    /**
     * 群组公告
     */
    private String announcement;
    
    /**
     * 群头像URL
     */
    private String avatarUrl;
     
    /**
     * 入群欢迎语
     */
    private String welcomeMessage;
    
    /**
     * 群二维码URL
     */
    private String qrCodeUrl;
     
    /**
     * 成员数量
     */
    private Integer memberCount;
    
    /**
     * 管理员数量
     */
    private Integer adminCount;
    
    /**
     * 任务数量
     */
    private Integer taskCount;
    
    /**
     * 消息数量
     */
    private Integer messageCount;
    
    /**
     * 群组状态：active活跃/disabled禁用/deleted已删除
     */
    private String status;
    
    /**
     * 配置JSON（扩展字段）
     */
    private String configJson;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * Web Key（用于前端识别和访问）
     */
    private String webKey;
}
