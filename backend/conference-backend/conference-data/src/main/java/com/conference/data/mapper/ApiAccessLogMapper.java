package com.conference.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.data.entity.ApiAccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ApiAccessLogMapper extends BaseMapper<ApiAccessLog> {

    /**
     * 统计API总请求数(今日)
     */
    @Select("SELECT COUNT(*) FROM data_api_access_log WHERE DATE(created_time) = CURDATE()")
    long countTodayRequests();

    /**
     * 统计今日错误数
     */
    @Select("SELECT COUNT(*) FROM data_api_access_log WHERE DATE(created_time) = CURDATE() AND status_code >= 400")
    long countTodayErrors();

    /**
     * 统计今日平均响应时间
     */
    @Select("SELECT COALESCE(AVG(response_time), 0) FROM data_api_access_log WHERE DATE(created_time) = CURDATE()")
    double avgTodayResponseTime();

    /**
     * 热门接口统计
     */
    @Select("SELECT method, path, " +
            "COUNT(*) AS requests, " +
            "ROUND(AVG(response_time)) AS avg_time, " +
            "ROUND(SUM(CASE WHEN status_code < 400 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS success_rate " +
            "FROM data_api_access_log " +
            "WHERE DATE(created_time) = CURDATE() " +
            "GROUP BY method, path " +
            "ORDER BY requests DESC " +
            "LIMIT 10")
    List<Map<String, Object>> topEndpoints();

    /**
     * 按小时统计响应时间(24小时)
     */
    @Select("SELECT HOUR(created_time) AS hour, ROUND(AVG(response_time)) AS avg_time " +
            "FROM data_api_access_log " +
            "WHERE created_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "GROUP BY HOUR(created_time) " +
            "ORDER BY hour")
    List<Map<String, Object>> hourlyResponseTime();

    /**
     * 按天统计错误率(最近7天)
     */
    @Select("SELECT DATE(created_time) AS date, " +
            "ROUND(SUM(CASE WHEN status_code >= 400 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS error_rate " +
            "FROM data_api_access_log " +
            "WHERE created_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(created_time) " +
            "ORDER BY date")
    List<Map<String, Object>> dailyErrorRate();
}
