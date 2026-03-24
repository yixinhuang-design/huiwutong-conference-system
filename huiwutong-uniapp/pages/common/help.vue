<template>
  <view class="help-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">帮助中心</text>
      </view>
    </view>

    <!-- 搜索框 -->
    <view class="search-bar card">
      <view class="search-input-wrapper">
        <text class="search-icon"><text class="fa fa-search"></text></text>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索问题..."
          @confirm="handleSearch"
        />
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="faq-section card">
      <view class="section-title">常见问题</view>
      <view class="faq-list">
        <view
          v-for="(faq, index) in faqList"
          :key="index"
          class="faq-item"
          @click="toggleFaq(index)"
        >
          <view class="faq-question">
            <text class="question-text">{{ faq.question }}</text>
            <text class="faq-arrow" :class="{ expanded: faq.expanded }">›</text>
          </view>
          <view v-if="faq.expanded" class="faq-answer">
            {{ faq.answer }}
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-links card">
      <view class="section-title">快捷入口</view>
      <view class="link-grid grid-3">
        <view
          v-for="(link, index) in quickLinks"
          :key="index"
          class="link-item"
          @click="handleLink(link.action)"
        >
          <view class="link-icon"><text :class="link.icon"></text></view>
          <view class="link-title">{{ link.title }}</view>
        </view>
      </view>
    </view>

    <!-- 联系方式 -->
    <view class="contact-card card">
      <view class="section-title">联系我们</view>
      <view class="contact-list">
        <view class="contact-item">
          <text class="contact-icon"><text class="fa fa-phone"></text></text>
          <view class="contact-info">
            <text class="contact-label">客服热线</text>
            <text class="contact-value">400-888-8888</text>
          </view>
        </view>
        <view class="contact-item">
          <text class="contact-icon"><text class="fa fa-envelope"></text></text>
          <view class="contact-info">
            <text class="contact-label">客服邮箱</text>
            <text class="contact-value">support@huiwutong.com</text>
          </view>
        </view>
        <view class="contact-item">
          <text class="contact-icon"><text class="fa fa-comments"></text></text>
          <view class="contact-info">
            <text class="contact-label">在线客服</text>
            <text class="contact-value">工作日 9:00-18:00</text>
          </view>
        </view>
      </view>
      <button class="contact-btn btn btn-primary btn-block">
        在线咨询
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchKeyword: '',
      faqList: [
        {
          question: '如何进行签到？',
          answer: '您可以通过以下方式签到：1）在日程页面点击"签到"按钮；2）在签到页面使用扫码签到或定位签到功能。签到时间为课程开始前30分钟至结束后10分钟。',
          expanded: false
        },
        {
          question: '如何查看我的座位？',
          answer: '在首页点击"座位图"或在功能导航中找到"座位图"入口，即可查看您的座位信息。座位图会显示您的座位位置和区域。',
          expanded: false
        },
        {
          question: '如何下载学习资料？',
          answer: '进入"学习资料"页面，选择需要的资料点击下载即可。支持的文件格式包括PDF、Word、PPT等。',
          expanded: false
        },
        {
          question: '如何修改个人信息？',
          answer: '进入"我的"页面，点击个人头像或"编辑"按钮，即可修改个人信息，包括姓名、单位、职务等。',
          expanded: false
        },
        {
          question: '忘记密码怎么办？',
          answer: '在登录页面点击"忘记密码"，通过手机验证码重置密码。如果绑定了邮箱，也可以通过邮箱找回密码。',
          expanded: false
        }
      ],
      quickLinks: [
        { icon: 'fa-book', title: '使用指南', action: 'guide' },
        { icon: 'fa-film', title: '视频教程', action: 'video' },
        { icon: 'fa-edit', title: '意见反馈', action: 'feedback' }
      ]
    }
  },

  methods: {
    /**
     * 搜索问题
     */
    handleSearch() {
      if (!this.searchKeyword.trim()) {
        uni.showToast({
          title: '请输入搜索关键词',
          icon: 'none'
        })
        return
      }

      uni.showToast({
        title: '搜索功能开发中',
        icon: 'none'
      })
    },

    /**
     * 切换FAQ展开状态
     */
    toggleFaq(index) {
      this.faqList[index].expanded = !this.faqList[index].expanded
    },

    /**
     * 处理快捷入口
     */
    handleLink(action) {
      const actions = {
        guide: '使用指南',
        video: '视频教程',
        feedback: '意见反馈'
      }

      if (actions[action]) {
        uni.showToast({
          title: actions[action] + '功能开发中',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.help-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.search-bar {
  margin: $spacing-md;
  padding: 0;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.search-icon {
  font-size: $font-size-lg;
}

.search-input {
  flex: 1;
  font-size: $font-size-md;
}

.faq-section,
.quick-links,
.contact-card {
  margin: $spacing-md;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.faq-item {
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  overflow: hidden;
}

.faq-question {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.faq-arrow {
  font-size: 40rpx;
  color: $text-tertiary;
  font-weight: 300;
  transition: $transition-base;
}

.faq-arrow.expanded {
  transform: rotate(90deg);
}

.faq-answer {
  padding: $spacing-md;
  padding-top: 0;
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.6;
  border-top: 1rpx solid $border-color;
}

.link-grid {
  padding: $spacing-lg 0;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.link-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: $border-radius-lg;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
}

.link-title {
  font-size: $font-size-sm;
  color: $text-primary;
}

.contact-list {
  margin-bottom: $spacing-md;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.contact-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.contact-value {
  font-size: $font-size-md;
  color: $text-primary;
  font-weight: 500;
}

.contact-btn {
  margin-top: $spacing-sm;
}
</style>
