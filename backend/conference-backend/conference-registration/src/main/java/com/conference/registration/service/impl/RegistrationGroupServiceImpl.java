package com.conference.registration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.registration.dto.GroupRequest;
import com.conference.registration.entity.Registration;
import com.conference.registration.entity.RegistrationGroup;
import com.conference.registration.entity.RegistrationGroupMember;
import com.conference.registration.mapper.RegistrationGroupMapper;
import com.conference.registration.mapper.RegistrationGroupMemberMapper;
import com.conference.registration.mapper.RegistrationMapper;
import com.conference.registration.service.RegistrationGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分组管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationGroupServiceImpl implements RegistrationGroupService {
    
    private final RegistrationGroupMapper groupMapper;
    private final RegistrationGroupMemberMapper groupMemberMapper;
    private final RegistrationMapper registrationMapper;
    
    @Override
    public List<RegistrationGroup> getGroupsByConference(Long conferenceId) {
        LambdaQueryWrapper<RegistrationGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationGroup::getConferenceId, conferenceId)
               .eq(RegistrationGroup::getDeleted, false)
               .orderByAsc(RegistrationGroup::getSort);
        return groupMapper.selectList(wrapper);
    }
    
    @Override
    public Map<String, Object> getGroupDetail(Long groupId) {
        Map<String, Object> detail = new HashMap<>();
        
        // 获取分组信息
        RegistrationGroup group = groupMapper.selectById(groupId);
        detail.put("group", group);
        
        // 获取成员列表
        LambdaQueryWrapper<RegistrationGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationGroupMember::getGroupId, groupId);
        List<RegistrationGroupMember> members = groupMemberMapper.selectList(wrapper);
        
        // 获取成员详细信息
        List<Long> registrationIds = members.stream()
                .map(RegistrationGroupMember::getRegistrationId)
                .collect(Collectors.toList());
        
        if (!registrationIds.isEmpty()) {
            LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
            regWrapper.in(Registration::getId, registrationIds);
            List<Registration> registrations = registrationMapper.selectList(regWrapper);
            detail.put("members", registrations);
        } else {
            detail.put("members", new ArrayList<>());
        }
        
        // 统计工作人员数量
        long staffCount = members.stream()
                .filter(m -> m.getIsStaff() != null && m.getIsStaff())
                .count();
        detail.put("staffCount", staffCount);
        
        return detail;
    }
    
    @Override
    @Transactional
    public RegistrationGroup createGroup(GroupRequest request) {
        RegistrationGroup group = new RegistrationGroup();
        group.setConferenceId(request.getConferenceId());
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setColor(request.getColor());
        group.setIcon(request.getIcon());
        group.setType(request.getType() != null ? request.getType() : "manual");
        group.setSort(request.getSort() != null ? request.getSort() : 0);
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        group.setDeleted(false);
        
        groupMapper.insert(group);
        
        // 如果有成员，分配成员
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            assignMembers(group.getId(), request.getMemberIds());
        }
        
        // 如果有工作人员，指定工作人员
        if (request.getStaffIds() != null && !request.getStaffIds().isEmpty()) {
            assignStaff(group.getId(), request.getStaffIds());
        }
        
        log.info("创建分组成功: groupId={}, name={}", group.getId(), group.getName());
        return group;
    }
    
    @Override
    @Transactional
    public RegistrationGroup updateGroup(Long id, GroupRequest request) {
        RegistrationGroup group = groupMapper.selectById(id);
        if (group == null) {
            throw new RuntimeException("分组不存在");
        }
        
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setColor(request.getColor());
        group.setIcon(request.getIcon());
        group.setSort(request.getSort());
        group.setUpdateTime(LocalDateTime.now());
        
        groupMapper.updateById(group);
        
        log.info("更新分组成功: groupId={}, name={}", id, group.getName());
        return group;
    }
    
    @Override
    @Transactional
    public void deleteGroup(Long id) {
        RegistrationGroup group = groupMapper.selectById(id);
        if (group == null) {
            throw new RuntimeException("分组不存在");
        }
        
        // 软删除
        group.setDeleted(true);
        group.setUpdateTime(LocalDateTime.now());
        groupMapper.updateById(group);
        
        // 删除分组关联
        LambdaQueryWrapper<RegistrationGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationGroupMember::getGroupId, id);
        groupMemberMapper.delete(wrapper);
        
        log.info("删除分组成功: groupId={}", id);
    }
    
    @Override
    @Transactional
    public void assignMembers(Long groupId, List<Long> memberIds) {
        // 先删除旧的成员关联
        LambdaQueryWrapper<RegistrationGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationGroupMember::getGroupId, groupId);
        groupMemberMapper.delete(wrapper);
        
        // 添加新的成员关联
        if (memberIds != null && !memberIds.isEmpty()) {
            List<RegistrationGroupMember> members = new ArrayList<>();
            for (Long registrationId : memberIds) {
                RegistrationGroupMember member = new RegistrationGroupMember();
                member.setGroupId(groupId);
                member.setRegistrationId(registrationId);
                member.setIsStaff(false);
                member.setCreateTime(LocalDateTime.now());
                members.add(member);
            }
            
            // 批量插入
            members.forEach(groupMemberMapper::insert);
        }
        
        log.info("分配成员成功: groupId={}, memberCount={}", groupId, memberIds != null ? memberIds.size() : 0);
    }
    
    @Override
    @Transactional
    public void removeMember(Long groupId, Long memberId) {
        LambdaQueryWrapper<RegistrationGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationGroupMember::getGroupId, groupId)
               .eq(RegistrationGroupMember::getRegistrationId, memberId);
        groupMemberMapper.delete(wrapper);
        
        log.info("移除成员成功: groupId={}, memberId={}", groupId, memberId);
    }
    
    @Override
    @Transactional
    public void assignStaff(Long groupId, List<Long> staffIds) {
        // 先清除所有工作人员标记
        LambdaQueryWrapper<RegistrationGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegistrationGroupMember::getGroupId, groupId);
        List<RegistrationGroupMember> members = groupMemberMapper.selectList(wrapper);
        members.forEach(m -> {
            m.setIsStaff(false);
            groupMemberMapper.updateById(m);
        });
        
        // 设置新的工作人员
        if (staffIds != null && !staffIds.isEmpty()) {
            staffIds.forEach(staffId -> {
                LambdaQueryWrapper<RegistrationGroupMember> staffWrapper = new LambdaQueryWrapper<>();
                staffWrapper.eq(RegistrationGroupMember::getGroupId, groupId)
                          .eq(RegistrationGroupMember::getRegistrationId, staffId);
                RegistrationGroupMember member = groupMemberMapper.selectOne(staffWrapper);
                if (member != null) {
                    member.setIsStaff(true);
                    groupMemberMapper.updateById(member);
                }
            });
        }
        
        log.info("指定工作人员成功: groupId={}, staffCount={}", groupId, staffIds != null ? staffIds.size() : 0);
    }
    
    @Override
    @Transactional
    public List<RegistrationGroup> autoGroup(Long conferenceId, String criteria) {
        log.info("开始自动分组: conferenceId={}, criteria={}", conferenceId, criteria);
        
        // 清除该会议的所有自动分组
        LambdaQueryWrapper<RegistrationGroup> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RegistrationGroup::getConferenceId, conferenceId)
                   .eq(RegistrationGroup::getType, "auto");
        List<RegistrationGroup> oldGroups = groupMapper.selectList(deleteWrapper);
        oldGroups.forEach(g -> {
            g.setDeleted(true);
            groupMapper.updateById(g);
        });
        
        // 获取所有报名数据
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getConferenceId, conferenceId);
        List<Registration> registrations = registrationMapper.selectList(regWrapper);
        
        // 根据条件分组
        Map<String, List<Registration>> groupedMap = new HashMap<>();
        
        if ("department".equals(criteria)) {
            // 按部门分组
            groupedMap = registrations.stream()
                    .collect(Collectors.groupingBy(r -> {
                        String dept = extractDepartment(r);
                        return dept != null ? dept : "未分类";
                    }));
        } else if ("position".equals(criteria)) {
            // 按职位分组
            groupedMap = registrations.stream()
                    .collect(Collectors.groupingBy(r -> {
                        String pos = extractPosition(r);
                        return pos != null ? pos : "未分类";
                    }));
        } else {
            // 默认按编号分组
            int groupSize = 10;
            for (int i = 0; i < registrations.size(); i += groupSize) {
                int groupNum = i / groupSize + 1;
                String key = "第" + groupNum + "组";
                int end = Math.min(i + groupSize, registrations.size());
                groupedMap.put(key, registrations.subList(i, end));
            }
        }
        
        // 创建分组
        List<RegistrationGroup> newGroups = new ArrayList<>();
        int sort = 0;
        for (Map.Entry<String, List<Registration>> entry : groupedMap.entrySet()) {
            String groupName = entry.getKey();
            List<Registration> groupMembers = entry.getValue();
            
            RegistrationGroup group = new RegistrationGroup();
            group.setConferenceId(conferenceId);
            group.setName(groupName);
            group.setDescription("自动生成 - " + criteria);
            group.setType("auto");
            group.setSort(sort++);
            group.setCreateTime(LocalDateTime.now());
            group.setUpdateTime(LocalDateTime.now());
            group.setDeleted(false);
            
            groupMapper.insert(group);
            newGroups.add(group);
            
            // 分配成员
            List<Long> memberIds = groupMembers.stream()
                    .map(Registration::getId)
                    .collect(Collectors.toList());
            assignMembers(group.getId(), memberIds);
        }
        
        log.info("自动分组完成: conferenceId={}, groupCount={}", conferenceId, newGroups.size());
        return newGroups;
    }
    
    /**
     * 从报名数据中提取部门信息
     */
    private String extractDepartment(Registration registration) {
        try {
            if (registration.getRegistrationData() != null) {
                com.alibaba.fastjson2.JSONObject data = com.alibaba.fastjson2.JSONObject.parseObject(registration.getRegistrationData());
                return data.getString("department");
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }
    
    /**
     * 从报名数据中提取职位信息
     */
    private String extractPosition(Registration registration) {
        try {
            if (registration.getRegistrationData() != null) {
                com.alibaba.fastjson2.JSONObject data = com.alibaba.fastjson2.JSONObject.parseObject(registration.getRegistrationData());
                return data.getString("position");
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }
}
