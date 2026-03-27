-- 智能排座系统 P2 优先级功能数据库初始化脚本
-- 包含: 住宿安排、用餐安排、车辆运输

-- ==================== 住宿安排表 ====================
CREATE TABLE IF NOT EXISTS conf_seating_accommodation (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    room_number VARCHAR(50) NOT NULL COMMENT '房间号',
    room_type VARCHAR(20) COMMENT '房型: SINGLE, DOUBLE, SUITE',
    capacity INT NOT NULL COMMENT '房间容量',
    assigned_count INT DEFAULT 0 COMMENT '已分配人数',
    address VARCHAR(255) COMMENT '房间地址',
    phone VARCHAR(20) COMMENT '联系电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_conference_room (conference_id, room_number),
    FOREIGN KEY (conference_id) REFERENCES conference(id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住宿安排';

-- ==================== 用餐安排表 ====================
CREATE TABLE IF NOT EXISTS conf_seating_dining (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    restaurant_name VARCHAR(100) NOT NULL COMMENT '餐厅名称',
    meal_type VARCHAR(20) COMMENT '用餐类型: BREAKFAST, LUNCH, DINNER',
    capacity INT NOT NULL COMMENT '用餐容量',
    assigned_count INT DEFAULT 0 COMMENT '已分配人数',
    location VARCHAR(255) COMMENT '用餐地址',
    meal_time VARCHAR(50) COMMENT '用餐时间',
    remarks VARCHAR(500) COMMENT '备注信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_conference_restaurant (conference_id, restaurant_name),
    FOREIGN KEY (conference_id) REFERENCES conference(id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用餐安排';

-- ==================== 车辆运输表 ====================
CREATE TABLE IF NOT EXISTS conf_seating_transport (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    license_plate VARCHAR(50) NOT NULL COMMENT '车牌号',
    vehicle_type VARCHAR(20) COMMENT '车型: BUS, CAR, VAN',
    capacity INT NOT NULL COMMENT '座位容量',
    assigned_count INT DEFAULT 0 COMMENT '已分配座位数',
    departure VARCHAR(100) COMMENT '出发地',
    destination VARCHAR(100) COMMENT '目的地',
    departure_time VARCHAR(50) COMMENT '出发时间',
    driver VARCHAR(50) COMMENT '驾驶员名称',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    remarks VARCHAR(500) COMMENT '备注信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_conference_license (conference_id, license_plate),
    FOREIGN KEY (conference_id) REFERENCES conference(id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆运输';

-- ==================== 创建索引以提高查询性能 ====================

-- 住宿安排查询索引
CREATE INDEX idx_accommodation_conference ON conf_seating_accommodation(conference_id, tenant_id);
CREATE INDEX idx_accommodation_status ON conf_seating_accommodation(conference_id, tenant_id) WHERE assigned_count < capacity;

-- 用餐安排查询索引
CREATE INDEX idx_dining_conference ON conf_seating_dining(conference_id, tenant_id);
CREATE INDEX idx_dining_status ON conf_seating_dining(conference_id, tenant_id) WHERE assigned_count < capacity;

-- 车辆运输查询索引
CREATE INDEX idx_transport_conference ON conf_seating_transport(conference_id, tenant_id);
CREATE INDEX idx_transport_status ON conf_seating_transport(conference_id, tenant_id) WHERE assigned_count < capacity;
