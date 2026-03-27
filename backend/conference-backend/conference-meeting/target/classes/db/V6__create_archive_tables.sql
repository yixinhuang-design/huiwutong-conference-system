-- ============================================================
-- 会议归档管理 - 数据库建表脚本
-- V6: 新增 conf_archive_* 系列表
-- ============================================================

-- 1. 归档资料表（课件 + 互动内容）
CREATE TABLE IF NOT EXISTS `conf_archive_material` (
  `id` BIGINT NOT NULL COMMENT '主键ID(雪花)',
  `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `category` VARCHAR(32) NOT NULL COMMENT '分类: courseware/interaction',
  `sub_category` VARCHAR(32) DEFAULT NULL COMMENT '子分类: ppt/pdf/video/photo/experience/exchange',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件URL',
  `file_size` VARCHAR(32) DEFAULT NULL COMMENT '文件大小',
  `file_type` VARCHAR(32) DEFAULT NULL COMMENT '文件类型',
  `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
  `content` TEXT DEFAULT NULL COMMENT '文本内容(心得等)',
  `author` VARCHAR(64) DEFAULT NULL COMMENT '作者',
  `upload_by` VARCHAR(64) DEFAULT NULL COMMENT '上传者',
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_meeting_category` (`meeting_id`, `category`),
  INDEX `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='归档资料表';

-- 2. 归档业务数据表（报到率/签到率/查寝率）
CREATE TABLE IF NOT EXISTS `conf_archive_business_data` (
  `id` BIGINT NOT NULL COMMENT '主键ID(雪花)',
  `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `data_type` VARCHAR(32) NOT NULL COMMENT '数据类型: report_rate/checkin_rate/dormitory_rate',
  `data_label` VARCHAR(128) NOT NULL COMMENT '数据标签(部门名/日程名/日期)',
  `data_datetime` VARCHAR(64) DEFAULT NULL COMMENT '数据日期时间',
  `expected_count` INT DEFAULT 0 COMMENT '应到人数',
  `actual_count` INT DEFAULT 0 COMMENT '实到人数',
  `rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '比率(%)',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_meeting_type` (`meeting_id`, `data_type`),
  INDEX `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='归档业务数据表';

-- 3. 归档消息群组表
CREATE TABLE IF NOT EXISTS `conf_archive_message_group` (
  `id` BIGINT NOT NULL COMMENT '主键ID(雪花)',
  `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `group_name` VARCHAR(128) NOT NULL COMMENT '群组名称',
  `message_count` INT DEFAULT 0 COMMENT '消息数量',
  `last_active_time` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_meeting` (`meeting_id`),
  INDEX `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='归档消息群组表';

-- 4. 归档消息表
CREATE TABLE IF NOT EXISTS `conf_archive_message` (
  `id` BIGINT NOT NULL COMMENT '主键ID(雪花)',
  `group_id` BIGINT NOT NULL COMMENT '群组ID',
  `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `sender` VARCHAR(64) DEFAULT NULL COMMENT '发送者',
  `send_time` DATETIME DEFAULT NULL COMMENT '发送时间',
  `message_type` VARCHAR(32) DEFAULT 'text' COMMENT '消息类型: text/image/file',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_group` (`group_id`),
  INDEX `idx_meeting` (`meeting_id`),
  INDEX `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='归档消息表';

-- 5. 归档配置表（每个会议一条）
CREATE TABLE IF NOT EXISTS `conf_archive_config` (
  `id` BIGINT NOT NULL COMMENT '主键ID(雪花)',
  `meeting_id` BIGINT NOT NULL COMMENT '会议ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `allow_student_upload` TINYINT(1) DEFAULT 1 COMMENT '是否允许学员上传',
  `is_packed` TINYINT(1) DEFAULT 0 COMMENT '是否已打包下载',
  `pack_time` DATETIME DEFAULT NULL COMMENT '打包时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_meeting` (`meeting_id`),
  INDEX `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='归档配置表';
