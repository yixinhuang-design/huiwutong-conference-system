# 数据库 SQL 示例与查询手册

## 📚 目录

1. [基础查询](#基础查询)
2. [用户管理](#用户管理)
3. [角色权限](#角色权限)
4. [多租户查询](#多租户查询)
5. [统计分析](#统计分析)
6. [数据维护](#数据维护)

---

## 基础查询

### 1.1 查看所有租户
```sql
SELECT 
  id,
  tenant_code,
  tenant_name,
  description,
  status,
  created_at
FROM sys_tenant
ORDER BY created_at DESC;
```

**预期结果**
```
id | tenant_code | tenant_name    | status | created_at
---+-------------+----------------+--------+------------------
1  | tenant_001  | 演示租户1      | active | 2025-01-26 ...
2  | tenant_002  | 演示租户2      | active | 2025-01-26 ...
3  | tenant_003  | 教育培训机构   | active | 2025-01-26 ...
...
```

### 1.2 查看租户详情（含统计）
```sql
SELECT 
  t.id,
  t.tenant_code,
  t.tenant_name,
  COUNT(DISTINCT u.id) as user_count,
  COUNT(DISTINCT r.id) as role_count,
  t.status
FROM sys_tenant t
LEFT JOIN sys_user u ON t.id = u.tenant_id
LEFT JOIN sys_role r ON t.id = r.tenant_id
GROUP BY t.id
ORDER BY t.created_at DESC;
```

**预期结果**
```
tenant_code | tenant_name  | user_count | role_count | status
------------+--------------+------------+------------+--------
tenant_001  | 演示租户1    | 7          | 6          | active
tenant_002  | 演示租户2    | 3          | 1          | active
tenant_003  | 教育培训机构 | 3          | 3          | active
```

---

## 用户管理

### 2.1 查看某租户的所有用户
```sql
-- 查看租户 1 的用户
SELECT 
  id,
  username,
  real_name,
  email,
  phone,
  status,
  created_at,
  last_login_at
FROM sys_user
WHERE tenant_id = 1
ORDER BY created_at DESC;
```

**替换 tenant_id 值即可查看其他租户**

### 2.2 查看某用户的详细信息
```sql
-- 查看租户 1 中用户名为 admin 的用户信息
SELECT 
  u.id,
  u.username,
  u.real_name,
  u.email,
  u.phone,
  u.status,
  COUNT(DISTINCT ur.role_id) as role_count,
  u.created_at,
  u.last_login_at
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id AND ur.tenant_id = u.tenant_id
WHERE u.tenant_id = 1
  AND u.username = 'admin'
GROUP BY u.id;
```

### 2.3 查看用户的角色列表
```sql
-- 查看租户 1 中 admin 用户有什么角色
SELECT 
  u.username,
  r.role_name,
  r.role_code,
  r.description
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id AND ur.tenant_id = u.tenant_id
JOIN sys_role r ON ur.role_id = r.id AND r.tenant_id = u.tenant_id
WHERE u.tenant_id = 1
  AND u.username = 'admin';
```

**预期结果**
```
username | role_name | role_code | description
---------+-----------+-----------+-------------------
admin    | 管理员    | admin     | 系统管理员
```

### 2.4 查看活跃用户
```sql
-- 查看所有活跃用户及其最后登录时间
SELECT 
  u.tenant_id,
  u.username,
  u.real_name,
  u.status,
  u.last_login_at,
  DATEDIFF(NOW(), u.last_login_at) as days_since_login
FROM sys_user u
WHERE u.status = 'active'
ORDER BY u.last_login_at DESC;
```

### 2.5 查看未使用过的账户
```sql
-- 查看从未登录过的用户
SELECT 
  u.id,
  u.username,
  u.real_name,
  u.created_at
FROM sys_user u
WHERE u.last_login_at IS NULL
ORDER BY u.created_at DESC;
```

### 2.6 添加新用户
```sql
-- 向租户 1 添加新用户
INSERT INTO sys_user (
  id, 
  tenant_id, 
  username, 
  password,  -- 应该使用加密密码
  email, 
  phone,
  real_name, 
  status, 
  created_at, 
  updated_at
) VALUES (
  100,                -- 用户 ID（确保唯一）
  1,                  -- 租户 ID
  'newuser',          -- 用户名
  '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO',  -- 密码（已加密 123456）
  'newuser@email.com',-- 邮箱
  '13800138888',      -- 电话
  '新用户',            -- 真实姓名
  'active',           -- 状态
  NOW(),              -- 创建时间
  NOW()               -- 更新时间
);
```

### 2.7 更新用户信息
```sql
-- 更新用户电话和邮箱
UPDATE sys_user
SET 
  email = 'newemail@example.com',
  phone = '13900139999',
  updated_at = NOW()
WHERE tenant_id = 1 AND id = 100;

-- 禁用用户账户
UPDATE sys_user
SET status = 'locked'
WHERE tenant_id = 1 AND username = 'newuser';

-- 激活被锁定的用户
UPDATE sys_user
SET status = 'active'
WHERE tenant_id = 1 AND id = 100;
```

### 2.8 删除用户
```sql
-- 软删除用户（推荐，保留数据记录）
UPDATE sys_user
SET deleted_at = NOW()
WHERE tenant_id = 1 AND id = 100;

-- 硬删除用户（谨慎！不可恢复）
DELETE FROM sys_user_role WHERE user_id = 100;
DELETE FROM sys_user WHERE id = 100;
```

---

## 角色权限

### 3.1 查看所有角色
```sql
-- 查看租户 1 的所有角色
SELECT 
  id,
  role_name,
  role_code,
  description,
  status
FROM sys_role
WHERE tenant_id = 1
ORDER BY id;
```

### 3.2 查看某角色的权限
```sql
-- 查看租户 1 中管理员角色有什么权限
SELECT 
  r.role_name,
  p.permission_name,
  p.permission_code,
  p.resource_type,
  p.action
FROM sys_role r
LEFT JOIN sys_role_permission rp ON r.id = rp.role_id
LEFT JOIN sys_permission p ON rp.permission_id = p.id
WHERE r.tenant_id = 1 AND r.role_code = 'admin'
ORDER BY p.permission_code;
```

### 3.3 查看有某权限的角色
```sql
-- 查看谁有"查看用户"的权限
SELECT DISTINCT
  r.role_name,
  r.role_code,
  p.permission_name
FROM sys_role r
JOIN sys_role_permission rp ON r.id = rp.role_id
JOIN sys_permission p ON rp.permission_id = p.id
WHERE p.permission_code = 'user.view'
ORDER BY r.role_name;
```

### 3.4 添加新角色
```sql
-- 向租户 1 添加新角色
INSERT INTO sys_role (
  id,
  tenant_id,
  role_name,
  role_code,
  description,
  status,
  created_at,
  updated_at
) VALUES (
  100,           -- 角色 ID
  1,             -- 租户 ID
  '审计员',       -- 角色名
  'auditor',      -- 角色编码
  '用于审计和日志查看', -- 描述
  'active',       -- 状态
  NOW(),
  NOW()
);
```

### 3.5 为角色分配权限
```sql
-- 将"查看用户"权限分配给"审计员"角色
INSERT INTO sys_role_permission (
  id,
  tenant_id,
  role_id,
  permission_id,
  created_at
) VALUES (
  500,          -- 关联 ID
  1,            -- 租户 ID
  100,          -- 角色 ID（审计员）
  1,            -- 权限 ID（查看用户）
  NOW()
);
```

### 3.6 移除角色权限
```sql
-- 移除某角色的某个权限
DELETE FROM sys_role_permission
WHERE tenant_id = 1 
  AND role_id = 100 
  AND permission_id = 1;
```

### 3.7 查看某用户的全部权限（链式查询）
```sql
-- 查看租户 1 中 admin 用户的所有权限
SELECT DISTINCT
  u.username,
  r.role_name,
  p.permission_name,
  p.permission_code,
  p.resource_type
FROM sys_user u
JOIN sys_user_role ur ON u.id = ur.user_id AND ur.tenant_id = u.tenant_id
JOIN sys_role r ON ur.role_id = r.id AND r.tenant_id = u.tenant_id
LEFT JOIN sys_role_permission rp ON r.id = rp.role_id
LEFT JOIN sys_permission p ON rp.permission_id = p.id
WHERE u.tenant_id = 1 AND u.username = 'admin'
ORDER BY r.role_name, p.permission_code;
```

---

## 多租户查询

### 4.1 对比两个租户的用户数
```sql
-- 比较租户 1 和租户 2 的用户数
SELECT 
  t.tenant_code,
  t.tenant_name,
  COUNT(u.id) as user_count,
  COUNT(CASE WHEN u.status = 'active' THEN 1 END) as active_count,
  COUNT(CASE WHEN u.status = 'locked' THEN 1 END) as locked_count
FROM sys_tenant t
LEFT JOIN sys_user u ON t.id = u.tenant_id
WHERE t.id IN (1, 2)
GROUP BY t.id
ORDER BY t.id;
```

### 4.2 跨租户查询（按用户名）
```sql
-- 查找名称为 "admin" 的用户在所有租户中的分布
SELECT 
  t.tenant_code,
  t.tenant_name,
  u.username,
  u.email,
  u.status
FROM sys_user u
JOIN sys_tenant t ON u.tenant_id = t.id
WHERE u.username = 'admin'
ORDER BY t.id;
```

### 4.3 租户间隔离验证
```sql
-- 验证租户隔离：确认租户 1 的用户无法看到租户 2 的用户
SELECT 
  (SELECT COUNT(*) FROM sys_user WHERE tenant_id = 1) as tenant1_users,
  (SELECT COUNT(*) FROM sys_user WHERE tenant_id = 2) as tenant2_users,
  (SELECT COUNT(*) FROM sys_user) as total_users;
```

---

## 统计分析

### 5.1 租户统计概览
```sql
-- 获取所有租户的统计概览
SELECT 
  t.id,
  t.tenant_code,
  t.tenant_name,
  t.status,
  COALESCE(u_count.cnt, 0) as user_count,
  COALESCE(r_count.cnt, 0) as role_count,
  COALESCE(p_count.cnt, 0) as permission_count,
  t.created_at
FROM sys_tenant t
LEFT JOIN (
  SELECT tenant_id, COUNT(*) as cnt FROM sys_user GROUP BY tenant_id
) u_count ON t.id = u_count.tenant_id
LEFT JOIN (
  SELECT tenant_id, COUNT(*) as cnt FROM sys_role GROUP BY tenant_id
) r_count ON t.id = r_count.tenant_id
LEFT JOIN (
  SELECT tenant_id, COUNT(*) as cnt FROM sys_permission GROUP BY tenant_id
) p_count ON t.id = p_count.tenant_id
ORDER BY t.id;
```

### 5.2 用户活跃度统计
```sql
-- 按登录情况统计用户
SELECT 
  t.tenant_code,
  u.status,
  COUNT(*) as user_count,
  COUNT(CASE WHEN u.last_login_at IS NOT NULL THEN 1 END) as logged_in_count,
  COUNT(CASE WHEN u.last_login_at IS NULL THEN 1 END) as never_logged_in,
  ROUND(AVG(DATEDIFF(NOW(), u.last_login_at)), 0) as avg_days_since_login
FROM sys_user u
JOIN sys_tenant t ON u.tenant_id = t.id
WHERE u.deleted_at IS NULL
GROUP BY t.id, u.status
ORDER BY t.tenant_code, u.status;
```

### 5.3 角色使用情况
```sql
-- 统计每个角色被多少用户使用
SELECT 
  t.tenant_code,
  r.role_name,
  r.role_code,
  COUNT(DISTINCT ur.user_id) as user_count,
  COUNT(DISTINCT rp.permission_id) as permission_count
FROM sys_role r
LEFT JOIN sys_user_role ur ON r.id = ur.role_id
LEFT JOIN sys_role_permission rp ON r.id = rp.role_id
JOIN sys_tenant t ON r.tenant_id = t.id
GROUP BY r.tenant_id, r.id
ORDER BY t.tenant_code, user_count DESC;
```

### 5.4 权限覆盖率
```sql
-- 查看哪些权限没有被分配给任何角色
SELECT 
  t.tenant_code,
  p.permission_name,
  p.permission_code,
  COUNT(rp.role_id) as role_count
FROM sys_permission p
JOIN sys_tenant t ON p.tenant_id = t.id
LEFT JOIN sys_role_permission rp ON p.id = rp.permission_id
GROUP BY p.id
HAVING role_count = 0
ORDER BY t.tenant_code, p.permission_code;
```

---

## 数据维护

### 6.1 给用户批量分配角色
```sql
-- 将租户 1 的所有普通用户分配给"审核员"角色
INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, created_at)
SELECT 
  ROW_NUMBER() OVER (ORDER BY u.id) + 1000,  -- 生成唯一 ID
  u.tenant_id,
  u.id,
  5,  -- 角色 ID 5 是审核员
  NOW()
FROM sys_user u
WHERE u.tenant_id = 1 
  AND u.status = 'active'
  AND u.id NOT IN (
    SELECT user_id FROM sys_user_role 
    WHERE tenant_id = 1 AND role_id = 5
  );
```

### 6.2 同步用户状态
```sql
-- 更新最后登录时间
UPDATE sys_user
SET last_login_at = NOW()
WHERE id = 1 AND tenant_id = 1;

-- 将 30 天未登录的用户标记为非活跃
UPDATE sys_user
SET status = 'inactive'
WHERE status = 'active'
  AND (last_login_at IS NULL OR DATEDIFF(NOW(), last_login_at) > 30);
```

### 6.3 清理数据
```sql
-- 清理已删除用户的角色关联（可选）
DELETE FROM sys_user_role
WHERE user_id IN (
  SELECT id FROM sys_user WHERE deleted_at IS NOT NULL
);

-- 清理孤立的权限分配
DELETE FROM sys_role_permission
WHERE permission_id NOT IN (
  SELECT id FROM sys_permission
);
```

### 6.4 数据一致性检查
```sql
-- 检查是否存在引用不存在的租户的用户
SELECT COUNT(*) as orphan_users
FROM sys_user u
WHERE u.tenant_id NOT IN (SELECT id FROM sys_tenant);

-- 检查是否存在没有分配角色的用户
SELECT u.id, u.username
FROM sys_user u
WHERE u.id NOT IN (
  SELECT DISTINCT user_id FROM sys_user_role
);

-- 检查是否存在重复的用户名（同一租户内）
SELECT tenant_id, username, COUNT(*) as cnt
FROM sys_user
WHERE deleted_at IS NULL
GROUP BY tenant_id, username
HAVING cnt > 1;
```

---

## 🎯 常用查询速查表

| 需求 | 查询 |
|------|------|
| 查看所有租户 | `SELECT * FROM sys_tenant;` |
| 查看租户 1 的用户 | `SELECT * FROM sys_user WHERE tenant_id = 1;` |
| 查看用户的角色 | `SELECT * FROM sys_user_role WHERE user_id = 1;` |
| 查看角色的权限 | `SELECT * FROM sys_role_permission WHERE role_id = 1;` |
| 添加用户 | `INSERT INTO sys_user VALUES (...);` |
| 给用户分配角色 | `INSERT INTO sys_user_role VALUES (...);` |
| 更新用户信息 | `UPDATE sys_user SET ... WHERE id = ?;` |
| 删除用户 | `UPDATE sys_user SET deleted_at = NOW() WHERE id = ?;` |

---

**提示：所有查询都应该过滤 deleted_at 字段，确保只查询有效数据！** ✨
