package com.conference.collaboration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.collaboration.entity.ChatMaterial;

import java.util.List;
import java.util.Map;

public interface ChatMaterialService extends IService<ChatMaterial> {

    /** 上传资料 */
    ChatMaterial uploadMaterial(ChatMaterial material);

    /** 按群组获取资料列表(分页) */
    Page<ChatMaterial> listByGroup(Long groupId, String category, int page, int size);

    /** 按会议获取资料列表(分页) */
    Page<ChatMaterial> listByConference(Long conferenceId, String category, int page, int size);

    /** 获取资料详情 */
    ChatMaterial getMaterialDetail(Long materialId);

    /** 删除资料 */
    void deleteMaterial(Long materialId);

    /** 增加下载计数 */
    void incrementDownloadCount(Long materialId);

    /** 搜索资料 */
    List<ChatMaterial> searchMaterials(Long conferenceId, String keyword);

    /** 获取资料统计 */
    Map<String, Object> getMaterialStats(Long conferenceId);
}
