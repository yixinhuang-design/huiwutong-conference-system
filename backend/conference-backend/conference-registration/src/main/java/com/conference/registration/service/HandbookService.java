package com.conference.registration.service;

import com.conference.registration.dto.HandbookSaveRequest;

import java.util.Map;

/**
 * 学员手册服务接口
 */
public interface HandbookService {

    /**
     * 保存/更新手册草稿（含所有配置）
     */
    Map<String, Object> saveHandbook(HandbookSaveRequest request);

    /**
     * 获取手册完整配置（含引用数据）
     * 日程从 conf_schedule 获取
     * 资料从 conf_schedule_attachment 获取
     * 学员名册从 conf_registration 获取
     * 分组从 conf_group + conf_group_member 获取
     */
    Map<String, Object> getHandbook(Long meetingId);

    /**
     * 获取名册数据：已审核学员列表
     * 数据来源：conf_registration (status=1)
     */
    Map<String, Object> getRosterData(Long meetingId);

    /**
     * 获取日程数据
     * 数据来源：conf_schedule
     */
    Map<String, Object> getScheduleData(Long meetingId);

    /**
     * 获取教案/资料数据
     * 数据来源：conf_schedule_attachment
     */
    Map<String, Object> getMaterialsData(Long meetingId);

    /**
     * 获取分组数据
     * 数据来源：conf_group + conf_group_member + conf_registration
     */
    Map<String, Object> getGroupingData(Long meetingId);

    /**
     * 生成手册并返回状态
     */
    Map<String, Object> generateHandbook(Long meetingId);
}
