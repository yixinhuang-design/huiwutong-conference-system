package com.conference.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.collaboration.entity.ChatMaterial;
import com.conference.collaboration.mapper.ChatMaterialMapper;
import com.conference.collaboration.service.ChatMaterialService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMaterialServiceImpl extends ServiceImpl<ChatMaterialMapper, ChatMaterial>
        implements ChatMaterialService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final ChatMaterialMapper materialMapper;

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    public ChatMaterial uploadMaterial(ChatMaterial material) {
        Long tenantId = getTenantId();
        material.setTenantId(tenantId);
        material.setDownloadCount(0);
        material.setDeleted(0);
        material.setCreateTime(LocalDateTime.now());
        materialMapper.insert(material);
        log.info("[租户{}] 资料上传成功: id={}, name={}", tenantId, material.getId(), material.getFileName());
        return material;
    }

    @Override
    public Page<ChatMaterial> listByGroup(Long groupId, String category, int page, int size) {
        LambdaQueryWrapper<ChatMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMaterial::getGroupId, groupId)
                .eq(ChatMaterial::getDeleted, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(ChatMaterial::getCategory, category);
        }
        wrapper.orderByDesc(ChatMaterial::getCreateTime);
        return materialMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<ChatMaterial> listByConference(Long conferenceId, String category, int page, int size) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<ChatMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMaterial::getTenantId, tenantId)
                .eq(ChatMaterial::getDeleted, 0);
        if (conferenceId != null) {
            wrapper.eq(ChatMaterial::getConferenceId, conferenceId);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(ChatMaterial::getCategory, category);
        }
        wrapper.orderByDesc(ChatMaterial::getCreateTime);
        return materialMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public ChatMaterial getMaterialDetail(Long materialId) {
        return materialMapper.selectById(materialId);
    }

    @Override
    public void deleteMaterial(Long materialId) {
        ChatMaterial material = materialMapper.selectById(materialId);
        if (material != null) {
            material.setDeleted(1);
            materialMapper.updateById(material);
            log.info("资料{}已删除", materialId);
        }
    }

    @Override
    public void incrementDownloadCount(Long materialId) {
        ChatMaterial material = materialMapper.selectById(materialId);
        if (material != null) {
            material.setDownloadCount(material.getDownloadCount() + 1);
            materialMapper.updateById(material);
        }
    }

    @Override
    public List<ChatMaterial> searchMaterials(Long conferenceId, String keyword) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<ChatMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMaterial::getTenantId, tenantId)
                .eq(ChatMaterial::getDeleted, 0);
        if (conferenceId != null) {
            wrapper.eq(ChatMaterial::getConferenceId, conferenceId);
        }
        wrapper.like(ChatMaterial::getFileName, keyword)
                .orderByDesc(ChatMaterial::getCreateTime)
                .last("LIMIT 50");
        return materialMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getMaterialStats(Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<ChatMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMaterial::getTenantId, tenantId)
                .eq(ChatMaterial::getDeleted, 0);
        if (conferenceId != null) {
            wrapper.eq(ChatMaterial::getConferenceId, conferenceId);
        }

        List<ChatMaterial> materials = materialMapper.selectList(wrapper);
        long total = materials.size();
        long totalSize = materials.stream()
                .filter(m -> m.getFileSize() != null)
                .mapToLong(ChatMaterial::getFileSize)
                .sum();
        long totalDownloads = materials.stream()
                .filter(m -> m.getDownloadCount() != null)
                .mapToInt(ChatMaterial::getDownloadCount)
                .sum();

        Map<String, Long> byCategory = materials.stream()
                .filter(m -> m.getCategory() != null)
                .collect(Collectors.groupingBy(ChatMaterial::getCategory, Collectors.counting()));

        Map<String, Long> byType = materials.stream()
                .filter(m -> m.getFileType() != null)
                .collect(Collectors.groupingBy(ChatMaterial::getFileType, Collectors.counting()));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("totalSize", totalSize);
        stats.put("totalDownloads", totalDownloads);
        stats.put("byCategory", byCategory);
        stats.put("byType", byType);
        return stats;
    }
}
