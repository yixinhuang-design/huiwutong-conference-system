package com.conference.common.result;

import lombok.Getter;

/**
 * 统一响应状态码
 */
@Getter
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // 服务端错误 5xx
    ERROR(500, "系统异常"),
    SYSTEM_ERROR(500, "系统异常"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误码 1xxx
    PARAM_ERROR(1001, "参数校验失败"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXIST(1003, "数据已存在"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    DUPLICATE_ERROR(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败"),
    
    // 认证授权错误 2xxx
    LOGIN_FAILED(2001, "登录失败，用户名或密码错误"),
    TOKEN_EXPIRED(2002, "Token已过期，请重新登录"),
    TOKEN_INVALID(2003, "Token无效"),
    ACCOUNT_DISABLED(2004, "账号已被禁用"),
    PASSWORD_ERROR(2005, "密码错误"),
    
    // 租户错误 3xxx
    TENANT_NOT_FOUND(3001, "租户不存在"),
    TENANT_DISABLED(3002, "租户已被禁用"),
    TENANT_EXPIRED(3003, "租户已过期"),
    TENANT_CODE_EXIST(3004, "租户代码已存在"),
    
    // 会议错误 4xxx
    CONFERENCE_NOT_FOUND(4001, "会议不存在"),
    CONFERENCE_ENDED(4002, "会议已结束"),
    REGISTRATION_CLOSED(4003, "报名已关闭"),
    SEAT_NOT_AVAILABLE(4004, "座位不可用"),
    
    // 第三方服务错误 5xxx
    SMS_SEND_FAILED(5001, "短信发送失败"),
    EMAIL_SEND_FAILED(5002, "邮件发送失败"),
    OCR_FAILED(5003, "OCR识别失败"),
    AI_SERVICE_ERROR(5004, "AI服务异常");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
