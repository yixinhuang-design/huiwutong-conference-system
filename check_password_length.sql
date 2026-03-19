-- 检查 admin 用户密码的完整内容和长度
SELECT 
    id,
    username,
    password,
    CHAR_LENGTH(password) as password_length,
    HEX(password) as password_hex
FROM sys_user 
WHERE username = 'admin' AND deleted = 0;

-- 检查密码字段的定义
DESCRIBE sys_user;

-- 查看 password 字段的具体类型
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    CHARACTER_SET_NAME,
    COLLATION_NAME,
    IS_NULLABLE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'password';
