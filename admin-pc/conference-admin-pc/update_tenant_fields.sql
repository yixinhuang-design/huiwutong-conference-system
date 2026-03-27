-- 使用 SQLTools 在 VS Code 中执行此脚本
-- 修改 sys_tenant 表，添加实际需要的字段

USE conference_auth;

-- 显示当前表结构（执行前）
SELECT '========== 修改前的表结构 ==========' AS '操作';
DESCRIBE sys_tenant;

-- 添加缺失的字段
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_name VARCHAR(100) COMMENT '联系人名称';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(20) COMMENT '联系人电话/登录号码';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_email VARCHAR(100) COMMENT '联系人邮箱';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS tenant_type VARCHAR(50) COMMENT '客户类型：self-rent/full-pay';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS valid_start_date DATE COMMENT '使用期限开始日期';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS valid_end_date DATE COMMENT '使用期限结束日期';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS max_users INT COMMENT '最大用户数';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS max_conferences INT COMMENT '最大会议数';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS storage_quota BIGINT COMMENT '存储配额(字节)';

-- 显示修改后的表结构
SELECT '========== 修改后的表结构 ==========' AS '操作';
DESCRIBE sys_tenant;

-- 验证数据（可选）
SELECT '========== 现有数据验证 ==========' AS '操作';
SELECT COUNT(*) AS '总记录数' FROM sys_tenant;
SELECT id, tenant_code, tenant_name, contact_name, contact_phone, status FROM sys_tenant LIMIT 5;
