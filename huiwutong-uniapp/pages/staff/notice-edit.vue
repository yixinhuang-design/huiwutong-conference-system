<template>
  <view class="notice-edit-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">{{ isEdit ? '编辑通知' : '新建通知' }}</text>
        <text class="header-action" @click="saveDraft">存草稿</text>
      </view>
    </view>

    <scroll-view class="form-scroll" scroll-y>
      <!-- 基本信息 -->
      <view class="form-section card">
        <view class="section-title">基本信息</view>

        <view class="form-item required">
          <text class="form-label">通知标题</text>
          <input
            v-model="formData.title"
            class="input-field"
            placeholder="请输入通知标题"
            :maxlength="100"
          />
        </view>

        <view class="form-item required">
          <text class="form-label">通知类型</text>
          <picker mode="selector" :range="noticeTypes" @change="onTypeChange">
            <view class="picker-input">
              {{ formData.type || '请选择通知类型' }}
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">接收对象</text>
          <picker mode="selector" :range="targetOptions" @change="onTargetChange">
            <view class="picker-input">
              {{ formData.target || '请选择接收对象' }}
            </view>
          </picker>
        </view>

        <view class="form-item" v-if="formData.target === 'specific'">
          <text class="form-label">选择学员</text>
          <view class="select-users-btn" @click="selectUsers">
            <text class="btn-text">已选择 {{ selectedUsers.length }} 人</text>
            <text class="btn-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 通知内容 -->
      <view class="form-section card">
        <view class="section-title">通知内容</view>

        <view class="form-item required">
          <text class="form-label">通知内容</text>
          <textarea
            v-model="formData.content"
            class="form-textarea"
            placeholder="请输入通知内容..."
            :maxlength="2000"
          ></textarea>
          <view class="input-count">
            <text>{{ formData.content.length }}/2000</text>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">添加附件（可选）</text>
          <view class="attachment-list">
            <view
              v-for="(file, index) in formData.attachments"
              :key="index"
              class="attachment-item"
            >
              <text class="file-icon">📎</text>
              <text class="file-name">{{ file.name }}</text>
              <text class="file-remove" @click="removeAttachment(index)">✕</text>
            </view>
            <view class="add-attachment-btn" @click="addAttachment">
              <text class="add-icon">+</text>
              <text class="add-text">添加附件</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 发送设置 -->
      <view class="form-section card">
        <view class="section-title">发送设置</view>

        <view class="form-item">
          <view class="switch-row">
            <text class="form-label">定时发送</text>
            <switch
              :checked="formData.scheduled"
              @change="toggleSchedule"
              color="#667eea"
            />
          </view>
        </view>

        <view class="form-item" v-if="formData.scheduled">
          <text class="form-label">发送时间</text>
          <picker
            mode="multiSelector"
            :range="[dateList, timeList]"
            :value="[dateIndex, timeIndex]"
            @change="onDateTimeChange"
          >
            <view class="picker-input">
              {{ formData.sendTime || '请选择发送时间' }}
            </view>
          </picker>
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
        <view class="notice-preview">
          <view class="preview-header">
            <text class="preview-type">{{ getTypeLabel(formData.type) }}</text>
            <text class="preview-time">刚刚</text>
          </view>
          <text class="preview-title">{{ formData.title || '通知标题' }}</text>
          <text class="preview-content">{{ formData.content || '通知内容...' }}</text>
          <view class="preview-attachments" v-if="formData.attachments.length > 0">
            <text class="attachments-label">附件：{{ formData.attachments.length }}个</text>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button
          class="btn btn-outline btn-block"
          @click="preview"
          v-if="!formData.title && !formData.content"
        >
          👁️ 预览效果
        </button>
        <button
          class="btn btn-outline btn-block"
          @click="sendTest"
        >
          🧪 发送测试
        </button>
        <button class="btn btn-primary btn-block" @click="submit">
          ✓ {{ formData.scheduled ? '定时发送' : '立即发送' }}
        </button>
      </view>
    </scroll-view>

    <!-- 选择学员弹窗 -->
    <view v-if="showUsersModal" class="modal-overlay" @click="hideUsersModal">
      <view class="users-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择接收学员</text>
          <text class="modal-close" @click="hideUsersModal">✕</text>
        </view>

        <view class="search-bar">
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索学员姓名或单位"
            @input="searchUsers"
          />
        </view>

        <view class="select-all-bar">
          <checkbox-group @change="selectAll">
            <label class="select-all-item">
              <checkbox :checked="isAllSelected" color="#667eea" />
              <text class="select-all-text">全选</text>
            </label>
          </checkbox-group>
        </view>

        <scroll-view class="users-list" scroll-y>
          <view
            v-for="user in filteredUsers"
            :key="user.id"
            class="user-item"
            @click="toggleUser(user)"
          >
            <checkbox-group>
              <label class="user-checkbox">
                <checkbox
                  :checked="isUserSelected(user.id)"
                  color="#667eea"
                />
              </label>
            </checkbox-group>
            <view class="user-info">
              <text class="user-name">{{ user.name }}</text>
              <text class="user-dept">{{ user.dept }}</text>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <text class="selected-count">已选择 {{ selectedUsers.length }} 人</text>
          <button class="btn btn-primary" @click="confirmUsers">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import notificationApi from '@/api/notification'
