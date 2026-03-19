-- ============================================
-- 报名租户ID不一致问题诊断SQL脚本
-- ============================================
-- 用途：诊断报名提交后找不到报名信息的问题
-- 执行环境：MySQL 数据库 conference_registration
-- 使用者：系统管理员/技术人员
-- ============================================

-- ============================================
-- 第1步：系统基本信息检查
-- ============================================
-- 检查默认租户是否存在及其信息
SELECT '【租户信息检查】' as step;
SELECT id, name, status, created_time 
FROM conf_tenant 
WHERE id = '2027317834622709762'
LIMIT 1;

-- ============================================
-- 第2步：会议基本信息检查
-- ============================================
-- 检查会议ID 2030309010523144200 是否存在
SELECT '【会议信息检查】' as step;
SELECT id, title, conference_date, status, tenant_id 
FROM conf_meeting 
WHERE id = 2030309010523144200 
LIMIT 1;

-- ============================================
-- 第3步：该会议的报名记录总数
-- ============================================
SELECT '【报名记录总数】' as step;
SELECT 
  COUNT(*) as 总数,
  COUNT(CASE WHEN tenant_id = '2027317834622709762' THEN 1 END) as '租户=2027317834622709762的数量'
FROM conf_registration 
WHERE meeting_id = 2030309010523144200
  AND deleted = 0;

-- ============================================
-- 第4步：检查该会议的所有报名记录及其租户ID分布
-- ============================================
-- 这是诊断的关键！如果这里能查到数据，说明数据存在
SELECT '【该会议所有报名记录（按租户分组）】' as step;
SELECT 
  tenant_id,
  COUNT(*) as 报名数,
  GROUP_CONCAT(DISTINCT phone) as 手机号列表
FROM conf_registration 
WHERE meeting_id = 2030309010523144200
  AND deleted = 0
GROUP BY tenant_id;

-- ============================================
-- 第5步：检查具体手机号的报名记录
-- ============================================
-- 如果您知道提交报名时用的手机号，使用这个查询
-- 请将 '13800000000' 替换为实际的手机号
SELECT '【特定手机号的报名记录】' as step;
SELECT 
  id,
  meeting_id,
  phone,
  real_name,
  tenant_id,
  status,
  registration_time,
  created_time
FROM conf_registration 
WHERE meeting_id = 2030309010523144200
  AND phone = '13800000000'  -- 请替换为实际的手机号
  AND deleted = 0;

-- ============================================
-- 第6步：检查最近提交的报名记录（最后10条）
-- ============================================
-- 按提交时间倒序查看，判断是否有新增记录
SELECT '【最近提交的报名记录（前10条）】' as step;
SELECT 
  id,
  meeting_id,
  phone,
  real_name,
  tenant_id,
  status,
  registration_time,
  created_time
FROM conf_registration 
WHERE meeting_id = 2030309010523144200
  AND deleted = 0
ORDER BY registration_time DESC
LIMIT 10;

-- ============================================
-- 第7步：检查所有已删除的报名记录
-- ============================================
-- 某些报名可能被标记为已删除（deleted=1）
SELECT '【已删除的报名记录】' as step;
SELECT 
  id,
  phone,
  real_name,
  tenant_id,
  registration_time,
  created_time
FROM conf_registration 
WHERE meeting_id = 2030309010523144200
  AND deleted = 1
LIMIT 10;

-- ============================================
-- 第8步：检查不同租户下的报名数量
-- ============================================
-- 查看整个系统中有多少个租户有报名记录
SELECT '【所有租户的报名数量统计】' as step;
SELECT 
  tenant_id,
  COUNT(*) as 报名数
FROM conf_registration 
WHERE meeting_id = 2030309010523144200
  AND deleted = 0
GROUP BY tenant_id
ORDER BY 报名数 DESC;

-- ============================================
-- 第9步：检查会议ID的类型和值
-- ============================================
-- 验证会议ID是否为 BIGINT 类型，有无精度丢失
SELECT '【会议ID数据类型和范围检查】' as step;
SELECT 
  MIN(meeting_id) as 最小ID,
  MAX(meeting_id) as 最大ID,
  COUNT(*) as 记录数
FROM conf_registration
WHERE meeting_id = 2030309010523144200;

