-- ============================================
-- AI智能咨询系统 数据库表
-- ============================================

-- 1. AI对话表 (会话级别)
CREATE TABLE IF NOT EXISTS ai_conversation (
    id              BIGINT          PRIMARY KEY COMMENT '雪花ID',
    tenant_id       BIGINT          NOT NULL COMMENT '租户ID',
    user_id         BIGINT          COMMENT '用户ID',
    user_name       VARCHAR(100)    COMMENT '用户名',
    conference_id   BIGINT          COMMENT '关联会议ID',
    title           VARCHAR(255)    NOT NULL DEFAULT '新对话' COMMENT '对话标题',
    status          VARCHAR(20)     NOT NULL DEFAULT 'active' COMMENT 'active/archived/deleted',
    message_count   INT             NOT NULL DEFAULT 0 COMMENT '消息条数',
    last_message    VARCHAR(500)    COMMENT '最后一条消息摘要',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_conference_id (conference_id),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话';

-- 2. AI消息表 (每条聊天消息)
CREATE TABLE IF NOT EXISTS ai_message (
    id              BIGINT          PRIMARY KEY COMMENT '雪花ID',
    tenant_id       BIGINT          NOT NULL COMMENT '租户ID',
    conversation_id BIGINT          NOT NULL COMMENT '所属对话ID',
    role            VARCHAR(20)     NOT NULL COMMENT 'user/ai/system',
    content         TEXT            NOT NULL COMMENT '消息内容(支持Markdown)',
    model           VARCHAR(50)     COMMENT 'AI模型名称',
    tokens_used     INT             COMMENT '消耗token数',
    response_time   INT             COMMENT '响应耗时(ms)',
    rating          VARCHAR(20)     COMMENT 'good/bad/null 用户评分',
    status          VARCHAR(20)     NOT NULL DEFAULT 'sent' COMMENT 'sent/error/regenerated',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息';

-- 3. AI知识库表
CREATE TABLE IF NOT EXISTS ai_knowledge (
    id              BIGINT          PRIMARY KEY COMMENT '雪花ID',
    tenant_id       BIGINT          NOT NULL COMMENT '租户ID',
    conference_id   BIGINT          COMMENT '关联会议ID',
    title           VARCHAR(255)    NOT NULL COMMENT '标题',
    summary         VARCHAR(500)    COMMENT '摘要',
    content         TEXT            COMMENT '详细内容',
    category        VARCHAR(50)     NOT NULL COMMENT '分类: 会议信息/出行指南/会务服务/报名咨询',
    icon            VARCHAR(100)    DEFAULT 'fas fa-file-alt' COMMENT '图标class',
    tags            VARCHAR(500)    COMMENT '标签(JSON数组)',
    views           INT             NOT NULL DEFAULT 0 COMMENT '查看次数',
    status          VARCHAR(20)     NOT NULL DEFAULT 'active' COMMENT 'active/disabled',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '排序',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_conference_id (conference_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库';

-- 4. AI常见问题(FAQ)表
CREATE TABLE IF NOT EXISTS ai_faq (
    id              BIGINT          PRIMARY KEY COMMENT '雪花ID',
    tenant_id       BIGINT          NOT NULL COMMENT '租户ID',
    conference_id   BIGINT          COMMENT '关联会议ID',
    question        VARCHAR(500)    NOT NULL COMMENT '问题',
    answer          TEXT            NOT NULL COMMENT '标准答案',
    category        VARCHAR(50)     NOT NULL COMMENT '分类',
    keywords        VARCHAR(500)    COMMENT '关键词(逗号分隔)',
    views           INT             NOT NULL DEFAULT 0 COMMENT '查看次数',
    rating          DECIMAL(3,1)    DEFAULT 0 COMMENT '平均评分',
    rating_count    INT             NOT NULL DEFAULT 0 COMMENT '评分次数',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '排序',
    status          VARCHAR(20)     NOT NULL DEFAULT 'active',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_conference_id (conference_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI常见问题';

-- 5. AI反馈表
CREATE TABLE IF NOT EXISTS ai_feedback (
    id              BIGINT          PRIMARY KEY COMMENT '雪花ID',
    tenant_id       BIGINT          NOT NULL COMMENT '租户ID',
    conversation_id BIGINT          COMMENT '对话ID',
    message_id      BIGINT          COMMENT '消息ID',
    user_id         BIGINT          COMMENT '用户ID',
    user_name       VARCHAR(100)    COMMENT '用户名',
    question        VARCHAR(500)    COMMENT '原始问题',
    answer          TEXT            COMMENT 'AI回答',
    feedback        VARCHAR(20)     NOT NULL COMMENT 'good/bad/neutral',
    comment         VARCHAR(500)    COMMENT '反馈详情',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_feedback (feedback),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI反馈';

-- 6. AI上下文记忆表 (用户会话上下文)
CREATE TABLE IF NOT EXISTS ai_context (
    id              BIGINT          PRIMARY KEY COMMENT '雪花ID',
    tenant_id       BIGINT          NOT NULL COMMENT '租户ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    user_name       VARCHAR(100)    COMMENT '用户名',
    conference_id   BIGINT          COMMENT '关联会议ID',
    context_data    JSON            COMMENT '上下文数据',
    turns           INT             NOT NULL DEFAULT 0 COMMENT '对话轮次',
    last_message    VARCHAR(500)    COMMENT '最后消息',
    duration        VARCHAR(50)     COMMENT '持续时长',
    last_update     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI上下文记忆';

-- 7. AI使用统计表 (按天汇总)
CREATE TABLE IF NOT EXISTS ai_usage_stats (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    tenant_id       BIGINT          NOT NULL COMMENT '租户ID',
    stat_date       DATE            NOT NULL COMMENT '统计日期',
    total_queries   INT             NOT NULL DEFAULT 0 COMMENT '总查询数',
    total_tokens    INT             NOT NULL DEFAULT 0 COMMENT '总token消耗',
    avg_response_ms INT             NOT NULL DEFAULT 0 COMMENT '平均响应时间(ms)',
    positive_count  INT             NOT NULL DEFAULT 0 COMMENT '正面反馈数',
    negative_count  INT             NOT NULL DEFAULT 0 COMMENT '负面反馈数',
    neutral_count   INT             NOT NULL DEFAULT 0 COMMENT '中性反馈数',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_date (tenant_id, stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用统计';

-- 8. AI功能配置表
CREATE TABLE IF NOT EXISTS ai_feature_config (
    id              BIGINT          PRIMARY KEY COMMENT '雪花ID',
    tenant_id       BIGINT          NOT NULL COMMENT '租户ID',
    feature_id      VARCHAR(50)     NOT NULL COMMENT '功能标识: qa/translate/summary/recommend',
    feature_name    VARCHAR(100)    NOT NULL COMMENT '功能名称',
    description     VARCHAR(255)    COMMENT '功能描述',
    icon            VARCHAR(100)    COMMENT '图标class',
    enabled         TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '0-关闭 1-开启',
    usage_count     INT             NOT NULL DEFAULT 0 COMMENT '使用次数',
    config          JSON            COMMENT '功能配置(JSON)',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_feature (tenant_id, feature_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI功能配置';
