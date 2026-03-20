<template>
  <view class="mobile-notice-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">发布通知</text>
        <text class="header-action" @click="saveDraft">草稿</text>
      </view>
    </view>

    <scroll-view class="form-scroll" scroll-y>
      <!-- 快速模板 -->
      <view class="template-section card">
        <view class="section-title">快速模板</view>
        <scroll-view class="template-scroll" scroll-x>
          <view class="template-list">
            <view
              v-for="template in noticeTemplates"
              :key="template.id"
              class="template-item"
              @click="useTemplate(template)"
            >
              <text class="template-icon">{{ template.icon }}</text>
              <text class="template-label">{{ template.label }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 通知表单 -->
      <view class="form-section card">
        <view class="form-item required">
          <text class="form-label">通知标题</text>
          <input
            v-model="formData.title"
            class="form-input"
            placeholder="请输入通知标题"
            :maxlength="100"
          />
        </view>

        <view class="form-item required">
          <text class="form-label">接收对象</text>
          <radio-group @change="onTargetChange">
            <label class="radio-item">
              <radio value="all" :checked="formData.target === 'all'" color="#667eea" />
              <text class="radio-label">全体学员</text>
            </label>
            <label class="radio-item">
              <radio value="group" :checked="formData.target === 'group'" color="#667eea" />
              <text class="radio-label">指定分组</text>
            </label>
          </radio-group>
        </view>

        <view class="form-item" v-if="formData.target === 'group'">
          <text class="form-label">选择分组</text>
          <picker mode="selector" :range="groupList" @change="onGroupChange">
            <view class="picker-input">
              {{ selectedGroup || '请选择分组' }}
            </view>
          </picker>
        </view>

        <view class="form-item required">
          <text class="form-label">通知内容</text>
          <textarea
            v-model="formData.content"
            class="form-textarea"
            placeholder="请输入通知内容..."
            :maxlength="1000"
          ></textarea>
          <view class="input-count">
            <text>{{ formData.content.length }}/1000</text>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">附加链接（可选）</text>
          <input
            v-model="formData.link"
            class="form-input"
            placeholder="输入跳转链接"
          />
        </view>

        <view class="form-item">
          <view class="switch-row">
            <text class="form-label">置顶显示</text>
            <switch
              :checked="formData.isTop"
              @change="toggleTop"
              color="#667eea"
            />
          </view>
        </view>
      </view>

      <!-- 预览 -->
      <view class="preview-section card" v-if="formData.title || formData.content">
        <view class="section-title">预览</view>
        <view class="notice-preview-mobile">
          <view class="preview-badge" v-if="formData.isTop">
            <text class="badge-text">置顶</text>
          </view>
          <text class="preview-type">{{ getTargetLabel() }}</text>
          <text class="preview-time">刚刚</text>
          <text class="preview-title">{{ formData.title || '通知标题' }}</text>
          <text class="preview-content">{{ formData.content || '通知内容...' }}</text>
          <text class="preview-link" v-if="formData.link">🔗 {{ formData.link || '无链接' }}</text>
        </view>
      </view>

      <!-- 发送选项 -->
      <view class="send-section card">
        <view class="section-title">发送选项</view>

        <view class="option-item">
          <view class="switch-row">
            <text class="option-label">立即发送</text>
            <switch
              :checked="sendMode === 'now'"
              @change="switchSendMode"
              color="#667eea"
            />
          </view>
        </view>

        <view class="option-item" v-if="sendMode === 'scheduled'">
          <text class="option-label">定时发送</text>
          <picker
            mode="multiSelector"
            :range="[dateList, timeList]"
            :value="[dateIndex, timeIndex]"
            @change="onDateTimeChange"
          >
            <view class="picker-input">
              {{ formData.sendTime || '选择发送时间' }}
            </view>
          </picker>
        </view>

        <view class="form-item" v-if="sendMode === 'scheduled'">
          <text class="option-label">备注说明</text>
          <textarea
            v-model="formData.remark"
            class="form-textarea"
            placeholder="定时发送的备注说明（可选）"
            :maxlength="200"
          ></textarea>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <button
          class="btn btn-outline btn-block"
          @click="sendTest"
        >
          🧪 发送测试
        </button>
        <button
          class="btn btn-primary btn-block"
          @click="publishNotice"
        >
          ✓ 发布通知
        </button>
      </view>
    </scroll-view>

    <!-- 模板选择弹窗 -->
    <view v-if="showTemplateModal" class="modal-overlay" @click="hideTemplateModal">
      <view class="template-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择模板</text>
          <text class="modal-close" @click="hideTemplateModal">✕</text>
        </view>

        <scroll-view class="template-list-scroll" scroll-y>
          <view
            v-for="template in noticeTemplates"
            :key="template.id"
            class="template-detail-item"
            @click="confirmTemplate(template)"
          >
            <view class="template-detail-header">
              <text class="template-detail-icon">{{ template.icon }}</text>
              <text class="template-detail-title">{{ template.label }}</text>
            </view>
            <text class="template-detail-preview">{{ template.preview }}</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      sendMode: 'now',
      dateIndex: 0,
      timeIndex: 0,
      selectedGroup: '',
      showTemplateModal: false,
      conferenceId: '',
      formData: {
        title: '',
        target: 'all',
        content: '',
        link: '',
        isTop: false,
        sendTime: '',
        remark: ''
      },
      noticeTemplates: [],
      groupList: ['第一组', '第二组', '第三组', '第四组', '第五组', '第六组', '第七组', '第八组'],
      dateList: ['今天', '明天', '后天', '自定义'],
      timeList: ['08:00', '09:00', '10:00', '11:00', '12:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00']
    }
  },

  onLoad(options) {
    this.conferenceId = options.conferenceId || uni.getStorageSync('conferenceId') || '2030309010523144194'
    this.loadTemplates()
  },

  methods: {
    async loadTemplates() {
      try {
        const notificationApi = require('@/api/notification').default
        const result = await notificationApi.templateList(this.conferenceId)
        if (result && Array.isArray(result)) {
          this.noticeTemplates = result.map(t => ({
            id: t.id,
            icon: this.getTemplateIcon(t.type),
            label: t.name,
            preview: (t.content || '').substring(0, 30) + '...',
            content: t.content || ''
          }))
        }
      } catch (e) {
        console.error('加载模板失败', e)
      }
    },

    getTemplateIcon(type) {
      const icons = { conference: '📢', registration: '⏰', checkin: '<text class="fa fa-check"></text>', schedule: '<text class="fa fa-calendar-alt"></text>', seat: '👺', bus: '🚌', accommodation: '🏠', full: '<text class="fa fa-edit"></text>', custom: '<text class="fa fa-pen"></text>' }
      return icons[type] || '📢'
    },

    onTargetChange(e) {
      this.formData.target = e.detail.value
    },

    onGroupChange(e) {
      this.selectedGroup = this.groupList[e.detail.value]
    },

    switchSendMode(e) {
      this.sendMode = e.detail.value ? 'scheduled' : 'now'
    },

    onDateTimeChange(e) {
      const date = this.dateList[e.detail.value[0]]
      const time = this.timeList[e.detail.value[1]]
      this.dateIndex = e.detail.value[0]
      this.timeIndex = e.detail.value[1]

      if (date === '自定义') {
        // 使用uni-app内置日期选择器
        uni.showModal({
          title: '自定义时间',
          editable: true,
          placeholderText: '请输入时间 (如: 2025-01-15 09:00)',
          success: (res) => {
            if (res.confirm && res.content) {
              this.formData.sendTime = res.content.trim()
            }
          }
        })
        return
      }

      this.formData.sendTime = `${date} ${time}`
    },

    toggleTop(e) {
      this.formData.isTop = e.detail.value
    },

    getTargetLabel() {
      if (this.formData.target === 'all') return '全体学员'
      if (this.formData.target === 'group') return this.selectedGroup || '指定分组'
      return '接收对象'
    },

    useTemplate(template) {
      this.showTemplateModal = true
    },

    hideTemplateModal() {
      this.showTemplateModal = false
    },

    confirmTemplate(template) {
      this.formData.title = template.label
      this.formData.content = template.content
      this.showTemplateModal = false

      uni.showToast({
        title: '已应用模板',
        icon: 'success'
      })
    },

    async sendTest() {
      if (!this.formData.title || !this.formData.content) {
        uni.showToast({ title: '请先填写标题和内容', icon: 'none' })
        return
      }
      uni.showLoading({ title: '测试发送中...' })
      try {
        const notificationApi = require('@/api/notification').default
        await notificationApi.send({
          conferenceId: this.conferenceId,
          type: this.formData.type || 'custom',
          title: '[TEST] ' + this.formData.title,
          content: this.formData.content,
          channels: ['system'],
          sendTiming: 'now',
          targetType: 'test'
        })
        uni.hideLoading()
        uni.showToast({ title: '测试发送成功', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '测试发送失败', icon: 'none' })
      }
    },

    saveDraft() {
      uni.setStorageSync('mobile_notice_draft', this.formData)
      uni.showToast({
        title: '草稿已保存',
        icon: 'success'
      })
    },

    publishNotice() {
      if (!this.formData.title) {
        uni.showToast({
          title: '请输入通知标题',
          icon: 'none'
        })
        return
      }

      if (!this.formData.content) {
        uni.showToast({
          title: '请输入通知内容',
          icon: 'none'
        })
        return
      }

      if (this.sendMode === 'scheduled' && !this.formData.sendTime) {
        uni.showToast({
          title: '请选择发送时间',
          icon: 'none'
        })
        return
      }

      uni.showModal({
        title: '发布通知',
        content: `确认${this.sendMode === 'now' ? '立即发布' : this.formData.sendTime + '发送'}此通知？`,
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '发送中...' })
            try {
              const notificationApi = require('@/api/notification').default
              await notificationApi.send({
                conferenceId: this.conferenceId,
                type: 'custom',
                title: this.formData.title,
                content: this.formData.content,
                channels: ['system', 'sms'],
                sendTiming: this.sendMode === 'now' ? 'now' : 'scheduled',
                scheduledTime: this.formData.sendTime || ''
              })
              uni.hideLoading()
              uni.removeStorageSync('mobile_notice_draft')
              uni.showToast({ title: '发送成功', icon: 'success' })
              setTimeout(() => { uni.navigateBack() }, 1500)
            } catch (e) {
              uni.hideLoading()
              uni.showToast({ title: '发送失败', icon: 'none' })
            }
          }
        }
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

