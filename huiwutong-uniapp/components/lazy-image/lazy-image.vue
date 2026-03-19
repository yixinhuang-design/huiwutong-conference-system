<!--
  图片懒加载组件
  支持不同平台的图片懒加载
-->
<template>
  <view
    :class="['lazy-image', customClass, { 'loaded': isLoaded, 'error': hasError }]"
    :style="containerStyle"
  >
    <!-- 实际图片 -->
    <image
      v-if="shouldLoad"
      :src="currentSrc"
      :mode="mode"
      :lazy-load="enableLazyLoad"
      :class="imageClass"
      :style="imageStyle"
      @load="handleLoad"
      @error="handleError"
    />

    <!-- 占位图 -->
    <view v-if="!isLoaded && !hasError" class="lazy-image-placeholder" :style="placeholderStyle">
      <slot name="placeholder">
        <text class="placeholder-text">加载中...</text>
      </slot>
    </view>

    <!-- 错误占位 -->
    <view v-if="hasError" class="lazy-image-error" :style="placeholderStyle">
      <slot name="error">
        <text class="error-text">加载失败</text>
      </slot>
    </view>
  </view>
</template>

<script>
export default {
  name: 'LazyImage',
  props: {
    // 图片地址
    src: {
      type: String,
      required: true
    },
    // 图片裁剪、缩放模式
    mode: {
      type: String,
      default: 'aspectFill'
    },
    // 宽度
    width: {
      type: [String, Number],
      default: '100%'
    },
    // 高度
    height: {
      type: [String, Number],
      default: 'auto'
    },
    // 占位图
    placeholder: {
      type: String,
      default: ''
    },
    // 错误图
    errorSrc: {
      type: String,
      default: ''
    },
    // 是否启用懒加载
    enableLazyLoad: {
      type: Boolean,
      default: true
    },
    // 自定义类名
    customClass: {
      type: String,
      default: ''
    },
    // 图片类名
    imageClass: {
      type: String,
      default: ''
    },
    // 圆角
    borderRadius: {
      type: [String, Number],
      default: 0
    },
    // 是否立即加载
    immediate: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isLoaded: false,
      hasError: false,
      shouldLoad: false,
      currentSrc: '',
      observer: null
    }
  },
  computed: {
    containerStyle() {
      const style = {
        width: typeof this.width === 'number' ? this.width + 'px' : this.width,
        borderRadius: typeof this.borderRadius === 'number' ? this.borderRadius + 'px' : this.borderRadius
      }

      if (this.height !== 'auto') {
        style.height = typeof this.height === 'number' ? this.height + 'px' : this.height
      }

      return style
    },
    imageStyle() {
      return {
        width: '100%',
        height: '100%',
        borderRadius: typeof this.borderRadius === 'number' ? this.borderRadius + 'px' : this.borderRadius
      }
    },
    placeholderStyle() {
      const style = {
        width: '100%',
        height: '100%'
      }

      if (this.height !== 'auto') {
        style.height = typeof this.height === 'number' ? this.height + 'px' : this.height
      } else {
        style.minHeight = '200px'
      }

      return style
    }
  },
  watch: {
    src: {
      handler(newSrc) {
        if (newSrc) {
          this.loadImage(newSrc)
        }
      },
      immediate: true
    }
  },
  mounted() {
    // #ifdef H5
    // H5使用 Intersection Observer API
    if (!this.immediate && this.enableLazyLoad) {
      this.initIntersectionObserver()
    } else {
      this.shouldLoad = true
      this.loadImage(this.src)
    }
    // #endif

    // #ifdef MP-WEIXIN || APP-PLUS
    // 小程序和App使用原生懒加载
    this.shouldLoad = true
    this.loadImage(this.src)
    // #endif
  },
  beforeUnmount() {
    // #ifdef H5
    if (this.observer) {
      this.observer.disconnect()
      this.observer = null
    }
    // #endif
  },
  methods: {
    /**
     * 初始化 Intersection Observer
     */
    // #ifdef H5
    initIntersectionObserver() {
      this.observer = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              this.shouldLoad = true
              this.loadImage(this.src)
              this.observer.unobserve(entry.target)
            }
          })
        },
        {
          rootMargin: '50px'
        }
      )

      const el = this.$el
      if (el) {
        this.observer.observe(el)
      }
    },
    // #endif

    /**
     * 加载图片
     */
    loadImage(src) {
      if (!src || this.isLoaded) {
        return
      }

      this.currentSrc = src
      this.hasError = false
    },

    /**
     * 图片加载完成
     */
    handleLoad(e) {
      this.isLoaded = true
      this.hasError = false
      this.$emit('load', e)
    },

    /**
     * 图片加载错误
     */
    handleError(e) {
      this.isLoaded = false
      this.hasError = true

      // 如果有错误图，尝试加载错误图
      if (this.errorSrc && this.currentSrc !== this.errorSrc) {
        this.currentSrc = this.errorSrc
      }

      this.$emit('error', e)
    },

    /**
     * 重新加载
     */
    reload() {
      this.isLoaded = false
      this.hasError = false
      this.shouldLoad = true
      this.currentSrc = this.src
    }
  }
}
</script>

<style lang="scss" scoped>
.lazy-image {
  position: relative;
  overflow: hidden;
  background-color: #f5f5f5;

  image {
    display: block;
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &.loaded image {
    opacity: 1;
  }

  .lazy-image-placeholder,
  .lazy-image-error {
    position: absolute;
    top: 0;
    left: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    color: #999;
    font-size: 14px;

    .placeholder-text,
    .error-text {
      color: #999;
    }
  }

  .lazy-image-error {
    background-color: #fafafa;

    .error-text {
      color: #f56c6c;
    }
  }
}
</style>
