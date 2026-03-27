/**
 * 安全管理器
 * 实现SM4加密和RBAC权限控制
 */

class SecurityManager {
    constructor() {
        this.encryptionAlgorithm = 'SM4';
        this.permissionModel = 'RBAC';
        this.roles = new Map();
        this.permissions = new Map();
        this.auditLogs = [];
        
        this.initialize();
    }
    
    /**
     * 初始化安全管理器
     */
    initialize() {
        // 定义角色和权限
        this.defineRolesAndPermissions();
        
        console.log('安全管理器初始化完成');
    }
    
    /**
     * 定义角色和权限
     */
    defineRolesAndPermissions() {
        // 会议管理员
        this.roles.set('conference_manager', {
            name: '会议管理员',
            permissions: [
                'conference:create',
                'conference:edit',
                'conference:delete',
                'registration:manage',
                'task:assign',
                'notification:send',
                'seating:arrange',
                'ai:manage',
                'navigation:configure',
                'data:view',
                'data:export',
                'group:manage',
                'user:manage'
            ]
        });
        
        // 协管员
        this.roles.set('assistant', {
            name: '协管员',
            permissions: [
                'task:view',
                'task:execute',
                'signin:manage',
                'group:view',
                'group:message',
                'data:view',
                'navigation:use'
            ]
        });
        
        // 参会用户
        this.roles.set('attendee', {
            name: '参会用户',
            permissions: [
                'conference:view',
                'registration:submit',
                'signin:self',
                'schedule:view',
                'seating:view',
                'group:view',
                'group:message',
                'navigation:use',
                'ai:use',
                'material:view'
            ]
        });
    }
    
    /**
     * SM4数据加密
     */
    encryptData(data, conferenceId) {
        try {
            const key = this.generateConferenceKey(conferenceId);
            const jsonData = JSON.stringify(data);
            
            // 实际环境中应使用SM4加密库
            // const encrypted = SM4.encrypt(jsonData, key);
            
            // 模拟加密
            const encrypted = btoa(jsonData);
            
            return encrypted;
        } catch (error) {
            console.error('数据加密失败:', error);
            throw error;
        }
    }
    
    /**
     * SM4数据解密
     */
    decryptData(encryptedData, conferenceId) {
        try {
            const key = this.generateConferenceKey(conferenceId);
            
            // 实际环境中应使用SM4解密库
            // const decrypted = SM4.decrypt(encryptedData, key);
            
            // 模拟解密
            const decrypted = atob(encryptedData);
            
            return JSON.parse(decrypted);
        } catch (error) {
            console.error('数据解密失败:', error);
            throw error;
        }
    }
    
    /**
     * 生成会议密钥
     */
    generateConferenceKey(conferenceId) {
        // 基于会议ID生成唯一密钥
        const salt = 'conference_secret_salt_2024';
        const combined = conferenceId + salt;
        
        // 简单的哈希（实际应使用加密库）
        let hash = 0;
        for (let i = 0; i < combined.length; i++) {
            const char = combined.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash;
        }
        
        return Math.abs(hash).toString(16).padEnd(32, '0').substring(0, 32);
    }
    
    /**
     * RBAC权限检查
     */
    checkPermission(userId, permission, conferenceId) {
        try {
            // 获取用户角色
            const userRole = this.getUserRole(userId, conferenceId);
            
            if (!userRole) {
                return false;
            }
            
            // 获取角色权限
            const rolePermissions = this.roles.get(userRole);
            
            if (!rolePermissions) {
                return false;
            }
            
            // 检查权限
            return rolePermissions.permissions.includes(permission);
        } catch (error) {
            console.error('权限检查失败:', error);
            return false;
        }
    }
    
    /**
     * 获取用户角色
     */
    getUserRole(userId, conferenceId) {
        // 模拟获取用户角色
        // 实际应从数据库查询
        
        const roleCache = this.permissions.get(`${userId}_${conferenceId}`);
        if (roleCache) {
            return roleCache;
        }
        
        // 默认返回参会用户角色
        return 'attendee';
    }
    
