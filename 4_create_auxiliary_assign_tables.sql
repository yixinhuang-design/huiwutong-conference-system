-- ============================================
-- 辅助安排人员分配关联表 + 讨论室表
-- 创建日期：2026-03-29
-- 说明：补全辅助安排（乘车/住宿/讨论室/用餐）的人员分配记录
-- ============================================

USE conference_seating;

-- ============================================
-- 讨论室安排表（新建）
-- ============================================
CREATE TABLE IF NOT EXISTS conf_seating_discussion (
    id BIGINT PRIMARY KEY COMMENT '讨论室ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    room_name VARCHAR(100) NOT NULL COMMENT '讨论室名称',
    location VARCHAR(200) COMMENT '位置',
    capacity INT DEFAULT 15 COMMENT '容纳人数',
    assigned_count INT DEFAULT 0 COMMENT '已分配人数',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    description VARCHAR(500) COMMENT '描述/议题',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    KEY idx_conference (conference_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '讨论室安排表';

-- ============================================
-- 乘车人员分配表
-- ============================================
CREATE TABLE IF NOT EXISTS conf_seating_transport_assign (
    id BIGINT PRIMARY KEY COMMENT '分配ID',
    transport_id BIGINT NOT NULL COMMENT '车辆ID',
    attendee_id BIGINT NOT NULL COMMENT '参会人员ID',
    attendee_name VARCHAR(100) COMMENT '人员姓名（冗余）',
    department VARCHAR(100) COMMENT '部门（冗余）',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_transport_attendee (transport_id, attendee_id),
    KEY idx_transport (transport_id),
    KEY idx_attendee (attendee_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '乘车人员分配表';

-- ============================================
-- 住宿人员分配表
-- ============================================
CREATE TABLE IF NOT EXISTS conf_seating_accommodation_assign (
    id BIGINT PRIMARY KEY COMMENT '分配ID',
    accommodation_id BIGINT NOT NULL COMMENT '住宿ID',
    attendee_id BIGINT NOT NULL COMMENT '参会人员ID',
    attendee_name VARCHAR(100) COMMENT '人员姓名（冗余）',
    department VARCHAR(100) COMMENT '部门（冗余）',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_accommodation_attendee (accommodation_id, attendee_id),
    KEY idx_accommodation (accommodation_id),
    KEY idx_attendee (attendee_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '住宿人员分配表';

-- ============================================
-- 讨论室人员分配表
-- ============================================
CREATE TABLE IF NOT EXISTS conf_seating_discussion_assign (
    id BIGINT PRIMARY KEY COMMENT '分配ID',
    discussion_id BIGINT NOT NULL COMMENT '讨论室ID',
    attendee_id BIGINT NOT NULL COMMENT '参会人员ID',
    attendee_name VARCHAR(100) COMMENT '人员姓名（冗余）',
    department VARCHAR(100) COMMENT '部门（冗余）',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_discussion_attendee (discussion_id, attendee_id),
    KEY idx_discussion (discussion_id),
    KEY idx_attendee (attendee_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '讨论室人员分配表';

-- ============================================
-- 用餐人员分配表
-- ============================================
CREATE TABLE IF NOT EXISTS conf_seating_dining_assign (
    id BIGINT PRIMARY KEY COMMENT '分配ID',
    dining_id BIGINT NOT NULL COMMENT '用餐安排ID',
    attendee_id BIGINT NOT NULL COMMENT '参会人员ID',
    attendee_name VARCHAR(100) COMMENT '人员姓名（冗余）',
    department VARCHAR(100) COMMENT '部门（冗余）',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_dining_attendee (dining_id, attendee_id),
    KEY idx_dining (dining_id),
    KEY idx_attendee (attendee_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '用餐人员分配表';

-- ============================================
-- 修复住宿表：补充缺失字段
-- ============================================
ALTER TABLE conf_seating_accommodation
    ADD COLUMN IF NOT EXISTS hotel_name VARCHAR(100) DEFAULT '' COMMENT '酒店名称' AFTER tenant_id,
    ADD COLUMN IF NOT EXISTS check_in_time DATETIME COMMENT '入住时间' AFTER assigned_count,
    ADD COLUMN IF NOT EXISTS check_out_time DATETIME COMMENT '退房时间' AFTER check_in_time;

COMMIT;
