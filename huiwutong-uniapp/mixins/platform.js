/**
 * 平台适配 Mixin
 * 提供平台相关的响应式数据和计算属性
 */

import { getPlatformInfo, getSystemInfo, getStatusBarHeight, getSafeArea, isNotchScreen } from '@/utils/platform'

export default {
  data() {
    return {
      // 平台信息
      platformInfo: getPlatformInfo(),

      // 系统信息
      systemInfo: {},

      // 状态栏高度
      statusBarHeight: 0,

      // 安全区域
      safeArea: {},

      // 是否为刘海屏
      isNotch: false,

      // 屏幕尺寸
      screenWidth: 375,
      screenHeight: 812,

      // 设备像素比
      pixelRatio: 2
    }
  },

  computed: {
    // 当前平台
    platform() {
      return this.platformInfo.platform
    },

    // 是否为H5
    isH5() {
      return this.platformInfo.isH5
    },

    // 是否为小程序
    isMP() {
      return this.platformInfo.isMP
    },

    // 是否为App
    isApp() {
      return this.platformInfo.isApp
    },

    // 是否为iOS
    isIOS() {
      return this.platformInfo.isIOS || false
    },

    // 是否为Android
    isAndroid() {
      return this.platformInfo.isAndroid || false
    },

    // 是否为微信小程序
    isMPWeixin() {
      // #ifdef MP-WEIXIN
      return true
      // #endif
      // #ifndef MP-WEIXIN
      return false
      // #endif
    },

    // 是否需要安全区域适配
    needSafeArea() {
      // iOS App需要安全区域
      return this.isApp && this.isIOS
    },

    // 顶部安全区域高度
    safeAreaTop() {
      if (this.needSafeArea && this.safeArea.top) {
        return this.safeArea.top
      }
      return 0
    },

    // 底部安全区域高度
    safeAreaBottom() {
      if (this.needSafeArea && this.safeArea.bottom) {
        return this.screenHeight - this.safeArea.bottom
      }
      return 0
    },

    // 自定义导航栏高度
    customNavHeight() {
      // 状态栏高度 + 导航栏高度
      return this.statusBarHeight + 44
    },

    // 是否为小屏幕设备
    isSmallScreen() {
      return this.screenWidth < 375
    },

    // 是否为大屏幕设备
    isLargeScreen() {
      return this.screenWidth >= 768
    }
  },

  async onLoad() {
    await this.initPlatformInfo()
  },

  methods: {
    /**
     * 初始化平台信息
     */
    async initPlatformInfo() {
      try {
        // 获取系统信息
        const systemInfo = await getSystemInfo()
        this.systemInfo = systemInfo
        this.screenWidth = systemInfo.screenWidth || 375
        this.screenHeight = systemInfo.screenHeight || 812
        this.pixelRatio = systemInfo.pixelRatio || 2

        // 获取状态栏高度
        const statusBarHeight = await getStatusBarHeight()
        this.statusBarHeight = statusBarHeight

        // 获取安全区域
        const safeArea = await getSafeArea()
        this.safeArea = safeArea

        // 判断是否为刘海屏
        const isNotch = await isNotchScreen()
        this.isNotch = isNotch
      } catch (error) {
        console.error('Init platform info error:', error)
      }
    },

    /**
     * 平台特定的样式类名
     */
    getPlatformClass() {
      return {
        'platform-h5': this.isH5,
        'platform-mp': this.isMP,
        'platform-app': this.isApp,
        'platform-ios': this.isIOS,
        'platform-android': this.isAndroid,
        'platform-mp-weixin': this.isMPWeixin,
        'has-notch': this.isNotch,
        'need-safe-area': this.needSafeArea,
        'small-screen': this.isSmallScreen,
        'large-screen': this.isLargeScreen
      }
    }
  }
}
