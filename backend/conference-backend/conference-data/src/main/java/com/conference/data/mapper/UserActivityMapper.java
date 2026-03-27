package com.conference.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.data.entity.UserActivity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserActivityMapper extends BaseMapper<UserActivity> {
}
