-- 任务依赖关系表
CREATE TABLE IF NOT EXISTS `task_dependency` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID(后置任务)',
  `predecessor_task_id` BIGINT NOT NULL COMMENT '前置任务ID',
  `dependency_type` VARCHAR(50) DEFAULT 'finish_to_start' COMMENT '依赖类型(finish_to_start/start_to_start/finish_to_finish)',
  `time_offset_minutes` INT DEFAULT 0 COMMENT '时间偏移(分钟)',
  `is_blocking` TINYINT DEFAULT 1 COMMENT '是否阻塞(1=是,0=否)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_predecessor` (`predecessor_task_id`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务依赖关系表';
