<!--
  自定义导航栏组件
  支持不同平台的导航栏样式
-->
<template>
  <view
    :class="['nav-bar', platformClass]"
    :style="navBarStyle"
  >
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 导航栏内容 -->
    <view class="nav-content">
      <!-- 返回按钮 -->
      <view v-if="showBack" class="nav-back" @click="handleBack">
        <text class="iconfont icon-back">{{ backIcon }}</text>
      </view>

      <!-- 标题 -->
      <view class="nav-title" :style="titleStyle">
        {{ title }}
      </view>

      <!-- 右侧操作区 -->
      <view class="nav-actions">
        <slot name="actions"></slot>
      </view>
    </view>
  </view>
</template>

<script>
import { getStatusBarHeight } from '@/utils/platform'

export default {
  name: 'NavBar',
  props: {
    // 标题
    title: {
      type: String,
      default: ''
    },
    // 是否显示返回按钮
    showBack: {
      type: Boolean,
      default: true
    },
    // 背景颜色
    bgColor: {
      type: String,
      default: '#667eea'
    },
    // 标题颜色
    titleColor: {
      type: String,
      default: '#ffffff'
    },
    // 返回图标
    backIcon: {
      type: String,
      default: '<'
    }
  },
  data() {
    return {
      statusBarHeight: 0
    }
  },
  computed: {
    navBarStyle() {
      return {
        backgroundColor: this.bgColor
      }
    },
    titleStyle() {
      return {
        color: this.titleColor
      }
    },
    platformClass() {
      // #ifdef H5
      return 'platform-h5'
      // #endif

      // #ifdef MP-WEIXIN
      return 'platform-mp'
      // #endif

      // #ifdef APP-PLUS
      return 'platform-app'
      // #endif

      return ''
    }
  },
  async mounted() {
    // 获取状态栏高度
    this.statusBarHeight = await getStatusBarHeight()
  },
  methods: {
    handleBack() {
      // #ifdef H5
      const pages = getCurrentPages()
      if (pages.length <= 1) {
        this.$router.back()
      } else {
        uni.navigateBack()
      }
      // #endif

      // #ifdef MP-WEIXIN || APP-PLUS
      uni.navigateBack()
      // #endif
    }
  }
}
</script>

<style lang="scss" scoped>
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;

  .status-bar {
    width: 100%;
  }

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 16px;

    .nav-back {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      cursor: pointer;

      // #ifdef H5
      &:hover {
        opacity: 0.7;
      }
      // #endif
    }

    .nav-title {
      flex: 1;
      font-size: 18px;
      font-weight: 600;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      padding: 0 12px;
    }

    .nav-actions {
      display: flex;
      align-items: center;
      min-width: 44px;
      justify-content: flex-end;
    }
  }

  // H5特定样式
  // #ifdef H5
  &.platform-h5 {
    .nav-back {
      cursor: pointer;
      user-select: none;
    }
  }
  // #endif

  // 小程序特定样式
  // #ifdef MP-WEIXIN
  &.platform-mp {
    .nav-title {
      max-width: 60%;
    }
  }
  // #endif

  // App特定样式
  // #ifdef APP-PLUS
  &.platform-app {
    .nav-back:active {
      opacity: 0.7;
    }
  }
  // #endif
}
</style>
