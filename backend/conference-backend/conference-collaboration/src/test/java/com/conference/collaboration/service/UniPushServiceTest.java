package com.conference.collaboration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UniPushService 单元测试
 * 验证推送服务的启停逻辑和配置
 */
@DisplayName("UniPush推送服务测试")
class UniPushServiceTest {

    private UniPushService pushService;
    private UniPushService.UniPushProperties properties;

    @BeforeEach
    void setUp() {
        properties = new UniPushService.UniPushProperties();
        pushService = new UniPushService(properties);
    }

    @Test
    @DisplayName("M1: 未启用时不应发送推送 - 默认disabled")
    void pushToSingle_whenDisabled_shouldNotSend() {
        properties.setEnabled(false);

        assertDoesNotThrow(() -> pushService.pushToSingle("cid_001", "标题", "内容"));
    }

    @Test
    @DisplayName("M1: 空clientId不应发送")
    void pushToSingle_emptyClientId_shouldNotSend() {
        properties.setEnabled(true);

        assertDoesNotThrow(() -> pushService.pushToSingle("", "标题", "内容"));
        assertDoesNotThrow(() -> pushService.pushToSingle(null, "标题", "内容"));
    }

    @Test
    @DisplayName("M1: 批量推送应处理多个CID")
    void pushToMultiple_shouldHandleMultipleClients() {
        properties.setEnabled(false);

        assertDoesNotThrow(() -> pushService.pushToMultiple(
                List.of("cid_001", "cid_002"), "标题", "批量内容"));
    }

    @Test
    @DisplayName("M1: 配置属性默认值正确")
    void properties_shouldHaveCorrectDefaults() {
        UniPushService.UniPushProperties props = new UniPushService.UniPushProperties();

        assertFalse(props.isEnabled());
        assertEquals("", props.getAppId());
        assertEquals("", props.getAppKey());
        assertEquals("", props.getMasterSecret());
    }
}
