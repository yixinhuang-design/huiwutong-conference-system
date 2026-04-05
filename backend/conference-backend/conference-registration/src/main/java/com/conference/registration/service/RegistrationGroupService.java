package com.conference.registration.service;

import com.conference.registration.dto.GroupRequest;
import com.conference.registration.entity.RegistrationGroup;

import java.util.List;
import java.util.Map;

/**
 * 分组管理服务接口
 */
public interface RegistrationGroupService {
    
    /**
     * 获取会议的所有分组
     */
    List<RegistrationGroup> getGroupsByConference(Long conferenceId);
    
    /**
     * 获取分组详情（包含成员）
     */
    Map<String, Object> getGroupDetail(Long groupId);
    
    /**
     * 创建分组
     */
    RegistrationGroup createGroup(GroupRequest request);
    
    /**
     * 更新分组
     */
    RegistrationGroup updateGroup(Long id, GroupRequest request);
    
    /**
     * 删除分组
     */
    void deleteGroup(Long id);
    
    /**
     * 分配成员到分组
     */
    void assignMembers(Long groupId, List<Long> memberIds);
    
    /**
     * 移除分组成员
     */
    void removeMember(Long groupId, Long memberId);
    
    /**
     * 指定工作人员
     */
    void assignStaff(Long groupId, List<Long> staffIds);
    
    /**
     * 自动分组
     */
    List<RegistrationGroup> autoGroup(Long conferenceId, String criteria);
}
