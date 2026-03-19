<template>
  <view class="feedback-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">问卷填写</text>
        <text class="header-action"></text>
      </view>
    </view>

    <scroll-view class="feedback-scroll" scroll-y>
      <!-- 问卷标题 -->
      <view class="questionnaire-header card">
        <text class="questionnaire-title">{{ questionnaire.title }}</text>
        <text class="questionnaire-desc">{{ questionnaire.description }}</text>
        <view class="questionnaire-meta">
          <text class="meta-item">⏰ 截止：{{ questionnaire.deadline }}</text>
          <text class="meta-item">📊 已填：{{ questionnaire.completedCount }}/{{ questionnaire.totalCount }}</text>
        </view>
      </view>

      <!-- 进度提示 -->
      <view class="progress-hint">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
        </view>
        <text class="progress-text">完成进度：{{ completedQuestions }}/{{ totalQuestions }}</text>
      </view>

      <!-- 问题列表 -->
      <view class="questions-list">
        <view
          v-for="(question, index) in questionnaire.questions"
          :key="question.id"
          class="question-card card"
          :id="'question-' + question.id"
        >
          <view class="question-header">
            <view class="question-number">Q{{ index + 1 }}</view>
            <view class="question-required" v-if="question.required">*</view>
          </view>

          <text class="question-title">{{ question.title }}</text>
          <text class="question-desc" v-if="question.description">{{ question.description }}</text>

          <!-- 单选题 -->
          <view v-if="question.type === 'single'" class="question-options">
            <view
              v-for="(option, optIndex) in question.options"
              :key="optIndex"
              class="option-item"
              :class="{ selected: answers[question.id] === option.value }"
              @click="selectSingleOption(question.id, option.value)"
            >
              <view class="option-radio">
                <view v-if="answers[question.id] === option.value" class="radio-checked"></view>
              </view>
              <text class="option-text">{{ option.label }}</text>
            </view>
          </view>

          <!-- 多选题 -->
          <view v-if="question.type === 'multiple'" class="question-options">
            <view
              v-for="(option, optIndex) in question.options"
              :key="optIndex"
              class="option-item"
              :class="{ selected: isMultipleSelected(question.id, option.value) }"
              @click="toggleMultipleOption(question.id, option.value)"
            >
              <view class="option-checkbox">
                <text v-if="isMultipleSelected(question.id, option.value)" class="checkbox-checked">✓</text>
              </view>
              <text class="option-text">{{ option.label }}</text>
            </view>
          </view>

          <!-- 评分题 -->
          <view v-if="question.type === 'rating'" class="question-rating">
            <view class="rating-stars">
              <text
                v-for="star in 5"
                :key="star"
                class="star-icon"
                :class="{ active: answers[question.id] >= star }"
                @click="selectRating(question.id, star)"
              >
                {{ star <= (answers[question.id] || 0) ? '⭐' : '☆' }}
              </text>
            </view>
            <text class="rating-text">{{ getRatingText(answers[question.id]) }}</text>
          </view>

          <!-- 文本题 -->
          <view v-if="question.type === 'text'" class="question-text">
            <textarea
              :value="answers[question.id] || ''"
              class="text-input"
              :placeholder="question.placeholder || '请输入您的回答'"
              :maxlength="question.maxLength || 500"
              @input="onTextInput(question.id, $event)"
            ></textarea>
            <view class="text-count">
              <text>{{ (answers[question.id] || '').length }}/{{ question.maxLength || 500 }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部按钮 -->
      <view class="action-buttons">
        <button class="btn btn-outline btn-block" @click="saveDraft">
          💾 保存草稿
        </button>
        <button class="btn btn-primary btn-block" @click="submitFeedback">
          ✓ 提交问卷
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      questionnaireId: '',
      answers: {},
      questionnaire: {
        id: 1,
        title: '2025年度第一期培训满意度调查',
        description: '感谢您参加本次培训！请根据您的实际体验，对以下方面进行评价，您的宝贵意见将帮助我们改进培训质量。',
        deadline: '2025-01-20 18:00',
        completedCount: 56,
        totalCount: 80,
        questions: [
          {
            id: 1,
            type: 'rating',
            title: '您对本次培训的整体满意度',
            required: true
          },
          {
            id: 2,
            type: 'single',
            title: '您认为本次培训内容的实用性如何',
            required: true,
            options: [
              { label: '非常实用', value: '5' },
              { label: '比较实用', value: '4' },
              { label: '一般', value: '3' },
              { label: '不太实用', value: '2' },
              { label: '完全不实用', value: '1' }
            ]
          },
          {
            id: 3,
            type: 'rating',
            title: '您对讲师的授课水平评价',
            required: true
          },
          {
            id: 4,
            type: 'multiple',
            title: '您认为本次培训的哪些方面做得好（可多选）',
            required: false,
            options: [
              { label: '培训内容安排合理', value: 'content' },
              { label: '讲师专业水平高', value: 'teacher' },
              { label: '培训环境设施好', value: 'environment' },
              { label: '组织服务周到', value: 'service' },
              { label: '学习资料充足', value: 'materials' },
              { label: '时间安排合理', value: 'schedule' }
            ]
          },
          {
            id: 5,
            type: 'single',
            title: '您参加培训的主要收获是什么',
            required: true,
            options: [
              { label: '增长了知识', value: 'knowledge' },
              { label: '提升了技能', value: 'skills' },
              { label: '拓宽了视野', value: 'vision' },
              { label: '结识了同行', value: 'network' },
              { label: '以上都有', value: 'all' }
            ]
          },
          {
            id: 6,
            type: 'text',
            title: '您对本次培训的建议或意见',
            description: '请详细描述您的建议，帮助我们改进',
            required: false,
            placeholder: '请输入您的建议或意见...',
            maxLength: 500
          },
          {
            id: 7,
            type: 'text',
            title: '您希望未来培训增加哪些内容',
            required: false,
            placeholder: '请输入您希望增加的培训内容...',
            maxLength: 500
          }
        ]
      }
    }
  },

  computed: {
    totalQuestions() {
      return this.questionnaire.questions.length
    },

    completedQuestions() {
      return this.questionnaire.questions.filter(q => {
        if (q.required) {
          return this.answers[q.id] !== undefined && this.answers[q.id] !== '' && this.answers[q.id] !== null
        }
        return false
      }).length
    },

    progressPercent() {
      if (this.totalQuestions === 0) return 0
      return Math.round((this.completedQuestions / this.totalQuestions) * 100)
    }
  },

  onLoad(options) {
    if (options.id) {
      this.questionnaireId = options.id
      this.loadQuestionnaire()
    }
    this.loadDraft()
  },

  methods: {
    loadQuestionnaire() {
      uni.showLoading({ title: '加载中...' })
      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    loadDraft() {
      const draft = uni.getStorageSync(`feedback_draft_${this.questionnaireId}`)
      if (draft) {
        this.answers = draft
      }
    },

    selectSingleOption(questionId, value) {
      this.$set(this.answers, questionId, value)
    },

    toggleMultipleOption(questionId, value) {
      if (!this.answers[questionId]) {
        this.$set(this.answers, questionId, [])
      }

      const index = this.answers[questionId].indexOf(value)
      if (index > -1) {
        this.answers[questionId].splice(index, 1)
      } else {
        this.answers[questionId].push(value)
      }
    },

    isMultipleSelected(questionId, value) {
      return this.answers[questionId] && this.answers[questionId].includes(value)
    },

    selectRating(questionId, rating) {
      this.$set(this.answers, questionId, rating)
    },

    getRatingText(rating) {
      const texts = ['', '非常不满意', '不满意', '一般', '满意', '非常满意']
      return texts[rating] || ''
    },

    onTextInput(questionId, e) {
      this.$set(this.answers, questionId, e.detail.value)
    },

    validateRequired() {
      const requiredQuestions = this.questionnaire.questions.filter(q => q.required)

      for (const question of requiredQuestions) {
        const answer = this.answers[question.id]

        if (answer === undefined || answer === null || answer === '') {
          uni.showToast({
            title: `请完成第${question.id}题`,
            icon: 'none'
          })

          this.scrollToQuestion(question.id)
          return false
        }

        if (Array.isArray(answer) && answer.length === 0) {
          uni.showToast({
            title: `请完成第${question.id}题`,
            icon: 'none'
          })

          this.scrollToQuestion(question.id)
          return false
        }
      }

      return true
    },

    scrollToQuestion(questionId) {
      uni.pageScrollTo({
        selector: `#question-${questionId}`,
        duration: 300
      })
    },

    saveDraft() {
      uni.setStorageSync(`feedback_draft_${this.questionnaireId}`, this.answers)
      uni.showToast({ title: '草稿已保存', icon: 'success' })
    },

    submitFeedback() {
      if (!this.validateRequired()) {
        return
      }

      uni.showModal({
        title: '提交问卷',
        content: '确认提交问卷？提交后不可修改',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '提交中...' })

            setTimeout(() => {
              uni.hideLoading()
              uni.removeStorageSync(`feedback_draft_${this.questionnaireId}`)

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
      if (Object.keys(this.answers).length > 0) {
        uni.showModal({
          title: '提示',
          content: '当前有未提交的内容，是否保存为草稿？',
          confirmText: '保存',
          cancelText: '不保存',
          success: (res) => {
            if (res.confirm) {
              this.saveDraft()
            }
            uni.navigateBack()
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

.feedback-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.questionnaire-header {
  margin-bottom: $spacing-md;
}

.questionnaire-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.5;
  margin-bottom: $spacing-sm;
}

.questionnaire-desc {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: $spacing-md;
}

.questionnaire-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.progress-hint {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.progress-bar {
  flex: 1;
  height: 8rpx;
  background: $bg-tertiary;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.progress-text {
  font-size: $font-size-sm;
  color: $text-secondary;
  white-space: nowrap;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.question-card {
  margin-bottom: 0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.question-number {
  display: inline-block;
  padding: 4rpx 12rpx;
  background: $primary-color;
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: 600;
  border-radius: $border-radius-sm;
}

.question-required {
  color: #ef4444;
  font-size: $font-size-lg;
  font-weight: bold;
}

.question-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.5;
  margin-bottom: $spacing-xs;
}

.question-desc {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
  line-height: 1.6;
  margin-bottom: $spacing-md;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.option-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  transition: all 0.3s;
}

.option-item.selected {
  background: rgba(102, 126, 234, 0.1);
  border: 1rpx solid $primary-color;
}

.option-radio,
.option-checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid $border-color;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s;
}

.option-checkbox {
  border-radius: $border-radius-sm;
}

.option-item.selected .option-radio,
.option-item.selected .option-checkbox {
  border-color: $primary-color;
  background: $primary-color;
}

.radio-checked {
  width: 16rpx;
  height: 16rpx;
  background: $text-white;
  border-radius: 50%;
}

.checkbox-checked {
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: bold;
}

.option-text {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.question-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg 0;
}

.rating-stars {
  display: flex;
  gap: $spacing-sm;
}

.star-icon {
  font-size: 64rpx;
  color: $border-color;
  transition: all 0.2s;
}

.star-icon.active {
  color: #f59e0b;
}

.rating-text {
  font-size: $font-size-md;
  color: $text-secondary;
}

.question-text {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.text-input {
  width: 100%;
  min-height: 200rpx;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  line-height: 1.6;
}

.text-count {
  text-align: right;
}

.text-count text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.action-buttons {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}
</style>
