package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingAssignment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 座位分配 Mapper
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingAssignmentMapper extends BaseMapper<SeatingAssignment> {
    
    /**
     * 查询会议的所有座位分配
     */
    @Select("SELECT * FROM conf_seating_assignment WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingAssignment> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
    
    /**
     * 查询参会人员的座位分配
     */
    @Select("SELECT * FROM conf_seating_assignment WHERE attendee_id = #{attendeeId} AND conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    SeatingAssignment selectByAttendeeId(@Param("attendeeId") Long attendeeId, @Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
