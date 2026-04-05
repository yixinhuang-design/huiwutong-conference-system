package com.conference.auth.dto;

import lombok.Data;

/**
 * 发送短信验证码请求
 */
@Data
public class SendSmsRequest {

    /** 手机号 */
    private String phone;

    /** 租户代码（可选，默认DEFAULT） */
    private String tenantCode;
}
