package com.conference.auth.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户信息
 */
@Data
public class UserInfo {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String username;
    private String realName;
    private String phone;
    private String userType;
    private String avatar;
    private List<String> roles;
    /** 用户是否已设置密码 */
    private Boolean hasPassword;
}
