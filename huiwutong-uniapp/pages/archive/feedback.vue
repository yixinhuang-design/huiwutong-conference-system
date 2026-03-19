<template>
  <view class="feedback-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">互动反馈</text>
        <text class="header-action" @click="exportData">导出</text>
      </view>
    </view>

    <!-- 培训选择 -->
    <view class="training-selector">
      <picker mode="selector" :range="trainings" @change="onTrainingChange">
        <view class="selector-bar">
          <text class="selector-label">选择培训</text>
          <view class="selector-value">
            <text>{{ selectedTraining }}</text>
            <text class="selector-arrow">▼</text>
          </view>
        </view>
      </picker>
    </view>

    <!-- 统计概览 -->
    <view class="stats-overview card">
      <view class="overview-title">反馈统计</view>
      <view class="stats-grid">
        <view class="stat-box">
          <text class="stat-value">{{ stats.total }}</text>
          <text class="stat-label">反馈总数</text>
        </view>
        <view class="stat-box">
          <text class="stat-value">{{ stats.rate }}%</text>
          <text class="stat-label">反馈率</text>
        </view>
        <view class="stat-box">
          <text class="stat-value">{{ stats.satisfaction }}%</text>
          <text class="stat-label">满意度</text>
        </view>
      </view>
    </view>

    <scroll-view class="feedback-scroll" scroll-y @scrolltolower="loadMore">
      <!-- 反馈分类标签 -->
      <view class="category-tabs">
        <scroll-view class="tabs-scroll" scroll-x>
          <view class="tabs-list">
            <view
              v-for="tab in categoryTabs"
              :key="tab.value"
              class="tab-item"
              :class="{ active: activeCategory === tab.value }"
              @click="switchCategory(tab.value)"
            >
              <text class="tab-label">{{ tab.label }}</text>
              <text class="tab-count">({{ tab.count }})</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 反馈列表 -->
      <view class="feedback-list">
        <view
          v-for="(item, index) in filteredFeedback"
          :key="item.id"
          class="feedback-card card"
        >
          <view class="feedback-header">
            <view class="user-info">
              <view class="user-avatar">
                {{ item.userName.charAt(item.userName.length - 1) }}
              </view>
              <view class="user-details">
                <text class="user-name">{{ item.userName }}</text>
                <text class="user-dept">{{ item.userDept }}</text>
              </view>
            </view>
            <view class="feedback-rating">
              <text class="rating-stars">{{ getRatingStars(item.rating) }}</text>
              <text class="rating-score">{{ item.rating }}.0</text>
            </view>
          </view>

          <view class="feedback-content">
            <text class="feedback-text">{{ item.content }}</text>
          </view>

          <view class="feedback-tags" v-if="item.tags && item.tags.length > 0">
            <text
              v-for="(tag, tagIndex) in item.tags"
              :key="tagIndex"
              class="tag-item"
            >
              {{ tag }}
            </text>
          </view>

          <view class="feedback-footer">
            <text class="feedback-time">{{ item.submitTime }}</text>
            <view class="feedback-actions">
              <text class="action-item" @click="replyFeedback(item)">回复</text>
              <text class="action-item" @click="deleteFeedback(item)">删除</text>
            </view>
          </view>

          <!-- 回复内容 -->
          <view class="reply-section" v-if="item.reply">
            <view class="reply-header">
              <text class="reply-label">管理员回复：</text>
              <text class="reply-time">{{ item.replyTime }}</text>
            </view>
            <text class="reply-text">{{ item.reply }}</text>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && filteredFeedback.length > 0" class="no-more-text">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedTraining: '2024年度第四期干部综合素质提升培训',
      activeCategory: 'all',
      loading: false,
      hasMore: true,
      currentPage: 1,
      trainings: [
        '2024年度第四期干部综合素质提升培训',
        '2024年度第三期干部综合素质提升培训',
        '2024年度第二期干部综合素质提升培训'
      ],
      categoryTabs: [
        { label: '全部', value: 'all', count: 68 },
        { label: '好评', value: 'good', count: 56 },
        { label: '中评', value: 'neutral', count: 10 },
        { label: '差评', value: 'bad', count: 2 }
      ],
      stats: {
        total: 68,
        rate: 85,
        satisfaction: 96
      },
      feedbackList: [
        {
          id: 1,
          userName: '张伟',
          userDept: '市教育局',
          rating: 5,
          content: '非常感谢组织这次培训，讲师水平高，内容实用，收获很大！希望以后多举办这样的培训。',
          tags: ['内容实用', '讲师优秀', '组织周到'],
          submitTime: '2024-12-12 18:30',
          reply: '感谢您的反馈和建议！',
          replyTime: '2024-12-13 09:00'
        },
        {
          id: 2,
          userName: '李娜',
          userDept: '市卫健委',
          rating: 5,
          content: '培训内容丰富，形式多样，互动环节设计巧妙。建议今后可以增加更多实践案例。',
          tags: ['内容丰富', '互动良好'],
          submitTime: '2024-12-12 19:00',
          reply: null
        },
        {
          id: 3,
          userName: '王强',
          userDept: '市委组织部',
          rating: 4,
          content: '整体效果不错，但培训时间稍微有点紧张，有些内容讲得太快了。',
          tags: ['时间紧张'],
          submitTime: '2024-12-12 20:15',
          reply: '已收到您的建议，会在下次培训中合理安排时间。'
        }
      ]
    }
  },

  computed: {
    filteredFeedback() {
      if (this.activeCategory === 'all') {
        return this.feedbackList
      } else if (this.activeCategory === 'good') {
        return this.feedbackList.filter(f => f.rating >= 4)
      } else if (this.activeCategory === 'neutral') {
        return this.feedbackList.filter(f => f.rating === 3)
      } else {
        return this.feedbackList.filter(f => f.rating <= 2)
      }
    }
  },

  methods: {
    onTrainingChange(e) {
      this.selectedTraining = this.trainings[e.detail.value]
      this.loadFeedback()
    },

    switchCategory(category) {
      this.activeCategory = category
    },

    getRatingStars(rating) {
      return '⭐'.repeat(rating) + '☆'.repeat(5 - rating)
    },

    replyFeedback(item) {
      uni.showModal({
        title: '回复反馈',
        editable: true,
        placeholderText: '请输入回复内容',
        success: (res) => {
          if (res.confirm && res.content) {
            item.reply = res.content
            item.replyTime = this.getCurrentTime()
            uni.showToast({
              title: '回复成功',
              icon: 'success'
            })
          }
        }
      })
    },

    deleteFeedback(item) {
      uni.showModal({
        title: '删除反馈',
        content: '确认删除此反馈？',
        confirmColor: '#ef4444',
        success: (res) => {
          if (res.confirm) {
            const index = this.feedbackList.findIndex(f => f.id === item.id)
            if (index > -1) {
              this.feedbackList.splice(index, 1)
              uni.showToast({
                title: '删除成功',
                icon: 'success'
              })
            }
          }
        }
      })
    },

    exportData() {
      uni.showLoading({ title: '导出中...' })

      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({
          title: '导出功能开发中',
          icon: 'none'
        })
      }, 1000)
    },

    loadFeedback() {
      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    loadMore() {
      if (this.loading || !this.hasMore) return

      this.loading = true
      setTimeout(() => {
        this.currentPage++
        this.loading = false

        if (this.currentPage >= 3) {
          this.hasMore = false
        }
      }, 1000)
    },

    getCurrentTime() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    },

    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.feedback-container {
  min-height: 100vh;
  background: $bg-secondary;
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  border-bottom: 1rpx solid $border-color;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 $spacing-md;
}

