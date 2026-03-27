package com.conference.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.meeting.dto.ScheduleCreateRequest;
import com.conference.meeting.dto.ScheduleResponse;
import com.conference.meeting.dto.ScheduleSettingsResponse;
import com.conference.meeting.dto.ScheduleUpdateRequest;
import com.conference.meeting.entity.Schedule;
import com.conference.meeting.entity.ScheduleAttachment;
import com.conference.meeting.entity.ScheduleSettings;
import com.conference.meeting.mapper.ScheduleAttachmentMapper;
import com.conference.meeting.mapper.ScheduleMapper;
import com.conference.meeting.mapper.ScheduleSettingsMapper;
import com.conference.meeting.service.IScheduleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 日程管理Service实现类
 */
@Slf4j
@Service
@AllArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements IScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final ScheduleSettingsMapper scheduleSettingsMapper;
    private final ScheduleAttachmentMapper scheduleAttachmentMapper;

    @Override
    @Transactional
    public ScheduleResponse createSchedule(Long meetingId, ScheduleCreateRequest request) {
        log.info("=== 日程创建开始 ===");
        
        // 获取租户ID
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 参数校验
        if (meetingId == null || meetingId <= 0) {
            throw new BusinessException("会议ID无效");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException("日程主题不能为空");
        }
        if (request.getStartTime() == null) {
            throw new BusinessException("开始时间不能为空");
        }
        if (request.getEndTime() == null) {
            throw new BusinessException("结束时间不能为空");
        }
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        if (!StringUtils.hasText(request.getLocation())) {
            throw new BusinessException("地点不能为空");
        }

        log.debug("日程创建参数校验成功，日程主题: {}", request.getTitle());

        // 创建日程
        Schedule schedule = Schedule.builder()
                .meetingId(meetingId)
                .tenantId(tenantId)
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .host(request.getHost())
                .speaker(request.getSpeaker())
                .speakerIntro(request.getSpeakerIntro())
                .notes(request.getNotes())
                .status(0) // 默认待发布
                .priority(request.getPriority() != null ? request.getPriority() : 1)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .createdBy(TenantContextHolder.getUserId())
                .createdTime(LocalDateTime.now())
                .deleted(0)
                .build();

        // 保存日程
        if (!this.save(schedule)) {
            throw new BusinessException("日程保存失败");
        }

        log.info("日程保存成功，ID: {}, 标题: {}", schedule.getId(), schedule.getTitle());

        // 创建日程设置
        if (request.getSettings() != null) {
            ScheduleSettings settings = convertSettingsRequestToEntity(request.getSettings(), schedule.getId(), meetingId, tenantId);
            if (scheduleSettingsMapper.insert(settings) <= 0) {
                throw new BusinessException("日程设置保存失败");
            }
            log.debug("日程设置保存成功");
        }

        log.info("=== 日程创建完成 ===");
        return getScheduleDetail(schedule.getId());
    }

    @Override
    @Transactional
    public ScheduleResponse updateSchedule(Long id, ScheduleUpdateRequest request) {
        log.info("=== 日程更新开始，ID: {} ===", id);
        
        // 获取租户ID
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查日程是否存在
        Schedule schedule = this.getById(id);
        if (schedule == null || !schedule.getTenantId().equals(tenantId)) {
            throw new BusinessException("日程不存在或无权限访问");
        }

        // 时间校验
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new BusinessException("开始时间不能晚于结束时间");
            }
        }

        log.debug("日程更新参数校验成功");

        // 更新日程信息
        if (StringUtils.hasText(request.getTitle())) {
            schedule.setTitle(request.getTitle());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (StringUtils.hasText(request.getLocation())) {
            schedule.setLocation(request.getLocation());
        }
        if (StringUtils.hasText(request.getHost())) {
            schedule.setHost(request.getHost());
        }
        if (StringUtils.hasText(request.getSpeaker())) {
            schedule.setSpeaker(request.getSpeaker());
        }
        if (StringUtils.hasText(request.getSpeakerIntro())) {
            schedule.setSpeakerIntro(request.getSpeakerIntro());
        }
        if (StringUtils.hasText(request.getNotes())) {
            schedule.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            schedule.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            schedule.setPriority(request.getPriority());
        }
        if (request.getSortOrder() != null) {
            schedule.setSortOrder(request.getSortOrder());
        }

        schedule.setUpdatedBy(TenantContextHolder.getUserId());
        schedule.setUpdatedTime(LocalDateTime.now());

        // 保存更新
        if (!this.updateById(schedule)) {
            throw new BusinessException("日程更新失败");
        }

        log.info("日程信息更新成功");

        // 更新日程设置
        if (request.getSettings() != null) {
            ScheduleSettings settings = scheduleSettingsMapper.selectByScheduleId(id);
            if (settings == null) {
                settings = convertSettingsRequestToEntity(request.getSettings(), id, schedule.getMeetingId(), tenantId);
                scheduleSettingsMapper.insert(settings);
                log.debug("日程设置创建成功");
            } else {
                updateSettingsFromRequest(settings, request.getSettings());
                scheduleSettingsMapper.updateById(settings);
                log.debug("日程设置更新成功");
            }
        }

        log.info("=== 日程更新完成 ===");
        return getScheduleDetail(id);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        log.info("=== 日程删除开始，ID: {} ===", id);
        
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Schedule schedule = this.getById(id);
        if (schedule == null || !schedule.getTenantId().equals(tenantId)) {
            throw new BusinessException("日程不存在或无权限访问");
        }

        // 逻辑删除
        if (!this.removeById(id)) {
            throw new BusinessException("日程删除失败");
        }

        // 同时删除相关的设置、附件等
        scheduleSettingsMapper.delete(new LambdaQueryWrapper<ScheduleSettings>()
                .eq(ScheduleSettings::getScheduleId, id));
        scheduleAttachmentMapper.delete(new LambdaQueryWrapper<ScheduleAttachment>()
                .eq(ScheduleAttachment::getScheduleId, id));

        log.info("=== 日程及其相关数据删除成功 ===");
    }

    @Override
    public ScheduleResponse getScheduleDetail(Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        Schedule schedule = this.getOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getId, id)
                .eq(Schedule::getTenantId, tenantId)
                .eq(Schedule::getDeleted, 0));
        
        if (schedule == null) {
            throw new BusinessException("日程不存在");
        }

        return convertScheduleToResponse(schedule);
    }

    @Override
    public IPage<ScheduleResponse> listSchedules(Long meetingId, Page<Schedule> page) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        IPage<Schedule> schedulePage = scheduleMapper.selectByMeetingId(page, meetingId, tenantId);
        
        IPage<ScheduleResponse> responsePage = schedulePage.convert(this::convertScheduleToResponse);
        return responsePage;
    }

    @Override
    public List<ScheduleResponse> listAllSchedules(Long meetingId) {
        Long tenantId = TenantContextHolder.getTenantId();
        log.info("[ScheduleService] listAllSchedules - meetingId: {}, tenantId from context: {}", meetingId, tenantId);
        
        List<Schedule> schedules = scheduleMapper.selectByMeetingIdList(meetingId, tenantId);
        log.info("[ScheduleService] query result count: {}", schedules.size());
        
        return schedules.stream()
                .map(this::convertScheduleToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> listNeedCheckinSchedules(Long meetingId) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        List<Schedule> schedules = scheduleMapper.selectNeedCheckinSchedules(meetingId, tenantId);
        return schedules.stream()
                .map(this::convertScheduleToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> listNeedReminderSchedules(Long meetingId) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        List<Schedule> schedules = scheduleMapper.selectNeedReminderSchedules(meetingId, tenantId);
        return schedules.stream()
                .map(this::convertScheduleToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> listOngoingSchedules(Long meetingId) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        List<Schedule> schedules = scheduleMapper.selectOngoingSchedules(meetingId, tenantId);
        return schedules.stream()
                .map(this::convertScheduleToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> listUpcomingSchedules(Long meetingId) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        List<Schedule> schedules = scheduleMapper.selectUpcomingSchedules(meetingId, tenantId);
        return schedules.stream()
                .map(this::convertScheduleToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> listSchedulesByTimeRange(Long meetingId, LocalDateTime startTime, LocalDateTime endTime) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        List<Schedule> schedules = scheduleMapper.selectByTimeRange(meetingId, tenantId, startTime, endTime);
        return schedules.stream()
                .map(this::convertScheduleToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ScheduleResponse getNextSchedule(Long meetingId) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        Schedule schedule = scheduleMapper.selectNextSchedule(meetingId, tenantId);
        if (schedule == null) {
            return null;
        }
        
        return convertScheduleToResponse(schedule);
    }

    @Override
    public ScheduleResponse getCurrentSchedule(Long meetingId) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        Schedule schedule = scheduleMapper.selectCurrentSchedule(meetingId, tenantId);
        if (schedule == null) {
            return null;
        }
        
        return convertScheduleToResponse(schedule);
    }

    @Override
    @Transactional
    public void publishSchedule(Long id) {
        log.info("发布日程，ID: {}", id);
        
        Schedule schedule = this.getById(id);
        if (schedule == null) {
            throw new BusinessException("日程不存在");
        }
        
        schedule.setStatus(1); // 1=已发布
        schedule.setUpdatedTime(LocalDateTime.now());
        this.updateById(schedule);
        
        log.info("日程发布成功");
    }

    @Override
    @Transactional
    public void cancelSchedule(Long id) {
        log.info("取消日程，ID: {}", id);
        
        Schedule schedule = this.getById(id);
        if (schedule == null) {
            throw new BusinessException("日程不存在");
        }
        
        schedule.setStatus(4); // 4=已取消
        schedule.setUpdatedTime(LocalDateTime.now());
        this.updateById(schedule);
        
        log.info("日程取消成功");
    }

    @Override
    public Integer countSchedules(Long meetingId) {
        Long tenantId = TenantContextHolder.getTenantId();
        return scheduleMapper.countByMeetingId(meetingId, tenantId);
    }

    @Override
    @Transactional
    public void deleteSchedulesByMeetingId(Long meetingId) {
        log.info("批量删除会议日程，会议ID: {}", meetingId);
        
        Long tenantId = TenantContextHolder.getTenantId();
        
        List<Schedule> schedules = scheduleMapper.selectByMeetingIdList(meetingId, tenantId);
        for (Schedule schedule : schedules) {
            deleteSchedule(schedule.getId());
        }
        
        log.info("批量删除完成");
    }

    @Override
    @Transactional
    public ScheduleResponse duplicateSchedule(Long id) {
        log.info("复制日程，源日程ID: {}", id);
        
        Long tenantId = TenantContextHolder.getTenantId();
        
        Schedule source = this.getOne(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getId, id)
                .eq(Schedule::getTenantId, tenantId));
        
        if (source == null) {
            throw new BusinessException("源日程不存在");
        }

        // 创建副本
        Schedule copy = Schedule.builder()
                .meetingId(source.getMeetingId())
                .tenantId(source.getTenantId())
                .title(source.getTitle() + "_复制")
                .startTime(source.getStartTime())
                .endTime(source.getEndTime())
                .location(source.getLocation())
                .host(source.getHost())
                .speaker(source.getSpeaker())
                .speakerIntro(source.getSpeakerIntro())
                .notes(source.getNotes())
                .status(0) // 新复制的日程为待发布状态
                .priority(source.getPriority())
                .sortOrder(source.getSortOrder())
                .createdBy(TenantContextHolder.getUserId())
                .createdTime(LocalDateTime.now())
                .deleted(0)
                .build();

        if (!this.save(copy)) {
            throw new BusinessException("日程复制失败");
        }

        // 复制设置
        ScheduleSettings sourceSettings = scheduleSettingsMapper.selectByScheduleId(id);
        if (sourceSettings != null) {
            ScheduleSettings copiedSettings = ScheduleSettings.builder()
                    .scheduleId(copy.getId())
                    .meetingId(copy.getMeetingId())
                    .tenantId(copy.getTenantId())
                    .needReport(sourceSettings.getNeedReport())
                    .reportMethod(sourceSettings.getReportMethod())
                    .reportDescription(sourceSettings.getReportDescription())
                    .needCheckin(sourceSettings.getNeedCheckin())
                    .checkinMethod(sourceSettings.getCheckinMethod())
                    .checkinDescription(sourceSettings.getCheckinDescription())
                    .needReminder(sourceSettings.getNeedReminder())
                    .reminderTarget(sourceSettings.getReminderTarget())
                    .reminderTime(sourceSettings.getReminderTime())
                    .reminderMethods(sourceSettings.getReminderMethods())
                    .allowChangeLocation(sourceSettings.getAllowChangeLocation())
                    .autoBroadcast(sourceSettings.getAutoBroadcast())
                    .broadcastUrl(sourceSettings.getBroadcastUrl())
                    .createdTime(LocalDateTime.now())
                    .deleted(0)
                    .build();
            scheduleSettingsMapper.insert(copiedSettings);
        }

        log.info("日程复制完成，新日程ID: {}", copy.getId());
        return getScheduleDetail(copy.getId());
    }

    // ==================== 辅助方法 ====================

    /**
     * 将Schedule转换为ScheduleResponse
     */
    private ScheduleResponse convertScheduleToResponse(Schedule schedule) {
        ScheduleSettings settings = scheduleSettingsMapper.selectByScheduleId(schedule.getId());
        List<ScheduleAttachment> attachments = scheduleAttachmentMapper.selectByScheduleId(schedule.getId());

        // 转换 settings 对象
        ScheduleSettingsResponse settingsResponse = null;
        if (settings != null) {
            // 转换 reminderMethods 从 JSON 字符串到列表
            List<String> reminderMethodsList = null;
            if (StringUtils.hasText(settings.getReminderMethods())) {
                try {
                    reminderMethodsList = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(settings.getReminderMethods(), 
                            com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance()
                                .constructCollectionType(java.util.List.class, String.class));
                } catch (Exception e) {
                    log.warn("Failed to parse reminderMethods JSON: {}", settings.getReminderMethods(), e);
                    reminderMethodsList = new java.util.ArrayList<>();
                }
            }
            
            settingsResponse = ScheduleSettingsResponse.builder()
                    .needReport(settings.getNeedReport() == 1)
                    .reportMethod(settings.getReportMethod())
                    .reportDescription(settings.getReportDescription())
                    .needCheckin(settings.getNeedCheckin() == 1)
                    .checkinMethod(settings.getCheckinMethod())
                    .checkinDescription(settings.getCheckinDescription())
                    .needReminder(settings.getNeedReminder() == 1)
                    .reminderTarget(settings.getReminderTarget())
                    .reminderTime(settings.getReminderTime())
                    .reminderMethods(reminderMethodsList)
                    .build();
        } else {
            // 如果没有 settings，返回默认的空对象以避免 null 指针异常
            settingsResponse = ScheduleSettingsResponse.builder()
                    .needReport(false)
                    .needCheckin(false)
                    .needReminder(false)
                    .build();
        }

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .meetingId(schedule.getMeetingId())
                .title(schedule.getTitle())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .location(schedule.getLocation())
                .host(schedule.getHost())
                .speaker(schedule.getSpeaker())
                .speakerIntro(schedule.getSpeakerIntro())
                .notes(schedule.getNotes())
                .status(schedule.getStatus())
                .statusText(getStatusText(schedule.getStatus()))
                .priority(schedule.getPriority())
                .sortOrder(schedule.getSortOrder())
                .createdBy(schedule.getCreatedBy())
                .createdTime(schedule.getCreatedTime())
                .updatedTime(schedule.getUpdatedTime())
                .settings(settingsResponse)
                .build();
    }

    /**
     * 将ScheduleSettingsRequest转换为ScheduleSettings实体
     */
    private ScheduleSettings convertSettingsRequestToEntity(
            com.conference.meeting.dto.ScheduleSettingsRequest request,
            Long scheduleId, Long meetingId, Long tenantId) {
        // 将 reminderMethods 转换为 JSON 数组字符串
        String reminderMethodsJson = null;
        if (request.getReminderMethods() != null && !request.getReminderMethods().isEmpty()) {
            try {
                reminderMethodsJson = com.fasterxml.jackson.databind.ObjectMapper.class.cast(
                    new com.fasterxml.jackson.databind.ObjectMapper()
                ).writeValueAsString(request.getReminderMethods());
            } catch (Exception e) {
                log.warn("Failed to serialize reminderMethods to JSON", e);
                reminderMethodsJson = null;
            }
        }
        
        return ScheduleSettings.builder()
                .scheduleId(scheduleId)
                .meetingId(meetingId)
                .tenantId(tenantId)
                .needReport(request.getNeedReport() != null ? (request.getNeedReport() ? 1 : 0) : 0)
                .reportMethod(request.getReportMethod())
                .reportDescription(request.getReportDescription())
                .needCheckin(request.getNeedCheckin() != null ? (request.getNeedCheckin() ? 1 : 0) : 0)
                .checkinMethod(request.getCheckinMethod())
                .checkinDescription(request.getCheckinDescription())
                .needReminder(request.getNeedReminder() != null ? (request.getNeedReminder() ? 1 : 0) : 0)
                .reminderTarget(request.getReminderTarget())
                .reminderTime(request.getReminderTime())
                .reminderMethods(reminderMethodsJson)
                .allowChangeLocation(request.getAllowChangeLocation() != null ? (request.getAllowChangeLocation() ? 1 : 0) : 1)
                .autoBroadcast(request.getAutoBroadcast() != null ? (request.getAutoBroadcast() ? 1 : 0) : 0)
                .broadcastUrl(request.getBroadcastUrl())
                .createdTime(LocalDateTime.now())
                .deleted(0)
                .build();
    }

    /**
     * 从ScheduleSettingsRequest更新ScheduleSettings
     */
    private void updateSettingsFromRequest(ScheduleSettings settings,
            com.conference.meeting.dto.ScheduleSettingsRequest request) {
        if (request.getNeedReport() != null) {
            settings.setNeedReport(request.getNeedReport() ? 1 : 0);
        }
        if (StringUtils.hasText(request.getReportMethod())) {
            settings.setReportMethod(request.getReportMethod());
        }
        if (StringUtils.hasText(request.getReportDescription())) {
            settings.setReportDescription(request.getReportDescription());
        }
        if (request.getNeedCheckin() != null) {
            settings.setNeedCheckin(request.getNeedCheckin() ? 1 : 0);
        }
        if (StringUtils.hasText(request.getCheckinMethod())) {
            settings.setCheckinMethod(request.getCheckinMethod());
        }
        if (StringUtils.hasText(request.getCheckinDescription())) {
            settings.setCheckinDescription(request.getCheckinDescription());
        }
        if (request.getNeedReminder() != null) {
            settings.setNeedReminder(request.getNeedReminder() ? 1 : 0);
        }
        if (StringUtils.hasText(request.getReminderTarget())) {
            settings.setReminderTarget(request.getReminderTarget());
        }
        if (request.getReminderTime() != null) {
            settings.setReminderTime(request.getReminderTime());
        }
        if (request.getReminderMethods() != null && !request.getReminderMethods().isEmpty()) {
            try {
                String reminderMethodsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(request.getReminderMethods());
                settings.setReminderMethods(reminderMethodsJson);
            } catch (Exception e) {
                log.warn("Failed to serialize reminderMethods to JSON", e);
            }
        }
        if (request.getAllowChangeLocation() != null) {
            settings.setAllowChangeLocation(request.getAllowChangeLocation() ? 1 : 0);
        }
        if (request.getAutoBroadcast() != null) {
            settings.setAutoBroadcast(request.getAutoBroadcast() ? 1 : 0);
        }
        if (StringUtils.hasText(request.getBroadcastUrl())) {
            settings.setBroadcastUrl(request.getBroadcastUrl());
        }
        settings.setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待发布";
            case 1: return "已发布";
            case 2: return "进行中";
            case 3: return "已结束";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}
