-- =====================================================================
-- V16: AI模块DDL修复 - 补充缺失列和缺失表, 添加初始数据
-- =====================================================================

-- 1. ai_conversation 补充 user_name 和 status 列
ALTER TABLE `ai_conversation`
  ADD COLUMN IF NOT EXISTS `user_name` VARCHAR(100) COMMENT '用户姓名' AFTER `user_id`,
  ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态 (active/archived/deleted)' AFTER `title`;

-- 2. ai_message 补充 tokens_used, response_time, status 列; 重命名 tokens → tokens_used
-- 先添加新列 (如果不存在)
ALTER TABLE `ai_message`
  ADD COLUMN IF NOT EXISTS `tokens_used` INT DEFAULT 0 COMMENT 'Token使用数' AFTER `rating`,
  ADD COLUMN IF NOT EXISTS `response_time` INT COMMENT '响应时间(ms)' AFTER `tokens_used`,
  ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) DEFAULT 'sent' COMMENT '消息状态 (sent/error/regenerated)' AFTER `response_time`;

-- 迁移旧 tokens 列数据到 tokens_used (如果旧列存在)
UPDATE `ai_message` SET `tokens_used` = `tokens` WHERE `tokens_used` = 0 AND `tokens` > 0;

-- 3. ai_knowledge 补充 status 和 sort_order 列
ALTER TABLE `ai_knowledge`
  ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态 (active/inactive)' AFTER `views`,
  ADD COLUMN IF NOT EXISTS `sort_order` INT DEFAULT 0 COMMENT '排序顺序' AFTER `status`;

-- 4. ai_feedback 补充缺失字段 (实体比DDL多几个字段)
ALTER TABLE `ai_feedback`
  ADD COLUMN IF NOT EXISTS `conversation_id` BIGINT COMMENT '对话ID' AFTER `tenant_id`,
  ADD COLUMN IF NOT EXISTS `user_name` VARCHAR(100) COMMENT '用户姓名' AFTER `user_id`,
  ADD COLUMN IF NOT EXISTS `question` TEXT COMMENT '用户问题' AFTER `user_name`,
  ADD COLUMN IF NOT EXISTS `answer` TEXT COMMENT 'AI回答' AFTER `question`,
  ADD COLUMN IF NOT EXISTS `feedback` VARCHAR(20) COMMENT '反馈类型 (good/bad/neutral)' AFTER `answer`;

-- =====================================================================
-- 5. 创建缺失的4张表
-- =====================================================================

