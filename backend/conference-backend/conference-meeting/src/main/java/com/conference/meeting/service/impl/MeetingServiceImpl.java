package com.conference.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.common.exception.BusinessException;
import com.conference.meeting.entity.Meeting;
import com.conference.meeting.mapper.MeetingMapper;
import com.conference.meeting.service.IMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class MeetingServiceImpl extends ServiceImpl<MeetingMapper, Meeting> implements IMeetingService {

    @Override
    @Transactional
    public Meeting createMeeting(Meeting meeting) {
        // 获取当前租户ID
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 基础参数校验
        if (!StringUtils.hasText(meeting.getMeetingCode())) {
            throw new BusinessException("会议编码不能为空");
        }
        if (!StringUtils.hasText(meeting.getMeetingName())) {
            throw new BusinessException("会议名称不能为空");
        }

        // 设置租户ID
        meeting.setTenantId(tenantId);

        // 兜底处理封面字段，避免超长base64触发数据库字段长度异常
        if (StringUtils.hasText(meeting.getCoverImageUrl())) {
            String cover = meeting.getCoverImageUrl();
            if (cover.startsWith("data:image/") || cover.length() > 255) {
                log.warn("封面图片字段过长或为base64内联数据，已自动清空。meetingCode={}", meeting.getMeetingCode());
                meeting.setCoverImageUrl(null);
            }
        }

        // 验证会议代码唯一性
        if (!validateMeetingCodeUnique(tenantId, meeting.getMeetingCode(), null)) {
            throw new BusinessException("会议代码已存在，请检查");
        }

        // 设置初始状态为草稿
        if (meeting.getStatus() == null) {
            meeting.setStatus(0);
        }

        // 设置创建时间
        meeting.setCreateTime(LocalDateTime.now());

        // 保存会议信息
        if (this.save(meeting)) {
            log.info("会议创建成功，ID: {}, 租户ID: {}, 会议名称: {}", meeting.getId(), tenantId, meeting.getMeetingName());
            return meeting;
        } else {
            throw new BusinessException("会议创建失败，请重试");
        }
    }

    @Override
    public Meeting getMeetingDetail(Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Meeting meeting = this.getById(id);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 检查多租户隔离：确保会议属于当前租户
        if (!Objects.equals(meeting.getTenantId(), tenantId)) {
            log.warn("尝试访问其他租户的会议数据，ID: {}, 请求租户: {}, 会议租户: {}", id, tenantId, meeting.getTenantId());
            throw new BusinessException("无权访问该会议");
        }

        return meeting;
    }

    @Override
    public IPage<Meeting> getMeetingList(Page<Meeting> page, Long tenantId, String keyword, Integer status) {
        LambdaQueryWrapper<Meeting> wrapper = new LambdaQueryWrapper<Meeting>()
                .eq(Meeting::getTenantId, tenantId)
                .eq(Meeting::getDeleted, 0)
                .orderByDesc(Meeting::getCreateTime);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Meeting::getMeetingName, keyword)
                    .or()
                    .like(Meeting::getMeetingCode, keyword)
                    .or()
                    .like(Meeting::getVenueName, keyword)
            );
        }

        if (status != null) {
            wrapper.eq(Meeting::getStatus, status);
        }

        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public Boolean updateMeeting(Meeting meeting) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 获取原始记录进行权限检查
        Meeting original = this.getById(meeting.getId());
        if (original == null) {
            throw new BusinessException("会议不存在");
        }

        // 检查多租户隔离
        if (!Objects.equals(original.getTenantId(), tenantId)) {
            log.warn("尝试修改其他租户的会议数据，ID: {}", meeting.getId());
            throw new BusinessException("无权修改该会议");
        }

        // 更新时间
        meeting.setUpdateTime(LocalDateTime.now());

        // 如果更改了会议代码，需要验证唯一性
        if (!Objects.equals(original.getMeetingCode(), meeting.getMeetingCode())) {
            if (!validateMeetingCodeUnique(tenantId, meeting.getMeetingCode(), meeting.getId())) {
                throw new BusinessException("会议代码已存在，请检查");
            }
        }

        return this.updateById(meeting);
    }

    @Override
    @Transactional
    public Boolean deleteMeeting(Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Meeting meeting = this.getById(id);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 检查多租户隔离
        if (!Objects.equals(meeting.getTenantId(), tenantId)) {
            log.warn("尝试删除其他租户的会议数据，ID: {}", id);
            throw new BusinessException("无权删除该会议");
        }

        // 软删除：更新 deleted 标志
        Meeting updateMeeting = new Meeting();
        updateMeeting.setId(id);
        updateMeeting.setDeleted(1);
        updateMeeting.setUpdateTime(LocalDateTime.now());

        return this.updateById(updateMeeting);
    }

    @Override
    @Transactional
    public Boolean updateMeetingStatus(Long id, Integer status) {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Meeting meeting = this.getById(id);
        if (meeting == null) {
            throw new BusinessException("会议不存在");
        }

        // 检查多租户隔离
        if (!Objects.equals(meeting.getTenantId(), tenantId)) {
            throw new BusinessException("无权修改该会议");
        }

        // 验证状态转换逻辑
        Integer currentStatus = meeting.getStatus();
        if (!isValidStatusTransition(currentStatus, status)) {
            throw new BusinessException("非法的状态转换");
        }

        Meeting updateMeeting = new Meeting();
        updateMeeting.setId(id);
        updateMeeting.setStatus(status);
        updateMeeting.setUpdateTime(LocalDateTime.now());

        return this.updateById(updateMeeting);
    }

    @Override
    public MeetingStatistics getMeetingStatistics(Long tenantId) {
        LambdaQueryWrapper<Meeting> baseWrapper = new LambdaQueryWrapper<Meeting>()
                .eq(Meeting::getTenantId, tenantId)
                .eq(Meeting::getDeleted, 0);

        Integer totalMeetings = Math.toIntExact(this.count(baseWrapper));
        Integer draftMeetings = Math.toIntExact(this.count(baseWrapper.clone().eq(Meeting::getStatus, 0)));
        Integer regOpenMeetings = Math.toIntExact(this.count(baseWrapper.clone().eq(Meeting::getStatus, 1)));
        Integer ongoingMeetings = Math.toIntExact(this.count(baseWrapper.clone().eq(Meeting::getStatus, 2)));
        Integer completedMeetings = Math.toIntExact(this.count(baseWrapper.clone().eq(Meeting::getStatus, 3)));

        return new MeetingStatistics(totalMeetings, draftMeetings, regOpenMeetings, ongoingMeetings, completedMeetings);
    }

    @Override
    public List<Meeting> getOngoingMeetings(Long tenantId) {
        LambdaQueryWrapper<Meeting> wrapper = new LambdaQueryWrapper<Meeting>()
                .eq(Meeting::getTenantId, tenantId)
                .eq(Meeting::getStatus, 2)
                .eq(Meeting::getDeleted, 0)
                .orderByDesc(Meeting::getStartTime);

        return this.list(wrapper);
    }

    @Override
    public Boolean validateMeetingCodeUnique(Long tenantId, String code, Long excludeId) {
        LambdaQueryWrapper<Meeting> wrapper = new LambdaQueryWrapper<Meeting>()
                .eq(Meeting::getTenantId, tenantId)
                .eq(Meeting::getMeetingCode, code)
                .eq(Meeting::getDeleted, 0);

        if (excludeId != null) {
            wrapper.ne(Meeting::getId, excludeId);
        }

        return this.count(wrapper) == 0;
    }

    /**
     * 验证状态转换是否合法
     * 0: 草稿
     * 1: 报名中
     * 2: 进行中
     * 3: 已完成
     * 4: 已取消
     */
    private boolean isValidStatusTransition(Integer currentStatus, Integer newStatus) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        // 允许的状态转换
        return switch (currentStatus) {
            case 0 -> newStatus >= 1 && newStatus <= 4; // 草稿可以转换到任何状态
            case 1 -> newStatus == 2 || newStatus == 4;    // 报名中可以转到进行中或已取消
            case 2 -> newStatus == 3 || newStatus == 4;    // 进行中可以转到已完成或已取消
            case 3, 4 -> false;                             // 已完成/已取消不能再转换
            default -> false;
        };
    }
}
