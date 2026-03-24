-- =====================================================================
-- 会议/培训管理系统 - 数据库初始化脚本
-- 版本: 1.0.0
-- 描述: 创建会议管理核心表和关联表
-- 时间: 2026-03-06
-- =====================================================================

-- =====================================================================
-- 1. 会议/培训主表增强
-- =====================================================================

ALTER TABLE conf_meeting
ADD COLUMN theme VARCHAR(255) COMMENT '会议主题/类别',
ADD COLUMN location_latitude DECIMAL(10,6) COMMENT '会议地点纬度',
ADD COLUMN location_longitude DECIMAL(10,6) COMMENT '会议地点经度',
ADD COLUMN cover_image_url VARCHAR(512) COMMENT '会议宣传图URL',
ADD COLUMN registration_config JSON COMMENT '报名配置信息(JSON格式)',
ADD COLUMN checkin_config JSON COMMENT '签到配置信息(JSON格式)',
ADD COLUMN notification_config JSON COMMENT '通知配置信息(JSON格式)';

-- =====================================================================
-- 2. 会议工作人员表
-- =====================================================================

CREATE TABLE IF NOT EXISTS conf_meeting_staff (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  meeting_id BIGINT NOT NULL COMMENT '会议ID',
  staff_id BIGINT NOT NULL COMMENT '工作人员ID',
  staff_name VARCHAR(100) COMMENT '工作人员名称',
  staff_phone VARCHAR(20) COMMENT '工作人员电话',
  role INT DEFAULT 0 COMMENT '角色(0=普通员工, 1=组长, 2=领队)',
  permissions JSON COMMENT '权限配置(JSON格式)',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  deleted INT DEFAULT 0 COMMENT '是否删除(0=否, 1=是)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_meeting_staff (meeting_id, staff_id, tenant_id),
  KEY idx_meeting_id (meeting_id),
  KEY idx_staff_id (staff_id),
  KEY idx_tenant_id (tenant_id),
  KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议工作人员表';

-- =====================================================================
-- 3. 会议二维码表
-- =====================================================================

CREATE TABLE IF NOT EXISTS conf_qrcode (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  meeting_id BIGINT NOT NULL COMMENT '会议ID',
  qr_type INT DEFAULT 0 COMMENT '二维码类型(0=签到, 1=报名, 2=会议信息)',
  qr_code VARCHAR(255) COMMENT '二维码内容',
  qr_url VARCHAR(512) COMMENT '二维码图片URL',
  qr_text VARCHAR(100) COMMENT '二维码文字描述',
  valid_start_time DATETIME COMMENT '有效期开始',
  valid_end_time DATETIME COMMENT '有效期结束',
  is_active INT DEFAULT 1 COMMENT '是否激活(0=否, 1=是)',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  deleted INT DEFAULT 0 COMMENT '是否删除(0=否, 1=是)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_meeting_type (meeting_id, qr_type, tenant_id),
  KEY idx_meeting_id (meeting_id),
  KEY idx_qr_type (qr_type),
  KEY idx_tenant_id (tenant_id),
  KEY idx_deleted (deleted),
  KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议二维码表';

-- =====================================================================
-- 4. 通知模板表
-- =====================================================================

CREATE TABLE IF NOT EXISTS conf_notification_template (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  meeting_id BIGINT COMMENT '会议ID(为NULL时为全局模板)',
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  template_type INT DEFAULT 0 COMMENT '模板类型(0=短信, 1=邮件, 2=推送)',
  template_content LONGTEXT COMMENT '模板内容',
  variables JSON COMMENT '变量列表(JSON格式)',
  is_default INT DEFAULT 0 COMMENT '是否为默认模板(0=否, 1=是)',
  send_time VARCHAR(20) COMMENT '发送时间(如: registration_start)',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  deleted INT DEFAULT 0 COMMENT '是否删除(0=否, 1=是)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_meeting_id (meeting_id),
  KEY idx_template_type (template_type),
  KEY idx_tenant_id (tenant_id),
  KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- =====================================================================
-- 5. 创建索引优化查询性能
-- =====================================================================

ALTER TABLE conf_meeting
ADD INDEX idx_tenant_status (tenant_id, status, deleted),
ADD INDEX idx_tenant_code (tenant_id, meeting_code, deleted),
ADD INDEX idx_start_end_time (start_time, end_time);

-- =====================================================================
-- 脚本执行完毕
-- =====================================================================
