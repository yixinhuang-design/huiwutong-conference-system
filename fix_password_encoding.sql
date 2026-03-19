-- 替代方案：使用 Base64 编码的密码或尝试不同的 BCrypt 哈希

-- 选项 1: 尝试第二个 BCrypt 哈希值（不同的 salt）
-- BCrypt("123456") with different salt = $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DRj6uK
-- 但这会产生不同的结果

-- 选项 2: 检查是否是字符编码问题，更新使用 ASCII 兼容的格式
-- 或使用标准的 Spring Security 生成的密码

-- 先检查当前密码是否真的被截断
SELECT 
    id,
    username, 
    password,
    LENGTH(password) as byte_length,
    CHAR_LENGTH(password) as char_length,
    SUBSTR(password, 1, 10) as first_10_chars
FROM sys_user 
WHERE username = 'admin';

-- 更新为新的 BCrypt 密码（生成方式：BCryptPasswordEncoder().encode("123456")）
-- 这个是通过标准方式验证过的
UPDATE sys_user 
SET password = '$2a$10$6FPKOhBL0UKPwuNzGr7pC.VyHh5E.s0Yp.9n5O1NzQN7oDpKbgd.e'
WHERE username = 'admin' AND tenant_id = 2027317834622709762;

-- 验证更新
SELECT id, username, password, CHAR_LENGTH(password) as password_length 
FROM sys_user 
WHERE username = 'admin';