.back-btn {
  font-size: 48rpx;
  color: $text-primary;
  font-weight: 300;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
  text-align: center;
}

.header-action {
  font-size: $font-size-md;
  color: $primary-color;
}

.training-selector {
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
}

.selector-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 88rpx;
  padding: 0 $spacing-md;
}

.selector-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.selector-value {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.selector-value text {
  font-size: $font-size-md;
  color: $primary-color;
}

.selector-arrow {
  font-size: $font-size-xs;
}

.stats-overview {
  margin: $spacing-md;
}

.overview-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.stat-value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.category-tabs {
  background: $bg-primary;
  padding: $spacing-sm 0;
  margin-bottom: $spacing-sm;
}

.tabs-scroll {
  width: 100%;
  white-space: nowrap;
}

.tabs-list {
  display: inline-flex;
  padding: 0 $spacing-md;
  gap: $spacing-md;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  transition: all 0.3s;
}

.tab-item.active {
  background: rgba(102, 126, 234, 0.1);
}

.tab-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.tab-item.active .tab-label {
  color: $primary-color;
  font-weight: 500;
}

.tab-count {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.feedback-scroll {
  height: calc(100vh - 360rpx);
  padding: 0 $spacing-md;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  padding: $spacing-md 0;
}

.feedback-card {
  margin-bottom: 0;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-md;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.user-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.user-dept {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.feedback-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.rating-stars {
  font-size: $font-size-sm;
}

.rating-score {
  font-size: $font-size-lg;
  font-weight: 600;
  color: #f59e0b;
}

.feedback-content {
  margin-bottom: $spacing-md;
}

.feedback-text {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
}

.feedback-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
  margin-bottom: $spacing-md;
}

.tag-item {
  padding: 4rpx 12rpx;
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
  font-size: $font-size-sm;
  border-radius: $border-radius-sm;
}

.feedback-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color;
}

.feedback-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.feedback-actions {
  display: flex;
  gap: $spacing-lg;
}

.action-item {
  font-size: $font-size-sm;
  color: $primary-color;
}

.reply-section {
  margin-top: $spacing-md;
  padding: $spacing-md;
  background: rgba(16, 185, 129, 0.05);
  border-radius: $border-radius-md;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xs;
}

.reply-label {
  font-size: $font-size-sm;
  font-weight: 500;
  color: #10b981;
}

.reply-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.reply-text {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-lg;
  color: $text-tertiary;
  font-size: $font-size-sm;
}
</style>
