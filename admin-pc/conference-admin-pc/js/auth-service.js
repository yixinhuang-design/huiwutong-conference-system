/**
 * API Service - 智能会议系统前端API调用封装
 * 用于管理所有与后端的通信
 */

class AuthService {
    constructor() {
        this.baseURL = 'http://localhost:8081/api';
        this.timeout = 10000;
    }

    /**
     * 发送HTTP请求
     */
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        };

        // 添加认证Token
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), this.timeout);

            const response = await fetch(url, {
                ...config,
                signal: controller.signal
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            return data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * 处理错误信息
     */
    handleError(error) {
        if (error.name === 'AbortError') {
            return new Error('请求超时');
        }
        if (error instanceof TypeError) {
            return new Error('网络连接失败，请检查服务器是否在线');
        }
        return error;
    }

    /**
     * 用户登录
     */
    async login(username, password, tenantCode = null) {
        const body = {
            username,
            password
        };
        
        if (tenantCode) {
            body.tenantCode = tenantCode;
        }

        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify(body)
        });
    }

    /**
     * 刷新Token
     */
    async refreshToken(refreshToken) {
        return this.request('/auth/refresh', {
            method: 'POST',
            body: JSON.stringify({ refreshToken })
        });
    }

    /**
     * 获取当前用户信息
     */
    async getCurrentUser() {
        return this.request('/auth/me', {
            method: 'GET'
        });
    }

    /**
     * 登出
     */
    async logout() {
        try {
            await this.request('/auth/logout', {
                method: 'POST'
            });
        } catch (error) {
            // 即使登出请求失败，也清除本地存储
            console.error('登出请求失败:', error);
        } finally {
            this.clearAuth();
        }
    }

    /**
     * 保存认证信息
     */
    saveAuth(data) {
        if (data.accessToken) {
            localStorage.setItem('authToken', data.accessToken);
        }
        if (data.refreshToken) {
            localStorage.setItem('refreshToken', data.refreshToken);
        }
        if (data.tokenType) {
            localStorage.setItem('tokenType', data.tokenType);
        }
        if (data.expiresIn) {
            localStorage.setItem('expiresIn', data.expiresIn);
            localStorage.setItem('tokenExpireTime', Date.now() + data.expiresIn * 1000);
        }
        if (data.userInfo) {
            const info = data.userInfo;
            if (info.id) localStorage.setItem('userId', info.id);
            if (info.username) localStorage.setItem('username', info.username);
            if (info.realName) localStorage.setItem('realName', info.realName);
            if (info.userType) localStorage.setItem('userType', info.userType);
            if (info.tenantId) localStorage.setItem('tenantId', info.tenantId);
            if (info.tenantName) localStorage.setItem('tenantName', info.tenantName);
        }
    }

    /**
     * 清除认证信息
     */
    clearAuth() {
        const authKeys = [
            'authToken',
            'refreshToken',
            'tokenType',
            'expiresIn',
            'tokenExpireTime',
            'userId',
            'username',
            'realName',
            'userType',
            'tenantId',
            'tenantName',
            'tenantCode'
        ];
        authKeys.forEach(key => localStorage.removeItem(key));
    }

    /**
     * 检查Token是否即将过期
     */
    isTokenExpiringSoon(minutesBefore = 5) {
        const expireTime = localStorage.getItem('tokenExpireTime');
        if (!expireTime) return false;
        return Date.now() > (parseInt(expireTime) - minutesBefore * 60 * 1000);
    }

    /**
     * 获取Authorization header值
     */
    getAuthHeader() {
        const token = localStorage.getItem('authToken');
        const tokenType = localStorage.getItem('tokenType') || 'Bearer';
        return token ? `${tokenType} ${token}` : null;
    }

    /**
     * 检查是否已登录
     */
    isAuthenticated() {
        const token = localStorage.getItem('authToken');
        return !!token && !this.isTokenExpired();
    }

    /**
     * 检查Token是否已过期
     */
    isTokenExpired() {
        const expireTime = localStorage.getItem('tokenExpireTime');
        if (!expireTime) return true;
        return Date.now() > parseInt(expireTime);
    }
}

// 全局导出（浏览器环境）
window.AuthService = new AuthService();
