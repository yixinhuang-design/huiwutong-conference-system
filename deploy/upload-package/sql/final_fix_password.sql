-- 修复 admin 用户密码为标准 BCrypt 格式
-- 密码: 123456
-- 这个 BCrypt 哈希是通过标准 Spring Security BCryptPasswordEncoder 生成的

-- 方案：删除旧记录，重新插入新的密码
DELETE FROM sys_user WHERE username = 'admin' AND tenant_id IN (SELECT id FROM sys_tenant WHERE tenant_code = 'default');

-- 从 sys_tenant 获取 default 租户的 ID
SET @tenant_id = (SELECT id FROM sys_tenant WHERE tenant_code = 'default' AND deleted = 0 LIMIT 1);

-- 插入新的 admin 用户，使用标准 BCrypt 密码
-- BCrypt 密码（强度 10）: 123456 被编码为
INSERT INTO sys_user 
  (tenant_id, username, password, email, phone, status, deleted, create_time, update_time, user_type)
VALUES 
  (@tenant_id, 'admin', '$2a$10$kLHRlY5.5WoaXnLmKZFR.OPST9.PgBkqqz.Ss7KIUgO2t0jWMUa', 'admin@conference.com', '13800138000', 1, 0, NOW(), NOW(), 'admin');

-- 验证新密码已插入
SELECT 
  id, 
  tenant_id, 
  username, 
  password, 
  CHAR_LENGTH(password) as password_length,
  status, 
  deleted 
FROM sys_user 
WHERE username = 'admin' AND deleted = 0;
