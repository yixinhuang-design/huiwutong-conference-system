package com.conference.group.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.conference.group.dto.GroupCreateRequest;
import com.conference.group.dto.GroupTaskRequest;
import com.conference.group.dto.GroupUpdateRequest;
import com.conference.group.entity.ConferenceGroup;
import com.conference.group.entity.GroupMember;
import com.conference.group.entity.GroupMessage;
import com.conference.group.entity.GroupTask;
import com.conference.group.mapper.GroupMapper;
import com.conference.group.mapper.GroupMemberMapper;
import com.conference.group.mapper.GroupMessageMapper;
import com.conference.group.mapper.GroupTaskMapper;
import com.conference.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 群组管理服务实现
 * 使用Web Key进行信息持久化
 * @author AI Executive
 * @date 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    
    private final GroupMapper groupMapper;
    private final GroupMemberMapper memberMapper;
    private final GroupTaskMapper taskMapper;
    private final GroupMessageMapper messageMapper;
    
    @Override
    @Transactional
    public ConferenceGroup createGroup(GroupCreateRequest request) {
        ConferenceGroup group = new ConferenceGroup();
        group.setConferenceId(request.getConferenceId());
        group.setGroupName(request.getGroupName());
        group.setGroupType(request.getGroupType());
        
        // 生成Web Key（用于前端识别）
        String webKey = "group_" + IdUtil.simpleUUID().substring(0, 16);
        group.setWebKey(webKey);
        
        // 初始化统计数据
        group.setMemberCount(0);
        group.setAdminCount(0);
        group.setTaskCount(0);
        group.setMessageCount(0);
        group.setStatus("active");
        
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        group.setCreatorId(request.getCreatorId());
        
        groupMapper.insert(group);
        
        log.info("创建群组成功: webKey={}, name={}", webKey, group.getGroupName());
        return group;
    }
    
    @Override
    @Transactional
    public ConferenceGroup updateGroup(String webKey, GroupUpdateRequest request) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        if (request.getGroupName() != null) {
            group.setGroupName(request.getGroupName());
        }
        if (request.getAnnouncement() != null) {
            group.setAnnouncement(request.getAnnouncement());
        }
        if (request.getAvatarUrl() != null) {
            group.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getWelcomeMessage() != null) {
            group.setWelcomeMessage(request.getWelcomeMessage());
        }
        if (request.getQrCodeUrl() != null) {
            group.setQrCodeUrl(request.getQrCodeUrl());
        }
        
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        log.info("更新群组成功: webKey={}", webKey);
        return group;
    }
    
    @Override
    @Transactional
    public void deleteGroup(String webKey) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        // 软删除
        group.setStatus("deleted");
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        log.info("删除群组成功: webKey={}", webKey);
    }
    
    @Override
    public Map<String, Object> getGroupDetail(String webKey) {
        Map<String, Object> detail = new HashMap<>();
        
        // 获取群组信息
        ConferenceGroup group = findByWebKey(webKey);
        detail.put("group", group);
        
        // 获取成员列表
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, group.getId())
               .orderByAsc(GroupMember::getJoinTime);
        List<GroupMember> members = memberMapper.selectList(wrapper);
        detail.put("members", members);
        
        // 统计信息
        long adminCount = members.stream().filter(m -> "admin".equals(m.getRole())).count();
        detail.put("adminCount", adminCount);
        
        // 获取任务列表
        LambdaQueryWrapper<GroupTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(GroupTask::getGroupId, group.getId())
                .orderByDesc(GroupTask::getCreateTime);
        List<GroupTask> tasks = taskMapper.selectList(taskWrapper);
        detail.put("tasks", tasks);
        
        // 获取最近消息
        LambdaQueryWrapper<GroupMessage> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.eq(GroupMessage::getGroupId, group.getId())
               .orderByDesc(GroupMessage::getSendTime)
               .last("50");
        List<GroupMessage> messages = messageMapper.selectList(msgWrapper);
        detail.put("recentMessages", messages);
        
        return detail;
    }
    
    @Override
    public List<ConferenceGroup> getGroupsByConference(Long conferenceId) {
        LambdaQueryWrapper<ConferenceGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConferenceGroup::getConferenceId, conferenceId)
               .ne(ConferenceGroup::getStatus, "deleted")
               .orderByDesc(ConferenceGroup::getCreateTime);
        return groupMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional
    public void addMembers(String webKey, List<Long> memberIds) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        int addedCount = 0;
        for (Long memberId : memberIds) {
            // 检查是否已存在
            LambdaQueryWrapper<GroupMember> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(GroupMember::getGroupId, group.getId())
                     .eq(GroupMember::getMemberId, memberId);
            long count = memberMapper.selectCount(checkWrapper);
            
            if (count == 0) {
                GroupMember member = new GroupMember();
                member.setGroupId(group.getId());
                member.setMemberId(memberId);
                member.setRole("member");
                member.setJoinTime(LocalDateTime.now());
                member.setWebKey("mem_" + IdUtil.simpleUUID().substring(0, 16));
                
                memberMapper.insert(member);
                addedCount++;
            }
        }
        
        // 更新成员数量
        group.setMemberCount(group.getMemberCount() + addedCount);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        log.info("添加成员成功: webKey={}, 新增={}", webKey, addedCount);
    }
    
    @Override
    @Transactional
    public void removeMember(String webKey, String memberWebKey) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        // 查找成员
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, group.getId())
               .eq(GroupMember::getWebKey, memberWebKey);
        
        GroupMember member = memberMapper.selectOne(wrapper);
        if (member != null) {
            memberMapper.deleteById(member.getId());
            
            // 更新成员数量
            group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
            group.setUpdateTime(LocalDateTime.now());
            groupMapper.updateById(group);
            
            log.info("移除成员成功: webKey={}, memberWebKey={}", webKey, memberWebKey);
        }
    }
    
    @Override
    @Transactional
    public void setAdmins(String webKey, List<String> adminWebKeys) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        // 将所有成员的role更新为admin
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(GroupMember::getWebKey, adminWebKeys);
        
        List<GroupMember> members = memberMapper.selectList(wrapper);
        members.forEach(m -> {
            m.setRole("admin");
            memberMapper.updateById(m);
        });
        
        // 统计管理员数量
        long adminCount = members.stream()
                .filter(m -> adminWebKeys.contains(m.getWebKey()))
                .count();
        
        group.setAdminCount((int) adminCount);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        log.info("设置管理员成功: webKey={}, 管理员数={}", webKey, adminCount);
    }
    
    @Override
    @Transactional
    public Map<String, Object> sendMessage(String webKey, String content, String messageType) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        // 创建消息记录
        GroupMessage message = new GroupMessage();
        message.setGroupId(group.getId());
        message.setMessageType(messageType);
        message.setContent(content);
        message.setSenderId(group.getCreatorId());
        message.setSenderName("系统管理员");
        message.setIsBot(true);
        message.setSendTime(LocalDateTime.now());
        message.setWebKey("msg_" + IdUtil.simpleUUID().substring(0, 16));
        
        messageMapper.insert(message);
        
        // 更新消息数量
        group.setMessageCount(group.getMessageCount() + 1);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        // TODO: 调用微信企业群API发送消息
        
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", message.getId());
        result.put("webKey", message.getWebKey());
        result.put("sendTime", message.getSendTime());
        
        log.info("发送消息成功: webKey={}, 消息类型={}", webKey, messageType);
        return result;
    }
    
    @Override
    public List<GroupMessage> getMessages(String webKey, int page, int pageSize) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            return Collections.emptyList();
        }
        
        // 分页查询
        int offset = (page - 1) * pageSize;
        
        LambdaQueryWrapper<GroupMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessage::getGroupId, group.getId())
               .orderByDesc(GroupMessage::getSendTime)
               .last(pageSize);
        
        return messageMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional
    public GroupTask createTask(String webKey, GroupTaskRequest request) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        
        GroupTask task = new GroupTask();
        task.setGroupId(group.getId());
        task.setTaskName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setTaskType(request.getTaskType());
        task.setStatus("pending");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "medium");
        
        // 处理指派人员
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            task.setAssigneeIds(String.join(",", request.getAssigneeIds()));
        }
        
        task.setDeadline(request.getDeadline());
        task.setProgress(0);
        task.setTotalTarget(request.getTotalTarget() != null ? request.getTotalTarget() : 0);
        task.setCompletedCount(0);
        
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        task.setReminderTime(request.getReminderTime());
        task.setWebKey("task_" + IdUtil.simpleUUID().substring(0, 16));
        
        taskMapper.insert(task);
        
        // 更新任务数量
        group.setTaskCount(group.getTaskCount() + 1);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        // TODO: 发送任务提醒（集成精准通知系统）
        
        log.info("创建任务成功: webKey={}, 任务名={}", webKey, task.getTaskName());
        return task;
    }
    
    @Override
    @Transactional
    public GroupTask updateTaskStatus(String webKey, String taskWebKey, String status, Integer progress, Integer completedCount) {
        // 查找任务
        LambdaQueryWrapper<GroupTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupTask::getWebKey, taskWebKey);
        
        GroupTask task = taskMapper.selectOne(wrapper);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        // 更新状态
        if (status != null) {
            task.setStatus(status);
            if ("completed".equals(status)) {
                task.setCompletedTime(LocalDateTime.now());
            }
        }
        
        if (progress != null) {
            task.setProgress(progress);
        }
        
        if (completedCount != null) {
            task.setCompletedCount(completedCount);
        }
        
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        
        // 更新群组任务数量统计
        
        log.info("更新任务状态成功: webKey={}, 状态={}, 进度={}%", webKey, status, progress);
        return task;
    }
    
    @Override
