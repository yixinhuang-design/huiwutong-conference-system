<template>
  <view class="alert-handle-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">预警处理</text>
        <text class="header-action" @click="refresh">刷新</text>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <view
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <scroll-view class="alerts-scroll" scroll-y @scrolltolower="loadMore">
      <!-- 预警列表 -->
      <view class="alerts-list">
        <view
          v-for="alert in filteredAlerts"
          :key="alert.id"
          class="alert-card card"
          :class="'priority-' + alert.priority"
        >
          <view class="alert-header">
            <view class="alert-left">
              <text class="alert-icon" :class="'icon-' + alert.type">
                {{ getAlertIcon(alert.type) }}
              </text>
              <view class="alert-title-info">
                <text class="alert-title">{{ alert.title }}</text>
                <text class="alert-type">{{ getTypeLabel(alert.type) }}</text>
              </view>
            </view>
            <view class="alert-status" :class="'status-' + alert.status">
              {{ getStatusLabel(alert.status) }}
            </view>
          </view>

          <view class="alert-content">
            <text class="alert-desc">{{ alert.description }}</text>
          </view>

          <view class="alert-details">
            <view class="detail-item">
              <text class="detail-label">触发时间</text>
              <text class="detail-value">{{ alert.triggerTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">涉及对象</text>
              <text class="detail-value">{{ alert.target }}</text>
            </view>
            <view class="detail-item" v-if="alert.value">
              <text class="detail-label">当前值</text>
              <text class="detail-value warning">{{ alert.value }}</text>
            </view>
          </view>

          <view class="alert-actions" v-if="alert.status === 'pending'">
            <button
              class="btn btn-sm btn-outline"
              @click="ignoreAlert(alert)"
            >
              忽略
            </button>
            <button
              class="btn btn-sm btn-primary"
              @click="handleAlert(alert)"
            >
              立即处理
            </button>
          </view>

          <view class="alert-resolution" v-if="alert.status !== 'pending'">
            <view class="resolution-info">
              <text class="resolution-label">处理人：{{ alert.handler }}</text>
              <text class="resolution-time">{{ alert.handleTime }}</text>
            </view>
            <view class="resolution-note" v-if="alert.note">
              <text class="note-label">处理说明：</text>
              <text class="note-text">{{ alert.note }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && filteredAlerts.length > 0" class="no-more-text">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import alertApi from '@/api/alert'

export default {
  data() {
    return {
      conferenceId: '',
      activeTab: 'pending',
      loading: false,
      hasMore: true,
      currentPage: 1,
      filterTabs: [
        { label: '待处理', value: 'pending' },
        { label: '处理中', value: 'processing' },
        { label: '已完成', value: 'resolved' }
      ],
      alerts: []
    }
  },

  onLoad(options) {
    this.conferenceId = options.conferenceId || uni.getStorageSync('conferenceId') || '2030309010523144194'
    this.loadAlerts()
  },

  computed: {
    filteredAlerts() {
      if (this.activeTab === 'pending') {
        return this.alerts.filter(a => a.status === 'pending')
      } else if (this.activeTab === 'processing') {
        return this.alerts.filter(a => a.status === 'processing')
      } else {
        return this.alerts.filter(a => a.status === 'resolved')
      }
    }
  },

  methods: {
    async loadAlerts() {
      try {
        this.loading = true
        const result = await alertApi.list({ conferenceId: this.conferenceId })
        const list = Array.isArray(result) ? result : (result.records || [])
        this.alerts = list.map(item => ({
          id: item.id,
          type: item.metric || 'other',
          priority: item.severity || 'medium',
          status: item.status || 'pending',
          title: `${item.metric}预警`,
          description: `当前值 ${item.metricValue}，阈值 ${item.threshold}`,
          triggerTime: item.createTime,
          target: item.conferenceId,
          value: `${item.metricValue} / ${item.threshold}`,
          handler: item.handlerName,
          handleTime: item.handleTime,
          note: item.remark
        }))
      } catch (e) {
        console.error('加载预警列表失败', e)
      } finally {
        this.loading = false
      }
    },

    switchTab(tab) {
      this.activeTab = tab
    },

    getAlertIcon(type) {
      const iconMap = {
        checkin: '<text class="fa fa-map-marker-alt"></text>',
        registration: '<text class="fa fa-edit"></text>',
        task: '<text class="fa fa-clipboard"></text>',
        satisfaction: '<text class="fa fa-star"></text>',
        seat: '<text class="fa fa-th-large"></text>',
        equipment: '🔧'
      }
      return iconMap[type] || '<text class="fa fa-exclamation-triangle"></text>'
    },

    getTypeLabel(type) {
      const labelMap = {
        checkin: '签到预警',
        registration: '报名预警',
        task: '任务预警',
        satisfaction: '满意度预警',
        seat: '座位预警',
        equipment: '设备预警'
      }
      return labelMap[type] || '其他预警'
    },

    getStatusLabel(status) {
      const labelMap = {
        pending: '待处理',
        processing: '处理中',
        resolved: '已完成'
      }
      return labelMap[status] || '未知'
    },

    ignoreAlert(alert) {
      uni.showModal({
        title: '忽略预警',
        content: '确认忽略此预警？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await alertApi.resolve(alert.id, { remark: '已忽略' })
              alert.status = 'resolved'
              alert.note = '已忽略'
              uni.showToast({ title: '已忽略', icon: 'success' })
            } catch (e) {
              uni.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      })
    },

    handleAlert(alert) {
      uni.showModal({
        title: '处理预警',
        content: '确认开始处理此预警？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await alertApi.process(alert.id)
              alert.status = 'processing'
              alert.handleTime = this.getCurrentTime()
              uni.showToast({ title: '标记为处理中', icon: 'success' })
            } catch (e) {
              uni.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      })
    },

    async refresh() {
      uni.showLoading({ title: '刷新中...' })
      await this.loadAlerts()
      uni.hideLoading()
      uni.showToast({ title: '刷新成功', icon: 'success' })
    },

    async loadMore() {
      if (this.loading || !this.hasMore) return
      this.currentPage++
      await this.loadAlerts()
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
@import '../../styles/global-patch.scss';

.alert-handle-container {
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

.filter-tabs {
  display: flex;
  background: $bg-primary;
  padding: $spacing-sm $spacing-md;
  margin-bottom: $spacing-sm;
}

.filter-tab {
  flex: 1;
  text-align: center;
  padding: $spacing-sm $spacing-md;
  font-size: $font-size-md;
  color: $text-secondary;
  border-radius: $border-radius-md;
  transition: all 0.3s;
}

.filter-tab.active {
  background: $primary-color;
  color: $text-white;
  font-weight: 500;
}

.alerts-scroll {
  height: calc(100vh - 200rpx);
  padding: $spacing-md;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.alert-card {
  margin-bottom: 0;
  border-left: 4rpx solid transparent;
}

.alert-card.priority-high {
  border-left-color: #ef4444;
}

.alert-card.priority-medium {
  border-left-color: #f59e0b;
}

.alert-card.priority-low {
  border-left-color: #10b981;
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.alert-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.alert-icon {
  font-size: 40rpx;
}

.alert-title-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.alert-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.alert-type {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.alert-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.alert-status.status-pending {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.alert-status.status-processing {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.alert-status.status-resolved {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.alert-content {
  margin-bottom: $spacing-md;
}

.alert-desc {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
}

.alert-details {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.detail-value {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.detail-value.warning {
  color: #ef4444;
  font-weight: 600;
}

.alert-actions {
  display: flex;
  gap: $spacing-sm;
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
}

.alert-resolution {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: rgba(16, 185, 129, 0.05);
  border-radius: $border-radius-md;
}

.resolution-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resolution-label,
.resolution-time {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.resolution-note {
  display: flex;
  gap: $spacing-xs;
}

.note-label {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.note-text {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.6;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-lg;
  color: $text-tertiary;
  font-size: $font-size-sm;
}
</style>
