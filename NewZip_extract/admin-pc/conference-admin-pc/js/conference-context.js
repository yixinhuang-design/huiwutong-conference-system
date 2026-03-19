/**
 * 会议上下文管理系统
 * 实现基于具体会议的全局数据管理和状态控制
 */

class ConferenceContextManager {
    constructor() {
        this.currentConference = null;
        this.dataSources = new Map();
        this.realtimeConnections = new Map();
        this.eventListeners = new Map();
        this.cache = new Map();
        
        // 初始化
        this.initialize();
    }
    
    /**
     * 初始化会议上下文管理器
     */
    async initialize() {
        try {
            // 从URL参数或localStorage获取当前会议ID
            const conferenceId = this.getConferenceIdFromUrl() || 
                                localStorage.getItem('currentConferenceId');
            
            if (conferenceId) {
                await this.loadConference(conferenceId);
            }
            
            // 设置全局事件监听
            this.setupGlobalEventListeners();
            
            console.log('会议上下文管理器初始化完成');
        } catch (error) {
            console.error('会议上下文管理器初始化失败:', error);
            throw error;
        }
    }
    
    /**
     * 从URL获取会议ID
     */
    getConferenceIdFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('conferenceId');
    }
    
    /**
     * 加载会议数据
     */
    async loadConference(conferenceId) {
        try {
            // 检查缓存
            if (this.cache.has(conferenceId)) {
                this.currentConference = this.cache.get(conferenceId);
                this.emit('conferenceLoaded', this.currentConference);
                return this.currentConference;
            }
            
            // 从API加载会议数据
            const conferenceData = await this.fetchConferenceData(conferenceId);
            
            // 设置当前会议
            this.currentConference = conferenceData;
            
            // 缓存数据
            this.cache.set(conferenceId, conferenceData);
            
            // 保存到localStorage
            localStorage.setItem('currentConferenceId', conferenceId);
            
            // 初始化会议相关系统
            await this.initializeConferenceSystems(conferenceData);
            
            // 触发事件
            this.emit('conferenceLoaded', conferenceData);
            
            return conferenceData;
        } catch (error) {
            console.error('加载会议数据失败:', error);
            throw error;
        }
    }
    
    /**
     * 获取会议数据
     */
    async fetchConferenceData(conferenceId) {
        // 模拟API调用
        const mockData = {
            id: conferenceId,
            name: '2024年度技术峰会',
            status: 'preparing',
            timeRange: {
                start: '2024-06-15 09:00:00',
                end: '2024-06-16 18:00:00'
            },
            location: {
                venue: '北京国际会议中心',
                address: '北京市朝阳区北辰东路8号',
                coordinates: { lat: 39.9042, lng: 116.4074 }
            },
            capacity: {
                total: 500,
                registered: 356,
                available: 144
            },
            groups: {
                managerGroup: {
                    id: `${conferenceId}_managers`,
                    name: '2024技术峰会-协管员工作群',
                    members: ['user_001', 'user_002', 'user_003']
                },
                attendeeGroups: [
                    {
                        id: `${conferenceId}_group1`,
                        name: '第一学习小组',
                        members: ['user_101', 'user_102', 'user_103']
                    },
                    {
                        id: `${conferenceId}_group2`,
                        name: '第二学习小组',
                        members: ['user_201', 'user_202', 'user_203']
                    }
                ]
            },
            systems: {
                registration: true,
                notification: true,
                collaboration: true,
                ai: true,
                navigation: true
            },
            permissions: {
                canCreate: true,
                canEdit: true,
                canDelete: false,
                canManageUsers: true,
                canManageGroups: true
            }
        };
        
        return mockData;
    }
    
    /**
     * 初始化会议相关系统
     */
    async initializeConferenceSystems(conferenceData) {
        const { id } = conferenceData;
        
        // 初始化数据源
        this.initializeDataSources(id);
        
        // 建立实时连接
        this.establishRealtimeConnections(id);
        
        // 初始化权限系统
        this.initializePermissions(conferenceData.permissions);
        
        console.log(`会议系统初始化完成: ${id}`);
    }
    
    /**
     * 初始化数据源
     */
    initializeDataSources(conferenceId) {
        const baseUrl = '/api/conferences';
        
        this.dataSources.set('registration', `${baseUrl}/${conferenceId}/registration`);
        this.dataSources.set('tasks', `${baseUrl}/${conferenceId}/tasks`);
        this.dataSources.set('groups', `${baseUrl}/${conferenceId}/groups`);
        this.dataSources.set('navigation', `${baseUrl}/${conferenceId}/navigation`);
        this.dataSources.set('ai', `${baseUrl}/${conferenceId}/ai-knowledge`);
        this.dataSources.set('seating', `${baseUrl}/${conferenceId}/seating`);
        this.dataSources.set('notifications', `${baseUrl}/${conferenceId}/notifications`);
        this.dataSources.set('attendance', `${baseUrl}/${conferenceId}/attendance`);
    }
    
    /**
     * 建立实时连接
     */
    establishRealtimeConnections(conferenceId) {
        // 模拟WebSocket连接
        const socket = {
            conferenceId,
            connected: true,
            listeners: new Map()
        };
        
        this.realtimeConnections.set(conferenceId, socket);
        
        // 设置心跳检测
        this.setupHeartbeat(conferenceId);
        
        console.log(`实时连接已建立: ${conferenceId}`);
    }
    
    /**
     * 设置心跳检测
     */
    setupHeartbeat(conferenceId) {
        setInterval(() => {
            const socket = this.realtimeConnections.get(conferenceId);
            if (socket && socket.connected) {
                // 发送心跳
                this.emit('heartbeat', { conferenceId, timestamp: Date.now() });
            }
        }, 30000); // 30秒心跳
    }
    
    /**
     * 初始化权限系统
     */
    initializePermissions(permissions) {
        this.permissions = permissions;
        this.emit('permissionsUpdated', permissions);
    }
    
    /**
     * 切换会议
     */
    async switchConference(conferenceId) {
        try {
            // 清理当前会议资源
            if (this.currentConference) {
                await this.cleanupConference(this.currentConference.id);
            }
            
            // 加载新会议
            await this.loadConference(conferenceId);
            
            // 更新URL
            this.updateUrl(conferenceId);
            
            console.log(`已切换到会议: ${conferenceId}`);
        } catch (error) {
            console.error('切换会议失败:', error);
            throw error;
        }
    }
    
    /**
     * 清理会议资源
     */
    async cleanupConference(conferenceId) {
        // 断开实时连接
        const socket = this.realtimeConnections.get(conferenceId);
        if (socket) {
            socket.connected = false;
            this.realtimeConnections.delete(conferenceId);
        }
        
        // 清理事件监听器
        this.cleanupEventListeners(conferenceId);
        
        console.log(`会议资源已清理: ${conferenceId}`);
    }
    
    /**
     * 清理事件监听器
     */
    cleanupEventListeners(conferenceId) {
        const listeners = this.eventListeners.get(conferenceId);
        if (listeners) {
            listeners.forEach(listener => {
                if (listener.cleanup) {
                    listener.cleanup();
                }
            });
            this.eventListeners.delete(conferenceId);
        }
    }
    
    /**
     * 更新URL
     */
    updateUrl(conferenceId) {
        const url = new URL(window.location);
        url.searchParams.set('conferenceId', conferenceId);
        window.history.pushState({}, '', url);
    }
    
    /**
     * 获取会议数据
     */
    getConferenceData() {
        return this.currentConference;
    }
    
    /**
     * 获取数据源URL
     */
    getDataSourceUrl(dataType) {
        return this.dataSources.get(dataType);
    }
    
    /**
     * 检查权限
     */
    hasPermission(permission) {
        return this.permissions && this.permissions[permission] === true;
    }
    
    /**
     * 同步数据
     */
    async syncData(dataType, payload) {
        try {
            const endpoint = this.dataSources.get(dataType);
            if (!endpoint) {
                throw new Error(`未知的数据类型: ${dataType}`);
            }
            
            const requestPayload = {
                ...payload,
                conferenceId: this.currentConference.id,
                timestamp: new Date().toISOString()
            };
            
            // 模拟API调用
            const result = await this.apiCall(endpoint, requestPayload);
            
            // 触发数据更新事件
            this.emit('dataUpdated', { dataType, payload: requestPayload, result });
            
            return result;
        } catch (error) {
            console.error('数据同步失败:', error);
            throw error;
        }
    }
    
    /**
     * API调用
     */
    async apiCall(endpoint, payload) {
        // 模拟API调用
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    success: true,
                    data: payload,
                    timestamp: new Date().toISOString()
                });
            }, 100);
        });
    }
    
    /**
     * 设置全局事件监听
     */
    setupGlobalEventListeners() {
        // 监听页面卸载
        window.addEventListener('beforeunload', () => {
            if (this.currentConference) {
                this.cleanupConference(this.currentConference.id);
            }
        });
        
        // 监听浏览器后退/前进
        window.addEventListener('popstate', (event) => {
            const conferenceId = this.getConferenceIdFromUrl();
            if (conferenceId && conferenceId !== this.currentConference?.id) {
                this.switchConference(conferenceId);
            }
        });
    }
    
    /**
     * 事件系统
     */
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
     * 获取会议统计信息
     */
    async getConferenceStats() {
        if (!this.currentConference) {
            return null;
        }
        
        try {
            const stats = {
                registration: await this.getRegistrationStats(),
                tasks: await this.getTaskStats(),
                groups: await this.getGroupStats(),
                attendance: await this.getAttendanceStats()
            };
            
            return stats;
        } catch (error) {
            console.error('获取会议统计失败:', error);
            return null;
        }
    }
    
    /**
     * 获取报名统计
     */
    async getRegistrationStats() {
        // 模拟数据
        return {
            total: 500,
            registered: 356,
            pending: 23,
            rejected: 5,
            rate: 71.2
        };
    }
    
    /**
     * 获取任务统计
     */
    async getTaskStats() {
        // 模拟数据
        return {
            total: 58,
            completed: 42,
            inProgress: 12,
            pending: 4,
            rate: 72.4
        };
    }
    
    /**
     * 获取群组统计
     */
    async getGroupStats() {
        if (!this.currentConference?.groups) {
            return null;
        }
        
        const { managerGroup, attendeeGroups } = this.currentConference.groups;
        
        return {
            managerGroup: {
                id: managerGroup.id,
                name: managerGroup.name,
                memberCount: managerGroup.members.length
            },
            attendeeGroups: attendeeGroups.map(group => ({
                id: group.id,
                name: group.name,
                memberCount: group.members.length
            })),
            totalGroups: attendeeGroups.length + 1
        };
    }
    
    /**
     * 获取考勤统计
     */
    async getAttendanceStats() {
        // 模拟数据
        return {
            total: 356,
            checkedIn: 328,
            notCheckedIn: 28,
            rate: 94.2
        };
    }
}

// 创建全局实例
window.conferenceContext = new ConferenceContextManager();

// 导出类
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ConferenceContextManager;
}
