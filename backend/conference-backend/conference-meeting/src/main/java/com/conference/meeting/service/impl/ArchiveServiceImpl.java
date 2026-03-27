package com.conference.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.conference.common.exception.BusinessException;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.meeting.dto.ArchiveMessageGroupResponse;
import com.conference.meeting.dto.ArchiveStatsResponse;
import com.conference.meeting.entity.*;
import com.conference.meeting.mapper.*;
import com.conference.meeting.service.IArchiveService;
import com.conference.meeting.service.IMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArchiveServiceImpl implements IArchiveService {

    @Autowired
    private ArchiveMaterialMapper materialMapper;

    @Autowired
    private ArchiveBusinessDataMapper businessDataMapper;

    @Autowired
    private ArchiveMessageGroupMapper messageGroupMapper;

    @Autowired
    private ArchiveMessageMapper messageMapper;

    @Autowired
    private ArchiveConfigMapper configMapper;

    @Autowired
    private IMeetingService meetingService;

    @Autowired
    private ScheduleAttachmentMapper scheduleAttachmentMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== 租户校验 ====================

    private Long requireTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }
        return tenantId;
    }

    private void validateMeetingAccess(Long meetingId) {
        // 复用 meetingService 的租户校验逻辑
        meetingService.getMeetingDetail(meetingId);
    }

    // ==================== 统计 ====================

    @Override
    public ArchiveStatsResponse getArchiveStats(Long meetingId) {
        validateMeetingAccess(meetingId);

        int coursewareCount = materialMapper.countByMeetingAndCategory(meetingId, "courseware");
        int interactionCount = materialMapper.countByMeetingAndCategory(meetingId, "interaction");

        // 业务数据：统计有多少种数据类型
        LambdaQueryWrapper<ArchiveBusinessData> bdWrapper = new LambdaQueryWrapper<ArchiveBusinessData>()
                .eq(ArchiveBusinessData::getMeetingId, meetingId)
                .eq(ArchiveBusinessData::getDeleted, 0)
                .select(ArchiveBusinessData::getDataType)
                .groupBy(ArchiveBusinessData::getDataType);
        int businessDataCount = Math.toIntExact(businessDataMapper.selectCount(
                new LambdaQueryWrapper<ArchiveBusinessData>()
                        .eq(ArchiveBusinessData::getMeetingId, meetingId)
                        .eq(ArchiveBusinessData::getDeleted, 0)
        ));

        // 消息总数
        int messageTotal = messageMapper.sumMessageCountByMeeting(meetingId);

        // 获取配置
        ArchiveConfig config = getOrCreateConfig(meetingId);

        return ArchiveStatsResponse.builder()
                .courseware(coursewareCount)
                .interaction(interactionCount)
                .businessData(businessDataCount)
                .messages(messageTotal)
                .allowStudentUpload(config.getAllowStudentUpload())
                .isPacked(config.getIsPacked())
                .build();
    }

    // ==================== 配置 ====================

    private ArchiveConfig getOrCreateConfig(Long meetingId) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<ArchiveConfig> wrapper = new LambdaQueryWrapper<ArchiveConfig>()
                .eq(ArchiveConfig::getMeetingId, meetingId)
                .eq(ArchiveConfig::getDeleted, 0);
        ArchiveConfig config = configMapper.selectOne(wrapper);
        if (config == null) {
            config = ArchiveConfig.builder()
                    .meetingId(meetingId)
                    .tenantId(tenantId)
                    .allowStudentUpload(true)
                    .isPacked(false)
                    .createTime(LocalDateTime.now())
                    .deleted(0)
                    .build();
            configMapper.insert(config);
            log.info("自动创建归档配置, meetingId={}", meetingId);
        }
        return config;
    }

    @Override
    public ArchiveConfig getArchiveConfig(Long meetingId) {
        validateMeetingAccess(meetingId);
        return getOrCreateConfig(meetingId);
    }

    @Override
    @Transactional
    public ArchiveConfig updateArchiveConfig(Long meetingId, Boolean allowStudentUpload) {
        validateMeetingAccess(meetingId);
        ArchiveConfig config = getOrCreateConfig(meetingId);
        if (allowStudentUpload != null) {
            config.setAllowStudentUpload(allowStudentUpload);
        }
        config.setUpdateTime(LocalDateTime.now());
        configMapper.updateById(config);
        log.info("更新归档配置, meetingId={}, allowStudentUpload={}", meetingId, allowStudentUpload);
        return config;
    }

    // ==================== 资料(课件+互动) ====================

    @Override
    public List<ArchiveMaterial> getMaterials(Long meetingId, String category) {
        validateMeetingAccess(meetingId);
        LambdaQueryWrapper<ArchiveMaterial> wrapper = new LambdaQueryWrapper<ArchiveMaterial>()
                .eq(ArchiveMaterial::getMeetingId, meetingId)
                .eq(ArchiveMaterial::getDeleted, 0)
                .orderByAsc(ArchiveMaterial::getSortOrder)
                .orderByDesc(ArchiveMaterial::getUploadTime);
        if (StringUtils.hasText(category)) {
            wrapper.eq(ArchiveMaterial::getCategory, category);
        }
        return materialMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public ArchiveMaterial addMaterial(ArchiveMaterial material) {
        Long tenantId = requireTenantId();
        validateMeetingAccess(material.getMeetingId());

        material.setTenantId(tenantId);
        if (material.getUploadTime() == null) {
            material.setUploadTime(LocalDateTime.now());
        }
        if (material.getUploadBy() == null) {
            material.setUploadBy(TenantContextHolder.getUserId());
        }
        if (material.getDownloadCount() == null) {
            material.setDownloadCount(0);
        }
        if (material.getSortOrder() == null) {
            material.setSortOrder(0);
        }
        material.setDeleted(0);
        material.setCreateTime(LocalDateTime.now());

        materialMapper.insert(material);
        log.info("新增归档资料, id={}, meetingId={}, category={}, title={}",
                material.getId(), material.getMeetingId(), material.getCategory(), material.getTitle());
        return material;
    }

    @Override
    @Transactional
    public Boolean deleteMaterial(Long meetingId, Long materialId) {
        validateMeetingAccess(meetingId);
        ArchiveMaterial material = materialMapper.selectById(materialId);
        if (material == null || material.getDeleted() == 1) {
            throw new BusinessException("资料不存在");
        }
        if (!Objects.equals(material.getMeetingId(), meetingId)) {
            throw new BusinessException("资料不属于该会议");
        }
        Long tenantId = requireTenantId();
        if (!Objects.equals(material.getTenantId(), tenantId)) {
            throw new BusinessException("无权删除该资料");
        }
        // 使用 removeById 进行逻辑删除
        materialMapper.deleteById(materialId);
        log.info("删除归档资料, id={}, title={}", materialId, material.getTitle());
        return true;
    }

    @Override
    public void incrementDownloadCount(Long materialId) {
        LambdaUpdateWrapper<ArchiveMaterial> wrapper = new LambdaUpdateWrapper<ArchiveMaterial>()
                .eq(ArchiveMaterial::getId, materialId)
                .setSql("download_count = download_count + 1");
        materialMapper.update(null, wrapper);
    }

    // ==================== 业务数据 ====================

    @Override
    public List<ArchiveBusinessData> getBusinessData(Long meetingId, String dataType) {
        validateMeetingAccess(meetingId);
        LambdaQueryWrapper<ArchiveBusinessData> wrapper = new LambdaQueryWrapper<ArchiveBusinessData>()
                .eq(ArchiveBusinessData::getMeetingId, meetingId)
                .eq(ArchiveBusinessData::getDeleted, 0)
                .orderByAsc(ArchiveBusinessData::getSortOrder)
                .orderByAsc(ArchiveBusinessData::getCreateTime);
        if (StringUtils.hasText(dataType)) {
            wrapper.eq(ArchiveBusinessData::getDataType, dataType);
        }
        return businessDataMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Boolean saveBusinessData(Long meetingId, String dataType, List<ArchiveBusinessData> dataList) {
        validateMeetingAccess(meetingId);
        Long tenantId = requireTenantId();

        // 先逻辑删除该类型的旧数据
        LambdaUpdateWrapper<ArchiveBusinessData> delWrapper = new LambdaUpdateWrapper<ArchiveBusinessData>()
                .eq(ArchiveBusinessData::getMeetingId, meetingId)
                .eq(ArchiveBusinessData::getDataType, dataType)
                .eq(ArchiveBusinessData::getDeleted, 0)
                .set(ArchiveBusinessData::getDeleted, 1);
        businessDataMapper.update(null, delWrapper);

        // 插入新数据
        int order = 0;
        for (ArchiveBusinessData data : dataList) {
            data.setMeetingId(meetingId);
            data.setTenantId(tenantId);
            data.setDataType(dataType);
            data.setSortOrder(order++);
            data.setDeleted(0);
            data.setCreateTime(LocalDateTime.now());
            businessDataMapper.insert(data);
        }
        log.info("批量保存业务数据, meetingId={}, dataType={}, count={}", meetingId, dataType, dataList.size());
        return true;
    }

    @Override
    @Transactional
    public ArchiveBusinessData addBusinessData(ArchiveBusinessData data) {
        Long tenantId = requireTenantId();
        validateMeetingAccess(data.getMeetingId());

        data.setTenantId(tenantId);
        data.setDeleted(0);
        data.setCreateTime(LocalDateTime.now());
        if (data.getSortOrder() == null) {
            data.setSortOrder(0);
        }
        businessDataMapper.insert(data);
        log.info("新增业务数据, id={}, meetingId={}, dataType={}", data.getId(), data.getMeetingId(), data.getDataType());
        return data;
    }

    // ==================== 消息群组 ====================

    @Override
    public List<ArchiveMessageGroupResponse> getMessageGroups(Long meetingId) {
        validateMeetingAccess(meetingId);

        LambdaQueryWrapper<ArchiveMessageGroup> wrapper = new LambdaQueryWrapper<ArchiveMessageGroup>()
                .eq(ArchiveMessageGroup::getMeetingId, meetingId)
                .eq(ArchiveMessageGroup::getDeleted, 0)
                .orderByDesc(ArchiveMessageGroup::getLastActiveTime);
        List<ArchiveMessageGroup> groups = messageGroupMapper.selectList(wrapper);

        // 为每个群组获取预览消息（最近2条）
        return groups.stream().map(group -> {
            LambdaQueryWrapper<ArchiveMessage> msgWrapper = new LambdaQueryWrapper<ArchiveMessage>()
                    .eq(ArchiveMessage::getGroupId, group.getId())
                    .eq(ArchiveMessage::getDeleted, 0)
                    .orderByDesc(ArchiveMessage::getSendTime)
                    .last("LIMIT 2");
            List<ArchiveMessage> previewMessages = messageMapper.selectList(msgWrapper);
            Collections.reverse(previewMessages);

            String lastActive = formatLastActive(group.getLastActiveTime());

            return ArchiveMessageGroupResponse.builder()
                    .id(group.getId())
                    .meetingId(group.getMeetingId())
                    .groupName(group.getGroupName())
                    .messageCount(group.getMessageCount())
                    .lastActive(lastActive)
                    .previewMessages(previewMessages)
                    .build();
        }).collect(Collectors.toList());
    }

    private String formatLastActive(LocalDateTime lastActiveTime) {
        if (lastActiveTime == null) return "未知";
        Duration duration = Duration.between(lastActiveTime, LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        long hours = duration.toHours();
        if (hours < 24) return hours + "小时前";
        long days = duration.toDays();
        if (days < 30) return days + "天前";
        return lastActiveTime.toLocalDate().toString();
    }

    @Override
    @Transactional
    public ArchiveMessageGroup addMessageGroup(ArchiveMessageGroup group) {
        Long tenantId = requireTenantId();
        validateMeetingAccess(group.getMeetingId());

        group.setTenantId(tenantId);
        if (group.getMessageCount() == null) {
            group.setMessageCount(0);
        }
        group.setDeleted(0);
        group.setCreateTime(LocalDateTime.now());
        messageGroupMapper.insert(group);
        log.info("新增消息群组, id={}, meetingId={}, groupName={}", group.getId(), group.getMeetingId(), group.getGroupName());
        return group;
    }

    @Override
    public List<ArchiveMessage> getGroupMessages(Long meetingId, Long groupId, String keyword) {
        validateMeetingAccess(meetingId);

        LambdaQueryWrapper<ArchiveMessage> wrapper = new LambdaQueryWrapper<ArchiveMessage>()
                .eq(ArchiveMessage::getGroupId, groupId)
                .eq(ArchiveMessage::getMeetingId, meetingId)
                .eq(ArchiveMessage::getDeleted, 0)
                .orderByAsc(ArchiveMessage::getSendTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(ArchiveMessage::getContent, keyword)
                    .or().like(ArchiveMessage::getSender, keyword));
        }
        return messageMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public ArchiveMessage addMessage(ArchiveMessage message) {
        Long tenantId = requireTenantId();
        validateMeetingAccess(message.getMeetingId());

        message.setTenantId(tenantId);
        if (message.getMessageType() == null) {
            message.setMessageType("text");
        }
        message.setDeleted(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // 更新群组消息数和最后活跃时间
        updateGroupStats(message.getGroupId());

        return message;
    }

    @Override
    @Transactional
    public Boolean batchAddMessages(Long meetingId, Long groupId, List<ArchiveMessage> messages) {
        Long tenantId = requireTenantId();
        validateMeetingAccess(meetingId);

        for (ArchiveMessage msg : messages) {
            msg.setGroupId(groupId);
            msg.setMeetingId(meetingId);
            msg.setTenantId(tenantId);
            if (msg.getMessageType() == null) {
                msg.setMessageType("text");
            }
            msg.setDeleted(0);
            msg.setCreateTime(LocalDateTime.now());
            messageMapper.insert(msg);
        }

        updateGroupStats(groupId);
        log.info("批量添加消息, meetingId={}, groupId={}, count={}", meetingId, groupId, messages.size());
        return true;
    }

    private void updateGroupStats(Long groupId) {
        long msgCount = messageMapper.selectCount(
                new LambdaQueryWrapper<ArchiveMessage>()
                        .eq(ArchiveMessage::getGroupId, groupId)
                        .eq(ArchiveMessage::getDeleted, 0)
        );
        LambdaUpdateWrapper<ArchiveMessageGroup> updateWrapper = new LambdaUpdateWrapper<ArchiveMessageGroup>()
                .eq(ArchiveMessageGroup::getId, groupId)
                .set(ArchiveMessageGroup::getMessageCount, (int) msgCount)
                .set(ArchiveMessageGroup::getLastActiveTime, LocalDateTime.now())
                .set(ArchiveMessageGroup::getUpdateTime, LocalDateTime.now());
        messageGroupMapper.update(null, updateWrapper);
    }

    // ==================== 从源数据同步 ====================

    @Override
    @Transactional
    public Map<String, Object> syncFromSource(Long meetingId) {
        validateMeetingAccess(meetingId);
        Long tenantId = requireTenantId();
        Map<String, Object> result = new LinkedHashMap<>();

        int materialsSynced = syncScheduleAttachments(meetingId, tenantId);
        result.put("materialsSynced", materialsSynced);

        Map<String, Integer> messageSyncResult = syncHandbookDiscussions(meetingId, tenantId);
        result.put("messageGroupsSynced", messageSyncResult.get("groups"));
        result.put("messagesSynced", messageSyncResult.get("messages"));

        // 同步聊天群组消息（conference_collaboration库）
        Map<String, Integer> chatSyncResult = syncChatMessages(meetingId, tenantId);
        result.put("chatGroupsSynced", chatSyncResult.get("groups"));
        result.put("chatMessagesSynced", chatSyncResult.get("messages"));

        int checkinSynced = syncCheckinData(meetingId, tenantId);
        result.put("checkinDataSynced", checkinSynced);

        // 同步报到率数据（按单位聚合）
        int reportRateSynced = syncReportRateData(meetingId, tenantId);
        result.put("reportRateDataSynced", reportRateSynced);

        log.info("归档数据同步完成, meetingId={}, result={}", meetingId, result);
        return result;
    }

    /**
     * 同步日程附件 → 归档资料（课件）
     * conf_schedule_attachment → conf_archive_material (category=courseware)
     */
    private int syncScheduleAttachments(Long meetingId, Long tenantId) {
        // 查询该会议所有未删除的日程附件
        LambdaQueryWrapper<ScheduleAttachment> wrapper = new LambdaQueryWrapper<ScheduleAttachment>()
                .eq(ScheduleAttachment::getMeetingId, meetingId)
                .eq(ScheduleAttachment::getDeleted, 0);
        List<ScheduleAttachment> attachments = scheduleAttachmentMapper.selectList(wrapper);

        if (attachments.isEmpty()) {
            log.info("会议{}无日程附件可同步", meetingId);
            return 0;
        }

        int synced = 0;
        for (ScheduleAttachment att : attachments) {
            // 去重：按 fileUrl 判断是否已同步
            long exists = materialMapper.selectCount(
                    new LambdaQueryWrapper<ArchiveMaterial>()
                            .eq(ArchiveMaterial::getMeetingId, meetingId)
                            .eq(ArchiveMaterial::getFileUrl, att.getFileUrl())
                            .eq(ArchiveMaterial::getCategory, "courseware")
                            .eq(ArchiveMaterial::getDeleted, 0)
            );
            if (exists > 0) {
                continue; // 已同步，跳过
            }

            ArchiveMaterial material = ArchiveMaterial.builder()
                    .meetingId(meetingId)
                    .tenantId(tenantId)
                    .category("courseware")
                    .subCategory(att.getFileType())
                    .title(att.getFileName())
                    .fileUrl(att.getFileUrl())
                    .fileSize(att.getFileSize() != null ? String.valueOf(att.getFileSize()) : null)
                    .fileType(att.getFileType())
                    .uploadBy(att.getUploadBy())
                    .uploadTime(att.getUploadTime())
                    .downloadCount(att.getDownloadCount() != null ? att.getDownloadCount() : 0)
                    .sortOrder(0)
                    .deleted(0)
                    .createTime(LocalDateTime.now())
                    .build();
            materialMapper.insert(material);
            synced++;
        }
        log.info("同步日程附件→归档资料, meetingId={}, synced={}/{}", meetingId, synced, attachments.size());
        return synced;
    }

    /**
     * 同步手册讨论 → 归档消息群组 + 消息
     * conf_handbook → conf_archive_message_group
     * conf_handbook_discussion → conf_archive_message
     * 注意：Handbook/HandbookDiscussion 在 conference-registration 模块，使用 JdbcTemplate 直接查询
     */
    private Map<String, Integer> syncHandbookDiscussions(Long meetingId, Long tenantId) {
        Map<String, Integer> result = new HashMap<>();
        result.put("groups", 0);
        result.put("messages", 0);

        // 查询该会议的手册
        List<Map<String, Object>> handbooks = jdbcTemplate.queryForList(
                "SELECT id, title, create_time FROM conf_handbook WHERE meeting_id = ? AND (deleted = 0 OR deleted IS NULL)",
                meetingId
        );

        if (handbooks.isEmpty()) {
            log.info("会议{}无手册可同步", meetingId);
            return result;
        }

        int groupsSynced = 0;
        int messagesSynced = 0;

        for (Map<String, Object> hb : handbooks) {
            Long handbookId = ((Number) hb.get("id")).longValue();
            String title = (String) hb.get("title");
            if (title == null || title.isBlank()) {
                title = "手册讨论";
            }

            // 去重：按 groupName 判断是否已同步
            long groupExists = messageGroupMapper.selectCount(
                    new LambdaQueryWrapper<ArchiveMessageGroup>()
                            .eq(ArchiveMessageGroup::getMeetingId, meetingId)
                            .eq(ArchiveMessageGroup::getGroupName, title)
                            .eq(ArchiveMessageGroup::getDeleted, 0)
            );

            Long archiveGroupId;
            if (groupExists > 0) {
                // 已有群组，获取其ID用于追加消息
                ArchiveMessageGroup existingGroup = messageGroupMapper.selectOne(
                        new LambdaQueryWrapper<ArchiveMessageGroup>()
                                .eq(ArchiveMessageGroup::getMeetingId, meetingId)
                                .eq(ArchiveMessageGroup::getGroupName, title)
                                .eq(ArchiveMessageGroup::getDeleted, 0)
                                .last("LIMIT 1")
                );
                archiveGroupId = existingGroup.getId();
            } else {
                // 创建归档消息群组
                ArchiveMessageGroup group = ArchiveMessageGroup.builder()
                        .meetingId(meetingId)
                        .tenantId(tenantId)
                        .groupName(title)
                        .messageCount(0)
                        .lastActiveTime(LocalDateTime.now())
                        .deleted(0)
                        .createTime(LocalDateTime.now())
                        .build();
                messageGroupMapper.insert(group);
                archiveGroupId = group.getId();
                groupsSynced++;
            }

            // 查询该手册的讨论记录
            List<Map<String, Object>> discussions = jdbcTemplate.queryForList(
                    "SELECT id, content, reference, sort_order, create_time FROM conf_handbook_discussion WHERE handbook_id = ?",
                    handbookId
            );

            for (Map<String, Object> disc : discussions) {
                String content = (String) disc.get("content");
                if (content == null || content.isBlank()) continue;

                // 去重：按 content + groupId 判断
                long msgExists = messageMapper.selectCount(
                        new LambdaQueryWrapper<ArchiveMessage>()
                                .eq(ArchiveMessage::getGroupId, archiveGroupId)
                                .eq(ArchiveMessage::getMeetingId, meetingId)
                                .eq(ArchiveMessage::getContent, content)
                                .eq(ArchiveMessage::getDeleted, 0)
                );
                if (msgExists > 0) continue;

                Object createTimeObj = disc.get("create_time");
                LocalDateTime sendTime = createTimeObj instanceof java.sql.Timestamp ts
                        ? ts.toLocalDateTime() : LocalDateTime.now();

                ArchiveMessage msg = ArchiveMessage.builder()
                        .groupId(archiveGroupId)
                        .meetingId(meetingId)
                        .tenantId(tenantId)
                        .content(content)
                        .sender("手册讨论")
                        .sendTime(sendTime)
                        .messageType("text")
                        .deleted(0)
                        .createTime(LocalDateTime.now())
                        .build();
                messageMapper.insert(msg);
                messagesSynced++;
            }

            // 更新群组统计
            updateGroupStats(archiveGroupId);
        }

        result.put("groups", groupsSynced);
        result.put("messages", messagesSynced);
        log.info("同步手册讨论→归档消息, meetingId={}, groups={}, messages={}", meetingId, groupsSynced, messagesSynced);
        return result;
    }

    /**
     * 同步签到数据 → 归档业务数据
     * conf_schedule_checkin + conf_schedule → conf_archive_business_data (dataType=checkin_rate)
     * 按日程聚合签到率
     */
    private int syncCheckinData(Long meetingId, Long tenantId) {
        // 查询该会议的所有日程
        List<Map<String, Object>> schedules = jdbcTemplate.queryForList(
                "SELECT id, title, start_time FROM conf_schedule WHERE meeting_id = ? AND (deleted = 0 OR deleted IS NULL) ORDER BY sort_order, start_time",
                meetingId
        );

        if (schedules.isEmpty()) {
            log.info("会议{}无日程可同步签到数据", meetingId);
            return 0;
        }

        // 查询该会议的总参会人数（用报名人数估算）
        Integer totalParticipants;
        try {
            totalParticipants = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM conf_registration WHERE meeting_id = ? AND (deleted = 0 OR deleted IS NULL)",
                    Integer.class, meetingId
            );
        } catch (Exception e) {
            totalParticipants = 0;
            log.warn("查询会议参会人数失败, meetingId={}, 使用默认值0", meetingId);
        }
        if (totalParticipants == null || totalParticipants == 0) {
            log.info("会议{}无参会人员，跳过签到数据同步", meetingId);
            return 0;
        }

        int synced = 0;
        for (Map<String, Object> schedule : schedules) {
            Long scheduleId = ((Number) schedule.get("id")).longValue();
            String title = (String) schedule.get("title");
            Object startTimeObj = schedule.get("start_time");
            String dateTimeStr = startTimeObj != null ? startTimeObj.toString() : "";

            // 统计该日程的签到人数
            Integer checkinCount;
            try {
                checkinCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(DISTINCT participant_id) FROM conf_schedule_checkin WHERE schedule_id = ? AND meeting_id = ? AND (deleted = 0 OR deleted IS NULL)",
                        Integer.class, scheduleId, meetingId
                );
            } catch (Exception e) {
                checkinCount = 0;
            }
            if (checkinCount == null) checkinCount = 0;

            // 只同步有签到记录的日程
            if (checkinCount == 0) continue;

            // 去重：按 dataType + dataLabel 判断
            long exists = businessDataMapper.selectCount(
                    new LambdaQueryWrapper<ArchiveBusinessData>()
                            .eq(ArchiveBusinessData::getMeetingId, meetingId)
                            .eq(ArchiveBusinessData::getDataType, "checkin_rate")
                            .eq(ArchiveBusinessData::getDataLabel, title)
                            .eq(ArchiveBusinessData::getDeleted, 0)
            );
            if (exists > 0) continue;

            java.math.BigDecimal rate = java.math.BigDecimal.valueOf(checkinCount * 100.0 / totalParticipants)
                    .setScale(1, java.math.RoundingMode.HALF_UP);

            ArchiveBusinessData data = ArchiveBusinessData.builder()
                    .meetingId(meetingId)
                    .tenantId(tenantId)
                    .dataType("checkin_rate")
                    .dataLabel(title)
                    .dataDatetime(dateTimeStr)
                    .expectedCount(totalParticipants)
                    .actualCount(checkinCount)
                    .rate(rate)
                    .sortOrder(synced)
                    .deleted(0)
                    .createTime(LocalDateTime.now())
                    .build();
            businessDataMapper.insert(data);
            synced++;
        }
        log.info("同步签到数据→归档业务数据, meetingId={}, synced={}", meetingId, synced);
        return synced;
    }

    /**
     * 同步聊天群组消息 → 归档消息群组 + 消息
     * conference_collaboration.chat_group → conf_archive_message_group
     * conference_collaboration.chat_message → conf_archive_message
     * 注意：chat_group 使用 conference_id 字段关联会议
     */
    private Map<String, Integer> syncChatMessages(Long meetingId, Long tenantId) {
        Map<String, Integer> result = new HashMap<>();
        result.put("groups", 0);
        result.put("messages", 0);

        List<Map<String, Object>> chatGroups;
        try {
            chatGroups = jdbcTemplate.queryForList(
                    "SELECT id, group_name, member_count, create_time FROM conference_collaboration.chat_group " +
                    "WHERE conference_id = ? AND (deleted = 0 OR deleted IS NULL) AND status = 1",
                    meetingId
            );
        } catch (Exception e) {
            log.warn("查询聊天群组失败(跨库), meetingId={}: {}", meetingId, e.getMessage());
            return result;
        }

        if (chatGroups.isEmpty()) {
            log.info("会议{}无聊天群组可同步", meetingId);
            return result;
        }

        int groupsSynced = 0;
        int messagesSynced = 0;

        for (Map<String, Object> cg : chatGroups) {
            Long chatGroupId = ((Number) cg.get("id")).longValue();
            String groupName = (String) cg.get("group_name");
            if (groupName == null || groupName.isBlank()) {
                groupName = "聊天群组";
            }

            // 去重：按 groupName 判断
            long groupExists = messageGroupMapper.selectCount(
                    new LambdaQueryWrapper<ArchiveMessageGroup>()
                            .eq(ArchiveMessageGroup::getMeetingId, meetingId)
                            .eq(ArchiveMessageGroup::getGroupName, groupName)
                            .eq(ArchiveMessageGroup::getDeleted, 0)
            );

            Long archiveGroupId;
            if (groupExists > 0) {
                ArchiveMessageGroup existingGroup = messageGroupMapper.selectOne(
                        new LambdaQueryWrapper<ArchiveMessageGroup>()
                                .eq(ArchiveMessageGroup::getMeetingId, meetingId)
                                .eq(ArchiveMessageGroup::getGroupName, groupName)
                                .eq(ArchiveMessageGroup::getDeleted, 0)
                                .last("LIMIT 1")
                );
                archiveGroupId = existingGroup.getId();
            } else {
                ArchiveMessageGroup group = ArchiveMessageGroup.builder()
                        .meetingId(meetingId)
                        .tenantId(tenantId)
                        .groupName(groupName)
                        .messageCount(0)
                        .lastActiveTime(LocalDateTime.now())
                        .deleted(0)
                        .createTime(LocalDateTime.now())
                        .build();
                messageGroupMapper.insert(group);
                archiveGroupId = group.getId();
                groupsSynced++;
            }

            // 查询该群组的聊天消息
            List<Map<String, Object>> chatMessages;
            try {
                chatMessages = jdbcTemplate.queryForList(
                        "SELECT id, sender_name, msg_type, content, create_time FROM conference_collaboration.chat_message " +
                        "WHERE group_id = ? AND (deleted = 0 OR deleted IS NULL) ORDER BY create_time",
                        chatGroupId
                );
            } catch (Exception e) {
                log.warn("查询聊天消息失败, chatGroupId={}: {}", chatGroupId, e.getMessage());
                continue;
            }

            for (Map<String, Object> cm : chatMessages) {
                String content = (String) cm.get("content");
                if (content == null || content.isBlank()) continue;
                String senderName = (String) cm.get("sender_name");
                if (senderName == null || senderName.isBlank()) senderName = "未知";

                // 去重：按 content + sender + groupId
                long msgExists = messageMapper.selectCount(
                        new LambdaQueryWrapper<ArchiveMessage>()
                                .eq(ArchiveMessage::getGroupId, archiveGroupId)
                                .eq(ArchiveMessage::getMeetingId, meetingId)
                                .eq(ArchiveMessage::getContent, content)
                                .eq(ArchiveMessage::getSender, senderName)
                                .eq(ArchiveMessage::getDeleted, 0)
                );
                if (msgExists > 0) continue;

                Object createTimeObj = cm.get("create_time");
                LocalDateTime sendTime = createTimeObj instanceof java.sql.Timestamp ts
                        ? ts.toLocalDateTime() : LocalDateTime.now();
                String msgType = (String) cm.get("msg_type");
                if (msgType == null) msgType = "text";

                ArchiveMessage msg = ArchiveMessage.builder()
                        .groupId(archiveGroupId)
                        .meetingId(meetingId)
                        .tenantId(tenantId)
                        .content(content)
                        .sender(senderName)
                        .sendTime(sendTime)
                        .messageType(msgType)
                        .deleted(0)
                        .createTime(LocalDateTime.now())
                        .build();
                messageMapper.insert(msg);
                messagesSynced++;
            }

            updateGroupStats(archiveGroupId);
        }

        result.put("groups", groupsSynced);
        result.put("messages", messagesSynced);
        log.info("同步聊天消息→归档消息, meetingId={}, groups={}, messages={}", meetingId, groupsSynced, messagesSynced);
        return result;
    }

    /**
     * 同步报到率数据 → 归档业务数据
     * conf_registration (按 organization 聚合) → conf_archive_business_data (dataType=report_rate)
     * status=1 表示已审核通过，sign_in_time 非空表示已报到
     */
    private int syncReportRateData(Long meetingId, Long tenantId) {
        List<Map<String, Object>> orgStats;
        try {
            orgStats = jdbcTemplate.queryForList(
                    "SELECT COALESCE(organization, '未知单位') AS org_name, " +
                    "COUNT(*) AS total, " +
                    "SUM(CASE WHEN sign_in_time IS NOT NULL THEN 1 ELSE 0 END) AS reported " +
                    "FROM conf_registration WHERE meeting_id = ? AND (deleted = 0 OR deleted IS NULL) " +
                    "GROUP BY organization ORDER BY organization",
                    meetingId
            );
        } catch (Exception e) {
            log.warn("查询报到率数据失败, meetingId={}: {}", meetingId, e.getMessage());
            return 0;
        }

        if (orgStats.isEmpty()) {
            log.info("会议{}无报名数据可同步报到率", meetingId);
            return 0;
        }

        // 检查是否已同步过报到率数据
        long existingCount = businessDataMapper.selectCount(
                new LambdaQueryWrapper<ArchiveBusinessData>()
                        .eq(ArchiveBusinessData::getMeetingId, meetingId)
                        .eq(ArchiveBusinessData::getDataType, "report_rate")
                        .eq(ArchiveBusinessData::getDeleted, 0)
        );
        if (existingCount > 0) {
            // 已有报到率数据，先清除旧数据再重新同步（报到率是动态变化的）
            businessDataMapper.update(null, new LambdaUpdateWrapper<ArchiveBusinessData>()
                    .eq(ArchiveBusinessData::getMeetingId, meetingId)
                    .eq(ArchiveBusinessData::getDataType, "report_rate")
                    .eq(ArchiveBusinessData::getDeleted, 0)
                    .set(ArchiveBusinessData::getDeleted, 1));
        }

        int synced = 0;
        for (Map<String, Object> os : orgStats) {
            String orgName = (String) os.get("org_name");
            int total = ((Number) os.get("total")).intValue();
            int reported = ((Number) os.get("reported")).intValue();
            java.math.BigDecimal rate = total > 0
                    ? java.math.BigDecimal.valueOf(reported * 100.0 / total).setScale(1, java.math.RoundingMode.HALF_UP)
                    : java.math.BigDecimal.ZERO;

            ArchiveBusinessData data = ArchiveBusinessData.builder()
                    .meetingId(meetingId)
                    .tenantId(tenantId)
                    .dataType("report_rate")
                    .dataLabel(orgName)
                    .expectedCount(total)
                    .actualCount(reported)
                    .rate(rate)
                    .sortOrder(synced)
                    .deleted(0)
                    .createTime(LocalDateTime.now())
                    .build();
            businessDataMapper.insert(data);
            synced++;
        }
        log.info("同步报到率→归档业务数据, meetingId={}, synced={}", meetingId, synced);
        return synced;
    }

    // ==================== 清空 ====================

    @Override
    @Transactional
    public Boolean clearAllArchive(Long meetingId) {
        validateMeetingAccess(meetingId);
        Long tenantId = requireTenantId();

        // 检查配置：必须已打包下载才能清空
        ArchiveConfig config = getOrCreateConfig(meetingId);
        if (!Boolean.TRUE.equals(config.getIsPacked())) {
            throw new BusinessException("请先完成打包下载后再进行清空操作");
        }

        // 逻辑删除所有资料
        materialMapper.update(null, new LambdaUpdateWrapper<ArchiveMaterial>()
                .eq(ArchiveMaterial::getMeetingId, meetingId)
                .eq(ArchiveMaterial::getDeleted, 0)
                .set(ArchiveMaterial::getDeleted, 1));

        // 逻辑删除所有业务数据
        businessDataMapper.update(null, new LambdaUpdateWrapper<ArchiveBusinessData>()
                .eq(ArchiveBusinessData::getMeetingId, meetingId)
                .eq(ArchiveBusinessData::getDeleted, 0)
                .set(ArchiveBusinessData::getDeleted, 1));

        // 逻辑删除所有消息
        messageMapper.update(null, new LambdaUpdateWrapper<ArchiveMessage>()
                .eq(ArchiveMessage::getMeetingId, meetingId)
                .eq(ArchiveMessage::getDeleted, 0)
                .set(ArchiveMessage::getDeleted, 1));

        // 逻辑删除所有群组
        messageGroupMapper.update(null, new LambdaUpdateWrapper<ArchiveMessageGroup>()
                .eq(ArchiveMessageGroup::getMeetingId, meetingId)
                .eq(ArchiveMessageGroup::getDeleted, 0)
                .set(ArchiveMessageGroup::getDeleted, 1));

        log.info("清空会议归档数据, meetingId={}, tenantId={}", meetingId, tenantId);
        return true;
    }

    // ==================== 打包 ====================

    @Override
    @Transactional
    public void markAsPacked(Long meetingId) {
        validateMeetingAccess(meetingId);
        ArchiveConfig config = getOrCreateConfig(meetingId);
        config.setIsPacked(true);
        config.setPackTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        configMapper.updateById(config);
        log.info("标记归档已打包, meetingId={}", meetingId);
    }

    // ==================== 导出 ====================

    @Override
    public String exportBusinessDataCsv(Long meetingId, String dataType) {
        validateMeetingAccess(meetingId);
        List<ArchiveBusinessData> dataList = getBusinessData(meetingId, dataType);

        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF"); // BOM for Excel UTF-8

        switch (dataType != null ? dataType : "") {
            case "report_rate":
                csv.append("部门/单位,应报到,已报到,未报到,报到率(%)\n");
                for (ArchiveBusinessData d : dataList) {
                    csv.append(escapeCsv(d.getDataLabel())).append(",")
                            .append(d.getExpectedCount()).append(",")
                            .append(d.getActualCount()).append(",")
                            .append(d.getExpectedCount() - d.getActualCount()).append(",")
                            .append(d.getRate()).append("\n");
                }
                break;
            case "checkin_rate":
                csv.append("日程名称,日期时间,应签到,已签到,缺席,签到率(%)\n");
                for (ArchiveBusinessData d : dataList) {
                    csv.append(escapeCsv(d.getDataLabel())).append(",")
                            .append(escapeCsv(d.getDataDatetime())).append(",")
                            .append(d.getExpectedCount()).append(",")
                            .append(d.getActualCount()).append(",")
                            .append(d.getExpectedCount() - d.getActualCount()).append(",")
                            .append(d.getRate()).append("\n");
                }
                break;
            case "dormitory_rate":
                csv.append("日期,应就寝,已就寝,未就寝,就寝率(%)\n");
                for (ArchiveBusinessData d : dataList) {
                    csv.append(escapeCsv(d.getDataLabel())).append(",")
                            .append(d.getExpectedCount()).append(",")
                            .append(d.getActualCount()).append(",")
                            .append(d.getExpectedCount() - d.getActualCount()).append(",")
                            .append(d.getRate()).append("\n");
                }
                break;
            default:
                csv.append("标签,时间,应到,实到,比率(%)\n");
                for (ArchiveBusinessData d : dataList) {
                    csv.append(escapeCsv(d.getDataLabel())).append(",")
                            .append(escapeCsv(d.getDataDatetime())).append(",")
                            .append(d.getExpectedCount()).append(",")
                            .append(d.getActualCount()).append(",")
                            .append(d.getRate()).append("\n");
                }
        }
        return csv.toString();
    }

    @Override
    public String exportMessagesCsv(Long meetingId, Long groupId) {
        validateMeetingAccess(meetingId);

        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF");
        csv.append("发送者,发送时间,消息内容\n");

        if (groupId != null) {
            List<ArchiveMessage> messages = getGroupMessages(meetingId, groupId, null);
            for (ArchiveMessage msg : messages) {
                csv.append(escapeCsv(msg.getSender())).append(",")
                        .append(msg.getSendTime() != null ? msg.getSendTime().toString() : "").append(",")
                        .append(escapeCsv(msg.getContent())).append("\n");
            }
        } else {
            // 导出所有群组消息
            List<ArchiveMessageGroup> groups = messageGroupMapper.selectList(
                    new LambdaQueryWrapper<ArchiveMessageGroup>()
                            .eq(ArchiveMessageGroup::getMeetingId, meetingId)
                            .eq(ArchiveMessageGroup::getDeleted, 0));
            csv = new StringBuilder();
            csv.append("\uFEFF");
            csv.append("群组名称,发送者,发送时间,消息内容\n");
            for (ArchiveMessageGroup group : groups) {
                List<ArchiveMessage> messages = getGroupMessages(meetingId, group.getId(), null);
                for (ArchiveMessage msg : messages) {
                    csv.append(escapeCsv(group.getGroupName())).append(",")
                            .append(escapeCsv(msg.getSender())).append(",")
                            .append(msg.getSendTime() != null ? msg.getSendTime().toString() : "").append(",")
                            .append(escapeCsv(msg.getContent())).append("\n");
                }
            }
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
