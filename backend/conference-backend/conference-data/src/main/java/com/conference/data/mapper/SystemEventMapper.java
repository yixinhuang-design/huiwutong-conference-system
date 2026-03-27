package com.conference.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.data.entity.SystemEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemEventMapper extends BaseMapper<SystemEvent> {
}
