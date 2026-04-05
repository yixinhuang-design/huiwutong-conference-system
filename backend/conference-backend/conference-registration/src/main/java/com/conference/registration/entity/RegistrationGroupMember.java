package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分组成员关联实体
 */
@Data
@TableName("registration_group_member")
public class RegistrationGroupMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 分组ID */
    private Long groupId;
    
    /** 报名ID */
    private Long registrationId;
    
    /** 是否为工作人员 */
    private Boolean isStaff;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
