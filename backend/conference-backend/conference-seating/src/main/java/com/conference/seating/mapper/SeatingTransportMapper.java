package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingTransport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 车辆运输 Mapper
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingTransportMapper extends BaseMapper<SeatingTransport> {
    
    /**
     * 查询会议的所有车辆
     */
    @Select("SELECT * FROM conf_seating_transport WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingTransport> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
    
    /**
     * 查询可用的车辆
     */
    @Select("SELECT * FROM conf_seating_transport WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND assigned_count < capacity")
    List<SeatingTransport> selectAvailableTransports(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
