-- =====================================================================
-- 协同服务数据库 DDL (与 Java Entity 完全匹配)
-- 修正版：确保所有字段与 Java 实体类一一对应
-- =====================================================================

USE `conference_collaboration`;

-- ========================================
-- 任务表 (对应 TaskInfo.java)
-- ========================================
DROP TABLE IF EXISTS `task_info`;
CREATE TABLE `task_info` (
    `id` BIGINT NOT NULL COMMENT '任务ID (雪花算法)',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `conference_id` BIGINT COMMENT '关联会议ID',
    `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(50) COMMENT '任务类型: checkin/room_check/evaluation/collection/custom',
    `category` VARCHAR(50) COMMENT '任务类别: venue/student/custom',
    `completion_method` VARCHAR(50) COMMENT '完成方式: text_image/location/questionnaire/collection',
    `description` TEXT COMMENT '任务描述',
    `priority` VARCHAR(20) DEFAULT 'medium' COMMENT '优先级: low/medium/high/urgent',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/in_progress/completed/overdue/cancelled',
    `deadline` DATETIME COMMENT '截止时间',
    `progress` INT DEFAULT 0 COMMENT '进度百分比',
    `creator_id` BIGINT COMMENT '创建人ID',
    `owner_name` VARCHAR(100) COMMENT '负责人姓名',
    `target_groups` TEXT COMMENT '目标群组(JSON)',
    `config` TEXT COMMENT '完成配置(JSON)',
    `attachments` TEXT COMMENT '附件列表(JSON)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0=正常,1=删除)',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_conference_id` (`conference_id`),
    KEY `idx_status` (`status`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- ========================================
-- 任务执行人表 (对应 TaskAssignee.java)
-- ========================================
DROP TABLE IF EXISTS `task_assignee`;
CREATE TABLE `task_assignee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户姓名',
    `role` VARCHAR(20) DEFAULT 'executor' COMMENT '角色: executor/assistant/observer',
    `is_main` TINYINT DEFAULT 0 COMMENT '是否主负责人',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/accepted/completed/rejected',
    `submit_content` TEXT COMMENT '提交内容',
    `submit_images` TEXT COMMENT '提交图片(JSON)',
    `submit_location` TEXT COMMENT '提交位置(JSON)',
    `submit_time` DATETIME COMMENT '提交时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_user` (`task_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行人表';

-- ========================================
-- 任务日志表 (对应 TaskLog.java)
-- ========================================
DROP TABLE IF EXISTS `task_log`;
CREATE TABLE `task_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(100) COMMENT '操作人姓名',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型: created/assigned/started/submitted/completed/cancelled/urged',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务日志表';

-- ========================================
-- 任务评论表 (对应 TaskComment.java - 新建)
-- ========================================
DROP TABLE IF EXISTS `task_comment`;
CREATE TABLE `task_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户姓名',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID(用于回复)',
    `attachments` TEXT COMMENT '附件(JSON)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0=正常,1=删除)',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务评论表';

-- ========================================
-- 任务模板表 (对应 TaskTemplate.java)
-- ========================================
DROP TABLE IF EXISTS `task_template`;
CREATE TABLE `task_template` (
    `id` BIGINT NOT NULL COMMENT '主键ID(雪花算法)',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `task_type` VARCHAR(50) COMMENT '任务类型',
    `category` VARCHAR(50) COMMENT '任务类别(venue/student/custom)',
    `completion_method` VARCHAR(50) COMMENT '完成方式(text_image/location/questionnaire/collection)',
    `description` VARCHAR(500) COMMENT '任务描述',
    `priority` VARCHAR(20) DEFAULT 'medium' COMMENT '优先级(low/medium/high/urgent)',
    `config` TEXT COMMENT '完成配置(JSON格式)',
    `icon` VARCHAR(100) COMMENT '模板图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统模板(1=是,0=否)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0=正常,1=删除)',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_category` (`tenant_id`, `category`, `deleted`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务模板表';

-- ========================================
-- 系统预设模板数据
-- ========================================
INSERT INTO `task_template` (`id`, `tenant_id`, `template_name`, `task_type`, `category`, `completion_method`, `description`, `priority`, `config`, `icon`, `sort_order`, `is_system`) VALUES
(1, 2027317834622709762, '会场设备检查', 'venue_check', 'venue', 'text_image', '检查会场设备是否正常', 'high', '{"minTextLength":10,"minImageCount":1}', 'el-icon-monitor', 1, 1),
(2, 2027317834622709762, '学员接待引导', 'reception', 'venue', 'location', '在指定位置接待引导学员', 'medium', '{"targetLocation":{"lat":0,"lng":0},"maxDistance":200}', 'el-icon-guide', 2, 1),
(3, 2027317834622709762, '每日签到', 'checkin', 'student', 'location', '学员每日签到打卡', 'medium', '{"targetLocation":{"lat":0,"lng":0},"maxDistance":100}', 'el-icon-location', 3, 1),
(4, 2027317834622709762, '晚间查寝', 'room_check', 'student', 'text_image', '检查学员住宿情况', 'high', '{"minTextLength":5,"minImageCount":1}', 'el-icon-house', 4, 1),
(5, 2027317834622709762, '课程评价', 'evaluation', 'student', 'questionnaire', '学员对课程进行评价', 'medium', '{"questions":[{"title":"课程内容满意度","type":"rating","required":true},{"title":"讲师授课水平","type":"rating","required":true},{"title":"您的建议","type":"text","required":false}]}', 'el-icon-document', 5, 1),
(6, 2027317834622709762, '用餐意向征集', 'collection', 'student', 'collection', '征集学员用餐偏好', 'low', '{"fields":[{"label":"饮食禁忌","type":"text","required":true},{"label":"餐食偏好","type":"select","options":["荤素均可","素食","清真"],"required":true}]}', 'el-icon-food', 6, 1),
(7, 2027317834622709762, '自定义任务', 'custom', 'custom', 'text_image', '自定义的通用任务', 'medium', '{"minTextLength":0,"minImageCount":0}', 'el-icon-edit', 7, 1);

-- ========================================
-- sys_user 表需要增加 client_id 列 (UniPush CID)
-- ========================================
-- ALTER TABLE `conference_auth`.`sys_user` ADD COLUMN `client_id` VARCHAR(100) COMMENT 'UniPush客户端ID' AFTER `phone`;
