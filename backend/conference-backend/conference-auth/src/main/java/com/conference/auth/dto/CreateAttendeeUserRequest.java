package com.conference.auth.dto;

import lombok.Data;

/**
 * 创建参会人员账号请求
 * 在报名审核通过时由registration服务调用
 */
@Data
public class CreateAttendeeUserRequest {

    /** 租户ID */
    private Long tenantId;

    /** 用户名（建议使用手机号） */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别 */
    private Integer gender;

    /** 关联的报名记录ID */
    private Long registrationId;
}