    /**
     * 设置用户角色
     */
    setUserRole(userId, conferenceId, role) {
        if (!this.roles.has(role)) {
            throw new Error(`未知角色: ${role}`);
        }
        
        this.permissions.set(`${userId}_${conferenceId}`, role);
        
        // 记录审计日志
        this.logAudit({
            action: 'set_user_role',
            userId: userId,
            conferenceId: conferenceId,
            role: role,
            timestamp: new Date().toISOString()
        });
    }
    
    /**
     * 批量检查权限
     */
    checkMultiplePermissions(userId, permissions, conferenceId) {
        return permissions.map(permission => ({
            permission: permission,
            granted: this.checkPermission(userId, permission, conferenceId)
        }));
    }
    
    /**
     * 多租户数据隔离
     */
    enforceDataIsolation(query, conferenceId) {
        // 在SQL查询中自动注入会议ID条件
        if (query.includes('WHERE')) {
            return query.replace('WHERE', `WHERE conference_id = '${conferenceId}' AND`);
        } else if (query.includes('where')) {
            return query.replace('where', `where conference_id = '${conferenceId}' AND`);
        } else {
            return query + ` WHERE conference_id = '${conferenceId}'`;
        }
    }
    
    /**
     * 记录审计日志
     */
    logAudit(auditData) {
        const log = {
            id: Date.now(),
            ...auditData,
            timestamp: auditData.timestamp || new Date().toISOString(),
            ip: this.getUserIP()
        };
        
        this.auditLogs.push(log);
        
        // 实际环境中应持久化到数据库
        console.log('审计日志:', log);
    }
    
    /**
     * 获取用户IP
     */
    getUserIP() {
        // 实际应从请求中获取
        return '127.0.0.1';
    }
    
    /**
     * 获取审计日志
     */
    getAuditLogs(filter = {}) {
        let logs = this.auditLogs;
        
        if (filter.userId) {
            logs = logs.filter(log => log.userId === filter.userId);
        }
        
        if (filter.conferenceId) {
            logs = logs.filter(log => log.conferenceId === filter.conferenceId);
        }
        
        if (filter.action) {
            logs = logs.filter(log => log.action === filter.action);
        }
        
        return logs;
    }
    
    /**
     * Token验证
     */
    verifyToken(token) {
        try {
            // 实际应使用JWT库验证
            // const decoded = jwt.verify(token, SECRET_KEY);
            
            // 模拟验证
            if (!token || token === 'invalid') {
                return {
                    valid: false,
                    error: 'Invalid token'
                };
            }
            
            return {
                valid: true,
                userId: 'user_001',
                exp: Date.now() + 3600000
            };
        } catch (error) {
            console.error('Token验证失败:', error);
            return {
                valid: false,
                error: error.message
            };
        }
    }
    
    /**
     * 生成Token
     */
    generateToken(userId, conferenceId) {
        // 实际应使用JWT库
        // const token = jwt.sign({ userId, conferenceId }, SECRET_KEY, { expiresIn: '24h' });
        
        // 模拟生成
        const payload = {
            userId: userId,
            conferenceId: conferenceId,
            iat: Date.now(),
            exp: Date.now() + 86400000 // 24小时
        };
        
        return btoa(JSON.stringify(payload));
    }
    
    /**
     * 敏感数据脱敏
     */
    maskSensitiveData(data, type) {
        switch (type) {
            case 'phone':
                // 手机号脱敏：138****1234
                return data.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
            
            case 'idcard':
                // 身份证脱敏：110101********1234
                return data.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
            
            case 'email':
                // 邮箱脱敏：abc***@example.com
                return data.replace(/(.{3}).*(@.*)/, '$1***$2');
            
            default:
                return data;
        }
    }
    
    /**
     * XSS防护
     */
    sanitizeInput(input) {
        if (typeof input !== 'string') {
            return input;
        }
        
        return input
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#x27;')
            .replace(/\//g, '&#x2F;');
    }
    
    /**
     * SQL注入防护
     */
    escapeSQLInput(input) {
        if (typeof input !== 'string') {
            return input;
        }
        
        return input
            .replace(/'/g, "''")
            .replace(/;/g, '')
            .replace(/--/g, '')
            .replace(/\/\*/g, '')
            .replace(/\*\//g, '');
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = SecurityManager;
}

// 全局实例
window.securityManager = new SecurityManager();
