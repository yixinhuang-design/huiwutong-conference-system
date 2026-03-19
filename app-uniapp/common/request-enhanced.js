// common/request.js - 增强的HTTP请求模块

import {
  API_BASE_URLS,
  HTTP_STATUS_CODES,
  REQUEST_TIMEOUT,
  RETRY_CONFIG,
} from './api/constants'
import {
  handleApiError,
  isRetryableError,
  isNetworkError,
  showErrorToast,
  showSuccessToast,
} from './api/error-handler'
import { useAuthStore } from '../stores/modules/auth'

// 请求队列管理（用于处理token刷新时的请求阻塞）
let requestQueue = []
let isRefreshing = false

/**
 * 处理请求队列中的所有请求
 * @param {string} token - 新token
 */
function processQueue(token) {
  requestQueue.forEach((cb) => cb(token))
  requestQueue = []
  isRefreshing = false
}

/**
 * 创建请求配置
 * @param {Object} options - 请求选项
 * @returns {Object} 完整的请求配置
 */
function createRequestConfig(options) {
  const {
    url,
    method = 'GET',
    data = null,
    header = {},
    timeout = REQUEST_TIMEOUT,
    showLoading = true,
    showSuccess = false,
    service = 'auth',
  } = options

  const authStore = useAuthStore()
  const baseUrl = API_BASE_URLS[service] || API_BASE_URLS.auth

  const config = {
    url: `${baseUrl}${url}`,
    method,
    data: method !== 'GET' ? data : null,
    params: method === 'GET' ? data : null,
    timeout,
    header: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...header,
    },
    showLoading,
    showSuccess,
  }

  // 添加认证token
  if (authStore.token) {
    config.header.Authorization = `Bearer ${authStore.token}`
  }

  // 添加租户ID（多租户场景）
  if (authStore.tenantId) {
    config.header['X-Tenant-Id'] = authStore.tenantId
  }

  return config
}

/**
 * 执行单次请求
 * @param {Object} config - 请求配置
 * @param {number} retryCount - 重试次数
 * @returns {Promise}
 */
function executeRequest(config, retryCount = 0) {
  return new Promise((resolve, reject) => {
    // 显示加载
    if (config.showLoading) {
      uni.showLoading({
        title: '加载中...',
        mask: true,
      })
    }

    uni.request({
      url: config.url,
      method: config.method,
      data: config.data,
      timeout: config.timeout,
      header: config.header,
      success(response) {
        // 隐藏加载
        if (config.showLoading) {
          uni.hideLoading()
        }

        // 检查HTTP状态码
        if (
          response.statusCode < HTTP_STATUS_CODES.OK ||
          response.statusCode >= 400
        ) {
          const error = new Error(`HTTP ${response.statusCode}`)
          error.response = {
            status: response.statusCode,
            data: response.data,
          }

          // 处理特定状态码
          if (response.statusCode === HTTP_STATUS_CODES.UNAUTHORIZED) {
            handleTokenExpired(config, retryCount)
              .then(() => executeRequest(config, retryCount + 1))
              .then(resolve)
              .catch(reject)
          } else if (
            isRetryableError(response.statusCode, retryCount)
          ) {
            // 重试请求
            setTimeout(
              () => {
                executeRequest(config, retryCount + 1)
                  .then(resolve)
                  .catch(reject)
              },
              RETRY_CONFIG.retryDelay * Math.pow(2, retryCount)
            )
          } else {
            handleApiError(error, { showToast: true, throwError: false })
            reject(error)
          }
          return
        }

        // 检查响应体状态码
        const responseData = response.data
        if (
          responseData.code !== 200 &&
          responseData.code !== 0 &&
          responseData.code !== 'success'
        ) {
          const error = new Error(
            responseData.message || '请求失败'
          )
          error.response = {
            status: response.statusCode,
            data: responseData,
          }

          handleApiError(error, { showToast: true, throwError: false })
          reject(error)
          return
        }

        // 请求成功
        const result = responseData.data || responseData.result || null

        if (config.showSuccess) {
          showSuccessToast(responseData.message || '操作成功')
        }

        resolve(result)
      },

      fail(error) {
        // 隐藏加载
        if (config.showLoading) {
          uni.hideLoading()
        }

        // 判断是否为网络错误
        if (isNetworkError(error)) {
          if (isRetryableError(0, retryCount)) {
            // 网络错误，重试
            setTimeout(
              () => {
                executeRequest(config, retryCount + 1)
                  .then(resolve)
                  .catch(reject)
              },
              RETRY_CONFIG.retryDelay * Math.pow(2, retryCount)
            )
          } else {
            showErrorToast('网络连接失败，请检查网络设置')
            reject(error)
          }
        } else {
          showErrorToast(error.message || '请求失败')
          reject(error)
        }
      },
    })
  })
}

