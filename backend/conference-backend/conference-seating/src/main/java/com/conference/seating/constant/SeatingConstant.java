package com.conference.seating.constant;

/**
 * 排座系统常量定义
 * @author AI
 * @date 2026-03-07
 */
public class SeatingConstant {

    // ===== 座位状态 =====
    public static final String SEAT_STATUS_AVAILABLE = "available";      // 可用
    public static final String SEAT_STATUS_ASSIGNED = "assigned";        // 已分配
    public static final String SEAT_STATUS_RESERVED = "reserved";        // 保留
    public static final String SEAT_STATUS_AISLE = "aisle";              // 过道

    // ===== 座位类型 =====
    public static final String SEAT_TYPE_NORMAL = "normal";              // 普通座位
    public static final String SEAT_TYPE_VIP = "vip";                    // VIP座位
    public static final String SEAT_TYPE_RESERVED = "reserved";          // 保留座位
    public static final String SEAT_TYPE_AISLE = "aisle";                // 过道

    // ===== 会场类型 =====
    public static final String VENUE_TYPE_NORMAL = "normal";             // 普通
    public static final String VENUE_TYPE_USHAPE = "ushape";             // U形
    public static final String VENUE_TYPE_ROUND = "round";               // 圆形
    public static final String VENUE_TYPE_THEATER = "theater";           // 剧院式

    // ===== 分配类型 =====
    public static final String ASSIGN_TYPE_AUTO = "auto";                // 自动分配
    public static final String ASSIGN_TYPE_MANUAL = "manual";            // 手动分配
    public static final String ASSIGN_TYPE_SWAP = "swap";                // 交换分配

    // ===== 分配状态 =====
    public static final String ASSIGNMENT_STATUS_ASSIGNED = "assigned";  // 已分配
    public static final String ASSIGNMENT_STATUS_CONFIRMED = "confirmed"; // 已确认
    public static final String ASSIGNMENT_STATUS_CANCELLED = "cancelled"; // 已取消

    // ===== 参会人员参加状态 =====
    public static final String ATTENDANCE_STATUS_PENDING = "pending";    // 待确认
    public static final String ATTENDANCE_STATUS_CONFIRMED = "confirmed"; // 已确认
    public static final String ATTENDANCE_STATUS_ABSENT = "absent";      // 缺席

    // ===== 算法名称 =====
    public static final String ALGORITHM_DEPARTMENT_GROUPING = "department_grouping";
    public static final String ALGORITHM_PRIORITY_BASED = "priority_based";
    public static final String ALGORITHM_VIP_OPTIMIZATION = "vip_optimization";

    // ===== 错误消息 =====
    public static final String ERROR_VENUE_NOT_FOUND = "会场不存在";
    public static final String ERROR_SEAT_NOT_FOUND = "座位不存在";
    public static final String ERROR_ASSIGNMENT_NOT_FOUND = "分配记录不存在";
    public static final String ERROR_ATTENDEE_NOT_FOUND = "参会人员不存在";
    public static final String ERROR_SEAT_ALREADY_ASSIGNED = "座位已被分配";
    public static final String ERROR_INSUFFICIENT_SEATS = "可用座位不足";
}
