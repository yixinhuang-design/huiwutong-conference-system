package com.conference.data.service;

import com.conference.data.mapper.BusinessDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 业务数据监控服务
 * 负责报到率、签到率、查寝率等业务统计数据
 * 通过跨库查询从conference_registration获取实时数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessDataService {

    private final BusinessDataMapper businessDataMapper;

    /**
     * 获取报到率数据
     * @param conferenceId 会议ID（可选，为null则汇总全部）
     */
    public Map<String, Object> getReportRate(Long conferenceId) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            if (conferenceId != null) {
                Map<String, Object> data = businessDataMapper.getReportRateByConference(conferenceId);
                if (data != null) {
                    long total = toLong(data.get("total"));
                    long approved = toLong(data.get("approved"));
                    long reported = toLong(data.get("reported"));
                    long pending = toLong(data.get("pending"));

                    // 应报到 = 审核通过的报名数
                    long expected = approved;
                    // 已报到 = 已签到的
                    long actual = reported;
                    // 报到率
                    double rate = expected > 0 ? Math.round(actual * 1000.0 / expected) / 10.0 : 0;

                    result.put("expected", expected);
                    result.put("actual", actual);
                    result.put("rate", rate);
                    result.put("total", total);
                    result.put("pending", pending);
                } else {
                    result.put("expected", 0);
                    result.put("actual", 0);
                    result.put("rate", 0);
                }
            } else {
                // 无指定会议时，汇总全部
                List<Map<String, Object>> overview = businessDataMapper.getReportRateOverview();
                long totalExpected = 0, totalActual = 0;
                for (Map<String, Object> row : overview) {
                    totalExpected += toLong(row.get("approved_count"));
                    totalActual += toLong(row.get("reported_count"));
                }
                double rate = totalExpected > 0 ? Math.round(totalActual * 1000.0 / totalExpected) / 10.0 : 0;
                result.put("expected", totalExpected);
                result.put("actual", totalActual);
                result.put("rate", rate);
                result.put("meetings", overview);
            }
        } catch (Exception e) {
            log.error("获取报到率数据异常: {}", e.getMessage(), e);
            result.put("expected", 0);
            result.put("actual", 0);
            result.put("rate", 0);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取签到率数据（按日程维度）
     * @param conferenceId 会议ID
     */
    public List<Map<String, Object>> getCheckinRate(Long conferenceId) {
        try {
            if (conferenceId == null) {
                // 未指定会议，获取第一个会议
                List<Map<String, Object>> meetings = businessDataMapper.listMeetings();
                if (!meetings.isEmpty()) {
                    conferenceId = toLong(meetings.get(0).get("id"));
                } else {
                    return Collections.emptyList();
                }
            }

            List<Map<String, Object>> scheduleData = businessDataMapper.getCheckinRateBySchedule(conferenceId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> row : scheduleData) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", row.get("schedule_id"));
                item.put("name", row.get("schedule_name"));
                item.put("time", row.get("start_time") != null ? row.get("start_time").toString() : "");

                long expected = toLong(row.get("expected"));
                long actual = toLong(row.get("actual"));
                double rate = expected > 0 ? Math.round(actual * 1000.0 / expected) / 10.0 : 0;

                item.put("expected", expected);
                item.put("actual", actual);
                item.put("rate", rate);
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            log.error("获取签到率数据异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取查寝率数据（按日期维度）
     * 基于签到记录按日期汇总
     * @param conferenceId 会议ID
     */
    public List<Map<String, Object>> getDormitoryRate(Long conferenceId) {
        try {
            if (conferenceId == null) {
                List<Map<String, Object>> meetings = businessDataMapper.listMeetings();
                if (!meetings.isEmpty()) {
                    conferenceId = toLong(meetings.get(0).get("id"));
                } else {
                    return Collections.emptyList();
                }
            }

            List<Map<String, Object>> dailyData = businessDataMapper.getDailyCheckinSummary(conferenceId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> row : dailyData) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("date", row.get("checkin_date") != null ? row.get("checkin_date").toString() : "");

                long expected = toLong(row.get("expected"));
                long actual = toLong(row.get("actual"));
                double rate = expected > 0 ? Math.round(actual * 1000.0 / expected) / 10.0 : 0;

                item.put("expected", expected);
                item.put("actual", actual);
                item.put("rate", rate);
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            log.error("获取查寝率数据异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取会议列表
     */
    public List<Map<String, Object>> getMeetingList() {
        try {
            return businessDataMapper.listMeetings();
        } catch (Exception e) {
            log.error("获取会议列表异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取会议详情
     */
    public Map<String, Object> getMeetingInfo(Long conferenceId) {
        try {
            return businessDataMapper.getMeetingInfo(conferenceId);
        } catch (Exception e) {
            log.error("获取会议信息异常: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private long toLong(Object val) {
        if (val == null) return 0;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        if (val instanceof java.math.BigDecimal) return ((java.math.BigDecimal) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (Exception e) {
            return 0;
        }
    }
}
