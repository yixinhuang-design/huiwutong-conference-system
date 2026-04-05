-- ========================================
-- 多租户系统初始化脚本
-- 时间: 2026-02-28
-- 功能: 自动初始化默认租户和管理员用户
-- ========================================

-- 1. 检查表结构
SELECT '检查系统租户表...' AS 操作;
DESCRIBE sys_tenant;

SELECT '检查系统用户表...' AS 操作;
DESCRIBE sys_user;

-- 2. 检查是否已存在default租户
SELECT '检查default租户是否存在...' AS 操作;
SELECT COUNT(*) as 现有租户数 FROM sys_tenant WHERE tenant_code = 'default' AND deleted = 0;

-- 3. 插入默认租户（使用INSERT IGNORE防止重复）
SELECT '准备插入default租户...' AS 操作;
INSERT IGNORE INTO sys_tenant (id, tenant_code, tenant_name, status, deleted, create_time, update_time)
VALUES (1, 'default', '默认租户', 1, 0, NOW(), NOW());

SELECT '✅ 默认租户已初始化' AS 操作;
SELECT * FROM sys_tenant WHERE tenant_code = 'default';

-- 4. 检查是否已存在admin用户
SELECT '检查admin用户是否存在...' AS 操作;
SELECT COUNT(*) as 现有用户数 FROM sys_user WHERE username = 'admin' AND deleted = 0;

-- 5. 插入管理员用户（密码: 123456）
SELECT '准备插入admin用户...' AS 操作;
INSERT IGNORE INTO sys_user (id, tenant_id, username, password, email, phone, status, deleted, create_time, update_time)
VALUES (1, 1, 'admin', MD5('123456'), 'admin@conference.com', '13800138000', 1, 0, NOW(), NOW());

SELECT '✅ 管理员用户已初始化' AS 操作;
SELECT id, tenant_id, username, email, status FROM sys_user WHERE username = 'admin';

-- 6. 最终验证
SELECT '=== 最终验证结果 ===' AS 验证;
SELECT '租户信息' AS 类型, COUNT(*) as 数量 FROM sys_tenant WHERE deleted = 0
UNION ALL
SELECT '用户信息', COUNT(*) FROM sys_user WHERE deleted = 0;

-- 7. 登录测试信息
SELECT '
╔════════════════════════════════════╗
║       登录测试信息                   ║
╠════════════════════════════════════╣
║ 租户代码: default                  ║
║ 用户名: admin                      ║
║ 密码: 123456                       ║
║ 访问地址: http://localhost:8080   ║
╚════════════════════════════════════╝
' AS 登录信息;
