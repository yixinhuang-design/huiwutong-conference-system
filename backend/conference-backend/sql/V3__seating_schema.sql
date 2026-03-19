USE `conference_seating`;

-- ========================================
-- 会场布局表
-- ========================================
CREATE TABLE IF NOT EXISTS `seat_venue_layout` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '布局ID',
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `layout_name` VARCHAR(100) NOT NULL COMMENT '布局名称',
    `layout_type` VARCHAR(50) NOT NULL COMMENT '布局类型: classroom-教室式, theater-剧院式, roundtable-圆桌式, u_shape-U型',
    `rows` INT NOT NULL COMMENT '总行数',
    `cols` INT NOT NULL COMMENT '总列数',
    `total_seats` INT DEFAULT 0 COMMENT '总座位数',
    `assigned_seats` INT DEFAULT 0 COMMENT '已分配座位数',
    `layout_data` JSON COMMENT '布局配置(JSON)',
    `aisle_config` JSON COMMENT '过道配置(JSON)',
    `stage_position` VARCHAR(20) DEFAULT 'front' COMMENT '舞台位置: front, back, left, right',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认布局',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_meeting_id` (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会场布局表';

-- ========================================
-- 座位表
-- ========================================
CREATE TABLE IF NOT EXISTS `seat_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '座位ID',
    `tenant_id` BIGINT NOT NULL,
    `layout_id` BIGINT NOT NULL COMMENT '布局ID',
    `row_num` INT NOT NULL COMMENT '行号',
    `col_num` INT NOT NULL COMMENT '列号',
    `seat_label` VARCHAR(20) COMMENT '座位标签(如A1, B2)',
    `seat_type` VARCHAR(20) DEFAULT 'normal' COMMENT '座位类型: normal-普通, vip-VIP, disabled-无障碍, reserved-预留',
    `area_name` VARCHAR(50) COMMENT '区域名称',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-空闲, 1-已分配, 2-锁定, 3-不可用',
    `registration_id` BIGINT COMMENT '报名ID(已分配时)',
    `assigned_time` DATETIME COMMENT '分配时间',
    `assigned_by` BIGINT COMMENT '分配人',
    `extra_info` JSON COMMENT '额外信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_layout_id` (`layout_id`),
    KEY `idx_registration_id` (`registration_id`),
    UNIQUE KEY `uk_layout_position` (`layout_id`, `row_num`, `col_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- ========================================
-- 排座规则表
-- ========================================
CREATE TABLE IF NOT EXISTS `seat_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `layout_id` BIGINT NOT NULL COMMENT '布局ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型: auto-自动, manual-手动, group-按分组, random-随机',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `rule_config` JSON COMMENT '规则配置',
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_layout_id` (`layout_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排座规则表';

-- ========================================
-- 排座历史表
-- ========================================
CREATE TABLE IF NOT EXISTS `seat_assignment_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `layout_id` BIGINT NOT NULL,
    `seat_id` BIGINT NOT NULL,
    `registration_id` BIGINT,
    `action` VARCHAR(20) NOT NULL COMMENT '操作: assign-分配, unassign-取消, swap-交换',
    `operator_id` BIGINT COMMENT '操作人',
    `reason` VARCHAR(200) COMMENT '原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_layout_id` (`layout_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排座历史表';
