/**
 * 群组系统 - Group System
 * 实现会议内置群组管理功能
 */

class GroupSystemManager {
    constructor(conferenceId) {
        this.conferenceId = conferenceId;
        this.groups = new Map();
        this.messages = new Map();
        this.members = new Map();
        this.socket = null;
        
        this.initialize();
    }
    
    /**
     * 初始化群组系统
     */
    async initialize() {
        try {
            // 加载会议群组
            await this.loadConferenceGroups();
            
            // 建立实时连接
            this.establishRealtimeConnection();
            
            console.log('群组系统初始化完成');
        } catch (error) {
            console.error('群组系统初始化失败:', error);
            throw error;
        }
    }
    
    /**
     * 加载会议群组
     */
    async loadConferenceGroups() {
        const endpoint = `/api/conferences/${this.conferenceId}/groups`;
        
        // 模拟API调用
        const mockGroups = [
            {
                id: `${this.conferenceId}_managers`,
                name: '协管员工作群',
                type: 'manager',
                conferenceId: this.conferenceId,
                memberCount: 12,
                createdAt: new Date().toISOString(),
                permissions: ['task_assign', 'message_send', 'member_manage']
            },
            {
                id: `${this.conferenceId}_all_attendees`,
                name: '学员全员群',
                type: 'attendee_all',
                conferenceId: this.conferenceId,
                memberCount: 356,
                createdAt: new Date().toISOString(),
                permissions: ['message_send']
            }
        ];
        
        mockGroups.forEach(group => {
            this.groups.set(group.id, group);
        });
        
        return mockGroups;
    }
    
    /**
     * 创建新群组
     */
    async createGroup(groupData) {
        const group = {
            id: `${this.conferenceId}_group_${Date.now()}`,
            conferenceId: this.conferenceId,
            name: groupData.name,
            type: groupData.type,
            members: groupData.members || [],
            memberCount: groupData.members ? groupData.members.length : 0,
            createdAt: new Date().toISOString(),
            permissions: groupData.permissions || ['message_send']
        };
        
        this.groups.set(group.id, group);
        
        // 通知后端创建群组
        await this.syncGroupToBackend(group);
        
        return group;
    }
    
    /**
     * 同步群组到后端
     */
    async syncGroupToBackend(group) {
        // 模拟API调用
        console.log('同步群组到后端:', group);
        return { success: true };
    }
    
    /**
     * 获取群组信息
     */
    getGroup(groupId) {
        return this.groups.get(groupId);
    }
    
    /**
     * 获取所有群组
     */
    getAllGroups() {
        return Array.from(this.groups.values());
    }
    
    /**
     * 添加群组成员
     */
    async addMember(groupId, userId) {
        const group = this.groups.get(groupId);
        if (!group) {
            throw new Error(`群组不存在: ${groupId}`);
        }
        
        if (!group.members) {
            group.members = [];
        }
        
        if (group.members.includes(userId)) {
            throw new Error('用户已在群组中');
        }
        
        group.members.push(userId);
        group.memberCount = group.members.length;
        
        await this.syncGroupToBackend(group);
        
        return group;
    }
    
    /**
     * 移除群组成员
     */
    async removeMember(groupId, userId) {
        const group = this.groups.get(groupId);
        if (!group) {
            throw new Error(`群组不存在: ${groupId}`);
        }
        
        const index = group.members.indexOf(userId);
        if (index === -1) {
            throw new Error('用户不在群组中');
        }
        
        group.members.splice(index, 1);
        group.memberCount = group.members.length;
        
        await this.syncGroupToBackend(group);
        
        return group;
    }
    
    /**
     * 发送群消息
     */
    async sendMessage(groupId, message) {
        const group = this.groups.get(groupId);
        if (!group) {
            throw new Error(`群组不存在: ${groupId}`);
        }
        
        const messageObj = {
            id: `msg_${Date.now()}`,
            groupId: groupId,
            senderId: message.senderId || 'system',
            senderName: message.senderName || '系统',
            content: message.content,
            type: message.type || 'text',
            timestamp: new Date().toISOString()
        };
        
        // 存储消息
        if (!this.messages.has(groupId)) {
            this.messages.set(groupId, []);
        }
        this.messages.get(groupId).push(messageObj);
        
        // 通过WebSocket发送
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(JSON.stringify({
                type: 'group_message',
                data: messageObj
            }));
        }
        
