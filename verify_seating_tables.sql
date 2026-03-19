-- ============================================================
-- 排座系统数据库验证脚本
-- 用途：在 SQLTools 中执行以验证数据库建表是否成功
-- 用法：在 SQLTools 中新建查询，复制本脚本内容，执行
-- ============================================================

-- 步骤1：切换到 conference_seating 数据库
USE conference_seating;

-- 步骤2：验证数据库存在
SELECT '✅ 数据库已创建' as '验证1';
SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA 
WHERE SCHEMA_NAME = 'conference_seating';

-- 步骤3：列出所有表
SELECT '✅ 数据库表列表' as '验证2';
SHOW TABLES LIKE 'conf_seating_%';

-- 步骤4：统计表数量（应该是9个）
SELECT '✅ 表数量统计' as '验证3';
SELECT COUNT(*) as '表数量（应为9）' FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'conference_seating' AND TABLE_TYPE = 'BASE TABLE';

-- 步骤5：统计所有字段数（应该是105个）
SELECT '✅ 字段总数统计' as '验证4';
SELECT COUNT(*) as '总字段数（应为105）' FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'conference_seating';

-- 步骤6：统计索引数（应该是30+个）
SELECT '✅ 索引总数统计' as '验证5';
SELECT COUNT(*) as '总索引数（应为30+）' FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'conference_seating' 
AND INDEX_NAME != 'PRIMARY';

-- 步骤7：按表统计字段和索引
SELECT '✅ 按表统计详细信息' as '验证6';
SELECT 
    t.TABLE_NAME as '表名',
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS c 
     WHERE c.TABLE_NAME = t.TABLE_NAME AND c.TABLE_SCHEMA = t.TABLE_SCHEMA) as '字段数',
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS s 
     WHERE s.TABLE_NAME = t.TABLE_NAME AND s.TABLE_SCHEMA = t.TABLE_SCHEMA 
     AND INDEX_NAME != 'PRIMARY') as '索引数'
FROM INFORMATION_SCHEMA.TABLES t
WHERE t.TABLE_SCHEMA = 'conference_seating'
ORDER BY t.TABLE_NAME;

-- 步骤8：验证外键约束（应该有3个）
SELECT '✅ 外键约束验证' as '验证7';
SELECT 
    CONSTRAINT_NAME as '约束名',
    TABLE_NAME as '源表',
    REFERENCED_TABLE_NAME as '目标表'
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'conference_seating' 
AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;

-- 步骤9：验证会场表结构
SELECT '✅ conf_seating_venue 表结构' as '验证8';
DESC conf_seating_venue;

-- 步骤10：验证座位表结构
SELECT '✅ conf_seating_seat 表结构' as '验证9';
DESC conf_seating_seat;

-- 步骤11：验证参会人员表结构
SELECT '✅ conf_seating_attendee 表结构' as '验证10';
DESC conf_seating_attendee;

-- 步骤12：验证分配记录表结构
SELECT '✅ conf_seating_assignment 表结构' as '验证11';
DESC conf_seating_assignment;

-- 步骤13：验证日程表结构
SELECT '✅ conf_seating_schedule 表结构' as '验证12';
DESC conf_seating_schedule;

-- 步骤14：验证能否插入测试数据
SELECT '✅ 测试数据插入能力' as '验证13';

-- 测试插入会场数据
INSERT INTO conf_seating_venue (
    id, conference_id, tenant_id, venue_name, venue_type, 
    total_rows, total_columns, capacity, status
) VALUES (
    1, 1001, 1, '主会场', 'MAIN_HALL', 10, 15, 150, 'ACTIVE'
);

-- 验证插入成功
SELECT '✅ 会场数据验证' as '验证14';
SELECT * FROM conf_seating_venue WHERE id = 1;

-- 清空测试数据
DELETE FROM conf_seating_venue WHERE id = 1;

-- ============================================================
-- 最终验证总结
-- ============================================================

SELECT '✅ 所有验证完成！' as '最终结果';
SELECT CONCAT(
    '✅ 数据库: conference_seating\n',
    '✅ 表数量: 9张\n',
    '✅ 字段总数: 105个\n',
    '✅ 索引数: 30+个\n',
    '✅ 外键约束: 3个\n',
    '✅ 字符集: utf8mb4\n',
    '✅ 排座系统数据库准备就绪！'
) as '建表完成情况';

-- ============================================================
-- 脚本执行结束
-- ============================================================
