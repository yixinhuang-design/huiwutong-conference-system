package com.conference.auth.service.impl;

import com.conference.auth.dto.CreateAttendeeUserRequest;
import com.conference.auth.dto.LoginRequest;
import com.conference.auth.dto.LoginResponse;
import com.conference.auth.dto.SetPasswordRequest;
import com.conference.auth.dto.SmsLoginRequest;
import com.conference.auth.dto.UserInfo;
import com.conference.auth.entity.SysRole;
import com.conference.auth.entity.SysTenant;
import com.conference.auth.entity.SysUser;
import com.conference.auth.entity.SysUserRole;
import com.conference.auth.mapper.SysRoleMapper;
import com.conference.auth.mapper.SysTenantMapper;
import com.conference.auth.mapper.SysUserMapper;
import com.conference.auth.mapper.SysUserRoleMapper;
import com.conference.auth.service.AuthService;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.ResultCode;
import com.conference.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysTenantMapper tenantMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

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

        // 密码验证：用户未设置密码时提示使用验证码登录
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "该账号未设置密码，请使用手机验证码登录");
        }

        // 密码验证：支持MD5格式密码和BCrypt格式密码
        String md5Password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        boolean isMd5Match = md5Password.equals(user.getPassword());
        boolean isBCryptMatch = !isMd5Match && user.getPassword() != null
                && user.getPassword().startsWith("$2") && passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isMd5Match && !isBCryptMatch) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // MD5密码自动升级为BCrypt（渐进式迁移）
        if (isMd5Match) {
            String bcryptPassword = passwordEncoder.encode(request.getPassword());
            user.setPassword(bcryptPassword);
            userMapper.updateById(user);
            log.info("用户[{}]密码已从MD5自动升级为BCrypt", user.getUsername());
        }

        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        Map<String, Object> claims = buildClaims(tenant, user, roles);

        String accessToken = JwtUtils.generateToken(String.valueOf(user.getId()), claims, expiration);
        String refreshToken = JwtUtils.generateRefreshToken(String.valueOf(user.getId()));

        return buildLoginResponse(accessToken, refreshToken, tenant, user, roles);
    }

    @Override
    public LoginResponse smsLogin(SmsLoginRequest request) {
        // 1. 确定租户
        SysTenant tenant = getTenant(request.getTenantCode());

        // 2. 按手机号查找用户（优先在租户下查，否则全局查）
        SysUser user = userMapper.selectByTenantAndPhone(tenant.getId(), request.getPhone());
        if (user == null) {
            // 尝试用手机号作为用户名查找（兼容手机号=用户名的情况）
            user = userMapper.selectByTenantAndUsername(tenant.getId(), request.getPhone());
        }
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "该手机号未注册，请先报名参会");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 3. 生成Token
        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        Map<String, Object> claims = buildClaims(tenant, user, roles);
        String accessToken = JwtUtils.generateToken(String.valueOf(user.getId()), claims, expiration);
        String refreshToken = JwtUtils.generateRefreshToken(String.valueOf(user.getId()));

        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户手机号验证码登录成功: phone={}, userId={}", request.getPhone(), user.getId());
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

    @Override
    @Transactional
    public void setPassword(Long userId, SetPasswordRequest request) {
        // 1. 参数验证
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码长度不能少于6位");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次密码输入不一致");
        }

        // 2. 查找用户
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        // 3. 如果已有密码，验证旧密码
        if (StringUtils.hasText(user.getPassword())) {
            if (!StringUtils.hasText(request.getOldPassword())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "请输入原密码");
            }
            // 支持MD5和BCrypt双格式验证
            String md5Old = DigestUtils.md5DigestAsHex(request.getOldPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            boolean oldMatch = md5Old.equals(user.getPassword())
                || (user.getPassword().startsWith("$2") && passwordEncoder.matches(request.getOldPassword(), user.getPassword()));
            if (!oldMatch) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "原密码错误");
            }
        }

        // 4. 设置新密码（BCrypt）
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户密码设置成功: userId={}", userId);
    }

    @Override
    @Transactional
    public Map<String, String> createAttendeeUser(CreateAttendeeUserRequest request) {
        // 1. 检查用户是否已存在
        SysUser existingUser = userMapper.selectByTenantAndUsername(request.getTenantId(), request.getUsername());
        if (existingUser != null) {
            Map<String, String> result = new HashMap<>();
            result.put("username", existingUser.getUsername());
            result.put("message", "账号已存在");
            result.put("exists", "true");
            return result;
        }

        // 2. 创建用户（不设置密码，用户首次用手机验证码登录后自行设置）
        SysUser user = new SysUser();
        user.setTenantId(request.getTenantId());
        user.setUsername(request.getUsername());
        user.setPassword(null); // 默认无密码，需通过手机号+验证码登录后设置
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setUserType("attendee");
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 4. 查找或创建 attendee 角色
        SysRole attendeeRole = roleMapper.selectOne(
            new QueryWrapper<SysRole>().eq("role_code", "attendee").eq("deleted", 0)
        );
        if (attendeeRole == null) {
            attendeeRole = new SysRole();
            attendeeRole.setTenantId(request.getTenantId());
            attendeeRole.setRoleCode("attendee");
            attendeeRole.setRoleName("参会人员");
            attendeeRole.setStatus(1);
            attendeeRole.setSort(30);
            attendeeRole.setDeleted(0);
            attendeeRole.setCreateTime(LocalDateTime.now());
            attendeeRole.setUpdateTime(LocalDateTime.now());
            roleMapper.insert(attendeeRole);
            log.info("自动创建attendee角色, id={}", attendeeRole.getId());
        }

        // 5. 关联用户角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(attendeeRole.getId());
        userRoleMapper.insert(userRole);

        // 5. 返回结果（无默认密码，用户需用手机验证码登录后自行设密码）
        Map<String, String> result = new HashMap<>();
        result.put("username", request.getUsername());
        result.put("userId", String.valueOf(user.getId()));
        result.put("exists", "false");
        result.put("message", "账号已创建，请使用手机号+验证码登录");
        log.info("参会人员账号创建成功: username={}, userId={}", request.getUsername(), user.getId());
        return result;
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
        info.setPhone(user.getPhone());
        info.setUserType(user.getUserType());
        info.setAvatar(user.getAvatar());
        info.setRoles(roles);
        info.setHasPassword(user.getPassword() != null && !user.getPassword().isEmpty());
        return info;
    }
}
