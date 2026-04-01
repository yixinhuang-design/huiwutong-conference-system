-- =====================================================
-- 通知已读跟踪表
-- 数据库: conference_notification
-- 用途: 记录每个用户对每条通知的已读状态
-- =====================================================

USE conference_notification;

CREATE TABLE IF NOT EXISTS `conf_notification_read` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `notification_id` BIGINT NOT NULL COMMENT '通知ID（关联conf_notification.id）',
    `user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '用户ID（关联sys_user.id）',
    `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notification_user` (`notification_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_notification_id` (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知已读跟踪表';
