<template>
  <view class="checkin-container">
    <!-- 自定义状态栏 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">报到签到</text>
      </view>
    </view>

    <!-- 会议信息 -->
    <view class="meeting-info card">
      <view class="meeting-title">{{ meetingInfo.title }}</view>
      <view class="meeting-meta">
        <text class="meta-item">📅 {{ meetingInfo.date }}</text>
        <text class="meta-item">📍 {{ meetingInfo.location }}</text>
      </view>
    </view>

    <!-- 签到状态 -->
    <view class="checkin-status" :class="statusClass">
      <view class="status-icon">{{ statusIcon }}</view>
      <view class="status-text">{{ statusText }}</view>
      <view class="status-time" v-if="checkinTime">{{ checkinTime }}</view>
    </view>

    <!-- 二维码签到 -->
    <view v-if="!checked && !loading" class="qrcode-section card">
      <view class="section-title">扫码签到</view>
      <view class="qrcode-container">
        <canvas canvas-id="qrcode" class="qrcode-canvas"></canvas>
      </view>
      <view class="qrcode-tip">请工作人员扫描二维码完成签到</view>
    </view>

    <!-- 定位签到 -->
    <view v-if="!checked && !loading" class="location-section card">
      <view class="section-title">定位签到</view>
      <view class="location-info">
        <text class="location-icon">📍</text>
        <text class="location-text">{{ locationText }}</text>
      </view>
      <button
        class="location-btn btn btn-primary btn-block"
        @click="handleLocationCheckin"
        :disabled="!inRange"
      >
        {{ inRange ? '确认签到' : '不在签到范围内' }}
      </button>
      <view class="location-tip">需在会议地点{{ checkinRange }}米范围内才能签到</view>
    </view>

    <!-- 签到记录 -->
    <view class="checkin-records card">
      <view class="section-title">签到记录</view>
      <view v-if="records.length > 0">
        <view
          v-for="(record, index) in records"
          :key="index"
          class="record-item"
        >
          <view class="record-time">{{ record.time }}</view>
          <view class="record-info">
            <text class="record-type">{{ record.type }}</text>
            <text class="record-location">{{ record.location }}</text>
          </view>
          <view class="record-status success">✅</view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">暂无签到记录</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      meetingId: '',
      meetingInfo: {
        title: '2025党务干部培训班',
        date: '2025年1月15日 - 1月19日',
        location: '市委党校报告厅'
      },
      checked: false,
      loading: true,
      checkinTime: '',
      locationText: '获取定位中...',
      inRange: false,
      checkinRange: 500,
      records: []
    }
  },

  computed: {
    statusClass() {
      if (this.loading) return 'status-loading'
      if (this.checked) return 'status-success'
      return 'status-pending'
    },

    statusIcon() {
      if (this.loading) return '⏳'
      if (this.checked) return '✅'
      return '⏰'
    },

    statusText() {
      if (this.loading) return '签到状态加载中...'
      if (this.checked) return '签到成功'
      return '等待签到'
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
    }

    this.initPage()
  },

  methods: {
    /**
     * 初始化页面
     */
    async initPage() {
      await this.loadCheckinStatus()
      await this.loadLocation()
      await this.loadRecords()

      if (!this.checked) {
        this.generateQRCode()
      }

      this.loading = false
    },

    /**
     * 加载签到状态
     */
    async loadCheckinStatus() {
      try {
        // TODO: 调用API获取签到状态
        // const res = await this.$api.meeting.getCheckinStatus(this.meetingId)

        // 模拟数据
        this.checked = false
      } catch (error) {
        console.error('Load checkin status error:', error)
      }
    },

    /**
     * 生成二维码
     */
    generateQRCode() {
      // TODO: 生成签到二维码
      // 使用uqrcode或其他二维码库
      console.log('Generate QR Code')
    },

    /**
     * 加载位置信息
     */
    async loadLocation() {
      try {
        const res = await uni.getLocation({
          type: 'gcj02'
        })

        const { latitude, longitude } = res

        // TODO: 调用地理编码API获取地址
        this.locationText = `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`

        // TODO: 判断是否在签到范围内
        // this.inRange = await this.checkInRange(latitude, longitude)
        this.inRange = true
      } catch (error) {
        console.error('Get location error:', error)
        this.locationText = '定位失败，请检查定位权限'
      }
    },

    /**
     * 定位签到
     */
    async handleLocationCheckin() {
      if (!this.inRange) {
        uni.showToast({
          title: '不在签到范围内',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({ title: '签到中...' })

        // TODO: 调用签到API
        // await this.$api.meeting.checkin(this.meetingId, {
        //   type: 'location',
        //   latitude: this.latitude,
        //   longitude: this.longitude
        // })

        setTimeout(() => {
          uni.hideLoading()

          this.checked = true
          this.checkinTime = this.formatTime(new Date())

          // 添加到记录
          this.records.unshift({
            time: this.formatTime(new Date()),
            type: '定位签到',
            location: this.locationText
          })

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
    },

    /**
     * 加载签到记录
     */
    async loadRecords() {
      try {
        // TODO: 调用API获取签到记录
        // const res = await this.$api.meeting.getCheckinRecords(this.meetingId)

        // 模拟数据
        this.records = [
          {
            time: '2025-01-15 08:55',
            type: '扫码签到',
            location: '市委党校报告厅'
          },
          {
            time: '2025-01-16 09:02',
            type: '定位签到',
            location: '市委党校报告厅'
          }
        ]
      } catch (error) {
        console.error('Load records error:', error)
      }
    },

    /**
     * 格式化时间
     */
    formatTime(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hour = String(date.getHours()).padStart(2, '0')
      const minute = String(date.getMinutes()).padStart(2, '0')

      return `${year}-${month}-${day} ${hour}:${minute}`
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.checkin-container {
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

.meeting-info {
  margin: $spacing-md;
}

.meeting-title {
  font-size: $font-size-xl;
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

.checkin-status {
  margin: $spacing-md;
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.status-loading {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.status-success {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
}

.status-pending {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
}

.status-icon {
  font-size: 120rpx;
}

.status-text {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.status-time {
  font-size: $font-size-md;
  color: $text-secondary;
}

.qrcode-section,
.location-section {
  margin: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.qrcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: $spacing-xl;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.qrcode-canvas {
  width: 400rpx;
  height: 400rpx;
}

.qrcode-tip {
  text-align: center;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.location-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.location-icon {
  font-size: $font-size-lg;
}

.location-text {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
}

.location-btn {
  margin-bottom: $spacing-sm;
}

.location-btn:disabled {
  opacity: 0.6;
}

.location-tip {
  text-align: center;
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.checkin-records {
  margin: $spacing-md;
}

.record-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.record-item:last-child {
  border-bottom: none;
}

.record-time {
  min-width: 180rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.record-type {
  font-size: $font-size-md;
  color: $text-primary;
  font-weight: 500;
}

.record-location {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.record-status {
  font-size: $font-size-xl;
}

.record-status.success {
  color: $success-color;
}
</style>
