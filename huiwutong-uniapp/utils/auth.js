/**
 * 认证工具函数
 */

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_INFO_KEY = 'userInfo'
const USER_TYPE_KEY = 'userType'
const TENANT_ID_KEY = 'tenantId'
const USER_ID_KEY = 'userId'

/**
 * 获取Token
 */
export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

/**
 * 设置Token
 */
export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token)
}

/**
 * 移除Token
 */
export function removeToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  const userInfo = uni.getStorageSync(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 */
export function setUserInfo(userInfo) {
  uni.setStorageSync(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  uni.removeStorageSync(USER_INFO_KEY)
}

/**
 * 获取用户类型
 */
export function getUserType() {
  return uni.getStorageSync(USER_TYPE_KEY) || ''
}

/**
 * 设置用户类型
 */
export function setUserType(userType) {
  uni.setStorageSync(USER_TYPE_KEY, userType)
}

/**
 * 移除用户类型
 */
export function removeUserType() {
  uni.removeStorageSync(USER_TYPE_KEY)
}

/**
 * 检查是否已登录
 */
export function isLoggedIn() {
  return !!getToken()
}

/**
 * 获取刷新Token
 */
export function getRefreshToken() {
  return uni.getStorageSync(REFRESH_TOKEN_KEY) || ''
}

/**
 * 设置刷新Token
 */
export function setRefreshToken(refreshToken) {
  uni.setStorageSync(REFRESH_TOKEN_KEY, refreshToken)
}

/**
 * 移除刷新Token
 */
export function removeRefreshToken() {
  uni.removeStorageSync(REFRESH_TOKEN_KEY)
}

/**
 * 获取租户ID
 */
export function getTenantId() {
  return uni.getStorageSync(TENANT_ID_KEY) || ''
}

/**
 * 设置租户ID
 */
export function setTenantId(tenantId) {
  uni.setStorageSync(TENANT_ID_KEY, tenantId)
}

/**
 * 移除租户ID
 */
export function removeTenantId() {
  uni.removeStorageSync(TENANT_ID_KEY)
}

/**
 * 获取用户ID
 */
export function getUserId() {
  return uni.getStorageSync(USER_ID_KEY) || ''
}

/**
 * 设置用户ID
 */
export function setUserId(userId) {
  uni.setStorageSync(USER_ID_KEY, userId)
}

/**
 * 移除用户ID
 */
export function removeUserId() {
  uni.removeStorageSync(USER_ID_KEY)
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  removeToken()
  removeRefreshToken()
  removeUserInfo()
  removeUserType()
  removeTenantId()
  removeUserId()
}
