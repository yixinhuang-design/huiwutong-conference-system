package com.conference.meeting.controller;

import com.conference.common.exception.BusinessException;
import com.conference.common.result.Result;
import com.conference.meeting.dto.ScheduleAttachmentResponse;
import com.conference.meeting.service.IScheduleAttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日程附件管理API Controller
 * 处理课件、PPT、Word等文件的上传、下载、删除
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule/attachment")
// @CrossOrigin  // 已禁用：使用Gateway的统一CORS配置
public class ScheduleAttachmentController {

    @Autowired
    private IScheduleAttachmentService scheduleAttachmentService;

    /**
     * 上传课件附件
     * POST /api/schedule/attachment/upload?scheduleId=123
     * 
     * @param scheduleId 日程ID
     * @param file 上传的文件
     * @param description 文件描述（可选）
     * @return 上传结果及文件信息
     */
    @PostMapping("/upload")
    public ResponseEntity<Result<ScheduleAttachmentResponse>> uploadAttachment(
            @RequestParam Long scheduleId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String description) {
        try {
            if (scheduleId == null || scheduleId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "日程ID无效"));
            }

            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "文件不能为空"));
            }

            log.info("上传课件附件 - scheduleId: {}, fileName: {}, fileSize: {}", 
                    scheduleId, file.getOriginalFilename(), file.getSize());

            ScheduleAttachmentResponse response = scheduleAttachmentService.uploadAttachment(
                    scheduleId, file, description);

            log.info("课件附件上传成功 - attachmentId: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success(response));

        } catch (BusinessException e) {
            log.warn("上传课件失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("上传课件异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "文件上传失败"));
        }
    }

    /**
     * 批量上传附件
     * POST /api/schedule/attachment/upload-batch?scheduleId=123
     * 
     * @param scheduleId 日程ID
     * @param files 上传的文件数组
     * @return 上传结果列表
     */
    @PostMapping("/upload-batch")
    public ResponseEntity<Result<List<ScheduleAttachmentResponse>>> uploadAttachmentBatch(
            @RequestParam Long scheduleId,
            @RequestParam MultipartFile[] files) {
        try {
            if (scheduleId == null || scheduleId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "日程ID无效"));
            }

            if (files == null || files.length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "文件不能为空"));
            }

            log.info("批量上传课件附件 - scheduleId: {}, fileCount: {}", scheduleId, files.length);

            List<ScheduleAttachmentResponse> responses = scheduleAttachmentService.uploadAttachmentBatch(
                    scheduleId, files);

            log.info("批量上传完成 - successCount: {}", responses.size());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success(responses));

        } catch (BusinessException e) {
            log.warn("批量上传课件失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("批量上传课件异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "批量上传失败"));
        }
    }

    /**
     * 获取日程的所有附件列表
     * GET /api/schedule/attachment/list?scheduleId=123
     * 
     * @param scheduleId 日程ID
     * @return 附件列表
     */
    @GetMapping("/list")
    public ResponseEntity<Result<List<ScheduleAttachmentResponse>>> listAttachments(
            @RequestParam Long scheduleId) {
        try {
            if (scheduleId == null || scheduleId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "日程ID无效"));
            }

            log.info("获取日程附件列表 - scheduleId: {}", scheduleId);

            List<ScheduleAttachmentResponse> attachments = scheduleAttachmentService.listAttachments(scheduleId);

            return ResponseEntity.ok(Result.success(attachments));

        } catch (Exception e) {
            log.error("获取附件列表异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取附件列表失败"));
        }
    }

    /**
     * 获取附件详情
     * GET /api/schedule/attachment/{id}
     * 
     * @param id 附件ID
     * @return 附件详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<ScheduleAttachmentResponse>> getAttachment(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "附件ID无效"));
            }

            log.info("获取附件详情 - id: {}", id);

            ScheduleAttachmentResponse attachment = scheduleAttachmentService.getAttachment(id);

            return ResponseEntity.ok(Result.success(attachment));

        } catch (BusinessException e) {
            log.warn("获取附件失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("获取附件异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取附件失败"));
        }
    }

    /**
     * 删除附件
     * DELETE /api/schedule/attachment/{id}
     * 
     * @param id 附件ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteAttachment(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "附件ID无效"));
            }

            log.info("删除附件 - id: {}", id);

            scheduleAttachmentService.deleteAttachment(id);

            log.info("附件删除成功 - id: {}", id);
            return ResponseEntity.ok(Result.success(null));

        } catch (BusinessException e) {
            log.warn("删除附件失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("删除附件异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "删除附件失败"));
        }
    }

    /**
     * 更新附件信息（描述等）
     * PUT /api/schedule/attachment/{id}
     * 
     * @param id 附件ID
     * @param body 更新内容
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<ScheduleAttachmentResponse>> updateAttachment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "附件ID无效"));
            }

            log.info("更新附件 - id: {}", id);

            String description = (String) body.get("description");
            ScheduleAttachmentResponse attachment = scheduleAttachmentService.updateAttachment(id, description);

            log.info("附件更新成功 - id: {}", id);
            return ResponseEntity.ok(Result.success(attachment));

        } catch (BusinessException e) {
            log.warn("更新附件失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("更新附件异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "更新附件失败"));
        }
    }

    /**
     * 获取附件下载地址（用于Web、App下载）
     * GET /api/schedule/attachment/{id}/download-url
     * 
     * @param id 附件ID
     * @return 下载地址
     */
    @GetMapping("/{id}/download-url")
    public ResponseEntity<Result<Map<String, String>>> getDownloadUrl(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "附件ID无效"));
            }

            log.info("获取附件下载地址 - id: {}", id);

            String downloadUrl = scheduleAttachmentService.getDownloadUrl(id);
            Map<String, String> result = new HashMap<>();
            result.put("downloadUrl", downloadUrl);

            return ResponseEntity.ok(Result.success(result));

        } catch (BusinessException e) {
            log.warn("获取下载地址失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("获取下载地址异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取下载地址失败"));
        }
    }

    /**
     * 记录附件下载（统计下载次数）
     * POST /api/schedule/attachment/{id}/download
     * 
     * @param id 附件ID
     * @return 操作结果
     */
    @PostMapping("/{id}/download")
    public ResponseEntity<Result<Void>> recordDownload(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "附件ID无效"));
            }

            log.info("记录附件下载 - id: {}", id);

            scheduleAttachmentService.incrementDownloadCount(id);

            return ResponseEntity.ok(Result.success(null));

        } catch (Exception e) {
            log.error("记录下载异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "记录下载失败"));
        }
    }

    /**
     * 验证文件是否允许上传
     * GET /api/schedule/attachment/validate?fileName=test.ppt&fileSize=1024
     * 
     * @param fileName 文件名
     * @param fileSize 文件大小（字节）
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ResponseEntity<Result<Map<String, Object>>> validateFile(
            @RequestParam String fileName,
            @RequestParam Long fileSize) {
        try {
            log.info("验证文件 - fileName: {}, fileSize: {}", fileName, fileSize);

            Map<String, Object> result = scheduleAttachmentService.validateFile(fileName, fileSize);

            return ResponseEntity.ok(Result.success(result));

        } catch (Exception e) {
            log.error("验证文件异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "验证文件失败"));
        }
    }
}
