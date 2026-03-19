package com.conference.meeting.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.meeting.dto.ScheduleCreateRequest;
import com.conference.meeting.dto.ScheduleResponse;
import com.conference.meeting.dto.ScheduleUpdateRequest;
import com.conference.meeting.entity.Schedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程管理Service接口
 */
public interface IScheduleService extends IService<Schedule> {

    /**
     * 创建日程
     */
    ScheduleResponse createSchedule(Long meetingId, ScheduleCreateRequest request);

    /**
     * 更新日程
     */
    ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request);

    /**
     * 删除日程
     */
    void deleteSchedule(Long id);

    /**
     * 获取日程详情
     */
    ScheduleResponse getScheduleDetail(Long id);

    /**
     * 分页查询会议日程列表
     */
    IPage<ScheduleResponse> listSchedules(Long meetingId, Page<Schedule> page);

    /**
     * 查询会议所有日程（不分页）
     */
    List<ScheduleResponse> listAllSchedules(Long meetingId);

    /**
     * 查询需要签到的日程
     */
    List<ScheduleResponse> listNeedCheckinSchedules(Long meetingId);

    /**
     * 查询需要提醒的日程
     */
    List<ScheduleResponse> listNeedReminderSchedules(Long meetingId);

    /**
     * 查询进行中的日程
     */
    List<ScheduleResponse> listOngoingSchedules(Long meetingId);

    /**
     * 查询即将开始的日程（30分钟内）
     */
    List<ScheduleResponse> listUpcomingSchedules(Long meetingId);

    /**
     * 查询时间范围内的日程
     */
    List<ScheduleResponse> listSchedulesByTimeRange(Long meetingId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取下一个日程
     */
    ScheduleResponse getNextSchedule(Long meetingId);

    /**
     * 获取当前日程
     */
    ScheduleResponse getCurrentSchedule(Long meetingId);

    /**
     * 发布日程
     */
    void publishSchedule(Long id);

    /**
     * 取消日程
     */
    void cancelSchedule(Long id);

    /**
     * 统计会议日程数
     */
    Integer countSchedules(Long meetingId);

    /**
     * 批量删除日程
     */
    void deleteSchedulesByMeetingId(Long meetingId);

    /**
     * 复制日程
     */
    ScheduleResponse duplicateSchedule(Long id);
}
