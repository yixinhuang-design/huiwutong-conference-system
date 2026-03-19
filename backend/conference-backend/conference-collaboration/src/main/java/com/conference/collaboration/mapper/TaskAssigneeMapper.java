package com.conference.collaboration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.collaboration.entity.TaskAssignee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskAssigneeMapper extends BaseMapper<TaskAssignee> {
}
