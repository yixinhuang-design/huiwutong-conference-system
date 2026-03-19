import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    // 系统信息
    systemInfo: {},
    // 网络状态
    networkType: 'unknown',
    // 当前页面路径
    currentPage: '',
    // 加载状态
    loading: false
  }),

  getters: {
    // 是否在线
    isOnline: (state) => state.networkType !== 'none'
  },

  actions: {
    /**
     * 设置系统信息
     */
    setSystemInfo(info) {
      this.systemInfo = info
    },

    /**
     * 设置网络类型
     */
    setNetworkType(type) {
      this.networkType = type
    },

    /**
     * 设置当前页面
     */
    setCurrentPage(page) {
      this.currentPage = page
    },

    /**
     * 设置加载状态
     */
    setLoading(loading) {
      this.loading = loading
    },

    /**
     * 获取系统信息
     */
    async getSystemInfo() {
      try {
        const res = await uni.getSystemInfo()
        this.setSystemInfo(res)
        return res
      } catch (error) {
        console.error('GetSystemInfo error:', error)
        return null
      }
    },

    /**
     * 获取网络类型
     */
    async getNetworkType() {
      try {
        const res = await uni.getNetworkType()
        this.setNetworkType(res.networkType)
        return res.networkType
      } catch (error) {
        console.error('GetNetworkType error:', error)
        return 'unknown'
      }
    }
  }
})
