package com.conference.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.meeting.entity.Meeting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MeetingMapper extends BaseMapper<Meeting> {

    IPage<Meeting> selectByStatus(Page<Meeting> page, @Param("tenantId") Long tenantId, 
                                   @Param("status") Integer status);

    @Select("SELECT * FROM conf_meeting WHERE tenant_id = #{tenantId} AND status IN (1, 3) " +
            "AND deleted = 0 ORDER BY start_time DESC")
    List<Meeting> selectOngoingMeetings(@Param("tenantId") Long tenantId);

    @Select("SELECT * FROM conf_meeting WHERE tenant_id = #{tenantId} AND meeting_code = #{code} " +
            "AND deleted = 0")
    Meeting selectByCode(@Param("tenantId") Long tenantId, @Param("code") String code);

    @Select("SELECT COUNT(*) FROM conf_meeting WHERE tenant_id = #{tenantId} AND deleted = 0")
    Integer countMeetings(@Param("tenantId") Long tenantId);
}
