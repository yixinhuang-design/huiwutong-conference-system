package com.conference.auth.dto;

import lombok.Data;

/**
 * 短信验证码登录请求
 */
@Data
public class SmsLoginRequest {

    /** 手机号 */
    private String phone;

    /** 短信验证码 */
    private String smsCode;

    /** 租户代码（可选，默认DEFAULT） */
    private String tenantCode;
}
