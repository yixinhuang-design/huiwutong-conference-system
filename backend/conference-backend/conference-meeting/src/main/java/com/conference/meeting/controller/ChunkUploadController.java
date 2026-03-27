package com.conference.meeting.controller;

import com.conference.common.result.Result;
import com.conference.meeting.dto.*;
import com.conference.meeting.service.IChunkUploadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 分块上传控制器 - 处理断点续传请求
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule/chunk")
@AllArgsConstructor
public class ChunkUploadController {

    private final IChunkUploadService chunkUploadService;

    /**
     * 初始化上传任务
     * POST /api/schedule/chunk/init
     */
    @PostMapping("/init")
    public Result<UploadProgressResponse> initializeUpload(
            @RequestParam String fileName,
            @RequestParam Long totalSize,
            @RequestParam String fileHash,
            @RequestParam Long scheduleId) {
        
        log.info("初始化上传任务 - fileName: {}, totalSize: {}", fileName, totalSize);
        
        String uploadId = UUID.randomUUID().toString();
        
        try {
            UploadProgressResponse response = chunkUploadService.initializeUpload(
                    uploadId, fileName, totalSize, fileHash, scheduleId);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("初始化上传任务失败", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 上传单个分块
     * POST /api/schedule/chunk/upload
     */
    @PostMapping("/upload")
    public Result<ChunkUploadResponse> uploadChunk(
            @RequestParam String uploadId,
            @RequestParam Integer chunkIndex,
            @RequestParam Integer totalChunks,
            @RequestParam Long totalSize,
            @RequestParam String fileHash,
            @RequestParam String chunkHash,
            @RequestParam String fileName,
            @RequestParam Long scheduleId,
            @RequestPart MultipartFile chunk) {
        
        log.info("上传分块 - uploadId: {}, chunkIndex: {}/{}", uploadId, chunkIndex, totalChunks);
        
        try {
            // 构建请求对象
            ChunkUploadRequest request = ChunkUploadRequest.builder()
                    .uploadId(uploadId)
                    .chunkIndex(chunkIndex)
                    .totalChunks(totalChunks)
                    .chunkSize(chunk.getSize())
                    .totalSize(totalSize)
                    .fileHash(fileHash)
                    .chunkHash(chunkHash)
                    .fileName(fileName)
                    .scheduleId(scheduleId)
                    .description("")
                    .build();
            
            ChunkUploadResponse response = chunkUploadService.uploadChunk(request, chunk);
            
            // 如果分块已全部上传完成，返回201 Created表示资源已创建
            if (response.getProgress() == 100) {
                return Result.success(response);
            }
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("上传分块失败", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询上传进度
     * GET /api/schedule/chunk/progress/{uploadId}
     */
    @GetMapping("/progress/{uploadId}")
    public Result<UploadProgressResponse> queryProgress(
            @PathVariable String uploadId) {
        
        log.debug("查询上传进度 - uploadId: {}", uploadId);
        
        try {
            UploadProgressResponse response = chunkUploadService.queryProgress(uploadId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询上传进度失败", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 完成上传并合并分块
     * POST /api/schedule/chunk/finish
     */
    @PostMapping("/finish")
    public Result<Object> finishUpload(
            @RequestParam String uploadId,
            @RequestParam Long scheduleId,
            @RequestParam(required = false) String description) {
        
        log.info("完成上传并合并 - uploadId: {}, scheduleId: {}", uploadId, scheduleId);
        
        try {
            Object attachment = chunkUploadService.mergeChunks(uploadId, scheduleId, 
                    description != null ? description : "");
            
            return Result.success(attachment);
        } catch (Exception e) {
            log.error("完成上传失败", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 取消上传
     * DELETE /api/schedule/chunk/{uploadId}
     */
    @DeleteMapping("/{uploadId}")
    public Result<Void> cancelUpload(
            @PathVariable String uploadId) {
        
        log.info("取消上传 - uploadId: {}", uploadId);
        
        try {
            chunkUploadService.cancelUpload(uploadId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("取消上传失败", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取上传统计信息
     * GET /api/schedule/chunk/stats
     */
    @GetMapping("/stats/{uploadId}")
    public Result<UploadStatsResponse> getUploadStats(
            @PathVariable String uploadId) {
        
        try {
            UploadProgressResponse progress = chunkUploadService.queryProgress(uploadId);
            
            UploadStatsResponse stats = UploadStatsResponse.builder()
                    .uploadId(uploadId)
                    .fileName(progress.getFileName())
                    .totalSize(progress.getTotalSize())
                    .uploadedSize(progress.getUploadedSize())
                    .totalChunks(progress.getTotalChunks())
                    .uploadedChunks(progress.getUploadedCount())
                    .progress(progress.getProgress())
                    .status(progress.getStatus())
                    .estimatedTime(progress.getEstimatedTime())
                    .build();
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取上传统计信息失败", e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 健康检查 - 验证服务是否可用
     * GET /api/schedule/chunk/health
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("分块上传服务正常运行");
    }
}
