<template>
  <view class="database-backup-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">数据备份</text>
        <text class="header-action" @click="createBackup">+ 新建备份</text>
      </view>
    </view>

    <!-- 备份统计 -->
    <view class="stats-section">
      <view class="stat-box">
        <text class="stat-value">{{ stats.totalBackups }}</text>
        <text class="stat-label">总备份数</text>
      </view>
      <view class="stat-box">
        <text class="stat-value">{{ stats.totalSize }}</text>
        <text class="stat-label">总占用空间</text>
      </view>
      <view class="stat-box">
        <text class="stat-value">{{ stats.lastBackup }}</text>
        <text class="stat-label">最后备份时间</text>
      </view>
    </view>

    <!-- 自动备份设置 -->
    <view class="auto-backup-section card">
      <view class="section-title">自动备份设置</view>

      <view class="setting-item">
        <view class="switch-row">
          <view class="switch-info">
            <text class="setting-label">启用自动备份</text>
            <text class="setting-desc">系统将按照设定自动执行备份</text>
          </view>
          <switch
            :checked="autoBackup.enabled"
            @change="toggleAutoBackup"
            color="#667eea"
          />
        </view>
      </view>

      <view v-if="autoBackup.enabled" class="backup-config">
        <view class="config-item">
          <text class="config-label">备份频率</text>
          <radio-group @change="onFrequencyChange">
            <label class="radio-item">
              <radio value="daily" :checked="autoBackup.frequency === 'daily'" color="#667eea" />
              <text class="radio-label">每天</text>
            </label>
            <label class="radio-item">
              <radio value="weekly" :checked="autoBackup.frequency === 'weekly'" color="#667eea" />
              <text class="radio-label">每周</text>
            </label>
            <label class="radio-item">
              <radio value="monthly" :checked="autoBackup.frequency === 'monthly'" color="#667eea" />
              <text class="radio-label">每月</text>
            </label>
          </radio-group>
        </view>

        <view class="config-item">
          <text class="config-label">备份时间</text>
          <picker mode="time" :value="autoBackup.time" @change="onTimeChange">
            <view class="picker-input">
              {{ autoBackup.time }}
            </view>
          </picker>
        </view>

        <view class="config-item">
          <text class="config-label">保留数量</text>
          <input
            v-model="autoBackup.retention"
            class="config-input"
            type="number"
            placeholder="保留的备份文件数量"
          />
        </view>
      </view>
    </view>

    <!-- 备份列表 -->
    <scroll-view class="backup-list-scroll" scroll-y>
      <view class="backup-list-section">
        <view class="section-header">
          <text class="section-title">备份记录</text>
          <view class="filter-tabs">
            <text
              class="filter-tab"
              :class="{ active: activeTab === 'all' }"
              @click="switchTab('all')"
            >
              全部
            </text>
            <text
              class="filter-tab"
              :class="{ active: activeTab === 'auto' }"
              @click="switchTab('auto')"
            >
              自动
            </text>
            <text
              class="filter-tab"
              :class="{ active: activeTab === 'manual' }"
              @click="switchTab('manual')"
            >
              手动
            </text>
          </view>
        </view>

        <view class="backup-list">
          <view
            v-for="backup in filteredBackups"
            :key="backup.id"
            class="backup-item card"
          >
            <view class="backup-header">
              <view class="backup-info">
                <text class="backup-icon">💾</text>
                <view class="backup-details">
                  <text class="backup-name">{{ backup.name }}</text>
                  <view class="backup-meta">
                    <text
                      class="backup-type"
                      :class="backup.type"
                    >
                      {{ backup.type === 'auto' ? '自动' : '手动' }}
                    </text>
                    <text class="backup-time">{{ backup.createTime }}</text>
                  </view>
                </view>
              </view>
              <view class="backup-status">
                <text class="status-badge" :class="backup.status">
                  {{ getStatusLabel(backup.status) }}
                </text>
              </view>
            </view>

            <view class="backup-stats">
              <view class="stat-item">
                <text class="stat-label">文件大小：</text>
                <text class="stat-value">{{ backup.size }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-label">数据表：</text>
                <text class="stat-value">{{ backup.tables }}个</text>
              </view>
              <view class="stat-item">
                <text class="stat-label">记录数：</text>
                <text class="stat-value">{{ backup.records }}条</text>
              </view>
            </view>

            <view class="backup-actions" v-if="backup.status === 'completed'">
              <button class="btn btn-sm btn-outline" @click="downloadBackup(backup)">
                ⬇️ 下载
              </button>
              <button class="btn btn-sm btn-outline" @click="restoreBackup(backup)">
                🔄 恢复
              </button>
              <button class="btn btn-sm btn-danger" @click="deleteBackup(backup)">
                🗑️ 删除
              </button>
            </view>

            <view class="backup-progress" v-if="backup.status === 'processing'">
              <view class="progress-info">
                <text class="progress-text">备份中... {{ backup.progress }}%</text>
              </view>
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: backup.progress + '%' }"
                ></view>
              </view>
            </view>

            <view class="backup-error" v-if="backup.status === 'failed'">
              <text class="error-text">{{ backup.error }}</text>
              <button class="btn btn-sm btn-outline" @click="retryBackup(backup)">
                🔄 重试
              </button>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 恢复确认弹窗 -->
    <view v-if="showRestoreModal" class="modal-overlay" @click="hideRestoreModal">
      <view class="restore-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">恢复备份</text>
          <text class="modal-close" @click="hideRestoreModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="warning-box">
            <text class="warning-icon">⚠️</text>
            <view class="warning-content">
              <text class="warning-title">危险操作</text>
              <text class="warning-text">恢复备份将覆盖当前数据，此操作不可撤销！请确保已做好当前数据备份。</text>
            </view>
          </view>

          <view class="restore-info">
            <view class="info-row">
              <text class="info-label">备份文件：</text>
              <text class="info-value">{{ selectedBackup?.name }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">备份时间：</text>
              <text class="info-value">{{ selectedBackup?.createTime }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">文件大小：</text>
              <text class="info-value">{{ selectedBackup?.size }}</text>
            </view>
          </view>

          <view class="confirm-check">
            <checkbox-group @change="onConfirmCheck">
              <label class="checkbox-item">
                <checkbox :checked="confirmRestore" color="#667eea" />
                <text class="checkbox-label">我已了解风险，确认恢复此备份</text>
              </label>
            </checkbox-group>
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn btn-outline" @click="hideRestoreModal">取消</button>
          <button
            class="btn btn-primary"
            :disabled="!confirmRestore"
            @click="confirmRestoreBackup"
          >
            确认恢复
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      activeTab: 'all',
      showRestoreModal: false,
      selectedBackup: null,
      confirmRestore: false,
      stats: {
        totalBackups: 28,
        totalSize: '156.8GB',
        lastBackup: '2小时前'
      },
      autoBackup: {
        enabled: true,
        frequency: 'daily',
        time: '02:00',
        retention: 7
      },
      backupList: [
        {
          id: 1,
          name: 'auto_backup_20250116_020000',
          type: 'auto',
          status: 'completed',
          createTime: '2025-01-16 02:00',
          size: '5.2GB',
          tables: 45,
          records: 156890
        },
        {
          id: 2,
          name: 'manual_backup_20250115_143000',
          type: 'manual',
          status: 'completed',
          createTime: '2025-01-15 14:30',
          size: '5.1GB',
          tables: 45,
          records: 156520
        },
        {
          id: 3,
          name: 'auto_backup_20250115_020000',
          type: 'auto',
          status: 'completed',
          createTime: '2025-01-15 02:00',
          size: '5.1GB',
          tables: 45,
          records: 156200
        },
        {
          id: 4,
          name: 'auto_backup_20250114_020000',
          type: 'auto',
          status: 'failed',
          createTime: '2025-01-14 02:00',
          size: '-',
          tables: '-',
          records: '-',
          error: '备份失败：存储空间不足'
        }
      ]
    }
  },

  computed: {
    filteredBackups() {
      if (this.activeTab === 'all') {
        return this.backupList
      }
      return this.backupList.filter(backup => backup.type === this.activeTab)
    }
  },

  methods: {
    getStatusLabel(status) {
      const labelMap = {
        completed: '完成',
        processing: '备份中',
        failed: '失败'
      }
      return labelMap[status] || status
    },

    toggleAutoBackup(e) {
      this.autoBackup.enabled = e.detail.value
    },

    onFrequencyChange(e) {
      this.autoBackup.frequency = e.detail.value
    },

    onTimeChange(e) {
      this.autoBackup.time = e.detail.value
    },

    switchTab(tab) {
      this.activeTab = tab
    },

    createBackup() {
      uni.showModal({
        title: '创建备份',
        content: '确定要立即创建数据备份吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '创建中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '备份任务已创建',
                icon: 'success'
              })

              // 模拟添加一个备份中任务
              this.backupList.unshift({
                id: Date.now(),
                name: `manual_backup_${new Date().toISOString().replace(/[:.]/g, '')}`,
                type: 'manual',
                status: 'processing',
                createTime: new Date().toLocaleString(),
                size: '-',
                tables: '-',
                records: '-',
                progress: 0
              })
            }, 1000)
          }
        }
      })
    },

    downloadBackup(backup) {
      uni.showLoading({ title: '准备下载...' })

      setTimeout(() => {
        uni.hideLoading()

        uni.showToast({
          title: '下载开始',
          icon: 'success'
        })
      }, 1000)
    },

    restoreBackup(backup) {
      this.selectedBackup = backup
      this.confirmRestore = false
      this.showRestoreModal = true
    },

    confirmRestoreCheck() {
      // 已在模板中处理
    },

    onConfirmCheck(e) {
      this.confirmRestore = e.detail.value.length > 0
    },

    confirmRestoreBackup() {
      if (!this.confirmRestore) return

      uni.showLoading({ title: '恢复中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hideRestoreModal()

        uni.showToast({
          title: '恢复成功',
          icon: 'success'
        })
      }, 2000)
    },

    hideRestoreModal() {
      this.showRestoreModal = false
    },

    deleteBackup(backup) {
      uni.showModal({
        title: '删除备份',
        content: `确定要删除备份"${backup.name}"吗？`,
        confirmColor: '#ef4444',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '删除中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '删除成功',
                icon: 'success'
              })
            }, 1000)
          }
        }
      })
    },

    retryBackup(backup) {
      uni.showLoading({ title: '重试中...' })

      setTimeout(() => {
        uni.hideLoading()

        uni.showToast({
          title: '重试任务已创建',
          icon: 'success'
        })

        backup.status = 'processing'
        backup.progress = 0
      }, 1000)
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

.database-backup-container {
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

.stats-section {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md;
}

.stat-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-lg $spacing-md;
  background: $bg-primary;
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

.auto-backup-section {
  margin: 0 $spacing-md $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.setting-item {
  margin-bottom: $spacing-lg;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-info {
  flex: 1;
}

.setting-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.setting-desc {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
  margin-top: 4rpx;
}

.backup-config {
  padding: $spacing-md;
  margin-top: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.config-item {
  margin-bottom: $spacing-lg;
}

.config-item:last-child {
  margin-bottom: 0;
}

.config-label {
  display: block;
  font-size: $font-size-md;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
}

.config-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
}

.picker-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
  text-align: center;
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

.backup-list-scroll {
  height: calc(100vh - 560rpx);
}

.backup-list-section {
  padding: 0 $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.filter-tabs {
  display: flex;
  gap: $spacing-sm;
}

.filter-tab {
  padding: $spacing-xs $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.filter-tab.active {
  background: $primary-color;
  color: $text-white;
}

.backup-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.backup-item {
  margin-bottom: $spacing-md;
}

.backup-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.backup-info {
  flex: 1;
  display: flex;
  gap: $spacing-md;
}

.backup-icon {
  font-size: 32rpx;
}

.backup-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.backup-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.backup-meta {
  display: flex;
  gap: $spacing-md;
}

.backup-type {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
}

.backup-type.auto {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.backup-type.manual {
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
}

.backup-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.backup-status {
  flex-shrink: 0;
}

.status-badge {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
}

.status-badge.completed {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.status-badge.processing {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.status-badge.failed {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.backup-stats {
  display: flex;
  gap: $spacing-lg;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.stat-item {
  display: flex;
  gap: $spacing-xs;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.stat-value {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: 500;
}

.backup-actions {
  display: flex;
  gap: $spacing-sm;
}

.backup-progress {
  margin-top: $spacing-md;
}

.progress-info {
  margin-bottom: $spacing-sm;
}

.progress-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.progress-bar {
  height: 8rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.backup-error {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: rgba(239, 68, 68, 0.1);
  border-radius: $border-radius-md;
}

.error-text {
  flex: 1;
  font-size: $font-size-sm;
  color: #ef4444;
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

.restore-modal {
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

.warning-box {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md;
  background: rgba(245, 158, 11, 0.1);
  border-radius: $border-radius-md;
  margin-bottom: $spacing-lg;
}

.warning-icon {
  font-size: 32rpx;
}

.warning-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.warning-title {
  font-size: $font-size-md;
  font-weight: 600;
  color: #f59e0b;
}

.warning-text {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.5;
}

.restore-info {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-lg;
}

.info-row {
  display: flex;
  margin-bottom: $spacing-sm;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 140rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.info-value {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
}

.confirm-check {
  padding: $spacing-md;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.checkbox-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.modal-footer {
  display: flex;
  gap: $spacing-md;
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
}

.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}
</style>
