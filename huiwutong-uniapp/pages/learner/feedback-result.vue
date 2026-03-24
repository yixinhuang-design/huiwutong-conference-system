<template>
  <view class="feedback-result-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">问卷结果</text>
        <text class="header-action" @click="exportData">导出</text>
      </view>
    </view>

    <!-- 问卷信息 -->
    <view class="questionnaire-info card">
      <view class="info-header">
        <text class="info-title">{{ questionnaireInfo.title }}</text>
        <view class="info-status">
          <text class="status-text">{{ questionnaireInfo.status }}</text>
        </view>
      </view>
      <view class="info-meta">
        <text class="meta-item">📊 已填写：{{ questionnaireInfo.completed }}/{{ questionnaireInfo.total }}</text>
        <text class="meta-item"><text class="fa fa-clock"></text> 截止：{{ questionnaireInfo.deadline }}</text>
      </view>
    </view>

    <!-- 统计概览 -->
    <view class="stats-section card">
      <view class="section-title">统计概览</view>
      <view class="stats-grid">
        <view class="stat-box">
          <text class="stat-value">{{ stats.responseRate }}%</text>
          <text class="stat-label">响应率</text>
        </view>
        <view class="stat-box">
          <text class="stat-value">{{ stats.completionRate }}%</text>
          <text class="stat-label">完成率</text>
        </view>
        <view class="stat-box">
          <text class="stat-value">{{ stats.avgScore }}</text>
          <text class="stat-label">平均分</text>
        </view>
      </view>
    </view>

    <scroll-view class="results-scroll" scroll-y>
      <!-- 题目统计列表 -->
      <view class="questions-section">
        <view class="section-title">题目统计</view>

        <view
          v-for="(question, index) in questionResults"
          :key="question.id"
          class="question-result card"
        >
          <view class="question-header">
            <text class="question-number">Q{{ index + 1 }}</text>
            <text class="question-title">{{ question.title }}</text>
            <text class="question-type">{{ getTypeLabel(question.type) }}</text>
          </view>

          <!-- 单选题统计 -->
          <view v-if="question.type === 'single'" class="option-stats">
            <view
              v-for="(option, optIndex) in question.options"
              :key="optIndex"
              class="option-stat-item"
            >
              <view class="option-info">
                <text class="option-label">{{ option.label }}</text>
                <view class="option-bar">
                  <view
                    class="option-fill"
                    :style="{ width: option.percent + '%' }"
                  ></view>
                </view>
              </view>
              <view class="option-data">
                <text class="option-count">{{ option.count }}人</text>
                <text class="option-percent">{{ option.percent }}%</text>
              </view>
            </view>
          </view>

          <!-- 多选题统计 -->
          <view v-if="question.type === 'multiple'" class="option-stats">
            <view
              v-for="(option, optIndex) in question.options"
              :key="optIndex"
              class="option-stat-item"
            >
              <view class="option-info">
                <text class="option-label">{{ option.label }}</text>
                <view class="option-bar">
                  <view
                    class="option-fill"
                    :style="{ width: option.percent + '%' }"
                  ></view>
                </view>
              </view>
              <view class="option-data">
                <text class="option-count">{{ option.count }}人</text>
                <text class="option-percent">{{ option.percent }}%</text>
              </view>
            </view>
          </view>

          <!-- 评分题统计 -->
          <view v-if="question.type === 'rating'" class="rating-stats">
            <view class="rating-distribution">
              <view
                v-for="star in 5"
                :key="star"
                class="rating-bar-item"
              >
                <text class="star-label">{{ star }}星</text>
                <view class="star-bar">
                  <view
                    class="star-fill"
                    :style="{ width: question.ratingDistribution[star - 1] + '%' }"
                  ></view>
                </view>
                <text class="star-count">{{ question.ratingCounts[star - 1] }}人</text>
              </view>
            </view>
            <view class="rating-summary">
              <text class="summary-label">平均分：</text>
              <text class="summary-value">{{ question.avgRating }}</text>
            </view>
          </view>

          <!-- 文本题统计 -->
          <view v-if="question.type === 'text'" class="text-stats">
            <view class="text-summary">
              <text class="summary-label">有效回答：</text>
              <text class="summary-value">{{ question.validCount }}/{{ question.totalCount }}</text>
            </view>
            <view class="text-preview">
              <text class="preview-label">回答示例：</text>
              <text class="preview-text">{{ question.sampleAnswer }}</text>
            </view>
            <view class="view-all-btn" @click="viewAllTextAnswers(question)">
              <text>查看全部回答 →</text>
            </view>
          </view>

          <view class="question-actions">
            <button
              class="btn btn-sm btn-outline"
              @click="viewQuestionDetail(question)"
            >
              查看详情
            </button>
          </view>
        </view>
      </view>

      <!-- 查看全部文本回答弹窗 -->
      <view v-if="showTextModal" class="modal-overlay" @click="hideTextModal">
        <view class="text-modal" @click.stop>
          <view class="modal-header">
            <text class="modal-title">全部回答</text>
            <text class="modal-close" @click="hideTextModal">✕</text>
          </view>

          <scroll-view class="modal-body" scroll-y>
            <view
              v-for="(answer, index) in textAnswers"
              :key="index"
              class="text-answer-item"
            >
              <view class="answer-header">
                <text class="answer-user">{{ answer.userName }}</text>
                <text class="answer-time">{{ answer.submitTime }}</text>
              </view>
              <text class="answer-content">{{ answer.content }}</text>
            </view>
          </scroll-view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      questionnaireId: '',
      showTextModal: false,
      textAnswers: [],
      questionnaireInfo: {
        title: '2025年度第一期培训满意度调查',
        status: '进行中',
        completed: 56,
        total: 80,
        deadline: '2025-01-20 18:00'
      },
      stats: {
        responseRate: 70,
        completionRate: 85,
        avgScore: 4.6
      },
      questionResults: [
        {
          id: 1,
          type: 'rating',
          title: '您对本次培训的整体满意度',
          ratingDistribution: [5, 12, 18, 15, 6],
          ratingCounts: [5, 12, 18, 15, 6],
          avgRating: 4.6
        },
        {
          id: 2,
          type: 'single',
          title: '您认为本次培训内容的实用性如何',
          options: [
            { label: '非常实用', count: 35, percent: 62.5 },
            { label: '比较实用', count: 15, percent: 26.8 },
            { label: '一般', count: 5, percent: 8.9 },
            { label: '不太实用', count: 1, percent: 1.8 },
            { label: '完全不实用', count: 0, percent: 0 }
          ]
        },
        {
          id: 3,
          type: 'multiple',
          title: '您认为本次培训的哪些方面做得好（可多选）',
          options: [
            { label: '培训内容安排合理', count: 48, percent: 85.7 },
            { label: '讲师专业水平高', count: 45, percent: 80.4 },
            { label: '培训环境设施好', count: 38, percent: 67.9 },
            { label: '组织服务周到', count: 42, percent: 75.0 },
            { label: '学习资料充足', count: 35, percent: 62.5 },
            { label: '时间安排合理', count: 30, percent: 53.6 }
          ]
        },
        {
          id: 6,
          type: 'text',
          title: '您对本次培训的建议或意见',
          validCount: 42,
          totalCount: 56,
          sampleAnswer: '建议增加更多实践案例的分享环节，希望能有更多互动交流的机会。'
        }
      ]
    }
  },

  onLoad(options) {
    if (options.id) {
      this.questionnaireId = options.id
      this.loadResults()
    }
  },

  methods: {
    getTypeLabel(type) {
      const labelMap = {
        single: '单选题',
        multiple: '多选题',
        rating: '评分题',
        text: '文本题'
      }
      return labelMap[type] || '其他'
    },

    viewQuestionDetail(question) {
      uni.showModal({
        title: '题目详情',
        content: `类型：${this.getTypeLabel(question.type)}\n回答数：${question.totalCount || 56}人`,
        showCancel: false
      })
    },

    viewAllTextAnswers(question) {
      // 模拟文本回答数据
      this.textAnswers = [
        { userName: '张伟', submitTime: '2025-01-16 10:30', content: '建议增加更多实践案例' },
        { userName: '李娜', submitTime: '2025-01-16 11:00', content: '培训很好，没有建议' },
        { userName: '王强', submitTime: '2025-01-16 11:30', content: '希望下次培训时间能更长一些' }
      ]
      this.showTextModal = true
    },

    hideTextModal() {
      this.showTextModal = false
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

    loadResults() {
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

.feedback-result-container {
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

.questionnaire-info {
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

.info-status {
  padding: 4rpx 12rpx;
  background: rgba(16, 185, 129, 0.1);
  border-radius: $border-radius-sm;
}

.status-text {
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

.stats-section {
  margin: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
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

.results-scroll {
  height: calc(100vh - 360rpx);
  padding: 0 $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.questions-section {
  margin-bottom: $spacing-lg;
}

.question-result {
  margin-bottom: $spacing-md;
}

.question-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.question-number {
  padding: 4rpx 12rpx;
  background: $primary-color;
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: 600;
  border-radius: $border-radius-sm;
}

.question-title {
  flex: 1;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.question-type {
  padding: 4rpx 12rpx;
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.option-stats {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.option-stat-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.option-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.option-label {
  width: 200rpx;
  font-size: $font-size-md;
  color: $text-primary;
}

.option-bar {
  flex: 1;
  height: 16rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 8rpx;
  overflow: hidden;
}

.option-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.option-data {
  display: flex;
  gap: $spacing-lg;
  padding-left: 200rpx;
}

.option-count {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.option-percent {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $primary-color;
}

.rating-stats {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.rating-distribution {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.rating-bar-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.star-label {
  width: 100rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.star-bar {
  flex: 1;
  height: 16rpx;
  background: rgba(245, 158, 11, 0.2);
  border-radius: 8rpx;
  overflow: hidden;
}

.star-fill {
  height: 100%;
  background: #f59e0b;
  transition: width 0.3s;
}

.star-count {
  width: 80rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
  text-align: right;
}

.rating-summary {
  display: flex;
  justify-content: center;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.summary-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.summary-value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: #f59e0b;
}

.text-stats {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.text-summary {
  display: flex;
  justify-content: space-between;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.summary-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.summary-value {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
}

.text-preview {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.preview-label {
  display: block;
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-xs;
}

.preview-text {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: 1.6;
}

.view-all-btn {
  padding: $spacing-md;
  text-align: center;
  color: $primary-color;
  font-size: $font-size-md;
}

.question-actions {
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color;
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg;
}

.text-modal {
  width: 100%;
  max-width: 600rpx;
  max-height: 80vh;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.modal-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.modal-close {
  font-size: $font-size-xl;
  color: $text-secondary;
  padding: $spacing-sm;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
}

.text-answer-item {
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xs;
}

.answer-user {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.answer-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.answer-content {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
}
</style>
