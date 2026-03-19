-- ============================================================
-- 初始化默认租户
-- 用于解决登录错误："租户不存在"
-- ============================================================

USE conference_auth;

-- 1. 检查 sys_tenant 表是否存在
SELECT '检查 sys_tenant 表...' as 'Step';
DESCRIBE sys_tenant;

-- 2. 检查现有租户数据
SELECT '当前租户数据：' as 'Step';
SELECT COUNT(*) as 租户数量 FROM sys_tenant WHERE deleted = 0;

-- 3. 插入默认租户（如果不存在）
SELECT '插入默认租户...' as 'Step';

INSERT INTO sys_tenant (
    id,
    tenant_code,
    tenant_name,
    description,
    status,
    created_at,
    updated_at,
    deleted
) 
SELECT 
    1,
    'default',
    '默认租户',
    '系统默认租户，用于测试',
    'active',
    NOW(),
    NOW(),
    0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_tenant WHERE tenant_code = 'default' AND deleted = 0
);

-- 4. 验证插入结果
SELECT '验证租户数据...' as 'Step';
SELECT 
    id,
    tenant_code as '租户编码',
    tenant_name as '租户名称',
    status as '状态',
    created_at as '创建时间'
FROM sys_tenant
WHERE tenant_code = 'default' AND deleted = 0;

-- 5. 检查 sys_user 表
SELECT '检查 sys_user 表...' as 'Step';
SELECT COUNT(*) as 用户数量 FROM sys_user WHERE deleted = 0;

-- 6. 创建默认管理员用户（如果不存在）
-- 注：密码需要根据实际的加密方式设置，这里使用示例
SELECT '插入默认管理员用户...' as 'Step';

INSERT INTO sys_user (
    id,
    tenant_id,
    username,
    password,
    email,
    real_name,
    status,
    created_at,
    updated_at,
    deleted
)
SELECT 
    1,
    1,
    'admin',
    MD5('123456'),  -- 密码：123456
    'admin@default.com',
    '系统管理员',
    'active',
    NOW(),
    NOW(),
    0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'admin' AND tenant_id = 1 AND deleted = 0
);

-- 7. 验证用户数据
SELECT '验证用户数据...' as 'Step';
SELECT 
    id,
    username as '用户名',
    email as '邮箱',
    real_name as '真实姓名',
    status as '状态'
FROM sys_user
WHERE tenant_id = 1 AND deleted = 0;

-- 8. 完成提示
SELECT '✅ 初始化完成！' as '结果',
       'tenant_code: default' as '租户编码',
       'username: admin' as '用户名',
       'password: 123456' as '密码';
