<template>
  <view class="task-detail-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">任务详情</text>
        <text class="header-action" @click="shareTask">分享</text>
      </view>
    </view>

    <scroll-view class="detail-scroll" scroll-y>
      <!-- 任务状态卡片 -->
      <view class="status-card" :class="'status-' + taskInfo.status">
        <view class="status-icon">{{ statusIcon }}</view>
        <view class="status-info">
          <text class="status-title">{{ statusTitle }}</text>
          <text class="status-desc">{{ statusDesc }}</text>
        </view>
      </view>

      <!-- 任务基本信息 -->
      <view class="info-section card">
        <view class="section-title">{{ taskInfo.title }}</view>

        <view class="task-meta">
          <view class="meta-item">
            <text class="meta-icon"><text class="fa fa-calendar-alt"></text></text>
            <text class="meta-text">截止时间：{{ taskInfo.deadline }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon"><text class="fa fa-user"></text></text>
            <text class="meta-text">发布人：{{ taskInfo.publisher }}</text>
          </view>
          <view class="meta-item" v-if="taskInfo.priority">
            <text class="meta-icon">⭐</text>
            <text class="meta-text">优先级：{{ taskInfo.priority }}</text>
          </view>
        </view>
      </view>

      <!-- 任务描述 -->
      <view class="desc-section card">
        <view class="section-title">任务描述</view>
        <text class="desc-text">{{ taskInfo.description }}</text>
      </view>

      <!-- 任务要求 -->
      <view class="requirements-section card" v-if="taskInfo.requirements && taskInfo.requirements.length > 0">
        <view class="section-title">任务要求</view>
        <view class="requirements-list">
          <view v-for="(req, index) in taskInfo.requirements" :key="index" class="requirement-item">
            <view class="requirement-num">{{ index + 1 }}</view>
            <text class="requirement-text">{{ req }}</text>
          </view>
        </view>
      </view>

      <!-- 任务附件 -->
      <view class="attachments-section card" v-if="taskInfo.attachments && taskInfo.attachments.length > 0">
        <view class="section-title">
          <text>相关附件</text>
          <text class="section-count">({{ taskInfo.attachments.length }})</text>
        </view>
        <view class="attachments-list">
          <view
            v-for="(file, index) in taskInfo.attachments"
            :key="index"
            class="attachment-item"
            @click="downloadFile(file)"
          >
            <text class="file-icon">📎</text>
            <view class="file-info">
              <text class="file-name">{{ file.name }}</text>
              <text class="file-size">{{ file.size }}</text>
            </view>
            <text class="download-icon">⬇</text>
          </view>
        </view>
      </view>

      <!-- 提交记录 -->
      <view class="submission-section card" v-if="taskInfo.status === 'submitted' || taskInfo.status === 'reviewed'">
        <view class="section-title">提交记录</view>
        <view class="submission-info">
          <view class="submission-item">
            <text class="submission-label">提交时间</text>
            <text class="submission-value">{{ taskInfo.submitTime }}</text>
          </view>
          <view class="submission-item">
            <text class="submission-label">提交内容</text>
            <text class="submission-value">{{ taskInfo.submitContent }}</text>
          </view>
          <view class="submission-item" v-if="taskInfo.status === 'reviewed'">
            <text class="submission-label">审核结果</text>
            <view class="review-result" :class="taskInfo.approved ? 'approved' : 'rejected'">
              {{ taskInfo.approved ? '✓ 已通过' : '✗ 未通过' }}
            </view>
          </view>
          <view class="submission-item" v-if="taskInfo.status === 'reviewed' && taskInfo.feedback">
            <text class="submission-label">审核意见</text>
            <text class="submission-value">{{ taskInfo.feedback }}</text>
          </view>
        </view>
      </view>

      <!-- 底部操作按钮 -->
      <view class="action-buttons" v-if="taskInfo.status === 'pending'">
        <button class="btn btn-outline btn-block" @click="markComplete">
          ✓ 标记完成
        </button>
        <button class="btn btn-primary btn-block" @click="submitTask">
          <text class="fa fa-edit"></text> 提交任务
        </button>
      </view>
    </scroll-view>

    <!-- 提交任务弹窗 -->
    <view v-if="showSubmitModal" class="modal-overlay" @click="hideSubmitModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">提交任务</text>
          <text class="modal-close" @click="hideSubmitModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">完成说明</text>
            <textarea
              v-model="submitForm.description"
              class="form-textarea"
              placeholder="请描述任务完成情况"
              :maxlength="500"
            ></textarea>
          </view>
          <view class="form-item">
            <text class="form-label">上传附件（可选）</text>
            <view class="upload-area" @click="chooseFile">
              <text class="upload-icon">📎</text>
              <text class="upload-text">点击选择文件</text>
            </view>
            <view v-if="submitForm.file" class="uploaded-file">
              <text class="file-name">{{ submitForm.file.name }}</text>
              <text class="file-remove" @click="removeFile">✕</text>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <button class="btn btn-secondary" @click="hideSubmitModal">取消</button>
          <button class="btn btn-primary" @click="confirmSubmit">提交</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import TaskAPI from '@/api/task'

export default {
  data() {
    return {
      taskId: '',
      showSubmitModal: false,
      submitForm: {
        description: '',
        file: null
      },
      taskInfo: {
        id: '',
        title: '加载中...',
        description: '',
        deadline: '',
        publisher: '',
        priority: '中',
        status: 'pending',
        requirements: [],
        attachments: []
      }
    }
  },

  computed: {
    statusIcon() {
      const iconMap = {
        pending: '<text class="fa fa-clipboard"></text>',
        submitted: '<text class="fa fa-check"></text>',
        reviewed: '<text class="fa fa-edit"></text>'
      }
      return iconMap[this.taskInfo.status] || '<text class="fa fa-clipboard"></text>'
    },

    statusTitle() {
      const titleMap = {
        pending: '待完成',
        submitted: '已提交',
        reviewed: '已审核'
      }
      return titleMap[this.taskInfo.status] || '待完成'
    },

    statusDesc() {
      const descMap = {
        pending: '请在截止时间前完成并提交任务',
        submitted: '任务已提交，等待审核',
        reviewed: this.taskInfo.approved ? '任务已通过审核' : '任务未通过审核'
      }
      return descMap[this.taskInfo.status] || ''
    }
  },

  onLoad(options) {
    if (options.id) {
      this.taskId = options.id
      this.loadTaskDetail()
    }
  },

  methods: {
    async loadTaskDetail() {
      uni.showLoading({ title: '加载中...' })
      try {
        const data = await TaskAPI.getTaskDetail(this.taskId)
        if (data) {
          const priorityMap = { high: '高', medium: '中', low: '低' }
          this.taskInfo = {
            id: data.id, title: data.taskName || '', description: data.description || '',
            deadline: data.deadline || '', publisher: data.ownerName || '',
            priority: priorityMap[data.priority] || '中', status: data.status || 'pending',
            requirements: [],
            attachments: data.attachments ? (typeof data.attachments === 'string' ? JSON.parse(data.attachments) : data.attachments) : []
          }
        }
      } catch(e) { console.error('Load task detail error:', e) }
      uni.hideLoading()
    },

    markComplete() {
      uni.showModal({
        title: '标记完成',
        content: '确认标记此任务为已完成？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await TaskAPI.completeTask(this.taskId)
              this.taskInfo.status = 'submitted'
              this.taskInfo.submitTime = this.getCurrentTime()
              uni.showToast({ title: '已标记完成', icon: 'success' })
            } catch(e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
          }
        }
      })
    },

    submitTask() {
      this.submitForm = {
        description: '',
        file: null
      }
      this.showSubmitModal = true
    },

    hideSubmitModal() {
      this.showSubmitModal = false
    },

    chooseFile() {
      uni.chooseMessageFile({
        count: 1,
        success: (res) => {
          this.submitForm.file = res.tempFiles[0]
        }
      })
    },

    removeFile() {
      this.submitForm.file = null
    },

    async confirmSubmit() {
      if (!this.submitForm.description.trim()) {
        uni.showToast({ title: '请填写完成说明', icon: 'none' })
        return
      }
      try {
        await TaskAPI.submitTask(this.taskId, {
          submitContent: this.submitForm.description,
          userId: 1, userName: '学员'
        })
        this.taskInfo.status = 'submitted'
        this.taskInfo.submitTime = this.getCurrentTime()
        this.taskInfo.submitContent = this.submitForm.description
        uni.showToast({ title: '提交成功', icon: 'success' })
      } catch(e) { uni.showToast({ title: '提交失败', icon: 'none' }) }
      this.showSubmitModal = false
    },

    downloadFile(file) {
      uni.showModal({
        title: file.name,
        content: `文件大小：${file.size}\n是否下载？`,
        success: (res) => {
          if (res.confirm) {
            uni.showToast({ title: '下载功能开发中', icon: 'none' })
          }
        }
      })
    },

    shareTask() {
      uni.showActionSheet({
        itemList: ['分享给好友', '生成二维码', '复制链接'],
        success: (res) => {
          uni.showToast({ title: '分享功能开发中', icon: 'none' })
        }
      })
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

.task-detail-container {
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

.detail-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.status-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.status-card.status-pending {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(245, 158, 11, 0.05) 100%);
  border: 1rpx solid rgba(245, 158, 11, 0.3);
}

.status-card.status-submitted {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(102, 126, 234, 0.05) 100%);
  border: 1rpx solid rgba(102, 126, 234, 0.3);
}

.status-card.status-reviewed {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.05) 100%);
  border: 1rpx solid rgba(16, 185, 129, 0.3);
}

