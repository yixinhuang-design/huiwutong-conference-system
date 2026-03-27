USE `conference_notification`;

-- ========================================
-- 通知模板表
-- ========================================
CREATE TABLE IF NOT EXISTS `notify_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `tenant_id` BIGINT NOT NULL,
    `template_code` VARCHAR(50) NOT NULL COMMENT '模板编码',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(20) NOT NULL COMMENT '类型: sms-短信, email-邮件, push-推送, wechat-微信',
    `title` VARCHAR(200) COMMENT '标题/主题',
    `content` TEXT NOT NULL COMMENT '内容模板',
    `variables` JSON COMMENT '变量列表(JSON)',
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_template_code` (`tenant_id`, `template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- ========================================
-- 通知记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `notify_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT COMMENT '会议ID',
    `template_id` BIGINT COMMENT '模板ID',
    `notify_type` VARCHAR(20) NOT NULL COMMENT '通知类型',
    `title` VARCHAR(200) COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `receiver_type` VARCHAR(20) COMMENT '接收人类型: all-全部, group-分组, individual-个人',
    `receiver_ids` JSON COMMENT '接收人ID列表',
    `total_count` INT DEFAULT 0 COMMENT '总发送数',
    `success_count` INT DEFAULT 0 COMMENT '成功数',
    `fail_count` INT DEFAULT 0 COMMENT '失败数',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待发送, 1-发送中, 2-已完成, 3-部分失败, 4-全部失败',
    `scheduled_time` DATETIME COMMENT '计划发送时间',
    `sent_time` DATETIME COMMENT '实际发送时间',
    `create_by` BIGINT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';

-- ========================================
-- 通知发送明细表
-- ========================================
CREATE TABLE IF NOT EXISTS `notify_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `record_id` BIGINT NOT NULL COMMENT '记录ID',
    `receiver_id` BIGINT COMMENT '接收人ID',
    `receiver_name` VARCHAR(50) COMMENT '接收人姓名',
    `receiver_contact` VARCHAR(100) NOT NULL COMMENT '接收联系方式',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待发送, 1-已发送, 2-已送达, 3-已读, 4-失败',
    `sent_time` DATETIME COMMENT '发送时间',
    `read_time` DATETIME COMMENT '阅读时间',
    `error_msg` VARCHAR(500) COMMENT '错误信息',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`),
    KEY `idx_receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知发送明细表';

-- ========================================
-- 预警规则表
-- ========================================
CREATE TABLE IF NOT EXISTS `notify_alert_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT COMMENT '会议ID(空表示全局)',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型: deadline-截止提醒, capacity-容量预警, sign_in-签到提醒',
    `trigger_condition` JSON NOT NULL COMMENT '触发条件(JSON)',
    `notify_template_id` BIGINT COMMENT '通知模板ID',
    `notify_targets` JSON COMMENT '通知目标',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `last_triggered` DATETIME COMMENT '上次触发时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_meeting` (`tenant_id`, `meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警规则表';

-- ========================================
-- 初始化通知模板
-- ========================================
INSERT INTO `notify_template` (`tenant_id`, `template_code`, `template_name`, `template_type`, `title`, `content`, `variables`) VALUES
(1, 'REG_SUCCESS', '报名成功通知', 'sms', NULL, '尊敬的${name}，您已成功报名${meetingName}，会议时间：${startTime}，地点：${venue}。请准时参加。', '["name","meetingName","startTime","venue"]'),
(1, 'REG_REVIEW', '报名审核通知', 'sms', NULL, '尊敬的${name}，您报名的${meetingName}审核${result}。${remark}', '["name","meetingName","result","remark"]'),
(1, 'MEETING_REMINDER', '会议提醒', 'push', '会议即将开始', '${meetingName}将于${startTime}在${venue}举行，请提前做好准备。', '["meetingName","startTime","venue"]'),
(1, 'SIGN_IN_REMINDER', '签到提醒', 'push', '请及时签到', '${meetingName}签到已开始，请尽快完成签到。', '["meetingName"]'),
(1, 'SEAT_ASSIGNED', '座位分配通知', 'push', '座位已分配', '尊敬的${name}，您在${meetingName}的座位为：${seatLabel}。', '["name","meetingName","seatLabel"]');
