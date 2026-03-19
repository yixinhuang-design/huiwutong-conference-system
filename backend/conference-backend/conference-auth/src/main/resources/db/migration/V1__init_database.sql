-- ========================================
-- 智能会务系统数据库初始化脚本
-- 支持多租户数据隔离
-- ========================================

-- ========================================
-- 1. 租户表 sys_tenant
-- ========================================
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT PRIMARY KEY COMMENT '租户ID',
    tenant_code VARCHAR(100) NOT NULL UNIQUE COMMENT '租户编码',
    tenant_name VARCHAR(255) NOT NULL COMMENT '租户名称',
    contact_name VARCHAR(100) COMMENT '联系人名称',
    contact_phone VARCHAR(20) COMMENT '联系人电话/登录号码',
    contact_email VARCHAR(100) COMMENT '联系人邮箱',
    tenant_type VARCHAR(50) COMMENT '客户类型：self-rent/full-pay',
    valid_start_date DATE COMMENT '使用期限开始日期',
    valid_end_date DATE COMMENT '使用期限结束日期',
    max_users INT COMMENT '最大用户数',
    max_conferences INT COMMENT '最大会议数',
    storage_quota BIGINT COMMENT '存储配额(字节)',
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
-- 3. 角色表 sys_role
-- ========================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY COMMENT '角色ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(100) NOT NULL COMMENT '角色编码',
    description VARCHAR(500) COMMENT '角色描述',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    UNIQUE KEY uk_tenant_code (tenant_id, role_code),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ========================================
-- 4. 权限表 sys_permission
-- ========================================
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY COMMENT '权限ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id VARCHAR(255) COMMENT '资源ID',
    action VARCHAR(50) COMMENT '操作：create/read/update/delete',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_resource_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ========================================
-- 5. 用户角色关联表 sys_user_role
-- ========================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY COMMENT '关联ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (tenant_id, user_id, role_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ========================================
-- 6. 角色权限关联表 sys_role_permission
-- ========================================
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY COMMENT '关联ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (tenant_id, role_id, permission_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ========================================
-- 7. 注册信息表 registration_info
-- ========================================
CREATE TABLE IF NOT EXISTS registration_info (
    id BIGINT PRIMARY KEY COMMENT '注册ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    attendee_name VARCHAR(100) NOT NULL COMMENT '参会者名称',
    attendee_email VARCHAR(255) COMMENT '参会者邮箱',
    attendee_phone VARCHAR(20) COMMENT '参会者电话',
    position VARCHAR(100) COMMENT '职位',
    company VARCHAR(255) COMMENT '公司',
    department VARCHAR(100) COMMENT '部门',
    registration_time TIMESTAMP NOT NULL COMMENT '报名时间',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/checked_in',
    remark VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注册信息表';

-- ========================================
-- 8. 座位表 seating_info
-- ========================================
CREATE TABLE IF NOT EXISTS seating_info (
    id BIGINT PRIMARY KEY COMMENT '座位ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    seat_number VARCHAR(50) NOT NULL COMMENT '座位号',
    row_number INT COMMENT '行号',
    column_number INT COMMENT '列号',
    zone_id BIGINT COMMENT '区域ID',
    status VARCHAR(20) DEFAULT 'available' COMMENT '状态：available/occupied/reserved/blocked',
    allocated_to BIGINT COMMENT '分配给的用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_seat (tenant_id, seat_number),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位信息表';

-- ========================================
-- 9. 通知表 notification_info
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
-- 10. 分组表 group_info
-- ========================================
CREATE TABLE IF NOT EXISTS group_info (
    id BIGINT PRIMARY KEY COMMENT '分组ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    group_name VARCHAR(100) NOT NULL COMMENT '分组名称',
    description VARCHAR(500) COMMENT '分组描述',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分组信息表';

-- ========================================
-- 11. 分组成员表 group_member
-- ========================================
CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT PRIMARY KEY COMMENT '成员关联ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    group_id BIGINT NOT NULL COMMENT '分组ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    member_role VARCHAR(50) COMMENT '成员角色：admin/member/viewer',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_group_user (tenant_id, group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分组成员表';

-- ========================================
-- 12. 审计日志表 audit_log
-- ========================================
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY COMMENT '日志ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    user_id BIGINT COMMENT '用户ID',
    operation_type VARCHAR(100) COMMENT '操作类型',
    entity_type VARCHAR(100) COMMENT '实体类型',
    entity_id BIGINT COMMENT '实体ID',
    operation_detail TEXT COMMENT '操作详情',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- ========================================
-- 初始测试数据
-- ========================================

-- 插入租户数据
INSERT INTO sys_tenant (id, tenant_code, tenant_name, description, status) VALUES
(1, 'tenant_001', '演示租户1', '用于演示和测试的租户', 'active'),
(2, 'tenant_002', '演示租户2', '用于演示和测试的租户', 'active');

-- 插入用户数据 (密码都是123456的bcrypt加密)
INSERT INTO sys_user (id, tenant_id, username, password, email, real_name, status) VALUES
(1, 1, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@tenant1.com', '管理员', 'active'),
(2, 1, 'user1', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'user1@tenant1.com', '用户1', 'active'),
(3, 2, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@tenant2.com', '管理员', 'active');

-- 插入角色数据
INSERT INTO sys_role (id, tenant_id, role_name, role_code, description, status) VALUES
(1, 1, '管理员', 'admin', '系统管理员', 'active'),
(2, 1, '普通用户', 'user', '普通用户', 'active'),
(3, 2, '管理员', 'admin', '系统管理员', 'active');

-- 插入用户角色关联
INSERT INTO sys_user_role (id, tenant_id, user_id, role_id) VALUES
(1, 1, 1, 1),
(2, 1, 2, 2),
(3, 2, 3, 3);

COMMIT;
