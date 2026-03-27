package com.conference.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.notification.entity.AlertEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlertEventMapper extends BaseMapper<AlertEvent> {

    @Select("SELECT status, COUNT(*) as count FROM conf_alert_event " +
            "WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0 " +
            "GROUP BY status")
    List<Map<String, Object>> getStatusStats(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );

    @Select("SELECT severity, COUNT(*) as count FROM conf_alert_event " +
            "WHERE conference_id = #{conferenceId} AND tenant_id = #{tenantId} AND deleted = 0 " +
            "GROUP BY severity")
    List<Map<String, Object>> getSeverityStats(
            @Param("conferenceId") Long conferenceId,
            @Param("tenantId") Long tenantId
    );
}
