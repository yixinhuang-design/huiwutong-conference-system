-- Fix conf_alert_event: add missing columns
ALTER TABLE conference_notification.conf_alert_event 
  ADD COLUMN IF NOT EXISTS severity VARCHAR(20) DEFAULT NULL COMMENT 'high/medium/low';

ALTER TABLE conference_notification.conf_alert_event
  ADD COLUMN IF NOT EXISTS metric VARCHAR(100) DEFAULT NULL COMMENT '指标名称';

ALTER TABLE conference_notification.conf_alert_event
  ADD COLUMN IF NOT EXISTS threshold DECIMAL(10,2) DEFAULT NULL COMMENT '阈值';

ALTER TABLE conference_notification.conf_alert_event
  MODIFY COLUMN metric_value DECIMAL(10,2) DEFAULT NULL COMMENT '指标值';

-- Fix task_info: add missing columns  
ALTER TABLE conference_collaboration.task_info
  ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT NULL COMMENT 'venue/student/custom';

ALTER TABLE conference_collaboration.task_info
  ADD COLUMN IF NOT EXISTS config TEXT DEFAULT NULL COMMENT 'JSON配置';