import registrationApi from '@/api/registration'

export default {
  data() {
    return {
      isEdit: false,
      noticeId: '',
      conferenceId: '',
      showUsersModal: false,
      searchKeyword: '',
      dateIndex: 0,
      timeIndex: 0,
      selectedUsers: [],
      noticeTypes: ['培训通知', '日程提醒', '资料更新', '活动通知', '其他'],
      targetOptions: ['全体学员', '特定学员', '工作人员'],
      dateList: ['今天', '明天', '后天'],
      timeList: ['08:00', '09:00', '10:00', '11:00', '12:00', '14:00', '15:00', '16:00', '17:00', '18:00'],
      formData: {
        title: '',
        type: '',
        target: '全体学员',
        content: '',
        attachments: [],
        scheduled: false,
        sendTime: '',
        isTop: false
      },
      allUsers: [],
      usersLoading: false
    }
  },

  computed: {
    filteredUsers() {
      if (!this.searchKeyword) return this.allUsers

      return this.allUsers.filter(user =>
        user.name.includes(this.searchKeyword) ||
        user.dept.includes(this.searchKeyword)
      )
    },

    isAllSelected() {
      return this.filteredUsers.length > 0 &&
        this.filteredUsers.every(user => this.isUserSelected(user.id))
    }
  },

  onLoad(options) {
    this.conferenceId = options.conferenceId || uni.getStorageSync('conferenceId') || '2030309010523144194'
    this.loadUsers()
    if (options.id) {
      this.noticeId = options.id
      this.isEdit = true
      this.loadNotice()
    }
  },

  methods: {
    onTypeChange(e) {
      this.formData.type = this.noticeTypes[e.detail.value]
    },

    onTargetChange(e) {
      this.formData.target = this.targetOptions[e.detail.value]
    },

    getTypeLabel(type) {
      return type || '通知'
    },

    toggleSchedule(e) {
      this.formData.scheduled = e.detail.value
    },

    toggleTop(e) {
      this.formData.isTop = e.detail.value
    },

    onDateTimeChange(e) {
      const date = this.dateList[e.detail.value[0]]
      const time = this.timeList[e.detail.value[1]]
      this.dateIndex = e.detail.value[0]
      this.timeIndex = e.detail.value[1]
      this.formData.sendTime = `${date} ${time}`
    },

    addAttachment() {
      uni.chooseMessageFile({
        count: 5,
        success: (res) => {
          this.formData.attachments.push(...res.tempFiles)
        }
      })
    },

    removeAttachment(index) {
      this.formData.attachments.splice(index, 1)
    },

    selectUsers() {
      this.showUsersModal = true
    },

    hideUsersModal() {
      this.showUsersModal = false
    },

    searchUsers() {
      // 关键词过滤由 computed filteredUsers 处理
      // 如果列表为空则尝试从后端加载
      if (this.allUsers.length === 0 && !this.usersLoading) {
        this.loadUsers()
      }
    },

    async loadUsers() {
      this.usersLoading = true
      try {
        const result = await registrationApi.list({
          conferenceId: this.conferenceId,
          page: 1,
          size: 200
        })
        const records = result.records || result.list || (Array.isArray(result) ? result : [])
        this.allUsers = records.map(r => ({
          id: r.id || r.userId,
          name: r.realName || r.name || r.username || '未知',
          dept: r.organization || r.department || r.unit || ''
        }))
      } catch (e) {
        console.error('加载用户列表失败', e)
        // 降级：保留空列表，用户可手动输入
      } finally {
        this.usersLoading = false
      }
    },

    selectAll(e) {
      const checked = e.detail.value.length > 0
      if (checked) {
        this.filteredUsers.forEach(user => {
          if (!this.isUserSelected(user.id)) {
            this.selectedUsers.push(user)
          }
        })
      } else {
        this.selectedUsers = []
      }
    },

    toggleUser(user) {
      if (this.isUserSelected(user.id)) {
        const index = this.selectedUsers.findIndex(u => u.id === user.id)
        if (index > -1) {
          this.selectedUsers.splice(index, 1)
        }
      } else {
        this.selectedUsers.push(user)
      }
    },

    isUserSelected(userId) {
      return this.selectedUsers.some(u => u.id === userId)
    },

    confirmUsers() {
      this.hideUsersModal()
    },

    preview() {
      if (!this.formData.title && !this.formData.content) {
        uni.showToast({ title: '请先填写通知内容', icon: 'none' })
        return
      }
      uni.showModal({
        title: this.formData.title || '通知预览',
        content: this.formData.content || '（无内容）',
        showCancel: false,
        confirmText: '关闭'
      })
    },

    async sendTest() {
      if (!this.formData.title || !this.formData.content) {
        uni.showToast({ title: '请先填写标题和内容', icon: 'none' })
        return
      }
      uni.showLoading({ title: '测试发送中...' })
      try {
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

    async saveDraft() {
      try {
        await notificationApi.saveDraft({
          conferenceId: this.conferenceId,
          type: 'custom',
          title: this.formData.title,
          content: this.formData.content,
          channels: ['system'],
          sendTiming: 'now'
        })
        uni.showToast({ title: '草稿已保存', icon: 'success' })
      } catch (e) {
        // 降级保存到本地
        uni.setStorageSync('notice_draft', this.formData)
        uni.showToast({ title: '已保存到本地', icon: 'success' })
      }
    },

    submit() {
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

      uni.showModal({
        title: this.formData.scheduled ? '定时发送' : '立即发送',
        content: `确认${this.formData.scheduled ? this.formData.sendTime + '发送' : '立即发送'}此通知？`,
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '发送中...' })
            try {
              await notificationApi.send({
                conferenceId: this.conferenceId,
                type: this.formData.type || 'custom',
                title: this.formData.title,
                content: this.formData.content,
                channels: ['system', 'sms'],
                sendTiming: this.formData.scheduled ? 'scheduled' : 'now',
                scheduledTime: this.formData.sendTime || ''
              })
              uni.hideLoading()
              uni.removeStorageSync('notice_draft')
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

    async loadNotice() {
      uni.showLoading({ title: '加载中...' })
      try {
        const result = await notificationApi.detail(this.noticeId)
        if (result) {
          this.formData.title = result.title || ''
          this.formData.content = result.content || ''
          this.formData.type = result.type || ''
        }
      } catch (e) {
        console.error('加载通知失败', e)
      } finally {
        uni.hideLoading()
      }
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

.notice-edit-container {
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

.form-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
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

.picker-input {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  text-align: center;
  font-size: $font-size-md;
  color: $text-primary;
}

.select-users-btn {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.btn-text {
  font-size: $font-size-md;
  color: $text-primary;
}

.btn-arrow {
  font-size: 32rpx;
  color: $text-tertiary;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.attachment-item {
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
  font-size: $font-size-lg;
  color: #ef4444;
  padding: 0 $spacing-sm;
}

.add-attachment-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: rgba(102, 126, 234, 0.05);
  border: 1rpx dashed $primary-color;
  border-radius: $border-radius-md;
}

.add-icon {
  font-size: 32rpx;
  color: $primary-color;
}

.add-text {
  font-size: $font-size-sm;
  color: $primary-color;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-section {
  margin-bottom: $spacing-md;
}

.notice-preview {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.preview-type {
  padding: 4rpx 12rpx;
  background: $primary-color;
  color: $text-white;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.preview-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.preview-title {
  display: block;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.preview-content {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
  white-space: pre-wrap;
}

.preview-attachments {
  margin-top: $spacing-sm;
}

.attachments-label {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.submit-section {
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
  align-items: flex-end;
}

.users-modal {
  width: 100%;
  height: 80vh;
  background: $bg-primary;
  border-radius: $border-radius-lg $border-radius-lg 0 0;
  padding: $spacing-lg;
  padding-bottom: calc(#{$spacing-lg} + constant(safe-area-inset-bottom));
  padding-bottom: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
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

.search-bar {
  margin-bottom: $spacing-md;
}

.search-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
}

.select-all-bar {
  padding: $spacing-sm 0;
  margin-bottom: $spacing-sm;
}

.select-all-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.select-all-text {
  font-size: $font-size-md;
  color: $text-primary;
}

.users-list {
  flex: 1;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-sm;
}

.user-checkbox {
  flex-shrink: 0;
}

.user-info {
  flex: 1;
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

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color;
}

.selected-count {
  font-size: $font-size-md;
  color: $text-secondary;
}
</style>
