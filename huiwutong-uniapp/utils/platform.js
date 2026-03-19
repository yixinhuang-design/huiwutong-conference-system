/**
 * 平台适配工具函数
 * 用于处理不同平台的差异
 */

/**
 * 获取当前平台信息
 */
export function getPlatformInfo() {
  // #ifdef H5
  return {
    platform: 'h5',
    isH5: true,
    isMP: false,
    isApp: false
  }
  // #endif

  // #ifdef MP-WEIXIN
  return {
    platform: 'mp-weixin',
    isH5: false,
    isMP: true,
    isApp: false
  }
  // #endif

  // #ifdef APP-PLUS
  return {
    platform: 'app-plus',
    isH5: false,
    isMP: false,
    isApp: true,
    isIOS: plus.os.name === 'iOS',
    isAndroid: plus.os.name === 'Android'
  }
  // #endif

  // 默认返回
  return {
    platform: 'unknown',
    isH5: false,
    isMP: false,
    isApp: false
  }
}

/**
 * 获取系统信息
 */
export function getSystemInfo() {
  return new Promise((resolve) => {
    uni.getSystemInfo({
      success: (res) => {
        resolve(res)
      },
      fail: () => {
        resolve({})
      }
    })
  })
}

/**
 * 获取状态栏高度
 */
export function getStatusBarHeight() {
  return new Promise((resolve) => {
    uni.getSystemInfo({
      success: (res) => {
        resolve(res.statusBarHeight || 0)
      },
      fail: () => {
        resolve(0)
      }
    })
  })
}

/**
 * 获取安全区域
 */
export function getSafeArea() {
  return new Promise((resolve) => {
    uni.getSystemInfo({
      success: (res) => {
        resolve(res.safeArea || {})
      },
      fail: () => {
        resolve({})
      }
    })
  })
}

/**
 * 判断是否为刘海屏
 */
export function isNotchScreen() {
  return new Promise((resolve) => {
    uni.getSystemInfo({
      success: (res) => {
        // #ifdef APP-PLUS
        const { model, system } = res
        // iPhone X系列
        const isIPhoneX = /iPhone X|iPhone 11|iPhone 12|iPhone 13|iPhone 14/i.test(model)
        // Android 刘海屏判断
        const isAndroidNotch = system === 'android' && res.statusBarHeight > 24
        resolve(isIPhoneX || isAndroidNotch)
        // #endif

        // #ifdef H5 || MP-WEIXIN
        resolve(false)
        // #endif
      },
      fail: () => {
        resolve(false)
      }
    })
  })
}

/**
 * 平台特定的导航方式
 * H5使用 router.push，其他平台使用 uni.navigateTo
 */
export function navigateTo(url) {
  // #ifdef H5
  // H5平台使用路由管理
  const pages = getCurrentPages()
  if (pages.length >= 10) {
    uni.redirectTo({ url })
  } else {
    uni.navigateTo({ url })
  }
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  uni.navigateTo({ url })
  // #endif
}

/**
 * 平台特定的返回方式
 */
export function navigateBack(delta = 1) {
  // #ifdef H5
  const pages = getCurrentPages()
  if (pages.length <= 1) {
    history.back()
  } else {
    uni.navigateBack({ delta })
  }
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  uni.navigateBack({ delta })
  // #endif
}

/**
 * 复制到剪贴板
 */
export function setClipboard(data) {
  return new Promise((resolve, reject) => {
    // #ifdef H5
    if (navigator.clipboard) {
      navigator.clipboard.writeText(data).then(resolve).catch(reject)
    } else {
      // 兼容性处理
      const textarea = document.createElement('textarea')
      textarea.value = data
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      resolve()
    }
    // #endif

    // #ifdef MP-WEIXIN || APP-PLUS
    uni.setClipboardData({
      data,
      success: resolve,
      fail: reject
    })
    // #endif
  })
}

/**
 * 打开外部链接
 */
export function openExternalLink(url) {
  // #ifdef H5
  window.open(url, '_blank')
  // #endif

  // #ifdef MP-WEIXIN
  // 小程序需要使用 web-view 或复制链接
  uni.setClipboardData({
    data: url,
    success: () => {
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    }
  })
  // #endif

  // #ifdef APP-PLUS
  plus.runtime.openURL(url)
  // #endif
}

/**
 * 拨打电话
 */
export function makePhoneCall(phoneNumber) {
  // #ifdef H5
  window.location.href = `tel:${phoneNumber}`
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  uni.makePhoneCall({
    phoneNumber
  })
  // #endif
}

/**
 * 选择图片
 */
export function chooseImage(options = {}) {
  const defaultOptions = {
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera']
  }

  // #ifdef H5
  // H5可能有特殊的文件上传处理
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      ...defaultOptions,
      ...options,
      success: resolve,
      fail: reject
    })
  })
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      ...defaultOptions,
      ...options,
      success: resolve,
      fail: reject
    })
  })
  // #endif
}

