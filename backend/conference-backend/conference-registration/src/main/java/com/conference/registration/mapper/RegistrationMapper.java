package com.conference.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.registration.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 报名 Mapper
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
    
    /**
     * 获取会议的报名统计
     */
    @Select("SELECT status, COUNT(*) as count FROM conf_registration " +
            "WHERE meeting_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0 " +
            "GROUP BY status")
    List<java.util.Map<String, Object>> getRegistrationStats(
        @Param("conferenceId") Long conferenceId,
        @Param("tenantId") Long tenantId
    );

    @Select("SELECT registration_config FROM conf_meeting WHERE id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0 LIMIT 1")
    String getMeetingRegistrationConfig(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId);

    @Update("UPDATE conf_meeting SET registration_config = #{config}, update_time = NOW() WHERE id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0")
    int updateMeetingRegistrationConfig(@Param("conferenceId") Long conferenceId, @Param("tenantId") Long tenantId, @Param("config") String config);
}
