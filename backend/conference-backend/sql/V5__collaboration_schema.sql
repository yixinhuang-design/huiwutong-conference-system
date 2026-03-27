USE `conference_collaboration`;

-- ========================================
-- 任务表
-- ========================================
CREATE TABLE IF NOT EXISTS `task_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT COMMENT '关联会议ID',
    `task_code` VARCHAR(50) NOT NULL COMMENT '任务编号',
    `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(50) COMMENT '任务类型',
    `description` TEXT COMMENT '任务描述',
    `priority` TINYINT DEFAULT 2 COMMENT '优先级: 1-低, 2-中, 3-高, 4-紧急',
    `start_time` DATETIME COMMENT '计划开始时间',
    `end_time` DATETIME COMMENT '计划结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `progress` INT DEFAULT 0 COMMENT '进度百分比',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待分配, 1-待开始, 2-进行中, 3-已完成, 4-已取消, 5-已逾期',
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `assignee_ids` JSON COMMENT '执行人ID列表',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父任务ID',
    `attachments` JSON COMMENT '附件列表',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_task_code` (`tenant_id`, `task_code`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_status` (`status`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- ========================================
-- 任务执行人表
-- ========================================
CREATE TABLE IF NOT EXISTS `task_assignee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) DEFAULT 'executor' COMMENT '角色: executor-执行者, reviewer-审核者, observer-观察者',
    `is_main` TINYINT DEFAULT 0 COMMENT '是否主负责人',
    `status` TINYINT DEFAULT 0 COMMENT '个人状态: 0-待接受, 1-已接受, 2-已拒绝',
    `accept_time` DATETIME COMMENT '接受时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_user` (`task_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行人表';

-- ========================================
-- 任务日志表
-- ========================================
CREATE TABLE IF NOT EXISTS `task_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `old_value` TEXT COMMENT '旧值',
    `new_value` TEXT COMMENT '新值',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务日志表';

-- ========================================
-- 任务评论表
-- ========================================
CREATE TABLE IF NOT EXISTS `task_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID',
    `attachments` JSON COMMENT '附件',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务评论表';

-- ========================================
-- 物资管理表
-- ========================================
CREATE TABLE IF NOT EXISTS `resource_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物资ID',
    `tenant_id` BIGINT NOT NULL,
    `item_code` VARCHAR(50) NOT NULL COMMENT '物资编码',
    `item_name` VARCHAR(100) NOT NULL COMMENT '物资名称',
    `category` VARCHAR(50) COMMENT '分类',
    `unit` VARCHAR(20) COMMENT '单位',
    `total_quantity` INT DEFAULT 0 COMMENT '总数量',
    `available_quantity` INT DEFAULT 0 COMMENT '可用数量',
    `location` VARCHAR(200) COMMENT '存放位置',
    `image_url` VARCHAR(255) COMMENT '图片',
    `remark` VARCHAR(500),
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_item_code` (`tenant_id`, `item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资表';

-- ========================================
-- 物资领用记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `resource_usage` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT COMMENT '会议ID',
    `item_id` BIGINT NOT NULL COMMENT '物资ID',
    `quantity` INT NOT NULL COMMENT '领用数量',
    `user_id` BIGINT NOT NULL COMMENT '领用人',
    `purpose` VARCHAR(200) COMMENT '用途',
    `usage_time` DATETIME COMMENT '使用时间',
    `return_time` DATETIME COMMENT '归还时间',
    `actual_return_time` DATETIME COMMENT '实际归还时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-申请中, 1-已领用, 2-已归还, 3-已损耗',
    `remark` VARCHAR(500),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资领用记录表';

-- ========================================
-- 消息表 (WebSocket消息持久化)
-- ========================================
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT COMMENT '会议ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送人ID',
    `receiver_id` BIGINT COMMENT '接收人ID(空表示广播)',
    `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型: text-文本, image-图片, file-文件, system-系统',
    `content` TEXT NOT NULL COMMENT '内容',
    `read_status` TINYINT DEFAULT 0 COMMENT '已读状态',
    `read_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';
