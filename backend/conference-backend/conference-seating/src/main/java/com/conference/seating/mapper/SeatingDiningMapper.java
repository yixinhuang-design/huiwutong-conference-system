package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingDining;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用餐安排 Mapper
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingDiningMapper extends BaseMapper<SeatingDining> {
    
    /**
     * 查询会议的所有用餐安排
     */
    @Select("SELECT * FROM conf_seating_dining WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingDining> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
    
    /**
     * 查询可用的用餐安排
     */
    @Select("SELECT * FROM conf_seating_dining WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND assigned_count < capacity")
    List<SeatingDining> selectAvailableDinings(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
