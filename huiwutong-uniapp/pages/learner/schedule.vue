<template>
  <view class="schedule-container">
    <!-- 自定义状态栏 -->
    <view class="custom-header">
      <view class="header-content">
        <view class="header-back" @click="goBack">
          <text class="fa fa-arrow-left"></text>
        </view>
        <text class="header-title">日程安排</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载日程中...</text>
    </view>

    <!-- 加载失败 -->
    <view v-else-if="loadError" class="empty-container">
      <text class="fa fa-exclamation-circle empty-icon"></text>
      <text class="empty-text">{{ loadError }}</text>
      <button class="btn btn-primary btn-sm" @click="loadAllSchedules">重新加载</button>
    </view>

    <view v-else>
      <!-- 日期选择器 -->
      <view class="date-selector card" v-if="dateList.length > 0">
        <scroll-view
          class="date-scroll"
          scroll-x
          :scroll-into-view="scrollIntoView"
          scroll-with-animation
        >
          <view
            v-for="(date, index) in dateList"
            :key="index"
            :id="'date-' + index"
            class="date-item"
            :class="{ active: selectedDateIndex === index }"
            @click="selectDate(index)"
          >
            <view class="date-weekday">{{ date.weekday }}</view>
            <view class="date-day">{{ date.day }}</view>
            <view class="date-month">{{ date.month }}月</view>
          </view>
        </scroll-view>
      </view>

      <!-- 日程列表 -->
      <view class="schedule-list">
        <view v-if="filteredSchedules.length > 0">
          <view
            v-for="item in filteredSchedules"
            :key="item.id"
            class="schedule-item card fade-in"
            :class="getItemClass(item)"
          >
            <!-- 优先级标记 -->
            <view v-if="item.priority >= 2" class="priority-bar" :class="'priority-' + item.priority"></view>

            <view class="schedule-time">
              <view class="time-range">
                {{ extractTime(item.startTime) }}
              </view>
              <view class="time-sep">~</view>
              <view class="time-range">
                {{ extractTime(item.endTime) }}
              </view>
              <view class="time-duration">{{ calcDuration(item.startTime, item.endTime) }}</view>
            </view>

            <view class="schedule-content">
              <view class="schedule-title-row">
                <text class="schedule-title">{{ item.title }}</text>
                <view v-if="item.priority === 3" class="priority-tag important">重要</view>
                <view v-else-if="item.priority === 2" class="priority-tag normal">关注</view>
              </view>
              <view class="schedule-location" v-if="item.location">
                <text class="fa fa-map-marker-alt meta-icon"></text>
                <text>{{ item.location }}</text>
              </view>
              <view class="schedule-speaker" v-if="item.speaker">
                <text class="fa fa-chalkboard-teacher meta-icon"></text>
                <text>{{ item.speaker }}</text>
              </view>
              <view class="schedule-host" v-if="item.host">
                <text class="fa fa-user-tie meta-icon"></text>
                <text>主持：{{ item.host }}</text>
              </view>
              <view class="schedule-notes" v-if="item.notes">
                <text class="fa fa-sticky-note meta-icon"></text>
                <text>{{ item.notes }}</text>
              </view>
            </view>

            <view class="schedule-status">
              <view :class="['status-badge', getScheduleStatusClass(item.status)]">
                {{ getScheduleStatusText(item.status) }}
              </view>
            </view>
          </view>
        </view>

        <view v-else class="empty-state">
          <text class="fa fa-calendar empty-icon"></text>
          <text class="empty-text">当天暂无日程安排</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { schedule as scheduleApi } from '@/api/index'

