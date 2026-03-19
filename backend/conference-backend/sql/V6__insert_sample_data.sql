-- ========================================
-- 多租户管理系统 - 扩展模拟数据脚本
-- ========================================
-- 说明：此脚本用于在基础模拟数据之上添加更多测试数据
-- 注意：ID 从现有数据之后开始，避免重复
-- ========================================

-- ========================================
-- 1. 添加更多租户
-- ========================================

-- 添加租户3-5 用于测试多租户隔离
INSERT INTO sys_tenant (id, tenant_code, tenant_name, description, status, created_at, updated_at) VALUES
(3, 'tenant_003', '教育培训机构', '用于教育行业的租户', 'active', NOW(), NOW()),
(4, 'tenant_004', '企业会议系统', '用于企业年会的租户', 'active', NOW(), NOW()),
(5, 'tenant_005', '学术论坛', '用于学术交流的租户', 'inactive', NOW(), NOW());

-- ========================================
-- 2. 添加更多用户
-- ========================================

-- 租户1的用户
INSERT INTO sys_user (id, tenant_id, username, password, email, phone, real_name, status, created_at, updated_at) VALUES
(4, 1, 'zhangsan', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'zhangsan@tenant1.com', '13800138001', '张三', 'active', NOW(), NOW()),
(5, 1, 'lisi', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'lisi@tenant1.com', '13800138002', '李四', 'active', NOW(), NOW()),
(6, 1, 'wangwu', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'wangwu@tenant1.com', '13800138003', '王五', 'active', NOW(), NOW()),
(7, 1, 'zhaoliu', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'zhaoliu@tenant1.com', '13800138004', '赵六', 'locked', NOW(), NOW());

-- 租户2的用户
INSERT INTO sys_user (id, tenant_id, username, password, email, phone, real_name, status, created_at, updated_at) VALUES
(8, 2, 'user2', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'user2@tenant2.com', '13800138005', '用户2', 'active', NOW(), NOW()),
(9, 2, 'user3', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'user3@tenant2.com', '13800138006', '用户3', 'active', NOW(), NOW());

