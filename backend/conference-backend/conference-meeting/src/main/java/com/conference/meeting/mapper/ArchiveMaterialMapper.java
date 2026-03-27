package com.conference.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.meeting.entity.ArchiveMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArchiveMaterialMapper extends BaseMapper<ArchiveMaterial> {

    @Select("SELECT COUNT(*) FROM conf_archive_material WHERE meeting_id = #{meetingId} AND category = #{category} AND deleted = 0")
    int countByMeetingAndCategory(@Param("meetingId") Long meetingId, @Param("category") String category);
}
