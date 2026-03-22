package com.conference.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.meeting.entity.ArchiveMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArchiveMessageMapper extends BaseMapper<ArchiveMessage> {

    @Select("SELECT COALESCE(SUM(g.message_count), 0) FROM conf_archive_message_group g WHERE g.meeting_id = #{meetingId} AND g.deleted = 0")
    int sumMessageCountByMeeting(@Param("meetingId") Long meetingId);
}
