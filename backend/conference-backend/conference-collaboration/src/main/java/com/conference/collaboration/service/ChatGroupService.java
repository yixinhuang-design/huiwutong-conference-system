package com.conference.collaboration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.collaboration.entity.ChatGroup;
import com.conference.collaboration.entity.ChatGroupMember;

import java.util.List;
import java.util.Map;

public interface ChatGroupService extends IService<ChatGroup> {

    /** 创建群组 */
    ChatGroup createGroup(ChatGroup group);

    /** 获取会议的群组列表 */
    List<ChatGroup> listByConference(Long conferenceId);

    /** 获取用户加入的群组 */
    List<Map<String, Object>> listMyGroups(Long userId, Long conferenceId);

    /** 获取群组详情(含成员列表) */
    Map<String, Object> getGroupDetail(Long groupId);

    /** 添加成员 */
    void addMember(Long groupId, ChatGroupMember member);

    /** 移除成员 */
    void removeMember(Long groupId, Long userId);

    /** 获取群成员列表 */
    List<ChatGroupMember> getMembers(Long groupId);

    /** 更新群设置 */
    void updateSettings(Long groupId, Map<String, Object> settings);

    /** 删除群组 */
    boolean deleteGroup(Long groupId);

    /** 更新成员角色 */
    void updateMemberRole(Long groupId, Long userId, String role);

    /** 群组统计 */
    Map<String, Object> getStats(Long conferenceId);
}
