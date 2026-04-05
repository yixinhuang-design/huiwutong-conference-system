-- 排座系统数据库验证脚本
-- 检查数据库和表是否成功创建

-- 1. 检查数据库是否存在
SELECT '=== 检查数据库 ===' as '步骤1';
SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA 
WHERE SCHEMA_NAME = 'conference_seating';

-- 2. 切换到目标数据库
USE conference_seating;

-- 3. 列出所有表
SELECT '=== 检查表列表 ===' as '步骤2';
SHOW TABLES;

-- 4. 统计表数量
SELECT '=== 检查表数量 ===' as '步骤3';
SELECT COUNT(*) as '表数量（应为9）' FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'conference_seating' AND TABLE_TYPE = 'BASE TABLE';

-- 5. 按表统计字段数
SELECT '=== 检查表结构 ===' as '步骤4';
SELECT 
    TABLE_NAME as '表名',
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS c 
     WHERE c.TABLE_NAME = t.TABLE_NAME AND c.TABLE_SCHEMA = t.TABLE_SCHEMA) as '字段数'
FROM INFORMATION_SCHEMA.TABLES t
WHERE t.TABLE_SCHEMA = 'conference_seating' AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- 6. 检查索引数量
SELECT '=== 检查索引数量 ===' as '步骤5';
SELECT 
    TABLE_NAME as '表名',
    COUNT(DISTINCT INDEX_NAME) as '索引数'
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'conference_seating'
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;

-- 7. 检查外键约束
SELECT '=== 检查外键约束 ===' as '步骤6';
SELECT 
    CONSTRAINT_NAME as '约束名',
    TABLE_NAME as '源表',
    REFERENCED_TABLE_NAME as '目标表'
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'conference_seating' 
AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- 8. 最终验证总结
SELECT '=== 建表验证完成 ===' as '最终状态';
SELECT CASE 
    WHEN COUNT(*) = 9 THEN '✅ 所有9张表已成功创建'
    ELSE CONCAT('⚠️ 只创建了', COUNT(*), '张表，应为9张')
END as '验证结果'
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'conference_seating' AND TABLE_TYPE = 'BASE TABLE';
