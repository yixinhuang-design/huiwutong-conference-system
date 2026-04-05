-- ========================================
-- 更新admin用户密码为BCrypt格式
-- ========================================

-- BCrypt 密码格式说明：
-- 原密码: 123456
-- BCrypt算法: Spring Security 使用的 BCryptPasswordEncoder
-- 轮数: 10 (默认)
-- 格式: $2a$10$...

-- 更新admin用户密码为BCrypt格式（123456）
UPDATE sys_user 
SET password = '$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUe'
WHERE username = 'admin' AND deleted = 0;

-- 验证更新结果
SELECT id, username, password, user_type, status FROM sys_user WHERE username='admin';
SELECT password LIKE '$2a$%' as is_bcrypt FROM sys_user WHERE username = 'admin';
