package com.conference.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.meeting.dto.ScheduleAttachmentResponse;
import com.conference.meeting.entity.ScheduleAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 日程附件服务接口
 * 处理课件、PPT等文件的上传、存储、下载、删除
 */
public interface IScheduleAttachmentService extends IService<ScheduleAttachment> {

    /**
     * 上传单个附件
     * 
     * @param scheduleId 日程ID
     * @param file 文件
     * @param description 文件描述
     * @return 附件响应对象
     */
    ScheduleAttachmentResponse uploadAttachment(Long scheduleId, MultipartFile file, String description);

    /**
     * 批量上传附件
     * 
     * @param scheduleId 日程ID
     * @param files 文件数组
     * @return 附件响应对象列表
     */
    List<ScheduleAttachmentResponse> uploadAttachmentBatch(Long scheduleId, MultipartFile[] files);

    /**
     * 获取日程的所有附件列表
     * 
     * @param scheduleId 日程ID
     * @return 附件列表
     */
    List<ScheduleAttachmentResponse> listAttachments(Long scheduleId);

    /**
     * 获取单个附件详情
     * 
     * @param id 附件ID
     * @return 附件响应对象
     */
    ScheduleAttachmentResponse getAttachment(Long id);

    /**
     * 删除附件
     * 
     * @param id 附件ID
     */
    void deleteAttachment(Long id);

    /**
     * 更新附件信息
     * 
     * @param id 附件ID
     * @param description 新的描述
     * @return 更新后的附件信息
     */
    ScheduleAttachmentResponse updateAttachment(Long id, String description);

    /**
     * 获取下载URL
     * 
     * @param id 附件ID
     * @return 下载URL
     */
    String getDownloadUrl(Long id);

    /**
     * 增加下载计数
     * 
     * @param id 附件ID
     */
    void incrementDownloadCount(Long id);

    /**
     * 验证文件是否允许上传
     * 
     * @param fileName 文件名
     * @param fileSize 文件大小（字节）
     * @return 验证结果map，包含isValid, message等字段
     */
    Map<String, Object> validateFile(String fileName, Long fileSize);

    /**
     * 按哈希值查询附件（检查重复）
     * 
     * @param scheduleId 日程ID
     * @param fileHash 文件哈希
     * @return 附件对象
     */
    ScheduleAttachment getByFileHash(Long scheduleId, String fileHash);
}