.mobile-notice-container {
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

.form-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.template-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.template-scroll {
  width: 100%;
  white-space: nowrap;
}

.template-list {
  display: inline-flex;
  padding: 0 $spacing-md;
  gap: $spacing-md;
}

.template-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  flex-shrink: 0;
}

.template-icon {
  font-size: 32rpx;
}

.template-label {
  font-size: $font-size-sm;
  color: $text-primary;
}

.form-section {
  margin-bottom: $spacing-md;
}

.form-item {
  margin-bottom: $spacing-lg;
}

.form-item.required .form-label::after {
  content: '*';
  color: #ef4444;
  margin-left: 4rpx;
}

.form-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.form-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
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

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.option-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
  margin-right: $spacing-xl;
}

.radio-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.picker-input {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  text-align: center;
  font-size: $font-size-md;
  color: $text-primary;
}

.preview-section {
  margin-bottom: $spacing-md;
}

.notice-preview-mobile {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.preview-badge {
  display: inline-block;
  padding: 4rpx 12rpx;
  background: #ef4444;
  color: $text-white;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
  margin-bottom: $spacing-sm;
}

.badge-text {
  font-size: $font-size-xs;
  font-weight: 500;
}

.preview-type {
  display: inline-block;
  padding: 4rpx 12rpx;
  background: $primary-color;
  color: $text-white;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
  margin-right: $spacing-sm;
}

.preview-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
  margin-right: $spacing-sm;
}

.preview-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.preview-content {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: $spacing-xs;
}

.preview-link {
  display: block;
  font-size: $font-size-sm;
  color: $primary-color;
}

.send-section {
  margin-bottom: $spacing-lg;
}

.option-item {
  margin-bottom: $spacing-lg;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
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

.template-modal {
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

.template-list-scroll {
  flex: 1;
  overflow-y: auto;
}

.template-detail-item {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-sm;
}

.template-detail-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-xs;
}

.template-detail-icon {
  font-size: 32rpx;
}

.template-detail-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.template-detail-preview {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.4;
}
</style>
