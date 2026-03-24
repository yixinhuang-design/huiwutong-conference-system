package com.conference.meeting.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson 全局配置 - 支持多种日期时间格式
 * 前端可能发送: "yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-ddTHH:mm:ss" 或 ISO 变体
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter[] SUPPORTED_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,                    // yyyy-MM-ddTHH:mm:ss
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
    };

    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            JavaTimeModule module = new JavaTimeModule();

            // 反序列化：支持多种输入格式
            module.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String text = p.getText().trim();
                    if (text.isEmpty()) return null;

                    for (DateTimeFormatter fmt : SUPPORTED_FORMATS) {
                        try {
                            return LocalDateTime.parse(text, fmt);
                        } catch (DateTimeParseException ignored) {
                        }
                    }
                    throw new IOException("无法解析日期时间: '" + text
                            + "'，支持格式: yyyy-MM-dd HH:mm:ss / yyyy-MM-ddTHH:mm:ss");
                }
            });

            // 序列化：统一输出 "yyyy-MM-dd HH:mm:ss"
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(OUTPUT_FORMAT));

            builder.modules(module);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}
