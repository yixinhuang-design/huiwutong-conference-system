package com.conference.meeting.controller;

import com.conference.common.exception.BusinessException;
import com.conference.common.result.Result;
import com.conference.meeting.dto.ArchiveMessageGroupResponse;
import com.conference.meeting.dto.ArchiveStatsResponse;
import com.conference.meeting.entity.*;
import com.conference.meeting.service.IArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/meeting/{meetingId}/archive")
// @CrossOrigin  // 已禁用：使用Gateway的统一CORS配置
public class ArchiveController {

    @Autowired
    private IArchiveService archiveService;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    // ==================== 统计 ====================

    @GetMapping("/stats")
    public ResponseEntity<Result<ArchiveStatsResponse>> getStats(@PathVariable Long meetingId) {
        try {
            ArchiveStatsResponse stats = archiveService.getArchiveStats(meetingId);
            return ResponseEntity.ok(Result.success("获取成功", stats));
        } catch (BusinessException e) {
            log.warn("获取归档统计失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("获取归档统计异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取归档统计失败"));
        }
    }

    // ==================== 数据同步 ====================

    /**
     * 从源数据表同步真实数据到归档表
     * 数据源: conf_schedule_attachment → 课件资料
     *         conf_handbook + conf_handbook_discussion → 消息群组 + 消息
     *         conf_schedule_checkin → 签到率业务数据
     */
    @PostMapping("/sync")
    public ResponseEntity<Result<Map<String, Object>>> syncFromSource(@PathVariable Long meetingId) {
        try {
            Map<String, Object> syncResult = archiveService.syncFromSource(meetingId);
            return ResponseEntity.ok(Result.success("数据同步完成", syncResult));
        } catch (BusinessException e) {
            log.warn("归档数据同步失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("归档数据同步异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "归档数据同步失败: " + e.getMessage()));
        }
    }

    // ==================== 配置 ====================

