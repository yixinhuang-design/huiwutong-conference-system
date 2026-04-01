package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingTransportAssign;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 乘车分配 Mapper
 */
public interface SeatingTransportAssignMapper extends BaseMapper<SeatingTransportAssign> {

    @Select("SELECT * FROM conf_seating_transport_assign WHERE transport_id = #{transportId} AND tenant_id = #{tenantId}")
    List<SeatingTransportAssign> selectByTransportId(@Param("transportId") Long transportId, @Param("tenantId") Long tenantId);

    @Select("SELECT * FROM conf_seating_transport_assign WHERE transport_id = #{transportId} AND attendee_id = #{attendeeId}")
    SeatingTransportAssign selectByTransportAndAttendee(@Param("transportId") Long transportId, @Param("attendeeId") Long attendeeId);

    @Select("SELECT * FROM conf_seating_transport_assign WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingTransportAssign> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
