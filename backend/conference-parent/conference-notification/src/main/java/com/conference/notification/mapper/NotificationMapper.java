package com.conference.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT status, COUNT(*) as count FROM conf_notification " +
            "WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0 " +
            "GROUP BY status")
    List<Map<String, Object>> getStatusStats(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );

    @Select("SELECT COALESCE(SUM(sent_count),0) as totalSent, " +
            "COALESCE(SUM(delivered_count),0) as delivered, " +
            "COALESCE(SUM(read_count),0) as readCount, " +
            "COALESCE(SUM(failed_count),0) as failed " +
            "FROM conf_notification " +
            "WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0")
    Map<String, Object> getSendStats(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );
}
