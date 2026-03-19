package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingVenue;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会场 Mapper
 * @author AI Assistant
 * @date 2026-03-12
 */
public interface SeatingVenueMapper extends BaseMapper<SeatingVenue> {
    
    /**
     * 查询会议+日程的所有会场
     */
    @Select("<script>" +
            "SELECT * FROM conf_seating_venue WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}" +
            "<if test='scheduleId != null'> AND schedule_id = #{scheduleId}</if>" +
            "<if test='scheduleId == null'> AND schedule_id IS NULL</if>" +
            "</script>")
    List<SeatingVenue> selectByConferenceAndSchedule(@Param("conferenceId") Long conferenceId,
                                                      @Param("scheduleId") Long scheduleId,
                                                      @Param("tenantId") Long tenantId);

    /**
     * 查询会议的所有会场（不区分日程）
     */
    @Select("SELECT * FROM conf_seating_venue WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingVenue> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
