<template>
  <view class="evaluation-result-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">评价结果</text>
        <text class="header-action" @click="exportData">导出</text>
      </view>
    </view>

    <!-- 评价信息 -->
    <view class="evaluation-info card">
      <view class="info-header">
        <text class="info-title">{{ evaluationInfo.title }}</text>
        <view class="completion-badge">
          <text class="badge-text">完成率 {{ evaluationInfo.completionRate }}%</text>
        </view>
      </view>
      <view class="info-meta">
        <text class="meta-item"><text class="fa fa-chart-bar"></text> 已评价：{{ evaluationInfo.completed }}/{{ evaluationInfo.total }}</text>
        <text class="meta-item"><text class="fa fa-clock"></text> 截止：{{ evaluationInfo.deadline }}</text>
      </view>
    </view>

    <!-- 总体评分 -->
    <view class="overall-rating card">
      <view class="section-title">总体评分</view>
      <view class="rating-display">
        <view class="rating-circle">
          <text class="rating-score">{{ overallRating.avg }}</text>
          <text class="rating-max">/5.0</text>
        </view>
        <view class="rating-stars">
          <text
            v-for="star in 5"
            :key="star"
            class="star-icon"
            :class="star <= overallRating.avg ? 'filled' : ''"
          >
            <text v-if="star <= overallRating.avg" class="fa fa-star"></text>
            <text v-else class="fa fa-star-o"></text>
          </text>
        </view>
      </view>
      <view class="rating-distribution">
        <view
          v-for="(item, index) in ratingDistribution"
          :key="index"
          class="dist-item"
        >
          <text class="dist-label">{{ 5 - index }}星</text>
          <view class="dist-bar">
            <view class="dist-fill" :style="{ width: item.percent + '%' }"></view>
          </view>
          <text class="dist-count">{{ item.count }}人</text>
        </view>
      </view>
    </view>

    <scroll-view class="results-scroll" scroll-y>
      <!-- 分类评价统计 -->
      <view class="category-section card">
        <view class="section-title">分类评价统计</view>
        <view class="category-list">
          <view
            v-for="category in categoryResults"
            :key="category.id"
            class="category-item"
          >
            <view class="category-header">
              <text class="category-icon"><text :class="category.icon"></text></text>
              <text class="category-name">{{ category.name }}</text>
            </view>
            <view class="category-rating">
              <view class="rating-stars-small">
                <text
                  v-for="star in 5"
                  :key="star"
                  class="star-icon-small"
                  :class="star <= category.avg ? 'filled' : ''"
                >
                  <text v-if="star <= category.avg" class="fa fa-star"></text>
                  <text v-else class="fa fa-star-o"></text>
                </text>
              </view>
              <text class="category-score">{{ category.avg }}</text>
            </view>
            <view class="category-details">
              <text class="detail-text">满分：{{ category.fullCount }}人</text>
              <text class="detail-text">平均分：{{ category.avg }}分</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 评价统计 -->
      <view class="stats-section card">
        <view class="section-title">评价统计</view>
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-label">评价人数</text>
            <text class="stat-value">{{ evaluationStats.totalCount }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-label">满分人数</text>
            <text class="stat-value">{{ evaluationStats.fullCount }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-label">满分率</text>
            <text class="stat-value">{{ evaluationStats.fullRate }}%</text>
          </view>
          <view class="stat-item">
            <text class="stat-label">好评率</text>
            <text class="stat-value highlight">{{ evaluationStats.goodRate }}%</text>
          </view>
        </view>
      </view>

      <!-- 评价详情列表 -->
      <view class="reviews-section card">
        <view class="section-title">评价详情</view>
        <view class="review-filters">
          <view
            v-for="filter in ratingFilters"
            :key="filter.value"
            class="filter-chip"
            :class="{ active: activeFilter === filter.value }"
            @click="switchFilter(filter.value)"
          >
            {{ filter.label }}
          </view>
        </view>

        <view class="reviews-list">
          <view
            v-for="review in filteredReviews"
            :key="review.id"
            class="review-item"
          >
            <view class="review-header">
              <view class="reviewer-info">
                <view class="reviewer-avatar">
                  {{ review.userName.charAt(review.userName.length - 1) }}
                </view>
                <view class="reviewer-details">
                  <text class="reviewer-name">{{ review.userName }}</text>
                  <text class="reviewer-dept">{{ review.userDept }}</text>
                </view>
              </view>
              <view class="review-rating">
                <view class="rating-stars-mini">
                  <text
                    v-for="star in 5"
                    :key="star"
                    class="star-icon-mini"
                    :class="star <= review.rating ? 'filled' : ''"
                  >
                    <text v-if="star <= review.rating" class="fa fa-star"></text>
                    <text v-else class="fa fa-star-o"></text>
                  </text>
                </view>
                <text class="rating-score">{{ review.rating }}.0</text>
              </view>
            </view>

            <view class="review-category-scores">
              <view
                v-for="(score, key) in review.categoryScores"
                :key="key"
                class="category-score"
              >
                <text class="score-label">{{ getCategoryLabel(key) }}：</text>
                <text class="score-value">{{ score }}分</text>
              </view>
            </view>

            <view class="review-comment" v-if="review.comment">
              <text class="comment-text">{{ review.comment }}</text>
            </view>

            <text class="review-time">{{ review.submitTime }}</text>
          </view>
        </view>

        <view v-if="hasMore" class="load-more-btn" @click="loadMore">
          <text>加载更多</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      evaluationId: '',
      activeFilter: 'all',
      hasMore: true,
      currentPage: 1,
      evaluationInfo: {
        title: '2025年度第一期干部综合素质提升培训',
        completionRate: 70,
        completed: 56,
        total: 80,
        deadline: '2025-01-20 18:00'
      },
      overallRating: {
        avg: 4.68
      },
      ratingDistribution: [
        { star: 5, count: 42, percent: 75 },
        { star: 4, count: 10, percent: 17.9 },
        { star: 3, count: 3, percent: 5.4 },
        { star: 2, count: 1, percent: 1.8 },
        { star: 1, count: 0, percent: 0 }
      ],
      categoryResults: [
        { id: 'content', name: '培训内容', icon: 'fa-book-open', avg: 4.72, fullCount: 48 },
        { id: 'teacher', name: '讲师水平', icon: 'fa-chalkboard-teacher', avg: 4.85, fullCount: 52 },
        { id: 'organization', name: '培训组织', icon: 'fa-university', avg: 4.65, fullCount: 45 },
        { id: 'effect', name: '培训效果', icon: 'fa-chart-line', avg: 4.50, fullCount: 38 }
      ],
      evaluationStats: {
        totalCount: 56,
        fullCount: 42,
        fullRate: 75,
        goodRate: 98.2
      },
      ratingFilters: [
        { label: '全部', value: 'all' },
        { label: '5星', value: 5 },
        { label: '4星', value: 4 },
        { label: '3星及以下', value: 'low' }
      ],
      reviews: [
        {
          id: 1,
          userName: '张伟',
          userDept: '市教育局',
          rating: 5,
          categoryScores: { content: 5, teacher: 5, organization: 5, effect: 5 },
          comment: '非常感谢组织和各位老师的辛勤付出，这次培训让我收获满满！',
          submitTime: '2025-01-16 18:30'
        },
        {
          id: 2,
          userName: '李娜',
          userDept: '市卫健委',
          rating: 5,
          categoryScores: { content: 5, teacher: 5, organization: 4, effect: 5 },
          comment: '培训内容丰富实用，讲师经验丰富，互动环节设计巧妙。',
          submitTime: '2025-01-16 19:00'
        },
        {
          id: 3,
          userName: '王强',
          userDept: '市委组织部',
          rating: 4,
          categoryScores: { content: 4, teacher: 5, organization: 4, effect: 4 },
          comment: '整体效果不错，但培训时间稍微有点紧张。',
          submitTime: '2025-01-16 20:15'
        }
      ]
    }
  },

  computed: {
    filteredReviews() {
      if (this.activeFilter === 'all') {
        return this.reviews
      } else if (this.activeFilter === 'low') {
        return this.reviews.filter(r => r.rating <= 3)
      } else {
        return this.reviews.filter(r => r.rating === this.activeFilter)
      }
    }
  },

  onLoad(options) {
    if (options.id) {
      this.evaluationId = options.id
      this.loadEvaluation()
    }
  },

  methods: {
    getRatingStars(rating) {
      return rating // 返回数字，模板中用v-if渲染
    },

    getCategoryLabel(key) {
      const labelMap = {
        content: '培训内容',
        teacher: '讲师水平',
        organization: '培训组织',
        effect: '培训效果'
      }
      return labelMap[key] || key
    },

    switchFilter(filter) {
      this.activeFilter = filter
    },

    loadMore() {
      if (!this.hasMore) return

      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hasMore = false
      }, 1000)
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

    loadEvaluation() {
      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
      }, 500)
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
@import '../../styles/global-patch.scss';

