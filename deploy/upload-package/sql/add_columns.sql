-- 添加缺失的字段到 sys_tenant 表
ALTER TABLE sys_tenant 
ADD COLUMN IF NOT EXISTS created_by BIGINT COMMENT '创建人ID' AFTER tenant_name,
ADD COLUMN IF NOT EXISTS updated_by BIGINT COMMENT '更新人ID' AFTER updated_at;

-- 验证字段是否已添加
SHOW COLUMNS FROM sys_tenant;
