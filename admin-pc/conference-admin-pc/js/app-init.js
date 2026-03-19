/**
 * 应用初始化脚本 - 检查认证状态
 * 在Vue应用加载前执行
 */

(function() {
    // 等待Auth Service加载 - 带有重试机制
    let retries = 0;
    const maxRetries = 50; // 最多等待5秒（50 * 100ms）

    function waitForAuthService() {
        if (typeof window.AuthService !== 'undefined' && typeof window.AuthService.isTokenExpired === 'function') {
            // AuthService已成功加载，执行检查
            checkAuthentication();
            setupTokenRefreshInterval();
        } else if (retries < maxRetries) {
            retries++;
            setTimeout(waitForAuthService, 100);
        } else {
            console.error('Auth Service加载超时');
        }
    }

    // 检查认证状态
    function checkAuthentication() {
        const token = localStorage.getItem('authToken');
        const currentPath = window.location.pathname;
        
        // 登录页不需要检查认证
        if (currentPath.includes('login.html')) {
            return;
        }

        // 如果没有token，重定向到登录页
        if (!token) {
            console.warn('未检测到有效的认证Token，正在重定向到登录页...');
            window.location.href = 'pages/login.html';
            return;
        }

        // 检查token是否过期
        if (window.AuthService.isTokenExpired()) {
            console.warn('认证Token已过期，正在重定向到登录页...');
            localStorage.removeItem('authToken');
            window.location.href = 'pages/login.html';
            return;
        }

        // 检查token是否即将过期（提前5分钟）
        if (window.AuthService.isTokenExpiringSoon(5)) {
            console.log('认证Token即将过期，准备刷新...');
            refreshTokenSilently();
        }

        console.log('认证检查完成，用户已登录');
    }

    // 自动刷新Token
    async function refreshTokenSilently() {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            if (!refreshToken) {
                throw new Error('无刷新令牌');
            }

            const response = await window.AuthService.refreshToken(refreshToken);
            
            if (response.code === 200 && response.data) {
                window.AuthService.saveAuth(response.data);
                console.log('Token已自动刷新');
            } else {
                throw new Error(response.message || 'Token刷新失败');
            }
        } catch (error) {
            console.warn('Token自动刷新失败:', error.message);
            // Token刷新失败，需要重新登录
            localStorage.removeItem('authToken');
            window.location.href = 'pages/login.html';
        }
    }

    // 设置定期刷新Token
    function setupTokenRefreshInterval() {
        // 每30分钟检查一次Token状态
        setInterval(() => {
            if (window.AuthService.isTokenExpiringSoon(5) && !window.location.pathname.includes('login.html')) {
                refreshTokenSilently();
            }
        }, 30 * 60 * 1000);
    }

    // 页面卸载时清理
    window.addEventListener('beforeunload', function() {
        // 保存最后的状态
        localStorage.setItem('lastVisitTime', new Date().toISOString());
    });

    // 页面加载时执行检查
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', waitForAuthService);
    } else {
        waitForAuthService();
    }

    // 暴露全局函数供其他脚本使用
    window.AppAuth = {
        checkAuthentication: checkAuthentication,
        refreshToken: refreshTokenSilently,
        logout: function() {
            window.AuthService.logout();
        },
        getCurrentUser: function() {
            return {
                userId: localStorage.getItem('userId'),
                username: localStorage.getItem('username'),
                realName: localStorage.getItem('realName'),
                userType: localStorage.getItem('userType'),
                tenantId: localStorage.getItem('tenantId'),
                tenantName: localStorage.getItem('tenantName')
            };
        },
        isAuthenticated: function() {
            return window.AuthService.isAuthenticated();
        }
    };

    console.log('应用初始化脚本已加载');
})();
