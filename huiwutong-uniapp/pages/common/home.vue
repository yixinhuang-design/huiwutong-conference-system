<template>
  <view class="home-container">
    <!-- 欢迎区域 -->
    <view class="welcome-card card fade-in">
      <view class="card-header">
        <view class="avatar">
          <text>{{ avatarText }}</text>
        </view>
        <view class="user-info">
          <view class="card-title">{{ greeting }}，{{ realName }}</view>
          <view class="card-subtitle-inline">
            <text>{{ userTypeName }}</text>
            <text class="sep" v-if="organization">｜</text>
            <text v-if="organization">{{ organization }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 今日提醒 -->
    <view v-if="todayReminder" class="info-banner fade-in">
      <view class="info-banner-content">
        <text class="fa fa-bullhorn"></text>
        <text class="info-banner-text">{{ todayReminder }}</text>
      </view>
    </view>

    <!-- 当前会议/培训列表 -->
    <view class="section-title">我参加的培训</view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading-state">
      <text class="fa fa-spinner fa-spin"></text>
      <text class="loading-text">加载中...</text>
    </view>

    <view v-else-if="meetings.length > 0">
      <view
        v-for="(meeting, index) in meetings"
        :key="meeting.id"
        class="meeting-card card fade-in"
        @click="goToMeetingDetail(meeting.id)"
      >
        <view class="meeting-header">
          <view class="meeting-info">
            <view class="meeting-title">{{ meeting.meetingName }}</view>
            <view class="meeting-meta">
              <text class="meta-item">
                <text><text class="fa fa-calendar-alt"></text></text>
                {{ formatDateRange(meeting.startTime, meeting.endTime) }}
              </text>
              <text class="meta-item">
                <text><text class="fa fa-map-marker-alt"></text></text>
                {{ meeting.venueName || '待定' }}
              </text>
              <text class="meta-item" v-if="meeting.currentParticipants != null">
                <text><text class="fa fa-users"></text></text>
                {{ meeting.currentParticipants || 0 }}{{ meeting.maxParticipants ? '/' + meeting.maxParticipants : '' }} 人
              </text>
            </view>
          </view>
          <view
            class="status-chip"
            :class="getStatusClass(meeting.status)"
          >
            <text class="status-dot"></text>
            {{ getStatusText(meeting.status) }}
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-state">
      <text class="empty-icon"><text class="fa fa-clipboard"></text></text>
      <text class="empty-text">暂无培训安排</text>
    </view>

    <!-- 最新通知 -->
    <view class="section-title" v-if="notifications.length > 0">最新通知</view>
    <view v-if="notifications.length > 0" class="notification-list">
      <view
        v-for="(noti, idx) in notifications"
        :key="noti.id || idx"
        class="notification-item card fade-in"
        @click="goToNotificationDetail(noti.id)"
      >
        <view class="noti-header">
          <view class="noti-title-row">
            <text class="noti-type-tag" :class="'noti-type-' + (noti.type || 'system')">{{ getNotiTypeName(noti.type) }}</text>
            <text class="noti-title">{{ noti.title || '系统通知' }}</text>
          </view>
          <text class="noti-status-dot" :class="'noti-st-' + (noti.status || 'sent')"></text>
        </view>
        <view class="noti-content" v-if="noti.content">{{ truncate(noti.content, 60) }}</view>
        <view class="noti-footer">
          <text class="noti-time">
            <text class="fa fa-clock"></text>
            {{ formatTime(noti.sentTime || noti.createTime) }}
          </text>
          <text class="noti-meta" v-if="noti.recipientCount">
            <text class="fa fa-users"></text>
            {{ noti.recipientCount }}人
          </text>
          <text class="noti-meta" v-if="noti.channels">
            <text class="fa fa-paper-plane"></text>
            {{ formatChannels(noti.channels) }}
          </text>
        </view>
      </view>
    </view>

    <!-- 功能导航 -->
    <view class="section-title">功能导航</view>
    <view class="grid-3">
      <view
        v-for="(feature, index) in featureList"
        :key="index"
        class="feature-tile"
        @click="navigateTo(feature.path)"
      >
        <view class="feature-icon-lg" :class="feature.colorClass || 'blue'">
          <text :class="feature.icon"></text>
        </view>
        <view class="feature-title">{{ feature.title }}</view>
      </view>
    </view>

    <!-- 底部安全区域 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script>
import { useUserStore } from '@/store/modules/user'
import { useAppStore } from '@/store/modules/app'
import { meeting as meetingApi, notification as notificationApi } from '@/api/index'

export default {
  data() {
    return {
      loading: false,
      todayReminder: '',
      meetings: [],
      notifications: [],
      featureList: [
        {
          icon: 'fa-book',
          title: '参会须知',
          path: '/pages/learner/guide',
          colorClass: 'purple'
        },
        {
          icon: 'fa-calendar-alt',
          title: '日程安排',
          path: '/pages/learner/schedule',
          colorClass: 'pink'
        },
        {
          icon: 'fa-address-book',
          title: '通讯录',
          path: '/pages/learner/contact',
          colorClass: 'blue'
        },
        {
          icon: 'fa-th-large',
          title: '座位图',
          path: '/pages/learner/seat',
          colorClass: 'green'
        },
        {
          icon: 'fa-check',
          title: '报到签到',
          path: '/pages/learner/checkin',
          colorClass: 'orange'
        },
        {
          icon: 'fa-book-open',
          title: '学习资料',
          path: '/pages/learner/materials',
          colorClass: 'cyan'
        }
      ]
    }
  },

  computed: {
    userStore() {
      return useUserStore()
    },

    realName() {
      return this.userStore.realName || '学员'
    },

    avatarText() {
      const name = this.realName
      return name ? name.charAt(name.length - 1) : '学'
    },

    userType() {
      return this.userStore.userType
    },

    userTypeName() {
      const typeMap = {
        learner: '学员',
        staff: '工作人员'
      }
      return typeMap[this.userType] || '学员'
    },

    organization() {
      return this.userStore.userInfo?.organization || ''
    },

    /** 根据当前时间段显示问候语 */
    greeting() {
      const hour = new Date().getHours()
      if (hour < 6) return '凌晨好'
      if (hour < 9) return '早上好'
      if (hour < 12) return '上午好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    }
  },

  onLoad() {
    this.initPage()
  },

  onShow() {
    // 页面显示时刷新数据
    this.loadMeetings()
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.initPage()
    setTimeout(() => uni.stopPullDownRefresh(), 500)
  },

  methods: {
    /**
     * 初始化页面
     */
    initPage() {
      // 加载会议列表
      this.loadMeetings()

      // 加载最新通知
      this.loadNotifications()

      // 根据用户类型调整功能列表
      this.adjustFeatureList()
    },

    /**
     * 加载会议列表（调用真实后端接口）
     */
    async loadMeetings() {
      this.loading = true
      try {
        // 优先获取进行中的会议
        let ongoingList = []
        try {
          const ongoingRes = await meetingApi.getOngoing()
          ongoingList = this.extractList(ongoingRes)
        } catch (e) {
          console.warn('获取进行中会议失败，回退到全量列表:', e)
        }

        // 获取全部会议列表（分页第1页）
        const listRes = await meetingApi.getList({ pageNum: 1, pageSize: 20 })
        let allMeetings = this.extractList(listRes)

        // 合并去重：进行中的排在最前
        if (ongoingList.length > 0) {
          const ongoingIds = new Set(ongoingList.map(m => String(m.id)))
          const rest = allMeetings.filter(m => !ongoingIds.has(String(m.id)))
          this.meetings = [...ongoingList, ...rest]
        } else {
          this.meetings = allMeetings
        }

        // 设置今日提醒（基于会议数据）
        this.setTodayReminder(ongoingList)
      } catch (error) {
        console.error('加载会议列表失败:', error)
        this.meetings = []
        this.todayReminder = ''
      } finally {
        this.loading = false
      }
    },

    /**
     * 加载最新通知
     */
    async loadNotifications() {
      try {
        const res = await notificationApi.list({ page: 1, pageSize: 5 })
        this.notifications = this.extractList(res)
      } catch (error) {
        console.warn('加载通知列表失败:', error)
        this.notifications = []
      }
    },

    /**
     * 从分页响应中提取数组
     * 兼容多种后端返回格式: { records: [] } / { list: [] } / { data: { records: [] } } / 直接数组
     */
    extractList(res) {
      if (!res) return []
      if (Array.isArray(res)) return res
      if (res.records && Array.isArray(res.records)) return res.records
      if (res.list && Array.isArray(res.list)) return res.list
      if (res.data) {
        if (Array.isArray(res.data)) return res.data
        if (res.data.records) return res.data.records
        if (res.data.list) return res.data.list
      }
      if (res.rows && Array.isArray(res.rows)) return res.rows
      return []
    },

    /**
     * 根据进行中的会议设置今日提醒
     */
    setTodayReminder(ongoingList) {
      if (ongoingList && ongoingList.length > 0) {
        const first = ongoingList[0]
        this.todayReminder = `"${first.meetingName}" 正在进行中，请留意日程安排。`
      } else if (this.meetings.length > 0) {
        // 找到最近即将开始的会议
        const upcoming = this.meetings.find(m => m.status === 0 || m.status === 1)
        if (upcoming) {
          this.todayReminder = `"${upcoming.meetingName}" 即将开始，请做好准备。`
        } else {
          this.todayReminder = ''
        }
      } else {
        this.todayReminder = ''
      }
    },

    /**
     * 调整功能列表
     */
    adjustFeatureList() {
      if (this.userType === 'staff') {
        // 工作人员显示不同功能（使用Font Awesome图标和统一样式）
        this.featureList = [
          {
            icon: 'fa-chart-bar',
            title: '数据看板',
            path: '/pages/staff/dashboard',
            colorClass: 'purple'
          },
          {
            icon: 'fa-tasks',
            title: '任务管理',
            path: '/pages/staff/task-list',
            colorClass: 'blue'
          },
          {
            icon: 'fa-users',
            title: '报名管理',
            path: '/pages/staff/registration-manage',
            colorClass: 'cyan'
          },
          {
            icon: 'fa-th-large',
            title: '座位管理',
            path: '/pages/staff/seat-manage',
            colorClass: 'green'
          },
          {
            icon: 'fa-bullhorn',
            title: '通知管理',
            path: '/pages/staff/notice-manage',
            colorClass: 'orange'
          },
          {
            icon: 'fa-chart-line',
            title: '数据分析',
            path: '/pages/staff/data-analysis',
            colorClass: 'purple'
          }
        ]
      }
    },

    /**
     * 获取状态样式类（后端 status 为 Integer）
     * 0=草稿, 1=报名中, 2=进行中, 3=已结束, 4=已取消
     */
    getStatusClass(status) {
      const classMap = {
        0: 'status-info',
        1: 'status-warn',
        2: 'status-accent',
        3: 'status-success',
        4: 'status-danger',
        // 兼容字符串格式
        'draft': 'status-info',
        'registering': 'status-warn',
        'ongoing': 'status-accent',
        'completed': 'status-success',
        'cancelled': 'status-danger'
      }
      return classMap[status] || 'status-accent'
    },

    /**
     * 获取状态显示文本
     */
    getStatusText(status) {
      const textMap = {
        0: '草稿',
        1: '报名中',
        2: '进行中',
        3: '已结束',
        4: '已取消',
        'draft': '草稿',
        'registering': '报名中',
        'ongoing': '进行中',
        'completed': '已结束',
        'cancelled': '已取消'
      }
      return textMap[status] || '进行中'
    },

    /**
     * 格式化日期范围
     * 将后端 startTime / endTime（如 "2026-04-01 09:00:00"）转为 "4月1日 - 4月5日 · 共5天"
     */
    formatDateRange(startTime, endTime) {
      if (!startTime) return '时间待定'
      try {
        const start = new Date(startTime.replace(/-/g, '/'))
        const end = endTime ? new Date(endTime.replace(/-/g, '/')) : null
        const sm = start.getMonth() + 1
        const sd = start.getDate()
        if (!end) return `${sm}月${sd}日`
        const em = end.getMonth() + 1
        const ed = end.getDate()
        const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
        return `${sm}月${sd}日 - ${em}月${ed}日 · 共${days > 0 ? days : 1}天`
      } catch (e) {
        return startTime
      }
    },

    /**
     * 格式化时间为友好格式
     */
    formatTime(timeStr) {
      if (!timeStr) return ''
      try {
        const t = new Date(timeStr.replace(/-/g, '/'))
        const now = new Date()
        const diff = now - t
        if (diff < 60000) return '刚刚'
        if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
        if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
        if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
        return `${t.getMonth() + 1}/${t.getDate()}`
      } catch (e) {
        return timeStr
      }
    },

    /**
     * 截断文本
     */
    truncate(text, maxLen) {
      if (!text) return ''
      return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
    },

    /**
     * 获取通知类型名称（后端 type 字段）
     */
    getNotiTypeName(type) {
      const map = {
        'conference': '会议',
        'registration': '报名',
        'schedule': '日程',
        'checkin': '签到',
        'reminder': '提醒',
        'urge': '催报',
        'system': '系统',
        'custom': '自定义'
      }
      return map[type] || '通知'
    },

    /**
     * 格式化发送渠道（后端 channels 字段，JSON字符串 ["sms","wechat"] 或已解析的数组）
     */
    formatChannels(channels) {
      if (!channels) return ''
      const channelMap = {
        'sms': '短信',
        'wechat': '微信',
        'email': '邮件',
        'push': '推送',
        'app': 'APP'
      }
      try {
        const arr = typeof channels === 'string' ? JSON.parse(channels) : channels
        if (Array.isArray(arr)) {
          return arr.map(c => channelMap[c] || c).join('/')
        }
        return channelMap[channels] || channels
      } catch (e) {
        return channels
      }
    },

    /**
     * 跳转到会议详情
     */
    goToMeetingDetail(id) {
      uni.navigateTo({
        url: `/pages/learner/meeting-detail?id=${id}`
      })
    },

    /**
     * 跳转到通知详情
     */
    goToNotificationDetail(id) {
      if (!id) return
      uni.navigateTo({
        url: `/pages/common/notification-detail?id=${id}`
      })
    },

    /**
     * 页面跳转
     */
    navigateTo(path) {
      uni.navigateTo({
        url: path
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.home-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.welcome-card {
  margin: $spacing-md;
  margin-top: 20rpx;
  border-radius: 24rpx; /* 匹配app原型的12px */
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 24rpx; /* 匹配app原型的12px */
  background: $icon-gradient-blue; /* 使用统一的蓝色渐变 */
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-white;
  font-size: 40rpx; /* 匹配app原型的20px */
  font-weight: 600;
  box-shadow: 0 8rpx 16rpx rgba(59, 130, 246, 0.3);
}

/* 用户头像图标（用于card-header） */
.user-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 24rpx; /* 匹配app原型的12px */
  background: $icon-gradient-blue;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-white;
  font-size: 40rpx; /* 匹配app原型的20px */
  box-shadow: 0 8rpx 16rpx rgba(59, 130, 246, 0.3);
}

.user-info {
  flex: 1;
}

.meeting-card {
  margin: $spacing-sm $spacing-md;
  transition: $transition-base;
  border-radius: 24rpx; /* 匹配app原型的12px */
}

.meeting-card:active {
  transform: scale(0.98);
}

.meeting-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: $spacing-md;
}

.meeting-info {
  flex: 1;
}

.meeting-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.meeting-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx $spacing-md;
  border-radius: $border-radius-lg;
  font-size: $font-size-xs;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: currentColor;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.safe-area-bottom {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
  min-height: $spacing-lg;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64rpx 0;
  color: $text-tertiary;
}

.loading-state .fa {
  font-size: 48rpx;
  margin-bottom: 16rpx;
}

.loading-text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

/* 今日提醒横幅内容 */
.info-banner-content {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}

.info-banner-content .fa {
  font-size: 32rpx;
  flex-shrink: 0;
}

.info-banner-text {
  flex: 1;
  font-size: $font-size-sm;
}

/* 通知列表 */
.notification-list {
  padding: 0 $spacing-md;
}

.notification-item {
  margin-bottom: $spacing-sm;
  border-radius: 20rpx;
  padding: $spacing-md;
}

.noti-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.noti-title-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex: 1;
  gap: 10rpx;
  overflow: hidden;
}

.noti-type-tag {
  display: inline-flex;
  align-items: center;
  padding: 2rpx 14rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.noti-type-conference {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}
.noti-type-registration {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}
.noti-type-schedule {
  background: rgba(139, 92, 246, 0.1);
  color: #8b5cf6;
}
.noti-type-checkin {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}
.noti-type-reminder,
.noti-type-urge {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}
.noti-type-system {
  background: rgba(107, 114, 128, 0.1);
  color: #6b7280;
}
.noti-type-custom {
  background: rgba(6, 182, 212, 0.1);
  color: #06b6d4;
}

.noti-status-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  flex-shrink: 0;
  margin-left: 12rpx;
}

.noti-st-sent {
  background: #10b981;
}
.noti-st-draft {
  background: #9ca3af;
}
.noti-st-pending,
.noti-st-sending {
  background: #f59e0b;
  animation: pulse 2s infinite;
}
.noti-st-failed {
  background: #ef4444;
}

.noti-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.noti-content {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.5;
  margin-bottom: 10rpx;
}

.noti-footer {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 20rpx;
  margin-top: 4rpx;
}

.noti-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
}

.noti-time .fa {
  font-size: 20rpx;
}

.noti-meta {
  font-size: $font-size-xs;
  color: $text-tertiary;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
}

.noti-meta .fa {
  font-size: 20rpx;
}
</style>
