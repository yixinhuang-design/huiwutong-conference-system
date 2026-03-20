-- Initialize tenant data for conference system
-- Author: AI Assistant
-- Date: 2026-02-27

USE conference_auth;

-- Insert main tenant
INSERT INTO sys_tenant (tenant_code, tenant_name, description, status, created_by, created_at, updated_at) 
VALUES ('MAIN_001', 'Main Tenant', 'Main system tenant', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Insert enterprise tenant  
INSERT INTO sys_tenant (tenant_code, tenant_name, description, status, created_by, created_at, updated_at)
VALUES ('ENT_002', 'Enterprise Edition', 'Enterprise version', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Insert education tenant
INSERT INTO sys_tenant (tenant_code, tenant_name, description, status, created_by, created_at, updated_at)
VALUES ('EDU_003', 'Education Edition', 'Education industry', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Insert event tenant
INSERT INTO sys_tenant (tenant_code, tenant_name, description, status, created_by, created_at, updated_at)
VALUES ('EVENT_004', 'Event Management', 'Large event conference', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Get main tenant ID
SET @main_tenant_id = (SELECT id FROM sys_tenant WHERE tenant_code = 'MAIN_001' LIMIT 1);

-- Insert users for main tenant
INSERT INTO sys_user (tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at)
VALUES (@main_tenant_id, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@example.com', '13800138000', 'System Admin', 'admin', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO sys_user (tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at)
VALUES (@main_tenant_id, 'staff', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'staff@example.com', '13800138001', 'Staff Member', 'staff', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO sys_user (tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at)
VALUES (@main_tenant_id, 'learner', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'learner@example.com', '13800138002', 'Learner', 'learner', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Insert roles for main tenant
INSERT INTO sys_role (tenant_id, role_code, role_name, description, data_scope, status, created_by, created_at, updated_at)
VALUES (@main_tenant_id, 'admin', 'System Admin', 'Administrator with all permissions', 1, 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO sys_role (tenant_id, role_code, role_name, description, data_scope, status, created_by, created_at, updated_at)
VALUES (@main_tenant_id, 'staff', 'Staff', 'Staff member role', 2, 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO sys_role (tenant_id, role_code, role_name, description, data_scope, status, created_by, created_at, updated_at)
VALUES (@main_tenant_id, 'user', 'User', 'Regular user role', 4, 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Get other tenant IDs
SET @ent_tenant_id = (SELECT id FROM sys_tenant WHERE tenant_code = 'ENT_002' LIMIT 1);
SET @edu_tenant_id = (SELECT id FROM sys_tenant WHERE tenant_code = 'EDU_003' LIMIT 1);
SET @event_tenant_id = (SELECT id FROM sys_tenant WHERE tenant_code = 'EVENT_004' LIMIT 1);

-- Insert users for other tenants
INSERT INTO sys_user (tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at)
VALUES (@ent_tenant_id, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@enterprise.com', '13800138010', 'Enterprise Admin', 'admin', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO sys_user (tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at)
VALUES (@edu_tenant_id, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@education.com', '13800138020', 'Education Admin', 'admin', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO sys_user (tenant_id, username, password, email, phone, real_name, user_type, status, created_by, created_at, updated_at)
VALUES (@event_tenant_id, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@event.com', '13800138030', 'Event Admin', 'admin', 'active', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Verification
SELECT '=== Tenants Created ===' as info;
SELECT id, tenant_code, tenant_name, status, created_at FROM sys_tenant ORDER BY id;

SELECT '=== Users Created ===' as info;
SELECT id, tenant_id, username, real_name, user_type, status FROM sys_user ORDER BY tenant_id, id;

SELECT '=== Roles Created ===' as info;
SELECT id, tenant_id, role_code, role_name, status FROM sys_role ORDER BY tenant_id, id;

-- Final statistics
SELECT '=== Data Statistics ===' as info;
SELECT 'Total Tenants' as metric, COUNT(*) as count FROM sys_tenant;
SELECT 'Total Users' as metric, COUNT(*) as count FROM sys_user;
SELECT 'Total Roles' as metric, COUNT(*) as count FROM sys_role;

SELECT 'Initialization completed successfully!' as status;
