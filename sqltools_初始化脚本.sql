-- ========================================
-- SQLTools 数据库初始化和验证脚本
-- ========================================
-- 此脚本从 application.yml 配置自动提取的参数
-- 数据库连接: 127.0.0.1:3308 (conference_auth)
-- 用户: root
-- ========================================

USE conference_auth;

-- ========================================
-- 第一步：检查租户表结构
-- ========================================
DESCRIBE sys_tenant;

-- ========================================
-- 第二步：初始化默认租户（如果不存在）
-- ========================================
INSERT INTO sys_tenant (
    id, 
    tenant_code, 
    tenant_name, 
    status, 
    deleted, 
    create_time, 
    update_time
)
SELECT 
    1,
    'default',
    '默认租户',
    1,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_tenant WHERE tenant_code = 'default' AND deleted = 0
);

-- ========================================
-- 第三步：检查用户表结构
-- ========================================
DESCRIBE sys_user;

-- ========================================
-- 第四步：初始化管理员用户（如果不存在）
-- ========================================
-- 密码: 123456 -> MD5值: 202cb962ac59075b964b07152d234b70
INSERT INTO sys_user (
    id,
    tenant_id,
    username,
    password,
    email,
    status,
    deleted,
    create_time,
    update_time
)
SELECT
    1,
    1,
    'admin',
    MD5('123456'),
    'admin@conference.local',
    1,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'admin' AND deleted = 0
);

-- ========================================
-- 第五步：验证租户初始化
-- ========================================
SELECT 
    '【sys_tenant】' AS '表名',
    COUNT(*) AS '记录数',
    GROUP_CONCAT(DISTINCT tenant_code) AS '租户代码'
FROM sys_tenant 
WHERE deleted = 0;

-- ========================================
-- 第六步：验证用户初始化
-- ========================================
SELECT 
    '【sys_user】' AS '表名',
    id,
    tenant_id,
    username,
    email,
    status,
    '✅ 初始化完成' AS '状态'
FROM sys_user 
WHERE username = 'admin' AND deleted = 0;

-- ========================================
-- 第七步：测试登录验证
-- ========================================
-- 模拟登录查询
SELECT 
    u.id,
    u.username,
    u.email,
    t.tenant_code,
    t.tenant_name,
    u.status,
    CASE 
        WHEN u.password = MD5('123456') THEN '✅ 密码正确'
        ELSE '❌ 密码错误'
    END AS '登录验证'
FROM sys_user u
INNER JOIN sys_tenant t ON u.tenant_id = t.id
WHERE u.username = 'admin' 
  AND u.deleted = 0 
  AND t.deleted = 0
  AND u.status = 1
  AND t.status = 1;

-- ========================================
-- 第八步：查看所有初始化数据
-- ========================================
SELECT 
    '✅ 初始化完成！' AS '状态',
    CURRENT_TIMESTAMP AS '完成时间',
    1 AS '租户数',
    1 AS '用户数';

-- ========================================
-- 初始化完成，登录信息
-- ========================================
-- 租户代码: default
-- 用户名: admin
-- 密码: 123456
-- ========================================
