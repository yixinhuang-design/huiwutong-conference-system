/**
 * 平台特定配置
 * 针对不同平台的优化配置
 */

/**
 * H5平台配置
 */
export const h5Config = {
  // API基础地址
  apiBaseUrl: process.env.NODE_ENV === 'development'
    ? 'http://localhost:8080/api'
    : 'https://api.huiwutong.com/api',

  // 上传文件地址
  uploadUrl: '',

  // 是否启用模拟数据
  enableMock: false,

  // 路由模式
  routerMode: 'hash',

  // 性能优化
  optimization: {
    // 启用代码分割
    codeSplitting: true,

    // 启用压缩
    compression: true,

    // 启用缓存
    caching: true
  },

  // 第三方SDK配置
  sdk: {
    // 地图SDK
    map: {
      type: 'qqmap',
      key: ''
    }
  }
}

/**
 * 微信小程序配置
 */
export const mpWeixinConfig = {
  // API基础地址
  apiBaseUrl: 'https://api.huiwutong.com/api/v1',

  // 上传文件地址
  uploadUrl: 'https://api.huiwutong.com/api/v1/upload',

  // 是否启用模拟数据
  enableMock: false,

  // 性能优化
  optimization: {
    // 按需加载
    onDemandLoading: true,

    // 分包预下载
    preloadRule: {
      pages: ['pages/common/home'],
      network: 'all'
    }
  },

  // 微信支付配置
  payment: {
    appId: ''
  },

  // 地图配置
  map: {
    type: 'qqmap',
    key: ''
  }
}

/**
 * App配置
 */
export const appConfig = {
  // API基础地址
  apiBaseUrl: 'https://api.huiwutong.com/api/v1',

  // 上传文件地址
  uploadUrl: 'https://api.huiwutong.com/api/v1/upload',

  // 是否启用模拟数据
  enableMock: false,

  // 性能优化
  optimization: {
    // 启用原生渲染
    nvue: false,

    // 启用V8引擎
    v8: true,

    // 启用硬件加速
    hardwareAccelerated: true
  },

  // 推送配置
  push: {
    // 使用UniPush
    provider: 'unipush',
    appKey: ''
  },

  // 支付配置
  payment: {
    weixin: {
      appId: ''
    },
    alipay: {
      appId: ''
    }
  },

  // 地图配置
  map: {
    type: 'amap',
    appKey: ''
  },

  // OAuth配置
  oauth: {
    weixin: {
      appId: '',
      appSecret: ''
    },
    apple: {
      enabled: true
    }
  }
}

/**
 * 获取当前平台配置
 */
export function getPlatformConfig() {
  // #ifdef H5
  return h5Config
  // #endif

  // #ifdef MP-WEIXIN
  return mpWeixinConfig
  // #endif

  // #ifdef APP-PLUS
  return appConfig
  // #endif

  // 默认返回H5配置
  return h5Config
}

/**
 * 根据环境获取API地址
 */
export function getApiBaseUrl() {
  const config = getPlatformConfig()
  return config.apiBaseUrl
}

/**
 * 获取上传地址
 */
export function getUploadUrl() {
  const config = getPlatformConfig()
  return config.uploadUrl || config.apiBaseUrl + '/upload'
}

export default {
  h5Config,
  mpWeixinConfig,
  appConfig,
  getPlatformConfig,
  getApiBaseUrl,
  getUploadUrl
}
