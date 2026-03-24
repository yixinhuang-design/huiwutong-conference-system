<template>
  <view class="home-container">
    <!-- 欢迎区域 -->
    <view class="welcome-card card fade-in">
      <view class="card-header">
        <view class="avatar">
          <text>{{ avatarText }}</text>
        </view>
        <view class="user-info">
          <view class="card-title">上午好，{{ realName }}</view>
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
      <strong><text class="fa fa-bullhorn"></text> 今日提醒</strong>
      {{ todayReminder }}
    </view>

    <!-- 当前会议/培训列表 -->
    <view class="section-title">我参加的培训</view>

    <view v-if="meetings.length > 0">
      <view
        v-for="(meeting, index) in meetings"
        :key="meeting.id"
        class="meeting-card card fade-in"
        @click="goToMeetingDetail(meeting.id)"
      >
        <view class="meeting-header">
          <view class="meeting-info">
            <view class="meeting-title">{{ meeting.title }}</view>
            <view class="meeting-meta">
              <text class="meta-item">
                <text><text class="fa fa-calendar-alt"></text></text>
                {{ meeting.dateRange }} · 共{{ meeting.days }}天
              </text>
              <text class="meta-item">
                <text><text class="fa fa-map-marker-alt"></text></text>
                {{ meeting.location }}
              </text>
            </view>
          </view>
          <view
            class="status-chip"
            :class="getStatusClass(meeting.status)"
          >
            <text class="status-dot"></text>
            {{ meeting.statusText }}
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-state">
      <text class="empty-icon"><text class="fa fa-clipboard"></text></text>
      <text class="empty-text">暂无培训安排</text>
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

export default {
  data() {
    return {
      todayReminder: '',
      meetings: [],
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
    }
  },

  onLoad() {
    this.initPage()
  },

  onShow() {
    // 页面显示时刷新数据
    this.loadMeetings()
  },

  methods: {
    /**
     * 初始化页面
     */
    initPage() {
      // 设置今日提醒
      this.setTodayReminder()

      // 加载会议列表
      this.loadMeetings()

      // 根据用户类型调整功能列表
      this.adjustFeatureList()
    },

    /**
     * 设置今日提醒
     */
    setTodayReminder() {
      const now = new Date()
      const hour = now.getHours()

      if (hour < 9) {
        this.todayReminder = '请在 09:00 前完成报到签到，携带胸牌入场。'
      } else if (hour >= 9 && hour < 12) {
        this.todayReminder = '上午课程即将开始，请前往指定会议室。'
      } else if (hour >= 12 && hour < 14) {
        this.todayReminder = '午休时间，下午课程将在 14:00 开始。'
      } else {
        this.todayReminder = '请注意下午课程安排，准时参加。'
      }
    },

    /**
     * 加载会议列表
     */
    async loadMeetings() {
      try {
        // TODO: 调用API获取会议列表
        // const res = await this.$api.meeting.getList()

        // 模拟数据
        this.meetings = [
          {
            id: 1,
            title: '2025党务干部培训班',
            dateRange: '1月15日 - 1月19日',
            days: 5,
            location: '市委党校报告厅',
            status: 'ongoing',
            statusText: '进行中'
          },
          {
            id: 2,
            title: '青年干部能力提升班',
            dateRange: '1月22日 - 1月26日',
            days: 5,
            location: '市委党校教学楼',
            status: 'upcoming',
            statusText: '即将开始'
          }
        ]
      } catch (error) {
        console.error('Load meetings error:', error)
        this.meetings = []
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
     * 获取状态样式类
     */
    getStatusClass(status) {
      const classMap = {
        ongoing: 'status-accent',
        upcoming: 'status-warn',
        completed: 'status-success',
        cancelled: 'status-danger'
      }
      return classMap[status] || 'status-accent'
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
</style>
