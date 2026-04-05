-- 恢复超级管理员 admin 账户
-- 密码: 123456 (MD5: 202cb962ac59075b964b07152d234b70)
-- user_type: admin (超级管理员)

-- 查询当前 admin 账户
SELECT id, tenant_id, username, password, user_type, status FROM sys_user WHERE username='admin';

-- 更新 admin 账户为正确的配置
UPDATE sys_user 
SET 
  password = '202cb962ac59075b964b07152d234b70',  -- MD5(123456)
  user_type = 'admin',
  status = 1,
  deleted = 0
WHERE username = 'admin';

-- 验证更新结果
SELECT id, tenant_id, username, password, user_type, status FROM sys_user WHERE username='admin';
