-- ========================================
-- 智能会务系统数据库初始化脚本（通知服务）
-- 支持多租户数据隔离
-- ========================================

-- ========================================
-- 1. 租户表 sys_tenant
-- ========================================
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT PRIMARY KEY COMMENT '租户ID',
    tenant_code VARCHAR(100) NOT NULL UNIQUE COMMENT '租户编码',
    tenant_name VARCHAR(255) NOT NULL COMMENT '租户名称',
    description VARCHAR(500) COMMENT '租户描述',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive',
    created_by BIGINT COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    INDEX idx_tenant_code (tenant_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- ========================================
-- 2. 用户表 sys_user
-- ========================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(255) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    real_name VARCHAR(100) COMMENT '真实姓名',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive/locked',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    UNIQUE KEY uk_tenant_username (tenant_id, username),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 3. 通知表 notification_info
-- ========================================
CREATE TABLE IF NOT EXISTS notification_info (
    id BIGINT PRIMARY KEY COMMENT '通知ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    title VARCHAR(255) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    notification_type VARCHAR(50) COMMENT '通知类型：email/sms/push/notification',
    recipient_ids VARCHAR(2000) COMMENT '接收者ID列表',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/sent/failed/retrying',
    sent_at TIMESTAMP NULL COMMENT '发送时间',
    created_by BIGINT COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知信息表';

-- ========================================
-- 初始数据
-- ========================================
INSERT INTO sys_tenant (id, tenant_code, tenant_name, status) VALUES
(1, 'tenant_001', '演示租户1', 'active'),
(2, 'tenant_002', '演示租户2', 'active');

INSERT INTO sys_user (id, tenant_id, username, password, email, real_name, status) VALUES
(1, 1, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@tenant1.com', '管理员', 'active');
