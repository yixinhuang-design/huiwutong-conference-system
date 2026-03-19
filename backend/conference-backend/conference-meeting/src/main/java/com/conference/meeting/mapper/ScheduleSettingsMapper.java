package com.conference.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.meeting.entity.ScheduleSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 日程设置Mapper接口
 */
@Mapper
public interface ScheduleSettingsMapper extends BaseMapper<ScheduleSettings> {

    /**
     * 按日程ID查询设置
     */
    @Select("SELECT * FROM conf_schedule_settings WHERE schedule_id = #{scheduleId} AND deleted = 0")
    ScheduleSettings selectByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * 检查日程是否需要签到
     */
    @Select("SELECT need_checkin FROM conf_schedule_settings WHERE schedule_id = #{scheduleId} AND deleted = 0")
    Integer checkNeedCheckin(@Param("scheduleId") Long scheduleId);

    /**
     * 检查日程是否需要提醒
     */
    @Select("SELECT need_reminder FROM conf_schedule_settings WHERE schedule_id = #{scheduleId} AND deleted = 0")
    Integer checkNeedReminder(@Param("scheduleId") Long scheduleId);

    /**
     * 检查日程是否需要报到
     */
    @Select("SELECT need_report FROM conf_schedule_settings WHERE schedule_id = #{scheduleId} AND deleted = 0")
    Integer checkNeedReport(@Param("scheduleId") Long scheduleId);
}
