<template>
  <view class="notifications-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">通知中心</text>
        <text class="header-action" @click="markAllRead">全部已读</text>
      </view>
    </view>

    <!-- 筛选Tab -->
    <view class="tab-bar">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        {{ tab.name }}
        <text v-if="tab.badge > 0" class="tab-badge">{{ tab.badge }}</text>
      </view>
    </view>

    <!-- 通知列表 -->
    <view class="notification-list">
      <view v-if="notificationList.length > 0">
        <view
          v-for="(notification, index) in notificationList"
          :key="index"
          class="notification-item list-item"
          :class="{ unread: !notification.read }"
          @click="viewDetail(notification)"
        >
          <view class="notification-icon" :style="{ background: notification.color }">
            <text>{{ notification.icon }}</text>
          </view>
          <view class="notification-info">
            <view class="notification-top">
              <text class="notification-title">{{ notification.title }}</text>
              <text class="notification-time">{{ notification.time }}</text>
            </view>
            <view class="notification-content">
              {{ notification.content }}
            </view>
          </view>
          <view v-if="!notification.read" class="unread-dot"></view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon"><text class="fa fa-bell"></text></text>
        <text class="empty-text">暂无通知</text>
      </view>
    </view>
  </view>
</template>

<script>
import notificationApi from '@/api/notification'

export default {
  data() {
    return {
      conferenceId: '',
      allNotifications: [],
      currentTab: 0,
      tabs: [
        { name: '全部', value: 'all', badge: 0 },
        { name: '系统', value: 'system', badge: 0 },
        { name: '课程', value: 'course', badge: 0 },
        { name: '活动', value: 'activity', badge: 0 }
      ],
      notificationList: []
    }
  },

  onLoad(options) {
    this.conferenceId = options.conferenceId || uni.getStorageSync('conferenceId') || '2030309010523144194'
    this.loadNotifications()
  },

  methods: {
    /**
     * 切换Tab
     */
    switchTab(index) {
      this.currentTab = index
      this.filterNotifications()
    },

    /**
     * 筛选通知
     */
    filterNotifications() {
      const tab = this.tabs[this.currentTab]
      if (tab.value === 'all') {
        this.notificationList = [...this.allNotifications]
      } else {
        this.notificationList = this.allNotifications.filter(
          item => item.type === tab.value
        )
      }
    },

    /**
     * 加载通知列表
     */
    async loadNotifications() {
      try {
        const result = await notificationApi.list({
          conferenceId: this.conferenceId,
          page: 1,
          pageSize: 50
        })
        const records = result.records || result || []
        const iconMap = { conference: '📢', registration: '<text class="fa fa-clock"></text>', checkin: '<text class="fa fa-check"></text>', schedule: '<text class="fa fa-calendar-alt"></text>', seat: '👺', bus: '🚌', accommodation: '🏠', custom: '✉️' }
        const colorMap = { conference: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', registration: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', checkin: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', custom: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
        this.allNotifications = records.map(n => ({
          id: n.id,
          title: n.title,
          content: n.content,
          time: n.sentTime || n.createTime || '',
          icon: iconMap[n.type] || '<text class="fa fa-bell"></text>',
          color: colorMap[n.type] || 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
          type: n.type === 'conference' ? 'system' : (n.type === 'checkin' || n.type === 'schedule' ? 'course' : 'activity'),
          read: n.status === 'read'
        }))
        this.notificationList = [...this.allNotifications]
        this.updateBadge()
      } catch (error) {
        console.error('Load notifications error:', error)
      }
    },

    /**
     * 查看详情（同时调用后端标记已读）
     */
    viewDetail(notification) {
      notification.read = true
      this.updateBadge()

      // 调用后端标记已读API
      try {
        notificationApi.markRead(notification.id, 0)
      } catch (e) {
        console.warn('标记已读失败:', e)
      }

      uni.showModal({
        title: notification.title,
        content: notification.content,
        showCancel: false,
        confirmText: '确定'
      })
    },

    /**
     * 全部标记已读（调用后端API）
     */
    markAllRead() {
      // 调用后端API
      try {
        notificationApi.markAllRead(this.conferenceId, 0)
      } catch (e) {
        console.warn('全部标记已读失败:', e)
      }

      this.allNotifications.forEach(item => {
        item.read = true
      })
      this.notificationList.forEach(item => {
        item.read = true
      })

      this.tabs.forEach(tab => {
        tab.badge = 0
      })

      uni.showToast({
        title: '已全部标记为已读',
        icon: 'success'
      })
    },

    /**
     * 更新徽章
     */
    updateBadge() {
      this.tabs[this.currentTab].badge = this.notificationList.filter(
        item => !item.read
      ).length
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.notifications-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.header-action {
  font-size: $font-size-sm;
  color: $primary-color;
  padding: 0 $spacing-sm;
}

.tab-bar {
  display: flex;
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
  padding: 0 $spacing-md;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  height: 88rpx;
  position: relative;
  font-size: $font-size-md;
  color: $text-secondary;
}

.tab-item.active {
  color: $primary-color;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: $primary-color;
  border-radius: 2rpx;
}

.tab-badge {
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: $danger-color;
  color: $text-white;
  font-size: $font-size-xs;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-list {
  padding: $spacing-md;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-sm;
  transition: $transition-base;
}

.notification-item.unread {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.notification-item:active {
  transform: scale(0.98);
}

.notification-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $border-radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.notification-info {
  flex: 1;
  min-width: 0;
}

.notification-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6rpx;
}

.notification-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
  flex-shrink: 0;
}

.notification-content {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: $danger-color;
  flex-shrink: 0;
  margin-top: 6rpx;
}
</style>
