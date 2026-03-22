package com.conference.meeting.dto;

import com.conference.meeting.entity.ArchiveMessage;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 消息群组响应（含预览消息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveMessageGroupResponse implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long meetingId;

    private String groupName;

    private Integer messageCount;

    private String lastActive;

    /** 预览消息（最近几条） */
    private List<ArchiveMessage> previewMessages;
}
