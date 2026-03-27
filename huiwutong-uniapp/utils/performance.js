/**
 * 性能优化工具函数
 * 包含图片懒加载、节流、防抖等性能优化相关功能
 */

/**
 * 图片懒加载指令
 * 使用 Intersection Observer API 实现
 */
export function LazyLoad(el, binding) {
  // #ifdef H5
  const io = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target
        const src = binding.value

        if (src) {
          img.src = src
          img.classList.add('loaded')
        }

        io.unobserve(img)
      }
    })
  }, {
    rootMargin: '50px'
  })

  io.observe(el)
  // #endif

  // #ifdef MP-WEIXIN || APP-PLUS
  // 小程序和App使用原生懒加载
  if (binding.value) {
    el.src = binding.value
  }
  // #endif
}

/**
 * 节流函数
 * 在指定时间间隔内只执行一次函数
 */
export function throttle(fn, delay = 300) {
  let lastTime = 0
  let timer = null

  return function(...args) {
    const now = Date.now()

    if (now - lastTime >= delay) {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }

      lastTime = now
      fn.apply(this, args)
    } else if (!timer) {
      timer = setTimeout(() => {
        lastTime = Date.now()
        timer = null
        fn.apply(this, args)
      }, delay - (now - lastTime))
    }
  }
}

/**
 * 防抖函数
 * 在函数停止调用指定时间后才执行
 */
