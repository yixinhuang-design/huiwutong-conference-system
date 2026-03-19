-- ========================================
-- 精准通知系统 数据库初始化脚本
-- Database: conference_notification
-- ========================================

CREATE DATABASE IF NOT EXISTS conference_notification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE conference_notification;

-- 1. 通知模板表
CREATE TABLE IF NOT EXISTS conf_notification_template (
    id BIGINT PRIMARY KEY COMMENT '模板ID(雪花)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    conference_id BIGINT DEFAULT NULL COMMENT '会议ID(null=全局模板)',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    type VARCHAR(50) NOT NULL COMMENT '通知类型: conference/registration/checkin/schedule/seat/bus/accommodation/full/custom',
    title VARCHAR(255) NOT NULL COMMENT '模板标题',
    content TEXT COMMENT '模板内容(支持变量)',
    variables VARCHAR(500) DEFAULT NULL COMMENT '变量列表(逗号分隔)',
    status INT DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除: 0=正常, 1=已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_conference_id (conference_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 2. 通知记录表
CREATE TABLE IF NOT EXISTS conf_notification (
    id BIGINT PRIMARY KEY COMMENT '通知ID(雪花)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    template_id BIGINT DEFAULT NULL COMMENT '模板ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    title VARCHAR(255) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    channels VARCHAR(200) DEFAULT NULL COMMENT '发送渠道JSON',
    recipient_filter TEXT DEFAULT NULL COMMENT '受众筛选条件JSON',
    recipient_count INT DEFAULT 0 COMMENT '接收人数',
    send_timing VARCHAR(20) DEFAULT 'now' COMMENT '发送时机: now/delayed/scheduled',
    scheduled_time DATETIME DEFAULT NULL COMMENT '定时发送时间',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft/pending/sending/sent/failed',
    sent_count INT DEFAULT 0 COMMENT '已发送数',
    delivered_count INT DEFAULT 0 COMMENT '已送达数',
    read_count INT DEFAULT 0 COMMENT '已阅读数',
    failed_count INT DEFAULT 0 COMMENT '发送失败数',
    sent_time DATETIME DEFAULT NULL COMMENT '实际发送时间',
    options TEXT DEFAULT NULL COMMENT '发送选项JSON',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_conference_id (conference_id),
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表';

-- 3. 预警规则表
CREATE TABLE IF NOT EXISTS conf_alert_rule (
    id BIGINT PRIMARY KEY COMMENT '规则ID(雪花)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    metric VARCHAR(50) NOT NULL COMMENT '监控指标',
    operator VARCHAR(10) NOT NULL DEFAULT '<' COMMENT '比较运算符',
    threshold DECIMAL(10,2) NOT NULL COMMENT '阈值',
    severity VARCHAR(20) NOT NULL DEFAULT 'medium' COMMENT '级别: high/medium/low',
    notify_sms TINYINT DEFAULT 0 COMMENT '短信通知',
    notify_system TINYINT DEFAULT 1 COMMENT '系统通知',
    enabled TINYINT DEFAULT 1 COMMENT '启用状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_conference_id (conference_id),
    INDEX idx_metric (metric),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警规则表';

-- 4. 预警事件表
CREATE TABLE IF NOT EXISTS conf_alert_event (
    id BIGINT PRIMARY KEY COMMENT '事件ID(雪花)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    rule_id BIGINT DEFAULT NULL COMMENT '触发规则ID',
    metric VARCHAR(50) NOT NULL COMMENT '预警指标',
    metric_value DECIMAL(10,2) DEFAULT NULL COMMENT '触发时指标值',
    threshold DECIMAL(10,2) DEFAULT NULL COMMENT '触发时阈值',
    severity VARCHAR(20) NOT NULL DEFAULT 'medium' COMMENT '级别',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/processing/resolved',
    handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
    handler_name VARCHAR(100) DEFAULT NULL COMMENT '处理人姓名',
    handle_time DATETIME DEFAULT NULL COMMENT '开始处理时间',
    resolve_time DATETIME DEFAULT NULL COMMENT '解决时间',
    remark TEXT DEFAULT NULL COMMENT '处理备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_conference_id (conference_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警事件表';

-- ========================================
-- 插入默认通知模板（租户ID使用默认值）
-- ========================================
INSERT INTO conf_notification_template (id, tenant_id, conference_id, name, type, title, content, variables, status, create_time) VALUES
(1, 2027317834622709762, NULL, '会议通知模板', 'conference', '【{conference}】会议通知', '尊敬的{name}，您已被邀请参加"{conference}"，请及时确认参会。会议地点：{location}，时间：{schedule}。', 'name,conference,location,schedule', 1, NOW()),
(2, 2027317834622709762, NULL, '报名提醒模板', 'registration', '【{conference}】报名提醒', '尊敬的{name}，"{conference}"报名即将截止，请尽快完成报名。', 'name,conference', 1, NOW()),
(3, 2027317834622709762, NULL, '签到提醒模板', 'checkin', '【{conference}】签到提醒', '尊敬的{name}，"{conference}"即将开始，请您准时到场签到。您的座位号：{seat}。', 'name,conference,seat', 1, NOW()),
(4, 2027317834622709762, NULL, '座位安排模板', 'seat', '【{conference}】座位安排通知', '尊敬的{name}，您在"{conference}"的座位已安排：{seat}，所在分组：{group}。', 'name,conference,seat,group', 1, NOW()),
(5, 2027317834622709762, NULL, '日程提醒模板', 'schedule', '【{conference}】日程提醒', '尊敬的{name}，"{conference}"日程安排：{schedule}，请做好准备。', 'name,conference,schedule', 1, NOW()),
(6, 2027317834622709762, NULL, '车次安排模板', 'bus', '【{conference}】车次安排通知', '尊敬的{name}，您的车次安排：{bus}，请准时乘车。', 'name,conference,bus', 1, NOW()),
(7, 2027317834622709762, NULL, '住宿安排模板', 'accommodation', '【{conference}】住宿安排通知', '尊敬的{name}，您的住宿安排：{room}，请凭身份证办理入住。', 'name,conference,room', 1, NOW()),
(8, 2027317834622709762, NULL, '完整安排模板', 'full', '【{conference}】完整安排通知', '尊敬的{name}，"{conference}"安排如下：\n座位：{seat}\n分组：{group}\n车次：{bus}\n住宿：{room}\n日程：{schedule}', 'name,conference,seat,group,bus,room,schedule', 1, NOW()),
(9, 2027317834622709762, NULL, '自定义通知模板', 'custom', '自定义通知', '请在此编辑自定义通知内容...', 'name,conference', 1, NOW());

-- 插入默认预警规则
INSERT INTO conf_alert_rule (id, tenant_id, conference_id, name, metric, operator, threshold, severity, notify_sms, notify_system, enabled, create_time) VALUES
(101, 2027317834622709762, 2030309010523144194, '报名率预警', 'registrationRate', '<', 80.00, 'medium', 0, 1, 1, NOW()),
(102, 2027317834622709762, 2030309010523144194, '签到率预警', 'checkinRate', '<', 70.00, 'high', 1, 1, 1, NOW()),
(103, 2027317834622709762, 2030309010523144194, '任务完成率预警', 'taskCompletionRate', '<', 60.00, 'low', 0, 1, 1, NOW());

-- 插入模拟预警事件
INSERT INTO conf_alert_event (id, tenant_id, conference_id, rule_id, metric, metric_value, threshold, severity, status, create_time) VALUES
(201, 2027317834622709762, 2030309010523144194, 102, 'checkinRate', 65.00, 70.00, 'high', 'pending', NOW()),
(202, 2027317834622709762, 2030309010523144194, 101, 'registrationRate', 78.50, 80.00, 'medium', 'pending', NOW()),
(203, 2027317834622709762, 2030309010523144194, 103, 'taskCompletionRate', 55.00, 60.00, 'low', 'processing', NOW());

SELECT 'All tables and data initialized successfully' AS result;
