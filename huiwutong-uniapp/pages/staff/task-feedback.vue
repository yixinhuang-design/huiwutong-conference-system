<template>
  <view class="task-feedback-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">任务反馈</text>
        <text class="header-action"></text>
      </view>
    </view>

    <scroll-view class="feedback-scroll" scroll-y>
      <!-- 任务信息 -->
      <view class="task-info card">
        <view class="task-title">{{ taskInfo.title }}</view>
        <view class="task-meta">
          <text class="meta-item"><text class="fa fa-calendar-alt"></text> {{ taskInfo.deadline }}</text>
          <text class="meta-item"><text class="fa fa-user"></text> {{ taskInfo.assignee }}</text>
        </view>
      </view>

      <!-- 反馈表单 -->
      <view class="feedback-form card">
        <view class="form-section">
          <view class="section-title">完成情况</view>
          <view class="rating-options">
            <view
              v-for="option in completionOptions"
              :key="option.value"
              class="rating-option"
              :class="{ selected: feedback.completion === option.value }"
              @click="selectCompletion(option.value)"
            >
              <text class="option-icon">{{ option.icon }}</text>
              <text class="option-label">{{ option.label }}</text>
            </view>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">完成进度</view>
          <view class="progress-input">
            <slider
              :value="feedback.progress"
              @change="onProgressChange"
              min="0"
              max="100"
              step="5"
              show-value
              activeColor="#667eea"
            />
            <text class="progress-text">{{ feedback.progress }}%</text>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">工作描述</view>
          <textarea
            v-model="feedback.description"
            class="form-textarea"
            placeholder="请描述任务完成的具体情况"
            :maxlength="500"
          ></textarea>
          <view class="input-count">
            <text>{{ feedback.description.length }}/500</text>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">遇到的问题</view>
          <textarea
            v-model="feedback.problems"
            class="form-textarea"
            placeholder="请描述任务执行过程中遇到的问题（选填）"
            :maxlength="500"
          ></textarea>
          <view class="input-count">
            <text>{{ feedback.problems.length }}/500</text>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">改进建议</view>
          <textarea
            v-model="feedback.suggestions"
            class="form-textarea"
            placeholder="请提出对任务或流程的改进建议（选填）"
            :maxlength="500"
          ></textarea>
          <view class="input-count">
            <text>{{ feedback.suggestions.length }}/500</text>
          </view>
        </view>

        <view class="form-section">
          <view class="section-title">附件上传</view>
          <view class="upload-list">
            <view
              v-for="(file, index) in feedback.files"
              :key="index"
              class="upload-item"
            >
              <text class="file-icon">📎</text>
              <text class="file-name">{{ file.name }}</text>
              <text class="file-remove" @click="removeFile(index)">✕</text>
            </view>
            <view class="upload-btn" @click="chooseFile">
              <text class="upload-icon">+</text>
              <text class="upload-text">添加附件</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 历史反馈 -->
      <view class="history-section card" v-if="historyList.length > 0">
        <view class="section-title">历史反馈</view>
        <view class="history-list">
          <view
            v-for="(history, index) in historyList"
            :key="history.id"
            class="history-item"
            @click="viewHistory(history)"
          >
            <view class="history-header">
              <text class="history-time">{{ history.time }}</text>
              <view class="history-status" :class="'status-' + history.status">
                {{ history.status }}
              </view>
            </view>
            <text class="history-desc">{{ history.description }}</text>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button class="btn btn-outline btn-block" @click="saveDraft">
          💾 保存草稿
        </button>
        <button class="btn btn-primary btn-block" @click="submitFeedback">
          ✓ 提交反馈
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import TaskAPI from '@/api/task'

