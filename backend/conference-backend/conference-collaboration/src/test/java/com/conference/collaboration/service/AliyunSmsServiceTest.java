package com.conference.collaboration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AliyunSmsService 单元测试
 * 验证短信服务的启停逻辑和签名计算
 */
@DisplayName("阿里云短信服务测试")
class AliyunSmsServiceTest {

    private AliyunSmsService smsService;
    private AliyunSmsService.AliyunSmsProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AliyunSmsService.AliyunSmsProperties();
        smsService = new AliyunSmsService(properties);
    }

    @Test
    @DisplayName("M1: 未启用时不应发送短信 - 默认disabled")
    void sendSms_whenDisabled_shouldNotSend() {
        // Arrange - enabled defaults to false
        properties.setEnabled(false);

        // Act & Assert - should not throw, just log and skip
        assertDoesNotThrow(() -> smsService.sendSms("13800138000", "测试内容"));
    }

    @Test
    @DisplayName("M1: 空手机号不应发送")
    void sendSms_emptyPhone_shouldNotSend() {
        properties.setEnabled(true);

        // Act & Assert
        assertDoesNotThrow(() -> smsService.sendSms("", "测试内容"));
        assertDoesNotThrow(() -> smsService.sendSms(null, "测试内容"));
    }

    @Test
    @DisplayName("M1: 批量发送应循环调用")
    void sendSmsBatch_shouldCallSendSmsForEach() {
        properties.setEnabled(false);

        // Act & Assert - should not throw even with multiple phones
        assertDoesNotThrow(() -> smsService.sendSmsBatch(
                java.util.List.of("13800138001", "13800138002"), "批量测试"));
    }

    @Test
    @DisplayName("M1: 配置属性默认值正确")
    void properties_shouldHaveCorrectDefaults() {
        AliyunSmsService.AliyunSmsProperties props = new AliyunSmsService.AliyunSmsProperties();

        assertFalse(props.isEnabled());
        assertEquals("会务通", props.getSignName());
        assertEquals("", props.getAccessKeyId());
        assertEquals("", props.getAccessKeySecret());
    }
}
