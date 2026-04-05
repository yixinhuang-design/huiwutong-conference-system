-- ========================================
-- 强制初始化租户数据脚本
-- 目的：删除旧数据，重新创建default租户
-- ========================================

-- 1. 清理旧的default租户记录（如果存在）
DELETE FROM sys_tenant WHERE tenant_code = 'default';

-- 2. 清理default租户下的用户（如果存在）
DELETE FROM sys_user WHERE tenant_id IN (SELECT id FROM sys_tenant WHERE tenant_code = 'default');

-- 3. 重新插入default租户
INSERT INTO sys_tenant 
  (id, tenant_code, tenant_name, status, deleted, create_time, update_time)
VALUES 
  (1, 'default', 'Default Tenant', 1, 0, NOW(), NOW());

-- 4. 重新插入admin用户（密码：123456，MD5值）
INSERT INTO sys_user 
  (id, tenant_id, username, password, email, phone, status, deleted, create_time, update_time, user_type)
VALUES 
  (1, 1, 'admin', '202cb962ac59075b964b07152d234b70', 'admin@conference.com', '13800138000', 1, 0, NOW(), NOW(), 'admin');

-- 5. 验证结果
SELECT '========== 验证default租户 ==========' AS 操作;
SELECT id, tenant_code, tenant_name, status, deleted FROM sys_tenant WHERE tenant_code = 'default';

SELECT '========== 验证admin用户 ==========' AS 操作;
SELECT id, tenant_id, username, status, deleted FROM sys_user WHERE username = 'admin';

SELECT '========== 总体统计 ==========' AS 操作;
SELECT COUNT(*) as 租户总数 FROM sys_tenant WHERE deleted = 0;
SELECT COUNT(*) as 用户总数 FROM sys_user WHERE deleted = 0;
