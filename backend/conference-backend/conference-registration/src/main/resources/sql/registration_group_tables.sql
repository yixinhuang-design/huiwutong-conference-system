-- ============================================
-- 报名管理系统 - 数据库表创建脚本
-- 创建时间：2026-03-24 20:30
-- 作者：AI Executive
-- 说明：包含分组管理、审核历史、审核规则相关表
-- ============================================

-- --------------------------------------------
-- 表1：报名分组表（registration_group）
-- 说明：存储报名分组的配置信息
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `registration_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分组名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分组描述',
    `color` VARCHAR(20) DEFAULT '#3b82f6' COMMENT '分组颜色（十六进制）',
    `icon` VARCHAR(50) DEFAULT 'fas fa-users' COMMENT '分组图标（Font Awesome类名）',
    `type` VARCHAR(20) DEFAULT 'manual' COMMENT '分组类型：manual手动/auto自动',
    `sort` INT DEFAULT 0 COMMENT '排序序号（数字越小越靠前）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `deleted` BOOLEAN DEFAULT FALSE COMMENT '是否删除（软删除标记）',
    PRIMARY KEY (`id`),
    KEY `idx_conference` (`conference_id`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名分组表';

-- --------------------------------------------
-- 表2：分组成员关联表（registration_group_member）
-- 说明：存储分组与报名的多对多关联关系
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `registration_group_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `group_id` BIGINT NOT NULL COMMENT '分组ID',
    `registration_id` BIGINT NOT NULL COMMENT '报名ID',
    `is_staff` BOOLEAN DEFAULT FALSE COMMENT '是否为工作人员',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入分组时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_member` (`group_id`, `registration_id`) COMMENT '同一分组中同一报名只能存在一条记录',
    KEY `idx_group` (`group_id`),
    KEY `idx_registration` (`registration_id`),
    KEY `idx_staff` (`is_staff`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分组成员关联表';

-- --------------------------------------------
-- 表3：审核日志表（registration_audit_log）
-- 说明：记录报名审核的操作历史
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `registration_audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `registration_id` BIGINT NOT NULL COMMENT '报名ID',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `auditor_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    `action` VARCHAR(20) NOT NULL COMMENT '审核操作：approve通过/reject拒绝',
    `comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见/备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_registration` (`registration_id`),
    KEY `idx_auditor` (`auditor_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核日志表';

-- --------------------------------------------
-- 表4：审核规则表（registration_audit_rule）
-- 说明：存储自动审核的规则配置
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `registration_audit_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `rule_type` VARCHAR(20) NOT NULL COMMENT '规则类型：keyword关键词/blacklist黑名单/whitelist白名单',
    `rule_content` TEXT NOT NULL COMMENT '规则内容（JSON格式）',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `enabled` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `priority` INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_conference` (`conference_id`),
    KEY `idx_type` (`rule_type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核规则表';

-- --------------------------------------------
-- 索引优化建议
-- --------------------------------------------

-- 为常用查询添加复合索引
ALTER TABLE `registration_group_member` 
    ADD INDEX `idx_group_staff` (`group_id`, `is_staff`);

ALTER TABLE `registration_audit_log` 
    ADD INDEX `idx_registration_time` (`registration_id`, `create_time`);

ALTER TABLE `registration_audit_rule` 
    ADD INDEX `idx_conference_enabled` (`conference_id`, `enabled`);

-- --------------------------------------------
-- 初始化数据（可选）
-- --------------------------------------------

-- 插入默认审核规则示例
INSERT INTO `registration_audit_rule` 
    (`conference_id`, `rule_type`, `rule_content`, `rule_name`, `enabled`, `priority`)
VALUES 
    (0, 'keyword', '{"keywords":["测试","test"],"action":"reject"}', '测试账号过滤', TRUE, 10),
    (0, 'whitelist', '{"phoneRegex":"^1[3-9]\\d{9}$","action":"approve"}', '手机号格式验证', TRUE, 5)
ON DUPLICATE KEY UPDATE `rule_name` = VALUES(`rule_name`);

-- --------------------------------------------
-- 数据清理脚本（用于开发/测试环境）
-- --------------------------------------------

-- 清空所有表数据（保留表结构）
-- TRUNCATE TABLE `registration_group_member`;
-- TRUNCATE TABLE `registration_group`;
-- TRUNCATE TABLE `registration_audit_log`;
-- TRUNCATE TABLE `registration_audit_rule`;

-- 删除所有表（慎用！）
-- DROP TABLE IF EXISTS `registration_group_member`;
-- DROP TABLE IF EXISTS `registration_group`;
-- DROP TABLE IF EXISTS `registration_audit_log`;
-- DROP TABLE IF EXISTS `registration_audit_rule`;

-- --------------------------------------------
-- 权限设置建议
-- --------------------------------------------

-- 创建数据库用户（如果需要）
-- CREATE USER IF NOT EXISTS 'conference_app'@'%' IDENTIFIED BY 'your_password';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON conference_registration.* TO 'conference_app'@'%';
-- FLUSH PRIVILEGES;

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
        'registration_group',
        'registration_group_member',
        'registration_audit_log',
        'registration_audit_rule'
    )
ORDER BY 
    TABLE_NAME;

-- --------------------------------------------
-- 性能优化建议
-- --------------------------------------------

-- 定期清理软删除的数据（超过90天）
-- DELETE FROM registration_group 
-- WHERE deleted = TRUE 
-- AND update_time < DATE_SUB(NOW(), INTERVAL 90 DAY);

-- 定期归档审核日志（超过1年）
-- 可考虑创建归档表或使用分区表

-- ============================================
-- 脚本结束
-- ============================================
