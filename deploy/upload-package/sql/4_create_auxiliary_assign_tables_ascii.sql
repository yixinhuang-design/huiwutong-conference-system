USE conference_seating;

CREATE TABLE IF NOT EXISTS conf_seating_discussion (
    id BIGINT PRIMARY KEY,
    conference_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    room_name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    capacity INT DEFAULT 15,
    assigned_count INT DEFAULT 0,
    start_time DATETIME,
    end_time DATETIME,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_conference (conference_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conf_seating_transport_assign (
    id BIGINT PRIMARY KEY,
    transport_id BIGINT NOT NULL,
    attendee_id BIGINT NOT NULL,
    attendee_name VARCHAR(100),
    department VARCHAR(100),
    conference_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_transport_attendee (transport_id, attendee_id),
    KEY idx_transport (transport_id),
    KEY idx_attendee (attendee_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conf_seating_accommodation_assign (
    id BIGINT PRIMARY KEY,
    accommodation_id BIGINT NOT NULL,
    attendee_id BIGINT NOT NULL,
    attendee_name VARCHAR(100),
    department VARCHAR(100),
    conference_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_accommodation_attendee (accommodation_id, attendee_id),
    KEY idx_accommodation (accommodation_id),
    KEY idx_attendee (attendee_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conf_seating_discussion_assign (
    id BIGINT PRIMARY KEY,
    discussion_id BIGINT NOT NULL,
    attendee_id BIGINT NOT NULL,
    attendee_name VARCHAR(100),
    department VARCHAR(100),
    conference_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_discussion_attendee (discussion_id, attendee_id),
    KEY idx_discussion (discussion_id),
    KEY idx_attendee (attendee_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conf_seating_dining_assign (
    id BIGINT PRIMARY KEY,
    dining_id BIGINT NOT NULL,
    attendee_id BIGINT NOT NULL,
    attendee_name VARCHAR(100),
    department VARCHAR(100),
    conference_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dining_attendee (dining_id, attendee_id),
    KEY idx_dining (dining_id),
    KEY idx_attendee (attendee_id),
    KEY idx_tenant_conference (tenant_id, conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- accommodation table already has hotel_name, check_in_time, check_out_time
