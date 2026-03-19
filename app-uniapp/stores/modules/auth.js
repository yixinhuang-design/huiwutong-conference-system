import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref(null)
  const token = ref('')
  const refreshToken = ref('')
  const tenantId = ref('')
  const permissions = ref([])
  const isAuthenticated = ref(false)
  const isLoading = ref(false)
  const lastLoginTime = ref(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value && isAuthenticated.value)
  const userFullName = computed(() => {
    if (!user.value) return ''
    return `${user.value.firstName || ''} ${user.value.lastName || ''}`.trim()
  })
  const userRole = computed(() => user.value?.role || 'user')
  const hasPermission = (permission) => permissions.value.includes(permission)

  // Actions
  const setUser = (userData) => {
    user.value = userData
    isAuthenticated.value = !!userData
  }

  const setToken = (newToken, newRefreshToken = null) => {
    token.value = newToken
    if (newRefreshToken) {
      refreshToken.value = newRefreshToken
    }
    // 保存到本地存储
    uni.setStorageSync('auth_token', newToken)
    if (newRefreshToken) {
      uni.setStorageSync('auth_refresh_token', newRefreshToken)
    }
  }

  const setTenantId = (id) => {
    tenantId.value = id
    uni.setStorageSync('tenant_id', id)
  }

  const setPermissions = (perms) => {
    permissions.value = perms || []
    uni.setStorageSync('user_permissions', JSON.stringify(perms))
  }

  const login = async (credentials) => {
    isLoading.value = true
    try {
      // API调用将在组件中进行，这里只是状态更新
      return {
        success: true,
        message: 'Login successful',
      }
    } catch (error) {
      isAuthenticated.value = false
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const logout = () => {
    user.value = null
    token.value = ''
    refreshToken.value = ''
    tenantId.value = ''
    permissions.value = []
    isAuthenticated.value = false
    lastLoginTime.value = null

    // 清除本地存储
    uni.removeStorageSync('auth_token')
    uni.removeStorageSync('auth_refresh_token')
    uni.removeStorageSync('tenant_id')
    uni.removeStorageSync('user_info')
    uni.removeStorageSync('user_permissions')
  }

  const restoreFromStorage = () => {
    const storedToken = uni.getStorageSync('auth_token')
    const storedUserInfo = uni.getStorageSync('user_info')
    const storedTenantId = uni.getStorageSync('tenant_id')
    const storedPermissions = uni.getStorageSync('user_permissions')

    if (storedToken) {
      token.value = storedToken
      isAuthenticated.value = true
    }

    if (storedUserInfo) {
      user.value = JSON.parse(storedUserInfo)
    }

    if (storedTenantId) {
      tenantId.value = storedTenantId
    }

    if (storedPermissions) {
      try {
        permissions.value = JSON.parse(storedPermissions)
      } catch {
        permissions.value = []
      }
    }
  }

  const refreshTokenIfNeeded = async () => {
    if (!refreshToken.value) {
      logout()
      return false
    }

    isLoading.value = true
    try {
      // 这里将调用刷新token的API
      // const newToken = await refreshTokenAPI(refreshToken.value)
      // setToken(newToken)
      return true
    } catch (error) {
      logout()
      return false
    } finally {
      isLoading.value = false
    }
  }

  const updateUserProfile = (updates) => {
    if (user.value) {
      user.value = {
        ...user.value,
        ...updates,
      }
      uni.setStorageSync('user_info', JSON.stringify(user.value))
    }
  }

  return {
    // State
    user,
    token,
    refreshToken,
    tenantId,
    permissions,
    isAuthenticated,
    isLoading,
    lastLoginTime,

    // Getters
    isLoggedIn,
    userFullName,
    userRole,
    hasPermission,

    // Actions
    setUser,
    setToken,
    setTenantId,
    setPermissions,
    login,
    logout,
    restoreFromStorage,
    refreshTokenIfNeeded,
    updateUserProfile,
  }
})
