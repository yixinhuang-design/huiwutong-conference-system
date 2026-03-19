-- ================================================
-- 日程管理测试数据快速插入脚本
-- 数据库: conference_registration (port 3308)
-- 表: conf_schedule
-- ================================================

-- 说明：
-- 前端请求的会议: meetingId = 2030309010523144194
-- 用户租户: tenantId = 2027317834622709762
-- 用户ID: userId = 8

-- 设置会话变量
SET @MEETING_ID = 2030309010523144194;
SET @TENANT_ID = 2027317834622709762;
SET @CREATED_BY = 8;

-- ================================================
-- 插入 5 个示例日程
-- ================================================
INSERT INTO conf_schedule (
    id, meeting_id, tenant_id, title, start_time, end_time, 
    location, host, speaker, status, priority, created_by, created_time, deleted
) VALUES 
(
    1001,
    @MEETING_ID,
    @TENANT_ID,
    '📢 开幕式',
    DATE_ADD(CURDATE(), INTERVAL 9 HOUR),
    DATE_ADD(CURDATE(), INTERVAL 10 HOUR),
    '主会场A厅',
    '李主任',
    '张总经理',
    1,
    3,
    @CREATED_BY,
    NOW(),
    0
),
(
    1002,
    @MEETING_ID,
    @TENANT_ID,
    '💻 主题报告：数字化转型',
    DATE_ADD(CURDATE(), INTERVAL 10 HOUR),
    DATE_ADD(CURDATE(), INTERVAL 11 HOUR + 30 MINUTE),
    '主会场A厅',
    '王主持',
    '赵教授',
    1,
    2,
    @CREATED_BY,
    NOW(),
    0
),
(
    1003,
    @MEETING_ID,
    @TENANT_ID,
    '🤖 分论坛：人工智能应用',
    DATE_ADD(CURDATE(), INTERVAL 11 HOUR + 45 MINUTE),
    DATE_ADD(CURDATE(), INTERVAL 13 HOUR + 15 MINUTE),
    '分会场B厅',
    '陈主持',
    '刘博士',
    1,
    2,
    @CREATED_BY,
    NOW(),
    0
),
(
    1004,
    @MEETING_ID,
    @TENANT_ID,
    '🍽️ 午餐休息',
    DATE_ADD(CURDATE(), INTERVAL 13 HOUR + 30 MINUTE),
    DATE_ADD(CURDATE(), INTERVAL 14 HOUR + 30 MINUTE),
    '餐饮区',
    NULL,
    NULL,
    1,
    1,
    @CREATED_BY,
    NOW(),
    0
),
(
    1005,
    @MEETING_ID,
    @TENANT_ID,
    '🎉 闭幕式',
    DATE_ADD(CURDATE(), INTERVAL 14 HOUR + 30 MINUTE),
    DATE_ADD(CURDATE(), INTERVAL 15 HOUR + 30 MINUTE),
    '主会场A厅',
    '李主任',
    '李主任',
    1,
    3,
    @CREATED_BY,
    NOW(),
    0
);

-- ================================================
-- 验证插入结果
-- ================================================
SELECT '--- 插入结果统计 ---' AS message;
SELECT COUNT(*) as total_schedules FROM conf_schedule 
WHERE meeting_id = @MEETING_ID AND tenant_id = @TENANT_ID AND deleted = 0;

SELECT '--- 完整日程列表 ---' AS message;
SELECT 
    id,
    title,
    start_time,
    end_time,
    location,
    host,
    status,
    priority
FROM conf_schedule 
WHERE meeting_id = @MEETING_ID AND tenant_id = @TENANT_ID AND deleted = 0
ORDER BY start_time ASC;

-- ================================================
-- 执行此脚本后：
-- 1. 刷新浏览器中的日程管理页面
-- 2. 应该看到 5 个日程加载成功
-- 3. 检查浏览器 Console，不应该有加载错误
-- ================================================
