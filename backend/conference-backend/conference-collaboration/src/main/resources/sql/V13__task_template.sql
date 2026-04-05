-- 任务模板表
CREATE TABLE IF NOT EXISTS `task_template` (
  `id` BIGINT NOT NULL COMMENT '主键ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `task_type` VARCHAR(50) COMMENT '任务类型',
  `category` VARCHAR(50) COMMENT '任务类别(venue/student/custom)',
  `completion_method` VARCHAR(50) COMMENT '完成方式(text_image/location/questionnaire/collection)',
  `description` VARCHAR(500) COMMENT '任务描述',
  `priority` VARCHAR(20) DEFAULT 'medium' COMMENT '优先级(low/medium/high/urgent)',
  `config` TEXT COMMENT '完成配置(JSON格式)',
  `icon` VARCHAR(100) COMMENT '模板图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `is_system` TINYINT DEFAULT 0 COMMENT '是否系统模板(1=是,0=否)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0=正常,1=删除)',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_category` (`tenant_id`, `category`, `deleted`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务模板表';

-- 插入系统预设模板
INSERT INTO `task_template` (`id`, `tenant_id`, `template_name`, `task_type`, `category`, `completion_method`, `description`, `priority`, `config`, `icon`, `sort_order`, `is_system`) VALUES
-- 会场任务模板
(1, 2027317834622709762, '会场设备检查', 'venue_check', 'venue', 'text_image', '检查会场设备是否正常运行，填写检查结果并上传照片', 'high', '{"requireText":true,"minTextLength":20,"requireImage":true,"minImageCount":3}', 'fa-clipboard-check', 1, 1),

(2, 2027317834622709762, '学员接待引导', 'reception', 'venue', 'location', '在指定地点接待学员，引导学员办理报到手续', 'high', '{"locationName":"主会场入口","latitude":30.2741,"longitude":120.1551,"locationRadius":100,"requirePhoto":true}', 'fa-handshake', 2, 1),

-- 学员任务模板
(3, 2027317834622709762, '每日签到', 'checkin', 'student', 'location', '每日定时签到，确认学员在会场', 'medium', '{"locationName":"会议中心","latitude":30.2741,"longitude":120.1551,"locationRadius":200,"requirePhoto":false}', 'fa-clock', 3, 1),

(4, 2027317834622709762, '晚间查寝', 'room_check', 'student', 'text_image', '检查学员住宿情况，确保安全', 'high', '{"requireText":true,"minTextLength":10,"requireImage":true,"minImageCount":1}', 'fa-bed', 4, 1),

(5, 2027317834622709762, '课程评价', 'evaluation', 'student', 'questionnaire', '对今日课程内容、讲师授课进行评价', 'medium', '{"questions":[{"title":"课程内容满意度","type":"rating"},{"title":"讲师授课水平","type":"rating"},{"title":"建议与意见","type":"text"}]}', 'fa-star', 5, 1),

(6, 2027317834622709762, '用餐意向征集', 'collection', 'student', 'collection', '征集学员用餐意向（素食/特殊需求等）', 'low', '{"fields":[{"label":"用餐类型","type":"radio","options":"自助餐\\n围餐\\n素食","required":true},{"label":"特殊需求","type":"textarea","required":false}]}', 'fa-utensils', 6, 1),

-- 其他任务模板
(7, 2027317834622709762, '自定义任务', 'custom', 'custom', 'text_image', '创建自定义任务内容', 'medium', '{}', 'fa-tasks', 99, 1);
