-- 报名信息表
CREATE TABLE registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    conference_id BIGINT NOT NULL COMMENT '会议ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    department VARCHAR(100) COMMENT '部门',
    position VARCHAR(100) COMMENT '职位',
    id_card VARCHAR(20) COMMENT '身份证号',
    id_card_photo_url TEXT COMMENT '身份证照片URL',
    diploma_photo_url TEXT COMMENT '学位证照片URL',
    registration_data JSON COMMENT '自定义报名数据',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected/checkedin',
    registration_time DATETIME NOT NULL COMMENT '报名时间',
    auditor_id BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    audit_remark TEXT COMMENT '审核备注',
    checkin_status INT DEFAULT 0 COMMENT '签到状态: 0-未签到 1-已签到',
    checkin_time DATETIME COMMENT '签到时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '软删除标记',
    
    KEY idx_tenant_id (tenant_id),
    KEY idx_conference_id (conference_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    UNIQUE KEY uk_conf_user (conference_id, user_id, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议报名表';

-- 报名审核日志表
CREATE TABLE registration_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    registration_id BIGINT NOT NULL COMMENT '报名ID',
    auditor_id BIGINT NOT NULL COMMENT '审核人ID',
    audit_result VARCHAR(20) NOT NULL COMMENT '审核结果: approved/rejected/pending',
    remark TEXT COMMENT '审核备注',
    audit_time DATETIME NOT NULL COMMENT '审核时间',
    audit_method VARCHAR(20) DEFAULT 'manual' COMMENT '审核方式: manual/auto/api',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '软删除标记',
    
    KEY idx_tenant_id (tenant_id),
    KEY idx_registration_id (registration_id),
    KEY idx_auditor_id (auditor_id),
    KEY idx_audit_time (audit_time),
    CONSTRAINT fk_audit_registration FOREIGN KEY (registration_id) REFERENCES registration(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名审核日志表';

-- 创建索引优化查询
CREATE INDEX idx_conf_status ON registration(conference_id, status);
CREATE INDEX idx_tenant_conf ON registration(tenant_id, conference_id);
CREATE INDEX idx_audit_conf ON registration_audit(tenant_id, registration_id, audit_time);
