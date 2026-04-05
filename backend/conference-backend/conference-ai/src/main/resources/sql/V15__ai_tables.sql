-- AI知识库表
CREATE TABLE IF NOT EXISTS `ai_knowledge` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `conference_id` BIGINT COMMENT '会议ID (NULL表示全局知识)',
  `title` VARCHAR(200) NOT NULL COMMENT '知识标题',
  `summary` VARCHAR(500) COMMENT '知识摘要',
  `content` TEXT COMMENT '知识内容 (Markdown)',
  `category` VARCHAR(50) DEFAULT 'default' COMMENT '分类',
  `icon` VARCHAR(100) DEFAULT 'fas fa-file-alt' COMMENT '图标',
  `tags` VARCHAR(500) COMMENT '标签 (逗号分隔)',
  `views` INT DEFAULT 0 COMMENT '浏览次数',
  `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态 (active/inactive)',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_conference` (`tenant_id`, `conference_id`, `deleted`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库';

-- AI对话表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `conference_id` BIGINT COMMENT '会议ID (NULL表示全局对话)',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(100) COMMENT '用户姓名',
  `title` VARCHAR(200) NOT NULL COMMENT '对话标题',
  `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态 (active/archived/deleted)',
  `last_message` VARCHAR(500) COMMENT '最后一条消息',
  `message_count` INT DEFAULT 0 COMMENT '消息数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_user` (`tenant_id`, `user_id`, `deleted`),
  KEY `idx_conference` (`conference_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话';

-- AI消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色 (user/ai/system)',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `model` VARCHAR(50) COMMENT '模型名称',
  `rating` VARCHAR(20) COMMENT '用户评价 (good/bad/neutral)',
  `tokens_used` INT DEFAULT 0 COMMENT 'Token使用数',
  `response_time` INT COMMENT '响应时间(ms)',
  `status` VARCHAR(20) DEFAULT 'sent' COMMENT '消息状态 (sent/error/regenerated)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_conversation` (`conversation_id`, `deleted`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息';

-- AI反馈表
CREATE TABLE IF NOT EXISTS `ai_feedback` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `conversation_id` BIGINT COMMENT '对话ID',
  `message_id` BIGINT COMMENT '消息ID',
  `user_id` BIGINT COMMENT '用户ID',
  `user_name` VARCHAR(100) COMMENT '用户姓名',
  `question` TEXT COMMENT '用户问题',
  `answer` TEXT COMMENT 'AI回答',
  `feedback` VARCHAR(20) COMMENT '反馈类型 (good/bad/neutral)',
  `rating` VARCHAR(20) COMMENT '评价',
  `comment` TEXT COMMENT '评论内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_message` (`message_id`),
  KEY `idx_feedback` (`feedback`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI反馈';

-- AI FAQ表
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
  `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_conference` (`tenant_id`, `conference_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI常见问题';

-- AI上下文记忆表
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
  KEY `idx_tenant_user` (`tenant_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI上下文记忆';

-- AI使用统计表
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

-- AI功能配置表
CREATE TABLE IF NOT EXISTS `ai_feature_config` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `feature_id` VARCHAR(50) NOT NULL COMMENT '功能标识',
  `feature_name` VARCHAR(100) NOT NULL COMMENT '功能名称',
  `description` VARCHAR(500) COMMENT '功能描述',
  `icon` VARCHAR(100) COMMENT '图标',
  `enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
  `usage_count` INT DEFAULT 0 COMMENT '使用次数',
  `config` TEXT COMMENT '配置数据 (JSON)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_feature` (`tenant_id`, `feature_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI功能配置';

-- 插入初始知识库数据
INSERT INTO `ai_knowledge` (`id`, `tenant_id`, `title`, `summary`, `content`, `category`, `icon`, `tags`) VALUES
(1, 2027317834622709762L, '会议日程查询', '查询会议日程安排和时间', '# 会议日程查询\n\n您可以通过会议详情页面查看完整的日程安排。\n\n## 查看方式\n1. 进入会议详情页\n2. 点击"日程管理"模块\n3. 查看"会议日程"选项卡\n\n## 日程信息包含\n- 开始时间\n- 结束时间\n- 会议地点\n- 参与人员\n- 会议主题', 'schedule', 'fas fa-calendar-alt', '日程,时间,安排'),

(2, 2027317834622709762L, '座位信息查询', '查询个人座位信息和位置', '# 座位信息查询\n\n您可以通过以下方式查询您的座位信息。\n\n## 查询方式\n1. 进入会议详情页\n2. 点击"座位管理"模块\n3. 在搜索框输入您的姓名\n4. 查看您的座位位置\n\n## 座位信息包含\n- 座位号\n- 区域位置\n- 座位类型\n- 周边设施', 'seat', 'fas fa-chair', '座位,位置,排座'),

(3, 2027317834622709762L, '会议资料下载', '下载会议相关资料和文档', '# 会议资料下载\n\n会议资料可以在会议详情页的"资料管理"模块中下载。\n\n## 资料类型\n- 会议议程\n- 演讲PPT\n- 参会手册\n- 会议地图\n- 名单表\n\n## 下载方式\n1. 进入会议详情页\n2. 点击"资料管理"\n3. 选择需要的资料\n4. 点击"下载"按钮', 'materials', 'fas fa-download', '资料,下载,文档'),

(4, 2027317834622709762L, '会场位置导航', '导航到会场位置', '# 会场位置导航\n\n## 会场地址\n会议中心主会场\n详细地址：XX市XX区XX路XX号\n\n## 交通方式\n### 地铁\n- 乘坐X号线到XX站下车\n- 从A出口步行5分钟\n\n### 公交\n- 乘坐XX路到XX站下车\n- 步行3分钟到达\n\n### 自驾\n- 导航搜索"XX会议中心"\n- 地下停车场有充足车位\n\n## 入场指引\n1. 到达会议中心正门\n2. 在签到处完成签到\n3. 查看座位图找到您的位置\n4. 按指引进入会场', 'navigation', 'fas fa-map-marker-alt', '导航,位置,交通');

-- 插入初始FAQ数据
INSERT INTO `ai_knowledge` (`id`, `tenant_id`, `title`, `summary`, `content`, `category`, `icon`, `tags`) VALUES
(101, 2027317834622709762L, '如何报名参加会议？', '报名流程说明', '# 报名流程\n\n1. 打开会议报名页面\n2. 填写个人信息\n3. 选择报名类型\n4. 提交报名信息\n5. 等待审核通过\n\n您可以通过会议详情页的"报名管理"查看报名状态。', 'faq', 'fas fa-question-circle', '报名,流程,FAQ'),

(102, 2027317834622709762L, '如何修改个人信息？', '个人信息修改方法', '# 修改个人信息\n\n1. 进入会议详情页\n2. 点击"参会人员"模块\n3. 找到您的信息\n4. 点击"编辑"按钮\n5. 修改需要更新的信息\n6. 保存更改', 'faq', 'fas fa-question-circle', '个人信息,修改,FAQ'),

(103, 2027317834622709762L, '会议提供餐饮吗？', '餐饮服务说明', '# 餐饮服务\n\n本次会议提供以下餐饮服务：\n\n## 早餐\n- 时间：07:00-08:30\n- 地点：餐厅一楼\n- 形式：自助餐\n\n## 午餐\n- 时间：12:00-13:30\n- 地点：宴会厅\n- 形式：自助餐\n\n## 茶歇\n- 时间：上午10:00和下午15:00\n- 地点：会场外走廊\n- 内容：咖啡、茶、小点心\n\n请凭参会证就餐。', 'faq', 'fas fa-question-circle', '餐饮,食物,FAQ');