-- ============================================
-- 第10步：检查报名表结构
-- ============================================
-- 确保表结构正确，有 tenant_id 和 meeting_id 字段
SELECT '【报名表结构信息】' as step;
SHOW COLUMNS FROM conf_registration 
WHERE Field IN ('id', 'meeting_id', 'phone', 'real_name', 'tenant_id', 'status', 'deleted', 'registration_time');

-- ============================================
-- 第11步：检查报名表索引
-- ============================================
-- 确保有有效的索引加快查询
SELECT '【报名表索引信息】' as step;
SHOW INDEX FROM conf_registration 
WHERE Column_name IN ('meeting_id', 'tenant_id', 'phone');

-- ============================================
-- 第12步：租户ID不一致的数据检查
-- ============================================
-- 如果您在 mysql 中执行以下查询，确认是否存在数据不一致的情况
SELECT '【租户ID的所有可能值】' as step;
SELECT DISTINCT tenant_id, COUNT(*) as 数量
FROM conf_registration 
WHERE meeting_id = 2030309010523144200
  AND deleted = 0
GROUP BY tenant_id;

-- ============================================
-- 诊断要点说明
-- ============================================
/*
执行上述SQL后，根据结果判断问题：

【场景 A】如果第3步能查到数据：
  → 说明报名数据已被保存到数据库
  → 问题可能是：
    1. 数据保存在了不同的租户ID下（查看第4、8步结果）
    2. 前端查询时使用了错误的租户ID
    3. 后端查询逻辑有问题

  → 解决方案：
    1. 查看第4、8步的结果，确认数据实际保存在哪个租户ID下
    2. 如果数据在不同的租户ID下，需要修复前端的租户ID获取逻辑
    3. 更新 scan-register.vue 中的租户ID优先级设置

【场景 B】如果第3步查不到数据（总数为0）：
  → 说明报名数据从未被保存
  → 问题可能是：
    1. 提交请求失败了，但前端显示了成功消息
    2. 后端验证失败，数据被拒绝
    3. 数据库连接或事务问题
    4. 数据库字段转换/精度问题

  → 解决方案：
    1. 检查后端日志，查看是否有异常信息
    2. 查看第5、6步结果，检查是否有相关手机号的记录
    3. 检查后端是否正确处理了报名提交请求

【场景 C】如果第5步能查到数据但租户ID不同：
  → 说明数据被保存到了不同的租户ID
  → 这是租户ID不一致的确凿证据

  → 解决方案：
    1. 这就是修复目标的问题所在！
    2. 应用代码修复（scan-register.vue 租户ID优先级调整）
    3. 重新提交报名进行验证

【场景 D】如果第10或11步显示表结构或索引有问题：
  → 可能是数据库初始化不完全
  → 需要检查数据库初始化SQL脚本

*/

-- ============================================
-- 快速诊断总结查询
-- ============================================
-- 这个查询可以快速看到核心的诊断信息
SELECT 
  '会议ID' as 诊断项,
  2030309010523144200 as 值
UNION ALL
SELECT '总报名数', COUNT(*) FROM conf_registration WHERE meeting_id = 2030309010523144200 AND deleted = 0
UNION ALL
SELECT '默认租户报名数', COUNT(*) FROM conf_registration WHERE meeting_id = 2030309010523144200 AND tenant_id = '2027317834622709762' AND deleted = 0
UNION ALL
SELECT '不同租户数', (SELECT COUNT(DISTINCT tenant_id) FROM conf_registration WHERE meeting_id = 2030309010523144200 AND deleted = 0)
UNION ALL
SELECT '表存在', IF(EXISTS(SELECT 1 FROM information_schema.TABLES WHERE TABLE_NAME = 'conf_registration' AND TABLE_SCHEMA = DATABASE()), '是', '否');

-- ============================================
-- 如果要恢复被意外删除的数据
-- ============================================
/*
如果数据被标记为删除（deleted=1），可以恢复：

-- 查看已删除的报名
SELECT * FROM conf_registration 
WHERE meeting_id = 2030309010523144200 
  AND deleted = 1;

-- 恢复已删除的报名（需要谨慎操作）
-- UPDATE conf_registration 
-- SET deleted = 0 
-- WHERE meeting_id = 2030309010523144200 
--   AND deleted = 1
--   AND phone = '13800000000';  -- 指定具体手机号

*/

-- ============================================
-- 脚本结束
-- ============================================
