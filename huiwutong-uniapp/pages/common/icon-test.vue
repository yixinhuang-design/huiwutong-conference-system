<template>
  <view class="icon-test-container">
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">图标测试</text>
      </view>
    </view>

    <view class="test-section card">
      <view class="section-title">基础图标测试</view>

      <view class="icon-grid">
        <view class="icon-item">
          <text class="icon-test"><text class="fa fa-check"></text></text>
          <text class="icon-label">fa-check</text>
        </view>
        <view class="icon-item">
          <text class="icon-test"><text class="fa fa-times"></text></text>
          <text class="icon-label">fa-times</text>
        </view>
        <view class="icon-item">
          <text class="icon-test"><text class="fa fa-home"></text></text>
          <text class="icon-label">fa-home</text>
        </view>
        <view class="icon-item">
          <text class="icon-test"><text class="fa fa-user"></text></text>
          <text class="icon-label">fa-user</text>
        </view>
        <view class="icon-item">
          <text class="icon-test"><text class="fa fa-star"></text></text>
          <text class="icon-label">fa-star</text>
        </view>
        <view class="icon-item">
          <text class="icon-test"><text class="fa fa-heart"></text></text>
          <text class="icon-label">fa-heart</text>
        </view>
        <view class="icon-item">
          <text class="icon-test"><text class="fa fa-calendar-alt"></text></text>
          <text class="icon-label">fa-calendar-alt</text>
        </view>
        <view class="icon-item">
          <text class="icon-test"><text class="fa fa-clock"></text></text>
          <text class="icon-label">fa-clock</text>
        </view>
      </view>
    </view>

    <view class="test-section card">
      <view class="section-title">动态图标测试</view>

      <view class="dynamic-test">
        <view class="test-row">
          <text class="test-label">动态类名：</text>
          <text class="icon-test"><text :class="dynamicIcon"></text></text>
          <text class="icon-name">{{ dynamicIcon }}</text>
        </view>
        <button class="btn btn-outline btn-sm" @click="switchIcon">
          切换图标
        </button>
      </view>
    </view>

    <view class="test-section card">
      <view class="section-title">颜色测试</view>

      <view class="icon-grid">
        <view class="icon-item">
          <text class="icon-test primary"><text class="fa fa-star"></text></text>
          <text class="icon-label">主要色</text>
        </view>
        <view class="icon-item">
          <text class="icon-test success"><text class="fa fa-check"></text></text>
          <text class="icon-label">成功色</text>
        </view>
        <view class="icon-item">
          <text class="icon-test warning"><text class="fa fa-exclamation-triangle"></text></text>
          <text class="icon-label">警告色</text>
        </view>
        <view class="icon-item">
          <text class="icon-test danger"><text class="fa fa-times"></text></text>
          <text class="icon-label">危险色</text>
        </view>
      </view>
    </view>

    <view class="test-section card">
      <view class="section-title">字体大小测试</view>

      <view class="size-test">
        <view class="size-item">
          <text class="icon-test" style="font-size: 24rpx"><text class="fa fa-home"></text></text>
          <text class="size-label">24rpx</text>
        </view>
        <view class="size-item">
          <text class="icon-test" style="font-size: 32rpx"><text class="fa fa-home"></text></text>
          <text class="size-label">32rpx</text>
        </view>
        <view class="size-item">
          <text class="icon-test" style="font-size: 48rpx"><text class="fa fa-home"></text></text>
          <text class="size-label">48rpx</text>
        </view>
        <view class="size-item">
          <text class="icon-test" style="font-size: 64rpx"><text class="fa fa-home"></text></text>
          <text class="size-label">64rpx</text>
        </view>
      </view>
    </view>

    <view class="test-section card">
      <view class="section-title">说明</view>
      <view class="info-text">
        <text>✓ 如果看到正常图标，说明 Font Awesome 加载成功</text>
        <text>✓ 如果看到占位符，请检查网络连接或字体文件</text>
        <text>✓ 支持动态类名绑定：:class="iconClass"</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      icons: ['fa-home', 'fa-user', 'fa-star', 'fa-heart', 'fa-check'],
      currentIndex: 0
    }
  },
  computed: {
    dynamicIcon() {
      return this.icons[this.currentIndex]
    }
  },
  methods: {
    switchIcon() {
      this.currentIndex = (this.currentIndex + 1) % this.icons.length
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.icon-test-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding: $spacing-md;
}

.test-section {
  margin-bottom: $spacing-md;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
  padding: $spacing-md 0;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.icon-test {
  font-size: 48rpx;
  color: $primary-color;
}

.icon-test.primary {
  color: $primary-color;
}

.icon-test.success {
  color: $success-color;
}

.icon-test.warning {
  color: $warning-color;
}

.icon-test.danger {
  color: $danger-color;
}

.icon-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  text-align: center;
}

.dynamic-test {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.test-row {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.test-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.icon-name {
  font-size: $font-size-sm;
  color: $primary-color;
  padding: $spacing-sm $spacing-md;
  background: rgba(102, 126, 234, 0.1);
  border-radius: $border-radius-sm;
}

.size-test {
  display: flex;
  justify-content: space-around;
  padding: $spacing-lg 0;
}

.size-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.size-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.info-text text {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: 1.6;
}
</style>
