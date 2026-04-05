/**
 * API请求封装
 */

import { getToken, setToken, getRefreshToken, setRefreshToken, removeToken, removeRefreshToken } from '@/utils/auth'

// API基础地址 - 开发环境使用localhost，真机调试时请替换为电脑IP地址
const DEV_BASE_URL = 'http://localhost:8080/api'
const PROD_BASE_URL = 'https://api.huiwutong.com/api'
const BASE_URL = process.env.NODE_ENV === 'development' ? DEV_BASE_URL : PROD_BASE_URL

/**
 * 请求拦截器
 */
function requestInterceptor(options) {
  // 添加Token
  const token = getToken()
  if (token) {
    options.header = {
      ...options.header,
      'Authorization': `Bearer ${token}`
    }
  }

  // 添加租户ID
  const tenantId = uni.getStorageSync('tenantId')
  if (tenantId) {
    options.header = {
      ...options.header,
      'X-Tenant-Id': tenantId
    }
  }

  // 添加用户ID
  const userId = uni.getStorageSync('userId')
  if (userId) {
    options.header = {
      ...options.header,
      'X-User-Id': userId
    }
  }

  // 添加默认Content-Type
  if (!options.header || !options.header['Content-Type']) {
    options.header = {
      ...options.header,
      'Content-Type': 'application/json'
    }
  }

  return options
}

/**
 * 响应拦截器
 */
function responseInterceptor(response) {
  const { statusCode, data } = response

  // HTTP状态码检查
  if (statusCode !== 200) {
    handleError({
      code: statusCode,
      message: `HTTP Error: ${statusCode}`
    })
    return Promise.reject(data)
  }

  // 业务状态码检查
  if (data.code !== undefined && data.code !== 200) {
    handleError({
      code: data.code,
      message: data.message || '请求失败'
    })
    return Promise.reject(data)
  }

  return data.data || data
}

/**
 * Token刷新锁 - 防止并发刷新
 */
let isRefreshing = false
let refreshSubscribers = []

function onTokenRefreshed(success) {
  refreshSubscribers.forEach(cb => cb(success))
  refreshSubscribers = []
}

/**
 * 尝试刷新Token
 */
function tryRefreshToken() {
  if (isRefreshing) {
    return new Promise(resolve => {
      refreshSubscribers.push(resolve)
    })
  }

  isRefreshing = true

  return new Promise(resolve => {
    const refreshTokenValue = getRefreshToken()
    if (!refreshTokenValue) {
      isRefreshing = false
      onTokenRefreshed(false)
      resolve(false)
      return
    }

    uni.request({
      url: BASE_URL + '/auth/refresh',
      method: 'POST',
      data: { refreshToken: refreshTokenValue },
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        const ok = res.statusCode === 200 && res.data?.code === 200 && res.data?.data?.accessToken
        if (ok) {
          setToken(res.data.data.accessToken)
          if (res.data.data.refreshToken) {
            setRefreshToken(res.data.data.refreshToken)
          }
        }
        isRefreshing = false
        onTokenRefreshed(!!ok)
        resolve(!!ok)
      },
      fail: () => {
        isRefreshing = false
        onTokenRefreshed(false)
        resolve(false)
      }
    })
  })
}

/**
 * 跳转到登录页
 */
function redirectToLogin() {
  removeToken()
  removeRefreshToken()
  uni.showToast({
    title: '登录已过期，请重新登录',
    icon: 'none',
    duration: 2000
  })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/index/login' })
  }, 2000)
}

/**
 * 错误处理
 */
function handleError(error) {
  console.error('API Error:', error)

  // 401由request函数内部处理Token刷新
  if (error.code === 401) return

  const message = error.message || '请求失败，请稍后重试'
  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  })
}

/**
 * 封装请求方法
 */
function request(options) {
  // 保存原始options用于Token刷新后重试
  const originalOptions = { ...options }

  // 请求拦截
  options = requestInterceptor(options)

  // 支持自定义baseUrl（用于跨服务调用）
  const baseUrl = options.baseUrl || BASE_URL

  return new Promise((resolve, reject) => {
    uni.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: options.header || {},
      timeout: options.timeout || 30000,
      success: (response) => {
        // 处理401 - 尝试刷新Token后重试
        const is401 = response.statusCode === 401 ||
          (response.data && response.data.code === 401)

        if (is401 && !originalOptions._isRetry) {
          tryRefreshToken().then(refreshed => {
            if (refreshed) {
              originalOptions._isRetry = true
              request(originalOptions).then(resolve).catch(reject)
            } else {
              redirectToLogin()
              reject({ code: 401, message: '登录已过期，请重新登录' })
            }
          })
          return
        }

        try {
          const result = responseInterceptor(response)
          resolve(result)
        } catch (error) {
          reject(error)
        }
      },
      fail: (error) => {
        console.error('Request fail:', error)
        handleError({
          code: -1,
          message: '网络请求失败，请检查网络连接'
        })
        reject(error)
      }
    })
  })
}

/**
 * GET请求
 */
export function get(url, data = {}, options = {}) {
  return request({
    url,
    method: 'GET',
    data,
    ...options
  })
}

/**
 * POST请求
 */
export function post(url, data = {}, options = {}) {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT请求
 */
export function put(url, data = {}, options = {}) {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE请求
 */
export function del(url, data = {}, options = {}) {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}

/**
 * 文件上传
 */
export function upload(url, filePath, options = {}) {
  const token = getToken()

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + url,
      filePath,
      name: options.name || 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      formData: options.formData || {},
      success: (response) => {
        try {
          const data = JSON.parse(response.data)
          if (data.code === 200) {
            resolve(data.data)
          } else {
            handleError(data)
            reject(data)
          }
        } catch (error) {
          reject(error)
        }
      },
      fail: (error) => {
        handleError({
          code: -1,
          message: '上传失败'
        })
        reject(error)
      }
    })
  })
}

/**
 * 文件下载
 */
export function download(url, options = {}) {
  const token = getToken()

  return new Promise((resolve, reject) => {
    uni.downloadFile({
      url: BASE_URL + url,
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (response) => {
        if (response.statusCode === 200) {
          resolve(response.tempFilePath)
        } else {
          reject(response)
        }
      },
      fail: (error) => {
        reject(error)
      }
    })
  })
}

/**
 * 获取Blob数据（用于文件下载）
 */
export function getBlob(url, data = {}, options = {}) {
  const token = getToken()

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method: 'GET',
      data,
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      responseType: 'arraybuffer',
      success: (response) => {
        if (response.statusCode === 200) {
          resolve(response.data)
        } else {
          reject(response)
        }
      },
      fail: (error) => {
        reject(error)
      }
    })
  })
}

export default request