.evaluation-result-container {
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

.evaluation-info {
  margin: $spacing-md;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-sm;
}

.info-title {
  flex: 1;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-right: $spacing-md;
}

.completion-badge {
  padding: 4rpx 12rpx;
  background: rgba(16, 185, 129, 0.1);
  border-radius: $border-radius-sm;
}

.badge-text {
  font-size: $font-size-sm;
  color: #10b981;
  font-weight: 500;
}

.info-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-lg;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.overall-rating {
  margin: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.rating-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-lg;
  padding: $spacing-xl 0;
}

.rating-circle {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.rating-score {
  font-size: 64rpx;
  font-weight: 600;
  color: #f59e0b;
}

.rating-max {
  font-size: $font-size-xl;
  color: $text-tertiary;
}

.rating-stars {
  display: flex;
  gap: $spacing-xs;
}

.star-icon {
  font-size: 48rpx;
}

.star-icon.filled {
  color: #f59e0b;
}

.rating-distribution {
  width: 100%;
  padding: $spacing-md 0;
}

.dist-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-sm;
}

.dist-label {
  width: 80rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.dist-bar {
  flex: 1;
  height: 12rpx;
  background: rgba(245, 158, 11, 0.2);
  border-radius: 6rpx;
  overflow: hidden;
}

.dist-fill {
  height: 100%;
  background: #f59e0b;
  transition: width 0.3s;
}

.dist-count {
  width: 60rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
  text-align: right;
}

.results-scroll {
  height: calc(100vh - 340rpx);
  padding: 0 $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.category-section,
.stats-section,
.reviews-section {
  margin-bottom: $spacing-md;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.category-item {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.category-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.category-icon {
  font-size: 32rpx;
}

.category-name {
  flex: 1;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.category-rating {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.rating-stars-small {
  display: flex;
  gap: 2rpx;
}

.star-icon-small {
  font-size: 24rpx;
}

.star-icon-small.filled {
  color: #f59e0b;
}

.category-score {
  font-size: $font-size-lg;
  font-weight: 600;
  color: #f59e0b;
}

.category-details {
  display: flex;
  gap: $spacing-lg;
}

.detail-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.stat-value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.stat-value.highlight {
  color: #10b981;
}

.review-filters {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
  flex-wrap: wrap;
}

.filter-chip {
  padding: $spacing-xs $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.filter-chip.active {
  background: $primary-color;
  color: $text-white;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.review-item {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.reviewer-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reviewer-details {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.reviewer-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.reviewer-dept {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.review-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.rating-stars-mini {
  font-size: 20rpx;
}

.rating-score {
  font-size: $font-size-md;
  font-weight: 600;
  color: #f59e0b;
}

.review-category-scores {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  margin-bottom: $spacing-sm;
}

.category-score {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.score-value {
  font-weight: 600;
  color: $primary-color;
}

.review-comment {
  margin-bottom: $spacing-sm;
}

.comment-text {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: 1.6;
}

.review-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.load-more-btn {
  padding: $spacing-md;
  text-align: center;
  color: $primary-color;
  font-size: $font-size-md;
}
</style>
