package com.conference.meeting.service;

import com.conference.meeting.dto.ArchiveMessageGroupResponse;
import com.conference.meeting.dto.ArchiveStatsResponse;
import com.conference.meeting.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 会议归档服务接口
 */
public interface IArchiveService {

    // =============== 统计 ===============
    /** 获取归档统计概览 */
    ArchiveStatsResponse getArchiveStats(Long meetingId);

    // =============== 配置 ===============
    /** 获取归档配置 */
    ArchiveConfig getArchiveConfig(Long meetingId);

    /** 更新归档配置 */
    ArchiveConfig updateArchiveConfig(Long meetingId, Boolean allowStudentUpload);

    // =============== 资料(课件+互动) ===============
    /** 获取资料列表 */
    List<ArchiveMaterial> getMaterials(Long meetingId, String category);

    /** 新增资料 */
    ArchiveMaterial addMaterial(ArchiveMaterial material);

    /** 删除资料 */
    Boolean deleteMaterial(Long meetingId, Long materialId);

    /** 增加下载次数 */
    void incrementDownloadCount(Long materialId);

    // =============== 业务数据 ===============
    /** 获取业务数据 */
    List<ArchiveBusinessData> getBusinessData(Long meetingId, String dataType);

    /** 批量保存业务数据（替换） */
    Boolean saveBusinessData(Long meetingId, String dataType, List<ArchiveBusinessData> dataList);

    /** 新增单条业务数据 */
    ArchiveBusinessData addBusinessData(ArchiveBusinessData data);

    // =============== 消息 ===============
    /** 获取消息群组列表(含预览) */
    List<ArchiveMessageGroupResponse> getMessageGroups(Long meetingId);

    /** 创建消息群组 */
    ArchiveMessageGroup addMessageGroup(ArchiveMessageGroup group);

    /** 获取群组内消息列表 */
    List<ArchiveMessage> getGroupMessages(Long meetingId, Long groupId, String keyword);

    /** 添加消息到群组 */
    ArchiveMessage addMessage(ArchiveMessage message);

    /** 批量添加消息 */
    Boolean batchAddMessages(Long meetingId, Long groupId, List<ArchiveMessage> messages);

    // =============== 数据同步 ===============
    /** 从源数据表（日程附件、手册讨论、签到等）同步真实数据到归档表 */
    Map<String, Object> syncFromSource(Long meetingId);

    // =============== 清空 ===============
    /** 清空会议所有归档数据 */
    Boolean clearAllArchive(Long meetingId);

    // =============== 打包 ===============
    /** 标记已打包 */
    void markAsPacked(Long meetingId);

    // =============== 导出 ===============
    /** 导出业务数据为CSV */
    String exportBusinessDataCsv(Long meetingId, String dataType);

    /** 导出消息为CSV */
    String exportMessagesCsv(Long meetingId, Long groupId);
}
