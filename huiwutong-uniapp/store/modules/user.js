import { defineStore } from 'pinia'
import { setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import auth from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null,
    userType: '' // learner | staff
  }),

  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,

    // 获取用户名
    username: (state) => state.userInfo?.username || '',

    // 获取真实姓名
    realName: (state) => state.userInfo?.realName || '',

    // 获取用户角色
    role: (state) => state.userInfo?.role || '',

    // 是否为学员
    isLearner: (state) => state.userType === 'learner',

    // 是否为工作人员
    isStaff: (state) => state.userType === 'staff'
  },

  actions: {
    /**
     * 设置Token
     */
    SET_TOKEN(token) {
      this.token = token
      setToken(token)
    },

    /**
     * 设置用户信息
     */
    SET_USER_INFO(userInfo) {
      this.userInfo = userInfo
      setUserInfo(userInfo)
    },

    /**
     * 设置用户类型
     */
    SET_USER_TYPE(userType) {
      this.userType = userType
      uni.setStorageSync('userType', userType)
    },

    /**
     * 登录
     */
    async login(loginData) {
      try {
        // 调用登录API
        const res = await auth.login({
          username: loginData.username,
          password: loginData.password,
          tenantCode: 'DEFAULT' // 默认租户代码
        })

        // 保存Token和用户信息
        this.SET_TOKEN(res.token)
        this.SET_USER_INFO(res.userInfo)
        this.SET_USER_TYPE(loginData.userType)

        // 保存用户ID和租户ID
        if (res.userInfo) {
          uni.setStorageSync('userId', res.userInfo.id)
          if (res.userInfo.tenantId) {
            uni.setStorageSync('tenantId', res.userInfo.tenantId)
          }
        }

        return res
      } catch (error) {
        console.error('Login error:', error)
        throw error
      }
    },

    /**
     * 登出
     */
    async logout() {
      try {
        // 调用登出API
        await auth.logout()

        // 清除本地状态
        this.token = ''
        this.userInfo = null
        this.userType = ''

        removeToken()
        removeUserInfo()
        uni.removeStorageSync('userType')
        uni.removeStorageSync('userId')
        uni.removeStorageSync('tenantId')

        // 跳转到登录页
        uni.reLaunch({
          url: '/pages/index/login'
        })
      } catch (error) {
        console.error('Logout error:', error)
        throw error
      }
    },

    /**
     * 获取用户信息
     */
    async getUserInfo() {
      try {
        // 调用获取用户信息API
        const res = await auth.getUserInfo()

        this.SET_USER_INFO(res)

        // 保存用户ID和租户ID
        if (res) {
          uni.setStorageSync('userId', res.id)
          if (res.tenantId) {
            uni.setStorageSync('tenantId', res.tenantId)
          }
        }

        return res
      } catch (error) {
        console.error('GetUserInfo error:', error)
        throw error
      }
    },

    /**
     * 初始化用户状态
     */
    initUserState() {
      const token = uni.getStorageSync('token') || ''
      const userInfo = getUserInfo()
      const userType = uni.getStorageSync('userType') || ''

      this.token = token
      this.userInfo = userInfo
      this.userType = userType
    }
  }
})
