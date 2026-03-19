package com.conference.registration.dto;

import lombok.Data;
import java.util.List;

/**
 * 手册保存请求DTO - 与前端 roster-generation.html 数据结构对齐
 */
@Data
public class HandbookSaveRequest {

    private Long meetingId;
    private String title;

    /** 封面配置 */
    private CoverConfig coverConfig;

    /** 目录配置 */
    private TocConfig tocConfig;

    /** 名册配置 */
    private RosterConfig rosterConfig;

    /** 名册字段配置 */
    private List<FieldConfig> rosterFields;

    /** 座位配置 */
    private SeatingConfig seatingConfig;

    /** 乘车配置 */
    private TransportConfig transportConfig;

    /** 住宿配置 */
    private HotelConfig hotelConfig;

    /** 就餐配置 */
    private MealConfig mealConfig;

    /** 讨论配置 */
    private DiscussionSessionConfig discussionConfig;

    /** 封底配置 */
    private BackcoverConfig backcoverConfig;

    /** 其他事项 */
    private String notesContent;

    /** 板块配置 */
    private List<SectionConfig> handbookSections;

    /** 分组配置 */
    private GroupingConfig grouping;

    /** 讨论题目列表 */
    private List<DiscussionTopic> discussionTopics;

    // ===== 内部DTO =====

    @Data
    public static class CoverConfig {
        private String title;
        private String subtitle;
        private String date;
        private String location;
        private String organizer;
        private String backgroundImage;
    }

    @Data
    public static class TocConfig {
        private String style;
        private Boolean showPageNumbers;
    }

    @Data
    public static class RosterConfig {
        private String displayMode; // "all" 或 "grouped"
        private Boolean showSeating;
        private Boolean showTransport;
        private Boolean showHotel;
        private Boolean showMeal;
        private Boolean showDiscussion;
    }

    @Data
    public static class FieldConfig {
        private String id;
        private String label;
        private Boolean visible;
    }

    @Data
    public static class SeatingConfig {
        private String source;
        private Boolean loaded;
        private Integer count;
        private String displayMode;
    }

    @Data
    public static class TransportConfig {
        private List<BusInfo> buses;
        private Boolean showInRoster;
    }

    @Data
    public static class BusInfo {
        private String name;
        private String route;
        private String time;
    }

    @Data
    public static class HotelConfig {
        private List<HotelInfo> hotels;
        private Boolean showRoomNumber;
    }

    @Data
    public static class HotelInfo {
        private String name;
        private String address;
        private String phone;
    }

    @Data
    public static class MealConfig {
        private MealTime breakfast;
        private MealTime lunch;
        private MealTime dinner;
        private Boolean showTableNumber;
    }

    @Data
    public static class MealTime {
        private String time;
        private String location;
    }

    @Data
    public static class DiscussionSessionConfig {
        private List<SessionInfo> sessions;
        private Boolean showGroupAssignment;
        private Boolean showModerator;
    }

    @Data
    public static class SessionInfo {
        private String name;
        private String time;
        private String location;
    }

    @Data
    public static class BackcoverConfig {
        private String style;
        private String phone;
        private String email;
        private String backgroundImage;
    }

    @Data
    public static class SectionConfig {
        private String id;
        private String name;
        private String icon;
        private String sourceType;
        private Boolean enabled;
        private Boolean completed;
    }

    @Data
    public static class GroupingConfig {
        private Boolean enabled;
        private Integer groupCount;
        private Integer groupSize;
        private String criteria;
    }

    @Data
    public static class DiscussionTopic {
        private String content;
        private String reference;
    }
}
