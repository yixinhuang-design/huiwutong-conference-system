package com.conference.meeting.service;

import com.conference.meeting.dto.ChunkUploadRequest;
import com.conference.meeting.dto.ChunkUploadResponse;
import com.conference.meeting.dto.UploadProgressResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 断点续传服务接口
 */
public interface IChunkUploadService {

    /**
     * 初始化上传任务
     * 
     * @param uploadId 上传ID
     * @param fileName 文件名
     * @param totalSize 文件总大小
     * @param fileHash 文件哈希值
     * @param scheduleId 日程ID
     * @return 初始化信息
     */
    UploadProgressResponse initializeUpload(String uploadId, String fileName, Long totalSize, String fileHash, Long scheduleId);

    /**
     * 上传单个分块
     * 
     * @param request 分块上传请求
     * @param chunk 分块数据
     * @return 上传结果
     */
    ChunkUploadResponse uploadChunk(ChunkUploadRequest request, MultipartFile chunk);

    /**
     * 查询上传进度
     * 
     * @param uploadId 上传ID
     * @return 进度信息
     */
    UploadProgressResponse queryProgress(String uploadId);

    /**
     * 合并分块完成上传
     * 
     * @param uploadId 上传ID
     * @param scheduleId 日程ID
     * @param description 文件描述
     * @return 最终的附件信息
     */
    Object mergeChunks(String uploadId, Long scheduleId, String description);

    /**
     * 暂停/取消上传
     * 
     * @param uploadId 上传ID
     */
    void cancelUpload(String uploadId);

    /**
     * 清理过期的临时文件
     */
    void cleanExpiredTempFiles();
}
