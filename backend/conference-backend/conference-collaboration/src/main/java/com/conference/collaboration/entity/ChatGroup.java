package com.conference.collaboration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_group")
public class ChatGroup {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long conferenceId;

    private String groupName;
    private String groupType;      // manager/attendee/study
    private String description;
    private String icon;
    private String announcement;
    private String avatar;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownerId;

    private Integer memberCount;
    private Integer maxMembers;
    private Integer muteAll;
    private Integer allowInvite;
    private Integer autoCreated;
    private Integer pinned;
    private String status;         // 0-已解散 1-正常

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
