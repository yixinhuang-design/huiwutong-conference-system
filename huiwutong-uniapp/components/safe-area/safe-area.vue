<!--
  安全区域适配组件
  用于适配不同平台的安全区域（刘海屏、底部指示器等）
-->
<template>
  <view
    :class="['safe-area', customClass]"
    :style="safeAreaStyle"
  >
    <slot></slot>
  </view>
</template>

<script>
export default {
  name: 'SafeArea',
  props: {
    // 顶部安全区域
    top: {
      type: Boolean,
      default: false
    },
    // 底部安全区域
    bottom: {
      type: Boolean,
      default: true
    },
    // 自定义类名
    customClass: {
      type: String,
      default: ''
    },
    // 背景颜色
    bgColor: {
      type: String,
      default: '#ffffff'
    }
  },
  computed: {
    safeAreaStyle() {
      const styles = {
        backgroundColor: this.bgColor
      }

      // #ifdef APP-PLUS
      // App端需要获取系统信息
      const systemInfo = uni.getSystemInfoSync()
      if (this.top) {
        styles.paddingTop = (systemInfo.statusBarHeight || 0) + 'px'
      }
      if (this.bottom) {
        // iOS底部安全区域
        if (systemInfo.platform === 'ios') {
          styles.paddingBottom = '34px'
        }
      }
      // #endif

      // #ifdef MP-WEIXIN
      // 微信小程序
      if (this.top) {
        const systemInfo = uni.getSystemInfoSync()
        styles.paddingTop = (systemInfo.statusBarHeight || 0) + 'px'
      }
      // #endif

      return styles
    }
  }
}
</script>

<style lang="scss" scoped>
.safe-area {
  width: 100%;

  // H5不需要特殊处理
  // #ifdef H5
  box-sizing: border-box;
  // #endif

  // #ifdef MP-WEIXIN
  box-sizing: border-box;
  // #endif

  // #ifdef APP-PLUS
  box-sizing: border-box;
  // #endif
}
</style>
