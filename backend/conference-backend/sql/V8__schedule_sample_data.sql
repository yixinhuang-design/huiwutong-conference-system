-- ========================================
-- 日程管理模块示例数据
-- 版本: V8
-- 说明: 为测试会议插入示例日程数据
-- ========================================

-- 使用用户当前的 tenant_id 作为测试租户
-- 会议ID: 2030309010523144194
-- 租户ID: 2027317834622709762

SET @MEETING_ID = 2030309010523144194;
SET @TENANT_ID = 2027317834622709762;
SET @CREATED_BY = 8; -- admin 用户ID

-- 1. 插入日程主数据
INSERT INTO conf_schedule (
    id, meeting_id, tenant_id, title, start_time, end_time, 
    location, host, speaker, speaker_intro, notes, status, 
    priority, sort_order, created_by, created_time, deleted
) VALUES 
(
    CONV(HEX(RAND()), 16, 10) % 9000000000000000000 + 1000000000000000000,
    @MEETING_ID,
    @TENANT_ID,
    '开幕式',
    DATE_ADD(CURDATE(), INTERVAL 9 HOUR),
    DATE_ADD(CURDATE(), INTERVAL 10 HOUR),
    '主会场A厅',
    '李主任',
    '张总经理',
    '资深专业人士，20年行业经验',
    '欢迎各位参会者',
    1,  -- 已发布
    3,  -- 特别重要
    1,
    @CREATED_BY,
    NOW(),
    0
),
(
    CONV(HEX(RAND()), 16, 10) % 9000000000000000000 + 1000000000000000000,
    @MEETING_ID,
    @TENANT_ID,
    '主题报告1：数字化转型',
    DATE_ADD(CURDATE(), INTERVAL 10 HOUR),
    DATE_ADD(CURDATE(), INTERVAL 11 HOUR + 30 MINUTE),
    '主会场A厅',
    '王主持',
    '赵教授',
    '清华大学计算机系，数字化转型专家',
    '重点学习数字化转型的最新方法论',
    1,  -- 已发布
    2,  -- 重要
    2,
    @CREATED_BY,
    NOW(),
    0
),
(
    CONV(HEX(RAND()), 16, 10) % 9000000000000000000 + 1000000000000000000,
    @MEETING_ID,
    @TENANT_ID,
    '主题报告2：人工智能应用',
    DATE_ADD(CURDATE(), INTERVAL 11 HOUR + 45 MINUTE),
    DATE_ADD(CURDATE(), INTERVAL 13 HOUR + 15 MINUTE),
    '分会场B厅',
    '陈主持',
    '刘博士',
    '业内著名AI研究者，多项专利持有人',
    '分享AI在企业中的实践案例',
    1,  -- 已发布
    2,  -- 重要
    3,
    @CREATED_BY,
    NOW(),
    0
),
(
    CONV(HEX(RAND()), 16, 10) % 9000000000000000000 + 1000000000000000000,
    @MEETING_ID,
    @TENANT_ID,
    '自由讨论与问答',
    DATE_ADD(CURDATE(), INTERVAL 13 HOUR + 30 MINUTE),
    DATE_ADD(CURDATE(), INTERVAL 14 HOUR + 30 MINUTE),
    '多功能厅',
    '主持人团队',
    '所有讲者',
    '邀请所有讲者与参会者互动',
    '欢迎现场提问和讨论',
    1,  -- 已发布
    1,  -- 正常
    4,
    @CREATED_BY,
    NOW(),
    0
),
(
    CONV(HEX(RAND()), 16, 10) % 9000000000000000000 + 1000000000000000000,
    @MEETING_ID,
    @TENANT_ID,
    '午餐休息',
    DATE_ADD(CURDATE(), INTERVAL 14 HOUR + 30 MINUTE),
    DATE_ADD(CURDATE(), INTERVAL 15 HOUR + 30 MINUTE),
    '餐饮区',
    NULL,
    NULL,
    NULL,
    '自助午餐，请在指定区域就餐',
    1,  -- 已发布
    1,  -- 正常
    5,
    @CREATED_BY,
    NOW(),
    0
),
(
    CONV(HEX(RAND()), 16, 10) % 9000000000000000000 + 1000000000000000000,
    @MEETING_ID,
    @TENANT_ID,
    '下午分论坛：创新与实践',
    DATE_ADD(CURDATE(), INTERVAL 15 HOUR + 30 MINUTE),
    DATE_ADD(CURDATE(), INTERVAL 17 HOUR),
    '分会场B厅、C厅',
    '组织委员会',
    '各领域专家',
    '多个专题分论坛',
    '参会者可选择感兴趣的分论坛参加',
    1,  -- 已发布
    2,  -- 重要
    6,
    @CREATED_BY,
    NOW(),
    0
),
(
    CONV(HEX(RAND()), 16, 10) % 9000000000000000000 + 1000000000000000000,
    @MEETING_ID,
    @TENANT_ID,
    '闭幕式',
    DATE_ADD(CURDATE(), INTERVAL 17 HOUR),
    DATE_ADD(CURDATE(), INTERVAL 18 HOUR),
    '主会场A厅',
    '李主任',
    '李主任',
    '会议组织者',
    '总结发言，颁发证书',
    1,  -- 已发布
    3,  -- 特别重要
    7,
    @CREATED_BY,
    NOW(),
    0
);

-- 2. 为已发布的日程插入设置数据
-- 获取刚插入的日程ID进行关联（这里使用简化方式，实际需要根据真实ID）
-- 注意：在生产环境中应该使用外键或触发器确保数据一致性

INSERT INTO conf_schedule_settings (
    id, schedule_id, meeting_id, tenant_id,
    need_report, report_method, report_description,
    need_checkin, checkin_method, checkin_description,
    need_reminder, reminder_target, reminder_time,
    allow_change_location, auto_broadcast,
    created_time, deleted
) 
SELECT 
    CONV(HEX(RAND()), 16, 10) % 9000000000000000000 + 1000000000000000000,
    id, meeting_id, tenant_id,
    1, 'qrcode', '请使用二维码报到',
    1, 'qrcode', '请使用二维码签到',
    1, 'all', 15,
    1, 0,
    NOW(), 0
FROM conf_schedule 
WHERE meeting_id = @MEETING_ID 
  AND tenant_id = @TENANT_ID
  AND deleted = 0
  AND id NOT IN (SELECT schedule_id FROM conf_schedule_settings WHERE deleted = 0);

-- 提交事务
COMMIT;

-- 验证插入结果
SELECT COUNT(*) as total_schedules FROM conf_schedule 
WHERE meeting_id = @MEETING_ID AND tenant_id = @TENANT_ID AND deleted = 0;

SELECT id, title, start_time, end_time, status FROM conf_schedule 
WHERE meeting_id = @MEETING_ID AND tenant_id = @TENANT_ID AND deleted = 0
ORDER BY start_time ASC;
