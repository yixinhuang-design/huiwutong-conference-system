-- ========================================
-- 会务系统数据库初始化脚本 V1.0
-- 创建日期: 2026-01-24
-- 说明: 核心表结构初始化
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `conference_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_registration` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_notification` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_seating` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `conference_collaboration` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `conference_auth`;

-- ========================================
-- 租户表 (多租户核心)
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
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- ========================================
-- 用户表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
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
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 角色表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `data_scope` TINYINT DEFAULT 1 COMMENT '数据权限范围: 1-全部, 2-本部门及下级, 3-本部门, 4-本人',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_role_code` (`tenant_id`, `role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ========================================
-- 菜单/权限表
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
-- 用户角色关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ========================================
-- 角色菜单关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- ========================================
-- 初始化数据
-- ========================================

-- 默认租户
INSERT INTO `sys_tenant` (`id`, `tenant_code`, `tenant_name`, `status`) VALUES
(1, 'default', '默认租户', 1),
(2, 'demo', '演示租户', 1);

-- 默认角色
INSERT INTO `sys_role` (`id`, `tenant_id`, `role_code`, `role_name`, `data_scope`) VALUES
(1, 1, 'super_admin', '超级管理员', 1),
(2, 1, 'admin', '管理员', 1),
(3, 1, 'staff', '协管员', 3),
(4, 1, 'learner', '学员', 4);

-- 默认管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`id`, `tenant_id`, `username`, `password`, `real_name`, `user_type`, `status`) VALUES
(1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin', 1);

-- 管理员角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

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
(31, 30, '统计报表', 'C', '/data/report', 'data/report/index', 'data:report:list', 'chart', 1);

-- 角色菜单关联 (超级管理员拥有所有菜单)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 10), (1, 11), (1, 12), (1, 13), (1, 14),
(1, 20), (1, 21), (1, 22),
(1, 30), (1, 31);