-- 5.1 AI FAQ表
CREATE TABLE IF NOT EXISTS `ai_faq` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `conference_id` BIGINT COMMENT '会议ID',
  `question` VARCHAR(500) NOT NULL COMMENT '问题',
  `answer` TEXT NOT NULL COMMENT '回答',
  `category` VARCHAR(50) DEFAULT 'general' COMMENT '分类',
  `keywords` VARCHAR(500) COMMENT '关键词 (逗号分隔)',
  `views` INT DEFAULT 0 COMMENT '查看次数',
  `rating` DECIMAL(3,2) COMMENT '评分',
  `rating_count` INT DEFAULT 0 COMMENT '评分次数',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态 (active/inactive)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_conference` (`tenant_id`, `conference_id`, `deleted`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI常见问题';

-- 5.2 AI上下文记忆表
CREATE TABLE IF NOT EXISTS `ai_context` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(100) COMMENT '用户姓名',
  `conference_id` BIGINT COMMENT '会议ID',
  `context_data` TEXT COMMENT '上下文数据 (JSON)',
  `turns` INT DEFAULT 0 COMMENT '对话轮数',
  `last_message` VARCHAR(500) COMMENT '最后一条消息',
  `duration` VARCHAR(50) COMMENT '对话时长',
  `last_update` DATETIME COMMENT '最后更新时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`, `user_id`),
  KEY `idx_conference` (`conference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI上下文记忆';

-- 5.3 AI使用统计表
CREATE TABLE IF NOT EXISTS `ai_usage_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `total_queries` INT DEFAULT 0 COMMENT '总查询数',
  `total_tokens` INT DEFAULT 0 COMMENT '总Token数',
  `avg_response_ms` INT DEFAULT 0 COMMENT '平均响应时间(ms)',
  `positive_count` INT DEFAULT 0 COMMENT '好评数',
  `negative_count` INT DEFAULT 0 COMMENT '差评数',
  `neutral_count` INT DEFAULT 0 COMMENT '中评数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_date` (`tenant_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用统计';

-- 5.4 AI功能配置表
CREATE TABLE IF NOT EXISTS `ai_feature_config` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `feature_id` VARCHAR(50) NOT NULL COMMENT '功能标识 (qa/translate/summary/recommend)',
  `feature_name` VARCHAR(100) NOT NULL COMMENT '功能名称',
  `description` VARCHAR(500) COMMENT '功能描述',
  `icon` VARCHAR(100) COMMENT '图标',
  `enabled` TINYINT DEFAULT 1 COMMENT '是否启用 (0-关闭 1-开启)',
  `usage_count` INT DEFAULT 0 COMMENT '使用次数',
  `config` TEXT COMMENT '配置数据 (JSON)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant` (`tenant_id`),
  UNIQUE KEY `uk_tenant_feature` (`tenant_id`, `feature_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI功能配置';

-- =====================================================================
-- 6. 插入 ai_feature_config 初始数据 (前端功能卡片依赖)
-- =====================================================================
INSERT INTO `ai_feature_config` (`id`, `tenant_id`, `feature_id`, `feature_name`, `description`, `icon`, `enabled`, `usage_count`, `config`) VALUES
(1, 2027317834622709762, 'qa', '智能问答', '基于AI的智能问答服务，自动理解用户问题并给出精准回答', 'fas fa-comments', 1, 0, '{"model":"qwen-turbo","maxTokens":2000}'),
(2, 2027317834622709762, 'translate', '智能翻译', '支持多语言互译，满足国际会议沟通需求', 'fas fa-language', 1, 0, '{"supportedLanguages":["zh","en","ja","ko"]}'),
(3, 2027317834622709762, 'summary', '内容摘要', '自动生成会议内容摘要，快速了解会议要点', 'fas fa-file-alt', 1, 0, '{"maxLength":500}'),
(4, 2027317834622709762, 'recommend', '智能推荐', '根据用户画像推荐相关日程、资料和联系人', 'fas fa-lightbulb', 1, 0, '{"topK":5}')
ON DUPLICATE KEY UPDATE `feature_name` = VALUES(`feature_name`);

-- =====================================================================
-- 7. 插入 ai_faq 初始数据 (FAQ功能依赖)
-- =====================================================================
INSERT INTO `ai_faq` (`id`, `tenant_id`, `question`, `answer`, `category`, `keywords`, `views`, `sort_order`, `status`) VALUES
(1, 2027317834622709762, '如何报名参加会议？', '报名流程：\n1. 打开会议报名页面\n2. 填写个人信息\n3. 选择报名类型\n4. 提交报名信息\n5. 等待审核通过\n\n您可以通过会议详情页的"报名管理"查看报名状态。', 'registration', '报名,注册,参加,registration', 0, 1, 'active'),
(2, 2027317834622709762, '如何修改个人信息？', '修改个人信息：\n1. 进入会议详情页\n2. 点击"参会人员"模块\n3. 找到您的信息\n4. 点击"编辑"按钮\n5. 修改需要更新的信息\n6. 保存更改', 'profile', '个人信息,修改,编辑,profile', 0, 2, 'active'),
(3, 2027317834622709762, '会议提供餐饮吗？', '本次会议提供以下餐饮服务：\n\n早餐：07:00-08:30，餐厅一楼，自助餐\n午餐：12:00-13:30，宴会厅，自助餐\n茶歇：上午10:00和下午15:00，会场外走廊\n\n请凭参会证就餐。', 'meal', '餐饮,食物,午餐,晚餐,早餐,meal', 0, 3, 'active'),
(4, 2027317834622709762, '如何签到？', '本次会议支持以下签到方式：\n1. 扫码签到 — 到达会场后扫描签到二维码\n2. 人脸识别 — 在入口处进行人脸识别签到\n3. 手动签到 — 在APP端"报到签到"页面手动签到\n\n请在会议开始前30分钟完成签到。', 'checkin', '签到,报到,打卡,checkin', 0, 4, 'active'),
(5, 2027317834622709762, '如何查看座位信息？', '查看座位信息：\n1. 进入会议详情页\n2. 点击"座位管理"模块\n3. 在搜索框输入您的姓名\n4. 查看您的座位位置\n\n座位信息包含：座位号、区域位置、座位类型。', 'seat', '座位,位置,排座,seat', 0, 5, 'active')
ON DUPLICATE KEY UPDATE `question` = VALUES(`question`);

-- 更新已有 ai_knowledge 记录的 status 字段
UPDATE `ai_knowledge` SET `status` = 'active' WHERE `status` IS NULL;
