<template>
  <view class="scan-seat-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">扫码查座</text>
        <text class="header-action" @click="viewMySeat">我的座位</text>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 扫码区域 -->
      <view class="scan-section card">
        <view class="scan-title">扫描座位二维码</view>
        <view class="scan-desc">扫描课桌或座位上的二维码查看详细信息</view>

        <view class="scan-box" @click="startScan">
          <text class="scan-icon">📷</text>
          <text class="scan-text">点击扫描座位码</text>
        </view>

        <button class="btn btn-primary btn-block" @click="startScan">
          📷 启动相机扫描
        </button>
      </view>

      <!-- 快速输入 -->
      <view class="quick-input-section card">
        <view class="input-title">或输入座位号</view>
        <view class="input-row">
          <view class="input-group">
            <text class="input-prefix">区域</text>
            <picker mode="selector" :range="areas" @change="onAreaChange">
              <view class="picker-value">{{ selectedArea || '请选择' }}</view>
            </picker>
          </view>
          <view class="input-group">
            <text class="input-prefix">排</text>
            <input
              v-model="rowNumber"
              type="number"
              placeholder="排号"
              class="number-input"
            />
          </view>
          <view class="input-group">
            <text class="input-prefix">座</text>
            <input
              v-model="seatNumber"
              type="number"
              placeholder="座号"
              class="number-input"
            />
          </view>
        </view>
        <button class="btn btn-outline btn-block" @click="querySeat">
          <text class="fa fa-search"></text> 查询座位
        </button>
      </view>

      <!-- 查询结果 -->
      <view v-if="seatInfo" class="result-section card">
        <view class="result-header">
          <text class="result-icon"><text class="fa fa-th-large"></text></text>
          <view class="seat-number">
            <text class="seat-area">{{ seatInfo.area }}</text>
            <text class="seat-code">{{ seatInfo.row }}排{{ seatInfo.seat }}座</text>
          </view>
        </view>

        <view class="seat-status" :class="'status-' + seatInfo.status">
          <text class="status-text">{{ getStatusText(seatInfo.status) }}</text>
        </view>

        <view class="seat-details">
          <view class="detail-item" v-if="seatInfo.occupant">
            <text class="detail-label">座位学员</text>
            <text class="detail-value">{{ seatInfo.occupant.name }}</text>
            <text class="detail-sub">{{ seatInfo.occupant.department }}</text>
          </view>

          <view class="detail-item">
            <text class="detail-label">座位位置</text>
            <text class="detail-value">{{ seatInfo.location }}</text>
          </view>

          <view class="detail-item">
            <text class="detail-label">座位类型</text>
            <text class="detail-value">{{ seatInfo.type }}</text>
          </view>

          <view class="detail-item" v-if="seatInfo.facilities && seatInfo.facilities.length > 0">
            <text class="detail-label">设施配备</text>
            <view class="facilities-list">
              <text
                v-for="(facility, index) in seatInfo.facilities"
                :key="index"
                class="facility-tag"
              >
                {{ facility }}
              </text>
            </view>
          </view>
        </view>

        <view class="seat-actions">
          <button
            v-if="seatInfo.status === 'available' && seatInfo.isMySeat"
            class="btn btn-primary btn-block"
            @click="confirmSeat"
          >
            ✓ 确认就座
          </button>
          <button
            v-if="seatInfo.occupant && seatInfo.occupant.id === currentUserId"
            class="btn btn-success btn-block"
            @click="viewMySeat"
          >
            <text class="fa fa-th-large"></text> 这是我的座位
          </button>
          <button
            class="btn btn-outline btn-block"
            @click="showNavigation"
          >
            <text class="fa fa-map-marker-alt"></text> 导航到此座位
          </button>
        </view>
      </view>

      <!-- 使用提示 -->
      <view class="tips-section card">
        <view class="tips-title">💡 使用提示</view>
        <view class="tips-list">
          <text class="tips-item">• 扫描座位码可快速定位座位位置</text>
          <text class="tips-item">• 也可通过区域+排号+座号查询</text>
          <text class="tips-item">• 座位信息如有疑问请联系工作人员</text>
          <text class="tips-item">• 请按照分配座位就座，请勿随意更换</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentUserId: '1',
      areas: ['A区', 'B区', 'C区', 'D区'],
      selectedArea: '',
      rowNumber: '',
      seatNumber: '',
      seatInfo: null
    }
  },

  methods: {
    startScan() {
      uni.scanCode({
        success: (res) => {
          console.log('扫码结果:', res)
          this.processSeatCode(res.result)
        },
        fail: (err) => {
          console.error('扫码失败:', err)
          uni.showToast({
            title: '扫码失败，请重试',
            icon: 'none'
          })
        }
      })
    },

    processSeatCode(code) {
      uni.showLoading({ title: '查询中...' })

      setTimeout(() => {
        uni.hideLoading()

        this.seatInfo = {
          area: 'A区',
          row: 3,
          seat: 5,
          code: code,
          status: 'occupied',
          location: '报告厅前排左侧',
          type: '普通座位',
          isMySeat: true,
          occupant: {
            id: '1',
            name: '张伟',
            department: '市教育局'
          },
          facilities: ['桌椅', '电源插座', '饮水杯']
        }

        uni.showToast({
          title: '查询成功',
          icon: 'success'
        })
      }, 1000)
    },

    onAreaChange(e) {
      this.selectedArea = this.areas[e.detail.value]
    },

    querySeat() {
      if (!this.selectedArea) {
        uni.showToast({
          title: '请选择区域',
          icon: 'none'
        })
        return
      }

      if (!this.rowNumber || !this.seatNumber) {
        uni.showToast({
          title: '请输入完整座位号',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '查询中...' })

      setTimeout(() => {
        uni.hideLoading()

        const isMySeat = this.selectedArea === 'A区' && this.rowNumber === '3' && this.seatNumber === '5'

        this.seatInfo = {
          area: this.selectedArea,
          row: parseInt(this.rowNumber),
          seat: parseInt(this.seatNumber),
          code: `${this.selectedArea}-${this.rowNumber}-${this.seatNumber}`,
          status: isMySeat ? 'occupied' : 'available',
          location: '报告厅前排左侧',
          type: '普通座位',
          isMySeat: isMySeat,
          occupant: isMySeat ? {
            id: '1',
            name: '张伟',
            department: '市教育局'
          } : null,
          facilities: ['桌椅', '电源插座', '饮水杯']
        }

        uni.showToast({
          title: '查询成功',
          icon: 'success'
        })
      }, 1000)
    },

    getStatusText(status) {
      const textMap = {
        available: '空闲',
        occupied: '已分配',
        reserved: '预留'
      }
      return textMap[status] || '未知'
    },

    confirmSeat() {
      uni.showModal({
        title: '确认就座',
        content: `确认在${this.seatInfo.area}${this.seatInfo.row}排${this.seatInfo.seat}座就座？`,
        success: (res) => {
          if (res.confirm) {
            uni.showToast({
              title: '已确认就座',
              icon: 'success'
            })
          }
        }
      })
    },

    viewMySeat() {
      uni.navigateTo({
        url: '/pages/learner/seat'
      })
    },

    showNavigation() {
      uni.navigateTo({
        url: '/pages/common/navigation'
      })
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

.scan-seat-container {
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

.content-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.scan-section {
  margin-bottom: $spacing-md;
}

.scan-title {
  display: block;
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  text-align: center;
  margin-bottom: $spacing-sm;
}

.scan-desc {
  display: block;
  font-size: $font-size-md;
  color: $text-secondary;
  text-align: center;
  margin-bottom: $spacing-lg;
}

.scan-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;
  padding: 80rpx $spacing-lg;
  background: $bg-tertiary;
  border: 2rpx dashed $border-color;
  border-radius: $border-radius-lg;
  margin-bottom: $spacing-lg;
}

.scan-icon {
  font-size: 120rpx;
}

.scan-text {
  font-size: $font-size-md;
  color: $text-tertiary;
}

.quick-input-section {
  margin-bottom: $spacing-md;
}

.input-title {
  display: block;
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  text-align: center;
  margin-bottom: $spacing-md;
}

.input-row {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.input-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.input-prefix {
  font-size: $font-size-sm;
  color: $text-secondary;
  white-space: nowrap;
}

.picker-value {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.number-input {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.result-section {
  margin-bottom: $spacing-md;
}

.result-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.result-icon {
  font-size: 64rpx;
}

.seat-number {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.seat-area {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.seat-code {
  font-size: $font-size-lg;
  color: $text-primary;
}

.seat-status {
  display: flex;
  justify-content: center;
  padding: $spacing-md;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-lg;
}

.seat-status.status-available {
  background: rgba(16, 185, 129, 0.1);
}

.seat-status.status-available .status-text {
  color: #10b981;
}

.seat-status.status-occupied {
  background: rgba(245, 158, 11, 0.1);
}

.seat-status.status-occupied .status-text {
  color: #f59e0b;
}

.seat-status.status-reserved {
  background: rgba(148, 163, 184, 0.1);
}

.seat-status.status-reserved .status-text {
  color: $text-tertiary;
}

.status-text {
  font-size: $font-size-lg;
  font-weight: 600;
}

.seat-details {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.detail-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.detail-value {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.detail-sub {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.facilities-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
}

.facility-tag {
  padding: 4rpx 12rpx;
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
  font-size: $font-size-sm;
  border-radius: $border-radius-sm;
}

.seat-actions {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.btn-success {
  background: #10b981;
  color: $text-white;
}

.tips-section {
  margin-bottom: $spacing-lg;
}

.tips-title {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.tips-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.8;
}
</style>
