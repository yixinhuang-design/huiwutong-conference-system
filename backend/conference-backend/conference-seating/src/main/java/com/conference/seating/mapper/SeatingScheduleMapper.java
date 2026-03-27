package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingSchedule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日程安排 Mapper
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingScheduleMapper extends BaseMapper<SeatingSchedule> {
    
    /**
     * 查询会议的所有日程
     */
    @Select("SELECT * FROM conf_seating_schedule WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingSchedule> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
