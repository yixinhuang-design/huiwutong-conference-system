/**
 * 实时数据更新模块
 * 实现WebSocket和轮询的实时数据更新机制
 */

class RealtimeUpdater {
    constructor(conferenceId) {
        this.conferenceId = conferenceId;
        this.socket = null;
        this.pollingTimers = new Map();
        this.updateCallbacks = new Map();
        this.isConnected = false;
        
        this.initialize();
    }
    
    /**
     * 初始化实时更新器
     */
    async initialize() {
        try {
            // 建立WebSocket连接
            await this.connectWebSocket();
            
            // 启动轮询备份
            this.startPollingBackup();
            
            console.log('实时更新器初始化完成');
        } catch (error) {
            console.error('实时更新器初始化失败:', error);
            // 如果WebSocket失败，使用轮询
            this.startPollingBackup();
        }
    }
    
    /**
     * 连接WebSocket
     */
    async connectWebSocket() {
        return new Promise((resolve, reject) => {
            try {
                const wsUrl = `wss://api.conference.com/ws/conference/${this.conferenceId}`;
                
                // 实际环境中使用真实WebSocket
                // this.socket = new WebSocket(wsUrl);
                
                // 模拟WebSocket连接
                console.log('WebSocket连接中...');
                
                // 模拟连接成功
                setTimeout(() => {
                    this.isConnected = true;
                    console.log('WebSocket连接成功');
                    this.setupSocketHandlers();
                    resolve();
                }, 100);
                
            } catch (error) {
                console.error('WebSocket连接失败:', error);
                this.isConnected = false;
                reject(error);
            }
        });
    }
    
    /**
     * 设置Socket处理器
     */
    setupSocketHandlers() {
        // 实际环境中的事件监听
        /*
        this.socket.onopen = () => {
            this.isConnected = true;
            console.log('WebSocket已打开');
            this.emit('connected');
        };
        
        this.socket.onmessage = (event) => {
            this.handleMessage(event.data);
        };
        
        this.socket.onerror = (error) => {
            console.error('WebSocket错误:', error);
            this.emit('error', error);
        };
        
        this.socket.onclose = () => {
            this.isConnected = false;
            console.log('WebSocket已关闭');
            this.emit('disconnected');
            
            // 尝试重连
            setTimeout(() => {
                this.connectWebSocket();
            }, 5000);
        };
        */
        
        // 模拟消息接收
        this.simulateRealtimeUpdates();
    }
    
    /**
     * 模拟实时更新（WebSocket未连接时的降级方案，不生成随机数据）
     * TODO: WebSocket连接成功后应由服务端推送真实数据
     */
    simulateRealtimeUpdates() {
        // WebSocket未实现前，不主动推送模拟数据
        // 当WebSocket连接后，此方法将被替换为真实推送接收
        console.log('[RealtimeUpdater] 等待WebSocket连接，暂不推送模拟数据');
    }
    
    /**
     * 生成默认更新数据（返回零值，避免模拟随机数据）
     */
    generateMockUpdate(type) {
        switch (type) {
            case 'registration':
                return {
                    newRegistrations: 0,
                    totalRegistered: 0
                };
            case 'task':
                return {
                    completedTasks: 0,
                    totalCompleted: 0
                };
            case 'attendance':
                return {
                    newCheckins: 0,
                    totalCheckedIn: 0
                };
            case 'group':
                return {
                    newMessages: 0,
                    activeGroups: 0
                };
            default:
                return {};
        }
    }
    
    /**
     * 处理消息
     */
    handleMessage(data) {
        try {
            const message = JSON.parse(data);
            
            // 触发对应的回调
            this.emit(message.type, message.data);
            
            // 触发通用更新回调
            this.emit('update', message);
        } catch (error) {
            console.error('处理消息失败:', error);
        }
    }
    
