-- ==================================================
-- 使用 SQLTools 执行此脚本来添加缺失的列到 sys_tenant 表
-- ==================================================

-- 第 1 步：检查当前表结构
DESCRIBE sys_tenant;

-- 第 2 步：添加缺失的列
-- 使用 IF NOT EXISTS 避免列重复添加
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_name VARCHAR(100) COMMENT '联系人名称';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(20) COMMENT '联系人电话/登录号码';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_email VARCHAR(100) COMMENT '联系人邮箱';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS domain VARCHAR(255) COMMENT '租户域名/标识';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500) COMMENT '租户Logo URL';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS tenant_type VARCHAR(50) COMMENT '客户类型：self-rent/full-pay';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS valid_start_date DATE COMMENT '使用期限开始日期';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS valid_end_date DATE COMMENT '使用期限结束日期';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS max_users INT COMMENT '最大用户数';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS max_conferences INT COMMENT '最大会议数';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS storage_quota BIGINT COMMENT '存储配额(字节)';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS account_limit INT COMMENT '账户数量限制';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS expire_time TIMESTAMP NULL COMMENT '过期时间';
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS remark VARCHAR(500) COMMENT '备注';

-- 第 3 步：验证所有列都已添加
DESCRIBE sys_tenant;

-- 第 4 步：检查现有数据
SELECT * FROM sys_tenant LIMIT 5;
