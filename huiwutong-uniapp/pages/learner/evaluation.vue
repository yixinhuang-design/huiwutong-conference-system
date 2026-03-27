<template>
  <view class="evaluation-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">评价打分</text>
        <text class="header-action"></text>
      </view>
    </view>

    <scroll-view class="evaluation-scroll" scroll-y>
      <!-- 培训信息 -->
      <view class="training-info card">
        <text class="training-title">{{ trainingInfo.title }}</text>
        <text class="training-date">{{ trainingInfo.date }}</text>
        <text class="training-desc">{{ trainingInfo.description }}</text>
      </view>

      <!-- 评价进度 -->
      <view class="progress-card">
        <view class="progress-header">
          <text class="progress-title">评价进度</text>
          <text class="progress-percent">{{ completedCount }}/{{ totalCount }}</text>
        </view>
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
        </view>
      </view>

      <!-- 评价项列表 -->
      <view class="evaluation-list">
        <view
          v-for="(category, index) in evaluationCategories"
          :key="category.id"
          class="evaluation-category"
        >
          <view class="category-header">
            <view class="category-icon">{{ category.icon }}</view>
            <view class="category-info">
              <text class="category-title">{{ category.title }}</text>
              <text class="category-desc">{{ category.description }}</text>
            </view>
          </view>

          <view class="evaluation-items">
            <view
              v-for="item in category.items"
              :key="item.id"
              class="evaluation-item card"
            >
              <text class="item-label">{{ item.label }}</text>

              <view class="rating-scale">
                <view
                  v-for="score in [1, 2, 3, 4, 5]"
                  :key="score"
                  class="score-option"
                  :class="{ selected: evaluations[item.id] === score }"
                  @click="selectScore(item.id, score)"
                >
                  <text class="score-number">{{ score }}</text>
                  <text class="score-label">{{ getScoreLabel(score) }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 综合评价 -->
      <view class="overall-section card">
        <view class="section-title">综合评价</view>
        <view class="overall-rating">
          <text class="overall-label">总体评分</text>
          <view class="overall-stars">
            <text
              v-for="star in 5"
              :key="star"
              class="star-icon"
              @click="selectOverallRating(star)"
            >
              {{ star <= overallRating ? '⭐' : '☆' }}
            </text>
          </view>
          <text class="overall-score">{{ overallRating }}.0</text>
        </view>

        <view class="comment-section">
          <text class="comment-label">评价建议</text>
          <textarea
            v-model="comment"
            class="comment-input"
            placeholder="请输入您的评价建议（选填）"
            :maxlength="500"
          ></textarea>
          <view class="comment-count">
            <text>{{ comment.length }}/500</text>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button class="btn btn-primary btn-block" @click="submitEvaluation">
          ✓ 提交评价
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      trainingId: '',
      evaluations: {},
      overallRating: 5,
      comment: '',
      trainingInfo: {
        title: '2025年度第一期干部综合素质提升培训',
        date: '2025年1月18日-20日',
        description: '为提升干部综合素质，增强履职能力，特举办本次培训'
      },
      evaluationCategories: [
        {
          id: 1,
          icon: '📚',
          title: '培训内容',
          description: '对培训课程内容设置的评价',
          items: [
            { id: 'content_1', label: '内容的针对性' },
            { id: 'content_2', label: '内容的实用性' },
            { id: 'content_3', label: '内容的系统性' },
            { id: 'content_4', label: '内容的创新性' }
          ]
        },
        {
          id: 2,
          icon: '👨‍🏫',
          title: '讲师水平',
          description: '对授课讲师专业能力的评价',
          items: [
            { id: 'teacher_1', label: '专业知识水平' },
            { id: 'teacher_2', label: '授课表达能力' },
            { id: 'teacher_3', label: '互动交流效果' },
            { id: 'teacher_4', label: '案例教学能力' }
          ]
        },
        {
          id: 3,
          icon: '🏛️',
          title: '培训组织',
          description: '对培训组织安排的评价',
          items: [
            { id: 'organization_1', label: '培训时间安排' },
            { id: 'organization_2', label: '培训环境设施' },
            { id: 'organization_3', label: '学习资料质量' },
            { id: 'organization_4', label: '后勤服务保障' }
          ]
        },
        {
          id: 4,
          icon: '📈',
          title: '培训效果',
          description: '对个人收获提升的评价',
          items: [
            { id: 'effect_1', label: '知识增长' },
            { id: 'effect_2', label: '能力提升' },
            { id: 'effect_3', label: '视野拓展' },
            { id: 'effect_4', label: '工作启发' }
          ]
        }
      ]
    }
  },

  computed: {
    totalCount() {
      let count = 0
      this.evaluationCategories.forEach(category => {
        count += category.items.length
      })
      return count
    },

    completedCount() {
      return Object.keys(this.evaluations).length
    },

    progressPercent() {
      if (this.totalCount === 0) return 0
      return Math.round((this.completedCount / this.totalCount) * 100)
    }
  },

  onLoad(options) {
    if (options.id) {
      this.trainingId = options.id
      this.loadEvaluation()
    }
  },

  methods: {
    loadEvaluation() {
      uni.showLoading({ title: '加载中...' })
      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    selectScore(itemId, score) {
      this.$set(this.evaluations, itemId, score)
    },

    selectOverallRating(rating) {
      this.overallRating = rating
    },

    getScoreLabel(score) {
      const labels = {
        1: '很差',
        2: '较差',
        3: '一般',
        4: '满意',
        5: '非常满意'
      }
      return labels[score] || ''
    },

    validateEvaluation() {
      const requiredItems = []

      this.evaluationCategories.forEach(category => {
        category.items.forEach(item => {
          if (!this.evaluations[item.id]) {
            requiredItems.push(item.label)
          }
        })
      })

      if (requiredItems.length > 0) {
        uni.showToast({
          title: '请完成所有评价项',
          icon: 'none'
        })
        return false
      }

      return true
    },

    submitEvaluation() {
      if (!this.validateEvaluation()) {
        return
      }

      const data = {
        trainingId: this.trainingId,
        evaluations: this.evaluations,
        overallRating: this.overallRating,
        comment: this.comment
      }

      uni.showModal({
        title: '提交评价',
        content: '确认提交您的评价？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '提交中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '提交成功',
                icon: 'success',
                duration: 2000
              })

              setTimeout(() => {
                uni.navigateBack()
              }, 2000)
            }, 1500)
          }
        }
      })
    },

    goBack() {
      if (this.completedCount > 0) {
        uni.showModal({
          title: '提示',
          content: '当前有未提交的评价，确认离开？',
          success: (res) => {
            if (res.confirm) {
              uni.navigateBack()
            }
          }
        })
      } else {
        uni.navigateBack()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.evaluation-container {
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

.evaluation-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.training-info {
  margin-bottom: $spacing-md;
}

.training-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.training-date {
  display: block;
  font-size: $font-size-md;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
}

.training-desc {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
  line-height: 1.6;
}

.progress-card {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: $border-radius-md;
  margin-bottom: $spacing-lg;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-title {
  font-size: $font-size-md;
  font-weight: 600;
  color: $primary-color;
}

.progress-percent {
  font-size: $font-size-md;
  font-weight: 600;
  color: $primary-color;
}

.progress-card .progress-bar {
  height: 8rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-card .progress-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.evaluation-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.category-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
}

.category-icon {
  font-size: 48rpx;
}

.category-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.category-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.category-desc {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.evaluation-items {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.evaluation-item {
  margin-bottom: 0;
}

.item-label {
  display: block;
  font-size: $font-size-md;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.rating-scale {
  display: flex;
  justify-content: space-between;
  gap: $spacing-xs;
}

.score-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.score-option.selected {
  background: rgba(102, 126, 234, 0.1);
  border-color: $primary-color;
}

.score-number {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.score-label {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.overall-section {
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.overall-rating {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-lg 0;
  margin-bottom: $spacing-lg;
  border-bottom: 1rpx solid $border-color;
}

.overall-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.overall-stars {
  display: flex;
  gap: $spacing-xs;
}

.star-icon {
  font-size: 48rpx;
  color: $border-color;
}

.star-icon:nth-child(-n+1) {
  color: #f59e0b;
}

.overall-score {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.comment-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.comment-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.comment-input {
  width: 100%;
  min-height: 200rpx;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  line-height: 1.6;
}

.comment-count {
  text-align: right;
}

.comment-count text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.submit-section {
  margin-bottom: $spacing-lg;
}
</style>