        return messageObj;
    }
    
    /**
     * 发送任务到群组
     */
    async sendTaskToGroup(groupId, task) {
        const message = {
            type: 'task',
            content: {
                taskId: task.id,
                title: task.title,
                description: task.description,
                deadline: task.deadline,
                priority: task.priority,
                assignee: task.assignee
            }
        };
        
        return await this.sendMessage(groupId, message);
    }
    
    /**
     * 获取群组消息
     */
    getMessages(groupId, limit = 50) {
        const messages = this.messages.get(groupId) || [];
        return messages.slice(-limit);
    }
    
    /**
     * 建立实时连接
     */
    establishRealtimeConnection() {
        // 模拟WebSocket连接
        try {
            // 实际环境中应该是真实的WebSocket URL
            // this.socket = new WebSocket(`wss://api.conference.com/ws/conference/${this.conferenceId}`);
            
            // 模拟连接成功
            console.log('群组实时连接已建立');
            
            // 设置心跳
            this.setupHeartbeat();
            
        } catch (error) {
            console.error('建立实时连接失败:', error);
        }
    }
    
    /**
     * 设置心跳
     */
    setupHeartbeat() {
        setInterval(() => {
            if (this.socket && this.socket.readyState === WebSocket.OPEN) {
                this.socket.send(JSON.stringify({
                    type: 'ping',
                    timestamp: Date.now()
                }));
            }
        }, 30000); // 30秒心跳
    }
    
    /**
     * 处理接收到的消息
     */
    handleIncomingMessage(data) {
        try {
            const message = JSON.parse(data);
            
            switch (message.type) {
                case 'group_message':
                    this.handleGroupMessage(message.data);
                    break;
                case 'member_join':
                    this.handleMemberJoin(message.data);
                    break;
                case 'member_leave':
                    this.handleMemberLeave(message.data);
                    break;
                case 'pong':
                    // 心跳响应
                    break;
                default:
                    console.warn('未知消息类型:', message.type);
            }
        } catch (error) {
            console.error('处理消息失败:', error);
        }
    }
    
    /**
     * 处理群消息
     */
    handleGroupMessage(message) {
        const groupId = message.groupId;
        if (!this.messages.has(groupId)) {
            this.messages.set(groupId, []);
        }
        this.messages.get(groupId).push(message);
        
        // 触发事件
        this.emit('messageReceived', message);
    }
    
    /**
     * 处理成员加入
     */
    handleMemberJoin(data) {
        const { groupId, userId } = data;
        const group = this.groups.get(groupId);
        if (group && !group.members.includes(userId)) {
            group.members.push(userId);
            group.memberCount = group.members.length;
        }
        
        this.emit('memberJoin', data);
    }
    
    /**
     * 处理成员离开
     */
    handleMemberLeave(data) {
        const { groupId, userId } = data;
        const group = this.groups.get(groupId);
        if (group) {
            const index = group.members.indexOf(userId);
            if (index > -1) {
                group.members.splice(index, 1);
                group.memberCount = group.members.length;
            }
        }
        
        this.emit('memberLeave', data);
    }
    
    /**
     * 事件系统
     */
    eventListeners = new Map();
    
    on(event, callback) {
        if (!this.eventListeners.has(event)) {
            this.eventListeners.set(event, []);
        }
        this.eventListeners.get(event).push(callback);
    }
    
    off(event, callback) {
        const listeners = this.eventListeners.get(event);
        if (listeners) {
            const index = listeners.indexOf(callback);
            if (index > -1) {
                listeners.splice(index, 1);
            }
        }
    }
    
    emit(event, data) {
        const listeners = this.eventListeners.get(event);
        if (listeners) {
            listeners.forEach(callback => {
                try {
                    callback(data);
                } catch (error) {
                    console.error(`事件处理器错误 (${event}):`, error);
                }
            });
        }
    }
    
    /**
     * 获取群组统计
     */
    async getGroupStats(groupId) {
        const group = this.groups.get(groupId);
        if (!group) {
            return null;
        }
        
        const messages = this.messages.get(groupId) || [];
        
        return {
            groupId: groupId,
            memberCount: group.memberCount,
            messageCount: messages.length,
            todayMessages: messages.filter(m => {
                const msgDate = new Date(m.timestamp);
                const today = new Date();
                return msgDate.toDateString() === today.toDateString();
            }).length,
            activeMembers: this.getActiveMembers(groupId)
        };
    }
    
    /**
     * 获取活跃成员
     */
    getActiveMembers(groupId) {
        const messages = this.messages.get(groupId) || [];
        const activeSenders = new Set();
        
        const oneDayAgo = Date.now() - 24 * 60 * 60 * 1000;
        messages.forEach(msg => {
            if (new Date(msg.timestamp).getTime() > oneDayAgo) {
                activeSenders.add(msg.senderId);
            }
        });
        
        return activeSenders.size;
    }
    
    /**
     * 清理资源
     */
    cleanup() {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
        
        this.groups.clear();
        this.messages.clear();
        this.eventListeners.clear();
        
        console.log('群组系统资源已清理');
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = GroupSystemManager;
}

// 全局实例
window.groupSystem = null;

/**
 * 初始化群组系统
 */
async function initializeGroupSystem(conferenceId) {
    if (!conferenceId) {
        throw new Error('会议ID不能为空');
    }
    
    window.groupSystem = new GroupSystemManager(conferenceId);
    return window.groupSystem;
}
