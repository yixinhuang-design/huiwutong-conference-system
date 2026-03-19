// common/api/auth.js - 认证API服务

import { request, post } from '../request-enhanced'
import { API_BASE_URLS, API_ENDPOINTS } from './constants'

const BASE_URL = API_BASE_URLS.auth

export const authAPI = {
  /**
   * 用户登录
   * @param {Object} credentials - 登录凭证 {username, password}
   * @returns {Promise<Object>} 登录响应 {token, refreshToken, user}
   */
  login(credentials) {
    return post(`${BASE_URL}${API_ENDPOINTS.AUTH_LOGIN}`, credentials, {
      showLoading: true,
      service: 'auth',
    })
  },

  /**
   * 用户注册
   * @param {Object} data - 注册数据 {username, phone, password}
   * @returns {Promise<Object>} 注册响应 {user, token}
   */
  register(data) {
    return post(`${BASE_URL}${API_ENDPOINTS.AUTH_REGISTER}`, data, {
      showLoading: true,
      showSuccess: true,
      service: 'auth',
    })
  },

  /**
   * 验证码登录
   * @param {Object} data - 登录数据 {phone, code}
   * @returns {Promise<Object>} 登录响应
   */
  loginByCode(data) {
    return post(`${BASE_URL}${API_ENDPOINTS.AUTH_LOGIN_BY_CODE}`, data, {
      showLoading: true,
      service: 'auth',
    })
  },

  /**
   * 刷新Token
   * @param {string} refreshToken - 刷新token
   * @returns {Promise<Object>} {token, refreshToken}
   */
  refreshToken(refreshToken) {
    return post(
      `${BASE_URL}${API_ENDPOINTS.AUTH_REFRESH}`,
      { refreshToken },
      {
        service: 'auth',
        showLoading: false,
      }
    )
  },

  /**
   * 登出
   * @returns {Promise<void>}
   */
  logout() {
    return post(
      `${BASE_URL}${API_ENDPOINTS.AUTH_LOGOUT}`,
      {},
      {
        service: 'auth',
        showLoading: false,
      }
    )
  },

  /**
   * 验证Token有效性
   * @param {string} token - JWT token
   * @returns {Promise<Object>} {valid, user}
   */
  verifyToken(token) {
    return request(`${BASE_URL}${API_ENDPOINTS.AUTH_VERIFY_TOKEN}`, {
      method: 'POST',
      data: { token },
      service: 'auth',
      showLoading: false,
    })
  },

  /**
   * 获取用户信息
   * @returns {Promise<Object>} 用户信息
   */
  getUserInfo() {
    return request(`${BASE_URL}/api/auth/user-info`, {
      method: 'GET',
      service: 'auth',
    })
  },

  /**
   * 更新用户信息
   * @param {Object} data - 用户数据
   * @returns {Promise<Object>} 更新后的用户信息
   */
  updateUserInfo(data) {
    return request(`${BASE_URL}/api/auth/user-info`, {
      method: 'PUT',
      data,
      service: 'auth',
      showSuccess: true,
    })
  },

  /**
   * 修改密码
   * @param {Object} data - {oldPassword, newPassword}
   * @returns {Promise<void>}
   */
  changePassword(data) {
    return post(`${BASE_URL}/api/auth/change-password`, data, {
      service: 'auth',
      showSuccess: true,
    })
  },

  /**
   * 请求重置密码
   * @param {string} email - 邮箱
   * @returns {Promise<void>}
   */
  requestPasswordReset(email) {
    return post(`${BASE_URL}/api/auth/request-reset-password`, { email }, {
      service: 'auth',
      showSuccess: true,
    })
  },

  /**
   * 重置密码
   * @param {Object} data - {token, newPassword}
   * @returns {Promise<void>}
   */
  resetPassword(data) {
    return post(`${BASE_URL}/api/auth/reset-password`, data, {
      service: 'auth',
      showSuccess: true,
    })
  },

  /**
   * 发送验证码
   * @param {string} phone - 手机号
   * @returns {Promise<void>}
   */
  sendVerificationCode(phone) {
    return post(
      `${BASE_URL}/api/auth/send-verification-code`,
      { phone },
      {
        service: 'auth',
        showSuccess: true,
      }
    )
  },

  /**
   * 验证验证码
   * @param {Object} data - {phone, code}
   * @returns {Promise<{valid: boolean}>}
   */
  verifyCode(data) {
    return post(`${BASE_URL}/api/auth/verify-code`, data, {
      service: 'auth',
      showLoading: false,
    })
  },

  /**
   * 获取用户权限列表
   * @returns {Promise<Array>} 权限列表
   */
  getPermissions() {
    return request(`${BASE_URL}/api/auth/permissions`, {
      method: 'GET',
      service: 'auth',
      showLoading: false,
    })
  },

  /**
   * 获取用户角色
   * @returns {Promise<Array>} 角色列表
   */
  getRoles() {
    return request(`${BASE_URL}/api/auth/roles`, {
      method: 'GET',
      service: 'auth',
      showLoading: false,
    })
  },
}

export default authAPI
