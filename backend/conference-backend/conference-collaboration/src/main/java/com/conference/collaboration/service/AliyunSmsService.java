package com.conference.collaboration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 阿里云SMS短信服务
 * 使用HTTP API直接调用，无需额外SDK依赖
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunSmsService {

    private final AliyunSmsProperties smsProperties;

    private static final String ALIYUN_SMS_ENDPOINT = "https://dysmsapi.aliyuncs.com";
    private static final String ALIYUN_SMS_VERSION = "2017-05-25";
    private static final String ALIYUN_SMS_ACTION = "SendSms";

    /**
     * 发送短信
     * @param phoneNumber 手机号
     * @param content 短信内容（用于日志，实际发送使用模板）
     */
    public void sendSms(String phoneNumber, String content) {
        if (!smsProperties.isEnabled()) {
            log.info("[SMS-disabled] 短信未启用, phone={}, content={}", phoneNumber, content);
            return;
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            log.warn("短信发送失败: 手机号为空");
            return;
        }

        try {
            Map<String, String> params = buildCommonParams();
            params.put("Action", ALIYUN_SMS_ACTION);
            params.put("PhoneNumbers", phoneNumber);
            params.put("SignName", smsProperties.getSignName());
            params.put("TemplateCode", smsProperties.getTemplateCode());
            // 模板参数：将content作为message参数传入
            params.put("TemplateParam", String.format("{\"message\":\"%s\"}", 
                content.length() > 200 ? content.substring(0, 200) : content));

            String signature = computeSignature(params, smsProperties.getAccessKeySecret());
            params.put("Signature", signature);

            String queryString = buildQueryString(params);
            String url = ALIYUN_SMS_ENDPOINT + "?" + queryString;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body().contains("\"Code\":\"OK\"")) {
                log.info("短信发送成功: phone={}", phoneNumber);
            } else {
                log.error("短信发送失败: phone={}, response={}", phoneNumber, response.body());
            }

        } catch (Exception e) {
            log.error("短信发送异常: phone={}", phoneNumber, e);
        }
    }

    /**
     * 批量发送短信
     */
    public void sendSmsBatch(List<String> phoneNumbers, String content) {
        for (String phone : phoneNumbers) {
            sendSms(phone, content);
        }
    }

    private Map<String, String> buildCommonParams() {
        Map<String, String> params = new TreeMap<>();
        params.put("Format", "JSON");
        params.put("Version", ALIYUN_SMS_VERSION);
        params.put("AccessKeyId", smsProperties.getAccessKeyId());
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("Timestamp", getUTCTimestamp());
        params.put("SignatureVersion", "1.0");
        params.put("SignatureNonce", UUID.randomUUID().toString());
        params.put("RegionId", "cn-hangzhou");
        return params;
    }

    private String computeSignature(Map<String, String> params, String accessKeySecret) 
            throws NoSuchAlgorithmException, InvalidKeyException, java.io.UnsupportedEncodingException {
        // 按参数名排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            canonicalizedQueryString.append("&")
                .append(percentEncode(entry.getKey()))
                .append("=")
                .append(percentEncode(entry.getValue()));
        }

        String stringToSign = "GET&" + percentEncode("/") + "&" + 
            percentEncode(canonicalizedQueryString.substring(1));

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    private String percentEncode(String value) throws java.io.UnsupportedEncodingException {
        return java.net.URLEncoder.encode(value, "UTF-8")
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }

    private String buildQueryString(Map<String, String> params) throws java.io.UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(percentEncode(entry.getKey()))
              .append("=")
              .append(percentEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private String getUTCTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    // ==================== 配置属性 ====================

    @Component
    @ConfigurationProperties(prefix = "notification.aliyun.sms")
    @lombok.Data
    public static class AliyunSmsProperties {
        /** 是否启用短信通知 */
        private boolean enabled = false;
        /** 阿里云AccessKeyId */
        private String accessKeyId = "";
        /** 阿里云AccessKeySecret */
        private String accessKeySecret = "";
        /** 短信签名 */
        private String signName = "会务通";
        /** 短信模板编号 */
        private String templateCode = "SMS_000000000";
    }
}
