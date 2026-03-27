/*
 * 群组管理 & 聊天 API 前端集成代码
 * 用于 group-management.html 的后端 API 集成
 * 放在 admin-pc/conference-admin-pc/js/api/collaboration-api.js
 */

/**
 * 群组管理 API 模块
 */
const GroupAPI = {
    baseURL: '/api/group',

    /** 创建群组 */
    createGroup(groupData) {
        return fetch(`${this.baseURL}/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(groupData)
        }).then(r => this.handleResponse(r));
    },

    /** 获取群组列表 */
    listGroups(conferenceId) {
        const params = conferenceId ? `?conferenceId=${conferenceId}` : '';
        return fetch(`${this.baseURL}/list${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 获取我加入的群组 */
    getMyGroups(userId, conferenceId) {
        const params = new URLSearchParams();
        if (userId) params.append('userId', userId);
        if (conferenceId) params.append('conferenceId', conferenceId);
        return fetch(`${this.baseURL}/my-groups?${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 获取群组详情 */
    getGroupDetail(groupId) {
        return fetch(`${this.baseURL}/${groupId}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 获取群成员列表 */
    getMembers(groupId) {
        return fetch(`${this.baseURL}/${groupId}/members`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 添加群成员 */
    addMember(groupId, memberData) {
        return fetch(`${this.baseURL}/${groupId}/members`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(memberData)
        }).then(r => this.handleResponse(r));
    },

    /** 批量添加群成员 */
    batchAddMembers(groupId, members) {
        return fetch(`${this.baseURL}/${groupId}/members/batch`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(members)
        }).then(r => this.handleResponse(r));
    },

    /** 移除群成员 */
    removeMember(groupId, userId) {
        return fetch(`${this.baseURL}/${groupId}/members/${userId}`, {
            method: 'DELETE',
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 更新群设置 */
    updateSettings(groupId, settings) {
        return fetch(`${this.baseURL}/${groupId}/settings`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(settings)
        }).then(r => this.handleResponse(r));
    },

    /** 删除群组 */
    deleteGroup(groupId) {
        return fetch(`${this.baseURL}/${groupId}`, {
            method: 'DELETE',
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 更新成员角色 */
    updateMemberRole(groupId, userId, role) {
        return fetch(`${this.baseURL}/${groupId}/members/${userId}/role`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify({ role })
        }).then(r => this.handleResponse(r));
    },

    /** 群组统计 */
    getStats(conferenceId) {
        const params = conferenceId ? `?conferenceId=${conferenceId}` : '';
        return fetch(`${this.baseURL}/stats${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    handleResponse(response) {
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return response.json().then(data => {
            if (data.code === 200 || data.success) return data.data || data;
            else throw new Error(data.message || '请求失败');
        });
    },

    getAuthToken() {
        const token = localStorage.getItem('authToken') || '';
        return token ? `Bearer ${token}` : '';
    }
};

/**
 * 聊天消息 API 模块
 */
const ChatAPI = {
    baseURL: '/api/chat',

    /** 发送消息(HTTP) */
    sendMessage(messageData) {
        return fetch(`${this.baseURL}/send`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(messageData)
        }).then(r => this.handleResponse(r));
    },

    /** 获取群组聊天记录 */
    getGroupMessages(groupId, page = 1, size = 50) {
        return fetch(`${this.baseURL}/group/${groupId}/messages?page=${page}&size=${size}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 获取私聊记录 */
    getPrivateMessages(userId, targetId, page = 1, size = 50) {
        return fetch(`${this.baseURL}/private/messages?userId=${userId}&targetId=${targetId}&page=${page}&size=${size}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 搜索消息 */
    searchMessages(groupId, keyword) {
        return fetch(`${this.baseURL}/group/${groupId}/search?keyword=${encodeURIComponent(keyword)}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 获取消息统计 */
    getMessageStats(groupId) {
        return fetch(`${this.baseURL}/group/${groupId}/stats`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    handleResponse(response) {
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return response.json().then(data => {
            if (data.code === 200 || data.success) return data.data || data;
            else throw new Error(data.message || '请求失败');
        });
    },

    getAuthToken() {
        const token = localStorage.getItem('authToken') || '';
        return token ? `Bearer ${token}` : '';
    }
};

/**
 * 任务分派 API 模块
 */
const TaskAPI = {
    baseURL: '/api/task',

    /** 创建任务 */
    createTask(taskData) {
        return fetch(`${this.baseURL}/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(taskData)
        }).then(r => this.handleResponse(r));
    },

    /** 删除任务 */
    deleteTask(taskId) {
        return fetch(`${this.baseURL}/${taskId}`, {
            method: 'DELETE',
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 更新任务 */
    updateTask(taskId, taskData) {
        return fetch(`${this.baseURL}/${taskId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(taskData)
        }).then(r => this.handleResponse(r));
    },

    /** 获取任务详情 */
    getTaskDetail(taskId) {
        return fetch(`${this.baseURL}/${taskId}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 获取任务列表 */
    listTasks(conferenceId, status, category, page = 1, size = 20) {
        const params = new URLSearchParams();
        if (conferenceId) params.append('conferenceId', conferenceId);
        if (status) params.append('status', status);
        if (category) params.append('category', category);
        params.append('page', page);
        params.append('size', size);
        return fetch(`${this.baseURL}/list?${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 获取我的任务 */
    getMyTasks(userId, status, page = 1, size = 20) {
        const params = new URLSearchParams({ userId, page, size });
        if (status) params.append('status', status);
        return fetch(`${this.baseURL}/my-tasks?${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 分配执行人 */
    assignTask(taskId, assignees) {
        return fetch(`${this.baseURL}/${taskId}/assign`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(assignees)
        }).then(r => this.handleResponse(r));
    },

    /** 获取执行人列表 */
    getAssignees(taskId) {
        return fetch(`${this.baseURL}/${taskId}/assignees`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 提交任务 */
    submitTask(taskId, submitData) {
        return fetch(`${this.baseURL}/${taskId}/submit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(submitData)
        }).then(r => this.handleResponse(r));
    },

    /** 完成任务 */
    completeTask(taskId, data) {
        return fetch(`${this.baseURL}/${taskId}/complete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(data)
        }).then(r => this.handleResponse(r));
    },

    /** 取消任务 */
    cancelTask(taskId, data) {
        return fetch(`${this.baseURL}/${taskId}/cancel`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(data)
        }).then(r => this.handleResponse(r));
    },

    /** 催办任务 */
    urgeTask(taskId, data) {
        return fetch(`${this.baseURL}/${taskId}/urge`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(data)
        }).then(r => this.handleResponse(r));
    },

    /** 获取任务日志 */
    getTaskLogs(taskId) {
        return fetch(`${this.baseURL}/${taskId}/logs`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 任务统计 */
    getStats(conferenceId) {
        const params = conferenceId ? `?conferenceId=${conferenceId}` : '';
        return fetch(`${this.baseURL}/stats${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    handleResponse(response) {
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return response.json().then(data => {
            if (data.code === 200 || data.success) return data.data || data;
            else throw new Error(data.message || '请求失败');
        });
    },

    getAuthToken() {
        const token = localStorage.getItem('authToken') || '';
        return token ? `Bearer ${token}` : '';
    }
};

/**
 * 资料管理 API 模块
 */
const MaterialAPI = {
    baseURL: '/api/material',

    /** 上传资料 */
    uploadMaterial(materialData) {
        return fetch(`${this.baseURL}/upload`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': this.getAuthToken() },
            body: JSON.stringify(materialData)
        }).then(r => this.handleResponse(r));
    },

    /** 按群组获取资料 */
    listByGroup(groupId, category, page = 1, size = 20) {
        const params = new URLSearchParams({ page, size });
        if (category) params.append('category', category);
        return fetch(`${this.baseURL}/group/${groupId}?${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 按会议获取资料 */
    listByConference(conferenceId, category, page = 1, size = 20) {
        const params = new URLSearchParams({ page, size });
        if (conferenceId) params.append('conferenceId', conferenceId);
        if (category) params.append('category', category);
        return fetch(`${this.baseURL}/list?${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 获取资料详情 */
    getMaterialDetail(materialId) {
        return fetch(`${this.baseURL}/${materialId}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 删除资料 */
    deleteMaterial(materialId) {
        return fetch(`${this.baseURL}/${materialId}`, {
            method: 'DELETE',
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 下载资料 */
    downloadMaterial(materialId) {
        return fetch(`${this.baseURL}/${materialId}/download`, {
            method: 'POST',
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 搜索资料 */
    searchMaterials(conferenceId, keyword) {
        const params = new URLSearchParams({ keyword: keyword });
        if (conferenceId) params.append('conferenceId', conferenceId);
        return fetch(`${this.baseURL}/search?${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    /** 资料统计 */
    getStats(conferenceId) {
        const params = conferenceId ? `?conferenceId=${conferenceId}` : '';
        return fetch(`${this.baseURL}/stats${params}`, {
            headers: { 'Authorization': this.getAuthToken() }
        }).then(r => this.handleResponse(r));
    },

    handleResponse(response) {
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return response.json().then(data => {
            if (data.code === 200 || data.success) return data.data || data;
            else throw new Error(data.message || '请求失败');
        });
    },

    getAuthToken() {
        const token = localStorage.getItem('authToken') || '';
        return token ? `Bearer ${token}` : '';
    }
};

/**
 * WebSocket 聊天客户端
 */
class ChatWebSocket {
    constructor(userId, userName) {
        this.userId = userId;
        this.userName = userName;
        this.ws = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.handlers = {};
        this.heartbeatInterval = null;
    }

    /** 连接WebSocket */
    connect() {
        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${wsProtocol}//${window.location.host}/ws/chat?userId=${this.userId}`;
        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            console.log('[WS] 连接已建立');
            this.reconnectAttempts = 0;
            this.startHeartbeat();
            this.emit('connected', { userId: this.userId });
        };

        this.ws.onmessage = (event) => {
            try {
                const msg = JSON.parse(event.data);
                this.emit(msg.type, msg);
            } catch (e) {
                console.error('[WS] 消息解析失败', e);
            }
        };

        this.ws.onclose = () => {
            console.log('[WS] 连接已关闭');
            this.stopHeartbeat();
            if (this.reconnectAttempts < this.maxReconnectAttempts) {
                this.reconnectAttempts++;
                const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
                console.log(`[WS] ${delay}ms后重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
                setTimeout(() => this.connect(), delay);
            }
            this.emit('disconnected', {});
        };

        this.ws.onerror = (error) => {
            console.error('[WS] 错误', error);
            this.emit('error', error);
        };
    }

    /** 加入群组 */
    joinGroup(groupId) {
        this.send({ type: 'join_group', groupId: String(groupId), userName: this.userName });
    }

    /** 离开群组 */
    leaveGroup(groupId) {
        this.send({ type: 'leave_group', groupId: String(groupId) });
    }

    /** 发送聊天消息 */
    sendMessage(groupId, content, msgType = 'text', extra = null) {
        this.send({
            type: 'chat_message',
            groupId: String(groupId),
            senderId: String(this.userId),
            senderName: this.userName,
            content: content,
            msgType: msgType,
            extra: extra ? JSON.stringify(extra) : null
        });
    }

    /** 发送正在输入状态 */
    sendTyping(groupId) {
        this.send({ type: 'typing', groupId: String(groupId), userName: this.userName });
    }

    /** 发送原始数据 */
    send(data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(data));
        }
    }

    /** 注册事件处理器 */
    on(event, handler) {
        if (!this.handlers[event]) this.handlers[event] = [];
        this.handlers[event].push(handler);
    }

    /** 触发事件 */
    emit(event, data) {
        const handlers = this.handlers[event] || [];
        handlers.forEach(h => h(data));
    }

    /** 开始心跳 */
    startHeartbeat() {
        this.heartbeatInterval = setInterval(() => {
            this.send({ type: 'heartbeat' });
        }, 30000);
    }

    /** 停止心跳 */
    stopHeartbeat() {
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
            this.heartbeatInterval = null;
        }
    }

    /** 断开连接 */
    disconnect() {
        this.maxReconnectAttempts = 0;
        this.stopHeartbeat();
        if (this.ws) this.ws.close();
    }
}

// 导出模块
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { GroupAPI, ChatAPI, TaskAPI, MaterialAPI, ChatWebSocket };
}
