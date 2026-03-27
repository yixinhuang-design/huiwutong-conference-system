<template>
  <view class="guide-container">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

    <!-- 引导页轮播 -->
    <swiper
      class="guide-swiper"
      :indicator-dots="true"
      :autoplay="false"
      :interval="3000"
      :duration="300"
      indicator-color="rgba(255, 255, 255, 0.5)"
      indicator-active-color="#667eea"
      @change="onSwiperChange"
    >
      <swiper-item v-for="(item, index) in guideList" :key="index">
        <view class="guide-item">
          <image class="guide-image" :src="item.image" mode="aspectFit" />
          <view class="guide-title">{{ item.title }}</view>
          <view class="guide-desc">{{ item.desc }}</view>

          <!-- 最后一页显示按钮 -->
          <button v-if="index === guideList.length - 1" class="guide-btn" @click="handleStart">
            立即体验
          </button>
          <button v-else class="guide-btn" @click="handleSkip">
            跳过
          </button>
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      currentIndex: 0,
      guideList: [
        {
          image: '/static/images/guide1.png',
          title: '智能会议助手',
          desc: '您的专属会议管家，让会议更高效'
        },
        {
          image: '/static/images/guide2.png',
          title: '会议管理',
          desc: '一键查看会议详情、日程安排'
        },
        {
          image: '/static/images/guide3.png',
          title: '智能提醒',
          desc: 'AI助手智能提醒，不错过任何重要信息'
        },
        {
          image: '/static/images/guide4.png',
          title: '便捷服务',
          desc: '报名、签到、座位，一站式服务'
        }
      ]
    }
  },

  onLoad() {
    // 获取状态栏高度
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
      }
    })

    // 检查是否已看过引导页
    const hasSeenGuide = uni.getStorageSync('hasSeenGuide')
    if (hasSeenGuide) {
      this.redirectToHome()
    }
  },

  methods: {
    /**
     * 轮播切换
     */
    onSwiperChange(e) {
      this.currentIndex = e.detail.current
    },

    /**
     * 开始使用
     */
    handleStart() {
      // 标记已看过引导页
      uni.setStorageSync('hasSeenGuide', true)

      // 跳转到首页
      this.redirectToHome()
    },

    /**
     * 跳过引导
     */
    handleSkip() {
      uni.setStorageSync('hasSeenGuide', true)
      this.redirectToHome()
    },

    /**
     * 跳转到首页
     */
    redirectToHome() {
      uni.switchTab({
        url: '/pages/common/home'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.guide-container {
  width: 100%;
  height: 100vh;
  background: $bg-primary;
}

.status-bar {
  width: 100%;
  background: $bg-primary;
}

.guide-swiper {
  width: 100%;
  height: calc(100vh - var(--status-bar-height));
}

.guide-item {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.guide-image {
  width: 500rpx;
  height: 500rpx;
  margin-bottom: $spacing-xl;
}

.guide-title {
  font-size: $font-size-xxl;
  font-weight: 600;
  color: $text-white;
  margin-bottom: $spacing-md;
  text-align: center;
}

.guide-desc {
  font-size: $font-size-md;
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  line-height: 1.6;
  margin-bottom: $spacing-xxl;
}

.guide-btn {
  min-width: 300rpx;
  padding: $spacing-md $spacing-xl;
  background: rgba(255, 255, 255, 0.2);
  border: 2rpx solid rgba(255, 255, 255, 0.5);
  border-radius: 50rpx;
  color: $text-white;
  font-size: $font-size-lg;
  font-weight: 500;
  backdrop-filter: blur(10rpx);
  transition: $transition-base;
}

.guide-btn:active {
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.3);
}
</style>
