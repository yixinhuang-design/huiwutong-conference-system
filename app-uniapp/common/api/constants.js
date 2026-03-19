// common/api/constants.js - API 常量和端点定义

export const API_BASE_URLS = {
  auth: 'http://localhost:8081',
  meeting: 'http://localhost:8084',
  registration: 'http://localhost:8082',
  notification: 'http://localhost:8085',
  analytics: 'http://localhost:8086',
}

// API端点集中管理
export const API_ENDPOINTS = {
  // ==================== 认证相关 ====================
  AUTH_LOGIN: '/api/auth/login',
  AUTH_REGISTER: '/api/auth/register',
  AUTH_LOGIN_BY_CODE: '/api/auth/loginByCode',
  AUTH_REFRESH: '/api/auth/refresh',
  AUTH_LOGOUT: '/api/auth/logout',
  AUTH_VERIFY_TOKEN: '/api/auth/verifyToken',

  // ==================== 租户管理 ====================
  TENANT_LIST: '/api/tenant/list',
  TENANT_CREATE: '/api/tenant/create',
  TENANT_GET: '/api/tenant/:id',
  TENANT_UPDATE: '/api/tenant/:id',
  TENANT_DELETE: '/api/tenant/:id',
  TENANT_ADMINS: '/api/tenant/:id/admins',
  TENANT_ADMINS_ADD: '/api/tenant/:id/admins',
  TENANT_ADMINS_DELETE: '/api/tenant/:id/admins/:userId',

  // ==================== 培训管理 ====================
  TRAINING_LIST: '/api/training/list',
  TRAINING_CREATE: '/api/training/create',
  TRAINING_GET: '/api/training/:id',
  TRAINING_UPDATE: '/api/training/:id',
  TRAINING_DELETE: '/api/training/:id',
  TRAINING_DUPLICATE: '/api/training/:id/duplicate',
  TRAINING_PUBLISH: '/api/training/:id/publish',
  TRAINING_SCHEDULE: '/api/training/:id/schedule',

  // ==================== 报名管理 ====================
  REGISTRATION_LIST: '/api/registration/list',
  REGISTRATION_GET: '/api/registration/:id',
  REGISTRATION_CREATE: '/api/registration/create',
  REGISTRATION_UPDATE: '/api/registration/:id',
  REGISTRATION_APPROVE: '/api/registration/:id/approve',
  REGISTRATION_REJECT: '/api/registration/:id/reject',
  REGISTRATION_APPEAL: '/api/registration/:id/appeal',
  REGISTRATION_STATISTICS: '/api/registration/statistics',

  // ==================== 座位管理 ====================
  SEAT_LIST: '/api/seat/list',
  SEAT_GET: '/api/seat/:id',
  SEAT_UPDATE: '/api/seat/:id',
  SEAT_ALLOCATE: '/api/seat/allocate',
  SEAT_BATCH_ALLOCATE: '/api/seat/batch-allocate',
  SEAT_DEALLOCATE: '/api/seat/:id/deallocate',
  SEAT_EXPORT: '/api/seat/export',
  SEAT_LAYOUT: '/api/seat/layout',

  // ==================== 签到管理 ====================
  CHECKIN_SCAN: '/api/checkin/scan',
  CHECKIN_SUBMIT: '/api/checkin/submit',
  CHECKIN_LIST: '/api/checkin/list',
  CHECKIN_STATUS: '/api/checkin/:id/status',
  CHECKIN_STATISTICS: '/api/checkin/statistics',

  // ==================== 分组管理 ====================
  GROUP_LIST: '/api/groups/list',
  GROUP_CREATE: '/api/groups/create',
  GROUP_GET: '/api/groups/:id',
  GROUP_UPDATE: '/api/groups/:id',
  GROUP_DELETE: '/api/groups/:id',
  GROUP_MEMBERS: '/api/groups/:id/members',
  GROUP_MEMBERS_ADD: '/api/groups/:id/members/add',
  GROUP_MEMBERS_REMOVE: '/api/groups/:id/members/remove',

  // ==================== 日程管理 ====================
  SCHEDULE_LIST: '/api/schedule/list',
  SCHEDULE_CREATE: '/api/schedule/create',
  SCHEDULE_GET: '/api/schedule/:id',
  SCHEDULE_UPDATE: '/api/schedule/:id',
  SCHEDULE_DELETE: '/api/schedule/:id',
  SCHEDULE_CHECK_CONFLICTS: '/api/schedule/check-conflicts',

  // ==================== 通知管理 ====================
  NOTICE_TEMPLATES: '/api/notice/templates',
  NOTICE_CREATE: '/api/notice/create',
  NOTICE_SEND: '/api/notice/send',
  NOTICE_SCHEDULE: '/api/notice/schedule',
  NOTICE_HISTORY: '/api/notice/history',
  NOTICE_STATISTICS: '/api/notice/statistics/:id',

  // ==================== 任务管理 ====================
  TASK_LIST: '/api/task/list',
  TASK_CREATE: '/api/task/create',
  TASK_GET: '/api/task/:id',
  TASK_UPDATE: '/api/task/:id',
  TASK_DELETE: '/api/task/:id',
  TASK_STATUS: '/api/task/:id/status',
  TASK_ASSIGN: '/api/task/:id/assign',
  TASK_FEEDBACK: '/api/task/:id/feedback',

  // ==================== 反馈和评价 ====================
  FEEDBACK_GET: '/api/feedback/:id',
  FEEDBACK_SUBMIT: '/api/feedback/submit',
  FEEDBACK_LIST: '/api/feedback/list',
  EVALUATION_SUBMIT: '/api/evaluation/submit',
  EVALUATION_RESULTS: '/api/evaluation/results',

  // ==================== 留言板 ====================
  GUESTBOOK_POST: '/api/guestbook/post',
  GUESTBOOK_LIST: '/api/guestbook/list',
  GUESTBOOK_COMMENT: '/api/guestbook/:id/comment',
  GUESTBOOK_LIKE: '/api/guestbook/:id/like',

  // ==================== 预警管理 ====================
  ALERT_CONFIG_LIST: '/api/alert/config/list',
  ALERT_CONFIG_CREATE: '/api/alert/config/create',
  ALERT_CONFIG_UPDATE: '/api/alert/config/:id',
  ALERT_CONFIG_DELETE: '/api/alert/config/:id',
  ALERT_CONFIG_TEST: '/api/alert/config/:id/test',
  ALERT_HANDLE_LIST: '/api/alert/list',
  ALERT_HANDLE_UPDATE: '/api/alert/:id/handle',
  ALERT_STATISTICS: '/api/alert/statistics',

  // ==================== 数据分析 ====================
  ANALYTICS_SUMMARY: '/api/analytics/summary',
  ANALYTICS_DETAILED: '/api/analytics/detailed',
  ANALYTICS_EXPORT: '/api/analytics/export',
  ANALYTICS_TRENDS: '/api/analytics/trends',
  ANALYTICS_COMPARISON: '/api/analytics/comparison',

  // ==================== 手册生成 ====================
  HANDBOOK_TEMPLATES: '/api/handbook/templates',
  HANDBOOK_GENERATE: '/api/handbook/generate',
  HANDBOOK_PREVIEW: '/api/handbook/preview',
  HANDBOOK_DOWNLOAD: '/api/handbook/download/:id',

  // ==================== 聊天系统 ====================
  CHAT_ROOMS: '/api/chat/rooms',
  CHAT_MESSAGES: '/api/chat/messages/:roomId',
  CHAT_SEND: '/api/chat/send',
  CHAT_USERS: '/api/chat/users',
}

