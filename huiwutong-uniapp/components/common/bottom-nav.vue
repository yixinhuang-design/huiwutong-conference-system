<template>
  <view class="bottom-nav">
    <view
      v-for="(item, index) in navList"
      :key="index"
      class="nav-item"
      :class="{ active: currentPath === item.path }"
      @click="navigateTo(item.path, index)"
    >
      <view class="nav-icon">
        <text v-if="currentPath === item.path">{{ item.activeIcon }}</text>
        <text v-else>{{ item.icon }}</text>
      </view>
      <view class="nav-text">{{ item.text }}</view>
      <view class="nav-indicator" v-if="currentPath === item.path"></view>
    </view>

    <!-- 安全区域 -->
    <view class="safe-area"></view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentPath: '/pages/common/home',
      navList: [
        {
          path: '/pages/common/home',
          icon: '🏠',
          activeIcon: '🏠',
          text: '首页'
        },
        {
          path: '/pages/common/assistant',
          icon: '🤖',
          activeIcon: '🤖',
          text: '助理'
        },
        {
          path: '/pages/common/communication',
          icon: '💬',
          activeIcon: '💬',
          text: '沟通'
        },
        {
          path: '/pages/common/past',
          icon: '📁',
          activeIcon: '📁',
          text: '档案'
        },
        {
          path: '/pages/common/profile',
          icon: '👤',
          activeIcon: '👤',
          text: '我的'
        }
      ]
    }
  },

  mounted() {
    // 获取当前页面路径
    this.getCurrentPath()
  },

  methods: {
    /**
     * 获取当前页面路径
     */
    getCurrentPath() {
      const pages = getCurrentPages()
      if (pages.length > 0) {
        const currentPage = pages[pages.length - 1]
        const route = currentPage.route || ''
        this.currentPath = '/' + route
      }
    },

    /**
     * 页面跳转
     */
    navigateTo(path, index) {
      if (this.currentPath === path) return

      // 使用switchTab跳转（因为配置了tabBar）
      uni.switchTab({
        url: path,
        fail: () => {
          // 如果switchTab失败，使用redirectTo
          uni.redirectTo({
            url: path
          })
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $bg-primary;
  border-top: 1rpx solid $border-color;
  display: flex;
  align-items: center;
  z-index: $z-index-fixed;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 12rpx;
  color: $text-secondary;
  transition: $transition-base;
  position: relative;
  height: 100rpx;
}

.nav-item.active {
  color: $accent-color;
}

.nav-item:active {
  background: $bg-secondary;
}

.nav-icon {
  font-size: 44rpx;
  line-height: 1;
}

.nav-text {
  font-size: 20rpx;
  font-weight: 500;
  line-height: 1;
}

.nav-indicator {
  position: absolute;
  bottom: 8rpx;
  width: 12rpx;
  height: 12rpx;
  background: $accent-color;
  border-radius: 50%;
}

.safe-area {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
  min-height: 0;
}
</style>
