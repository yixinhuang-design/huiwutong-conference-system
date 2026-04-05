import { defineStore } from 'pinia'
import { setToken, removeToken, getToken, getUserInfo, setUserInfo, removeUserInfo, setRefreshToken, removeRefreshToken } from '@/utils/auth'
import auth from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null,
    userType: '' // attendee | staff
  }),

  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,

    // 获取用户名
    username: (state) => state.userInfo?.username || '',

    // 获取真实姓名
    realName: (state) => state.userInfo?.realName || '',

    // 获取手机号
    phone: (state) => state.userInfo?.phone || '',

    // 角色列表（后端返回数组如 ["admin"], ["user"], ["attendee"]）
    roles: (state) => state.userInfo?.roles || [],

    // 用户类型
    userTypeStr: (state) => state.userInfo?.userType || '',

    // 是否为管理员
    isAdmin: (state) => (state.userInfo?.roles || []).includes('admin'),

    // 是否为参会人员
    isAttendee: (state) => (state.userInfo?.roles || []).includes('attendee'),

    // 是否为工作人员
    isStaff: (state) => state.userType === 'staff' || (state.userInfo?.roles || []).includes('user'),

    // 是否已设置密码
    hasPassword: (state) => !!state.userInfo?.hasPassword
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
     * 处理登录成功后的公共逻辑
     */
    _handleLoginSuccess(res) {
      // 后端返回字段名是 accessToken
      this.SET_TOKEN(res.accessToken)

      // 保存refreshToken
      if (res.refreshToken) {
        setRefreshToken(res.refreshToken)
      }

      this.SET_USER_INFO(res.userInfo)

      // 根据角色自动判断用户类型
      const roles = res.userInfo?.roles || []
      if (roles.includes('attendee')) {
        this.SET_USER_TYPE('attendee')
      } else if (roles.includes('admin') || roles.includes('user')) {
        this.SET_USER_TYPE('staff')
      }

      // 保存用户ID和租户ID
      if (res.userInfo) {
        uni.setStorageSync('userId', String(res.userInfo.id))
        if (res.userInfo.tenantId) {
          uni.setStorageSync('tenantId', String(res.userInfo.tenantId))
        }
      }
    },

    /**
     * 发送短信验证码
     */
    async sendSmsCode(phone) {
      try {
        const res = await auth.sendSmsCode({ phone })
        return res
      } catch (error) {
        console.error('Send SMS code error:', error)
        throw error
      }
    },

    /**
     * 短信验证码登录
     */
    async smsLogin(loginData) {
      try {
        const res = await auth.smsLogin({
          phone: loginData.phone,
          smsCode: loginData.smsCode,
          tenantCode: loginData.tenantCode || 'DEFAULT'
        })

        this._handleLoginSuccess(res)
        return res
      } catch (error) {
        console.error('SMS Login error:', error)
        throw error
      }
    },

    /**
     * 密码登录
     */
    async login(loginData) {
      try {
        const res = await auth.login({
          username: loginData.username,
          password: loginData.password,
          tenantCode: loginData.tenantCode || 'DEFAULT'
        })

        this._handleLoginSuccess(res)
        return res
      } catch (error) {
        console.error('Login error:', error)
        throw error
      }
    },

    /**
     * 设置/修改密码
     */
    async setPassword(data) {
      try {
        const res = await auth.setPassword(data)
        // 设置密码后更新本地用户信息
        if (this.userInfo) {
          this.userInfo.hasPassword = true
          setUserInfo(this.userInfo)
        }
        return res
      } catch (error) {
        console.error('Set password error:', error)
        throw error
      }
    },

    /**
     * 登出
     */
    async logout() {
      try {
        await auth.logout()
      } catch (e) {
        console.warn('Logout API call failed:', e)
      }

      // 无论API调用成功与否，都清除本地状态
      this.token = ''
      this.userInfo = null
      this.userType = ''

      removeToken()
      removeRefreshToken()
      removeUserInfo()
      uni.removeStorageSync('userType')
      uni.removeStorageSync('userId')
      uni.removeStorageSync('tenantId')

      uni.reLaunch({
        url: '/pages/index/login'
      })
    },

    /**
     * 获取用户信息
     */
    async getUserInfo() {
      try {
        const res = await auth.getUserInfo()
        this.SET_USER_INFO(res)

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