-- 租户3的用户
INSERT INTO sys_user (id, tenant_id, username, password, email, phone, real_name, status, created_at, updated_at) VALUES
(10, 3, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@tenant3.com', '13800138007', '管理员', 'active', NOW(), NOW()),
(11, 3, 'teacher1', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'teacher1@tenant3.com', '13800138008', '教师1', 'active', NOW(), NOW()),
(12, 3, 'student1', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'student1@tenant3.com', '13800138009', '学生1', 'active', NOW(), NOW());

-- 租户4的用户
INSERT INTO sys_user (id, tenant_id, username, password, email, phone, real_name, status, created_at, updated_at) VALUES
(13, 4, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@tenant4.com', '13800138010', '管理员', 'active', NOW(), NOW()),
(14, 4, 'ceo', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'ceo@tenant4.com', '13800138011', '首席执行官', 'active', NOW(), NOW()),
(15, 4, 'manager1', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'manager1@tenant4.com', '13800138012', '部门经理1', 'active', NOW(), NOW()),
(16, 4, 'manager2', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'manager2@tenant4.com', '13800138013', '部门经理2', 'active', NOW(), NOW());

-- 租户5的用户
INSERT INTO sys_user (id, tenant_id, username, password, email, phone, real_name, status, created_at, updated_at) VALUES
(17, 5, 'admin', '$2a$10$nzZg6ZbJI8JD1MQ5I5y82.qH7cq3C7Og1YJqDe5l8KPgNJlYRz9jO', 'admin@tenant5.com', '13800138014', '论坛管理员', 'active', NOW(), NOW());

-- ========================================
-- 3. 添加更多角色
-- ========================================

-- 租户1的角色
INSERT INTO sys_role (id, tenant_id, role_name, role_code, description, status, created_at, updated_at) VALUES
(4, 1, '主管', 'supervisor', '部门主管', 'active', NOW(), NOW()),
(5, 1, '审核员', 'reviewer', '内容审核员', 'active', NOW(), NOW()),
(6, 1, '访客', 'guest', '临时访客', 'active', NOW(), NOW());

-- 租户2的角色
INSERT INTO sys_role (id, tenant_id, role_name, role_code, description, status, created_at, updated_at) VALUES
(7, 2, '普通用户', 'user', '普通用户', 'active', NOW(), NOW());

-- 租户3的角色
INSERT INTO sys_role (id, tenant_id, role_name, role_code, description, status, created_at, updated_at) VALUES
(8, 3, '管理员', 'admin', '系统管理员', 'active', NOW(), NOW()),
(9, 3, '教师', 'teacher', '教师', 'active', NOW(), NOW()),
(10, 3, '学生', 'student', '学生', 'active', NOW(), NOW());

-- 租户4的角色
INSERT INTO sys_role (id, tenant_id, role_name, role_code, description, status, created_at, updated_at) VALUES
(11, 4, '管理员', 'admin', '系统管理员', 'active', NOW(), NOW()),
(12, 4, '董事长', 'chairman', '董事长', 'active', NOW(), NOW()),
(13, 4, '部门经理', 'manager', '部门经理', 'active', NOW(), NOW()),
(14, 4, '员工', 'employee', '普通员工', 'active', NOW(), NOW());

-- 租户5的角色
INSERT INTO sys_role (id, tenant_id, role_name, role_code, description, status, created_at, updated_at) VALUES
(15, 5, '论坛管理员', 'admin', '论坛管理员', 'active', NOW(), NOW()),
(16, 5, '版主', 'moderator', '版主', 'active', NOW(), NOW()),
(17, 5, '发言人', 'speaker', '发言人', 'active', NOW(), NOW());

-- ========================================
-- 4. 添加用户角色关联
-- ========================================

-- 租户1的用户角色
INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, created_at) VALUES
(4, 1, 4, 2, NOW()),    -- 张三是普通用户
(5, 1, 5, 4, NOW()),    -- 李四是主管
(6, 1, 6, 5, NOW()),    -- 王五是审核员
(7, 1, 7, 6, NOW());    -- 赵六是访客

-- 租户2的用户角色
INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, created_at) VALUES
(8, 2, 8, 7, NOW()),    -- user2是普通用户
(9, 2, 9, 7, NOW());    -- user3是普通用户

-- 租户3的用户角色
INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, created_at) VALUES
(10, 3, 10, 8, NOW()),  -- admin是管理员
(11, 3, 11, 9, NOW()),  -- teacher1是教师
(12, 3, 12, 10, NOW()); -- student1是学生

-- 租户4的用户角色
INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, created_at) VALUES
(13, 4, 13, 11, NOW()), -- admin是管理员
(14, 4, 14, 12, NOW()), -- ceo是董事长
(15, 4, 15, 13, NOW()), -- manager1是部门经理
(16, 4, 16, 13, NOW()); -- manager2是部门经理

-- 租户5的用户角色
INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, created_at) VALUES
(17, 5, 17, 15, NOW()); -- admin是论坛管理员

-- ========================================
-- 5. 添加权限数据（可选）
-- ========================================

-- 租户1的权限
INSERT INTO sys_permission (id, tenant_id, permission_name, permission_code, resource_type, action, created_at, updated_at) VALUES
(1, 1, '查看用户', 'user.view', 'user', 'read', NOW(), NOW()),
(2, 1, '创建用户', 'user.create', 'user', 'create', NOW(), NOW()),
(3, 1, '编辑用户', 'user.edit', 'user', 'update', NOW(), NOW()),
(4, 1, '删除用户', 'user.delete', 'user', 'delete', NOW(), NOW()),
(5, 1, '查看会议', 'conference.view', 'conference', 'read', NOW(), NOW()),
(6, 1, '创建会议', 'conference.create', 'conference', 'create', NOW(), NOW()),
(7, 1, '管理座位', 'seat.manage', 'seat', 'update', NOW(), NOW()),
(8, 1, '发送通知', 'notification.send', 'notification', 'create', NOW(), NOW());

-- 租户3的权限
INSERT INTO sys_permission (id, tenant_id, permission_name, permission_code, resource_type, action, created_at, updated_at) VALUES
(9, 3, '查看课程', 'course.view', 'course', 'read', NOW(), NOW()),
(10, 3, '创建课程', 'course.create', 'course', 'create', NOW(), NOW()),
(11, 3, '查看成绩', 'score.view', 'score', 'read', NOW(), NOW()),
(12, 3, '编辑成绩', 'score.edit', 'score', 'update', NOW(), NOW());

