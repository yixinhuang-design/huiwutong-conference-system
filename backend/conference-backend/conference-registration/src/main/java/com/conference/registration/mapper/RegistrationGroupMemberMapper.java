package com.conference.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.registration.entity.RegistrationGroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分组成员 Mapper 接口
 */
@Mapper
public interface RegistrationGroupMemberMapper extends BaseMapper<RegistrationGroupMember> {
    
    /**
     * 获取分组的所有成员
     */
    List<RegistrationGroupMember> getByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 获取成员所在的分组
     */
    List<RegistrationGroupMember> getByRegistrationId(@Param("registrationId") Long registrationId);
    
    /**
     * 获取分组中的工作人员
     */
    List<RegistrationGroupMember> getStaffByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 删除分组的所有成员
     */
    int deleteByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 移除分组成员
     */
    int deleteByGroupAndMember(@Param("groupId") Long groupId, @Param("registrationId") Long registrationId);
    
    /**
     * 统计分组成员数量
     */
    Long countByGroupId(@Param("groupId") Long groupId);
    
    /**
     * 统计分组工作人员数量
     */
    Long countStaffByGroupId(@Param("groupId") Long groupId);
}
