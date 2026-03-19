package com.conference.registration.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 报名请求 DTO
 */
@Data
public class RegistrationRequest {
    
    /**
     * 会议 ID
     */
    @NotNull(message = "会议ID不能为空")
    private Long conferenceId;
    
    /**
     * 报名人姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;
    
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 部门
     */
    @NotBlank(message = "部门不能为空")
    private String department;
    
    /**
     * 职位
     */
    @NotBlank(message = "职位不能为空")
    private String position;
    
    /**
     * 身份证号 (可选，如果未识别可为null或'未识别')
     */
    private String idCard;
    
    /**
     * 身份证照片 Base64 或 URL
     */
    private String idCardPhotoUrl;
    
    /**
     * 学历证明照片 Base64 或 URL
     */
    private String diplomaPhotoUrl;
    
    /**
     * 其他自定义字段 (JSON)
     */
    private String registrationData;
}
