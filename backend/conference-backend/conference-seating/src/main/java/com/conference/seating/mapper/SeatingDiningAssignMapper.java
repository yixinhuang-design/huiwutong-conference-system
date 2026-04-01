package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingDiningAssign;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用餐分配 Mapper
 */
public interface SeatingDiningAssignMapper extends BaseMapper<SeatingDiningAssign> {

    @Select("SELECT * FROM conf_seating_dining_assign WHERE dining_id = #{diningId} AND tenant_id = #{tenantId}")
    List<SeatingDiningAssign> selectByDiningId(@Param("diningId") Long diningId, @Param("tenantId") Long tenantId);

    @Select("SELECT * FROM conf_seating_dining_assign WHERE dining_id = #{diningId} AND attendee_id = #{attendeeId}")
    SeatingDiningAssign selectByDiningAndAttendee(@Param("diningId") Long diningId, @Param("attendeeId") Long attendeeId);

    @Select("SELECT * FROM conf_seating_dining_assign WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingDiningAssign> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
