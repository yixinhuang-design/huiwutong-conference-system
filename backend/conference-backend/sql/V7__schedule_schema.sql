-- ========================================
-- 日程管理模块数据库迁移脚本
-- 版本: V7
-- 日期: 2026-03-11
-- 说明: 创建日程管理相关表
-- ========================================

-- 1. 创建日程(Schedule)主表
CREATE TABLE IF NOT EXISTS conf_schedule (
    id BIGINT PRIMARY KEY COMMENT '日程ID（唯一标识）',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID（多租户隔离）',
    title VARCHAR(255) NOT NULL COMMENT '日程主题',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    location VARCHAR(255) COMMENT '详细地点（如：主会场A厅、分会场B厅）',
    host VARCHAR(100) COMMENT '主持人姓名',
    speaker VARCHAR(100) COMMENT '主讲人姓名',
    speaker_intro LONGTEXT COMMENT '主讲人介绍（职称、研究方向等）',
    notes LONGTEXT COMMENT '备注说明',
    status TINYINT DEFAULT 0 COMMENT '日程状态：0-待发布, 1-已发布, 2-进行中, 3-已结束, 4-已取消',
    priority INT DEFAULT 1 COMMENT '优先级（1=正常，2=重要，3=特别重要）',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    created_by BIGINT COMMENT '创建人ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '最后修改人ID',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志（0=正常，1=删除）',
    
    -- 索引优化
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_start_time (start_time),
    KEY idx_end_time (end_time),
    KEY idx_status (status),
    KEY idx_created_time (created_time),
    UNIQUE KEY uk_meeting_schedule (meeting_id, title, start_time, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议日程主表';

-- 2. 创建日程设置表（报到、签到、提醒等配置）
CREATE TABLE IF NOT EXISTS conf_schedule_settings (
    id BIGINT PRIMARY KEY COMMENT '设置ID',
    schedule_id BIGINT NOT NULL UNIQUE COMMENT '日程ID',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    
    -- 报到配置
    need_report TINYINT DEFAULT 0 COMMENT '是否需要报到',
    report_method VARCHAR(50) COMMENT '报到方式：print（打印表单签字）、qrcode（二维码扫码）',
    report_description VARCHAR(255) COMMENT '报到说明文字',
    
    -- 签到配置
    need_checkin TINYINT DEFAULT 0 COMMENT '是否需要签到',
    checkin_method VARCHAR(50) COMMENT '签到方式：print（打印表单）、qrcode（二维码扫码）',
    checkin_description VARCHAR(255) COMMENT '签到说明文字',
    
    -- 提醒配置
    need_reminder TINYINT DEFAULT 0 COMMENT '是否需要提醒',
    reminder_target VARCHAR(50) COMMENT '提醒对象：staff（工作人员）、all（全体人员）、user（用户自主设置）',
    reminder_time INT DEFAULT 15 COMMENT '提前提醒时间（分钟）',
    reminder_methods JSON COMMENT '提醒方式数组：["app", "sms", "wechat"]',
    
    -- 其他配置
    allow_change_location TINYINT DEFAULT 1 COMMENT '是否允许更改地点',
    auto_broadcast TINYINT DEFAULT 0 COMMENT '是否自动播放日程',
    broadcast_url VARCHAR(500) COMMENT '直播地址',
    
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    
    -- 索引
    KEY idx_schedule_id (schedule_id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程设置表';

-- 3. 创建日程附件表（课件、PPT等资料）
CREATE TABLE IF NOT EXISTS conf_schedule_attachment (
    id BIGINT PRIMARY KEY COMMENT '附件ID',
    schedule_id BIGINT NOT NULL COMMENT '日程ID',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(50) COMMENT '文件类型（ppt, pptx, pdf, doc, docx等）',
    file_url VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    file_hash VARCHAR(255) COMMENT '文件哈希值（防止重复上传）',
    description VARCHAR(255) COMMENT '文件描述',
    upload_by BIGINT COMMENT '上传者ID',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    
    -- 索引
    KEY idx_schedule_id (schedule_id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id),
    UNIQUE KEY uk_schedule_file (schedule_id, file_hash, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程附件表';

-- 4. 创建日程参与人员表
CREATE TABLE IF NOT EXISTS conf_schedule_participant (
    id BIGINT PRIMARY KEY COMMENT '参与人员ID',
    schedule_id BIGINT NOT NULL COMMENT '日程ID',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    participant_id BIGINT NOT NULL COMMENT '参与人员ID',
    participant_name VARCHAR(100) COMMENT '参与人员名称',
    participant_phone VARCHAR(20) COMMENT '参与人员电话',
    participant_role VARCHAR(50) COMMENT '参与人员角色：speaker（主讲人）、host（主持人）、attendee（参会者）',
    status TINYINT DEFAULT 0 COMMENT '参与状态：0-邀请已发送, 1-已确认, 2-已拒绝, 3-已签到, 4-缺席',
    checkin_time DATETIME COMMENT '签到时间',
    checkin_location VARCHAR(255) COMMENT '签到地点',
    notes VARCHAR(255) COMMENT '备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    
    -- 索引
    KEY idx_schedule_id (schedule_id),
    KEY idx_participant_id (participant_id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_status (status),
    UNIQUE KEY uk_schedule_participant (schedule_id, participant_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程参与人员表';

-- 5. 创建日程提醒记录表
CREATE TABLE IF NOT EXISTS conf_schedule_reminder (
    id BIGINT PRIMARY KEY COMMENT '提醒记录ID',
    schedule_id BIGINT NOT NULL COMMENT '日程ID',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    target_user_id BIGINT NOT NULL COMMENT '目标用户ID',
    reminder_type VARCHAR(50) NOT NULL COMMENT '提醒类型：pre_start（开始前提醒）、start（开始提醒）、end（结束提醒）',
    reminder_channel VARCHAR(50) COMMENT '提醒渠道：app, sms, wechat',
    send_time DATETIME COMMENT '发送时间',
    status TINYINT DEFAULT 0 COMMENT '发送状态：0-待发送, 1-已发送, 2-已读, 3-失败',
    failure_reason VARCHAR(255) COMMENT '失败原因',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    
    -- 索引
    KEY idx_schedule_id (schedule_id),
    KEY idx_target_user_id (target_user_id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_send_time (send_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程提醒记录表';

-- 6. 创建日程签到记录表
CREATE TABLE IF NOT EXISTS conf_schedule_checkin (
    id BIGINT PRIMARY KEY COMMENT '签到记录ID',
    schedule_id BIGINT NOT NULL COMMENT '日程ID',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    participant_id BIGINT NOT NULL COMMENT '参与人员ID',
    checkin_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    checkin_location VARCHAR(255) COMMENT '签到地点',
    checkin_method VARCHAR(50) COMMENT '签到方式：qrcode（二维码）、nfc（NFC卡）、manual（手动签到）、app（应用签到）',
    qrcode_id VARCHAR(255) COMMENT '使用的二维码ID',
    device_info VARCHAR(500) COMMENT '签到设备信息（JSON）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    
    -- 索引
    KEY idx_schedule_id (schedule_id),
    KEY idx_participant_id (participant_id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_checkin_time (checkin_time),
    UNIQUE KEY uk_schedule_participant_checkin (schedule_id, participant_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程签到记录表';

-- 7. 创建日程日志表（审计日志）
CREATE TABLE IF NOT EXISTS conf_schedule_log (
    id BIGINT PRIMARY KEY COMMENT '日志ID',
    schedule_id BIGINT NOT NULL COMMENT '日程ID',
    meeting_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人名称',
    action_type VARCHAR(50) NOT NULL COMMENT '操作类型：create, update, delete, publish, cancel, view',
    action_detail LONGTEXT COMMENT '操作详情（JSON）',
    old_value LONGTEXT COMMENT '修改前值（JSON）',
    new_value LONGTEXT COMMENT '修改后值（JSON）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    
    -- 索引
    KEY idx_schedule_id (schedule_id),
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_operator_id (operator_id),
    KEY idx_created_time (created_time),
    KEY idx_action_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程操作日志表（审计日志）';

-- 8. 创建日程模板表（用于快速创建日程）
CREATE TABLE IF NOT EXISTS conf_schedule_template (
    id BIGINT PRIMARY KEY COMMENT '模板ID',
    meeting_id BIGINT COMMENT '会议ID（如果为NULL，则为通用模板）',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    template_name VARCHAR(255) NOT NULL COMMENT '模板名称',
    template_description VARCHAR(255) COMMENT '模板描述',
    duration_minutes INT COMMENT '日程时长（分钟）',
    location VARCHAR(255) COMMENT '默认地点',
    host VARCHAR(100) COMMENT '默认主持人',
    settings JSON COMMENT '默认设置（JSON）',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_by BIGINT COMMENT '创建人ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志',
    
    -- 索引
    KEY idx_meeting_id (meeting_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程模板表';

-- 9. 添加索引以提升查询性能
ALTER TABLE conf_schedule ADD KEY idx_meeting_status (meeting_id, status);
ALTER TABLE conf_schedule ADD KEY idx_tenant_meeting (tenant_id, meeting_id);
ALTER TABLE conf_schedule_settings ADD KEY idx_need_checkin (need_checkin);
ALTER TABLE conf_schedule_settings ADD KEY idx_need_reminder (need_reminder);

-- 提交事务
COMMIT;
