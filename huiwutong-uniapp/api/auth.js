/**
 * 认证相关API
 * 对接认证服务 (端口8081)
 */

import { post, get } from './request'

export default {
  /**
   * 用户登录
   * POST /auth/login
   */
  login(data) {
    return post('/auth/login', data)
  },

  /**
   * 刷新Token
   * POST /auth/refresh
   */
  refreshToken(refreshToken) {
    return post('/auth/refresh', { refreshToken })
  },

  /**
   * 获取当前用户信息
   * GET /auth/me
   */
  getUserInfo() {
    return get('/auth/me')
  },

  /**
   * 用户登出
   * POST /auth/logout
   */
  logout() {
    return post('/auth/logout')
  }
}
