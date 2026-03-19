package com.conference.registration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 报名动态字段配置请求
 */
@Data
public class RegistrationFieldConfigRequest {

    @NotNull(message = "会议ID不能为空")
    private Long conferenceId;

    @Valid
    private List<FieldConfig> fields = new ArrayList<>();

    @Data
    public static class FieldConfig {
        private String key;
        private String label;
        private String type;
        private Boolean required;
        private String placeholder;
        private List<String> options = new ArrayList<>();
        private Integer sort;
        private Boolean enabled;
        private String validationPattern;
        private String validationMessage;
    }
}
