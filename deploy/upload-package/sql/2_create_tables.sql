-- 步骤2：创建所有表
USE conference_seating;

-- 表1：会场配置表
CREATE TABLE IF NOT EXISTS conf_seating_venue (
    id BIGINT PRIMARY KEY COMMENT '会场ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    venue_name VARCHAR(100) NOT NULL COMMENT '会场名称',
    venue_type VARCHAR(50) COMMENT '会场类型：normal/ushape/round/theater',
    total_rows INT NOT NULL DEFAULT 10 COMMENT '总行数',
    total_columns INT NOT NULL DEFAULT 20 COMMENT '总列数',
    capacity INT COMMENT '会场容纳人数',
    layout_data JSON COMMENT '布局配置JSON(包含过道位置等)',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active/inactive',
    created_by BIGINT COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_conf_venue (conference_id, venue_name),
    KEY idx_tenant_conference (tenant_id, conference_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会场配置表';

-- 表2：座位表
CREATE TABLE IF NOT EXISTS conf_seating_seat (
    id BIGINT PRIMARY KEY COMMENT '座位ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    venue_id BIGINT NOT NULL COMMENT '会场ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    seat_number VARCHAR(50) NOT NULL COMMENT '座位编号(如A01, B15等)',
    row_num INT NOT NULL COMMENT '行号',
    column_num INT NOT NULL COMMENT '列号',
    seat_type VARCHAR(20) DEFAULT 'normal' COMMENT '座位类型：normal/vip/reserved/aisle',
    status VARCHAR(20) DEFAULT 'available' COMMENT '状态：available/assigned/reserved/aisle',
    assigned_user_id BIGINT COMMENT '分配人员ID',
    assigned_user_name VARCHAR(100) COMMENT '分配人员名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_seat_number (venue_id, seat_number),
    KEY idx_conference_venue (conference_id, venue_id),
    KEY idx_tenant_conference (tenant_id, conference_id),
    KEY idx_assigned_user (assigned_user_id),
    KEY idx_status (status),
    KEY idx_seat_type (seat_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- 表3：参会人员表
CREATE TABLE IF NOT EXISTS conf_seating_attendee (
    id BIGINT PRIMARY KEY COMMENT '人员ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    user_id BIGINT COMMENT '用户系统ID',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    department VARCHAR(100) COMMENT '部门',
    position VARCHAR(100) COMMENT '职位',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    is_vip TINYINT DEFAULT 0 COMMENT '是否VIP',
    is_reserved TINYINT DEFAULT 0 COMMENT '是否保留座位',
    assigned_seat_id BIGINT COMMENT '分配座位ID',
    assigned_at TIMESTAMP NULL COMMENT '分配时间',
    confirmed TINYINT DEFAULT 0 COMMENT '是否确认',
    attendance_status VARCHAR(20) DEFAULT 'pending' COMMENT '参加状态：pending/confirmed/absent',
    extra_data JSON COMMENT '扩展数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    KEY idx_conference_attendee (conference_id, id),
    KEY idx_tenant_conference (tenant_id, conference_id),
    KEY idx_user_id (user_id),
    KEY idx_assigned_seat (assigned_seat_id),
    KEY idx_is_vip (is_vip),
    KEY idx_department (department),
    FOREIGN KEY fk_seat (assigned_seat_id) REFERENCES conf_seating_seat(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参会人员表';

-- 表4：座位分配记录表
CREATE TABLE IF NOT EXISTS conf_seating_assignment (
    id BIGINT PRIMARY KEY COMMENT '分配记录ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    attendee_id BIGINT NOT NULL COMMENT '参会人员ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    schedule_id BIGINT COMMENT '日程ID（如有多日程）',
    assign_type VARCHAR(20) COMMENT '分配类型：auto(自动)/manual(手动)/swap(交换)',
    algorithm_name VARCHAR(100) COMMENT '使用的算法名称(自动分配时)',
    assign_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    assigned_by BIGINT COMMENT '分配人ID',
    status VARCHAR(20) DEFAULT 'assigned' COMMENT '状态：assigned/confirmed/cancelled',
    confirm_time TIMESTAMP NULL COMMENT '确认时间',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    KEY idx_conference_assignment (conference_id, id),
    KEY idx_tenant_conference (tenant_id, conference_id),
    KEY idx_seat_attendee (seat_id, attendee_id),
    KEY idx_assign_time (assign_time),
    KEY idx_status (status),
    FOREIGN KEY fk_seat (seat_id) REFERENCES conf_seating_seat(id) ON DELETE CASCADE,
    FOREIGN KEY fk_attendee (attendee_id) REFERENCES conf_seating_attendee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位分配记录表';

-- 表5：日程表
CREATE TABLE IF NOT EXISTS conf_seating_schedule (
    id BIGINT PRIMARY KEY COMMENT '日程ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    schedule_name VARCHAR(100) NOT NULL COMMENT '日程名称',
    schedule_date DATE COMMENT '日期',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    description VARCHAR(500) COMMENT '描述',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认日程',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    KEY idx_conference_schedule (conference_id, id),
    KEY idx_tenant_conference (tenant_id, conference_id),
    KEY idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程表';

-- 表6：车辆安排表
CREATE TABLE IF NOT EXISTS conf_seating_transport (
    id BIGINT PRIMARY KEY COMMENT '车辆ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    vehicle_name VARCHAR(100) NOT NULL COMMENT '车辆名称',
    vehicle_type VARCHAR(50) NOT NULL COMMENT '车辆类型：小车/中巴/大巴',
    plate_number VARCHAR(50) COMMENT '车牌号',
    departure VARCHAR(100) COMMENT '出发地',
    destination VARCHAR(100) COMMENT '目的地',
    departure_time DATETIME COMMENT '出发时间',
    capacity INT DEFAULT 45 COMMENT '容纳人数',
    assigned_count INT DEFAULT 0 COMMENT '已分配人数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    KEY idx_conference (conference_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆安排表';

-- 表7：住宿安排表
CREATE TABLE IF NOT EXISTS conf_seating_accommodation (
    id BIGINT PRIMARY KEY COMMENT '住宿ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    hotel_name VARCHAR(100) NOT NULL COMMENT '酒店名称',
    room_number VARCHAR(50) NOT NULL COMMENT '房间号',
    room_type VARCHAR(50) COMMENT '房间类型：标准间/豪华间/套房',
    capacity INT DEFAULT 2 COMMENT '房间容纳人数',
    assigned_count INT DEFAULT 0 COMMENT '已分配人数',
    check_in_time DATETIME COMMENT '入住时间',
    check_out_time DATETIME COMMENT '退房时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    KEY idx_conference (conference_id),
    KEY idx_tenant_conference (tenant_id, conference_id),
    UNIQUE KEY uk_room (hotel_name, room_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='住宿安排表';

-- 表8：用餐安排表
CREATE TABLE IF NOT EXISTS conf_seating_dining (
    id BIGINT PRIMARY KEY COMMENT '用餐ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    dining_name VARCHAR(100) NOT NULL COMMENT '用餐安排名称',
    dining_type VARCHAR(50) COMMENT '用餐类型：早餐/午餐/晚餐/茶歇',
    dining_date DATE COMMENT '用餐日期',
    dining_time TIME COMMENT '用餐时间',
    location VARCHAR(100) COMMENT '用餐地点',
    capacity INT COMMENT '容纳人数',
    assigned_count INT DEFAULT 0 COMMENT '已分配人数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    KEY idx_conference (conference_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用餐安排表';

-- 表9：座位分配历史表
CREATE TABLE IF NOT EXISTS conf_seating_assignment_history (
    id BIGINT PRIMARY KEY COMMENT '历史记录ID',
    assignment_id BIGINT NOT NULL COMMENT '分配记录ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    seat_id BIGINT COMMENT '座位ID',
    attendee_id BIGINT COMMENT '参会人员ID',
    old_seat_id BIGINT COMMENT '旧座位ID',
    operation VARCHAR(50) COMMENT '操作类型：assign/cancel/swap',
    operator_id BIGINT COMMENT '操作人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    KEY idx_conference (conference_id),
    KEY idx_assignment (assignment_id),
    KEY idx_attendee (attendee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位分配历史表';

SELECT '✅ 9张表已创建' as '完成状态';
