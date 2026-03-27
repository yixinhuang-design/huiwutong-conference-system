package com.conference.meeting.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingCreateRequest {

    @JsonAlias("meeting_code")
    private String meetingCode;

    @JsonAlias("meeting_name")
    private String meetingName;

    @JsonAlias("meeting_type")
    private String meetingType;

    @JsonAlias("start_time")
    private LocalDateTime startTime;

    @JsonAlias("end_time")
    private LocalDateTime endTime;

    @JsonAlias("registration_start")
    private LocalDateTime registrationStart;

    @JsonAlias("registration_end")
    private LocalDateTime registrationEnd;

    @JsonAlias("venue_name")
    private String venueName;

    @JsonAlias("venue_address")
    private String venueAddress;

    @JsonAlias("location_latitude")
    private Double locationLatitude;

    @JsonAlias("location_longitude")
    private Double locationLongitude;

    @JsonAlias("max_participants")
    private Integer maxParticipants;

    @JsonAlias("theme")
    private String theme;

    @JsonAlias("cover_image_url")
    private String coverImageUrl;

    @JsonAlias("registration_config")
    private String registrationConfig;

    @JsonAlias("checkin_config")
    private String checkinConfig;

    @JsonAlias("notification_config")
    private String notificationConfig;

    @JsonAlias("description")
    private String description;

    @JsonAlias("remarks")
    private String remarks;
}
