USE `conference_registration`;

-- ========================================
-- 会议/培训班表
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_meeting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `meeting_code` VARCHAR(50) NOT NULL COMMENT '会议编号',
    `meeting_name` VARCHAR(200) NOT NULL COMMENT '会议名称',
    `meeting_type` VARCHAR(50) COMMENT '会议类型',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `venue_name` VARCHAR(200) COMMENT '会场名称',
    `venue_address` VARCHAR(500) COMMENT '会场地址',
    `max_participants` INT DEFAULT 0 COMMENT '最大参会人数',
    `current_participants` INT DEFAULT 0 COMMENT '当前报名人数',
    `registration_start` DATETIME COMMENT '报名开始时间',
    `registration_end` DATETIME COMMENT '报名截止时间',
    `description` TEXT COMMENT '会议描述',
    `agenda` TEXT COMMENT '会议议程(JSON)',
    `contact_info` VARCHAR(500) COMMENT '联系信息',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-报名中, 2-报名截止, 3-进行中, 4-已结束, 5-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` BIGINT,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_meeting_code` (`tenant_id`, `meeting_code`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议/培训班表';

-- ========================================
-- 报名表
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `user_id` BIGINT COMMENT '用户ID(已注册用户)',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `id_card` VARCHAR(20) COMMENT '身份证号',
    `gender` TINYINT COMMENT '性别',
    `organization` VARCHAR(200) COMMENT '单位/组织',
    `position` VARCHAR(100) COMMENT '职位',
    `email` VARCHAR(100) COMMENT '邮箱',
    `dietary_requirements` VARCHAR(200) COMMENT '饮食要求',
    `accommodation_required` TINYINT DEFAULT 0 COMMENT '是否需要住宿',
    `arrival_time` DATETIME COMMENT '预计到达时间',
    `departure_time` DATETIME COMMENT '预计离开时间',
    `extra_info` JSON COMMENT '额外信息(JSON)',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-已通过, 2-已拒绝, 3-已取消, 4-已签到',
    `review_time` DATETIME COMMENT '审核时间',
    `review_by` BIGINT COMMENT '审核人ID',
    `review_remark` VARCHAR(500) COMMENT '审核备注',
    `sign_in_time` DATETIME COMMENT '签到时间',
    `qr_code` VARCHAR(200) COMMENT '签到二维码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_meeting` (`tenant_id`, `meeting_id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名表';

-- ========================================
-- 分组表
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `group_name` VARCHAR(100) NOT NULL COMMENT '分组名称',
    `group_type` VARCHAR(50) COMMENT '分组类型',
    `max_members` INT DEFAULT 0 COMMENT '最大人数',
    `current_members` INT DEFAULT 0 COMMENT '当前人数',
    `leader_id` BIGINT COMMENT '组长ID',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(500),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_meeting_id` (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分组表';

-- ========================================
-- 分组成员表
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_group_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_id` BIGINT NOT NULL COMMENT '分组ID',
    `registration_id` BIGINT NOT NULL COMMENT '报名ID',
    `is_leader` TINYINT DEFAULT 0 COMMENT '是否组长',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_registration` (`group_id`, `registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分组成员表';