export function debounce(fn, delay = 300) {
  let timer = null

  return function(...args) {
    if (timer) {
      clearTimeout(timer)
    }

    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 虚拟列表计算
 * 计算可见区域的数据
 */
export function getVisibleData({
  allData = [],
  itemHeight = 50,
  containerHeight = 600,
  scrollTop = 0,
  bufferCount = 5
}) {
  const startIndex = Math.max(0, Math.floor(scrollTop / itemHeight) - bufferCount)
  const endIndex = Math.min(
    allData.length - 1,
    Math.floor((scrollTop + containerHeight) / itemHeight) + bufferCount
  )

  const visibleData = allData.slice(startIndex, endIndex + 1)
  const offset = startIndex * itemHeight

  return {
    visibleData,
    startIndex,
    endIndex,
    offset,
    totalHeight: allData.length * itemHeight
  }
}

/**
 * 分页数据加载
 */
export class PaginationLoader {
  constructor(options = {}) {
    this.page = 1
    this.pageSize = options.pageSize || 10
    this.total = 0
    this.loading = false
    this.finished = false
    this.data = []
    this.onLoad = options.onLoad
  }

  /**
   * 加载更多数据
   */
  async loadMore() {
    if (this.loading || this.finished) {
      return
    }

    this.loading = true

    try {
      const result = await this.onLoad(this.page, this.pageSize)

      if (result && result.list) {
        this.data = [...this.data, ...result.list]
        this.total = result.total || 0

        if (this.data.length >= this.total) {
          this.finished = true
        } else {
          this.page++
        }
      } else {
        this.finished = true
      }
    } catch (error) {
      console.error('Load more error:', error)
      throw error
    } finally {
      this.loading = false
    }
  }

  /**
   * 刷新数据
   */
  async refresh() {
    this.page = 1
    this.data = []
    this.finished = false
    await this.loadMore()
  }

  /**
   * 重置
   */
  reset() {
    this.page = 1
    this.total = 0
    this.loading = false
    this.finished = false
    this.data = []
  }
}

/**
 * 图片压缩
 */
export function compressImage(src, quality = 0.8) {
  return new Promise((resolve, reject) => {
    uni.compressImage({
      src,
      quality,
      success: (res) => {
        resolve(res.tempFilePath)
      },
      fail: reject
    })
  })
}

/**
 * 缓存管理
 */
export class CacheManager {
  constructor(prefix = 'cache_') {
    this.prefix = prefix
    this.cache = new Map()
  }

  /**
   * 设置缓存
   */
  set(key, value, expire = 30 * 60 * 1000) {
    const cacheKey = this.prefix + key
    const data = {
      value,
      expire: Date.now() + expire
    }

    // #ifdef H5
    localStorage.setItem(cacheKey, JSON.stringify(data))
    // #endif

    // #ifdef MP-WEIXIN || APP-PLUS
    uni.setStorageSync(cacheKey, JSON.stringify(data))
    // #endif

    this.cache.set(key, data)
  }

  /**
   * 获取缓存
   */
  get(key) {
    const cacheKey = this.prefix + key

    // 先从内存中获取
    if (this.cache.has(key)) {
      const data = this.cache.get(key)
      if (Date.now() < data.expire) {
        return data.value
      } else {
        this.cache.delete(key)
      }
    }

    // 从存储中获取
    let cacheData
    // #ifdef H5
    cacheData = localStorage.getItem(cacheKey)
    // #endif

    // #ifdef MP-WEIXIN || APP-PLUS
    cacheData = uni.getStorageSync(cacheKey)
    // #endif

    if (cacheData) {
      try {
        const data = JSON.parse(cacheData)
        if (Date.now() < data.expire) {
          this.cache.set(key, data)
          return data.value
        } else {
          this.remove(key)
        }
      } catch (error) {
        console.error('Parse cache error:', error)
      }
    }

    return null
  }

  /**
   * 移除缓存
   */
  remove(key) {
    const cacheKey = this.prefix + key

    this.cache.delete(key)

    // #ifdef H5
    localStorage.removeItem(cacheKey)
    // #endif

    // #ifdef MP-WEIXIN || APP-PLUS
    uni.removeStorageSync(cacheKey)
    // #endif
  }

  /**
   * 清空所有缓存
   */
  clear() {
    this.cache.clear()

    // #ifdef H5
    Object.keys(localStorage).forEach(key => {
      if (key.startsWith(this.prefix)) {
        localStorage.removeItem(key)
      }
    })
    // #endif

    // #ifdef MP-WEIXIN || APP-PLUS
    const info = uni.getStorageInfoSync()
    info.keys.forEach(key => {
      if (key.startsWith(this.prefix)) {
        uni.removeStorageSync(key)
      }
    })
    // #endif
  }

  /**
   * 清理过期缓存
   */
  clearExpired() {
    const now = Date.now()

    // #ifdef H5
    Object.keys(localStorage).forEach(key => {
      if (key.startsWith(this.prefix)) {
        try {
          const data = JSON.parse(localStorage.getItem(key))
          if (data.expire < now) {
            localStorage.removeItem(key)
          }
        } catch (error) {
          localStorage.removeItem(key)
        }
      }
    })
    // #endif

    // #ifdef MP-WEIXIN || APP-PLUS
    const info = uni.getStorageInfoSync()
    info.keys.forEach(key => {
      if (key.startsWith(this.prefix)) {
        try {
          const data = uni.getStorageSync(key)
          const parsed = JSON.parse(data)
          if (parsed.expire < now) {
            uni.removeStorageSync(key)
          }
        } catch (error) {
          uni.removeStorageSync(key)
        }
      }
    })
    // #endif
  }
}

/**
 * 性能监控
 */
export class PerformanceMonitor {
  constructor() {
    this.metrics = {}
  }

  /**
   * 开始计时
   */
  start(key) {
    this.metrics[key] = {
      startTime: Date.now(),
      endTime: null,
      duration: null
    }
  }

  /**
   * 结束计时
   */
  end(key) {
    if (this.metrics[key]) {
      this.metrics[key].endTime = Date.now()
      this.metrics[key].duration = this.metrics[key].endTime - this.metrics[key].startTime

      return this.metrics[key].duration
    }
    return null
  }

  /**
   * 获取耗时
   */
  getDuration(key) {
    return this.metrics[key]?.duration || null
  }

  /**
   * 输出所有性能指标
   */
  log() {
    console.table(
      Object.entries(this.metrics)
        .filter(([_, metric]) => metric.duration)
        .map(([key, metric]) => ({
          key,
          duration: `${metric.duration}ms`
        }))
    )
  }
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 计算列表渲染性能
 */
export function measureRenderPerformance(componentName, renderFn) {
  const monitor = new PerformanceMonitor()
  monitor.start(componentName)

  const result = renderFn()

  // #ifdef H5
  // 等待下一个tick
  Promise.resolve().then(() => {
    monitor.end(componentName)
    const duration = monitor.getDuration(componentName)
    if (duration > 16) {
      console.warn(`${componentName} render takes ${duration}ms, may cause frame drop`)
    }
  })
  // #endif

  return result
}

export default {
  LazyLoad,
  throttle,
  debounce,
  getVisibleData,
  PaginationLoader,
  compressImage,
  CacheManager,
  PerformanceMonitor,
  formatFileSize,
  measureRenderPerformance
}
