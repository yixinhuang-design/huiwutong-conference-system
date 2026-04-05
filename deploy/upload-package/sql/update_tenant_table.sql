-- 修改 sys_tenant 表，添加缺失的字段
-- 使用 ALTER TABLE IF ... 的方式逐个添加字段

ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_name VARCHAR(100) COMMENT '联系人名称' AFTER tenant_name;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(20) COMMENT '联系人电话/登录号码' AFTER contact_name;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS contact_email VARCHAR(100) COMMENT '联系人邮箱' AFTER contact_phone;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS domain VARCHAR(255) COMMENT '租户域名/标识' AFTER contact_email;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500) COMMENT '租户Logo URL' AFTER domain;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS tenant_type VARCHAR(50) COMMENT '客户类型：self-rent/full-pay' AFTER logo_url;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS valid_start_date DATE COMMENT '使用期限开始日期' AFTER tenant_type;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS valid_end_date DATE COMMENT '使用期限结束日期' AFTER valid_start_date;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS max_users INT COMMENT '最大用户数' AFTER valid_end_date;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS max_conferences INT COMMENT '最大会议数' AFTER max_users;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS storage_quota BIGINT COMMENT '存储配额(字节)' AFTER max_conferences;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS account_limit INT COMMENT '账户数量限制' AFTER storage_quota;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS expire_time TIMESTAMP NULL COMMENT '过期时间' AFTER account_limit;
ALTER TABLE sys_tenant ADD COLUMN IF NOT EXISTS remark VARCHAR(500) COMMENT '备注' AFTER expire_time;

-- 验证修改结果
DESC sys_tenant;
