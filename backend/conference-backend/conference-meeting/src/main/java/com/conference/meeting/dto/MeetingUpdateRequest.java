package com.conference.meeting.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingUpdateRequest {

    private Long id;

    @JsonProperty("meetingCode")
    @JsonAlias({"meeting_code"})
    private String meetingCode;

    @JsonProperty("meetingName")
    @JsonAlias({"meeting_name"})
    private String meetingName;

    // 与 Meeting 实体一致，类型为 String（如 "training", "conference" 等）
    @JsonProperty("meetingType")
    @JsonAlias({"meeting_type"})
    private String meetingType;

    @JsonProperty("startTime")
    @JsonAlias({"start_time"})
    private LocalDateTime startTime;

    @JsonProperty("endTime")
    @JsonAlias({"end_time"})
    private LocalDateTime endTime;

    @JsonProperty("registrationStart")
    @JsonAlias({"registration_start"})
    private LocalDateTime registrationStart;

    @JsonProperty("registrationEnd")
    @JsonAlias({"registration_end"})
    private LocalDateTime registrationEnd;

    @JsonProperty("venueName")
    @JsonAlias({"venue_name"})
    private String venueName;

    @JsonProperty("venueAddress")
    @JsonAlias({"venue_address"})
    private String venueAddress;

    // 与 Meeting 实体一致，类型为 Double
    @JsonProperty("locationLatitude")
    @JsonAlias({"location_latitude"})
    private Double locationLatitude;

    // 与 Meeting 实体一致，类型为 Double
    @JsonProperty("locationLongitude")
    @JsonAlias({"location_longitude"})
    private Double locationLongitude;

    @JsonProperty("maxParticipants")
    @JsonAlias({"max_participants"})
    private Integer maxParticipants;

    private String theme;

    @JsonProperty("coverImageUrl")
    @JsonAlias({"cover_image_url"})
    private String coverImageUrl;

    @JsonProperty("registrationConfig")
    @JsonAlias({"registration_config"})
    private String registrationConfig;

    @JsonProperty("checkinConfig")
    @JsonAlias({"checkin_config"})
    private String checkinConfig;

    @JsonProperty("notificationConfig")
    @JsonAlias({"notification_config"})
    private String notificationConfig;

    private String description;

    private String remarks;

    private Integer status;
}
