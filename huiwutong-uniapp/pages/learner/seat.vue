<template>
  <view class="seat-container">
    <!-- 自定义状态栏 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">座位查询</text>
      </view>
    </view>

    <!-- 我的座位信息 -->
    <view v-if="mySeat" class="my-seat-card card">
      <view class="section-title">我的座位</view>
      <view class="seat-info">
        <view class="seat-number">{{ mySeat.number }}</view>
        <view class="seat-details">
          <text class="detail-item">📍 {{ mySeat.area }}</text>
          <text class="detail-item">🪑 第{{ mySeat.row }}行 第{{ mySeat.column }}列</text>
        </view>
      </view>
      <button class="nav-btn btn btn-primary btn-block" @click="navigateToSeat">
        <text>🧭</text> 导航到座位
      </button>
    </view>

    <!-- 座位图 -->
    <view class="seat-map-card card">
      <view class="section-title">
        <text>座位图</text>
        <text class="section-subtitle">{{ meetingInfo.title }}</text>
      </view>

      <!-- 图例 -->
      <view class="legend">
        <view class="legend-item">
          <view class="legend-color available"></view>
          <text class="legend-text">可选</text>
        </view>
        <view class="legend-item">
          <view class="legend-color occupied"></view>
          <text class="legend-text">已占</text>
        </view>
        <view class="legend-item">
          <view class="legend-color mine"></view>
          <text class="legend-text">我的</text>
        </view>
      </view>

      <!-- 座位网格 -->
      <scroll-view class="seat-scroll" scroll-x scroll-y>
        <view class="seat-grid">
          <view
            v-for="(seat, index) in seatList"
            :key="index"
            class="seat-item"
            :class="getSeatClass(seat)"
            @click="handleSeatClick(seat)"
          >
            <text class="seat-number">{{ seat.row }}-{{ seat.col }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 座位详情弹窗 -->
    <view v-if="selectedSeat" class="seat-modal" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">座位详情</text>
          <text class="modal-close" @click="closeModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="detail-row">
            <text class="detail-label">座位号</text>
            <text class="detail-value">{{ selectedSeat.row }}-{{ selectedSeat.col }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">区域</text>
            <text class="detail-value">{{ selectedSeat.area }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">状态</text>
            <text class="detail-value" :class="getStatusClass(selectedSeat)">
              {{ getStatusText(selectedSeat) }}
            </text>
          </view>
          <view v-if="selectedSeat.user" class="detail-row">
            <text class="detail-label">占用者</text>
            <text class="detail-value">{{ selectedSeat.user }}</text>
          </view>
        </view>
        <view class="modal-footer">
          <button class="btn btn-secondary btn-block" @click="closeModal">
            关闭
          </button>
        </view>
      </view>
    </view>

    <!-- 功能按钮 -->
    <view class="action-buttons">
      <button class="action-btn btn btn-outline" @click="scanQRCode">
        <text>📱</text> 扫码查座
      </button>
      <button class="action-btn btn btn-outline" @click="refreshSeats">
        <text>🔄</text> 刷新
      </button>
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
        location: '市委党校报告厅'
      },
      mySeat: null,
      selectedSeat: null,
      seatList: []
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
      await this.loadMySeat()
      await this.loadSeatMap()
    },

    /**
     * 加载我的座位
     */
    async loadMySeat() {
      try {
        // TODO: 调用API获取我的座位
        // const res = await this.$api.meeting.getMySeat(this.meetingId)

        // 模拟数据
        this.mySeat = {
          number: 'A-15',
          area: 'A区',
          row: 3,
          column: 5
        }
      } catch (error) {
        console.error('Load my seat error:', error)
      }
    },

    /**
     * 加载座位图
     */
    async loadSeatMap() {
      try {
        uni.showLoading({ title: '加载中...' })

        // TODO: 调用API获取座位图
        // const res = await this.$api.meeting.getSeats(this.meetingId)

        // 模拟数据 - 生成10x8的座位网格
        const seats = []
        const rows = 10
        const cols = 8

        for (let r = 1; r <= rows; r++) {
          for (let c = 1; c <= cols; c++) {
            const isMine = this.mySeat && this.mySeat.row === r && this.mySeat.column === c
            const isOccupied = Math.random() > 0.3 && !isMine

            seats.push({
              row: r,
              col: c,
              area: r <= 5 ? 'A区' : 'B区',
              status: isMine ? 'mine' : (isOccupied ? 'occupied' : 'available'),
              user: isOccupied ? `学员${r}${c}` : null
            })
          }
        }

        this.seatList = seats
      } catch (error) {
        console.error('Load seat map error:', error)
      } finally {
        uni.hideLoading()
      }
    },

    /**
     * 获取座位样式类
     */
    getSeatClass(seat) {
      return seat.status
    },

    /**
     * 获取状态文本
     */
    getStatusText(seat) {
      const textMap = {
        available: '可选',
        occupied: '已占用',
        mine: '我的座位'
      }
      return textMap[seat.status] || '未知'
    },

    /**
     * 获取状态样式类
     */
    getStatusClass(seat) {
      const classMap = {
        available: 'text-success',
        occupied: 'text-secondary',
        mine: 'text-primary'
      }
      return classMap[seat.status] || ''
    },

    /**
     * 点击座位
     */
    handleSeatClick(seat) {
      this.selectedSeat = seat
    },

    /**
     * 关闭弹窗
     */
    closeModal() {
      this.selectedSeat = null
    },

    /**
     * 导航到座位
     */
    navigateToSeat() {
      if (!this.mySeat) return

      uni.navigateTo({
        url: `/pages/common/navigation?seat=${this.mySeat.number}`
      })
    },

    /**
     * 扫码查座
     */
    scanQRCode() {
      uni.scanCode({
        success: (res) => {
          console.log('Scan result:', res)
          // TODO: 解析二维码并跳转到座位
          uni.showToast({
            title: '扫码成功',
            icon: 'success'
          })
        },
        fail: () => {
          uni.showToast({
            title: '扫码失败',
            icon: 'none'
          })
        }
      })
    },

    /**
     * 刷新座位图
     */
    async refreshSeats() {
      await this.loadSeatMap()

      uni.showToast({
        title: '刷新成功',
        icon: 'success'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.seat-container {
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

.my-seat-card,
.seat-map-card {
  margin: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.section-subtitle {
  font-size: $font-size-sm;
  font-weight: 400;
  color: $text-secondary;
}

.seat-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.seat-number {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-lg;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-xl;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seat-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.detail-item {
  font-size: $font-size-sm;
  color: $text-primary;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.nav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
}

.legend {
  display: flex;
  gap: $spacing-lg;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.legend-color {
  width: 32rpx;
  height: 32rpx;
  border-radius: $border-radius-xs;
}

.legend-color.available {
  background: #d1fae5;
}

.legend-color.occupied {
  background: #fee2e2;
}

.legend-color.mine {
  background: $primary-gradient;
}

.legend-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.seat-scroll {
  width: 100%;
  height: 600rpx;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(8, 80rpx);
  gap: 12rpx;
  padding: $spacing-md;
}

.seat-item {
  width: 80rpx;
  height: 80rpx;
  border-radius: $border-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-xs;
  font-weight: 600;
  transition: $transition-base;
}

.seat-item.available {
  background: #d1fae5;
  color: $success-color;
}

.seat-item.occupied {
  background: #fee2e2;
  color: $danger-color;
}

.seat-item.mine {
  background: $primary-gradient;
  color: $text-white;
  box-shadow: $shadow-md;
}

.seat-item:active {
  transform: scale(0.9);
}

.seat-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: $z-index-modal;
}

.modal-content {
  width: 80%;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color;
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
  padding: $spacing-lg;
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.detail-value {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.detail-value.text-success {
  color: $success-color;
}

.detail-value.text-primary {
  color: $primary-color;
}

.modal-footer {
  padding: $spacing-md;
  border-top: 1rpx solid $border-color;
}

.action-buttons {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
}

.text-success {
  color: $success-color;
}

.text-secondary {
  color: $text-secondary;
}

.text-primary {
  color: $primary-color;
}
</style>
