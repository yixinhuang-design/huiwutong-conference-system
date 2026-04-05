package com.conference.auth.dto;

import lombok.Data;

/**
 * 设置/修改密码请求
 */
@Data
public class SetPasswordRequest {

    /** 新密码 */
    private String newPassword;

    /** 确认密码 */
    private String confirmPassword;

    /** 旧密码（可选，首次设置密码时不需要） */
    private String oldPassword;
}
