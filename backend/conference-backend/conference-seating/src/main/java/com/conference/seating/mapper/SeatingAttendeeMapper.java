package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingAttendee;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 参会人员 Mapper
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingAttendeeMapper extends BaseMapper<SeatingAttendee> {
    
    /**
     * 查询会议的所有参会人员
     */
    @Select("SELECT * FROM conf_seating_attendee WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingAttendee> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