/**
 * 扫码功能
 */
export function scanCode() {
  // #ifdef H5
  return new Promise((resolve, reject) => {
    uni.showToast({
      title: 'H5暂不支持扫码',
      icon: 'none'
    })
    reject(new Error('H5 not support scan'))
  })
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  return new Promise((resolve, reject) => {
    uni.scanCode({
      success: (res) => {
        resolve(res.result)
      },
      fail: reject
    })
  })
  // #endif
}

/**
 * 获取位置信息
 */
export function getLocation() {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: 'gcj02',
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 打开地图查看位置
 */
export function openLocation(latitude, longitude, name = '', address = '') {
  // #ifdef H5
  // H5使用第三方地图
  openExternalLink(`https://apis.map.qq.com/uri/v1/marker?marker=coord:${latitude},${longitude};title:${name};addr:${address}`)
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  uni.openLocation({
    latitude,
    longitude,
    name,
    address
  })
  // #endif
}

/**
 * 支付功能
 */
export function requestPayment(orderInfo) {
  // #ifdef H5
  return new Promise((resolve, reject) => {
    uni.showToast({
      title: '请在App中完成支付',
      icon: 'none'
    })
    reject(new Error('H5 payment not supported'))
  })
  // #endif

  // #ifdef MP-WEIXIN
  return new Promise((resolve, reject) => {
    uni.requestPayment({
      provider: 'wxpay',
      ...orderInfo,
      success: resolve,
      fail: reject
    })
  })
  // #endif

  // #ifdef APP-PLUS
  return new Promise((resolve, reject) => {
    uni.requestPayment({
      provider: orderInfo.provider || 'wxpay',
      orderInfo: orderInfo.orderInfo,
      success: resolve,
      fail: reject
    })
  })
  // #endif
}

/**
 * 分享功能
 */
export function share(options) {
  const { title, path, imageUrl } = options

  // #ifdef H5
  // H5需要自定义分享
  return false
  // #endif

  // #ifdef MP-WEIXIN
  // 小程序分享需要用户点击触发
  return {
    title,
    path,
    imageUrl
  }
  // #endif

  // #ifdef APP-PLUS
  // App使用原生分享
  uni.share({
    provider: 'weixin',
    type: 0,
    title,
    href: path,
    imageUrl,
    success: () => {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    }
  })
  return true
  // #endif
}

/**
 * 保存图片到相册
 */
export function saveImageToPhotos(filePath) {
  // #ifdef H5
  return new Promise((resolve, reject) => {
    uni.showToast({
      title: 'H5暂不支持保存图片',
      icon: 'none'
    })
    reject(new Error('H5 not support save image'))
  })
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  return new Promise((resolve, reject) => {
    uni.saveImageToPhotosAlbum({
      filePath,
      success: () => {
        uni.showToast({
          title: '已保存到相册',
          icon: 'success'
        })
        resolve()
      },
      fail: reject
    })
  })
  // #endif
}

/**
 * 获取网络状态
 */
export function getNetworkType() {
  return new Promise((resolve) => {
    uni.getNetworkType({
      success: (res) => {
        resolve(res.networkType)
      },
      fail: () => {
        resolve('unknown')
      }
    })
  })
}

/**
 * 震动反馈
 */
export function vibrate(short = false) {
  // #ifdef H5
  // H5不支持震动
  return
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  if (short) {
    uni.vibrateShort()
  } else {
    uni.vibrateLong()
  }
  // #endif
}

/**
 * 设置导航栏标题
 */
export function setNavigationBarTitle(title) {
  uni.setNavigationBarTitle({
    title
  })
}

/**
 * 设置导航栏颜色
 */
export function setNavigationBarColor(frontColor, backgroundColor) {
  uni.setNavigationBarColor({
    frontColor,
    backgroundColor,
    animation: {
      duration: 200,
      timingFunc: 'easeIn'
    }
  })
}

/**
 * 显示加载提示
 */
export function showLoading(title = '加载中...', mask = true) {
  uni.showLoading({
    title,
    mask
  })
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
  uni.hideLoading()
}

/**
 * 页面下拉刷新
 */
export function startPullDownRefresh() {
  uni.startPullDownRefresh()
}

/**
 * 停止下拉刷新
 */
export function stopPullDownRefresh() {
  uni.stopPullDownRefresh()
}

export default {
  getPlatformInfo,
  getSystemInfo,
  getStatusBarHeight,
  getSafeArea,
  isNotchScreen,
  navigateTo,
  navigateBack,
  setClipboard,
  openExternalLink,
  makePhoneCall,
  chooseImage,
  scanCode,
  getLocation,
  openLocation,
  requestPayment,
  share,
  saveImageToPhotos,
  getNetworkType,
  vibrate,
  setNavigationBarTitle,
  setNavigationBarColor,
  showLoading,
  hideLoading,
  startPullDownRefresh,
  stopPullDownRefresh
}
