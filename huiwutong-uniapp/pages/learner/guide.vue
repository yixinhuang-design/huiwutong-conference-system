<template>
  <view class="guide-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">使用指南</text>
      </view>
    </view>

    <!-- 快速导航 -->
    <view class="quick-nav card">
      <scroll-view class="nav-scroll" scroll-x>
        <view class="nav-list">
          <view
            v-for="(item, index) in quickNav"
            :key="index"
            class="nav-item"
            @click="scrollToSection(index)"
          >
            <text class="nav-icon"><text :class="item.icon"></text></text>
            <text class="nav-text">{{ item.title }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 指南内容 -->
    <scroll-view class="guide-content" scroll-y :scroll-into-view="scrollSection">
      <view :id="'section-0'" class="guide-section card">
        <view class="section-header">
          <text class="section-icon"><text class="fa fa-check"></text></text>
          <text class="section-title-text">报到须知</text>
        </view>
        <view class="section-content">
          <view class="guide-item">
            <text class="item-title">报到时间</text>
            <text class="item-text">培训当天08:30-09:00报到</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      scrollSection: '',
      quickNav: [
        { icon: 'fa-check', title: '报到须知' },
        { icon: 'fa-clock', title: '签到规则' }
      ]
    }
  },
  methods: {
    scrollToSection(index) {
      this.scrollSection = 'section-' + index
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.guide-container {
  min-height: 100vh;
  background: $bg-secondary;
}

.quick-nav {
  margin: $spacing-md;
}

.nav-list {
  display: inline-flex;
  gap: $spacing-sm;
}

.nav-item {
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
}

.section-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.section-icon {
  font-size: 40rpx;
}

.section-title-text {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}
</style>
