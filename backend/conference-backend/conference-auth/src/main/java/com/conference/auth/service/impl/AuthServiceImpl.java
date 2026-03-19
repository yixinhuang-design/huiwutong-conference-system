package com.conference.auth.service.impl;

import com.conference.auth.dto.LoginRequest;
import com.conference.auth.dto.LoginResponse;
import com.conference.auth.dto.UserInfo;
import com.conference.auth.entity.SysTenant;
import com.conference.auth.entity.SysUser;
import com.conference.auth.mapper.SysRoleMapper;
import com.conference.auth.mapper.SysTenantMapper;
import com.conference.auth.mapper.SysUserMapper;
import com.conference.auth.service.AuthService;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.ResultCode;
import com.conference.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysTenantMapper tenantMapper;
    private final SysRoleMapper roleMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysTenant tenant = getTenant(request.getTenantCode());

        SysUser user = userMapper.selectByTenantAndUsername(tenant.getId(), request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        // 密码验证：支持MD5格式密码和BCrypt格式密码
        // 将前端明文密码转换为MD5格式
        String md5Password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!md5Password.equals(user.getPassword()) && !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        Map<String, Object> claims = buildClaims(tenant, user, roles);

        String accessToken = JwtUtils.generateToken(String.valueOf(user.getId()), claims, expiration);
        String refreshToken = JwtUtils.generateRefreshToken(String.valueOf(user.getId()));

        return buildLoginResponse(accessToken, refreshToken, tenant, user, roles);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        Object type = claims.get("type");
        if (type == null || !"refresh".equals(type.toString())) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = Long.valueOf(claims.getSubject());
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        SysTenant tenant = tenantMapper.selectById(user.getTenantId());
        validateTenant(tenant);

        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);
        Map<String, Object> newClaims = buildClaims(tenant, user, roles);

        String accessToken = JwtUtils.generateToken(String.valueOf(userId), newClaims, expiration);
        String newRefreshToken = JwtUtils.generateRefreshToken(String.valueOf(userId));

        return buildLoginResponse(accessToken, newRefreshToken, tenant, user, roles);
    }

    @Override
    public UserInfo getCurrentUser(String accessToken) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(accessToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        Long userId = Long.valueOf(claims.getSubject());
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        SysTenant tenant = tenantMapper.selectById(user.getTenantId());
        validateTenant(tenant);

        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);
        return buildUserInfo(tenant, user, roles);
    }

    private SysTenant getTenant(String tenantCode) {
        String code = StringUtils.hasText(tenantCode) ? tenantCode : "default";
        SysTenant tenant = tenantMapper.selectByTenantCode(code);
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }
        validateTenant(tenant);
        return tenant;
    }

    private void validateTenant(SysTenant tenant) {
        if (tenant == null) {
            throw new BusinessException(ResultCode.TENANT_NOT_FOUND);
        }
        // status: 1=启用, 0=禁用
        if (tenant.getStatus() == null || tenant.getStatus() != 1) {
            throw new BusinessException(ResultCode.TENANT_DISABLED);
        }
    }

    private Map<String, Object> buildClaims(SysTenant tenant, SysUser user, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tenantId", tenant.getId());
        claims.put("username", user.getUsername());
        claims.put("userType", user.getUserType());
        claims.put("roles", roles);
        return claims;
    }

    private LoginResponse buildLoginResponse(String accessToken, String refreshToken, SysTenant tenant, SysUser user, List<String> roles) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiration / 1000);
        response.setUserInfo(buildUserInfo(tenant, user, roles));
        return response;
    }

    private UserInfo buildUserInfo(SysTenant tenant, SysUser user, List<String> roles) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setTenantId(tenant.getId());
        info.setTenantName(tenant.getTenantName());
        info.setUsername(user.getUsername());
        info.setRealName(user.getRealName());
        info.setUserType(user.getUserType());
        info.setAvatar(user.getAvatar());
        info.setRoles(roles);
        return info;
    }
}
