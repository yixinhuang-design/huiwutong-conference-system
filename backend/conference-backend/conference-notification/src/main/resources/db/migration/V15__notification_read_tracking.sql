-- ========================================
-- 通知已读跟踪表
-- 用于追踪用户阅读通知的行为
-- ========================================

CREATE TABLE IF NOT EXISTS conf_notification_read (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    notification_id BIGINT NOT NULL COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    read_time DATETIME NOT NULL COMMENT '阅读时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_user (notification_id, user_id),
    KEY idx_notification_id (notification_id),
    KEY idx_user_id (user_id),
    KEY idx_read_time (read_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知已读跟踪表';

-- ========================================
-- 添加通知失败记录表（可选）
-- 用于记录发送失败的详细信息，支持重试和问题排查
-- ========================================

CREATE TABLE IF NOT EXISTS conf_notification_failure (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    notification_id BIGINT NOT NULL COMMENT '通知ID',
    channel VARCHAR(20) NOT NULL COMMENT '渠道：sms/unipush/email/wechat',
    recipient VARCHAR(200) COMMENT '接收人（手机号/邮箱/用户ID）',
    error_message TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    status VARCHAR(20) DEFAULT 'failed' COMMENT '状态：failed/retrying/success',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_notification_id (notification_id),
    KEY idx_channel (channel),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知发送失败记录表';

-- ========================================
-- 为通知表添加重试次数字段（如果不存在）
-- ========================================

ALTER TABLE conf_notification
ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0 COMMENT '重试次数' AFTER failed_count;
