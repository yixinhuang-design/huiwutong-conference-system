// common/api/error-handler.js - API错误处理

import { HTTP_STATUS_CODES, ERROR_CODES } from './constants'

const errorMessages = {
  // 认证错误
  AUTH_001: '用户名或密码错误',
  AUTH_002: 'Token已过期，请重新登录',
  AUTH_003: 'Token无效',
  AUTH_004: '未授权，请先登录',

  // 业务错误
  BIZ_001: '培训不存在',
  BIZ_002: '报名记录不存在',
  BIZ_003: '座位不可用',
  BIZ_004: '重复报名',

  // 系统错误
  SYS_001: '系统错误，请稍后重试',
  SYS_002: '服务暂时不可用',
  SYS_003: '数据库错误',

  // HTTP状态码错误
  400: '请求参数错误',
  401: '未授权，请重新登录',
  403: '无权限访问此资源',
  404: '请求的资源不存在',
  409: '请求冲突',
  500: '服务器错误',
  502: '网关错误',
  503: '服务暂时不可用',
  504: '网关超时',
}

/**
 * 处理API错误
 * @param {Error} error - 错误对象
 * @param {Object} options - 配置选项
 * @returns {Object} 处理后的错误对象
 */
export function handleApiError(error, options = {}) {
  const { showToast = true, throwError = true } = options

  const response = error.response
  const status = response?.status
  const data = response?.data || {}
  const code = data.code || `HTTP_${status}`

  // 构建错误对象
  const apiError = {
    code: code,
    status: status,
    message:
      errorMessages[code] ||
      errorMessages[status] ||
      data.message ||
      error.message ||
      '操作失败',
    originalError: error,
    timestamp: new Date().toISOString(),
  }

  // 记录错误到日志系统
  logError(apiError)

  // 显示用户提示
  if (showToast) {
    showErrorToast(apiError.message)
  }

  // 处理特殊错误码
  if (code === ERROR_CODES.TOKEN_EXPIRED || status === HTTP_STATUS_CODES.UNAUTHORIZED) {
    handleUnauthorized()
  }

  if (throwError) {
    return Promise.reject(apiError)
  }

  return apiError
}

/**
 * 显示错误提示
 * @param {string} message - 错误信息
 */
export function showErrorToast(message) {
  uni.showToast({
    title: message || '操作失败',
    icon: 'error',
    duration: 2000,
  })
}

/**
 * 显示成功提示
 * @param {string} message - 成功信息
 */
export function showSuccessToast(message = '操作成功') {
  uni.showToast({
    title: message,
    icon: 'success',
    duration: 2000,
  })
}

/**
 * 处理未授权错误（登出用户，跳转到登录）
 */
export function handleUnauthorized() {
  // 这里将由拦截器处理
  console.warn('User unauthorized - need to logout and redirect to login')
}

/**
 * 记录错误到日志系统
 * @param {Object} apiError - API错误对象
 */
export function logError(apiError) {
  const logEntry = {
    timestamp: apiError.timestamp,
    code: apiError.code,
    status: apiError.status,
    message: apiError.message,
    url: apiError.originalError?.config?.url,
    method: apiError.originalError?.config?.method,
  }

  // 在开发环境打印到控制台
  if (process.env.NODE_ENV === 'development') {
    console.error('[API Error]', logEntry)
  }

  // 可以将错误发送到服务器日志系统
  // sendErrorLog(logEntry)
}

/**
 * 判断错误是否可重试
 * @param {number} status - HTTP状态码
 * @param {number} retryCount - 已重试次数
 * @returns {boolean}
 */
export function isRetryableError(status, retryCount = 0) {
  const retryableStatuses = [408, 429, 500, 502, 503, 504]
  return retryableStatuses.includes(status) && retryCount < 3
}

/**
 * 获取错误消息
 * @param {string|number} code - 错误码或HTTP状态码
 * @returns {string}
 */
export function getErrorMessage(code) {
  return errorMessages[code] || '操作失败，请稍后重试'
}

/**
 * 是否为网络错误
 * @param {Error} error - 错误对象
 * @returns {boolean}
 */
export function isNetworkError(error) {
  return (
    !error.response ||
    error.code === 'ECONNABORTED' ||
    error.message === 'Network Error'
  )
}
