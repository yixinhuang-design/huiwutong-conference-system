package com.conference.auth.controller;

import com.conference.auth.dto.*;
import com.conference.auth.service.AuthService;
import com.conference.auth.service.SmsCodeService;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.Result;
import com.conference.common.result.ResultCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final SmsCodeService smsCodeService;

    // ==================== 密码登录 ====================

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    // ==================== 短信验证码登录 ====================

    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@RequestBody SendSmsRequest request) {
        if (!StringUtils.hasText(request.getPhone())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号不能为空");
        }
        boolean sent = smsCodeService.sendCode(request.getPhone());
        if (!sent) {
            return Result.error("验证码发送太频繁，请稍后重试");
        }
        return Result.success();
    }

    /**
     * 手机号+验证码登录
     */
    @PostMapping("/sms/login")
    public Result<LoginResponse> smsLogin(@RequestBody SmsLoginRequest request) {
        if (!StringUtils.hasText(request.getPhone()) || !StringUtils.hasText(request.getSmsCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号和验证码不能为空");
        }
        // 校验验证码
        if (!smsCodeService.verifyCode(request.getPhone(), request.getSmsCode())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "验证码错误或已过期");
        }
        return Result.success(authService.smsLogin(request));
    }

    // ==================== 密码管理 ====================

    /**
     * 设置/修改密码（登录后调用）
     */
    @PostMapping("/password/set")
    public Result<Void> setPassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody SetPasswordRequest request) {
        String token = extractToken(authorization);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        UserInfo userInfo = authService.getCurrentUser(token);
        authService.setPassword(userInfo.getId(), request);
        return Result.success();
    }

    // ==================== 参会人员账号创建 ====================

    /**
     * 为审核通过的参会人员创建登录账号
     * 由registration服务在审核通过时调用
     */
    @PostMapping("/users/create-attendee")
    public Result<Map<String, String>> createAttendeeUser(@RequestBody CreateAttendeeUserRequest request) {
        return Result.success(authService.createAttendeeUser(request));
    }

    // ==================== 通用 ====================

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refresh(request.getRefreshToken()));
    }

    @GetMapping("/me")
    public Result<UserInfo> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return Result.success(authService.getCurrentUser(token));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        com.conference.common.tenant.TenantContextHolder.clear();
        return Result.success();
    }

    private String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
