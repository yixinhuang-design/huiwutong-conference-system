package com.conference.auth.controller;

import com.conference.auth.dto.LoginRequest;
import com.conference.auth.dto.LoginResponse;
import com.conference.auth.dto.RefreshTokenRequest;
import com.conference.auth.dto.UserInfo;
import com.conference.auth.service.AuthService;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.Result;
import com.conference.common.result.ResultCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

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
        // 清除服务端租户上下文
        // 注：当前JWT为无状态Token，服务端无Token黑名单机制
        // 客户端应在收到成功响应后清除本地存储的Token
        // 后续可接入Redis实现Token黑名单以支持真正的服务端Token失效
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
