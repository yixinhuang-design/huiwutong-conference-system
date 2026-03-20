-- ========================================
-- 智能会议系统 - 租户管理初始化脚本
-- ========================================
-- 目的: 初始化租户、用户、角色等管理数据
-- 执行环境: MySQL 9.6+
-- 执行用户: root (或具有创建表权限的用户)
-- ========================================

USE `conference_auth`;

-- ========================================
-- 1. 验证核心表是否存在 (由 Flyway 创建)
-- ========================================

-- 检查 sys_tenant 表
SELECT 'Checking sys_tenant table...' AS status;
SHOW TABLES LIKE 'sys_tenant';

-- ========================================
-- 2. 清除现有测试数据 (可选)
-- ========================================

-- 先删除关联数据，再删除租户
-- DELETE FROM sys_user WHERE tenant_id IN (1, 2, 3, 4, 5);
-- DELETE FROM sys_tenant WHERE id IN (1, 2, 3, 4, 5);

-- ========================================
-- 3. 初始化租户数据 (多租户场景)
-- ========================================

-- 主租户 - 用于演示和测试
INSERT INTO sys_tenant 
(tenant_code, tenant_name, description, status, created_by, created_at, updated_at) 
VALUES 
('tenant_001', '智能会议系统-主租户', '系统主要演示租户', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 租户 2 - 企业版本
INSERT INTO sys_tenant 
(tenant_code, tenant_name, description, status, created_by, created_at, updated_at) 
VALUES 
('tenant_002', '企业会议版本', '企业级会议系统租户', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 租户 3 - 教育版本
INSERT INTO sys_tenant 
(tenant_code, tenant_name, description, status, created_by, created_at, updated_at) 
VALUES 
('tenant_003', '教育培训机构', '用于教育行业的租户', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 租户 4 - 活动版本
INSERT INTO sys_tenant 
(tenant_code, tenant_name, description, status, created_by, created_at, updated_at) 
VALUES 
('tenant_004', '大型活动会议', '用于大型活动和会议的租户', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 租户 5 - 测试版本 (禁用)
INSERT INTO sys_tenant 
(tenant_code, tenant_name, description, status, created_by, created_at, updated_at) 
VALUES 
('tenant_005', '测试租户', '系统测试用租户', 'inactive', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ========================================
-- 4. 查询已创建的租户
-- ========================================

SELECT 'Tenants created:' AS info;
SELECT id, tenant_code, tenant_name, status, created_at 
FROM sys_tenant 
ORDER BY id;

-- ========================================
-- 5. 初始化管理员用户
-- ========================================

-- 获取主租户 ID (假设为 1)
SET @tenant_id = (SELECT id FROM sys_tenant WHERE tenant_code = 'tenant_001' LIMIT 1);

-- 主租户管理员
INSERT INTO sys_user 
(tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at) 
VALUES 
(@tenant_id, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 
 'admin@example.com', '13800138000', '系统管理员', 'admin', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 协管员用户
INSERT INTO sys_user 
(tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at) 
VALUES 
(@tenant_id, 'staff', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 
 'staff@example.com', '13800138001', '协管员', 'staff', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 学员用户
INSERT INTO sys_user 
(tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at) 
VALUES 
(@tenant_id, 'learner', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 
 'learner@example.com', '13800138002', '学员', 'learner', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ========================================
-- 6. 初始化角色数据
-- ========================================

-- 管理员角色
INSERT INTO sys_role 
(tenant_id, role_code, role_name, description, data_scope, status, created_by, created_at, updated_at) 
VALUES 
(@tenant_id, 'admin', '系统管理员', '拥有所有权限的管理员', 1, 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 协管员角色
INSERT INTO sys_role 
(tenant_id, role_code, role_name, description, data_scope, status, created_by, created_at, updated_at) 
VALUES 
(@tenant_id, 'staff', '协管员', '协助管理的角色', 2, 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 普通用户角色
INSERT INTO sys_role 
(tenant_id, role_code, role_name, description, data_scope, status, created_by, created_at, updated_at) 
VALUES 
(@tenant_id, 'user', '普通用户', '普通用户角色', 4, 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ========================================
-- 7. 查询已创建的用户和角色
-- ========================================

SELECT 'Users created:' AS info;
SELECT id, tenant_id, username, real_name, user_type, status, created_at 
FROM sys_user 
WHERE tenant_id = @tenant_id
ORDER BY id;

SELECT 'Roles created:' AS info;
SELECT id, tenant_id, role_code, role_name, status, created_at 
FROM sys_role 
WHERE tenant_id = @tenant_id
ORDER BY id;

-- ========================================
-- 8. 初始化更多租户的用户 (可选)
-- ========================================

-- 租户 2 的管理员
SET @tenant_id_2 = (SELECT id FROM sys_tenant WHERE tenant_code = 'tenant_002' LIMIT 1);
INSERT INTO sys_user 
(tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at) 
VALUES 
(@tenant_id_2, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 
 'admin@tenant2.com', '13800138101', '企业管理员', 'admin', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 租户 3 的管理员
SET @tenant_id_3 = (SELECT id FROM sys_tenant WHERE tenant_code = 'tenant_003' LIMIT 1);
INSERT INTO sys_user 
(tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at) 
VALUES 
(@tenant_id_3, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 
 'admin@tenant3.com', '13800138102', '教育管理员', 'admin', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ========================================
-- 9. 数据统计验证
-- ========================================

SELECT '=== 数据统计 ===' AS title;

SELECT 'Total Tenants' AS metric, COUNT(*) AS count FROM sys_tenant;
SELECT 'Total Users' AS metric, COUNT(*) AS count FROM sys_user;
SELECT 'Total Roles' AS metric, COUNT(*) AS count FROM sys_role;

SELECT '=== 租户统计 ===' AS title;
SELECT 
    t.id, 
    t.tenant_code, 
    t.tenant_name, 
    t.status,
    (SELECT COUNT(*) FROM sys_user WHERE tenant_id = t.id) AS user_count,
    (SELECT COUNT(*) FROM sys_role WHERE tenant_id = t.id) AS role_count
FROM sys_tenant t
ORDER BY t.id;

-- ========================================
-- 10. 完整性检查
-- ========================================

SELECT '=== 数据完整性检查 ===' AS title;

-- 检查是否有租户没有管理员
SELECT t.id, t.tenant_code, t.tenant_name 
FROM sys_tenant t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user u 
    WHERE u.tenant_id = t.id AND u.user_type = 'admin'
)
AS tenants_without_admin;

-- 检查是否有租户没有角色
SELECT t.id, t.tenant_code, t.tenant_name 
FROM sys_tenant t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role r 
    WHERE r.tenant_id = t.id
)
AS tenants_without_roles;

-- ========================================
-- 11. 测试账号信息 (用于登录测试)
-- ========================================

SELECT '=== 测试账号 ===' AS title;
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.email,
    u.phone,
    u.user_type,
    t.tenant_code,
    t.tenant_name,
    'Password: 123456 (需要 BCrypt 解密)' AS password_hint
FROM sys_user u
INNER JOIN sys_tenant t ON u.tenant_id = t.id
WHERE u.status = 'active'
ORDER BY t.id, u.user_type DESC;

-- ========================================
-- 12. 执行完成日志
-- ========================================

SELECT '✅ 租户初始化完成！' AS completion_status;
SELECT NOW() AS execution_time;
SELECT 'All tenant management data initialized successfully' AS message;

-- ========================================
-- 说明
-- ========================================
/*

【执行步骤】
1. 打开 MySQL 数据库管理工具 (如 MySQL Workbench 或 SQLTools)
2. 连接到 MySQL 9.6 数据库 (localhost:3308)
3. 复制本脚本的全部内容
4. 粘贴到查询编辑器
5. 执行 (Ctrl+Enter 或点击执行按钮)

【默认测试账号】
- 用户名: admin / staff / learner
- 密码: 123456
- 租户编码: tenant_001 (主租户)

【关键说明】
- 脚本使用 ON DUPLICATE KEY UPDATE 避免重复插入
- 所有密码均为 BCrypt 加密形式
- 支持多租户隔离
- 可根据需要调整租户和用户数据

【注意事项】
- 必须在 Flyway 初始化完成后执行 (应用启动时自动执行)
- 如需清空数据，取消注释第 2 部分的 DELETE 语句
- 建议在测试环境执行，生产环境需要谨慎

*/
