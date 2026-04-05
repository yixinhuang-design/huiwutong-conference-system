package com.conference.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群组成员实体
 */
@Data
@TableName("conference_group_member")
public class GroupMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 群组ID
     */
    private Long groupId;
    
    /**
     * 成员ID
     */
    private Long memberId;
    
    /**
     * 成员名称
     */
    private String memberName;
    
    /**
     * 成员角色：member普通成员/admin管理员/owner群主
     */
    private String role;
    
    /**
     * 入群时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;
    
    /**
     * Web Key（用于前端识别和访问）
     */
    private String webKey;
    
    /**
     * 扩展信息JSON
     */
    private String extJson;
}
