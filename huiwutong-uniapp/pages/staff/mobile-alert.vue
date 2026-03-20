<template>
  <view class="mobile-alert-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">预警处理</text>
        <text class="header-action" @click="viewHistory">历史</text>
      </view>
    </view>

    <!-- 紧急程度标识 -->
    <view class="urgency-banner" :class="'urgency-' + currentAlert.urgency">
      <text class="urgency-icon">{{ getUrgencyIcon(currentAlert.urgency) }}</text>
      <view class="urgency-info">
        <text class="urgency-title">{{ getUrgencyTitle(currentAlert.urgency) }}</text>
        <text class="urgency-desc">{{ getUrgencyDesc(currentAlert.urgency) }}</text>
      </view>
    </view>

    <scroll-view class="alert-scroll" scroll-y>
      <!-- 预警详情 -->
      <view class="alert-detail card">
        <view class="detail-header">
          <text class="detail-title">{{ currentAlert.title }}</text>
          <view class="status-badge" :class="'status-' + currentAlert.status">
            {{ getStatusLabel(currentAlert.status) }}
          </view>
        </view>

        <view class="detail-content">
          <text class="detail-desc">{{ currentAlert.description }}</text>
        </view>

        <view class="detail-meta">
          <view class="meta-item">
            <text class="meta-label">预警类型</text>
            <text class="meta-value">{{ currentAlert.type }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">触发时间</text>
            <text class="meta-value">{{ currentAlert.triggerTime }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-label">涉及对象</text>
            <text class="meta-value">{{ currentAlert.target }}</text>
          </view>
        </view>

        <view class="detail-threshold" v-if="currentAlert.threshold">
          <text class="threshold-label">触发阈值：</text>
          <text class="threshold-value warning">{{ currentAlert.threshold }}</text>
        </view>
      </view>

      <!-- 处理操作 -->
      <view class="action-section card">
        <view class="section-title">处理操作</view>

        <view class="quick-actions">
          <view
            v-for="action in quickActions"
            :key="action.value"
            class="quick-action-btn"
            @click="handleQuickAction(action)"
          >
            <text class="action-icon">{{ action.icon }}</text>
            <text class="action-label">{{ action.label }}</text>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">处理结果</text>
          <picker mode="selector" :range="handleResults" @change="onResultChange">
            <view class="picker-input">
              {{ handleResult || '请选择处理结果' }}
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">处理说明</text>
          <textarea
            v-model="handleNote"
            class="form-textarea"
            placeholder="请说明处理情况..."
            :maxlength="500"
          ></textarea>
          <view class="input-count">
            <text>{{ handleNote.length }}/500</text>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">上传图片（可选）</text>
          <view class="upload-area">
            <view
              v-for="(img, index) in uploadedImages"
              :key="index"
              class="upload-item"
            >
              <image class="upload-image" :src="img" mode="aspectFill"></image>
              <text class="upload-remove" @click="removeImage(index)">✕</text>
            </view>
            <view class="upload-btn" @click="chooseImage" v-if="uploadedImages.length < 3">
              <text class="upload-icon">📷</text>
              <text class="upload-text">添加图片</text>
            </view>
          </view>
        </view>

        <button class="btn btn-primary btn-block" @click="submitHandle">
          ✓ 提交处理
        </button>
      </view>

      <!-- 相关预警 -->
      <view class="related-alerts card" v-if="relatedAlerts.length > 0">
        <view class="section-title">相关预警</view>
        <view class="related-list">
          <view
            v-for="alert in relatedAlerts"
            :key="alert.id"
            class="related-item"
            @click="viewAlert(alert)"
          >
            <view class="related-icon" :class="'urgency-' + alert.urgency">
              {{ getUrgencyIcon(alert.urgency) }}
            </view>
            <view class="related-info">
              <text class="related-title">{{ alert.title }}</text>
              <text class="related-time">{{ alert.triggerTime }}</text>
            </view>
            <view class="related-status" :class="'status-' + alert.status">
              {{ getStatusLabel(alert.status) }}
            </view>
          </view>
        </view>
      </view>

      <!-- 处理记录 -->
      <view class="history-section card" v-if="handleHistory.length > 0">
        <view class="section-title">处理记录</view>
        <view class="history-list">
          <view
            v-for="(history, index) in handleHistory"
            :key="history.id"
            class="history-item"
          >
            <view class="history-header">
              <text class="history-handler">{{ history.handler }}</text>
              <text class="history-time">{{ history.handleTime }}</text>
            </view>
            <view class="history-result">
              <text class="result-label">结果：</text>
              <text class="result-value" :class="'result-' + history.result">
                {{ history.result }}
              </text>
            </view>
            <view class="history-note" v-if="history.note">
              <text class="note-label">说明：</text>
              <text class="note-text">{{ history.note }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 快速操作弹窗 -->
    <view v-if="showActionModal" class="modal-overlay" @click="hideActionModal">
      <view class="action-modal" @click.stop>
        <view class="modal-title">快速操作</view>
        <view class="action-list">
          <view
            v-for="action in actionList"
            :key="action.value"
            class="action-list-item"
            @click="executeAction(action)"
          >
            <text class="action-list-icon">{{ action.icon }}</text>
            <text class="action-list-label">{{ action.label }}</text>
          </view>
        </view>
        <view class="modal-cancel" @click="hideActionModal">
          <text>取消</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      alertId: '',
      handleResult: '',
      handleNote: '',
      uploadedImages: [],
      showActionModal: false,
      currentAlert: {
        title: '',
        urgency: 'medium',
        status: 'pending',
        type: '',
        description: '',
        triggerTime: '',
        target: '',
        threshold: ''
      },
      quickActions: [
        { icon: '<text class="fa fa-phone"></text>', label: '电话通知', value: 'call' },
        { icon: '<text class="fa fa-comments"></text>', label: '发送消息', value: 'message' },
        { icon: '📧', label: '发送邮件', value: 'email' },
        { icon: '<text class="fa fa-clipboard"></text>', label: '查看详情', value: 'detail' }
      ],
      handleResults: ['已处理', '跟进中', '误报', '忽略'],
      relatedAlerts: [],
      handleHistory: [],
      actionList: [
        { icon: '<text class="fa fa-phone"></text>', label: '电话联系当事人', value: 'call' },
        { icon: '<text class="fa fa-comments"></text>', label: '发送消息提醒', value: 'message' },
        { icon: '📧', label: '发送邮件通知', value: 'email' },
        { icon: '<text class="fa fa-users"></text>', label: '查看学员信息', value: 'userInfo' },
        { icon: '📊', label: '查看详细数据', value: 'data' }
      ]
    }
  },

  onLoad(options) {
    if (options.id) {
      this.alertId = options.id
      this.loadAlert()
    }
  },

  methods: {
    getUrgencyIcon(urgency) {
      const iconMap = {
        high: '🔴',
        medium: '🟡',
        low: '🟢'
      }
      return iconMap[urgency] || '⚪'
    },

    getUrgencyTitle(urgency) {
      const titleMap = {
        high: '紧急',
        medium: '重要',
        low: '一般'
      }
      return titleMap[urgency] || ''
    },

    getUrgencyDesc(urgency) {
      const descMap = {
        high: '需要立即处理',
        medium: '建议尽快处理',
        low: '可稍后处理'
      }
      return descMap[urgency] || ''
    },

    getStatusLabel(status) {
      const labelMap = {
        pending: '待处理',
        processing: '处理中',
        resolved: '已处理',
        ignored: '已忽略'
      }
      return labelMap[status] || status
    },

    handleQuickAction(action) {
      if (action.value === 'call') {
        uni.makePhoneCall({
          phoneNumber: '138****0001'
        })
      } else if (action.value === 'message') {
        uni.navigateTo({
          url: '/pages/learner/chat-private'
        })
      } else if (action.value === 'detail') {
        uni.showModal({
          title: '预警详情',
          content: this.currentAlert.description,
          showCancel: false
        })
      } else {
        uni.showModal({
          title: action.label,
          content: '功能开发中',
          icon: 'none'
        })
      }
    },

    onResultChange(e) {
      this.handleResult = this.handleResults[e.detail.value]
    },

    chooseImage() {
      uni.chooseImage({
        count: 3 - this.uploadedImages.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.uploadedImages.push(...res.tempFilePaths)
        }
      })
    },

    removeImage(index) {
      this.uploadedImages.splice(index, 1)
    },

    viewAlert(alert) {
      uni.showModal({
        title: alert.title,
        content: alert.description,
        showCancel: false
      })
    },

    async viewHistory() {
      uni.showLoading({ title: '加载中...' })
      try {
        const alertApi = require('@/api/alert').default
        const conferenceId = uni.getStorageSync('conferenceId') || '2030309010523144194'
        const result = await alertApi.list({ conferenceId, status: 'resolved' })
        const list = Array.isArray(result) ? result : (result.records || [])
        uni.hideLoading()
        if (list.length === 0) {
          uni.showToast({ title: '暂无历史记录', icon: 'none' })
          return
        }
        this.relatedAlerts = list.slice(0, 10).map(a => ({
          id: a.id,
          title: `${a.metric}预警`,
          time: a.createTime || '',
          status: a.status,
          description: `当前值 ${a.metricValue}% ${a.threshold ? '阈值 ' + a.threshold + '%' : ''}`
        }))
        uni.showToast({ title: `已加载${this.relatedAlerts.length}条历史`, icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '加载历史失败', icon: 'none' })
      }
    },

    showActionModal() {
      this.showActionModal = true
    },

    hideActionModal() {
      this.showActionModal = false
    },

    executeAction(action) {
      this.hideActionModal()
      this.handleQuickAction({ value: action.value })
    },

    submitHandle() {
      if (!this.handleResult) {
        uni.showToast({
          title: '请选择处理结果',
          icon: 'none'
        })
        return
      }

      if (!this.handleNote.trim()) {
        uni.showToast({
          title: '请填写处理说明',
          icon: 'none'
        })
        return
      }

      uni.showModal({
        title: '提交处理',
        content: '确认提交处理结果？',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '提交中...' })
            try {
              const alertApi = require('@/api/alert').default
              if (this.handleResult === '已处理' || this.handleResult === '误报' || this.handleResult === '忽略') {
                await alertApi.resolve(this.alertId, { remark: this.handleNote })
                this.currentAlert.status = 'resolved'
              } else {
                await alertApi.process(this.alertId)
                this.currentAlert.status = 'processing'
              }
              uni.hideLoading()
              uni.showToast({ title: '提交成功', icon: 'success' })
              setTimeout(() => { uni.navigateBack() }, 1500)
            } catch (e) {
              uni.hideLoading()
              uni.showToast({ title: '提交失败', icon: 'none' })
            }
          }
        }
      })
    },

    async loadAlert() {
      uni.showLoading({ title: '加载中...' })
      try {
        const alertApi = require('@/api/alert').default
        const conferenceId = uni.getStorageSync('conferenceId') || '2030309010523144194'
        const result = await alertApi.list({ conferenceId })
        const list = Array.isArray(result) ? result : (result.records || [])
        const alert = list.find(a => String(a.id) === String(this.alertId))
        if (alert) {
          this.currentAlert = {
            title: `${alert.metric}预警`,
            urgency: alert.severity || 'medium',
            status: alert.status || 'pending',
            type: alert.metric,
            description: `当前值 ${alert.metricValue}，阈值 ${alert.threshold}`,
            triggerTime: alert.createTime,
            target: alert.conferenceId,
            threshold: `${alert.metricValue} / ${alert.threshold}`
          }
        }
      } catch (e) {
        console.error('加载预警详情失败', e)
      } finally {
        uni.hideLoading()
      }
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

.mobile-alert-container {
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

.urgency-banner {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg $spacing-md;
  margin-bottom: $spacing-md;
}

.urgency-banner.urgency-high {
  background: rgba(239, 68, 68, 0.1);
  border-bottom: 1rpx solid rgba(239, 68, 68, 0.3);
}

.urgency-banner.urgency-medium {
  background: rgba(245, 158, 11, 0.1);
  border-bottom: 1rpx solid rgba(245, 158, 11, 0.3);
}

.urgency-banner.urgency-low {
  background: rgba(16, 185, 129, 0.1);
  border-bottom: 1rpx solid rgba(16, 185, 129, 0.3);
}

.urgency-icon {
  font-size: 48rpx;
}

.urgency-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.urgency-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.urgency-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.alert-scroll {
  height: calc(100vh - 200rpx);
  padding: 0 $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.card {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.detail-title {
  flex: 1;
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  margin-right: $spacing-md;
}

.status-badge {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.status-pending {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.status-badge.status-processing {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.status-badge.status-resolved {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.detail-content {
  margin-bottom: $spacing-md;
}

.detail-desc {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
}

.detail-meta {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.meta-item {
  display: flex;
  justify-content: space-between;
}

.meta-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.meta-value {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.detail-threshold {
  display: flex;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: rgba(239, 68, 68, 0.05);
  border-radius: $border-radius-md;
}

.threshold-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.threshold-value.warning {
  flex: 1;
  font-size: $font-size-md;
  font-weight: 600;
  color: #ef4444;
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  margin-bottom: $spacing-lg;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
}

.action-icon {
  font-size: 40rpx;
}

.action-label {
  font-size: $font-size-sm;
  color: $text-primary;
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

.picker-input {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  text-align: center;
  font-size: $font-size-md;
  color: $text-primary;
}

.form-textarea {
  width: 100%;
  min-height: 160rpx;
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

.upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.upload-item {
  position: relative;
  width: 160rpx;
  height: 160rpx;
}

.upload-image {
  width: 100%;
  height: 100%;
  border-radius: $border-radius-md;
}

.upload-remove {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 32rpx;
  height: 32rpx;
  background: rgba(0, 0, 0, 0.6);
  color: $text-white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-lg;
}

.upload-btn {
  width: 160rpx;
  height: 160rpx;
  border: 2rpx dashed $border-color;
  border-radius: $border-radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
}

.upload-icon {
  font-size: 48rpx;
}

.upload-text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.related-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.related-icon {
  font-size: 32rpx;
}

.related-icon.urgency-high {
  color: #ef4444;
}

.related-icon.urgency-medium {
  color: #f59e0b;
}

.related-icon.urgency-low {
  color: #10b981;
}

.related-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.related-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.related-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.related-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.related-status.status-pending {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.related-status.status-resolved {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
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

.history-handler {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.history-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.history-result {
  display: flex;
  gap: $spacing-xs;
}

.result-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.result-value.result-已处理 {
  color: #10b981;
}

.result-value.result-跟进中 {
  color: #f59e0b;
}

.result-value.result-误报 {
  color: $text-tertiary;
}

.history-note {
  margin-top: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1rpx solid $border-color;
}

.note-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.note-text {
  font-size: $font-size-sm;
  color: $text-primary;
  line-height: 1.6;
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

.action-modal {
  width: 100%;
  max-width: 500rpx;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
}

.modal-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  text-align: center;
  margin-bottom: $spacing-lg;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.action-list-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.action-list-icon {
  font-size: 32rpx;
}

.action-list-label {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.modal-cancel {
  text-align: center;
  padding: $spacing-md;
  color: $text-tertiary;
  font-size: $font-size-md;
}
</style>
