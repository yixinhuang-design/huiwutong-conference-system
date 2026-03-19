package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingSeat;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 座位 Mapper
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingSeatMapper extends BaseMapper<SeatingSeat> {
    
    /**
     * 查询会场的所有座位
     */
    @Select("SELECT * FROM conf_seating_seat WHERE venue_id = #{venueId} AND tenant_id = #{tenantId}")
    List<SeatingSeat> selectByVenueId(@Param("venueId") Long venueId, @Param("tenantId") Long tenantId);
}
