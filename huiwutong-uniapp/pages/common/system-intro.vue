<template>
  <view class="system-intro-container">
    <!-- 自定义导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="nav-back" @click="handleBack">
          <text class="icon">←</text>
        </view>
        <view class="nav-title">系统介绍</view>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view class="content-scroll" scroll-y>
      <!-- 系统简介 -->
      <view class="intro-section">
        <view class="section-title">
          <text class="title-icon">📱</text>
          <text class="title-text">智能会议助手</text>
        </view>
        <view class="section-content">
          <text class="content-text">
            智能会议助手是一款专为会议场景打造的全方位移动应用，为会议组织者和参会者提供便捷、高效的会议管理服务。
          </text>
        </view>
      </view>

      <!-- 核心功能 -->
      <view class="feature-section">
        <view class="section-title">
          <text class="title-icon">✨</text>
          <text class="title-text">核心功能</text>
        </view>

        <view class="feature-list">
          <view class="feature-item">
            <view class="feature-icon">🎯</view>
            <view class="feature-info">
              <view class="feature-name">会议管理</view>
              <view class="feature-desc">查看会议详情、日程安排、参会人员等信息</view>
            </view>
          </view>

          <view class="feature-item">
            <view class="feature-icon">📝</view>
            <view class="feature-info">
              <view class="feature-name">在线报名</view>
              <view class="feature-desc">支持在线报名、信息填写、资料上传</view>
            </view>
          </view>

          <view class="feature-item">
            <view class="feature-icon">✅</view>
            <view class="feature-info">
              <view class="feature-name">扫码签到</view>
              <view class="feature-desc">二维码签到，快速便捷，智能识别</view>
            </view>
          </view>

          <view class="feature-item">
            <view class="feature-icon">💺</view>
            <view class="feature-info">
              <view class="feature-name">座位管理</view>
              <view class="feature-desc">查看座位安排，支持在线选座和换座</view>
            </view>
          </view>

          <view class="feature-item">
            <view class="feature-icon">💬</view>
            <view class="feature-info">
              <view class="feature-name">即时沟通</view>
              <view class="feature-desc">支持群聊和私聊，方便参会者交流</view>
            </view>
          </view>

          <view class="feature-item">
            <view class="feature-icon">📊</view>
            <view class="feature-info">
              <view class="feature-name">数据统计</view>
              <view class="feature-desc">实时统计分析，数据可视化展示</view>
            </view>
          </view>

          <view class="feature-item">
            <view class="feature-icon">🤖</view>
            <view class="feature-info">
              <view class="feature-name">AI助手</view>
              <view class="feature-desc">智能问答、日程提醒、信息推送</view>
            </view>
          </view>

          <view class="feature-item">
            <view class="feature-icon">📁</view>
            <view class="feature-info">
              <view class="feature-name">会议档案</view>
              <view class="feature-desc">资料归档、历史记录、精彩回顾</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 适用场景 -->
      <view class="scene-section">
        <view class="section-title">
          <text class="title-icon">🏢</text>
          <text class="title-text">适用场景</text>
        </view>

        <view class="scene-list">
          <view class="scene-item">
            <view class="scene-tag">政府会议</view>
            <text class="scene-desc">各级党政机关会议管理</text>
          </view>

          <view class="scene-item">
            <view class="scene-tag">企业会议</view>
            <text class="scene-desc">企业内部会议、客户会议</text>
          </view>

          <view class="scene-item">
            <view class="scene-tag">学术会议</view>
            <text class="scene-desc">学术研讨、论坛讲座</text>
          </view>

          <view class="scene-item">
            <view class="scene-tag">培训会议</view>
            <text class="scene-desc">培训课程、研修班</text>
          </view>
        </view>
      </view>

      <!-- 系统特色 -->
      <view class="highlight-section">
        <view class="section-title">
          <text class="title-icon">🌟</text>
          <text class="title-text">系统特色</text>
        </view>

        <view class="highlight-list">
          <view class="highlight-item">
            <view class="highlight-number">01</view>
            <view class="highlight-content">
              <view class="highlight-title">多端支持</view>
              <view class="highlight-desc">支持H5、微信小程序、App多端使用</view>
            </view>
          </view>

          <view class="highlight-item">
            <view class="highlight-number">02</view>
            <view class="highlight-content">
              <view class="highlight-title">安全可靠</view>
              <view class="highlight-desc">企业级安全保障，数据加密传输</view>
            </view>
          </view>

          <view class="highlight-item">
            <view class="highlight-number">03</view>
            <view class="highlight-content">
              <view class="highlight-title">智能高效</view>
              <view class="highlight-desc">AI智能辅助，提升会议效率</view>
            </view>
          </view>

          <view class="highlight-item">
            <view class="highlight-number">04</view>
            <view class="highlight-content">
              <view class="highlight-title">灵活配置</view>
              <view class="highlight-desc">支持多租户、多角色、权限管理</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部信息 -->
      <view class="footer-info">
        <text class="footer-text">版本号：v1.0.0</text>
        <text class="footer-text">© 2024 智能会议助手 版权所有</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0
    }
  },

  onLoad() {
    // 获取状态栏高度
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
      }
    })
  },

  methods: {
    /**
     * 返回上一页
     */
    handleBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';

.system-intro-container {
  width: 100%;
  min-height: 100vh;
  background: $bg-secondary;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: $bg-primary;
  box-shadow: $shadow-sm;
  z-index: 100;
}

.nav-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 $spacing-md;
}

.nav-back {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  cursor: pointer;
}

.nav-title {
  flex: 1;
  font-size: $font-size-lg;
  font-weight: 600;
  text-align: center;
}

.nav-placeholder {
  width: 44px;
}

.content-scroll {
  height: calc(100vh - var(--status-bar-height) - 44px);
  margin-top: calc(var(--status-bar-height) + 44px);
}

.intro-section,
.feature-section,
.scene-section,
.highlight-section {
  background: $bg-primary;
  margin: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  box-shadow: $shadow-sm;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.title-icon {
  font-size: 32rpx;
  margin-right: $spacing-sm;
}

.title-text {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.content-text {
  font-size: $font-size-md;
  line-height: 1.8;
  color: $text-secondary;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  padding: $spacing-md;
  background: $bg-secondary;
  border-radius: $border-radius-md;
}

.feature-icon {
  font-size: 48rpx;
  margin-right: $spacing-md;
}

.feature-info {
  flex: 1;
}

.feature-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.feature-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.6;
}

.scene-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-md;
}

.scene-item {
  background: $bg-secondary;
  padding: $spacing-md;
  border-radius: $border-radius-md;
  text-align: center;
}

.scene-tag {
  display: inline-block;
  padding: 8rpx 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: $text-white;
  font-size: $font-size-sm;
  border-radius: $border-radius-full;
  margin-bottom: $spacing-sm;
}

.scene-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.highlight-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.highlight-item {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: $border-radius-md;
  border-left: 4px solid $primary-color;
}

.highlight-number {
  font-size: 48rpx;
  font-weight: 700;
  color: $primary-color;
  margin-right: $spacing-md;
  min-width: 80rpx;
}

.highlight-content {
  flex: 1;
}

.highlight-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.highlight-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.footer-info {
  padding: $spacing-xxl $spacing-md;
  text-align: center;
}

.footer-text {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
  margin-bottom: $spacing-xs;
}
</style>
