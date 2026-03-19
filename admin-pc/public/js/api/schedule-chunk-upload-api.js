/**
 * 断点续传API - schedule-chunk-upload-api.js
 * 处理分块上传、进度查询、断点续传等功能
 */

const ChunkUploadAPI = {
    /**
     * 初始化上传任务
     * @param fileName {string} 文件名
     * @param totalSize {number} 文件总大小
     * @param fileHash {string} 文件哈希值
     * @param scheduleId {number} 日程ID
     * @returns {Promise<{code, message, data}>}
     */
    initializeUpload: async function(fileName, totalSize, fileHash, scheduleId) {
        const params = new URLSearchParams({
            fileName: fileName,
            totalSize: totalSize,
            fileHash: fileHash,
            scheduleId: scheduleId
        });

        const response = await fetch(`${API_BASE_URL}/api/schedule/chunk/init?${params}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });

        if (!response.ok) {
            throw new Error(`初始化上传失败: ${response.statusText}`);
        }

        return await response.json();
    },

    /**
     * 上传单个分块
     * @param uploadId {string} 上传任务ID
     * @param chunkIndex {number} 分块索引
     * @param totalChunks {number} 总分块数
     * @param totalSize {number} 文件总大小
     * @param fileHash {string} 文件哈希值
     * @param chunkHash {string} 分块哈希值
     * @param fileName {string} 文件名
     * @param scheduleId {number} 日程ID
     * @param chunk {Blob} 分块数据
     * @returns {Promise<{code, message, data}>}
     */
    uploadChunk: async function(uploadId, chunkIndex, totalChunks, totalSize, 
                               fileHash, chunkHash, fileName, scheduleId, chunk) {
        const formData = new FormData();
        formData.append('uploadId', uploadId);
        formData.append('chunkIndex', chunkIndex);
        formData.append('totalChunks', totalChunks);
        formData.append('totalSize', totalSize);
        formData.append('fileHash', fileHash);
        formData.append('chunkHash', chunkHash);
        formData.append('fileName', fileName);
        formData.append('scheduleId', scheduleId);
        formData.append('chunk', chunk);

        const response = await fetch(`${API_BASE_URL}/api/schedule/chunk/upload`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error(`上传分块失败: ${response.statusText}`);
        }

        return await response.json();
    },

    /**
     * 查询上传进度
     * @param uploadId {string} 上传任务ID
     * @returns {Promise<{code, message, data}>}
     */
    queryProgress: async function(uploadId) {
        const response = await fetch(
            `${API_BASE_URL}/api/schedule/chunk/progress/${uploadId}`,
            {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            }
        );

        if (!response.ok) {
            throw new Error(`查询进度失败: ${response.statusText}`);
        }

        return await response.json();
    },

    /**
     * 完成上传并合并分块
     * @param uploadId {string} 上传任务ID
     * @param scheduleId {number} 日程ID
     * @param description {string} 附件描述
     * @returns {Promise<{code, message, data}>}
     */
    finishUpload: async function(uploadId, scheduleId, description = '') {
        const params = new URLSearchParams({
            uploadId: uploadId,
            scheduleId: scheduleId,
            description: description
        });

        const response = await fetch(
            `${API_BASE_URL}/api/schedule/chunk/finish?${params}`,
            {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            }
        );

        if (!response.ok) {
            throw new Error(`完成上传失败: ${response.statusText}`);
        }

        return await response.json();
    },

    /**
     * 取消上传
     * @param uploadId {string} 上传任务ID
     * @returns {Promise<{code, message, data}>}
     */
    cancelUpload: async function(uploadId) {
        const response = await fetch(
            `${API_BASE_URL}/api/schedule/chunk/${uploadId}`,
            {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            }
        );

        if (!response.ok) {
            throw new Error(`取消上传失败: ${response.statusText}`);
        }

        return await response.json();
    },

    /**
     * 获取上传统计信息
     * @param uploadId {string} 上传任务ID
     * @returns {Promise<{code, message, data}>}
     */
    getUploadStats: async function(uploadId) {
        const response = await fetch(
            `${API_BASE_URL}/api/schedule/chunk/stats/${uploadId}`,
            {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            }
        );

        if (!response.ok) {
            throw new Error(`获取统计信息失败: ${response.statusText}`);
        }

        return await response.json();
    },

    /**
     * 验证分块上传服务健康状态
     * @returns {Promise<{code, message, data}>}
     */
    checkHealth: async function() {
        const response = await fetch(
            `${API_BASE_URL}/api/schedule/chunk/health`,
            {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            }
        );

        if (!response.ok) {
            throw new Error('分块上传服务不可用');
        }

        return await response.json();
    }
};

/**
 * 文件处理工具类
 */
const FileUploadUtils = {
    /**
     * 计算文件SHA256哈希值
     * @param file {File} 文件对象
     * @returns {Promise<string>} SHA256哈希值（十六进制字符串）
     */
    calculateFileHash: async function(file) {
        const buffer = await file.arrayBuffer();
        const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    },

    /**
     * 计算分块SHA256哈希值
     * @param chunk {Blob} 分块数据
     * @returns {Promise<string>} SHA256哈希值
     */
    calculateChunkHash: async function(chunk) {
        const buffer = await chunk.arrayBuffer();
        const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    },

    /**
     * 分割文件为分块
     * @param file {File} 文件对象
     * @param chunkSize {number} 分块大小（字节），默认5MB
     * @returns {Array<Blob>} 分块数组
     */
    splitFileToChunks: function(file, chunkSize = 5 * 1024 * 1024) {
        const chunks = [];
        for (let i = 0; i < file.size; i += chunkSize) {
            chunks.push(file.slice(i, Math.min(i + chunkSize, file.size)));
        }
        return chunks;
    },

    /**
     * 格式化文件大小显示
     * @param bytes {number} 字节数
     * @returns {string} 格式化后的大小（如 "10.5 MB"）
     */
    formatFileSize: function(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
    },

    /**
     * 格式化时间（秒数转为可读字符串）
     * @param seconds {number} 秒数
     * @returns {string} 格式化时间
     */
    formatTime: function(seconds) {
        if (!seconds || seconds < 0) return '未知';
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);

        let result = '';
        if (hours > 0) result += hours + '小时 ';
        if (minutes > 0) result += minutes + '分钟 ';
        if (secs > 0 || result === '') result += secs + '秒';

        return result;
    },

    /**
     * 获取文件扩展名
     * @param fileName {string} 文件名
     * @returns {string} 扩展名（小写）
     */
    getFileExtension: function(fileName) {
        if (!fileName || !fileName.includes('.')) {
            return '';
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    },

    /**
     * 验证文件是否允许上传
     * @param file {File} 文件对象
     * @param maxSize {number} 最大大小（字节），默认100MB
     * @param allowedExtensions {Array<string>} 允许的扩展名列表
     * @returns {{valid: boolean, message: string}}
     */
    validateFile: function(file, maxSize = 100 * 1024 * 1024, 
                          allowedExtensions = ['ppt', 'pptx', 'pdf', 'doc', 'docx', 'xls', 'xlsx', 'txt', 'jpg', 'jpeg', 'png', 'gif']) {
        // 检查文件大小
        if (file.size > maxSize) {
            return {
                valid: false,
                message: `文件大小超过${FileUploadUtils.formatFileSize(maxSize)}的限制`
            };
        }

        // 检查文件扩展名
        const extension = FileUploadUtils.getFileExtension(file.name);
        if (allowedExtensions.length > 0 && !allowedExtensions.includes(extension)) {
            return {
                valid: false,
                message: `不支持的文件类型: .${extension}，允许的类型: ${allowedExtensions.join(', ')}`
            };
        }

        return { valid: true, message: '文件验证通过' };
    }
};

/**
 * 断点续传管理器类
 */
class ChunkUploadManager {
    constructor(file, scheduleId, options = {}) {
        this.file = file;
        this.scheduleId = scheduleId;
        this.uploadId = null;
        this.chunkSize = options.chunkSize || 5 * 1024 * 1024; // 5MB
        this.maxRetry = options.maxRetry || 3;
        this.retryDelay = options.retryDelay || 1000;
        
        // 状态管理
        this.uploadState = {
            uploadedChunks: new Map(), // chunkIndex -> {hash, uploaded: boolean}
            uploadedSize: 0,
            startTime: null,
            paused: false,
            cancelled: false,
            error: null
        };

        // 进度回调
        this.onProgress = options.onProgress || (() => {});
        this.onComplete = options.onComplete || (() => {});
        this.onError = options.onError || (() => {});
        this.onStatusChange = options.onStatusChange || (() => {});
    }

    /**
     * 开始上传
     * @returns {Promise<Object>} 附件信息
     */
    async start() {
        try {
            // 验证文件
            const validation = FileUploadUtils.validateFile(this.file);
            if (!validation.valid) {
                throw new Error(validation.message);
            }

            // 计算文件哈希
            this.onStatusChange('计算文件哈希...');
            const fileHash = await FileUploadUtils.calculateFileHash(this.file);

            // 初始化上传任务
            this.onStatusChange('初始化上传任务...');
            const initResult = await ChunkUploadAPI.initializeUpload(
                this.file.name,
                this.file.size,
                fileHash,
                this.scheduleId
            );

            if (initResult.code !== 200) {
                throw new Error(initResult.message);
            }

            this.uploadId = initResult.data.uploadId;
            this.uploadState.startTime = Date.now();

            // 分割文件
            const chunks = FileUploadUtils.splitFileToChunks(this.file, this.chunkSize);
            const totalChunks = chunks.length;

            // 初始化分块状态
            for (let i = 0; i < totalChunks; i++) {
                this.uploadState.uploadedChunks.set(i, {
                    hash: null,
                    uploaded: false,
                    retryCount: 0
                });
            }

            this.onStatusChange('开始上传...');

            // 上传分块（可以配置并发数）
            const concurrency = 3; // 同时上传3个分块
            for (let i = 0; i < totalChunks; i += concurrency) {
                if (this.uploadState.cancelled) {
                    throw new Error('上传已取消');
                }

                if (this.uploadState.paused) {
                    await this.waitForResume();
                }

                // 并发上传多个分块
                const uploadPromises = [];
                for (let j = 0; j < concurrency && (i + j) < totalChunks; j++) {
                    uploadPromises.push(
                        this.uploadChunkWithRetry(i + j, chunks[i + j], fileHash, totalChunks)
                    );
                }

                await Promise.all(uploadPromises);
            }

            // 完成上传
            this.onStatusChange('合并分块...');
            const finishResult = await ChunkUploadAPI.finishUpload(
                this.uploadId,
                this.scheduleId,
                ''
            );

            if (finishResult.code !== 200) {
                throw new Error(finishResult.message);
            }

            this.onStatusChange('上传完成');
            this.onComplete(finishResult.data);

            return finishResult.data;

        } catch (error) {
            this.uploadState.error = error.message;
            this.onError(error);
            throw error;
        }
    }

    /**
     * 带重试的分块上传
     * @private
     */
    async uploadChunkWithRetry(chunkIndex, chunk, fileHash, totalChunks) {
        const chunkState = this.uploadState.uploadedChunks.get(chunkIndex);
        
        for (let attempt = 0; attempt <= this.maxRetry; attempt++) {
            try {
                // 计算分块哈希
                const chunkHash = await FileUploadUtils.calculateChunkHash(chunk);
                chunkState.hash = chunkHash;

                // 上传分块
                const response = await ChunkUploadAPI.uploadChunk(
                    this.uploadId,
                    chunkIndex,
                    totalChunks,
                    this.file.size,
                    fileHash,
                    chunkHash,
                    this.file.name,
                    this.scheduleId,
                    chunk
                );

                if (response.code !== 200) {
                    throw new Error(response.message);
                }

                // 更新状态
                chunkState.uploaded = true;
                chunkState.retryCount = 0;
                this.uploadState.uploadedSize += chunk.size;

                // 回调进度
                const progress = Math.round((this.uploadState.uploadedSize / this.file.size) * 100);
                const elapsed = (Date.now() - this.uploadState.startTime) / 1000;
                const speed = this.uploadState.uploadedSize / elapsed / (1024 * 1024); // MB/s
                const eta = (this.file.size - this.uploadState.uploadedSize) / (speed * 1024 * 1024);

                this.onProgress({
                    progress,
                    uploadedSize: this.uploadState.uploadedSize,
                    totalSize: this.file.size,
                    speed: speed.toFixed(2) + ' MB/s',
                    eta: FileUploadUtils.formatTime(eta),
                    uploadedChunks: Array.from(this.uploadState.uploadedChunks.values())
                        .filter(s => s.uploaded).length,
                    totalChunks
                });

                return; // 上传成功，退出重试循环

            } catch (error) {
                chunkState.retryCount = attempt + 1;

                if (attempt < this.maxRetry) {
                    // 等待后重试
                    await new Promise(resolve => setTimeout(resolve, this.retryDelay * Math.pow(2, attempt)));
                } else {
                    throw new Error(`分块${chunkIndex}上传失败: ${error.message}`);
                }
            }
        }
    }

    /**
     * 暂停上传
     */
    pause() {
        this.uploadState.paused = true;
        this.onStatusChange('上传已暂停');
    }

    /**
     * 继续上传
     */
    resume() {
        this.uploadState.paused = false;
        this.onStatusChange('继续上传中...');
    }

    /**
     * 等待恢复
     * @private
     */
    async waitForResume() {
        return new Promise(resolve => {
            const checkInterval = setInterval(() => {
                if (!this.uploadState.paused) {
                    clearInterval(checkInterval);
                    resolve();
                }
            }, 100);
        });
    }

    /**
     * 取消上传
     */
    async cancel() {
        try {
            this.uploadState.cancelled = true;
            if (this.uploadId) {
                await ChunkUploadAPI.cancelUpload(this.uploadId);
            }
            this.onStatusChange('上传已取消');
        } catch (error) {
            console.error('取消上传失败:', error);
        }
    }

    /**
     * 获取上传进度信息
     */
    getProgress() {
        if (!this.uploadId) return null;

        const uploadedChunks = Array.from(this.uploadState.uploadedChunks.values())
            .filter(s => s.uploaded).length;
        const totalChunks = this.uploadState.uploadedChunks.size;
        const progress = Math.round((uploadedChunks / totalChunks) * 100);

        return {
            uploadId: this.uploadId,
            fileName: this.file.name,
            fileSize: this.file.size,
            uploadedSize: this.uploadState.uploadedSize,
            uploadedChunks,
            totalChunks,
            progress,
            isPaused: this.uploadState.paused,
            isCancelled: this.uploadState.cancelled,
            error: this.uploadState.error
        };
    }
}
