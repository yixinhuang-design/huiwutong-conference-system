/**
 * 本地存储工具函数
 */

/**
 * 设置存储
 */
export function setStorage(key, value) {
  try {
    const data = typeof value === 'string' ? value : JSON.stringify(value)
    uni.setStorageSync(key, data)
    return true
  } catch (e) {
    console.error('setStorage error:', e)
    return false
  }
}

/**
 * 获取存储
 */
export function getStorage(key) {
  try {
    const data = uni.getStorageSync(key)
    if (!data) return null

    try {
      return JSON.parse(data)
    } catch (e) {
      return data
    }
  } catch (e) {
    console.error('getStorage error:', e)
    return null
  }
}

/**
 * 移除存储
 */
export function removeStorage(key) {
  try {
    uni.removeStorageSync(key)
    return true
  } catch (e) {
    console.error('removeStorage error:', e)
    return false
  }
}

/**
 * 清空所有存储
 */
export function clearStorage() {
  try {
    uni.clearStorageSync()
    return true
  } catch (e) {
    console.error('clearStorage error:', e)
    return false
  }
}

/**
 * 获取存储信息
 */
export function getStorageInfo() {
  try {
    return uni.getStorageInfoSync()
  } catch (e) {
    console.error('getStorageInfo error:', e)
    return null
  }
}
