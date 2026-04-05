-- 检查现有数据
SELECT '========== 检查现有租户 ==========' AS status;
SELECT * FROM sys_tenant;

SELECT '========== 检查现有用户 ==========' AS status;
SELECT id, username FROM sys_user;

SELECT '========== 清理default租户 ==========' AS status;
DELETE FROM sys_tenant WHERE tenant_code = 'default';
DELETE FROM sys_user WHERE tenant_id IN (SELECT id FROM sys_tenant WHERE tenant_code = 'default') OR username = 'admin';

SELECT '========== 插入default租户 ==========' AS status;
INSERT INTO sys_tenant (tenant_code, tenant_name, status, deleted, create_time, update_time)
VALUES ('default', 'Default Tenant', 1, 0, NOW(), NOW());

SET @tenant_id = LAST_INSERT_ID();

SELECT '========== 获取新租户ID ==========' AS status;
SELECT @tenant_id AS new_tenant_id;

SELECT '========== 插入admin用户 ==========' AS status;
INSERT INTO sys_user (tenant_id, username, password, email, phone, status, deleted, create_time, update_time, user_type)
VALUES (@tenant_id, 'admin', '202cb962ac59075b964b07152d234b70', 'admin@conference.com', '13800138000', 1, 0, NOW(), NOW(), 'admin');

SELECT '========== 验证结果 ==========' AS status;
SELECT * FROM sys_tenant WHERE tenant_code = 'default';
SELECT * FROM sys_user WHERE username = 'admin';
