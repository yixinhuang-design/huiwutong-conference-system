-- =====================================================
-- 学员手册相关表（conf_handbook, conf_handbook_discussion）
-- 模块: conference-registration (端口 8082)
-- =====================================================

-- 手册配置主表
CREATE TABLE IF NOT EXISTS `conf_handbook` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '手册标题',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已生成, 2-已发布',
    `cover_config` TEXT DEFAULT NULL COMMENT '封面配置JSON',
    `toc_config` TEXT DEFAULT NULL COMMENT '目录配置JSON',
    `roster_config` TEXT DEFAULT NULL COMMENT '名册配置JSON',
    `roster_fields` TEXT DEFAULT NULL COMMENT '名册字段配置JSON',
    `seating_config` TEXT DEFAULT NULL COMMENT '座位配置JSON',
    `transport_config` TEXT DEFAULT NULL COMMENT '乘车配置JSON',
    `hotel_config` TEXT DEFAULT NULL COMMENT '住宿配置JSON',
    `meal_config` TEXT DEFAULT NULL COMMENT '就餐配置JSON',
    `discussion_config` TEXT DEFAULT NULL COMMENT '讨论配置JSON',
    `backcover_config` TEXT DEFAULT NULL COMMENT '封底配置JSON',
    `notes_content` TEXT DEFAULT NULL COMMENT '其他事项内容',
    `sections_config` TEXT DEFAULT NULL COMMENT '板块启用和排序配置JSON',
    `grouping_config` TEXT DEFAULT NULL COMMENT '分组配置JSON',
    `pdf_url` VARCHAR(500) DEFAULT NULL COMMENT '生成的PDF路径',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_handbook_meeting` (`meeting_id`),
    INDEX `idx_handbook_tenant` (`tenant_id`),
    UNIQUE INDEX `uk_handbook_meeting_tenant` (`meeting_id`, `tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学员手册配置表';

-- 手册讨论题目表
CREATE TABLE IF NOT EXISTS `conf_handbook_discussion` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `handbook_id` BIGINT NOT NULL COMMENT '手册ID',
    `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    `content` TEXT NOT NULL COMMENT '讨论题目内容',
    `reference` TEXT DEFAULT NULL COMMENT '参考说明',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_discussion_handbook` (`handbook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='手册讨论题目表';
