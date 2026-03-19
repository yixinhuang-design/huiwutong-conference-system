-- 使用 HEX 和 SUBSTRING 验证密码完整性
SELECT 
  id,
  username,
  password,
  LENGTH(password) as byte_length,
  CHAR_LENGTH(password) as char_length,
  HEX(password) as password_hex
FROM sys_user 
WHERE username = 'admin' AND deleted = 0;

-- 查看前 20 个字符
SELECT SUBSTRING(password, 1, 20) as first_20 FROM sys_user WHERE username = 'admin' AND deleted = 0;

-- 查看整个密码
SELECT password FROM sys_user WHERE username = 'admin' AND deleted = 0;
