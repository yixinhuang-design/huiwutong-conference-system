<template>
  <view class="dashboard-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">数据看板</text>
      </view>
    </view>

    <!-- 实时数据卡片 -->
    <view class="realtime-cards">
      <view class="realtime-card card">
        <view class="card-top">
          <text class="card-icon"><text class="fa fa-users"></text></text>
          <text class="card-label">总人数</text>
        </view>
        <view class="card-value">{{ stats.totalPeople }}</view>
        <view class="card-change positive">
          <text>↑</text>
          <text>{{ stats.peopleChange }}</text>
        </view>
      </view>

      <view class="realtime-card card">
        <view class="card-top">
          <text class="card-icon"><text class="fa fa-check"></text></text>
          <text class="card-label">已签到</text>
        </view>
        <view class="card-value">{{ stats.checkedIn }}</view>
        <view class="card-change positive">
          <text>↑</text>
          <text>{{ stats.checkInChange }}</text>
        </view>
      </view>

      <view class="realtime-card card">
        <view class="card-top">
          <text class="card-icon">⏰</text>
          <text class="card-label">进行中</text>
        </view>
        <view class="card-value">{{ stats.ongoing }}</view>
        <view class="card-status">当前课程</view>
      </view>

      <view class="realtime-card card">
        <view class="card-top">
          <text class="card-icon">📊</text>
          <text class="card-label">完成率</text>
        </view>
        <view class="card-value">{{ stats.completionRate }}%</view>
        <view class="card-progress">
          <view class="progress-bar" :style="{ width: stats.completionRate + '%' }"></view>
        </view>
      </view>
    </view>

    <!-- 今日日程 -->
    <view class="today-schedule card">
      <view class="section-title">今日日程</view>
      <view v-if="todaySchedule.length > 0">
        <view
          v-for="(item, index) in todaySchedule"
          :key="index"
          class="schedule-item"
          :class="{ current: item.current, completed: item.completed }"
        >
          <view class="time-badge">
            <text class="time-text">{{ item.time }}</text>
          </view>
          <view class="schedule-info">
            <view class="schedule-title">{{ item.title }}</view>
            <view class="schedule-location">
              <text><text class="fa fa-map-marker-alt"></text></text> {{ item.location }}
            </view>
          </view>
          <view class="schedule-status" :class="item.statusClass">
            {{ item.statusText }}
          </view>
        </view>
      </view>
    </view>

    <!-- 数据图表 -->
    <view class="chart-section card">
      <view class="section-title">签到统计</view>
      <view class="chart-placeholder">
        <text class="chart-icon">📈</text>
        <text class="chart-text">签到趋势图</text>
        <text class="chart-hint">（图表功能开发中）</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions card">
      <view class="section-title">快捷操作</view>
      <view class="action-grid grid-3">
        <view class="action-tile feature-tile" @click="goToManage('registration')">
          <view class="action-icon"><text class="fa fa-users"></text></view>
          <view class="action-title feature-title">报名管理</view>
        </view>
        <view class="action-tile feature-tile" @click="goToManage('grouping')">
          <view class="action-icon">🔖</view>
          <view class="action-title feature-title">分组管理</view>
        </view>
        <view class="action-tile feature-tile" @click="goToManage('seat')">
          <view class="action-icon"><text class="fa fa-th-large"></text></view>
          <view class="action-title feature-title">座位管理</view>
        </view>
        <view class="action-tile feature-tile" @click="goToManage('notice')">
          <view class="action-icon">📢</view>
          <view class="action-title feature-title">通知管理</view>
        </view>
        <view class="action-tile feature-tile" @click="goToManage('task')">
          <view class="action-icon"><text class="fa fa-check"></text></view>
          <view class="action-title feature-title">任务管理</view>
        </view>
        <view class="action-tile feature-tile" @click="goToManage('data')">
          <view class="action-icon">📊</view>
          <view class="action-title feature-title">数据分析</view>
        </view>
      </view>
    </view>

    <!-- 待处理事项 -->
    <view class="pending-list card">
      <view class="section-title">待处理</view>
      <view v-if="pendingList.length > 0">
        <view
          v-for="(item, index) in pendingList"
          :key="index"
          class="pending-item"
          @click="handlePending(item)"
        >
          <view class="pending-icon" :style="{ background: item.color }">
            <text>{{ item.icon }}</text>
          </view>
          <view class="pending-info">
            <view class="pending-title">{{ item.title }}</view>
            <view class="pending-meta">{{ item.meta }}</view>
          </view>
          <view class="pending-badge">{{ item.count }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      stats: {
        totalPeople: 120,
        peopleChange: '3人',
        checkedIn: 115,
        checkInChange: '5人',
        ongoing: 1,
        completionRate: 85
      },
      todaySchedule: [
        {
          time: '09:00-11:30',
          title: '专题讲座：新时代党的建设',
          location: '报告厅',
          current: false,
          completed: true,
          statusText: '已完成',
          statusClass: 'status-completed'
        },
        {
          time: '14:00-17:00',
          title: '分组讨论',
          location: 'A栋302室',
          current: true,
          completed: false,
          statusText: '进行中',
          statusClass: 'status-current'
        },
        {
          time: '19:00-21:00',
          title: '观看纪录片',
          location: '报告厅',
          current: false,
          completed: false,
          statusText: '未开始',
          statusClass: 'status-pending'
        }
      ],
      pendingList: [
        {
          title: '待审核报名',
          meta: '5人申请',
          count: 5,
          icon: '<text class="fa fa-user"></text>',
          color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          action: 'registration'
        },
        {
          title: '待发布通知',
          meta: '2条草稿',
          count: 2,
          icon: '📢',
          color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
          action: 'notice'
        },
        {
          title: '待处理预警',
          meta: '3条提醒',
          count: 3,
          icon: '<text class="fa fa-exclamation-triangle"></text>',
          color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
          action: 'alert'
        }
      ]
    }
  },

  onLoad() {
    this.loadStats()
  },

  methods: {
    /**
     * 加载统计数据
     */
    async loadStats() {
      try {
        // TODO: 调用API获取统计数据
        // const res = await this.$api.stats.getDashboard()
      } catch (error) {
        console.error('Load stats error:', error)
      }
    },

    /**
     * 跳转到管理页面
     */
    goToManage(type) {
      const routeMap = {
        registration: '/pages/staff/registration-manage',
        grouping: '/pages/staff/grouping-manage',
        seat: '/pages/staff/seat-manage',
        notice: '/pages/staff/notice-manage',
        task: '/pages/staff/task-list',
        data: '/pages/staff/data-analysis'
      }

      if (routeMap[type]) {
        uni.navigateTo({
          url: routeMap[type]
        })
      }
    },

    /**
     * 处理待办事项
     */
    handlePending(item) {
      this.goToManage(item.action)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.dashboard-container {
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
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.realtime-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
  padding: $spacing-md;
}

.realtime-card {
  padding: $spacing-md;
  text-align: center;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.card-icon {
  font-size: 40rpx;
}

.card-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.card-value {
  font-size: 56rpx;
  font-weight: 700;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: $spacing-sm;
}

.card-change {
  font-size: $font-size-sm;
  color: $success-color;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  font-weight: 500;
}

.card-status {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.card-progress {
  width: 100%;
  height: 8rpx;
  background: $bg-tertiary;
  border-radius: 4rpx;
  overflow: hidden;
  margin-top: $spacing-sm;
}

.progress-bar {
  height: 100%;
  background: $primary-gradient;
  border-radius: 4rpx;
  transition: width 0.3s;
}

.today-schedule,
.chart-section,
.quick-actions,
.pending-list {
  margin: $spacing-md;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.schedule-item:last-child {
  border-bottom: none;
}

.time-badge {
  min-width: 140rpx;
  padding: $spacing-sm;
  background: $bg-tertiary;
  border-radius: $border-radius-sm;
  text-align: center;
}

.time-text {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $primary-color;
}

.schedule-info {
  flex: 1;
}

.schedule-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 6rpx;
}

.schedule-location {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.schedule-item.current .time-badge {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
}

.schedule-item.completed {
  opacity: 0.5;
}

.schedule-status {
  padding: 6rpx $spacing-sm;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.status-completed {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: $success-color;
}

.status-current {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: $accent-color;
}

.status-pending {
  background: $bg-tertiary;
  color: $text-secondary;
}

.chart-placeholder {
  padding: 80rpx 0;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.chart-icon {
  font-size: 96rpx;
  opacity: 0.3;
}

.chart-text {
  font-size: $font-size-md;
  color: $text-secondary;
}

.chart-hint {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.action-grid {
  padding: $spacing-lg 0;
}

.action-tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.action-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: $border-radius-lg;
  background: $primary-gradient;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
}

.action-title {
  font-size: $font-size-sm;
  color: $text-primary;
}

.pending-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.pending-item:last-child {
  border-bottom: none;
}

.pending-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: $border-radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
}

.pending-info {
  flex: 1;
}

.pending-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 6rpx;
}

.pending-meta {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.pending-badge {
  min-width: 40rpx;
  height: 40rpx;
  padding: 0 10rpx;
  background: $danger-color;
  color: $text-white;
  font-size: $font-size-xs;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
