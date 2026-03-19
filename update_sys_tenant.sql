-- 修改 sys_tenant 表：添加实际需要的字段
-- 注意：字段顺序要匹配前端和后端的实际使用

USE conference_auth;

-- 添加缺失的字段
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_name VARCHAR(100) COMMENT '联系人名称' AFTER tenant_name;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(20) COMMENT '联系人电话/登录号码' AFTER contact_name;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_email VARCHAR(100) COMMENT '联系人邮箱' AFTER contact_phone;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS tenant_type VARCHAR(50) COMMENT '客户类型：self-rent/full-pay' AFTER contact_email;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS valid_start_date DATE COMMENT '使用期限开始日期' AFTER tenant_type;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS valid_end_date DATE COMMENT '使用期限结束日期' AFTER valid_start_date;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS max_users INT COMMENT '最大用户数' AFTER valid_end_date;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS max_conferences INT COMMENT '最大会议数' AFTER max_users;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS storage_quota BIGINT COMMENT '存储配额(字节)' AFTER max_conferences;

-- 验证表结构
DESCRIBE sys_tenant;

-- 查看现有数据
SELECT id, tenant_code, tenant_name, contact_name, contact_phone, status FROM sys_tenant LIMIT 5;
