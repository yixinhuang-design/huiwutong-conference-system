package com.conference.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.auth.entity.SysUser;
import com.conference.auth.mapper.SysUserMapper;
import com.conference.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;

    /**
     * 搜索系统用户（按姓名/手机号模糊搜索）
     */
    @GetMapping("/search")
    public Result<?> searchUsers(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // @TableLogic on SysUser.deleted auto-appends deleted=0

        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w
                .like(SysUser::getRealName, kw)
                .or().like(SysUser::getPhone, kw)
                .or().like(SysUser::getUsername, kw)
            );
        }

        wrapper.select(SysUser::getId, SysUser::getUsername, SysUser::getRealName,
                SysUser::getPhone, SysUser::getEmail, SysUser::getAvatar,
                SysUser::getGender, SysUser::getUserType, SysUser::getStatus,
                SysUser::getTenantId);
        wrapper.orderByDesc(SysUser::getId);
        wrapper.last("LIMIT 50");

        List<SysUser> users = userMapper.selectList(wrapper);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("realName", u.getRealName());
            map.put("phone", u.getPhone());
            map.put("email", u.getEmail());
            map.put("avatar", u.getAvatar());
            map.put("department", "");
            map.put("status", u.getStatus());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 获取所有可用用户列表
     */
    @GetMapping("/list")
    public Result<?> listUsers(
            @RequestParam(required = false) Long tenantId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // @TableLogic on SysUser.deleted auto-appends deleted=0

        if (tenantId != null) {
            wrapper.eq(SysUser::getTenantId, tenantId);
        }

        wrapper.select(SysUser::getId, SysUser::getUsername, SysUser::getRealName,
                SysUser::getPhone, SysUser::getEmail, SysUser::getAvatar,
                SysUser::getTenantId);
        wrapper.orderByAsc(SysUser::getId);

        List<SysUser> users = userMapper.selectList(wrapper);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("realName", u.getRealName());
            map.put("phone", u.getPhone());
            map.put("email", u.getEmail());
            map.put("avatar", u.getAvatar());
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }
}