export default {
  data() {
    return {
      meetingId: '',
      loading: true,
      loadError: '',
      scrollIntoView: '',
      selectedDateIndex: 0,
      dateList: [],
      allSchedules: []  // 后端返回的全部日程
    }
  },

  computed: {
    /**
     * 按选中日期过滤日程
     */
    filteredSchedules() {
      if (!this.dateList.length || !this.allSchedules.length) return []
      const selected = this.dateList[this.selectedDateIndex]
      if (!selected) return []
      const dateStr = selected.dateStr // 'YYYY-MM-DD'
      return this.allSchedules
        .filter(s => {
          if (!s.startTime) return false
          return s.startTime.substring(0, 10) === dateStr
        })
        .sort((a, b) => {
          // 先按 sortOrder，再按 startTime
          if (a.sortOrder !== b.sortOrder) return (a.sortOrder || 0) - (b.sortOrder || 0)
          return (a.startTime || '').localeCompare(b.startTime || '')
        })
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
      this.loadAllSchedules()
    } else {
      this.loading = false
      this.loadError = '缺少会议ID参数'
    }
  },

  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },

    /**
     * 加载该会议所有日程（一次拉取，前端按日期过滤）
     */
    async loadAllSchedules() {
      this.loading = true
      this.loadError = ''
      try {
        const res = await scheduleApi.listAll(this.meetingId)
        const data = res.data || res
        const list = Array.isArray(data) ? data : (data.records || data.list || [])
        this.allSchedules = list
        this.buildDateList(list)
      } catch (error) {
        console.error('Load schedules error:', error)
        this.loadError = '加载日程失败，请检查网络后重试'
        this.allSchedules = []
        this.dateList = []
      } finally {
        this.loading = false
      }
    },

    /**
     * 从日程数据中提取不重复的日期列表
     */
    buildDateList(schedules) {
      const weekdays = ['日', '一', '二', '三', '四', '五', '六']
      const dateSet = new Set()

      schedules.forEach(s => {
        if (s.startTime) {
          dateSet.add(s.startTime.substring(0, 10)) // 'YYYY-MM-DD'
        }
      })

      // 按日期排序
      const sortedDates = [...dateSet].sort()

      const todayStr = this.getTodayStr()

      this.dateList = sortedDates.map(dateStr => {
        const d = new Date(dateStr.replace(/-/g, '/'))
        const isToday = dateStr === todayStr
        return {
          dateStr,
          weekday: isToday ? '今天' : '周' + weekdays[d.getDay()],
          day: d.getDate(),
          month: d.getMonth() + 1
        }
      })

      // 默认选中今天，如果今天不在列表中则选第一天
      if (this.dateList.length > 0) {
        const todayIndex = this.dateList.findIndex(d => d.dateStr === todayStr)
        this.selectedDateIndex = todayIndex >= 0 ? todayIndex : 0
        this.scrollIntoView = 'date-' + this.selectedDateIndex
      }
    },

    /**
     * 获取今天的日期字符串 'YYYY-MM-DD'
     */
    getTodayStr() {
      const d = new Date()
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },

    /**
     * 选择日期
     */
    selectDate(index) {
      this.selectedDateIndex = index
      this.scrollIntoView = 'date-' + index
    },

    /**
     * 从 "2026-04-10 09:00:00" 提取 "09:00"
     */
    extractTime(dateTimeStr) {
      if (!dateTimeStr) return '--:--'
      const timePart = dateTimeStr.substring(11, 16)
      return timePart || '--:--'
    },

    /**
     * 计算时长
     */
    calcDuration(startStr, endStr) {
      if (!startStr || !endStr) return ''
      try {
        const s = new Date(startStr.replace(/-/g, '/'))
        const e = new Date(endStr.replace(/-/g, '/'))
        const diffMin = Math.round((e - s) / (1000 * 60))
        if (diffMin < 0) return ''
        if (diffMin < 60) return diffMin + '分钟'
        const h = Math.floor(diffMin / 60)
        const m = diffMin % 60
        return m > 0 ? `${h}小时${m}分` : `${h}小时`
      } catch {
        return ''
      }
    },

    /**
     * 日程状态样式 class
     * 0-待发布 1-已发布 2-进行中 3-已结束 4-已取消
     */
    getScheduleStatusClass(status) {
      const map = {
        0: 'status-draft',
        1: 'status-published',
        2: 'status-ongoing',
        3: 'status-ended',
        4: 'status-cancelled'
      }
      return map[status] || 'status-tertiary'
    },

    /**
     * 日程状态文本
     */
    getScheduleStatusText(status) {
      const map = {
        0: '待发布',
        1: '已发布',
        2: '进行中',
        3: '已结束',
        4: '已取消'
      }
      return map[status] || '未知'
    },

    /**
     * 卡片样式 class
     */
    getItemClass(item) {
      const cls = []
      if (item.status === 3) cls.push('ended')
      if (item.status === 2) cls.push('ongoing')
      if (item.status === 4) cls.push('cancelled')
      return cls.join(' ')
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.schedule-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* ---- Header ---- */
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
  justify-content: space-between;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  .fa { font-size: 32rpx; color: $text-primary; }
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.header-placeholder {
  width: 60rpx;
}

/* ---- Loading / Empty ---- */
.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 40rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid rgba(0,0,0,0.08);
  border-top-color: $primary-color;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: $spacing-md;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.empty-icon {
  font-size: 80rpx;
  color: $text-placeholder;
  margin-bottom: $spacing-md;
}

.empty-text {
  font-size: $font-size-md;
  color: $text-secondary;
  margin-bottom: $spacing-lg;
}

/* ---- Date Selector ---- */
.date-selector {
  margin: $spacing-md;
  padding: 0;
  border-radius: 24rpx;
}

.date-scroll {
  white-space: nowrap;
}

.date-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-md $spacing-lg;
  margin: $spacing-sm;
  border-radius: 20rpx;
  transition: all 0.25s;
}

