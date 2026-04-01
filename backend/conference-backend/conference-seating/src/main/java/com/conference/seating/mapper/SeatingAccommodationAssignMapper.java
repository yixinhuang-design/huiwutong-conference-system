package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingAccommodationAssign;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住宿分配 Mapper
 */
public interface SeatingAccommodationAssignMapper extends BaseMapper<SeatingAccommodationAssign> {

    @Select("SELECT * FROM conf_seating_accommodation_assign WHERE accommodation_id = #{accommodationId} AND tenant_id = #{tenantId}")
    List<SeatingAccommodationAssign> selectByAccommodationId(@Param("accommodationId") Long accommodationId, @Param("tenantId") Long tenantId);

    @Select("SELECT * FROM conf_seating_accommodation_assign WHERE accommodation_id = #{accommodationId} AND attendee_id = #{attendeeId}")
    SeatingAccommodationAssign selectByAccommodationAndAttendee(@Param("accommodationId") Long accommodationId, @Param("attendeeId") Long attendeeId);

    @Select("SELECT * FROM conf_seating_accommodation_assign WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingAccommodationAssign> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