-- 租户4的权限
INSERT INTO sys_permission (id, tenant_id, permission_name, permission_code, resource_type, action, created_at, updated_at) VALUES
(13, 4, '查看财务报表', 'finance.view', 'finance', 'read', NOW(), NOW()),
(14, 4, '审批支出', 'finance.approve', 'finance', 'update', NOW(), NOW()),
(15, 4, '查看员工信息', 'employee.view', 'employee', 'read', NOW(), NOW());

-- ========================================
-- 6. 添加角色权限关联（可选）
-- ========================================

-- 租户1：管理员角色的权限
INSERT INTO sys_role_permission (id, tenant_id, role_id, permission_id, created_at) VALUES
(1, 1, 1, 1, NOW()),  -- 管理员 > 查看用户
(2, 1, 1, 2, NOW()),  -- 管理员 > 创建用户
(3, 1, 1, 3, NOW()),  -- 管理员 > 编辑用户
(4, 1, 1, 4, NOW()),  -- 管理员 > 删除用户
(5, 1, 1, 5, NOW()),  -- 管理员 > 查看会议
(6, 1, 1, 6, NOW()),  -- 管理员 > 创建会议
(7, 1, 1, 7, NOW()),  -- 管理员 > 管理座位
(8, 1, 1, 8, NOW());  -- 管理员 > 发送通知

-- 租户1：普通用户的权限
INSERT INTO sys_role_permission (id, tenant_id, role_id, permission_id, created_at) VALUES
(9, 1, 2, 1, NOW()),  -- 普通用户 > 查看用户（仅自己）
(10, 1, 2, 5, NOW()); -- 普通用户 > 查看会议

-- 租户3：管理员角色的权限
INSERT INTO sys_role_permission (id, tenant_id, role_id, permission_id, created_at) VALUES
(11, 3, 8, 9, NOW()),  -- 管理员 > 查看课程
(12, 3, 8, 10, NOW()), -- 管理员 > 创建课程
(13, 3, 8, 11, NOW()), -- 管理员 > 查看成绩
(14, 3, 8, 12, NOW()); -- 管理员 > 编辑成绩

-- 租户3：教师角色的权限
INSERT INTO sys_role_permission (id, tenant_id, role_id, permission_id, created_at) VALUES
(15, 3, 9, 9, NOW()),  -- 教师 > 查看课程
(16, 3, 9, 10, NOW()), -- 教师 > 创建课程
(17, 3, 9, 11, NOW()), -- 教师 > 查看成绩
(18, 3, 9, 12, NOW()); -- 教师 > 编辑成绩

-- 租户3：学生角色的权限
INSERT INTO sys_role_permission (id, tenant_id, role_id, permission_id, created_at) VALUES
(19, 3, 10, 9, NOW()),  -- 学生 > 查看课程
(20, 3, 10, 11, NOW()); -- 学生 > 查看成绩（仅自己）

-- ========================================
-- 验证语句（可在插入后执行）
-- ========================================

-- 查看所有租户
-- SELECT id, tenant_code, tenant_name, status FROM sys_tenant;

-- 查看租户1的所有用户
-- SELECT id, username, real_name, email, status FROM sys_user WHERE tenant_id = 1;

-- 查看租户1的用户角色关联
-- SELECT u.username, r.role_name 
-- FROM sys_user u
-- JOIN sys_user_role ur ON u.id = ur.user_id
-- JOIN sys_role r ON ur.role_id = r.id
-- WHERE u.tenant_id = 1;

-- 查看租户统计
-- SELECT 
--   t.tenant_code,
--   t.tenant_name,
--   COUNT(DISTINCT u.id) AS user_count,
--   COUNT(DISTINCT r.id) AS role_count
-- FROM sys_tenant t
-- LEFT JOIN sys_user u ON t.id = u.tenant_id
-- LEFT JOIN sys_role r ON t.id = r.tenant_id
-- GROUP BY t.id;

COMMIT;
