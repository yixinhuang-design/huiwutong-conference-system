-- 修复租户禁用问题
UPDATE sys_tenant SET status = 'active' WHERE tenant_code = 'default';

-- 验证结果
SELECT id, tenant_code, tenant_name, status FROM sys_tenant WHERE tenant_code='default';

-- 如果需要重新初始化所有数据
-- TRUNCATE TABLE sys_tenant;
-- INSERT INTO sys_tenant (id, tenant_code, tenant_name, description, status, created_at, updated_at) 
-- VALUES (1, 'default', '默认租户', '系统默认租户', 'active', NOW(), NOW());

-- TRUNCATE TABLE sys_user;
-- INSERT INTO sys_user (id, tenant_id, username, password, real_name, status, created_at, updated_at) 
-- VALUES (1, 1, 'admin', '123456', '管理员', 1, NOW(), NOW());
