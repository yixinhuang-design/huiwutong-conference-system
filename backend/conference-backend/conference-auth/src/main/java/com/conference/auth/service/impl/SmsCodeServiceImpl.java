package com.conference.auth.service.impl;

import com.conference.auth.service.SmsCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码服务实现
 * 
 * 开发/测试模式：验证码固定为 123456，不调用真实短信接口
 * 生产模式：生成随机6位验证码，调用短信服务商API（如阿里云SMS）
 * 
 * 验证码存储：ConcurrentHashMap 内存缓存（5分钟过期）
 * 生产环境建议改为 Redis 实现
 */
@Slf4j
@Service
public class SmsCodeServiceImpl implements SmsCodeService {

    /** 验证码缓存 key=phone, value=SmsEntry(code, expireTime) */
    private final Map<String, SmsEntry> codeCache = new ConcurrentHashMap<>();

    /** Mock模式：开发测试时验证码固定为123456 */
    @Value("${sms.mock-mode:true}")
    private boolean mockMode;

    /** 验证码有效期（秒） */
    @Value("${sms.code-expire:300}")
    private int codeExpireSeconds;

    /** 发送间隔限制（秒） */
    @Value("${sms.send-interval:60}")
    private int sendIntervalSeconds;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public boolean sendCode(String phone) {
        // 检查发送间隔
        SmsEntry existing = codeCache.get(phone);
        if (existing != null && !existing.canResend(sendIntervalSeconds)) {
            log.warn("短信发送过于频繁: phone={}", maskPhone(phone));
            return false;
        }

        // 生成验证码
        String code;
        if (mockMode) {
            code = "123456";
            log.info("【Mock模式】短信验证码: phone={}, code={}", maskPhone(phone), code);
        } else {
            code = generateCode();
            log.info("短信验证码已生成: phone={}", maskPhone(phone));
            // TODO: 调用真实短信服务商API发送
            // sendRealSms(phone, code);
        }

        // 缓存验证码
        long expireTime = System.currentTimeMillis() + codeExpireSeconds * 1000L;
        codeCache.put(phone, new SmsEntry(code, expireTime, System.currentTimeMillis()));

        // 清理过期数据（简易清理，生产环境用Redis TTL）
        cleanExpiredEntries();

        return true;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        SmsEntry entry = codeCache.get(phone);
        if (entry == null) {
            log.warn("验证码不存在: phone={}", maskPhone(phone));
            return false;
        }

        if (entry.isExpired()) {
            codeCache.remove(phone);
            log.warn("验证码已过期: phone={}", maskPhone(phone));
            return false;
        }

        if (!entry.code.equals(code)) {
            log.warn("验证码错误: phone={}", maskPhone(phone));
            return false;
        }

        // 验证成功后移除（一次性使用）
        codeCache.remove(phone);
        return true;
    }

    /**
     * 生成6位随机数字验证码
     */
    private String generateCode() {
        int num = RANDOM.nextInt(900000) + 100000;
        return String.valueOf(num);
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 清理过期条目
     */
    private void cleanExpiredEntries() {
        long now = System.currentTimeMillis();
        codeCache.entrySet().removeIf(entry -> entry.getValue().expireTime < now);
    }

    /**
     * 短信缓存条目
     */
    private static class SmsEntry {
        final String code;
        final long expireTime;
        final long sendTime;

        SmsEntry(String code, long expireTime, long sendTime) {
            this.code = code;
            this.expireTime = expireTime;
            this.sendTime = sendTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        boolean canResend(int intervalSeconds) {
            return System.currentTimeMillis() - sendTime > intervalSeconds * 1000L;
        }
    }
}
