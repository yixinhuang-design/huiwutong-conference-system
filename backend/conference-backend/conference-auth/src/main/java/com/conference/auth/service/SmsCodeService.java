package com.conference.auth.service;

/**
 * 短信验证码服务
 * 负责验证码的生成、缓存和校验
 * 当前实现为内存缓存模式，生产环境建议接入Redis + 真实短信服务商（如阿里云SMS）
 */
public interface SmsCodeService {

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendCode(String phone);

    /**
     * 校验短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否校验通过
     */
    boolean verifyCode(String phone, String code);
}
