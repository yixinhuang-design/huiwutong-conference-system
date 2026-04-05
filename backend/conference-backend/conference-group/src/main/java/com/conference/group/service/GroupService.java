package com.conference.group.service;

import com.conference.group.dto.GroupCreateRequest;
import com.conference.group.dto.GroupTaskRequest;
import com.conference.group.dto.GroupUpdateRequest;
import com.conference.group.entity.ConferenceGroup;
import com.conference.group.entity.GroupMessage;
import com.conference.group.entity.GroupTask;

import java.util.List;
import java.util.Map;

/**
 * 群组管理服务接口
 * @author AI Executive
 * @date 2026-03-24
 */
public interface GroupService {
    
    /**
     * 创建群组
     */
    ConferenceGroup createGroup(GroupCreateRequest request);
    
    /**
     * 更新群组
     */
    ConferenceGroup updateGroup(String webKey, GroupUpdateRequest request);
    
    /**
     * 删除群组
     */
    void deleteGroup(String webKey);
    
    /**
     * 获取群组详情
     */
    Map<String, Object> getGroupDetail(String webKey);
    
    /**
     * 获取会议的所有群组
     */
    List<ConferenceGroup> getGroupsByConference(Long conferenceId);
    
    /**
     * 添加成员
     */
    void addMembers(String webKey, List<Long> memberIds);
    
    /**
     * 移除成员
     */
    void removeMember(String webKey, String memberWebKey);
    
    /**
     * 设置管理员
     */
    void setAdmins(String webKey, List<String> adminWebKeys);
    
    /**
     * 发送消息
     */
    Map<String, Object> sendMessage(String webKey, String content, String messageType);
    
    /**
     * 获取消息列表
     */
    List<GroupMessage> getMessages(String webKey, int page, int pageSize);
    
    /**
     * 创建任务
     */
    GroupTask createTask(String webKey, GroupTaskRequest request);
    
    /**
     * 更新任务状态
     */
    GroupTask updateTaskStatus(String webKey, String taskWebKey, String status, Integer progress, Integer completedCount);
    
    /**
     * 获取任务列表
     */
    List<GroupTask> getTasksByGroup(String webKey);
    
    /**
     * 获取统计数据
     */
    Map<String, Object> getGroupStats(String webKey);
    
    /**
     * 根据webKey查找群组
     */
    ConferenceGroup findByWebKey(String webKey);
}
