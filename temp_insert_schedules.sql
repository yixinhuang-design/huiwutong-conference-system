SET @MEETING_ID = 2030309010523144194;
SET @TENANT_ID = 2027317834622709762;
SET @CREATED_BY = 8;

INSERT INTO conf_schedule (
    id, meeting_id, tenant_id, title, start_time, end_time, 
    location, host, speaker, status, priority, created_by, created_time, deleted
) VALUES 
(1001, @MEETING_ID, @TENANT_ID, '开幕式',
 DATE_ADD(CURDATE(), INTERVAL 9 HOUR),
 DATE_ADD(CURDATE(), INTERVAL 10 HOUR),
 '主会场A厅', '李主任', '张总经理', 1, 3, @CREATED_BY, NOW(), 0),
(1002, @MEETING_ID, @TENANT_ID, '主题报告：数字化转型',
 DATE_ADD(CURDATE(), INTERVAL 10 HOUR),
 DATE_ADD(CURDATE(), INTERVAL 11 HOUR + 30 MINUTE),
 '主会场A厅', '王主持', '赵教授', 1, 2, @CREATED_BY, NOW(), 0),
(1003, @MEETING_ID, @TENANT_ID, '分论坛：人工智能应用',
 DATE_ADD(CURDATE(), INTERVAL 11 HOUR + 45 MINUTE),
 DATE_ADD(CURDATE(), INTERVAL 13 HOUR + 15 MINUTE),
 '分会场B厅', '陈主持', '刘博士', 1, 2, @CREATED_BY, NOW(), 0),
(1004, @MEETING_ID, @TENANT_ID, '午餐休息',
 DATE_ADD(CURDATE(), INTERVAL 13 HOUR + 30 MINUTE),
 DATE_ADD(CURDATE(), INTERVAL 14 HOUR + 30 MINUTE),
 '餐饮区', NULL, NULL, 1, 1, @CREATED_BY, NOW(), 0),
(1005, @MEETING_ID, @TENANT_ID, '闭幕式',
 DATE_ADD(CURDATE(), INTERVAL 14 HOUR + 30 MINUTE),
 DATE_ADD(CURDATE(), INTERVAL 15 HOUR + 30 MINUTE),
 '主会场A厅', '李主任', '李主任', 1, 3, @CREATED_BY, NOW(), 0);

SELECT COUNT(*) as total_schedules FROM conf_schedule 
WHERE meeting_id = @MEETING_ID AND tenant_id = @TENANT_ID AND deleted = 0;

SELECT '--- 完整日程列表 ---' AS message;
SELECT id, title, start_time, end_time FROM conf_schedule 
WHERE meeting_id = @MEETING_ID AND tenant_id = @TENANT_ID AND deleted = 0
ORDER BY start_time ASC;
