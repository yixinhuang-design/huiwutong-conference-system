package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名表
 */
@Data
@TableName("conf_registration")
public class Registration {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 会议 ID
     */
    @TableField("meeting_id")
    private Long conferenceId;
    
    /**
     * 租户 ID
     */
    @TableField("tenant_id")
    private Long tenantId;
    
    /**
     * 用户 ID（报名人）
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 报名人姓名
     */
    @TableField("name")
    private String realName;
    
    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;
    
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 部门
     */
    @TableField("organization")
    private String department;
    
    /**
     * 职位
     */
    @TableField("position")
    private String position;
    
    /**
     * 性别：1-男, 2-女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 身份证号（加密存储）
     */
    @TableField("id_card")
    private String idCard;
    
    /**
     * 身份证照片 URL
     */
    @TableField(exist = false)
    private String idCardPhotoUrl;
    
    /**
     * 学历证明照片 URL
     */
    @TableField(exist = false)
    private String diplomaPhotoUrl;
    
    /**
     * 报名数据 (JSON 格式，存储自定义字段)
     */
    @TableField("extra_info")
    private String registrationData;
    
    /**
        * 报名状态：0-待审核,1-已通过,2-已拒绝,3-已取消,4-已签到
     */
        @TableField("status")
        private Integer status;
    
    /**
     * 审核备注
     */
    @TableField("review_remark")
    private String auditRemark;
    
    /**
     * 审核人 ID
     */
    @TableField("review_by")
    private Long auditorId;
    
    /**
     * 审核时间
     */
    @TableField("review_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    /**
     * 报名时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationTime;
    
    /**
     * 签到时间
     */
    @TableField("sign_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkinTime;
    
    /**
     * 签到状态：0-未签到, 1-已签到
     */
    @TableField(exist = false)
    private Integer checkinStatus;

    @TableField("deleted")
    private Integer deleted;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
