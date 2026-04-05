package com.conference.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.registration.entity.RegistrationGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分组 Mapper 接口
 */
@Mapper
public interface RegistrationGroupMapper extends BaseMapper<RegistrationGroup> {
    
    /**
     * 获取会议的所有分组
     */
    List<RegistrationGroup> getGroupsByConference(@Param("conferenceId") Long conferenceId);
    
    /**
     * 统计会议的分组数量
     */
    Long countByConference(@Param("conferenceId") Long conferenceId);
}
