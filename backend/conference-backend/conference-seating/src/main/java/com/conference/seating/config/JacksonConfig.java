package com.conference.seating.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson 全局配置
 * 1. 支持多种日期时间输入格式，空字符串→null
 * 2. Long类型序列化为String，避免JS精度丢失（雪花ID）
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter[] DATE_TIME_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,                    // yyyy-MM-ddTHH:mm:ss / yyyy-MM-ddTHH:mm
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
    };

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE,                         // yyyy-MM-dd
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
    };

    private static final DateTimeFormatter[] TIME_FORMATS = {
            DateTimeFormatter.ISO_LOCAL_TIME,                         // HH:mm:ss / HH:mm
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm"),
    };

    private static final DateTimeFormatter DATE_TIME_OUTPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_OUTPUT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_OUTPUT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            JavaTimeModule module = new JavaTimeModule();

            // ====== LocalDateTime 反序列化：支持多格式 + 空字符串→null ======
            module.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String text = p.getText();
                    if (text == null || text.trim().isEmpty()) return null;
                    text = text.trim();
                    for (DateTimeFormatter fmt : DATE_TIME_FORMATS) {
                        try { return LocalDateTime.parse(text, fmt); } catch (DateTimeParseException ignored) {}
                    }
                    throw new IOException("无法解析日期时间: '" + text + "'");
                }
            });

            // ====== LocalDate 反序列化 ======
            module.addDeserializer(LocalDate.class, new JsonDeserializer<>() {
                @Override
                public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String text = p.getText();
                    if (text == null || text.trim().isEmpty()) return null;
                    text = text.trim();
                    for (DateTimeFormatter fmt : DATE_FORMATS) {
                        try { return LocalDate.parse(text, fmt); } catch (DateTimeParseException ignored) {}
                    }
                    throw new IOException("无法解析日期: '" + text + "'");
                }
            });

            // ====== LocalTime 反序列化 ======
            module.addDeserializer(LocalTime.class, new JsonDeserializer<>() {
                @Override
                public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String text = p.getText();
                    if (text == null || text.trim().isEmpty()) return null;
                    text = text.trim();
                    for (DateTimeFormatter fmt : TIME_FORMATS) {
                        try { return LocalTime.parse(text, fmt); } catch (DateTimeParseException ignored) {}
                    }
                    throw new IOException("无法解析时间: '" + text + "'");
                }
            });

            // ====== 序列化：统一输出格式 ======
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_OUTPUT));
            module.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_OUTPUT));
            module.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_OUTPUT));

            builder.modules(module);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // ====== Long → String 避免前端JS精度丢失 ======
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
