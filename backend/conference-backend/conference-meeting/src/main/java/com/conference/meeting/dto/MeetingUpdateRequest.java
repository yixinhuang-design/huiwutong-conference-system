package com.conference.meeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingUpdateRequest {

    private Long id;

    @JsonProperty("meeting_code")
    private String meetingCode;

    @JsonProperty("meeting_name")
    private String meetingName;

    @JsonProperty("meeting_type")
    private Integer meetingType;

    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @JsonProperty("registration_start")
    private LocalDateTime registrationStart;

    @JsonProperty("registration_end")
    private LocalDateTime registrationEnd;

    @JsonProperty("venue_name")
    private String venueName;

    @JsonProperty("venue_address")
    private String venueAddress;

    @JsonProperty("location_latitude")
    private BigDecimal locationLatitude;

    @JsonProperty("location_longitude")
    private BigDecimal locationLongitude;

    @JsonProperty("max_participants")
    private Integer maxParticipants;

    @JsonProperty("theme")
    private String theme;

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("registration_config")
    private String registrationConfig;

    @JsonProperty("checkin_config")
    private String checkinConfig;

    @JsonProperty("notification_config")
    private String notificationConfig;

    @JsonProperty("description")
    private String description;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("status")
    private Integer status;
}