    @GetMapping("/config")
    public ResponseEntity<Result<ArchiveConfig>> getConfig(@PathVariable Long meetingId) {
        try {
            ArchiveConfig config = archiveService.getArchiveConfig(meetingId);
            return ResponseEntity.ok(Result.success("获取成功", config));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("获取归档配置异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取归档配置失败"));
        }
    }

    @PutMapping("/config")
    public ResponseEntity<Result<ArchiveConfig>> updateConfig(
            @PathVariable Long meetingId,
            @RequestBody Map<String, Object> body) {
        try {
            Boolean allowStudentUpload = null;
            if (body.containsKey("allowStudentUpload")) {
                allowStudentUpload = Boolean.valueOf(body.get("allowStudentUpload").toString());
            }
            ArchiveConfig config = archiveService.updateArchiveConfig(meetingId, allowStudentUpload);
            return ResponseEntity.ok(Result.success("配置更新成功", config));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("更新归档配置异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "更新归档配置失败"));
        }
    }

    // ==================== 资料(课件+互动) ====================

    @GetMapping("/materials")
    public ResponseEntity<Result<List<ArchiveMaterial>>> getMaterials(
            @PathVariable Long meetingId,
            @RequestParam(required = false) String category) {
        try {
            List<ArchiveMaterial> materials = archiveService.getMaterials(meetingId, category);
            return ResponseEntity.ok(Result.success("获取成功", materials));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("获取归档资料异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取归档资料失败"));
        }
    }

    @PostMapping("/materials")
    public ResponseEntity<Result<ArchiveMaterial>> addMaterial(
            @PathVariable Long meetingId,
            @RequestBody ArchiveMaterial material) {
        try {
            material.setMeetingId(meetingId);
            ArchiveMaterial created = archiveService.addMaterial(material);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("资料添加成功", created));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("添加归档资料异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "添加归档资料失败"));
        }
    }

    @PostMapping("/materials/upload")
    public ResponseEntity<Result<Map<String, String>>> uploadMaterialFile(
            @PathVariable Long meetingId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "请选择要上传的文件"));
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "文件大小不能超过100MB"));
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            }

            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "archive", String.valueOf(meetingId));
            Files.createDirectories(uploadDir);

            Path target = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + "/uploads/archive/" + meetingId + "/" + fileName;

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("fileName", originalFilename);
            data.put("fileSize", formatFileSize(file.getSize()));
            data.put("fileType", getFileTypeFromName(originalFilename));

            return ResponseEntity.ok(Result.success("文件上传成功", data));
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "文件上传失败，请稍后重试"));
        }
    }

    @DeleteMapping("/materials/{materialId}")
    public ResponseEntity<Result<Boolean>> deleteMaterial(
            @PathVariable Long meetingId,
            @PathVariable Long materialId) {
        try {
            Boolean result = archiveService.deleteMaterial(meetingId, materialId);
            return ResponseEntity.ok(Result.success("资料删除成功", result));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("删除归档资料异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "删除归档资料失败"));
        }
    }

    @PostMapping("/materials/{materialId}/download-count")
    public ResponseEntity<Result<Boolean>> incrementDownloadCount(
            @PathVariable Long meetingId,
            @PathVariable Long materialId) {
        try {
            archiveService.incrementDownloadCount(materialId);
            return ResponseEntity.ok(Result.success("记录成功", true));
        } catch (Exception e) {
            log.error("记录下载次数异常", e);
            return ResponseEntity.ok(Result.success("记录成功", true));
        }
    }

    // ==================== 业务数据 ====================

    @GetMapping("/business-data")
    public ResponseEntity<Result<List<ArchiveBusinessData>>> getBusinessData(
            @PathVariable Long meetingId,
            @RequestParam(required = false) String type) {
        try {
            List<ArchiveBusinessData> dataList = archiveService.getBusinessData(meetingId, type);
            return ResponseEntity.ok(Result.success("获取成功", dataList));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("获取业务数据异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取业务数据失败"));
        }
    }

    @PostMapping("/business-data")
    public ResponseEntity<Result<ArchiveBusinessData>> addBusinessData(
            @PathVariable Long meetingId,
            @RequestBody ArchiveBusinessData data) {
        try {
            data.setMeetingId(meetingId);
            ArchiveBusinessData created = archiveService.addBusinessData(data);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("业务数据添加成功", created));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("添加业务数据异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "添加业务数据失败"));
        }
    }

    @PostMapping("/business-data/batch")
    public ResponseEntity<Result<Boolean>> saveBusinessDataBatch(
            @PathVariable Long meetingId,
            @RequestParam String type,
            @RequestBody List<ArchiveBusinessData> dataList) {
        try {
            Boolean result = archiveService.saveBusinessData(meetingId, type, dataList);
            return ResponseEntity.ok(Result.success("业务数据保存成功", result));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("批量保存业务数据异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "保存业务数据失败"));
        }
    }

    // ==================== 消息群组 ====================

    @GetMapping("/message-groups")
    public ResponseEntity<Result<List<ArchiveMessageGroupResponse>>> getMessageGroups(
            @PathVariable Long meetingId) {
        try {
            List<ArchiveMessageGroupResponse> groups = archiveService.getMessageGroups(meetingId);
            return ResponseEntity.ok(Result.success("获取成功", groups));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("获取消息群组异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取消息群组失败"));
        }
    }

    @PostMapping("/message-groups")
    public ResponseEntity<Result<ArchiveMessageGroup>> addMessageGroup(
            @PathVariable Long meetingId,
            @RequestBody ArchiveMessageGroup group) {
        try {
            group.setMeetingId(meetingId);
            ArchiveMessageGroup created = archiveService.addMessageGroup(group);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("群组创建成功", created));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("创建消息群组异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "创建消息群组失败"));
        }
    }

    @GetMapping("/message-groups/{groupId}/messages")
    public ResponseEntity<Result<List<ArchiveMessage>>> getGroupMessages(
            @PathVariable Long meetingId,
            @PathVariable Long groupId,
            @RequestParam(required = false) String keyword) {
        try {
            List<ArchiveMessage> messages = archiveService.getGroupMessages(meetingId, groupId, keyword);
            return ResponseEntity.ok(Result.success("获取成功", messages));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("获取群组消息异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取群组消息失败"));
        }
    }

    @PostMapping("/message-groups/{groupId}/messages")
    public ResponseEntity<Result<Boolean>> addMessages(
            @PathVariable Long meetingId,
            @PathVariable Long groupId,
            @RequestBody List<ArchiveMessage> messages) {
        try {
            Boolean result = archiveService.batchAddMessages(meetingId, groupId, messages);
            return ResponseEntity.ok(Result.success("消息添加成功", result));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("添加消息异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "添加消息失败"));
        }
    }

    // ==================== 清空 ====================

    @DeleteMapping("/clear")
    public ResponseEntity<Result<Boolean>> clearAllArchive(@PathVariable Long meetingId) {
        try {
            Boolean result = archiveService.clearAllArchive(meetingId);
            return ResponseEntity.ok(Result.success("归档数据已全部清空", result));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("清空归档数据异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "清空归档数据失败"));
        }
    }

    // ==================== 打包标记 ====================

    @PostMapping("/pack")
    public ResponseEntity<Result<Boolean>> markPacked(@PathVariable Long meetingId) {
        try {
            archiveService.markAsPacked(meetingId);
            return ResponseEntity.ok(Result.success("打包完成", true));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("标记打包异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "操作失败"));
        }
    }

    // ==================== 导出 ====================

    @GetMapping("/export/business-data")
    public ResponseEntity<byte[]> exportBusinessData(
            @PathVariable Long meetingId,
            @RequestParam(required = false) String type) {
        try {
            String csv = archiveService.exportBusinessDataCsv(meetingId, type);
            byte[] bytes = csv.getBytes("UTF-8");

            String fileName = "business_data_" + (type != null ? type : "all") + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(bytes.length);

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("导出业务数据异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/export/messages")
    public ResponseEntity<byte[]> exportMessages(
            @PathVariable Long meetingId,
            @RequestParam(required = false) Long groupId) {
        try {
            String csv = archiveService.exportMessagesCsv(meetingId, groupId);
            byte[] bytes = csv.getBytes("UTF-8");

            String fileName = "messages_" + (groupId != null ? "group_" + groupId : "all") + ".csv";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(bytes.length);

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("导出消息异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ==================== 工具方法 ====================

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return String.format("%.1fKB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1fMB", bytes / (1024.0 * 1024));
        return String.format("%.1fGB", bytes / (1024.0 * 1024 * 1024));
    }

    private String getFileTypeFromName(String fileName) {
        if (fileName == null) return "file";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "ppt";
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "word";
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return "excel";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) return "image";
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".wmv")) return "video";
        if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac")) return "audio";
        if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) return "archive";
        return "file";
    }
}
