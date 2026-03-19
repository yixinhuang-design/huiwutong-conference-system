package com.conference.registration.controller;

import com.conference.common.result.Result;
import com.conference.registration.dto.HandbookSaveRequest;
import com.conference.registration.service.HandbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学员手册 Controller
 *
 * 数据来源:
 * - 日程 → conf_schedule (日程管理)
 * - 教案/资料 → conf_schedule_attachment (日程管理)
 * - 学员名册 → conf_registration (报名汇总)
 * - 分组 → conf_group + conf_group_member (报名汇总)
 * - 讨论题目 → conf_handbook_discussion (手册独有)
 * - 手册配置 → conf_handbook (手册独有)
 */
@RestController
@RequestMapping("/api/handbook")
@RequiredArgsConstructor
public class HandbookController {

    private final HandbookService handbookService;

    /**
     * 获取手册完整数据（配置 + 引用数据）
     * 前端打开编辑页时调用，一次获取全部数据
     */
    @GetMapping("/get")
    public Result<Map<String, Object>> getHandbook(@RequestParam Long meetingId) {
        Map<String, Object> data = handbookService.getHandbook(meetingId);
        return Result.success(data);
    }

    /**
     * 保存手册草稿
     */
    @PostMapping("/save")
    public Result<Map<String, Object>> saveHandbook(@RequestBody HandbookSaveRequest request) {
        Map<String, Object> data = handbookService.saveHandbook(request);
        return Result.success(data);
    }

    /**
     * 获取名册数据（学员列表）
     * 数据来源: conf_registration (status=1 已审核)
     */
    @GetMapping("/roster")
    public Result<Map<String, Object>> getRosterData(@RequestParam Long meetingId) {
        Map<String, Object> data = handbookService.getRosterData(meetingId);
        return Result.success(data);
    }

    /**
     * 获取日程数据
     * 数据来源: conf_schedule
     */
    @GetMapping("/schedule")
    public Result<Map<String, Object>> getScheduleData(@RequestParam Long meetingId) {
        Map<String, Object> data = handbookService.getScheduleData(meetingId);
        return Result.success(data);
    }

    /**
     * 获取教案/资料数据
     * 数据来源: conf_schedule_attachment
     */
    @GetMapping("/materials")
    public Result<Map<String, Object>> getMaterialsData(@RequestParam Long meetingId) {
        Map<String, Object> data = handbookService.getMaterialsData(meetingId);
        return Result.success(data);
    }

    /**
     * 获取分组数据
     * 数据来源: conf_group + conf_group_member + conf_registration
     */
    @GetMapping("/groups")
    public Result<Map<String, Object>> getGroupingData(@RequestParam Long meetingId) {
        Map<String, Object> data = handbookService.getGroupingData(meetingId);
        return Result.success(data);
    }

    /**
     * 生成手册
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateHandbook(@RequestParam Long meetingId) {
        Map<String, Object> data = handbookService.generateHandbook(meetingId);
        return Result.success(data);
    }
}
