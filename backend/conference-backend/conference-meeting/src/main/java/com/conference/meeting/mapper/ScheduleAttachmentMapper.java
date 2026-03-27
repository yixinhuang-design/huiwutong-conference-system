package com.conference.meeting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.meeting.entity.ScheduleAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日程附件Mapper接口
 */
@Mapper
public interface ScheduleAttachmentMapper extends BaseMapper<ScheduleAttachment> {

    /**
     * 按日程ID查询所有附件
     */
    @Select("SELECT * FROM conf_schedule_attachment WHERE schedule_id = #{scheduleId} AND deleted = 0 " +
            "ORDER BY upload_time DESC")
    List<ScheduleAttachment> selectByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * 按文件哈希查询附件
     */
    @Select("SELECT * FROM conf_schedule_attachment WHERE schedule_id = #{scheduleId} AND file_hash = #{fileHash} " +
            "AND deleted = 0")
    ScheduleAttachment selectByFileHash(@Param("scheduleId") Long scheduleId, @Param("fileHash") String fileHash);

    /**
     * 统计日程的附件数量
     */
    @Select("SELECT COUNT(*) FROM conf_schedule_attachment WHERE schedule_id = #{scheduleId} AND deleted = 0")
    Integer countByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * 更新下载次数
     */
    int incrementDownloadCount(@Param("id") Long id);
}
