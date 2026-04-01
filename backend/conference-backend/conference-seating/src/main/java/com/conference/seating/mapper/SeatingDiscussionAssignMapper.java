package com.conference.seating.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.seating.entity.SeatingDiscussionAssign;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 讨论室分配 Mapper
 */
public interface SeatingDiscussionAssignMapper extends BaseMapper<SeatingDiscussionAssign> {

    @Select("SELECT * FROM conf_seating_discussion_assign WHERE discussion_id = #{discussionId} AND tenant_id = #{tenantId}")
    List<SeatingDiscussionAssign> selectByDiscussionId(@Param("discussionId") Long discussionId, @Param("tenantId") Long tenantId);

    @Select("SELECT * FROM conf_seating_discussion_assign WHERE discussion_id = #{discussionId} AND attendee_id = #{attendeeId}")
    SeatingDiscussionAssign selectByDiscussionAndAttendee(@Param("discussionId") Long discussionId, @Param("attendeeId") Long attendeeId);

    @Select("SELECT * FROM conf_seating_discussion_assign WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId}")
    List<SeatingDiscussionAssign> selectByConferenceId(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);
}
