package com.conference.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.meeting.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程管理Mapper接口
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 按会议ID查询日程列表（分页）
     */
    IPage<Schedule> selectByMeetingId(Page<Schedule> page, @Param("meetingId") Long meetingId, 
                                       @Param("tenantId") Long tenantId);

    /**
     * 查询某会议的所有日程（不分页）
     */
    @Select("SELECT * FROM conf_schedule WHERE meeting_id = #{meetingId} AND tenant_id = #{tenantId} " +
            "AND deleted = 0 ORDER BY start_time ASC")
    List<Schedule> selectByMeetingIdList(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId);

    /**
     * 按状态查询日程
     */
    @Select("SELECT * FROM conf_schedule WHERE meeting_id = #{meetingId} AND tenant_id = #{tenantId} " +
            "AND status = #{status} AND deleted = 0 ORDER BY start_time ASC")
    List<Schedule> selectByStatus(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId, 
                                  @Param("status") Integer status);

    /**
     * 查询进行中的日程
     */
    @Select("SELECT * FROM conf_schedule WHERE meeting_id = #{meetingId} AND tenant_id = #{tenantId} " +
            "AND status = 2 AND start_time <= NOW() AND end_time >= NOW() AND deleted = 0")
    List<Schedule> selectOngoingSchedules(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId);

    /**
     * 查询即将开始的日程（30分钟内）
     */
    @Select("SELECT * FROM conf_schedule WHERE meeting_id = #{meetingId} AND tenant_id = #{tenantId} " +
            "AND status IN (1, 2) AND start_time > NOW() AND start_time <= DATE_ADD(NOW(), INTERVAL 30 MINUTE) " +
            "AND deleted = 0 ORDER BY start_time ASC")
    List<Schedule> selectUpcomingSchedules(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId);

    /**
     * 查询某时间范围内的日程
     */
    @Select("SELECT * FROM conf_schedule WHERE meeting_id = #{meetingId} AND tenant_id = #{tenantId} " +
            "AND start_time >= #{startTime} AND end_time <= #{endTime} AND deleted = 0 ORDER BY start_time ASC")
    List<Schedule> selectByTimeRange(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId,
                                     @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询需要签到的日程
     */
    @Select("SELECT s.* FROM conf_schedule s JOIN conf_schedule_settings ss ON s.id = ss.schedule_id " +
            "WHERE s.meeting_id = #{meetingId} AND s.tenant_id = #{tenantId} AND ss.need_checkin = 1 " +
            "AND s.deleted = 0 ORDER BY s.start_time ASC")
    List<Schedule> selectNeedCheckinSchedules(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId);

    /**
     * 查询需要提醒的日程
     */
    @Select("SELECT s.* FROM conf_schedule s JOIN conf_schedule_settings ss ON s.id = ss.schedule_id " +
            "WHERE s.meeting_id = #{meetingId} AND s.tenant_id = #{tenantId} AND ss.need_reminder = 1 " +
            "AND s.deleted = 0 ORDER BY s.start_time ASC")
    List<Schedule> selectNeedReminderSchedules(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId);

    /**
     * 统计会议日程数量
     */
    @Select("SELECT COUNT(*) FROM conf_schedule WHERE meeting_id = #{meetingId} AND tenant_id = #{tenantId} " +
            "AND deleted = 0")
    Integer countByMeetingId(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId);

    /**
     * 查询指定会议的下一个日程
     */
    @Select("SELECT * FROM conf_schedule WHERE meeting_id = #{meetingId} AND tenant_id = #{tenantId} " +
            "AND start_time > NOW() AND deleted = 0 ORDER BY start_time ASC LIMIT 1")
    Schedule selectNextSchedule(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId);

    /**
     * 查询指定会议的当前日程
     */
    @Select("SELECT * FROM conf_schedule WHERE meeting_id = #{meetingId} AND tenant_id = #{tenantId} " +
            "AND start_time <= NOW() AND end_time >= NOW() AND deleted = 0 LIMIT 1")
    Schedule selectCurrentSchedule(@Param("meetingId") Long meetingId, @Param("tenantId") Long tenantId);
}