public List<GroupTask> getTasksByGroup(String webKey) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<GroupTask> wrapper = newError LambdaQueryWrapper<>();
        wrapper.eq(GroupTask::getGroupId, group.getId())
               .orderByDesc(GroupTask::getCreateTime);
        
        return taskMapper.selectList(wrapper);
    }
    
    @Override
    public Map<String, Object> getGroupStats(String webKey) {
        ConferenceGroup group = findByWebKey(webKey);
        if (group == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("memberCount", group.getMemberCount());
        stats.put("adminCount", group.getAdminCount());
        stats.put("taskCount", group.getTaskCount());
        stats.put("messageCount", group.getMessageCount());
        stats.put("status", group.getStatus());
        
        // 计算任务完成率
        List<GroupTask> tasks = getTasksByGroup(webKey);
        if (!tasks.isEmpty()) {
            long completedTasks = tasks.stream().filter(t -> "completed".equals(t.getStatus())).count();
            stats.put("taskCompletionRate", (completedTasks * 100.0 / tasks.size()));
        } else {
            stats.put("taskCompletionRate", 0.0);
        }
        
        // 成员活跃度
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getGroupId, group.getId())
                   .orderByDesc(GroupMember::getLastActiveTime)
                   .last(7);
        long activeMembers = memberWrapper.selectCount(memberWrapper);
        stats.put("activeMembers", activeMembers);
        
        return stats;
    }
    
    @Override
    public ConferenceGroup findByWebKey(String webKey) {
        LambdaQueryWrapper<ConferenceGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConferenceGroup::getWebKey, webKey)
               .ne(ConferenceGroup::getStatus, "deleted");
        
        return groupMapper.selectOne(wrapper);
    }
}
