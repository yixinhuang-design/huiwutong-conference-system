-- 步骤3：创建优化索引
USE conference_seating;

-- 会场查询速度优化
ALTER TABLE conf_seating_venue ADD UNIQUE KEY uk_tenant_conf (tenant_id, conference_id);

-- 座位查询速度优化
ALTER TABLE conf_seating_seat ADD KEY idx_venue_status (venue_id, status);
ALTER TABLE conf_seating_seat ADD KEY idx_seat_type_status (seat_type, status);

-- 参会人员查询速度优化
ALTER TABLE conf_seating_attendee ADD KEY idx_department_position (department, position);
ALTER TABLE conf_seating_attendee ADD KEY idx_is_vip_reserved (is_vip, is_reserved);

-- 分配记录查询速度优化
ALTER TABLE conf_seating_assignment ADD KEY idx_attendee_schedule (attendee_id, schedule_id);
ALTER TABLE conf_seating_assignment ADD KEY idx_assign_type (assign_type);

SELECT '✅ 优化索引已创建' as '完成状态';
SELECT '✅ 排座系统数据库建表完全成功！' as '最终状态';
