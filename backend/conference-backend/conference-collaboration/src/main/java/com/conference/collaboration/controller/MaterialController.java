package com.conference.collaboration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.collaboration.entity.ChatMaterial;
import com.conference.collaboration.service.ChatMaterialService;
import com.conference.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 资料管理 Controller
 * 支持资料上传、下载、查看、删除、搜索等完整功能
 */
@Slf4j
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class MaterialController {

    private final ChatMaterialService materialService;

    // ==================== 资料CRUD ====================

    /** 上传资料 */
    @PostMapping("/upload")
    public Result<ChatMaterial> uploadMaterial(@RequestBody ChatMaterial material) {
        ChatMaterial saved = materialService.uploadMaterial(material);
        return Result.ok("资料上传成功", saved);
    }

    /** 按群组获取资料列表 */
    @GetMapping("/group/{groupId}")
    public Result<Page<ChatMaterial>> listByGroup(
            @PathVariable Long groupId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChatMaterial> result = materialService.listByGroup(groupId, category, page, size);
        return Result.ok(result);
    }

    /** 按会议获取资料列表 */
    @GetMapping("/list")
    public Result<Page<ChatMaterial>> listByConference(
            @RequestParam(required = false) Long conferenceId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChatMaterial> result = materialService.listByConference(conferenceId, category, page, size);
        return Result.ok(result);
    }

    /** 获取资料详情 */
    @GetMapping("/{materialId}")
    public Result<ChatMaterial> getMaterialDetail(@PathVariable Long materialId) {
        ChatMaterial material = materialService.getMaterialDetail(materialId);
        if (material == null) {
            return Result.fail("资料不存在");
        }
        return Result.ok(material);
    }

    /** 删除资料 */
    @DeleteMapping("/{materialId}")
    public Result<String> deleteMaterial(@PathVariable Long materialId) {
        materialService.deleteMaterial(materialId);
        return Result.ok("资料已删除");
    }

    // ==================== 下载 ====================

    /** 记录下载(增加下载计数) */
    @PostMapping("/{materialId}/download")
    public Result<String> downloadMaterial(@PathVariable Long materialId) {
        materialService.incrementDownloadCount(materialId);
        ChatMaterial material = materialService.getMaterialDetail(materialId);
        return Result.ok("下载记录已更新", material != null ? material.getFileUrl() : null);
    }

    // ==================== 搜索 ====================

    /** 搜索资料 */
    @GetMapping("/search")
    public Result<List<ChatMaterial>> searchMaterials(
            @RequestParam(required = false) Long conferenceId,
            @RequestParam String keyword) {
        List<ChatMaterial> results = materialService.searchMaterials(conferenceId, keyword);
        return Result.ok(results);
    }

    // ==================== 统计 ====================

    /** 资料统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getMaterialStats(
            @RequestParam(required = false) Long conferenceId) {
        Map<String, Object> stats = materialService.getMaterialStats(conferenceId);
        return Result.ok(stats);
    }
}
