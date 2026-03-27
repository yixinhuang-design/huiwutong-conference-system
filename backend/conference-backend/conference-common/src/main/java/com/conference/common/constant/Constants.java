package com.conference.common.constant;

/**
 * 系统常量定义
 */
public class Constants {
    
    private Constants() {}
    
    // ==================== 系统常量 ====================
    
    /** 系统名称 */
    public static final String SYSTEM_NAME = "智能会务系统";
    
    /** API版本 */
    public static final String API_VERSION = "v1";
    
    /** API前缀 */
    public static final String API_PREFIX = "/api/" + API_VERSION;
    
    // ==================== 请求头常量 ====================
    
    /** Token请求头 */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    
    /** Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /** 租户ID请求头 */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    
    /** 用户ID请求头 */
    public static final String HEADER_USER_ID = "X-User-Id";
    
    /** 用户名请求头 */
    public static final String HEADER_USERNAME = "X-Username";
    
    // ==================== 缓存Key前缀 ====================
    
    /** 用户Token缓存 */
    public static final String CACHE_USER_TOKEN = "user:token:";
    
    /** 用户信息缓存 */
    public static final String CACHE_USER_INFO = "user:info:";
    
    /** 权限缓存 */
    public static final String CACHE_USER_PERMISSION = "user:permission:";
    
    /** 验证码缓存 */
    public static final String CACHE_CAPTCHA = "captcha:";
    
    /** 短信验证码缓存 */
    public static final String CACHE_SMS_CODE = "sms:code:";
    
    // ==================== 状态常量 ====================
    
    /** 启用状态 */
    public static final Integer STATUS_ENABLED = 1;
    
    /** 禁用状态 */
    public static final Integer STATUS_DISABLED = 0;
    
    /** 删除标记 - 未删除 */
    public static final Integer DEL_FLAG_NORMAL = 0;
    
    /** 删除标记 - 已删除 */
    public static final Integer DEL_FLAG_DELETED = 1;
    
    // ==================== 分页常量 ====================
    
    /** 默认页码 */
    public static final Integer DEFAULT_PAGE_NUM = 1;
    
    /** 默认每页大小 */
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    
    /** 最大每页大小 */
    public static final Integer MAX_PAGE_SIZE = 100;
    
    // ==================== 会议状态 ====================
    
    /** 会议状态 - 筹备中 */
    public static final String CONFERENCE_STATUS_PREPARING = "preparing";
    
    /** 会议状态 - 进行中 */
    public static final String CONFERENCE_STATUS_ONGOING = "ongoing";
    
    /** 会议状态 - 已结束 */
    public static final String CONFERENCE_STATUS_FINISHED = "finished";
    
    /** 会议状态 - 已取消 */
    public static final String CONFERENCE_STATUS_CANCELLED = "cancelled";
    
    // ==================== 报名状态 ====================
    
    /** 报名状态 - 待审核 */
    public static final String REGISTRATION_STATUS_PENDING = "pending";
    
    /** 报名状态 - 已通过 */
    public static final String REGISTRATION_STATUS_APPROVED = "approved";
    
    /** 报名状态 - 已拒绝 */
    public static final String REGISTRATION_STATUS_REJECTED = "rejected";
    
    /** 报名状态 - 已取消 */
    public static final String REGISTRATION_STATUS_CANCELLED = "cancelled";
    
    // ==================== 任务状态 ====================
    
    /** 任务状态 - 待处理 */
    public static final String TASK_STATUS_PENDING = "pending";
    
    /** 任务状态 - 进行中 */
    public static final String TASK_STATUS_IN_PROGRESS = "in_progress";
    
    /** 任务状态 - 已完成 */
    public static final String TASK_STATUS_COMPLETED = "completed";
    
    /** 任务状态 - 已取消 */
    public static final String TASK_STATUS_CANCELLED = "cancelled";
}
