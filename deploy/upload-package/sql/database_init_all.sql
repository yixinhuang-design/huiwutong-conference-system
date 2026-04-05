-- =====================================================================
-- 智能会务系统 - 全数据库综合初始化脚本
-- 生成日期: 2026-03-22
-- 说明: 包含所有微服务的数据库创建、表结构、索引和初始数据
-- =====================================================================
-- 数据库清单:
--   1. conference_auth        (认证服务 - 端口8081)
--   2. conference_registration (报名服务 - 端口8082)
--   3. conference_notification (通知服务 - 端口8083)
--   4. conference_seating      (排座服务 - 端口8085)
--   5. conference_collaboration(协同服务 - 端口8089)
--   6. conference_db           (数据/AI/导航公共库 - 端口8086/8088)
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 第一部分: 创建所有数据库
-- =====================================================================
CREATE DATABASE IF NOT EXISTS `conference_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_registration` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_notification` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_seating` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_collaboration` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- #####################################################################
-- #####################################################################
-- 第二部分: conference_auth 数据库 (认证/权限服务)
-- 来源: conference-auth V1__init_database.sql + sql/V1__init_schema.sql
-- #####################################################################
-- #####################################################################

USE `conference_auth`;

-- ========================================
-- 1. 租户表 sys_tenant
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_tenant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户ID',
    `tenant_code` VARCHAR(50) NOT NULL COMMENT '租户编码',
    `tenant_name` VARCHAR(100) NOT NULL COMMENT '租户名称',
    `contact_name` VARCHAR(50) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `domain` VARCHAR(100) COMMENT '租户域名',
    `logo_url` VARCHAR(255) COMMENT 'Logo地址',
    `expire_time` DATETIME COMMENT '到期时间',
    `account_limit` INT DEFAULT -1 COMMENT '账号上限(-1无限制)',
    `tenant_type` VARCHAR(50) COMMENT '客户类型：self-rent/full-pay',
    `valid_start_date` DATE COMMENT '使用期限开始日期',
    `valid_end_date` DATE COMMENT '使用期限结束日期',
    `max_users` INT COMMENT '最大用户数',
    `max_conferences` INT COMMENT '最大会议数',
    `storage_quota` BIGINT COMMENT '存储配额(字节)',
    `description` VARCHAR(500) COMMENT '租户描述/备注',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` BIGINT COMMENT '创建人ID',
    `updated_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    `deleted_at` TIMESTAMP NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- ========================================
-- 2. 用户表 sys_user
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(100) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(255) COMMENT '邮箱',
    `avatar` VARCHAR(255) COMMENT '头像地址',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `user_type` VARCHAR(20) DEFAULT 'staff' COMMENT '用户类型: admin-管理员, staff-协管员, learner-学员',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常, 2-锁定',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `login_fail_count` INT DEFAULT 0 COMMENT '登录失败次数',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `deleted_at` TIMESTAMP NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 3. 角色表 sys_role
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `role_code` VARCHAR(100) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(500) COMMENT '角色描述',
    `data_scope` TINYINT DEFAULT 1 COMMENT '数据权限范围: 1-全部, 2-本部门及下级, 3-本部门, 4-本人',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `deleted_at` TIMESTAMP NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_role_code` (`tenant_id`, `role_code`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ========================================
-- 4. 菜单/权限表 sys_menu
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
    `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `menu_type` CHAR(1) NOT NULL COMMENT '类型: M-目录, C-菜单, F-按钮',
    `path` VARCHAR(200) COMMENT '路由地址',
    `component` VARCHAR(200) COMMENT '组件路径',
    `perms` VARCHAR(100) COMMENT '权限标识',
    `icon` VARCHAR(100) COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `visible` TINYINT DEFAULT 1 COMMENT '是否可见',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

-- ========================================
-- 5. 权限表 sys_permission
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `resource_type` VARCHAR(50) COMMENT '资源类型',
    `resource_id` VARCHAR(255) COMMENT '资源ID',
    `action` VARCHAR(50) COMMENT '操作：create/read/update/delete',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_resource_type` (`resource_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ========================================
-- 6. 用户角色关联表 sys_user_role
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT COMMENT '租户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ========================================
-- 7. 角色菜单关联表 sys_role_menu
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- ========================================
-- 8. 角色权限关联表 sys_role_permission
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`tenant_id`, `role_id`, `permission_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ========================================
-- 9. 审计日志表 audit_log
-- ========================================
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `user_id` BIGINT COMMENT '用户ID',
    `operation_type` VARCHAR(100) COMMENT '操作类型',
    `entity_type` VARCHAR(100) COMMENT '实体类型',
    `entity_id` BIGINT COMMENT '实体ID',
    `operation_detail` TEXT COMMENT '操作详情',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- ========================================
-- conference_auth 初始数据
-- ========================================

-- 默认租户
INSERT INTO `sys_tenant` (`id`, `tenant_code`, `tenant_name`, `status`) VALUES
(1, 'default', '默认租户', 1),
(2, 'demo', '演示租户', 1)
ON DUPLICATE KEY UPDATE `tenant_name` = VALUES(`tenant_name`);

-- 默认角色
INSERT INTO `sys_role` (`id`, `tenant_id`, `role_code`, `role_name`, `data_scope`) VALUES
(1, 1, 'super_admin', '超级管理员', 1),
(2, 1, 'admin', '管理员', 1),
(3, 1, 'staff', '协管员', 3),
(4, 1, 'learner', '学员', 4)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- 默认管理员账号 (密码: admin123)
INSERT INTO `sys_user` (`id`, `tenant_id`, `username`, `password`, `real_name`, `user_type`, `status`) VALUES
(1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin', 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);

-- 管理员角色关联
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 初始化菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(1, 0, '系统管理', 'M', '/system', NULL, NULL, 'setting', 1),
(2, 1, '用户管理', 'C', '/system/user', 'system/user/index', 'system:user:list', 'user', 1),
(3, 1, '角色管理', 'C', '/system/role', 'system/role/index', 'system:role:list', 'peoples', 2),
(4, 1, '菜单管理', 'C', '/system/menu', 'system/menu/index', 'system:menu:list', 'tree-table', 3),
(5, 1, '租户管理', 'C', '/system/tenant', 'system/tenant/index', 'system:tenant:list', 'component', 4),
(10, 0, '会务管理', 'M', '/conference', NULL, NULL, 'documentation', 2),
(11, 10, '会议管理', 'C', '/conference/meeting', 'conference/meeting/index', 'conference:meeting:list', 'date', 1),
(12, 10, '报名管理', 'C', '/conference/registration', 'conference/registration/index', 'conference:registration:list', 'form', 2),
(13, 10, '座位编排', 'C', '/conference/seating', 'conference/seating/index', 'conference:seating:list', 'table', 3),
(14, 10, '通知管理', 'C', '/conference/notification', 'conference/notification/index', 'conference:notification:list', 'message', 4),
(20, 0, '任务协同', 'M', '/task', NULL, NULL, 'guide', 3),
(21, 20, '任务列表', 'C', '/task/list', 'task/list/index', 'task:list', 'list', 1),
(22, 20, '任务派发', 'C', '/task/dispatch', 'task/dispatch/index', 'task:dispatch', 'edit', 2),
(30, 0, '数据统计', 'M', '/data', NULL, NULL, 'chart', 4),
(31, 30, '统计报表', 'C', '/data/report', 'data/report/index', 'data:report:list', 'chart', 1)
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`);

-- 角色菜单关联 (超级管理员拥有所有菜单)
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 10), (1, 11), (1, 12), (1, 13), (1, 14),
(1, 20), (1, 21), (1, 22),
(1, 30), (1, 31);


-- #####################################################################
-- #####################################################################
-- 第三部分: conference_registration 数据库 (报名/会议管理服务)
-- 来源: conference-registration V1.0.0 + sql/V2__registration_schema.sql
--        + conference-meeting V1__init_database.sql + sql/V3__meeting_enhancement.sql
-- 注意: conference-meeting 服务也使用此数据库
-- #####################################################################
-- #####################################################################

USE `conference_registration`;

-- ========================================
-- 1. 会议/培训班表 conf_meeting
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_meeting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `meeting_code` VARCHAR(50) NOT NULL COMMENT '会议编号',
    `meeting_name` VARCHAR(200) NOT NULL COMMENT '会议名称',
    `meeting_type` VARCHAR(50) COMMENT '会议类型',
    `theme` VARCHAR(255) COMMENT '会议主题/类别',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `venue_name` VARCHAR(200) COMMENT '会场名称',
    `venue_address` VARCHAR(500) COMMENT '会场地址',
    `location_latitude` DECIMAL(10,6) COMMENT '会议地点纬度',
    `location_longitude` DECIMAL(10,6) COMMENT '会议地点经度',
    `cover_image_url` VARCHAR(512) COMMENT '会议宣传图URL',
    `max_participants` INT DEFAULT 0 COMMENT '最大参会人数',
    `current_participants` INT DEFAULT 0 COMMENT '当前报名人数',
    `registration_config` JSON COMMENT '报名配置信息(JSON格式)',
    `checkin_config` JSON COMMENT '签到配置信息(JSON格式)',
    `notification_config` JSON COMMENT '通知配置信息(JSON格式)',
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
    KEY `idx_start_time` (`start_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_tenant_status` (`tenant_id`, `status`, `deleted`),
    KEY `idx_tenant_code` (`tenant_id`, `meeting_code`, `deleted`),
    KEY `idx_start_end_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议/培训班表';

-- ========================================
-- 2. 报名表 conf_registration
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
-- 3. 报名信息表 registration (registration服务内部Flyway版本)
-- ========================================
CREATE TABLE IF NOT EXISTS `registration` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(100) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `department` VARCHAR(100) COMMENT '部门',
    `position` VARCHAR(100) COMMENT '职位',
    `id_card` VARCHAR(20) COMMENT '身份证号',
    `id_card_photo_url` TEXT COMMENT '身份证照片URL',
    `diploma_photo_url` TEXT COMMENT '学位证照片URL',
    `registration_data` JSON COMMENT '自定义报名数据',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected/checkedin',
    `registration_time` DATETIME NOT NULL COMMENT '报名时间',
    `auditor_id` BIGINT COMMENT '审核人ID',
    `audit_time` DATETIME COMMENT '审核时间',
    `audit_remark` TEXT COMMENT '审核备注',
    `checkin_status` INT DEFAULT 0 COMMENT '签到状态: 0-未签到 1-已签到',
    `checkin_time` DATETIME COMMENT '签到时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT DEFAULT 0 COMMENT '软删除标记',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_conference_id` (`conference_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_conf_status` (`conference_id`, `status`),
    KEY `idx_tenant_conf` (`tenant_id`, `conference_id`),
    UNIQUE KEY `uk_conf_user` (`conference_id`, `user_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议报名表';

-- ========================================
-- 4. 报名审核日志表 registration_audit
-- ========================================
CREATE TABLE IF NOT EXISTS `registration_audit` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `registration_id` BIGINT NOT NULL COMMENT '报名ID',
    `auditor_id` BIGINT NOT NULL COMMENT '审核人ID',
    `audit_result` VARCHAR(20) NOT NULL COMMENT '审核结果: approved/rejected/pending',
    `remark` TEXT COMMENT '审核备注',
    `audit_time` DATETIME NOT NULL COMMENT '审核时间',
    `audit_method` VARCHAR(20) DEFAULT 'manual' COMMENT '审核方式: manual/auto/api',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT DEFAULT 0 COMMENT '软删除标记',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_registration_id` (`registration_id`),
    KEY `idx_auditor_id` (`auditor_id`),
    KEY `idx_audit_time` (`audit_time`),
    KEY `idx_audit_conf` (`tenant_id`, `registration_id`, `audit_time`),
    CONSTRAINT `fk_audit_registration` FOREIGN KEY (`registration_id`) REFERENCES `registration`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名审核日志表';

-- ========================================
-- 5. 分组表 conf_group
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
-- 6. 分组成员表 conf_group_member
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

-- ========================================
-- 7. 会议工作人员表 conf_meeting_staff
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_meeting_staff` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `staff_id` BIGINT NOT NULL COMMENT '工作人员ID',
    `staff_name` VARCHAR(100) COMMENT '工作人员名称',
    `staff_phone` VARCHAR(20) COMMENT '工作人员电话',
    `role` INT DEFAULT 0 COMMENT '角色(0=普通员工, 1=组长, 2=领队)',
    `permissions` JSON COMMENT '权限配置(JSON格式)',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `deleted` INT DEFAULT 0 COMMENT '是否删除(0=否, 1=是)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_meeting_staff` (`meeting_id`, `staff_id`, `tenant_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议工作人员表';

-- ========================================
-- 8. 会议二维码表 conf_qrcode
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_qrcode` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `qr_type` INT DEFAULT 0 COMMENT '二维码类型(0=签到, 1=报名, 2=会议信息)',
    `qr_code` VARCHAR(255) COMMENT '二维码内容',
    `qr_url` VARCHAR(512) COMMENT '二维码图片URL',
    `qr_text` VARCHAR(100) COMMENT '二维码文字描述',
    `qrcode_token` VARCHAR(255) COMMENT '二维码令牌',
    `valid_start_time` DATETIME COMMENT '有效期开始',
    `valid_end_time` DATETIME COMMENT '有效期结束',
    `is_active` INT DEFAULT 1 COMMENT '是否激活(0=否, 1=是)',
    `used_count` INT DEFAULT 0 COMMENT '使用次数',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `deleted` INT DEFAULT 0 COMMENT '是否删除(0=否, 1=是)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_meeting_type` (`meeting_id`, `qr_type`, `tenant_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_qr_type` (`qr_type`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议二维码表';

-- ========================================
-- 9. 通知模板表 conf_notification_template
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_notification_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `meeting_id` BIGINT COMMENT '会议ID(为NULL时为全局模板)',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` INT DEFAULT 0 COMMENT '模板类型(0=短信, 1=邮件, 2=推送)',
    `template_content` LONGTEXT COMMENT '模板内容',
    `variables` JSON COMMENT '变量列表(JSON格式)',
    `is_default` INT DEFAULT 0 COMMENT '是否为默认模板(0=否, 1=是)',
    `send_time` VARCHAR(20) COMMENT '发送时间(如: registration_start)',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `deleted` INT DEFAULT 0 COMMENT '是否删除(0=否, 1=是)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- ========================================
-- 10. 日程管理表 conf_schedule
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_schedule` (
    `id` BIGINT PRIMARY KEY COMMENT '日程ID（唯一标识）',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID（多租户隔离）',
    `title` VARCHAR(255) NOT NULL COMMENT '日程主题',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `location` VARCHAR(255) COMMENT '详细地点',
    `host` VARCHAR(100) COMMENT '主持人姓名',
    `speaker` VARCHAR(100) COMMENT '主讲人姓名',
    `speaker_intro` LONGTEXT COMMENT '主讲人介绍',
    `notes` LONGTEXT COMMENT '备注说明',
    `status` TINYINT DEFAULT 0 COMMENT '日程状态：0-待发布, 1-已发布, 2-进行中, 3-已结束, 4-已取消',
    `priority` INT DEFAULT 1 COMMENT '优先级（1=正常，2=重要，3=特别重要）',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `created_by` BIGINT COMMENT '创建人ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '最后修改人ID',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志',
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_meeting_status` (`meeting_id`, `status`),
    KEY `idx_tenant_meeting` (`tenant_id`, `meeting_id`),
    UNIQUE KEY `uk_meeting_schedule` (`meeting_id`, `title`, `start_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议日程主表';

-- ========================================
-- 11. 日程设置表 conf_schedule_settings
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_schedule_settings` (
    `id` BIGINT PRIMARY KEY COMMENT '设置ID',
    `schedule_id` BIGINT NOT NULL UNIQUE COMMENT '日程ID',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `need_report` TINYINT DEFAULT 0 COMMENT '是否需要报到',
    `report_method` VARCHAR(50) COMMENT '报到方式：print/qrcode',
    `report_description` VARCHAR(255) COMMENT '报到说明文字',
    `need_checkin` TINYINT DEFAULT 0 COMMENT '是否需要签到',
    `checkin_method` VARCHAR(50) COMMENT '签到方式：print/qrcode',
    `checkin_description` VARCHAR(255) COMMENT '签到说明文字',
    `need_reminder` TINYINT DEFAULT 0 COMMENT '是否需要提醒',
    `reminder_target` VARCHAR(50) COMMENT '提醒对象：staff/all/user',
    `reminder_time` INT DEFAULT 15 COMMENT '提前提醒时间（分钟）',
    `reminder_methods` JSON COMMENT '提醒方式数组',
    `allow_change_location` TINYINT DEFAULT 1 COMMENT '是否允许更改地点',
    `auto_broadcast` TINYINT DEFAULT 0 COMMENT '是否自动播放日程',
    `broadcast_url` VARCHAR(500) COMMENT '直播地址',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_need_checkin` (`need_checkin`),
    KEY `idx_need_reminder` (`need_reminder`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程设置表';

-- ========================================
-- 12. 日程附件表 conf_schedule_attachment
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_schedule_attachment` (
    `id` BIGINT PRIMARY KEY COMMENT '附件ID',
    `schedule_id` BIGINT NOT NULL COMMENT '日程ID',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    `file_hash` VARCHAR(255) COMMENT '文件哈希值',
    `description` VARCHAR(255) COMMENT '文件描述',
    `upload_by` BIGINT COMMENT '上传者ID',
    `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `deleted` TINYINT DEFAULT 0,
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    UNIQUE KEY `uk_schedule_file` (`schedule_id`, `file_hash`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程附件表';

-- ========================================
-- 13. 日程参与人员表 conf_schedule_participant
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_schedule_participant` (
    `id` BIGINT PRIMARY KEY COMMENT '参与人员ID',
    `schedule_id` BIGINT NOT NULL COMMENT '日程ID',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `participant_id` BIGINT NOT NULL COMMENT '参与人员ID',
    `participant_name` VARCHAR(100) COMMENT '参与人员名称',
    `participant_phone` VARCHAR(20) COMMENT '参与人员电话',
    `participant_role` VARCHAR(50) COMMENT '角色：speaker/host/attendee',
    `status` TINYINT DEFAULT 0 COMMENT '参与状态：0-邀请已发送, 1-已确认, 2-已拒绝, 3-已签到, 4-缺席',
    `checkin_time` DATETIME COMMENT '签到时间',
    `checkin_location` VARCHAR(255) COMMENT '签到地点',
    `notes` VARCHAR(255) COMMENT '备注',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_participant_id` (`participant_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`),
    UNIQUE KEY `uk_schedule_participant` (`schedule_id`, `participant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程参与人员表';

-- ========================================
-- 14. 日程提醒记录表 conf_schedule_reminder
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_schedule_reminder` (
    `id` BIGINT PRIMARY KEY COMMENT '提醒记录ID',
    `schedule_id` BIGINT NOT NULL COMMENT '日程ID',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `target_user_id` BIGINT NOT NULL COMMENT '目标用户ID',
    `reminder_type` VARCHAR(50) NOT NULL COMMENT '提醒类型：pre_start/start/end',
    `reminder_channel` VARCHAR(50) COMMENT '提醒渠道：app, sms, wechat',
    `send_time` DATETIME COMMENT '发送时间',
    `status` TINYINT DEFAULT 0 COMMENT '发送状态：0-待发送, 1-已发送, 2-已读, 3-失败',
    `failure_reason` VARCHAR(255) COMMENT '失败原因',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_target_user_id` (`target_user_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_send_time` (`send_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程提醒记录表';

-- ========================================
-- 15. 日程签到记录表 conf_schedule_checkin
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_schedule_checkin` (
    `id` BIGINT PRIMARY KEY COMMENT '签到记录ID',
    `schedule_id` BIGINT NOT NULL COMMENT '日程ID',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `participant_id` BIGINT NOT NULL COMMENT '参与人员ID',
    `checkin_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    `checkin_location` VARCHAR(255) COMMENT '签到地点',
    `checkin_method` VARCHAR(50) COMMENT '签到方式：qrcode/nfc/manual/app',
    `qrcode_id` VARCHAR(255) COMMENT '使用的二维码ID',
    `device_info` VARCHAR(500) COMMENT '签到设备信息（JSON）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_participant_id` (`participant_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_checkin_time` (`checkin_time`),
    UNIQUE KEY `uk_schedule_participant_checkin` (`schedule_id`, `participant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程签到记录表';

-- ========================================
-- 16. 日程操作日志表 conf_schedule_log
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_schedule_log` (
    `id` BIGINT PRIMARY KEY COMMENT '日志ID',
    `schedule_id` BIGINT NOT NULL COMMENT '日程ID',
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) COMMENT '操作人名称',
    `action_type` VARCHAR(50) NOT NULL COMMENT '操作类型：create/update/delete/publish/cancel/view',
    `action_detail` LONGTEXT COMMENT '操作详情（JSON）',
    `old_value` LONGTEXT COMMENT '修改前值（JSON）',
    `new_value` LONGTEXT COMMENT '修改后值（JSON）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    KEY `idx_schedule_id` (`schedule_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_action_type` (`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程操作日志表';

-- ========================================
-- 17. 日程模板表 conf_schedule_template
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_schedule_template` (
    `id` BIGINT PRIMARY KEY COMMENT '模板ID',
    `meeting_id` BIGINT COMMENT '会议ID（NULL=通用模板）',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `template_name` VARCHAR(255) NOT NULL COMMENT '模板名称',
    `template_description` VARCHAR(255) COMMENT '模板描述',
    `duration_minutes` INT COMMENT '日程时长（分钟）',
    `location` VARCHAR(255) COMMENT '默认地点',
    `host` VARCHAR(100) COMMENT '默认主持人',
    `settings` JSON COMMENT '默认设置（JSON）',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `created_by` BIGINT COMMENT '创建人ID',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程模板表';


-- #####################################################################
-- #####################################################################
-- 第四部分: conference_notification 数据库 (通知服务)
-- 来源: conference-notification V1__init_database.sql + sql/V4__notification_schema.sql
-- #####################################################################
-- #####################################################################

USE `conference_notification`;

-- ========================================
-- 1. 通知模板表 notify_template
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
-- 2. 通知记录表 notify_record
-- ========================================
CREATE TABLE IF NOT EXISTS `notify_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT COMMENT '会议ID',
    `template_id` BIGINT COMMENT '模板ID',
    `notify_type` VARCHAR(20) NOT NULL COMMENT '通知类型',
    `title` VARCHAR(200) COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `receiver_type` VARCHAR(20) COMMENT '接收人类型: all/group/individual',
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
-- 3. 通知发送明细表 notify_detail
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
-- 4. 预警规则表 notify_alert_rule
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
-- 5. 通知信息表 notification_info (Flyway迁移版本)
-- ========================================
CREATE TABLE IF NOT EXISTS `notification_info` (
    `id` BIGINT PRIMARY KEY COMMENT '通知ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `title` VARCHAR(255) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `notification_type` VARCHAR(50) COMMENT '通知类型：email/sms/push/notification',
    `recipient_ids` VARCHAR(2000) COMMENT '接收者ID列表',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/sent/failed/retrying',
    `sent_at` TIMESTAMP NULL COMMENT '发送时间',
    `created_by` BIGINT COMMENT '创建人',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知信息表';

-- ========================================
-- conference_notification 初始数据 - 通知模板
-- ========================================
INSERT INTO `notify_template` (`tenant_id`, `template_code`, `template_name`, `template_type`, `title`, `content`, `variables`) VALUES
(1, 'REG_SUCCESS', '报名成功通知', 'sms', NULL, '尊敬的${name}，您已成功报名${meetingName}，会议时间：${startTime}，地点：${venue}。请准时参加。', '["name","meetingName","startTime","venue"]'),
(1, 'REG_REVIEW', '报名审核通知', 'sms', NULL, '尊敬的${name}，您报名的${meetingName}审核${result}。${remark}', '["name","meetingName","result","remark"]'),
(1, 'MEETING_REMINDER', '会议提醒', 'push', '会议即将开始', '${meetingName}将于${startTime}在${venue}举行，请提前做好准备。', '["meetingName","startTime","venue"]'),
(1, 'SIGN_IN_REMINDER', '签到提醒', 'push', '请及时签到', '${meetingName}签到已开始，请尽快完成签到。', '["meetingName"]'),
(1, 'SEAT_ASSIGNED', '座位分配通知', 'push', '座位已分配', '尊敬的${name}，您在${meetingName}的座位为：${seatLabel}。', '["name","meetingName","seatLabel"]')
ON DUPLICATE KEY UPDATE `template_name` = VALUES(`template_name`);


-- #####################################################################
-- #####################################################################
-- 第五部分: conference_seating 数据库 (排座服务)
-- 来源: 2_create_tables.sql + conference-seating V1__init + sql/V3__seating_schema.sql
-- #####################################################################
-- #####################################################################

USE `conference_seating`;

-- ========================================
-- 1. 会场配置表 conf_seating_venue
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_venue` (
    `id` BIGINT PRIMARY KEY COMMENT '会场ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `venue_name` VARCHAR(100) NOT NULL COMMENT '会场名称',
    `venue_type` VARCHAR(50) COMMENT '会场类型：normal/ushape/round/theater',
    `total_rows` INT NOT NULL DEFAULT 10 COMMENT '总行数',
    `total_columns` INT NOT NULL DEFAULT 20 COMMENT '总列数',
    `capacity` INT COMMENT '会场容纳人数',
    `layout_data` JSON COMMENT '布局配置JSON(包含过道位置等)',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive',
    `created_by` BIGINT COMMENT '创建人ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_by` BIGINT COMMENT '更新人ID',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_conf_venue` (`conference_id`, `venue_name`),
    KEY `idx_tenant_conference` (`tenant_id`, `conference_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会场配置表';

-- ========================================
-- 2. 座位表 conf_seating_seat
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_seat` (
    `id` BIGINT PRIMARY KEY COMMENT '座位ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `venue_id` BIGINT NOT NULL COMMENT '会场ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `seat_number` VARCHAR(50) NOT NULL COMMENT '座位编号(如A01, B15等)',
    `row_num` INT NOT NULL COMMENT '行号',
    `column_num` INT NOT NULL COMMENT '列号',
    `seat_type` VARCHAR(20) DEFAULT 'normal' COMMENT '座位类型：normal/vip/reserved/aisle',
    `status` VARCHAR(20) DEFAULT 'available' COMMENT '状态：available/assigned/reserved/aisle',
    `assigned_user_id` BIGINT COMMENT '分配人员ID',
    `assigned_user_name` VARCHAR(100) COMMENT '分配人员名称',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_seat_number` (`venue_id`, `seat_number`),
    KEY `idx_conference_venue` (`conference_id`, `venue_id`),
    KEY `idx_tenant_conference` (`tenant_id`, `conference_id`),
    KEY `idx_assigned_user` (`assigned_user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_seat_type` (`seat_type`),
    KEY `idx_venue_status` (`venue_id`, `status`),
    KEY `idx_seat_type_status` (`seat_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- ========================================
-- 3. 参会人员表 conf_seating_attendee
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_attendee` (
    `id` BIGINT PRIMARY KEY COMMENT '人员ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `user_id` BIGINT COMMENT '用户系统ID',
    `name` VARCHAR(100) NOT NULL COMMENT '姓名',
    `department` VARCHAR(100) COMMENT '部门',
    `position` VARCHAR(100) COMMENT '职位',
    `phone` VARCHAR(20) COMMENT '电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `is_vip` TINYINT DEFAULT 0 COMMENT '是否VIP',
    `is_reserved` TINYINT DEFAULT 0 COMMENT '是否保留座位',
    `assigned_seat_id` BIGINT COMMENT '分配座位ID',
    `assigned_at` TIMESTAMP NULL COMMENT '分配时间',
    `confirmed` TINYINT DEFAULT 0 COMMENT '是否确认',
    `attendance_status` VARCHAR(20) DEFAULT 'pending' COMMENT '参加状态：pending/confirmed/absent',
    `extra_data` JSON COMMENT '扩展数据',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_conference_attendee` (`conference_id`, `id`),
    KEY `idx_tenant_conference` (`tenant_id`, `conference_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_assigned_seat` (`assigned_seat_id`),
    KEY `idx_is_vip` (`is_vip`),
    KEY `idx_department` (`department`),
    KEY `idx_department_position` (`department`, `position`),
    KEY `idx_is_vip_reserved` (`is_vip`, `is_reserved`),
    CONSTRAINT `fk_attendee_seat` FOREIGN KEY (`assigned_seat_id`) REFERENCES `conf_seating_seat`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参会人员表';

-- ========================================
-- 4. 座位分配记录表 conf_seating_assignment
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_assignment` (
    `id` BIGINT PRIMARY KEY COMMENT '分配记录ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `seat_id` BIGINT NOT NULL COMMENT '座位ID',
    `attendee_id` BIGINT NOT NULL COMMENT '参会人员ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `schedule_id` BIGINT COMMENT '日程ID（如有多日程）',
    `assign_type` VARCHAR(20) COMMENT '分配类型：auto/manual/swap',
    `algorithm_name` VARCHAR(100) COMMENT '使用的算法名称',
    `assign_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    `assigned_by` BIGINT COMMENT '分配人ID',
    `status` VARCHAR(20) DEFAULT 'assigned' COMMENT '状态：assigned/confirmed/cancelled',
    `confirm_time` TIMESTAMP NULL COMMENT '确认时间',
    `cancel_reason` VARCHAR(500) COMMENT '取消原因',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_conference_assignment` (`conference_id`, `id`),
    KEY `idx_tenant_conference` (`tenant_id`, `conference_id`),
    KEY `idx_seat_attendee` (`seat_id`, `attendee_id`),
    KEY `idx_assign_time` (`assign_time`),
    KEY `idx_status` (`status`),
    KEY `idx_attendee_schedule` (`attendee_id`, `schedule_id`),
    KEY `idx_assign_type` (`assign_type`),
    CONSTRAINT `fk_assignment_seat` FOREIGN KEY (`seat_id`) REFERENCES `conf_seating_seat`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_assignment_attendee` FOREIGN KEY (`attendee_id`) REFERENCES `conf_seating_attendee`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位分配记录表';

-- ========================================
-- 5. 日程表 conf_seating_schedule
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_schedule` (
    `id` BIGINT PRIMARY KEY COMMENT '日程ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `schedule_name` VARCHAR(100) NOT NULL COMMENT '日程名称',
    `schedule_date` DATE COMMENT '日期',
    `start_time` TIME COMMENT '开始时间',
    `end_time` TIME COMMENT '结束时间',
    `description` VARCHAR(500) COMMENT '描述',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认日程',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_conference_schedule` (`conference_id`, `id`),
    KEY `idx_tenant_conference` (`tenant_id`, `conference_id`),
    KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程表';

-- ========================================
-- 6. 车辆安排表 conf_seating_transport
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_transport` (
    `id` BIGINT PRIMARY KEY COMMENT '车辆ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `vehicle_name` VARCHAR(100) NOT NULL COMMENT '车辆名称',
    `vehicle_type` VARCHAR(50) NOT NULL COMMENT '车辆类型：小车/中巴/大巴',
    `plate_number` VARCHAR(50) COMMENT '车牌号',
    `departure` VARCHAR(100) COMMENT '出发地',
    `destination` VARCHAR(100) COMMENT '目的地',
    `departure_time` DATETIME COMMENT '出发时间',
    `capacity` INT DEFAULT 45 COMMENT '容纳人数',
    `assigned_count` INT DEFAULT 0 COMMENT '已分配人数',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_conference` (`conference_id`),
    KEY `idx_tenant_conference` (`tenant_id`, `conference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆安排表';

-- ========================================
-- 7. 住宿安排表 conf_seating_accommodation
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_accommodation` (
    `id` BIGINT PRIMARY KEY COMMENT '住宿ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `hotel_name` VARCHAR(100) NOT NULL COMMENT '酒店名称',
    `room_number` VARCHAR(50) NOT NULL COMMENT '房间号',
    `room_type` VARCHAR(50) COMMENT '房间类型：标准间/豪华间/套房',
    `capacity` INT DEFAULT 2 COMMENT '房间容纳人数',
    `assigned_count` INT DEFAULT 0 COMMENT '已分配人数',
    `check_in_time` DATETIME COMMENT '入住时间',
    `check_out_time` DATETIME COMMENT '退房时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_conference` (`conference_id`),
    KEY `idx_tenant_conference` (`tenant_id`, `conference_id`),
    UNIQUE KEY `uk_room` (`hotel_name`, `room_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='住宿安排表';

-- ========================================
-- 8. 用餐安排表 conf_seating_dining
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_dining` (
    `id` BIGINT PRIMARY KEY COMMENT '用餐ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `dining_name` VARCHAR(100) NOT NULL COMMENT '用餐安排名称',
    `dining_type` VARCHAR(50) COMMENT '用餐类型：早餐/午餐/晚餐/茶歇',
    `dining_date` DATE COMMENT '用餐日期',
    `dining_time` TIME COMMENT '用餐时间',
    `location` VARCHAR(100) COMMENT '用餐地点',
    `capacity` INT COMMENT '容纳人数',
    `assigned_count` INT DEFAULT 0 COMMENT '已分配人数',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_conference` (`conference_id`),
    KEY `idx_tenant_conference` (`tenant_id`, `conference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用餐安排表';

-- ========================================
-- 9. 座位分配历史表 conf_seating_assignment_history
-- ========================================
CREATE TABLE IF NOT EXISTS `conf_seating_assignment_history` (
    `id` BIGINT PRIMARY KEY COMMENT '历史记录ID',
    `assignment_id` BIGINT NOT NULL COMMENT '分配记录ID',
    `conference_id` BIGINT NOT NULL COMMENT '会议ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `seat_id` BIGINT COMMENT '座位ID',
    `attendee_id` BIGINT COMMENT '参会人员ID',
    `old_seat_id` BIGINT COMMENT '旧座位ID',
    `operation` VARCHAR(50) COMMENT '操作类型：assign/cancel/swap',
    `operator_id` BIGINT COMMENT '操作人ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_conference` (`conference_id`),
    KEY `idx_assignment` (`assignment_id`),
    KEY `idx_attendee` (`attendee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位分配历史表';

-- ========================================
-- 10. 会场布局表 seat_venue_layout (V3扩展)
-- ========================================
CREATE TABLE IF NOT EXISTS `seat_venue_layout` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '布局ID',
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
    `layout_name` VARCHAR(100) NOT NULL COMMENT '布局名称',
    `layout_type` VARCHAR(50) NOT NULL COMMENT '布局类型: classroom/theater/roundtable/u_shape',
    `rows` INT NOT NULL COMMENT '总行数',
    `cols` INT NOT NULL COMMENT '总列数',
    `total_seats` INT DEFAULT 0 COMMENT '总座位数',
    `assigned_seats` INT DEFAULT 0 COMMENT '已分配座位数',
    `layout_data` JSON COMMENT '布局配置(JSON)',
    `aisle_config` JSON COMMENT '过道配置(JSON)',
    `stage_position` VARCHAR(20) DEFAULT 'front' COMMENT '舞台位置: front/back/left/right',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认布局',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_meeting_id` (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会场布局表';

-- ========================================
-- 11. 座位信息表 seat_info (V3扩展)
-- ========================================
CREATE TABLE IF NOT EXISTS `seat_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '座位ID',
    `tenant_id` BIGINT NOT NULL,
    `layout_id` BIGINT NOT NULL COMMENT '布局ID',
    `row_num` INT NOT NULL COMMENT '行号',
    `col_num` INT NOT NULL COMMENT '列号',
    `seat_label` VARCHAR(20) COMMENT '座位标签(如A1, B2)',
    `seat_type` VARCHAR(20) DEFAULT 'normal' COMMENT '座位类型: normal/vip/disabled/reserved',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表(布局关联)';

-- ========================================
-- 12. 排座规则表 seat_rule
-- ========================================
CREATE TABLE IF NOT EXISTS `seat_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `layout_id` BIGINT NOT NULL COMMENT '布局ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型: auto/manual/group/random',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `rule_config` JSON COMMENT '规则配置',
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_layout_id` (`layout_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排座规则表';

-- ========================================
-- 13. 排座历史表 seat_assignment_history
-- ========================================
CREATE TABLE IF NOT EXISTS `seat_assignment_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `layout_id` BIGINT NOT NULL,
    `seat_id` BIGINT NOT NULL,
    `registration_id` BIGINT,
    `action` VARCHAR(20) NOT NULL COMMENT '操作: assign/unassign/swap',
    `operator_id` BIGINT COMMENT '操作人',
    `reason` VARCHAR(200) COMMENT '原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_layout_id` (`layout_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排座历史表';

-- 添加额外唯一约束
ALTER TABLE `conf_seating_venue` ADD UNIQUE KEY IF NOT EXISTS `uk_tenant_conf` (`tenant_id`, `conference_id`);


-- #####################################################################
-- #####################################################################
-- 第六部分: conference_collaboration 数据库 (协同服务)
-- 来源: sql/V5__collaboration_schema.sql
-- #####################################################################
-- #####################################################################

USE `conference_collaboration`;

-- ========================================
-- 1. 任务表 task_info
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
-- 2. 任务执行人表 task_assignee
-- ========================================
CREATE TABLE IF NOT EXISTS `task_assignee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) DEFAULT 'executor' COMMENT '角色: executor/reviewer/observer',
    `is_main` TINYINT DEFAULT 0 COMMENT '是否主负责人',
    `status` TINYINT DEFAULT 0 COMMENT '个人状态: 0-待接受, 1-已接受, 2-已拒绝',
    `accept_time` DATETIME COMMENT '接受时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_user` (`task_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行人表';

-- ========================================
-- 3. 任务日志表 task_log
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
-- 4. 任务评论表 task_comment
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
-- 5. 物资管理表 resource_item
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
-- 6. 物资领用记录表 resource_usage
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
-- 7. 消息表 message (WebSocket消息持久化)
-- ========================================
CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `meeting_id` BIGINT COMMENT '会议ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送人ID',
    `receiver_id` BIGINT COMMENT '接收人ID(空表示广播)',
    `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型: text/image/file/system',
    `content` TEXT NOT NULL COMMENT '内容',
    `read_status` TINYINT DEFAULT 0 COMMENT '已读状态',
    `read_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_meeting_id` (`meeting_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';


-- #####################################################################
-- #####################################################################
-- 第七部分: conference_db 数据库 (AI/数据统计/导航 公共数据库)
-- 来源: conference-ai V2__create_ai_tables.sql
-- 服务: conference-ai(8086), conference-data(8088), conference-navigation
-- #####################################################################
-- #####################################################################

USE `conference_db`;

-- ========================================
-- 1. AI对话表 ai_conversation
-- ========================================
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` BIGINT PRIMARY KEY COMMENT '雪花ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `user_id` BIGINT COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户名',
    `conference_id` BIGINT COMMENT '关联会议ID',
    `title` VARCHAR(255) NOT NULL DEFAULT '新对话' COMMENT '对话标题',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'active/archived/deleted',
    `message_count` INT NOT NULL DEFAULT 0 COMMENT '消息条数',
    `last_message` VARCHAR(500) COMMENT '最后一条消息摘要',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_conference_id` (`conference_id`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话';

-- ========================================
-- 2. AI消息表 ai_message
-- ========================================
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` BIGINT PRIMARY KEY COMMENT '雪花ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `conversation_id` BIGINT NOT NULL COMMENT '所属对话ID',
    `role` VARCHAR(20) NOT NULL COMMENT 'user/ai/system',
    `content` TEXT NOT NULL COMMENT '消息内容(支持Markdown)',
    `model` VARCHAR(50) COMMENT 'AI模型名称',
    `tokens_used` INT COMMENT '消耗token数',
    `response_time` INT COMMENT '响应耗时(ms)',
    `rating` VARCHAR(20) COMMENT 'good/bad/null 用户评分',
    `status` VARCHAR(20) NOT NULL DEFAULT 'sent' COMMENT 'sent/error/regenerated',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息';

-- ========================================
-- 3. AI知识库表 ai_knowledge
-- ========================================
CREATE TABLE IF NOT EXISTS `ai_knowledge` (
    `id` BIGINT PRIMARY KEY COMMENT '雪花ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `conference_id` BIGINT COMMENT '关联会议ID',
    `title` VARCHAR(255) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) COMMENT '摘要',
    `content` TEXT COMMENT '详细内容',
    `category` VARCHAR(50) NOT NULL COMMENT '分类: 会议信息/出行指南/会务服务/报名咨询',
    `icon` VARCHAR(100) DEFAULT 'fas fa-file-alt' COMMENT '图标class',
    `tags` VARCHAR(500) COMMENT '标签(JSON数组)',
    `views` INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'active/disabled',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT NOT NULL DEFAULT 0,
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_conference_id` (`conference_id`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库';

-- ========================================
-- 4. AI常见问题(FAQ)表 ai_faq
-- ========================================
CREATE TABLE IF NOT EXISTS `ai_faq` (
    `id` BIGINT PRIMARY KEY COMMENT '雪花ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `conference_id` BIGINT COMMENT '关联会议ID',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '标准答案',
    `category` VARCHAR(50) NOT NULL COMMENT '分类',
    `keywords` VARCHAR(500) COMMENT '关键词(逗号分隔)',
    `views` INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    `rating` DECIMAL(3,1) DEFAULT 0 COMMENT '平均评分',
    `rating_count` INT NOT NULL DEFAULT 0 COMMENT '评分次数',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` INT NOT NULL DEFAULT 0,
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_conference_id` (`conference_id`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI常见问题';

-- ========================================
-- 5. AI反馈表 ai_feedback
-- ========================================
CREATE TABLE IF NOT EXISTS `ai_feedback` (
    `id` BIGINT PRIMARY KEY COMMENT '雪花ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `conversation_id` BIGINT COMMENT '对话ID',
    `message_id` BIGINT COMMENT '消息ID',
    `user_id` BIGINT COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户名',
    `question` VARCHAR(500) COMMENT '原始问题',
    `answer` TEXT COMMENT 'AI回答',
    `feedback` VARCHAR(20) NOT NULL COMMENT 'good/bad/neutral',
    `comment` VARCHAR(500) COMMENT '反馈详情',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_feedback` (`feedback`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI反馈';

-- ========================================
-- 6. AI上下文记忆表 ai_context
-- ========================================
CREATE TABLE IF NOT EXISTS `ai_context` (
    `id` BIGINT PRIMARY KEY COMMENT '雪花ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户名',
    `conference_id` BIGINT COMMENT '关联会议ID',
    `context_data` JSON COMMENT '上下文数据',
    `turns` INT NOT NULL DEFAULT 0 COMMENT '对话轮次',
    `last_message` VARCHAR(500) COMMENT '最后消息',
    `duration` VARCHAR(50) COMMENT '持续时长',
    `last_update` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI上下文记忆';

-- ========================================
-- 7. AI使用统计表 ai_usage_stats
-- ========================================
CREATE TABLE IF NOT EXISTS `ai_usage_stats` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `total_queries` INT NOT NULL DEFAULT 0 COMMENT '总查询数',
    `total_tokens` INT NOT NULL DEFAULT 0 COMMENT '总token消耗',
    `avg_response_ms` INT NOT NULL DEFAULT 0 COMMENT '平均响应时间(ms)',
    `positive_count` INT NOT NULL DEFAULT 0 COMMENT '正面反馈数',
    `negative_count` INT NOT NULL DEFAULT 0 COMMENT '负面反馈数',
    `neutral_count` INT NOT NULL DEFAULT 0 COMMENT '中性反馈数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_date` (`tenant_id`, `stat_date`),
    KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用统计';

-- ========================================
-- 8. AI功能配置表 ai_feature_config
-- ========================================
CREATE TABLE IF NOT EXISTS `ai_feature_config` (
    `id` BIGINT PRIMARY KEY COMMENT '雪花ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `feature_id` VARCHAR(50) NOT NULL COMMENT '功能标识: qa/translate/summary/recommend',
    `feature_name` VARCHAR(100) NOT NULL COMMENT '功能名称',
    `description` VARCHAR(255) COMMENT '功能描述',
    `icon` VARCHAR(100) COMMENT '图标class',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0-关闭 1-开启',
    `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `config` JSON COMMENT '功能配置(JSON)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_tenant_feature` (`tenant_id`, `feature_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI功能配置';


-- =====================================================================
-- 第八部分: 恢复外键检查
-- =====================================================================
SET FOREIGN_KEY_CHECKS = 1;


-- =====================================================================
-- 完成!
-- =====================================================================
-- 数据库总计: 6 个
-- 表结构总计: 约 55 张表
-- 
-- 数据库 → 服务映射:
--   conference_auth          → conference-auth (端口 8081)
--   conference_registration  → conference-registration (端口 8082)
--                             + conference-meeting (端口 8084)
--   conference_notification  → conference-notification (端口 8083)
--   conference_seating       → conference-seating (端口 8085)
--   conference_collaboration → conference-collaboration (端口 8089)
--   conference_db            → conference-ai (端口 8086)
--                             + conference-data (端口 8088)
--                             + conference-navigation
-- =====================================================================

SELECT '✅ 全部数据库和表已创建完成!' AS '状态';