.status-icon {
  font-size: 64rpx;
}

.status-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.status-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.status-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.card {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.section-count {
  font-size: $font-size-md;
  font-weight: normal;
  color: $text-tertiary;
  margin-left: $spacing-xs;
}

.task-meta {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.meta-icon {
  font-size: $font-size-md;
}

.meta-text {
  font-size: $font-size-md;
  color: $text-secondary;
}

.desc-text {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.8;
}

.requirements-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.requirement-item {
  display: flex;
  gap: $spacing-md;
  align-items: flex-start;
}

.requirement-num {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: $primary-color;
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.requirement-text {
  flex: 1;
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
  padding-top: 8rpx;
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.file-icon {
  font-size: 40rpx;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.file-info .file-name {
  font-size: $font-size-md;
  color: $text-primary;
}

.file-info .file-size {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.download-icon {
  font-size: 32rpx;
  color: $primary-color;
}

.submission-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.submission-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.submission-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.submission-value {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: 1.6;
}

.review-result {
  display: inline-block;
  padding: 8rpx 16rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  font-weight: 500;
}

.review-result.approved {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.review-result.rejected {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.action-buttons {
  display: flex;
  gap: $spacing-md;
  padding-top: $spacing-md;
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

.modal-content {
  width: 100%;
  max-width: 600rpx;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  max-height: 80vh;
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
  margin-bottom: $spacing-lg;
}

.form-item {
  margin-bottom: $spacing-lg;
}

.form-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-md;
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

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-xl;
  background: $bg-tertiary;
  border: 2rpx dashed $border-color;
  border-radius: $border-radius-md;
}

.upload-icon {
  font-size: 64rpx;
}

.upload-text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.uploaded-file {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  background: rgba(102, 126, 234, 0.1);
  border-radius: $border-radius-md;
  margin-top: $spacing-sm;
}

.uploaded-file .file-name {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
}

.file-remove {
  font-size: $font-size-lg;
  color: #ef4444;
  padding: 0 $spacing-sm;
}

.modal-footer {
  display: flex;
  gap: $spacing-md;
}

.modal-footer .btn {
  flex: 1;
}
</style>
