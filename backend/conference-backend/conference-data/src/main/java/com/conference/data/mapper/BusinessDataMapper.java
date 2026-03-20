package com.conference.data.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 业务数据统计Mapper
 * 通过跨库查询访问conference_registration库中的业务数据
 */
@Mapper
public interface BusinessDataMapper {

    /**
     * 获取报到率数据（某会议的报名总数和各状态统计）
     * status: 0-待审核, 1-已通过, 2-已拒绝, 3-已取消, 4-已签到
     */
    @Select("SELECT " +
            "  COUNT(*) AS total, " +
            "  SUM(CASE WHEN status IN (1, 4) THEN 1 ELSE 0 END) AS approved, " +
            "  SUM(CASE WHEN sign_in_time IS NOT NULL THEN 1 ELSE 0 END) AS reported, " +
            "  SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pending " +
            "FROM conference_registration.conf_registration " +
            "WHERE meeting_id = #{conferenceId} AND deleted = 0")
    Map<String, Object> getReportRateByConference(@Param("conferenceId") Long conferenceId);

    /**
     * 获取会议列表
     */
    @Select("SELECT id, meeting_name, tenant_id, status, start_time, end_time, " +
            "  max_participants, current_participants " +
            "FROM conference_registration.conf_meeting " +
            "WHERE deleted = 0 " +
            "ORDER BY start_time DESC")
    List<Map<String, Object>> listMeetings();

    /**
     * 获取签到率数据（按日程维度）
     */
    @Select("SELECT " +
            "  s.id AS schedule_id, " +
            "  s.title AS schedule_name, " +
            "  s.start_time, " +
            "  s.end_time, " +
            "  (SELECT COUNT(*) FROM conference_registration.conf_registration " +
            "   WHERE meeting_id = s.meeting_id AND status IN (1, 4) AND deleted = 0) AS expected, " +
            "  (SELECT COUNT(*) FROM conference_registration.conf_schedule_checkin " +
            "   WHERE schedule_id = s.id AND deleted = 0) AS actual " +
            "FROM conference_registration.conf_schedule s " +
            "WHERE s.meeting_id = #{conferenceId} AND s.deleted = 0 " +
            "ORDER BY s.start_time ASC")
    List<Map<String, Object>> getCheckinRateBySchedule(@Param("conferenceId") Long conferenceId);

    /**
     * 获取全部会议的报到率总览
     */
    @Select("SELECT " +
            "  m.id AS conference_id, " +
            "  m.meeting_name, " +
            "  COUNT(r.id) AS total_registrations, " +
            "  SUM(CASE WHEN r.status IN (1, 4) THEN 1 ELSE 0 END) AS approved_count, " +
            "  SUM(CASE WHEN r.sign_in_time IS NOT NULL THEN 1 ELSE 0 END) AS reported_count " +
            "FROM conference_registration.conf_meeting m " +
            "LEFT JOIN conference_registration.conf_registration r " +
            "  ON m.id = r.meeting_id AND r.deleted = 0 " +
            "WHERE m.deleted = 0 " +
            "GROUP BY m.id, m.meeting_name " +
            "ORDER BY m.start_time DESC")
    List<Map<String, Object>> getReportRateOverview();

    /**
     * 获取指定会议的日期维度签到汇总（模拟查寝率数据结构）
     */
    @Select("SELECT " +
            "  DATE(sc.checkin_time) AS checkin_date, " +
            "  (SELECT COUNT(*) FROM conference_registration.conf_registration " +
            "   WHERE meeting_id = #{conferenceId} AND status IN (1, 4) AND deleted = 0) AS expected, " +
            "  COUNT(DISTINCT sc.participant_id) AS actual " +
            "FROM conference_registration.conf_schedule_checkin sc " +
            "INNER JOIN conference_registration.conf_schedule s ON sc.schedule_id = s.id " +
            "WHERE s.meeting_id = #{conferenceId} AND sc.deleted = 0 " +
            "GROUP BY DATE(sc.checkin_time) " +
            "ORDER BY checkin_date ASC")
    List<Map<String, Object>> getDailyCheckinSummary(@Param("conferenceId") Long conferenceId);

    /**
     * 获取指定会议的基本信息
     */
    @Select("SELECT id, meeting_name, tenant_id, status, start_time, end_time, " +
            "  max_participants, current_participants, venue_name " +
            "FROM conference_registration.conf_meeting " +
            "WHERE id = #{conferenceId} AND deleted = 0")
    Map<String, Object> getMeetingInfo(@Param("conferenceId") Long conferenceId);
}
