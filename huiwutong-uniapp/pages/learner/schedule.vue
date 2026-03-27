<template>
  <view class="schedule-container">
    <!-- 自定义状态栏 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">日程安排</text>
      </view>
    </view>

    <!-- 日期选择器 -->
    <view class="date-selector card">
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
      <view v-if="scheduleList.length > 0">
        <view
          v-for="(item, index) in scheduleList"
          :key="item.id"
          class="schedule-item card fade-in"
          :class="{ checked: item.checked }"
        >
          <view class="schedule-time">
            <view class="time-range">
              {{ item.startTime }} - {{ item.endTime }}
            </view>
            <view class="time-duration">{{ item.duration }}</view>
          </view>

          <view class="schedule-content">
            <view class="schedule-title">{{ item.title }}</view>
            <view class="schedule-location">
              <text class="fa fa-map-marker-alt location-icon"></text>
              {{ item.location }}
            </view>
            <view class="schedule-speaker" v-if="item.speaker">
              <text class="fa fa-user speaker-icon"></text>
              {{ item.speaker }}
            </view>
          </view>

          <view class="schedule-status">
            <view
              v-if="item.checked"
              class="status-badge status-success"
            >
              <text class="fa fa-check"></text>
              已签到
            </view>
            <view
              v-else-if="item.canCheckin"
              class="status-badge status-accent"
              @click="handleCheckin(item)"
            >
              <text class="fa fa-qrcode"></text>
              签到
            </view>
            <view v-else class="status-badge status-tertiary">
              未开始
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="fa fa-calendar empty-icon"></text>
        <text class="empty-text">暂无日程安排</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      meetingId: '',
      scrollIntoView: '',
      selectedDateIndex: 0,
      dateList: [],
      scheduleList: []
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
    }

    this.initDateList()
    this.loadSchedule()
  },

  methods: {
    /**
     * 初始化日期列表
     */
    initDateList() {
      const weekdays = ['日', '一', '二', '三', '四', '五', '六']
      const today = new Date()
      const list = []

      // 生成7天日期
      for (let i = 0; i < 7; i++) {
        const date = new Date(today)
        date.setDate(today.getDate() + i)

        list.push({
          weekday: i === 0 ? '今天' : '周' + weekdays[date.getDay()],
          day: date.getDate(),
          month: date.getMonth() + 1,
          date: date
        })
      }

      this.dateList = list
    },

    /**
     * 选择日期
     */
    selectDate(index) {
      this.selectedDateIndex = index
      this.scrollIntoView = 'date-' + index
      this.loadSchedule()
    },

    /**
     * 加载日程
     */
    async loadSchedule() {
      try {
        const selectedDate = this.dateList[this.selectedDateIndex]

        // TODO: 调用API获取日程
        // const res = await this.$api.meeting.getSchedule(this.meetingId, selectedDate.date)

        // 模拟数据
        this.scheduleList = [
          {
            id: 1,
            title: '开班仪式',
            startTime: '09:00',
            endTime: '09:30',
            duration: '30分钟',
            location: '报告厅',
            speaker: '张部长',
            checked: true,
            canCheckin: false
          },
          {
            id: 2,
            title: '专题讲座：新时代党的建设',
            startTime: '09:30',
            endTime: '11:30',
            duration: '2小时',
            location: '报告厅',
            speaker: '李教授',
            checked: true,
            canCheckin: false
          },
          {
            id: 3,
            title: '分组讨论',
            startTime: '14:00',
            endTime: '17:00',
            duration: '3小时',
            location: 'A栋302室',
            speaker: '各组召集人',
            checked: false,
            canCheckin: true
          },
          {
            id: 4,
            title: '晚餐',
            startTime: '18:00',
            endTime: '19:00',
            duration: '1小时',
            location: '餐厅',
            speaker: '',
            checked: false,
            canCheckin: false
          }
        ]
      } catch (error) {
        console.error('Load schedule error:', error)
        this.scheduleList = []
      }
    },

    /**
     * 签到
     */
    handleCheckin(item) {
      uni.showModal({
        title: '确认签到',
        content: `是否确认签到"${item.title}"？`,
        success: (res) => {
          if (res.confirm) {
            this.doCheckin(item)
          }
        }
      })
    },

    /**
     * 执行签到
     */
    async doCheckin(item) {
      try {
        uni.showLoading({ title: '签到中...' })

        // TODO: 调用签到API
        // await this.$api.meeting.checkin(this.meetingId, {
        //   scheduleId: item.id
        // })

        setTimeout(() => {
          uni.hideLoading()

          // 更新状态
          item.checked = true
          item.canCheckin = false

          uni.showToast({
            title: '签到成功',
            icon: 'success'
          })
        }, 1000)
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '签到失败',
          icon: 'none'
        })
      }
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

.date-selector {
  margin: $spacing-md;
  padding: 0;
  border-radius: 24rpx; /* 匹配app原型 */
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
  border-radius: 20rpx; /* 匹配app原型 */
  transition: $transition-base;
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

.schedule-list {
  padding: 0 $spacing-md;
}

.schedule-item {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  transition: $transition-base;
  border-radius: 24rpx; /* 匹配app原型 */
  animation: fadeIn 0.3s ease-in;
}

.schedule-item.checked {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-color: $success-color;
}

.schedule-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-sm;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 20rpx; /* 匹配app原型 */
  min-width: 120rpx;
  text-align: center;
  border: 2rpx solid $border-color;
}

.time-range {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 6rpx;
}

.time-duration {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.schedule-content {
  flex: 1;
  padding: $spacing-sm 0;
}

.schedule-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.schedule-location,
.schedule-speaker {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: 6rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.location-icon,
.speaker-icon {
  color: $primary-color;
  font-size: $font-size-md;
}

.schedule-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx $spacing-md;
  border-radius: 20rpx; /* 匹配app原型 */
  font-size: $font-size-xs;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.status-success {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #10b981;
}

.status-accent {
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  color: #3b82f6;
  cursor: pointer;
}

.status-tertiary {
  background: $bg-tertiary;
  color: $text-secondary;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xxl;
  color: $text-tertiary;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: $spacing-md;
  opacity: 0.5;
}

.empty-text {
  font-size: $font-size-md;
}
</style>