    /**
     * 启动轮询备份
     */
    startPollingBackup() {
        // 报名数据轮询（30秒）
        this.startPolling('registration', 30000, async () => {
            const data = await this.fetchRegistrationData();
            this.emit('registrationUpdate', data);
        });
        
        // 任务数据轮询（60秒）
        this.startPolling('task', 60000, async () => {
            const data = await this.fetchTaskData();
            this.emit('taskUpdate', data);
        });
        
        // 考勤数据轮询（30秒）
        this.startPolling('attendance', 30000, async () => {
            const data = await this.fetchAttendanceData();
            this.emit('attendanceUpdate', data);
        });
        
        console.log('轮询备份已启动');
    }
    
    /**
     * 启动轮询
     */
    startPolling(key, interval, callback) {
        // 清除已存在的定时器
        this.stopPolling(key);
        
        // 立即执行一次
        callback();
        
        // 设置定时器
        const timer = setInterval(callback, interval);
        this.pollingTimers.set(key, timer);
    }
    
    /**
     * 停止轮询
     */
    stopPolling(key) {
        const timer = this.pollingTimers.get(key);
        if (timer) {
            clearInterval(timer);
            this.pollingTimers.delete(key);
        }
    }
    
    /**
     * 停止所有轮询
     */
    stopAllPolling() {
        this.pollingTimers.forEach((timer, key) => {
            clearInterval(timer);
        });
        this.pollingTimers.clear();
    }
    
    /**
     * 获取报名数据
     */
    async fetchRegistrationData() {
        // 模拟API调用
        return {
            total: 500,
            registered: 356,
            pending: 23,
            rate: 71.2
        };
    }
    
    /**
     * 获取任务数据
     */
    async fetchTaskData() {
        return {
            total: 58,
            completed: 42,
            inProgress: 12,
            pending: 4,
            rate: 72.4
        };
    }
    
    /**
     * 获取考勤数据
     */
    async fetchAttendanceData() {
        return {
            total: 356,
            checkedIn: 328,
            notCheckedIn: 28,
            rate: 94.2
        };
    }
    
    /**
     * 订阅更新
     */
    subscribe(dataType, callback) {
        if (!this.updateCallbacks.has(dataType)) {
            this.updateCallbacks.set(dataType, []);
        }
        
        this.updateCallbacks.get(dataType).push(callback);
        
        // 返回取消订阅函数
        return () => {
            this.unsubscribe(dataType, callback);
        };
    }
    
    /**
     * 取消订阅
     */
    unsubscribe(dataType, callback) {
        const callbacks = this.updateCallbacks.get(dataType);
        if (callbacks) {
            const index = callbacks.indexOf(callback);
            if (index > -1) {
                callbacks.splice(index, 1);
            }
        }
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
        
        // 触发订阅的回调
        const callbacks = this.updateCallbacks.get(event);
        if (callbacks) {
            callbacks.forEach(callback => {
                try {
                    callback(data);
                } catch (error) {
                    console.error(`订阅回调错误 (${event}):`, error);
                }
            });
        }
    }
    
    /**
     * 发送消息
     */
    send(data) {
        if (!this.isConnected || !this.socket) {
            console.warn('WebSocket未连接，消息未发送');
            return false;
        }
        
        try {
            this.socket.send(JSON.stringify(data));
            return true;
        } catch (error) {
            console.error('发送消息失败:', error);
            return false;
        }
    }
    
    /**
     * 清理资源
     */
    cleanup() {
        // 关闭WebSocket
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
        
        // 停止所有轮询
        this.stopAllPolling();
        
        // 清除事件监听器
        this.eventListeners.clear();
        this.updateCallbacks.clear();
        
        console.log('实时更新器资源已清理');
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = RealtimeUpdater;
}

// 全局实例
window.realtimeUpdater = null;

/**
 * 初始化实时更新器
 */
function initializeRealtimeUpdater(conferenceId) {
    if (!conferenceId) {
        throw new Error('会议ID不能为空');
    }
    
    window.realtimeUpdater = new RealtimeUpdater(conferenceId);
    return window.realtimeUpdater;
}