export default {
  data() {
    return {
      taskId: '',
      taskInfo: {
        title: '加载中...',
        deadline: '2025-01-17 12:00',
        assignee: '李老师'
      },
      feedback: {
        completion: 'completed',
        progress: 100,
        description: '',
        problems: '',
        suggestions: '',
        files: []
      },
      completionOptions: [
        { icon: '<text class="fa fa-check"></text>', label: '已完成', value: 'completed' },
        { icon: '🔄', label: '进行中', value: 'inProgress' },
        { icon: '<text class="fa fa-exclamation-triangle"></text>', label: '有困难', value: 'difficult' },
        { icon: '❌', label: '未完成', value: 'incomplete' }
      ],
      historyList: []
    }
  },

  onLoad(options) {
    if (options.id) {
      this.taskId = options.id
      this.loadTaskInfo()
    }
    this.loadDraft()
  },

  methods: {
    async loadTaskInfo() {
      uni.showLoading({ title: '加载中...' })
      try {
        const data = await TaskAPI.getTaskDetail(this.taskId)
        if (data) {
          this.taskInfo = {
            title: data.taskName || '', deadline: data.deadline || '',
            assignee: data.ownerName || ''
          }
        }
        const logs = await TaskAPI.getTaskLogs(this.taskId)
        this.historyList = (logs || []).map(l => ({
          id: l.id, time: l.createTime || '', status: l.action || '',
          description: l.remark || ''
        }))
      } catch(e) { console.error('Load task info error:', e) }
      uni.hideLoading()
    },

    loadDraft() {
      const draft = uni.getStorageSync(`task_feedback_draft_${this.taskId}`)
      if (draft) {
        this.feedback = draft
      }
    },

    selectCompletion(value) {
      this.feedback.completion = value
      if (value === 'completed') {
        this.feedback.progress = 100
      }
    },

    onProgressChange(e) {
      this.feedback.progress = e.detail.value
    },

    chooseFile() {
      uni.chooseMessageFile({
        count: 5,
        success: (res) => {
          this.feedback.files.push(...res.tempFiles)
        }
      })
    },

    removeFile(index) {
      this.feedback.files.splice(index, 1)
    },

    saveDraft() {
      uni.setStorageSync(`task_feedback_draft_${this.taskId}`, this.feedback)
      uni.showToast({ title: '草稿已保存', icon: 'success' })
    },

    submitFeedback() {
      if (!this.feedback.description.trim()) {
        uni.showToast({ title: '请填写工作描述', icon: 'none' })
        return
      }

      uni.showModal({
        title: '提交反馈',
        content: '确认提交任务反馈？',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '提交中...' })
            try {
              await TaskAPI.submitTask(this.taskId, {
                submitContent: this.feedback.description,
                userId: 1, userName: '管理员'
              })
              uni.hideLoading()
              uni.removeStorageSync(`task_feedback_draft_${this.taskId}`)
              uni.showToast({ title: '提交成功', icon: 'success' })

              setTimeout(() => {
                uni.navigateBack()
              }, 1500)
            } catch(e) {
              uni.hideLoading()
              uni.showToast({ title: '提交失败', icon: 'none' })
            }
          }
        }
      })
    },

    viewHistory(history) {
      uni.showModal({
        title: '历史反馈',
        content: history.description,
        showCancel: false
      })
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

.task-feedback-container {
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

.task-info {
  margin-bottom: $spacing-md;
}

.task-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.task-meta {
  display: flex;
  gap: $spacing-lg;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.feedback-form {
  margin-bottom: $spacing-md;
}

.form-section {
  margin-bottom: $spacing-xl;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.rating-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.rating-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-lg;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.rating-option.selected {
  background: rgba(102, 126, 234, 0.1);
  border-color: $primary-color;
}

.option-icon {
  font-size: 48rpx;
}

.option-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.rating-option.selected .option-label {
  color: $primary-color;
  font-weight: 500;
}

.progress-input {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.progress-text {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $primary-color;
  min-width: 80rpx;
  text-align: right;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  line-height: 1.6;
}

.input-count {
  text-align: right;
  margin-top: $spacing-xs;
}

.input-count text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.upload-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.upload-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.file-icon {
  font-size: $font-size-lg;
}

.file-name {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.file-remove {
  font-size: $font-size-xl;
  color: #ef4444;
  padding: 0 $spacing-sm;
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-lg;
  background: $bg-tertiary;
  border: 2rpx dashed $border-color;
  border-radius: $border-radius-md;
}

.upload-icon {
  font-size: 48rpx;
  color: $text-tertiary;
}

.upload-text {
  font-size: $font-size-md;
  color: $text-tertiary;
}

.history-section {
  margin-bottom: $spacing-md;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.history-item {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xs;
}

.history-time {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.history-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.history-status.status-已审核 {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.history-status.status-待审核 {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.history-desc {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: 1.6;
}

.submit-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}
</style>
