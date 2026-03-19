<template>
  <view class="task-detail-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">任务详情</text>
        <text class="header-action" @click="showMoreOptions">⋯</text>
      </view>
    </view>

    <scroll-view class="detail-scroll" scroll-y>
      <!-- 任务基本信息 -->
      <view class="info-section card">
        <view class="section-header">
          <text class="section-title">{{ taskInfo.title }}</text>
          <view class="task-priority" :class="'priority-' + taskInfo.priority">
            {{ taskInfo.priority === 'high' ? '高优先级' : taskInfo.priority === 'medium' ? '中优先级' : '低优先级' }}
          </view>
        </view>

        <view class="task-meta-grid">
          <view class="meta-item">
            <text class="meta-label">任务类型</text>
            <text class="meta-value">{{ typeMap[taskInfo.type] }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">截止时间</text>
            <text class="meta-value">{{ taskInfo.deadline }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">负责人</text>
            <text class="meta-value">{{ taskInfo.assignee }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">当前状态</text>
            <view class="task-status" :class="'status-' + taskInfo.status">
              {{ statusMap[taskInfo.status] }}
            </view>
          </view>
        </view>
      </view>

      <!-- 任务描述 -->
      <view class="desc-section card">
        <view class="section-title">任务描述</view>
        <text class="desc-text">{{ taskInfo.description }}</text>
      </view>

      <!-- 任务要求 -->
      <view class="requirements-section card" v-if="taskInfo.requirements">
        <view class="section-title">任务要求</view>
        <view class="requirements-list">
          <view v-for="(req, index) in taskInfo.requirements" :key="index" class="requirement-item">
            <text class="requirement-dot">•</text>
            <text class="requirement-text">{{ req }}</text>
          </view>
        </view>
      </view>

      <!-- 进度跟踪 -->
      <view class="progress-section card">
        <view class="section-title">完成进度</view>
        <view class="progress-display">
          <view class="progress-bar-large">
            <view class="progress-fill" :style="{ width: taskInfo.progress + '%' }"></view>
          </view>
          <text class="progress-text">{{ taskInfo.progress }}%</text>
        </view>

        <view class="progress-actions" v-if="taskInfo.status !== 'completed'">
          <button class="btn btn-outline" @click="updateProgress">
            📊 更新进度
          </button>
        </view>
      </view>

      <!-- 子任务列表 -->
      <view class="subtasks-section card" v-if="taskInfo.subtasks && taskInfo.subtasks.length > 0">
        <view class="section-title">子任务清单</view>
        <view class="subtasks-list">
          <view
            v-for="(subtask, index) in taskInfo.subtasks"
            :key="index"
            class="subtask-item"
            :class="{ completed: subtask.completed }"
          >
            <view class="subtask-checkbox" @click="toggleSubtask(index)">
              <text v-if="subtask.completed" class="checkbox-icon">✓</text>
            </view>
            <text class="subtask-text">{{ subtask.title }}</text>
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
            @click="previewFile(file)"
          >
            <text class="file-icon">📄</text>
            <view class="file-info">
              <text class="file-name">{{ file.name }}</text>
              <text class="file-size">{{ file.size }}</text>
            </view>
            <text class="file-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 操作记录 -->
      <view class="history-section card">
        <view class="section-title">操作记录</view>
        <view class="history-timeline">
          <view
            v-for="(log, index) in operationLogs"
            :key="index"
            class="history-item"
          >
            <view class="timeline-dot"></view>
            <view class="history-content">
              <text class="history-action">{{ log.action }}</text>
              <text class="history-time">{{ log.time }}</text>
              <text class="history-user">{{ log.user }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部按钮区 -->
      <view class="action-buttons" v-if="taskInfo.status !== 'completed'">
        <button class="btn btn-outline btn-block" @click="transferTask" v-if="taskInfo.status === 'pending'">
          转交任务
        </button>
        <button class="btn btn-primary btn-block" @click="completeTask">
          ✓ 完成任务
        </button>
      </view>
    </scroll-view>

    <!-- 进度更新弹窗 -->
    <view v-if="showProgressModal" class="modal-overlay" @click="hideProgressModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">更新进度</text>
          <text class="modal-close" @click="hideProgressModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">完成进度（%）</text>
            <slider
              class="progress-slider"
              :value="tempProgress"
              @change="onProgressChange"
              min="0"
              max="100"
              step="5"
              show-value
              activeColor="#667eea"
            />
          </view>
          <view class="form-item">
            <text class="form-label">备注说明</text>
            <textarea
              v-model="progressNote"
              class="form-textarea"
              placeholder="请输入进度更新的详细说明"
              :maxlength="200"
            ></textarea>
          </view>
        </view>
        <view class="modal-footer">
          <button class="btn btn-secondary" @click="hideProgressModal">取消</button>
          <button class="btn btn-primary" @click="saveProgress">确定</button>
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
      showProgressModal: false,
      tempProgress: 0,
      progressNote: '',
      taskInfo: {
        id: '',
        title: '加载中...',
        description: '',
        type: '',
        priority: 'medium',
        status: 'pending',
        deadline: '',
        assignee: '',
        progress: 0,
        requirements: [],
        subtasks: [],
        attachments: []
      },
      operationLogs: [],
      statusMap: {
        pending: '待处理',
        processing: '进行中',
        completed: '已完成'
      },
      typeMap: {
        registration: '报名审核',
        grouping: '分组管理',
        seat: '座位分配',
        notice: '通知发送',
        checkin: '签到管理',
        other: '其他'
      }
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
          this.taskInfo = {
            id: data.id, title: data.taskName || '', description: data.description || '',
            type: data.taskType || 'other', priority: data.priority || 'medium',
            status: data.status || 'pending', deadline: data.deadline || '',
            assignee: data.ownerName || '', progress: data.progress || 0,
            requirements: [], subtasks: [],
            attachments: data.attachments ? (typeof data.attachments === 'string' ? JSON.parse(data.attachments) : data.attachments) : []
          }
        }
        // 加载操作日志
        try {
          const logs = await TaskAPI.getTaskLogs(this.taskId)
          this.operationLogs = (logs || []).map(l => ({
            action: l.action || l.remark || '', time: l.createTime || '', user: l.operatorName || ''
          }))
        } catch(e) { /* ignore */ }
      } catch(e) { console.error('Load task detail error:', e) }
      uni.hideLoading()
    },

    updateProgress() {
      this.tempProgress = this.taskInfo.progress
      this.progressNote = ''
      this.showProgressModal = true
    },

    hideProgressModal() {
      this.showProgressModal = false
    },

    onProgressChange(e) {
      this.tempProgress = e.detail.value
    },

    async saveProgress() {
      try {
        await taskApi.updateTask({
          id: this.taskInfo.id,
          progress: this.tempProgress,
          status: this.tempProgress === 100 ? 'completed' : this.taskInfo.status
        })
        this.taskInfo.progress = this.tempProgress
        this.operationLogs.unshift({
          action: `更新进度至${this.tempProgress}%`,
          time: this.getCurrentTime(),
          user: '我'
        })
        if (this.tempProgress === 100) {
          this.taskInfo.status = 'completed'
        }
        uni.showToast({ title: '进度已更新', icon: 'success' })
      } catch(e) {
        uni.showToast({ title: '更新失败', icon: 'none' })
      }
      this.showProgressModal = false
    },

    toggleSubtask(index) {
      this.taskInfo.subtasks[index].completed = !this.taskInfo.subtasks[index].completed

      const completedCount = this.taskInfo.subtasks.filter(st => st.completed).length
      const total = this.taskInfo.subtasks.length
      this.taskInfo.progress = Math.round((completedCount / total) * 100)
    },

    previewFile(file) {
      uni.showModal({
        title: file.name,
        content: `文件大小：${file.size}\n是否预览？`,
        success: (res) => {
          if (res.confirm) {
            uni.showToast({ title: '预览功能开发中', icon: 'none' })
          }
        }
      })
    },

    transferTask() {
      uni.showModal({
        title: '转交任务',
        editable: true,
        placeholderText: '请输入接收人ID',
        content: '确认要转交此任务给其他人吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const assigneeId = res.content || ''
              await taskApi.assignTask(this.taskInfo.id, { assigneeIds: [assigneeId] })
              uni.showToast({ title: '任务已转交', icon: 'success' })
              setTimeout(() => uni.navigateBack(), 1500)
            } catch(e) {
              uni.showToast({ title: '转交失败', icon: 'none' })
            }
          }
        }
      })
    },

    completeTask() {
      uni.showModal({
        title: '完成任务',
        content: '确认此任务已完成？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await TaskAPI.completeTask(this.taskId)
              this.taskInfo.status = 'completed'
              this.taskInfo.progress = 100
              uni.showToast({ title: '任务已完成', icon: 'success' })
            } catch(e) { uni.showToast({ title: '操作失败', icon: 'none' }) }
          }
        }
      })
    },

    showMoreOptions() {
      const itemList = ['编辑任务', '删除任务', '取消']
      uni.showActionSheet({
        itemList,
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.showToast({ title: '编辑功能开发中', icon: 'none' })
          } else if (res.tapIndex === 1) {
            this.deleteTask()
          }
        }
      })
    },

    deleteTask() {
      uni.showModal({
        title: '删除任务',
        content: '确认删除此任务？删除后不可恢复',
        confirmColor: '#ef4444',
        success: async (res) => {
          if (res.confirm) {
            try {
              await TaskAPI.cancelTask(this.taskId)
              uni.showToast({ title: '任务已删除', icon: 'success' })
              setTimeout(() => uni.navigateBack(), 1500)
            } catch(e) { uni.showToast({ title: '删除失败', icon: 'none' }) }
          }
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
  font-size: 40rpx;
  color: $text-primary;
}

.detail-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.card {
  margin-bottom: $spacing-md;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.task-priority {
  padding: 8rpx 16rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  font-weight: 500;
  white-space: nowrap;
}

.task-priority.priority-high {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.task-priority.priority-medium {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.task-priority.priority-low {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.task-meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-md;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.meta-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.meta-value {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.task-status {
  display: inline-block;
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  font-weight: 500;
}

.task-status.status-pending {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.task-status.status-processing {
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
}

.task-status.status-completed {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.desc-text {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.8;
}

.requirements-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.requirement-item {
  display: flex;
  gap: $spacing-sm;
}

.requirement-dot {
  color: $primary-color;
  font-weight: bold;
}

.requirement-text {
  flex: 1;
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
}

.progress-display {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.progress-bar-large {
  flex: 1;
  height: 24rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 12rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.progress-text {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
  min-width: 80rpx;
  text-align: right;
}

.progress-actions {
  display: flex;
  gap: $spacing-md;
}

.subtasks-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.subtask-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  transition: all 0.3s;
}

.subtask-item.completed {
  opacity: 0.6;
}

.subtask-checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.subtask-item.completed .subtask-checkbox {
  background: $primary-color;
  border-color: $primary-color;
}

.checkbox-icon {
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: bold;
}

.subtask-text {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.subtask-item.completed .subtask-text {
  text-decoration: line-through;
  color: $text-tertiary;
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

.file-name {
  font-size: $font-size-md;
  color: $text-primary;
}

.file-size {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.file-arrow {
  font-size: 32rpx;
  color: $text-tertiary;
}

.section-count {
  font-size: $font-size-md;
  font-weight: normal;
  color: $text-tertiary;
  margin-left: $spacing-xs;
}

.history-timeline {
  position: relative;
  padding-left: 40rpx;
}

.history-timeline::before {
  content: '';
  position: absolute;
  left: 8rpx;
  top: 0;
  bottom: 0;
  width: 2rpx;
  background: $border-color;
}

.history-item {
  position: relative;
  padding-bottom: $spacing-lg;
}

.history-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -32rpx;
  top: 4rpx;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: $primary-color;
  border: 3rpx solid $bg-primary;
}

.history-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.history-action {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.history-time,
.history-user {
  font-size: $font-size-sm;
  color: $text-tertiary;
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

.progress-slider {
  width: 100%;
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

.modal-footer {
  display: flex;
  gap: $spacing-md;
}

.modal-footer .btn {
  flex: 1;
}
</style>
