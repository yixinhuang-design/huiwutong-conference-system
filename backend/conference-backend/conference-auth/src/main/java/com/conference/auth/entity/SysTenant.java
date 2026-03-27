package com.conference.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户表 - 与数据库 sys_tenant 表对应
 */
@Data
@TableName("sys_tenant")
public class SysTenant implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String tenantCode;
    private String tenantName;
    private String contactName;           // 联系人名称
    private String contactPhone;          // 登录号码（用作登录用户名）
    private String contactEmail;          // 联系人邮箱
    private String tenantType;            // 客户类型：self-rent/full-pay
    private LocalDate validStartDate;     // 使用期限开始日期
    private LocalDate validEndDate;       // 使用期限结束日期
    private Integer maxUsers;             // 最大用户数限制
    private Integer maxConferences;       // 最大会议数限制
    private Long storageQuota;            // 存储配额(字节)
    private Integer status;               // 状态：1=启用, 0=禁用
    private String domain;                // 域名
    private String logoUrl;               // 企业logo URL
    private String remark;                // 备注
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

