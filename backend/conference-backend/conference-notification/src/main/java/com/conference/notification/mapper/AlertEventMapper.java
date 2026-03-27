package com.conference.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.notification.entity.AlertEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlertEventMapper extends BaseMapper<AlertEvent> {

    @Select("SELECT status, COUNT(*) as count FROM conf_alert_event " +
            "WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0 " +
            "GROUP BY status")
    List<Map<String, Object>> getStatusStats(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );

    @Select("SELECT severity, COUNT(*) as count FROM conf_alert_event " +
            "WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0 " +
            "GROUP BY severity")
    List<Map<String, Object>> getSeverityStats(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );

    /**
     * 获取报到率指标（跨库查询 conference_registration）
     */
    @Select("SELECT COUNT(*) as total, " +
            "SUM(CASE WHEN status IN (1, 4) THEN 1 ELSE 0 END) as approved, " +
            "SUM(CASE WHEN sign_in_time IS NOT NULL THEN 1 ELSE 0 END) as reported " +
            "FROM conference_registration.conf_registration " +
            "WHERE meeting_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0")
    Map<String, Object> getRegistrationMetrics(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );

    /**
     * 获取签到率指标（跨库查询 conference_registration）
     */
    @Select("SELECT " +
            "(SELECT COUNT(*) FROM conference_registration.conf_registration " +
            " WHERE meeting_id = #{conferenceId} AND tenant_id = #{tenantId} AND status IN (1, 4) AND deleted = 0) as expected, " +
            "(SELECT COUNT(DISTINCT sc.participant_id) FROM conference_registration.conf_schedule_checkin sc " +
            " INNER JOIN conference_registration.conf_schedule s ON sc.schedule_id = s.id " +
            " WHERE s.meeting_id = #{conferenceId} AND sc.tenant_id = #{tenantId} AND sc.deleted = 0) as actual")
    Map<String, Object> getCheckinMetrics(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );

    /**
     * 获取最近7天的预警趋势数据
     */
    @Select("SELECT DATE(create_time) as alert_date, COUNT(*) as count " +
            "FROM conf_alert_event " +
            "WHERE tenant_id = #{tenantId} AND deleted = 0 " +
            "AND create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY alert_date ASC")
    List<Map<String, Object>> getAlertDailyTrend(@Param("tenantId") Long tenantId);

    /**
     * 获取任务(日程)完成率指标（跨库查询 conference_registration）
     * 计算方式：有签到记录的日程数 / 总日程数 × 100
     */
    @Select("SELECT " +
            "(SELECT COUNT(*) FROM conference_registration.conf_schedule " +
            " WHERE meeting_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0) as total, " +
            "(SELECT COUNT(DISTINCT sc.schedule_id) FROM conference_registration.conf_schedule_checkin sc " +
            " INNER JOIN conference_registration.conf_schedule s ON sc.schedule_id = s.id " +
            " WHERE s.meeting_id = #{conferenceId} AND sc.tenant_id = #{tenantId} AND sc.deleted = 0) as completed")
    Map<String, Object> getTaskCompletionMetrics(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );
}
