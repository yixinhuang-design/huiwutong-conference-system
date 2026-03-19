package com.conference.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.collaboration.entity.ChatGroup;
import com.conference.collaboration.entity.ChatGroupMember;
import com.conference.collaboration.mapper.ChatGroupMapper;
import com.conference.collaboration.mapper.ChatGroupMemberMapper;
import com.conference.collaboration.service.ChatGroupService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup>
        implements ChatGroupService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final ChatGroupMapper groupMapper;
    private final ChatGroupMemberMapper memberMapper;

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    @Transactional
    public ChatGroup createGroup(ChatGroup group) {
        Long tenantId = getTenantId();
        group.setTenantId(tenantId);
        group.setMemberCount(group.getMemberCount() != null ? group.getMemberCount() : 1);
        group.setMaxMembers(group.getMaxMembers() != null ? group.getMaxMembers() : 500);
        group.setMuteAll(0);
        group.setAllowInvite(group.getAllowInvite() != null ? group.getAllowInvite() : 1);
        group.setAutoCreated(group.getAutoCreated() != null ? group.getAutoCreated() : 0);
        group.setStatus(1);
        group.setDeleted(0);
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.insert(group);

        // 创建者自动成为群主
        if (group.getOwnerId() != null) {
            ChatGroupMember owner = new ChatGroupMember();
            owner.setGroupId(group.getId());
            owner.setUserId(group.getOwnerId());
            owner.setUserName("群主");
            owner.setMemberRole("owner");
            owner.setIsMuted(0);
            owner.setJoinTime(LocalDateTime.now());
            memberMapper.insert(owner);
        }

        log.info("[租户{}] 创建群组成功: id={}, name={}", tenantId, group.getId(), group.getGroupName());
        return group;
    }

    @Override
    public List<ChatGroup> listByConference(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<ChatGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatGroup::getTenantId, tenantId)
                .eq(ChatGroup::getStatus, 1);
        if (conferenceId != null) {
            wrapper.eq(ChatGroup::getConferenceId, conferenceId);
        }
        wrapper.orderByDesc(ChatGroup::getCreateTime);
        return groupMapper.selectList(wrapper);
    }

    @Override
    public List<Map<String, Object>> listMyGroups(Long userId, Long conferenceId) {
        Long tenantId = getTenantId();
        // 先查用户所在的所有群
        LambdaQueryWrapper<ChatGroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ChatGroupMember::getUserId, userId);
        List<ChatGroupMember> memberships = memberMapper.selectList(memberWrapper);

        if (memberships.isEmpty()) {
            // 如果用户不在任何群，返回该会议下所有群组(管理员视角)
            List<ChatGroup> allGroups = listByConference(conferenceId);
            return allGroups.stream().map(g -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("group", g);
                map.put("myRole", "none");
                map.put("unreadCount", 0);
                return map;
            }).collect(Collectors.toList());
        }

        List<Long> groupIds = memberships.stream()
                .map(ChatGroupMember::getGroupId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ChatGroup> groupWrapper = new LambdaQueryWrapper<>();
        groupWrapper.eq(ChatGroup::getTenantId, tenantId)
                .eq(ChatGroup::getStatus, 1)
                .eq(ChatGroup::getDeleted, 0)
                .in(ChatGroup::getId, groupIds);
        if (conferenceId != null) {
            groupWrapper.eq(ChatGroup::getConferenceId, conferenceId);
        }
        groupWrapper.orderByDesc(ChatGroup::getCreateTime);
        List<ChatGroup> groups = groupMapper.selectList(groupWrapper);

        Map<Long, ChatGroupMember> memberMap = memberships.stream()
                .collect(Collectors.toMap(ChatGroupMember::getGroupId, m -> m, (a, b) -> a));

        return groups.stream().map(g -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("group", g);
            ChatGroupMember membership = memberMap.get(g.getId());
            map.put("myRole", membership != null ? membership.getMemberRole() : "member");
            map.put("unreadCount", 0);
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getGroupDetail(Long groupId) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            return null;
        }
        List<ChatGroupMember> members = getMembers(groupId);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("group", group);
        detail.put("members", members);
        detail.put("memberCount", members.size());
        return detail;
    }

    @Override
    @Transactional
    public void addMember(Long groupId, ChatGroupMember member) {
        // 检查是否已是成员
        LambdaQueryWrapper<ChatGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, member.getUserId());
        if (memberMapper.selectCount(wrapper) > 0) {
            log.warn("用户{}已是群组{}的成员", member.getUserId(), groupId);
            return;
        }

        member.setGroupId(groupId);
        member.setMemberRole(member.getMemberRole() != null ? member.getMemberRole() : "member");
        member.setIsMuted(0);
        member.setJoinTime(LocalDateTime.now());
        memberMapper.insert(member);

        // 更新群成员数
        ChatGroup group = groupMapper.selectById(groupId);
        if (group != null) {
            group.setMemberCount(group.getMemberCount() + 1);
            group.setUpdateTime(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        log.info("用户{}加入群组{}", member.getUserId(), groupId);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ChatGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId);
        memberMapper.delete(wrapper);

        // 更新群成员数
        ChatGroup group = groupMapper.selectById(groupId);
        if (group != null) {
            int count = group.getMemberCount() - 1;
            group.setMemberCount(count > 0 ? count : 0);
            group.setUpdateTime(LocalDateTime.now());
            groupMapper.updateById(group);
        }
        log.info("用户{}被移出群组{}", userId, groupId);
    }

    @Override
    public List<ChatGroupMember> getMembers(Long groupId) {
        LambdaQueryWrapper<ChatGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatGroupMember::getGroupId, groupId)
                .orderByAsc(ChatGroupMember::getJoinTime);
        return memberMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long groupId, Long userId, String role) {
        LambdaQueryWrapper<ChatGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId);
        ChatGroupMember member = memberMapper.selectOne(wrapper);
        if (member == null) {
            throw new RuntimeException("该用户不是群成员");
        }
        LambdaUpdateWrapper<ChatGroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatGroupMember::getGroupId, groupId)
                .eq(ChatGroupMember::getUserId, userId)
                .set(ChatGroupMember::getMemberRole, role);
        memberMapper.update(null, updateWrapper);
        log.info("群组{}中用户{}角色已更新为{}", groupId, userId, role);
    }

    @Override
    @Transactional
    public void updateSettings(Long groupId, Map<String, Object> settings) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }

        if (settings.containsKey("groupName")) {
            group.setGroupName((String) settings.get("groupName"));
        }
        if (settings.containsKey("announcement")) {
            group.setAnnouncement((String) settings.get("announcement"));
        }
        if (settings.containsKey("description")) {
            group.setDescription((String) settings.get("description"));
        }
        if (settings.containsKey("muteAll")) {
            group.setMuteAll(((Number) settings.get("muteAll")).intValue());
        }
        if (settings.containsKey("allowInvite")) {
            group.setAllowInvite(((Number) settings.get("allowInvite")).intValue());
        }
        if (settings.containsKey("maxMembers")) {
            group.setMaxMembers(((Number) settings.get("maxMembers")).intValue());
        }
        if (settings.containsKey("icon")) {
            group.setIcon((String) settings.get("icon"));
        }

        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        log.info("群组{}设置已更新", groupId);
    }

    @Override
    @Transactional
    public boolean deleteGroup(Long groupId) {
        ChatGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群组不存在");
        }
        // 使用 MyBatis-Plus 的 deleteById 触发 @TableLogic 逻辑删除
        // （@TableLogic 会将 deleteById 转换为 UPDATE SET deleted=1 WHERE id=? AND deleted=0）
        groupMapper.deleteById(groupId);
        // 同时移除所有成员
        LambdaQueryWrapper<ChatGroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ChatGroupMember::getGroupId, groupId);
        memberMapper.delete(memberWrapper);
        log.info("群组{}已删除", groupId);
        return true;
    }

    @Override
    public Map<String, Object> getStats(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<ChatGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatGroup::getTenantId, tenantId);
        if (conferenceId != null) {
            wrapper.eq(ChatGroup::getConferenceId, conferenceId);
        }

        List<ChatGroup> groups = groupMapper.selectList(wrapper);
        long total = groups.size();
        long active = groups.stream().filter(g -> g.getStatus() == 1).count();
        long totalMembers = groups.stream().mapToInt(g -> g.getMemberCount() != null ? g.getMemberCount() : 0).sum();

        Map<String, Long> byType = groups.stream()
                .filter(g -> g.getGroupType() != null)
                .collect(Collectors.groupingBy(ChatGroup::getGroupType, Collectors.counting()));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalGroups", total);
        stats.put("activeGroups", active);
        stats.put("totalMembers", totalMembers);
        stats.put("byType", byType);
        return stats;
    }
}
