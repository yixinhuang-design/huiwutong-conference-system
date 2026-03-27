package com.conference.registration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名表
 */
@Data
@TableName("conf_registration")
public class Registration {
    
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 会议 ID
     */
    @TableField("meeting_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conferenceId;
    
    /**
     * 租户 ID
     */
    @TableField("tenant_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;
    
    /**
     * 用户 ID（报名人）
     */
    @TableField("user_id")
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 饮食要求
     */
    @TableField("dietary_requirements")
    private String dietaryRequirements;
    
    /**
     * 是否需要住宿：0-否, 1-是
     */
    @TableField("accommodation_required")
    private Integer accommodationRequired;
    
    /**
     * 到达时间
     */
    @TableField("arrival_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arrivalTime;
    
    /**
     * 离开时间
     */
    @TableField("departure_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime departureTime;
    
    /**
     * 二维码
     */
    @TableField("qr_code")
    private String qrCode;
    
    /**
     * 身份证照片 URL（不在数据库中）
     */
    @TableField(exist = false)
    private String idCardPhotoUrl;
    
    /**
     * 学历证明照片 URL（不在数据库中）
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
    @JsonSerialize(using = ToStringSerializer.class)
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
