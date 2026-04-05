package com.conference.auth.service;

import com.conference.auth.dto.CreateAttendeeUserRequest;
import com.conference.auth.dto.LoginRequest;
import com.conference.auth.dto.LoginResponse;
import com.conference.auth.dto.SetPasswordRequest;
import com.conference.auth.dto.SmsLoginRequest;
import com.conference.auth.dto.UserInfo;

import java.util.Map;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    /**
     * 手机号+短信验证码登录
     */
    LoginResponse smsLogin(SmsLoginRequest request);

    LoginResponse refresh(String refreshToken);

    UserInfo getCurrentUser(String accessToken);

    /**
     * 设置/修改密码（已登录用户）
     * @param userId 当前用户ID
     * @param request 密码设置请求
     */
    void setPassword(Long userId, SetPasswordRequest request);

    /**
     * 为审核通过的参会人员创建登录账号
     */
    Map<String, String> createAttendeeUser(CreateAttendeeUserRequest request);
}