// HTTP 状态码映射
export const HTTP_STATUS_CODES = {
  OK: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
}

// 请求超时设置 (毫秒)
export const REQUEST_TIMEOUT = 10000

// 自动重试配置
export const RETRY_CONFIG = {
  maxRetries: 3,
  retryableStatusCodes: [408, 429, 500, 502, 503, 504],
  retryDelay: 1000, // 初始延迟，每次重试翻倍
}

// 权限列表
export const PERMISSIONS = {
  // 培训权限
  TRAINING_VIEW: 'training:view',
  TRAINING_CREATE: 'training:create',
  TRAINING_EDIT: 'training:edit',
  TRAINING_DELETE: 'training:delete',
  TRAINING_PUBLISH: 'training:publish',

  // 报名权限
  REGISTRATION_VIEW: 'registration:view',
  REGISTRATION_APPROVE: 'registration:approve',
  REGISTRATION_REJECT: 'registration:reject',

  // 座位权限
  SEAT_VIEW: 'seat:view',
  SEAT_ALLOCATE: 'seat:allocate',
  SEAT_MANAGE: 'seat:manage',

  // 签到权限
  CHECKIN_VIEW: 'checkin:view',
  CHECKIN_MANAGE: 'checkin:manage',

  // 分组权限
  GROUP_VIEW: 'group:view',
  GROUP_MANAGE: 'group:manage',

  // 任务权限
  TASK_VIEW: 'task:view',
  TASK_CREATE: 'task:create',
  TASK_MANAGE: 'task:manage',

  // 通知权限
  NOTICE_SEND: 'notice:send',
  NOTICE_TEMPLATE: 'notice:template',

  // 系统权限
  SYSTEM_ADMIN: 'system:admin',
  TENANT_MANAGE: 'tenant:manage',
}

// 错误码定义
export const ERROR_CODES = {
  // 认证错误
  INVALID_CREDENTIALS: 'AUTH_001',
  TOKEN_EXPIRED: 'AUTH_002',
  TOKEN_INVALID: 'AUTH_003',
  UNAUTHORIZED: 'AUTH_004',

  // 业务错误
  TRAINING_NOT_FOUND: 'BIZ_001',
  REGISTRATION_NOT_FOUND: 'BIZ_002',
  SEAT_NOT_AVAILABLE: 'BIZ_003',
  DUPLICATE_REGISTRATION: 'BIZ_004',

  // 系统错误
  INTERNAL_ERROR: 'SYS_001',
  SERVICE_UNAVAILABLE: 'SYS_002',
  DATABASE_ERROR: 'SYS_003',
}
