package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingAccommodation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住宿安排 Mapper
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingAccommodationMapper extends BaseMapper<SeatingAccommodation> {
    
    /**
     * 查询会议的所有住宿安排
     */
    @Select("SELECT * FROM conf_seating_accommodation WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingAccommodation> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
    
    /**
     * 查询可用的住宿安排
     */
    @Select("SELECT * FROM conf_seating_accommodation WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND assigned_count < capacity")
    List<SeatingAccommodation> selectAvailableAccommodations(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
