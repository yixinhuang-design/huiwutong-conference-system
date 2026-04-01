package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingDiscussion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 讨论室 Mapper
 */
public interface SeatingDiscussionMapper extends BaseMapper<SeatingDiscussion> {

    @Select("SELECT * FROM conf_seating_discussion WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingDiscussion> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);

    @Select("SELECT * FROM conf_seating_discussion WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND assigned_count < capacity")
    List<SeatingDiscussion> selectAvailableDiscussions(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
