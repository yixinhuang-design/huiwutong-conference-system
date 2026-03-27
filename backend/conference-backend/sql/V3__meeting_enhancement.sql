-- ========================================
-- 会议/培训系统增强迁移脚本
-- 版本: 3
-- 日期: 2026-03-06
-- 说明: 添加缺失列和新的关系表
-- ========================================

-- 1. 为 conf_meeting 表添加缺失的列
ALTER TABLE conf_meeting ADD COLUMN theme VARCHAR(255) COMMENT '会议主题' AFTER meeting_type;
ALTER TABLE conf_meeting ADD COLUMN location_latitude DECIMAL(10, 6) COMMENT '地点纬度' AFTER venue_address;
ALTER TABLE conf_meeting ADD COLUMN location_longitude DECIMAL(10, 6) COMMENT '地点经度' AFTER location_latitude;
ALTER TABLE conf_meeting ADD COLUMN cover_image_url VARCHAR(500) COMMENT '会议覆盖图片URL' AFTER location_longitude;
ALTER TABLE conf_meeting ADD COLUMN registration_config LONGTEXT COMMENT '报名配置(JSON)' AFTER max_participants;
ALTER TABLE conf_meeting ADD COLUMN checkin_config LONGTEXT COMMENT '签到配置(JSON)' AFTER registration_config;
ALTER TABLE conf_meeting ADD COLUMN notification_config LONGTEXT COMMENT '通知配置(JSON)' AFTER checkin_config;

-- 2. 创建会议人员关系表
CREATE TABLE IF NOT EXISTS conf_meeting_staff (
    id BIGINT PRIMARY KEY COMMENT '主键',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    staff_id BIGINT NOT NULL COMMENT '职工/用户ID',
    staff_phone VARCHAR(20) COMMENT '职工电话',
    staff_name VARCHAR(100) COMMENT '职工姓名',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    permissions VARCHAR(500) COMMENT '权限列表(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    UNIQUE KEY uk_meeting_staff (meeting_id, staff_id, tenant_id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_staff_id (staff_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议人员关系表';

-- 3. 创建二维码表
CREATE TABLE IF NOT EXISTS conf_qrcode (
    id BIGINT PRIMARY KEY COMMENT '主键',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    qrcode_token VARCHAR(255) NOT NULL COMMENT '二维码令牌',
    qrcode_image_url VARCHAR(500) COMMENT '二维码图片URL',
    code_type TINYINT COMMENT '二维码类型：1-报名, 2-签到',
    valid_time DATETIME COMMENT '有效期',
    used_count INT DEFAULT 0 COMMENT '使用次数',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    UNIQUE KEY uk_qrcode_token (qrcode_token, tenant_id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码表';

-- 4. 创建通知模板表
CREATE TABLE IF NOT EXISTS conf_notification_template (
    id BIGINT PRIMARY KEY COMMENT '主键',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    template_name VARCHAR(100) COMMENT '模板名称',
    template_type TINYINT COMMENT '模板类型：1-报名通知, 2-签到提醒, 3-日程通知',
    channel VARCHAR(50) COMMENT '发送渠道：app, sms, wechat',
    content LONGTEXT COMMENT '模板内容',
    variables VARCHAR(500) COMMENT '变量列表(JSON)',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 5. 在 conf_meeting 表上添加索引以提升查询性能
ALTER TABLE conf_meeting ADD KEY idx_status (status);
ALTER TABLE conf_meeting ADD KEY idx_start_time (start_time);
ALTER TABLE conf_meeting ADD KEY idx_create_time (create_time);

-- 6. 更新 conf_meeting 表的注释和字段定义
ALTER TABLE conf_meeting MODIFY COLUMN registration_config LONGTEXT COMMENT '报名配置(JSON)';
ALTER TABLE conf_meeting MODIFY COLUMN checkin_config LONGTEXT COMMENT '签到配置(JSON)';
ALTER TABLE conf_meeting MODIFY COLUMN notification_config LONGTEXT COMMENT '通知配置(JSON)';

-- 7. 为表添加更多描述性注释
ALTER TABLE conf_meeting_staff COMMENT = '会议与人员的关系表，用于存储会议的创建者、管理员等人员信息及其权限';
ALTER TABLE conf_qrcode COMMENT = '二维码存储表，用于会议报名和签到的二维码生成和验证';
ALTER TABLE conf_notification_template COMMENT = '通知模板表，存储会议的报名、签到等各类通知的模板内容';

-- 提交事务
COMMIT;
