/*
 * 日程管理 API 前端集成代码
 * 用于 schedule-mgr.html 的后端 API 集成
 * 放在 admin-pc/conference-admin-pc/js/api/schedule-api.js
 */

/**
 * 日程管理 API 模块
 */
const ScheduleAPI = {
    // 基础URL（根据部署环境调整）
    // 注意：后端实际运行在 localhost:8084，不是 9001
    baseURL: '/api/schedule',

    /**
     * 创建日程
     * @param {Object} scheduleData - 日程数据
     * @returns {Promise<Object>} 返回创建的日程详情
     */
    createSchedule(meetingId, scheduleData) {
        return fetch(`${this.baseURL}/create?meetingId=${meetingId}`, {
            method: 'POST',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(scheduleData)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 更新日程
     * @param {number} scheduleId - 日程ID
     * @param {Object} scheduleData - 日程数据
     * @returns {Promise<Object>} 返回更新后的日程详情
     */
    updateSchedule(scheduleId, scheduleData) {
        return fetch(`${this.baseURL}/${scheduleId}`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(scheduleData)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 删除日程
     * @param {number} scheduleId - 日程ID
     * @returns {Promise<void>}
     */
    deleteSchedule(scheduleId) {
        return fetch(`${this.baseURL}/${scheduleId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 获取日程详情
     * @param {number} scheduleId - 日程ID
     * @returns {Promise<Object>} 返回日程详情
     */
    getSchedule(scheduleId) {
        return fetch(`${this.baseURL}/${scheduleId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 分页查询日程列表
     * @param {number} meetingId - 会议ID
     * @param {number} pageNo - 页码（从1开始）
     * @param {number} pageSize - 每页数量
     * @returns {Promise<Object>} 返回分页结果
     */
    listSchedules(meetingId, pageNo = 1, pageSize = 10) {
        return fetch(`${this.baseURL}/list?meetingId=${meetingId}&pageNo=${pageNo}&pageSize=${pageSize}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 获取全部日程（不分页）
     * @param {string} meetingId - 会议ID
     * @returns {Promise<Array>} 返回日程列表
     */
    allSchedules(meetingId) {
        return fetch(`${this.baseURL}/all?meetingId=${meetingId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 查询需要签到的日程
     * @param {string} meetingId - 会议ID
     * @returns {Promise<Array>} 返回需要签到的日程列表
     */
    getNeedCheckinSchedules(meetingId) {
        return fetch(`${this.baseURL}/need-checkin?meetingId=${meetingId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 查询需要提醒的日程
     * @param {string} meetingId - 会议ID
     * @returns {Promise<Array>} 返回需要提醒的日程列表
     */
    getNeedReminderSchedules(meetingId) {
        return fetch(`${this.baseURL}/need-reminder?meetingId=${meetingId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 查询进行中的日程
     * @param {number} meetingId - 会议ID
     * @returns {Promise<Array>} 返回进行中的日程列表
     */
    getOngoingSchedules(meetingId) {
        return fetch(`${this.baseURL}/ongoing?meetingId=${meetingId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 查询即将开始的日程（30分钟内）
     * @param {number} meetingId - 会议ID
     * @returns {Promise<Array>} 返回即将开始的日程列表
     */
    getUpcomingSchedules(meetingId) {
        return fetch(`${this.baseURL}/upcoming?meetingId=${meetingId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 获取下一个日程
     * @param {number} meetingId - 会议ID
     * @returns {Promise<Object>} 返回下一个日程
     */
    getNextSchedule(meetingId) {
        return fetch(`${this.baseURL}/next?meetingId=${meetingId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 获取当前日程
     * @param {number} meetingId - 会议ID
     * @returns {Promise<Object>} 返回当前日程
     */
    getCurrentSchedule(meetingId) {
        return fetch(`${this.baseURL}/current?meetingId=${meetingId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 发布日程
     * @param {number} scheduleId - 日程ID
     * @returns {Promise<void>}
     */
    publishSchedule(scheduleId) {
        return fetch(`${this.baseURL}/${scheduleId}/publish`, {
            method: 'PUT',
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 取消日程
     * @param {number} scheduleId - 日程ID
     * @returns {Promise<void>}
     */
    cancelSchedule(scheduleId) {
        return fetch(`${this.baseURL}/${scheduleId}/cancel`, {
            method: 'PUT',
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 复制日程
     * @param {number} scheduleId - 日程ID
     * @returns {Promise<Object>} 返回新创建的日程详情
     */
    duplicateSchedule(scheduleId) {
        return fetch(`${this.baseURL}/${scheduleId}/duplicate`, {
            method: 'POST',
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 统计日程数量
     * @param {number} meetingId - 会议ID
     * @returns {Promise<number>} 返回日程数量
     */
    countSchedules(meetingId) {
        return fetch(`${this.baseURL}/count?meetingId=${meetingId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * ==================== 附件管理 API ====================
     */

    /**
     * 上传单个附件
     * @param {number} scheduleId - 日程ID
     * @param {File} file - 文件对象
     * @param {string} description - 文件描述（可选）
     * @returns {Promise<Object>} 返回附件信息
     */
    uploadAttachment(scheduleId, file, description = '') {
        const formData = new FormData();
        formData.append('file', file);
        if (description) {
            formData.append('description', description);
        }

        return fetch(`${this.baseURL}/attachment/upload?scheduleId=${scheduleId}`, {
            method: 'POST',
            headers: this.getAuthHeaders(false),
            body: formData
        }).then(response => this.handleResponse(response));
    },

    /**
     * 批量上传附件
     * @param {number} scheduleId - 日程ID
     * @param {File[]} files - 文件数组
     * @returns {Promise<Array>} 返回附件列表
     */
    uploadAttachmentBatch(scheduleId, files) {
        const formData = new FormData();
        for (let file of files) {
            formData.append('files', file);
        }

        return fetch(`${this.baseURL}/attachment/upload-batch?scheduleId=${scheduleId}`, {
            method: 'POST',
            headers: this.getAuthHeaders(false),
            body: formData
        }).then(response => this.handleResponse(response));
    },

    /**
     * 获取日程的所有附件列表
     * @param {number} scheduleId - 日程ID
     * @returns {Promise<Array>} 返回附件列表
     */
    listAttachments(scheduleId) {
        return fetch(`${this.baseURL}/attachment/list?scheduleId=${scheduleId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 获取附件详情
     * @param {number} attachmentId - 附件ID
     * @returns {Promise<Object>} 返回附件详情
     */
    getAttachment(attachmentId) {
        return fetch(`${this.baseURL}/attachment/${attachmentId}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 更新附件信息（描述）
     * @param {number} attachmentId - 附件ID
     * @param {Object} data - 更新数据 {description: '...'}
     * @returns {Promise<Object>} 返回更新后的附件
     */
    updateAttachment(attachmentId, data) {
        return fetch(`${this.baseURL}/attachment/${attachmentId}`, {
            method: 'PUT',
            headers: this.getAuthHeaders(),
            body: JSON.stringify(data)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 删除附件
     * @param {number} attachmentId - 附件ID
     * @returns {Promise<void>}
     */
    deleteAttachment(attachmentId) {
        return fetch(`${this.baseURL}/attachment/${attachmentId}`, {
            method: 'DELETE',
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 获取附件下载地址
     * @param {number} attachmentId - 附件ID
     * @returns {Promise<Object>} 返回 {downloadUrl: '...'}
     */
    getDownloadUrl(attachmentId) {
        return fetch(`${this.baseURL}/attachment/${attachmentId}/download-url`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 记录附件下载（用于统计下载次数）
     * @param {number} attachmentId - 附件ID
     * @returns {Promise<void>}
     */
    recordDownload(attachmentId) {
        return fetch(`${this.baseURL}/attachment/${attachmentId}/download`, {
            method: 'POST',
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 验证文件是否允许上传
     * @param {string} fileName - 文件名
     * @param {number} fileSize - 文件大小（字节）
     * @returns {Promise<Object>} 返回 {isValid: boolean, message: '...'}
     */
    validateFile(fileName, fileSize) {
        return fetch(`${this.baseURL}/attachment/validate?fileName=${encodeURIComponent(fileName)}&fileSize=${fileSize}`, {
            headers: this.getAuthHeaders(false)
        }).then(response => this.handleResponse(response));
    },

    /**
     * 处理响应
     * @private
     */
    handleResponse(response) {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json().then(data => {
            if (data.code === 200 || data.code === 0 || data.success) {
                return data.data || data;
            } else {
                throw new Error(data.message || '请求失败');
            }
        });
    },

    /**
     * 获取认证令牌
     * @private
     */
    getAuthToken() {
        // 从localStorage获取token，添加Bearer前缀
        const token = localStorage.getItem('authToken') || '';
        return token ? `Bearer ${token}` : '';
    },

    /**
     * 获取完整请求头（含认证和租户ID）
     * @private
     */
    getAuthHeaders(withContentType = true) {
        const headers = {};
        if (withContentType) headers['Content-Type'] = 'application/json';
        const token = localStorage.getItem('authToken') || '';
        if (token) headers['Authorization'] = `Bearer ${token}`;
        const tenantId = localStorage.getItem('tenantId') || '';
        if (tenantId) headers['X-Tenant-Id'] = tenantId;
        return headers;
    }
};

// 导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ScheduleAPI;
}
