-- =====================================================
-- 数据监控模块 - 建表脚本
-- 数据库: conference_db (conference-data 服务使用)
-- 包含5张监控表 + 初始服务健康数据
-- =====================================================

USE conference_db;

-- 1. 系统指标快照表
CREATE TABLE IF NOT EXISTS `data_system_metrics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `cpu_usage` DECIMAL(5,1) DEFAULT NULL COMMENT 'CPU使用率(%)',
    `memory_usage` DECIMAL(5,1) DEFAULT NULL COMMENT '内存使用率(%)',
    `memory_total` BIGINT DEFAULT NULL COMMENT '总内存(bytes)',
    `memory_used` BIGINT DEFAULT NULL COMMENT '已用内存(bytes)',
    `heap_usage` DECIMAL(5,1) DEFAULT NULL COMMENT '堆内存使用率(%)',
    `heap_total` BIGINT DEFAULT NULL COMMENT '堆内存总量(bytes)',
    `heap_used` BIGINT DEFAULT NULL COMMENT '堆内存已用(bytes)',
    `disk_usage` DECIMAL(5,1) DEFAULT NULL COMMENT '磁盘使用率(%)',
    `disk_total` BIGINT DEFAULT NULL COMMENT '磁盘总量(bytes)',
    `disk_used` BIGINT DEFAULT NULL COMMENT '磁盘已用(bytes)',
    `thread_count` INT DEFAULT NULL COMMENT '活跃线程数',
    `active_connections` INT DEFAULT NULL COMMENT '活跃DB连接数',
    `gc_count` BIGINT DEFAULT NULL COMMENT 'GC次数',
    `gc_time` BIGINT DEFAULT NULL COMMENT 'GC耗时(ms)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    PRIMARY KEY (`id`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统指标快照表';

-- 2. 服务健康状态表
CREATE TABLE IF NOT EXISTS `data_service_health` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `service_name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `service_url` VARCHAR(500) DEFAULT NULL COMMENT '服务地址',
    `status` VARCHAR(20) NOT NULL DEFAULT 'unknown' COMMENT '状态: healthy/offline/unknown',
    `response_time` BIGINT DEFAULT NULL COMMENT '响应时间(ms)',
    `last_check_time` DATETIME DEFAULT NULL COMMENT '最后检查时间',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_service_name` (`service_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务健康状态表';

-- 3. API访问日志表
CREATE TABLE IF NOT EXISTS `data_api_access_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `service_name` VARCHAR(100) DEFAULT NULL COMMENT '服务名称',
    `method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
    `path` VARCHAR(500) DEFAULT NULL COMMENT '请求路径',
    `status_code` INT DEFAULT NULL COMMENT 'HTTP状态码',
    `response_time` BIGINT DEFAULT NULL COMMENT '响应时间(ms)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    PRIMARY KEY (`id`),
    INDEX `idx_created_time` (`created_time`),
    INDEX `idx_service_name` (`service_name`),
    INDEX `idx_status_code` (`status_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API访问日志表';

-- 4. 系统事件表
CREATE TABLE IF NOT EXISTS `data_system_event` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `event_type` VARCHAR(50) DEFAULT NULL COMMENT '事件类型: info/warning/error',
    `event_source` VARCHAR(100) DEFAULT NULL COMMENT '事件来源',
    `message` TEXT COMMENT '事件消息',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    PRIMARY KEY (`id`),
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统事件表';

-- 5. 用户活动记录表
CREATE TABLE IF NOT EXISTS `data_user_activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '用户名',
    `action` VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `action_type` VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标类型',
    `target_id` VARCHAR(100) DEFAULT NULL COMMENT '目标ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活动记录表';

-- =====================================================
-- 初始化服务健康数据（各微服务信息）
-- =====================================================
INSERT IGNORE INTO `data_service_health` (`service_name`, `service_url`, `status`) VALUES
    ('Conference Gateway', 'http://localhost:8080', 'unknown'),
    ('Registration Service', 'http://localhost:8082', 'unknown'),
    ('Notification Service', 'http://localhost:8083', 'unknown'),
    ('Data Service', 'http://localhost:8088', 'unknown'),
    ('AI Service', 'http://localhost:8089', 'unknown'),
    ('MySQL Database', 'jdbc:mysql://localhost:3308', 'unknown');