/**
 * 处理Token过期
 * @param {Object} config - 原请求配置
 * @param {number} retryCount - 重试次数
 * @returns {Promise}
 */
async function handleTokenExpired(config, retryCount) {
  const authStore = useAuthStore()

  // 如果已经在刷新token，则等待
  if (isRefreshing) {
    return new Promise((resolve) => {
      requestQueue.push((newToken) => {
        config.header.Authorization = `Bearer ${newToken}`
        resolve()
      })
    })
  }

  isRefreshing = true

  try {
    // 调用刷新token的API
    const newToken = await refreshToken()
    authStore.setToken(newToken)
    config.header.Authorization = `Bearer ${newToken}`
    processQueue(newToken)
    return Promise.resolve()
  } catch (error) {
    // token刷新失败，登出用户
    authStore.logout()
    uni.reLaunch({
      url: '/pages/common/login/login',
    })
    return Promise.reject(error)
  }
}

/**
 * 刷新Token
 * @returns {Promise<string>} 新的token
 */
async function refreshToken() {
  const authStore = useAuthStore()

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URLS.auth}/api/auth/refresh`,
      method: 'POST',
      header: {
        Authorization: `Bearer ${authStore.refreshToken}`,
      },
      timeout: REQUEST_TIMEOUT,
      success(response) {
        if (response.statusCode === 200) {
          const data = response.data
          if (data.code === 200 || data.code === 0) {
            resolve(data.data?.token || data.data)
          } else {
            reject(new Error('Token刷新失败'))
          }
        } else {
          reject(new Error(`HTTP ${response.statusCode}`))
        }
      },
      fail(error) {
        reject(error)
      },
    })
  })
}

/**
 * 主请求函数
 * @param {string} url - 请求URL
 * @param {Object} options - 请求选项
 * @returns {Promise}
 */
export function request(url, options = {}) {
  const config = createRequestConfig({
    url,
    ...options,
  })

  return executeRequest(config)
}

/**
 * GET请求快捷方法
 */
export function get(url, data = null, options = {}) {
  return request(url, {
    method: 'GET',
    data,
    ...options,
  })
}

/**
 * POST请求快捷方法
 */
export function post(url, data = null, options = {}) {
  return request(url, {
    method: 'POST',
    data,
    ...options,
  })
}

/**
 * PUT请求快捷方法
 */
export function put(url, data = null, options = {}) {
  return request(url, {
    method: 'PUT',
    data,
    ...options,
  })
}

/**
 * DELETE请求快捷方法
 */
export function deleteRequest(url, data = null, options = {}) {
  return request(url, {
    method: 'DELETE',
    data,
    ...options,
  })
}

/**
 * PATCH请求快捷方法
 */
export function patch(url, data = null, options = {}) {
  return request(url, {
    method: 'PATCH',
    data,
    ...options,
  })
}

/**
 * 创建FormData请求（用于文件上传）
 */
export function requestFormData(url, formData, options = {}) {
  const authStore = useAuthStore()
  const baseUrl = API_BASE_URLS[options.service || 'auth']

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${baseUrl}${url}`,
      filePath: formData.filePath,
      name: formData.name || 'file',
      formData: formData.data || {},
      header: {
        Authorization: authStore.token
          ? `Bearer ${authStore.token}`
          : '',
        'X-Tenant-Id': authStore.tenantId,
      },
      success(response) {
        const data = JSON.parse(response.data)
        if (response.statusCode === 200 && data.code === 200) {
          resolve(data.data)
        } else {
          reject(new Error(data.message || '上传失败'))
        }
      },
      fail(error) {
        reject(error)
      },
    })
  })
}

export default request
