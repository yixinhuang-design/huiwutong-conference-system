SELECT '=== 验证数据初始化结果 ===' AS 状态;
SELECT '租户数据:' AS 检查;
SELECT id, tenant_code, tenant_name, status FROM sys_tenant WHERE deleted = 0;
SELECT '用户数据:' AS 检查;
SELECT id, tenant_id, username, status FROM sys_user WHERE deleted = 0;
SELECT 'admin用户密码验证:' AS 检查;
SELECT username, password = MD5('123456') AS 密码正确 FROM sys_user WHERE username = 'admin';
