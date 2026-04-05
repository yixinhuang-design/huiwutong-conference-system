-- ============================================
-- 群组管理系统 - 数据库表创建脚本
-- 创建时间：2026-03-24 21:20
-- 作者：AI Executive
-- 说明：使用Web Key进行信息持久化
-- ============================================

-- --------------------------------------------
-- 表1：会议群组表（conference_group）
-- 说明：存储群组的基本信息
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `conference_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '群组ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `group_name` VARCHAR(200) NOT NULL COMMENT '群组名称',
    `group_type` VARCHAR(20) NOT NULL COMMENT '群组类型：staff工作人员群/student学员群',
    `wechat_chat_id` VARCHAR(100) DEFAULT NULL COMMENT '微信企业群ID',
    `owner_id` BIGINT DEFAULT NULL COMMENT '群主ID',
    `owner_name` VARCHAR(100) DEFAULT NULL COMMENT '群主名称',
    `announcement` TEXT DEFAULT NULL COMMENT '群公告',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '群头像URL',
    `welcome_message` TEXT DEFAULT NULL COMMENT '入群欢迎语',
    `qr_code_url` VARCHAR(500) DEFAULT NULL COMMENT '群二维码URL',
    `member_count` INT DEFAULT 0 COMMENT '成员数量',
    `admin_count` INT DEFAULT 0 COMMENT '管理员数量',
    `task_count` INT DEFAULT 0 COMMENT '任务数量',
    `message_count` INT DEFAULT 0 COMMENT '消息数量',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '群组状态：active/disabled/deleted',
    `config_json` TEXT DEFAULT NULL COMMENT '配置JSON（扩展字段）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `web_key` VARCHAR(64) NOT NULL COMMENT 'Web Key（用于前端识别和访问）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_web_key` (`web_key`) COMMENT 'Web Key唯一索引',
    KEY `idx_conference` (`conference_id`),
    KEY `idx_type` (`group_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议群组表';

-- --------------------------------------------
-- 表2：群组成员表（conference_group_member）
-- 说明：存储群组成员信息
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `conference_group_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员ID',
    `group_id` BIGINT NOT NULL COMMENT '群组ID',
    `member_id` BIGINT NOT NULL COMMENT '成员ID',
    `member_name` VARCHAR(100) DEFAULT NULL COMMENT '成员名称',
    `role` VARCHAR(20) DEFAULT 'member' COMMENT '成员角色：member/admin/owner',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
    `last_active_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    `web_key` VARCHAR(64) NOT NULL COMMENT 'Web Key（用于前端识别和访问）',
    `ext_json` TEXT DEFAULT NULL COMMENT '扩展信息JSON',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_web_key` (`web_key`) COMMENT 'Web Key唯一索引',
    KEY `idx_group` (`group_id`),
    KEY `idx_member` (`member_id`),
    KEY `idx_role` (`role`),
    KEY `idx_join_time` (`join_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';

-- --------------------------------------------
-- 表3：群组任务表（conference_group_task）
-- 说明：存储群组任务信息
-- --------------------------------------------
CREATE TABLE IF TABLE EXISTS `conference_group_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `group_id` BIGINT NOT NULL COMMENT '群组ID',
    `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `description` TEXT DEFAULT NULL COMMENT '任务描述',
    `task_type` VARCHAR(20) DEFAULT 'general' COMMENT '任务类型：meeting/reminder/collect/feedback',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '任务状态：pending/in_progress/completed/cancelled',
    `priority` VARCHAR(20) DEFAULT 'medium' COMMENT '优先级：high/medium/low',
    `assignee_ids` VARCHAR(500) DEFAULT NULL COMMENT '指派给（用户ID列表，逗号分隔）',
    `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
    `progress` INT DEFAULT 0 COMMENT '完成进度（0-100）',
    `total_target` INT DEFAULT 0 COMMENT '总目标数量',
    `completed_count` INT DEFAULT 0 COMMENT '已完成数量',
    `notes` TEXT DEFAULT NULL COMMENT '任务备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update_time',
    `reminder_time` DATETIME DEFAULT NULL COMMENT '提醒时间',
    `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `web_key` VARCHAR(64) NOT NULL COMMENT 'Web Key（用于前端识别和访问）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_web_key` (`web_key`) COMMENT 'Web Key唯一索引',
    KEY `idx_group` (`group_id`),
    `idx_status` (`status`),
    `idx_priority` (`priority`),
    KEY `idx_deadline` (`deadline`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组任务表';

-- --------------------------------------------
-- 表4：群组消息表（conference_group_message）
-- 说明：存储群组消息记录
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `conference_group_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `group_id` BIGINT NOT NULL COMMENT '群组ID',
    `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型：text/image/file/voice/video/card',
    `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID',
    `sender_name` VARCHAR(100) DEFAULT NULL COMMENT '发送者名称',
    `content` TEXT DEFAULT NULL COMMENT '消息内容',
    `media_url` VARCHAR(500) DEFAULT NULL COMMENT '媒体URL（图片/文件/音视频）',
    `card_json` TEXT DEFAULT NULL COMMENT '卡片JSON',
    `is_bot` BOOLEAN DEFAULT FALSE COMMENT '是否为机器人消息',
    `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `web_key` VARCHAR(64) NOT NULL COMMENT 'Web Key（用于前端识别和访问）',
    `msg_id` BIGINT DEFAULT NULL COMMENT '消息序号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_web_key` (`web_key`) COMMENT 'Web Key唯一索引',
    KEY `idx_group` (`group_id`),
    KEY `idx_sender` (`sender_id`),
    `KEY `idx_send_time` (`send_time`),
    KEY `idx_msg_id` (`msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组消息表';

-- --------------------------------------------
-- 索引优化建议
-- --------------------------------------------

-- 复合索引优化
ALTER TABLE conference_group_member 
    ADD INDEX idx_group_role (group_id, role);

ALTER TABLE conference_group_message 
    ADD INDEX idx_group_time (group_id, send_time);

-- --------------------------------------------
-- 初始化数据（可选）
-- --------------------------------------------

-- 示例：插入测试群组
INSERT INTO conference_group (
    conference_id, group_name, group_type, 
    owner_id, owner_name, web_key, 
    member_count, status
) VALUES (
    1, '测试工作人员群', 'staff',
    1, '系统管理员', 'group_test_123456789abc',
    0, 'active'
);

-- --------------------------------------------
-- 数据完整性检查
-- --------------------------------------------

-- 检查表是否创建成功
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    UPDATE_TIME
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN (
        'conference_group',
        'conference_group_member',
        'conference_group_task',
        'conference_group_message'
    )
ORDER BY 
    TABLE_NAME;

-- ============================================
-- 脚本结束
-- ============================================
