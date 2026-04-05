package com.conference.collaboration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.collaboration.entity.TaskDependency;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务依赖关系 Mapper
 * @author AI Executive
 * @date 2026-04-01
 */
@Mapper
public interface TaskDependencyMapper extends BaseMapper<TaskDependency> {
}
