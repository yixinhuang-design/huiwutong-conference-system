package com.conference.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.data.entity.SystemMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SystemMetricsMapper extends BaseMapper<SystemMetrics> {

    /**
     * 获取最新N条系统指标（用于实时图表）
     */
    @Select("SELECT * FROM data_system_metrics ORDER BY created_time DESC LIMIT #{limit}")
    List<SystemMetrics> getLatestMetrics(int limit);

    /**
     * 获取数据库连接信息
     */
    @Select("SHOW STATUS LIKE 'Threads_connected'")
    Map<String, Object> getDbConnections();

    /**
     * 获取数据库QPS
     */
    @Select("SHOW STATUS LIKE 'Questions'")
    Map<String, Object> getDbQuestions();

    /**
     * 获取慢查询数量
     */
    @Select("SHOW STATUS LIKE 'Slow_queries'")
    Map<String, Object> getSlowQueries();

    /**
     * 获取InnoDB Buffer Pool 物理读次数（缓存未命中时的磁盘读取）
     */
    @Select("SHOW STATUS LIKE 'Innodb_buffer_pool_reads'")
    Map<String, Object> getBufferPoolReads();

    /**
     * 获取InnoDB Buffer Pool 逻辑读请求次数（缓存命中+未命中总数）
     */
    @Select("SHOW STATUS LIKE 'Innodb_buffer_pool_read_requests'")
    Map<String, Object> getBufferPoolReadRequests();
}
