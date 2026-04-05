/**
 * 认证相关API
 * 对接认证服务 (端口8081)
 */

import { post, get } from './request'

export default {
  /**
   * 用户登录（密码方式）
   * POST /auth/login
   */
  login(data) {
    return post('/auth/login', data)
  },

  /**
   * 发送短信验证码
   * POST /auth/sms/send
   */
  sendSmsCode(data) {
    return post('/auth/sms/send', data)
  },

  /**
   * 短信验证码登录
   * POST /auth/sms/login
   */
  smsLogin(data) {
    return post('/auth/sms/login', data)
  },

  /**
   * 设置/修改密码（需登录状态）
   * POST /auth/password/set
   */
  setPassword(data) {
    return post('/auth/password/set', data)
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