.date-item.active {
  background: $primary-gradient;
  color: $text-white;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.4);
}

.date-weekday {
  font-size: $font-size-sm;
  margin-bottom: 6rpx;
}

.date-day {
  font-size: $font-size-xl;
  font-weight: 600;
}

.date-month {
  font-size: $font-size-xs;
  opacity: 0.8;
}

/* ---- Schedule List ---- */
.schedule-list {
  padding: 0 $spacing-md;
}

.schedule-item {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  border-radius: 24rpx;
  animation: fadeIn 0.3s ease-in;
  position: relative;
  overflow: hidden;
}

.schedule-item.ongoing {
  border-left: 6rpx solid #10b981;
}

.schedule-item.ended {
  opacity: 0.6;
}

.schedule-item.cancelled {
  opacity: 0.5;
  text-decoration: line-through;
}

/* ---- Priority Bar ---- */
.priority-bar {
  position: absolute;
  top: 0;
  right: 0;
  width: 8rpx;
  height: 100%;
}

.priority-bar.priority-2 {
  background: #f59e0b;
}

.priority-bar.priority-3 {
  background: #ef4444;
}

/* ---- Time Block ---- */
.schedule-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-sm 12rpx;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 20rpx;
  min-width: 110rpx;
  text-align: center;
  border: 2rpx solid $border-color;
}

.time-range {
  font-size: 26rpx;
  font-weight: 600;
  color: $text-primary;
}

.time-sep {
  font-size: $font-size-xs;
  color: $text-secondary;
  margin: 2rpx 0;
}

.time-duration {
  font-size: 20rpx;
  color: $text-secondary;
  margin-top: 4rpx;
}

/* ---- Content ---- */
.schedule-content {
  flex: 1;
  padding: $spacing-sm 0;
  min-width: 0;
}

.schedule-title-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 10rpx;
}

.schedule-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.priority-tag {
  flex-shrink: 0;
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 16rpx;
  font-weight: 500;
}

.priority-tag.important {
  background: #fef2f2;
  color: #ef4444;
}

.priority-tag.normal {
  background: #fffbeb;
  color: #f59e0b;
}

.schedule-location,
.schedule-speaker,
.schedule-host,
.schedule-notes {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: 6rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-icon {
  color: $primary-color;
  font-size: 24rpx;
  flex-shrink: 0;
}

/* ---- Status Badge ---- */
.schedule-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: $font-size-xs;
  font-weight: 500;
}

.status-draft {
  background: #f1f5f9;
  color: #64748b;
}

.status-published {
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  color: #3b82f6;
}

.status-ongoing {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #10b981;
}

.status-ended {
  background: #f1f5f9;
  color: #94a3b8;
}

.status-cancelled {
  background: #fef2f2;
  color: #ef4444;
}

.status-tertiary {
  background: $bg-tertiary;
  color: $text-secondary;
}

/* ---- Empty State ---- */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx $spacing-lg;
  color: $text-tertiary;
}

.empty-state .empty-icon {
  font-size: 100rpx;
  margin-bottom: $spacing-md;
  opacity: 0.4;
}

.empty-state .empty-text {
  font-size: $font-size-md;
  color: $text-secondary;
}
</style>
