/**
 * 会议生命周期管理器
 * 负责会议的创建、启动、结束等生命周期管理
 */

class ConferenceLifecycleManager {
    constructor() {
        this.conferences = new Map();
        this.eventListeners = new Map();
    }
    
    /**
     * 创建新会议
     */
    async createConference(conferenceData) {
        try {
            // 1. 验证数据
            this.validateConferenceData(conferenceData);
            
            // 2. 生成会议ID
            const conferenceId = this.generateConferenceId();
            
            // 3. 创建会议记录
            const conference = {
                id: conferenceId,
                ...conferenceData,
                status: 'preparing',
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
            };
            
            // 4. 保存会议
            this.conferences.set(conferenceId, conference);
            
            // 5. 初始化会议系统
            await this.initializeConferenceSystems(conferenceId);
            
            // 6. 创建群组
            await this.createConferenceGroups(conferenceId);
            
            // 7. 触发事件
            this.emit('conferenceCreated', conference);
            
            console.log('会议创建成功:', conferenceId);
            return conference;
        } catch (error) {
            console.error('创建会议失败:', error);
            throw error;
        }
    }
    
    /**
     * 验证会议数据
     */
    validateConferenceData(data) {
        if (!data.name) {
            throw new Error('会议名称不能为空');
        }
        if (!data.timeRange || !data.timeRange.start || !data.timeRange.end) {
            throw new Error('会议时间不能为空');
        }
        if (!data.location) {
            throw new Error('会议地点不能为空');
        }
    }
    
    /**
     * 生成会议ID
     */
    generateConferenceId() {
        const date = new Date();
        const dateStr = date.toISOString().slice(0, 10).replace(/-/g, '');
        const random = Math.random().toString(36).substring(2, 5);
        return `conf_${dateStr}_${random}`;
    }
    
    /**
     * 初始化会议系统
     */
    async initializeConferenceSystems(conferenceId) {
        console.log('初始化会议系统:', conferenceId);
        // 实际实现中会初始化各个子系统
    }
    
    /**
     * 创建会议群组
     */
    async createConferenceGroups(conferenceId) {
        console.log('创建会议群组:', conferenceId);
        // 实际实现中会创建协管员群和学员群
    }
    
    /**
     * 开始会议
     */
    async startConference(conferenceId) {
        const conference = this.conferences.get(conferenceId);
        if (!conference) {
            throw new Error('会议不存在');
        }
        
        conference.status = 'ongoing';
        conference.updatedAt = new Date().toISOString();
        
        this.emit('conferenceStarted', conference);
        console.log('会议已开始:', conferenceId);
    }
    
    /**
     * 结束会议
     */
    async endConference(conferenceId) {
        const conference = this.conferences.get(conferenceId);
        if (!conference) {
            throw new Error('会议不存在');
        }
        
        conference.status = 'ended';
        conference.updatedAt = new Date().toISOString();
        
        this.emit('conferenceEnded', conference);
        console.log('会议已结束:', conferenceId);
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
    
    emit(event, data) {
        const listeners = this.eventListeners.get(event);
        if (listeners) {
            listeners.forEach(callback => callback(data));
        }
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ConferenceLifecycleManager;
}

// 全局实例
window.conferenceManager = new ConferenceLifecycleManager();
