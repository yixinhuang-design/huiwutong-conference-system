-- 报名系统数据流诊断SQL脚本
-- 2026-03-10

-- 步骤1：查看conf_registration表中的所有记录
SELECT 'Step 1: 所有报名记录' as diagnostic;
SELECT 
  id, 
  tenant_id, 
  meeting_id,
  name, 
  phone, 
  organization, 
  position,
  status,
  create_time,
  deleted
FROM conference_registration.conf_registration
ORDER BY create_time DESC
LIMIT 10;

-- 步骤2：查看是否有特定会议ID的记录
SELECT 'Step 2: 会议ID=2030309010523144200的记录' as diagnostic;
SELECT COUNT(*) as count
FROM conference_registration.conf_registration
WHERE meeting_id = 2030309010523144200
  AND deleted = 0;

-- 步骤3：查看所有不同的meeting_id值
SELECT 'Step 3: 所有不同的meeting_id值' as diagnostic;
SELECT DISTINCT meeting_id, COUNT(*) as record_count
FROM conference_registration.conf_registration
WHERE deleted = 0
GROUP BY meeting_id
ORDER BY record_count DESC;

-- 步骤4：检查是否有数据被误存到其他位置
SELECT 'Step 4: 检查conf_meeting表（会议主表）' as diagnostic;
SELECT id, meeting_name, meeting_code, status
FROM conference_registration.conf_meeting
LIMIT 10;

-- 步骤5：查询最新的报名记录详情
SELECT 'Step 5: 最新的报名记录详情' as diagnostic;
SELECT 
  id as registration_id, 
  meeting_id, 
  name, 
  phone, 
  organization, 
  position, 
  email,
  status,
  extra_info,
  create_time
FROM conference_registration.conf_registration
WHERE deleted = 0
ORDER BY create_time DESC
LIMIT 1;

-- 步骤6：按phone查询最新的报名（以防ID被修改）
SELECT 'Step 6: 按手机号查询的最新报名' as diagnostic;
SELECT 
  id, 
  meeting_id, 
  name, 
  phone, 
  create_time
FROM conference_registration.conf_registration
WHERE deleted = 0
ORDER BY create_time DESC
LIMIT 5;

-- 步骤7：检查租户ID是否匹配
SELECT 'Step 7: 检查租户ID的记录分布' as diagnostic;
SELECT 
  tenant_id, 
  COUNT(*) as record_count,
  GROUP_CONCAT(DISTINCT meeting_id) as meeting_ids
FROM conference_registration.conf_registration
WHERE deleted = 0
GROUP BY tenant_id;

-- 步骤8：查看是否有报名被标记为deleted=1
SELECT 'Step 8: 被删除的报名记录' as diagnostic;
SELECT COUNT(*) as deleted_count
FROM conference_registration.conf_registration
WHERE deleted = 1;

-- 步骤9：检查数据库中所有表
SELECT 'Step 9: conference_registration数据库中的所有表' as diagnostic;
SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'conference_registration'
ORDER BY TABLE_NAME;
