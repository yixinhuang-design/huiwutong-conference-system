<template>
  <view class="seat-detail-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">座位详情</text>
        <text class="header-action" @click="shareSeat">分享</text>
      </view>
    </view>

    <scroll-view class="detail-scroll" scroll-y>
      <!-- 座位信息卡片 -->
      <view class="seat-card card">
        <view class="seat-header">
          <view class="seat-icon"><text class="fa fa-th-large"></text></view>
          <view class="seat-info">
            <text class="seat-area">{{ seatInfo.area }}</text>
            <text class="seat-number">{{ seatInfo.row }}排{{ seatInfo.seat }}座</text>
          </view>
          <view class="seat-badge" :class="'badge-' + seatInfo.status">
            {{ getStatusText(seatInfo.status) }}
          </view>
        </view>

        <view class="seat-meta">
          <view class="meta-item">
            <text class="meta-icon"><text class="fa fa-map-marker-alt"></text></text>
            <text class="meta-text">{{ seatInfo.location }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon">🏷️</text>
            <text class="meta-text">{{ seatInfo.type }}</text>
          </view>
        </view>
      </view>

      <!-- 占用信息 -->
      <view v-if="seatInfo.occupant" class="occupant-section card">
        <view class="section-title">占用学员</view>
        <view class="occupant-card">
          <view class="occupant-avatar">
            {{ seatInfo.occupant.name.charAt(seatInfo.occupant.name.length - 1) }}
          </view>
          <view class="occupant-info">
            <text class="occupant-name">{{ seatInfo.occupant.name }}</text>
            <text class="occupant-dept">{{ seatInfo.occupant.department }}</text>
            <text class="occupant-position">{{ seatInfo.occupant.position }}</text>
          </view>
        </view>

        <view class="occupant-actions" v-if="!seatInfo.isMySeat">
          <button class="btn btn-outline btn-block btn-sm" @click="viewProfile">
            <text class="fa fa-user"></text> 查看资料
          </button>
          <button class="btn btn-primary btn-block btn-sm" @click="startChat">
            <text class="fa fa-comments"></text> 发消息
          </button>
        </view>
      </view>

      <!-- 周围学员 -->
      <view class="neighbors-section card">
        <view class="section-title">周围学员</view>
        <view class="neighbors-grid">
          <view
            v-for="(neighbor, index) in seatInfo.neighbors"
            :key="index"
            class="neighbor-item"
            @click="viewNeighbor(neighbor)"
          >
            <view class="neighbor-avatar">
              {{ neighbor.name.charAt(neighbor.name.length - 1) }}
            </view>
            <text class="neighbor-name">{{ neighbor.name }}</text>
            <text class="neighbor-position">{{ neighbor.relativePos }}</text>
          </view>
        </view>
      </view>

      <!-- 设施配置 -->
      <view class="facilities-section card">
        <view class="section-title">设施配备</view>
        <view class="facilities-list">
          <view
            v-for="(facility, index) in seatInfo.facilities"
            :key="index"
            class="facility-item"
          >
            <text class="facility-icon">{{ facility.icon }}</text>
            <view class="facility-info">
              <text class="facility-name">{{ facility.name }}</text>
              <text class="facility-desc">{{ facility.description }}</text>
            </view>
            <view
              class="facility-status"
              :class="facility.available ? 'available' : 'unavailable'"
            >
              {{ facility.available ? '可用' : '占用' }}
            </view>
          </view>
        </view>
      </view>

      <!-- 座位照片 -->
      <view class="photos-section card" v-if="seatInfo.photos && seatInfo.photos.length > 0">
        <view class="section-title">座位照片</view>
        <scroll-view class="photos-scroll" scroll-x>
          <view class="photos-list">
            <view
              v-for="(photo, index) in seatInfo.photos"
              :key="index"
              class="photo-item"
              @click="previewPhoto(index)"
            >
              <image class="photo-image" :src="photo" mode="aspectFill"></image>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 操作按钮 -->
      <view class="actions-section">
        <button
          v-if="seatInfo.isMySeat"
          class="btn btn-primary btn-block"
          @click="showNavigation"
        >
          <text class="fa fa-map-marker-alt"></text> 导航到此座位
        </button>
        <button
          class="btn btn-outline btn-block"
          @click="viewSeatMap"
        >
          🗺️ 查看座位图
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      seatId: '',
      seatInfo: {
        area: 'A区',
        row: 3,
        seat: 5,
        status: 'occupied',
        location: '报告厅前排左侧靠窗',
        type: '普通座位',
        isMySeat: true,
        occupant: {
          id: '1',
          name: '张伟',
          department: '市教育局',
          position: '科长'
        },
        neighbors: [
          { id: '2', name: '李娜', department: '市卫健委', position: '副科长', relativePos: '前排' },
          { id: '3', name: '王强', department: '市委组织部', position: '主任', relativePos: '右侧' },
          { id: '4', name: '刘芳', department: '市财政局', position: '科员', relativePos: '后排' }
        ],
        facilities: [
          { icon: '<text class="fa fa-th-large"></text>', name: '桌椅', description: '培训桌椅', available: true },
          { icon: '🔌', name: '电源插座', description: '220V电源', available: true },
          { icon: '🥤', name: '饮水杯', description: '一次性水杯', available: true },
          { icon: '<text class="fa fa-edit"></text>', name: '学习用品', description: '笔、本子', available: true }
        ],
        photos: [
          'https://via.placeholder.com/300x200?text=座位正面',
          'https://via.placeholder.com/300x200?text=座位侧面',
          'https://via.placeholder.com/300x200?text=周围环境'
        ]
      }
    }
  },

  onLoad(options) {
    if (options.id) {
      this.seatId = options.id
      this.loadSeatDetail()
    }
  },

  methods: {
    loadSeatDetail() {
      uni.showLoading({ title: '加载中...' })
      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    getStatusText(status) {
      const textMap = {
        available: '空闲',
        occupied: '已占用',
        reserved: '预留'
      }
      return textMap[status] || '未知'
    },

    viewProfile() {
      uni.showToast({ title: '查看资料功能开发中', icon: 'none' })
    },

    startChat() {
      if (this.seatInfo.occupant) {
        uni.navigateTo({
          url: `/pages/learner/chat-private?id=${this.seatInfo.occupant.id}&name=${this.seatInfo.occupant.name}`
        })
      }
    },

    viewNeighbor(neighbor) {
      uni.showModal({
        title: neighbor.name,
        content: `${neighbor.department}\n${neighbor.position}\n${neighbor.relativePos}`,
        showCancel: false
      })
    },

    showNavigation() {
      uni.navigateTo({
        url: '/pages/common/navigation'
      })
    },

    viewSeatMap() {
      uni.navigateTo({
        url: '/pages/learner/seat'
      })
    },

    previewPhoto(index) {
      uni.previewImage({
        current: index,
        urls: this.seatInfo.photos
      })
    },

    shareSeat() {
      uni.showActionSheet({
        itemList: ['分享给好友', '生成二维码'],
        success: (res) => {
          uni.showToast({ title: '分享功能开发中', icon: 'none' })
        }
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

.seat-detail-container {
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

.detail-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.card {
  margin-bottom: $spacing-md;
}

.seat-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.seat-icon {
  font-size: 64rpx;
}

.seat-info {
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

.seat-number {
  font-size: $font-size-lg;
  color: $text-primary;
}

.seat-badge {
  padding: 8rpx 16rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  font-weight: 500;
}

.seat-badge.badge-available {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.seat-badge.badge-occupied {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.seat-badge.badge-reserved {
  background: rgba(148, 163, 184, 0.1);
  color: $text-tertiary;
}

.seat-meta {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.meta-icon {
  font-size: $font-size-md;
}

.meta-text {
  font-size: $font-size-md;
  color: $text-secondary;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.occupant-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.occupant-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-lg;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.occupant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.occupant-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.occupant-dept {
  font-size: $font-size-md;
  color: $text-secondary;
}

.occupant-position {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.occupant-actions {
  display: flex;
  gap: $spacing-sm;
}

.btn-sm {
  padding: $spacing-sm $spacing-md;
  font-size: $font-size-sm;
}

.neighbors-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.neighbor-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.neighbor-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: $text-white;
  font-size: $font-size-md;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.neighbor-name {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.neighbor-position {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.facilities-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.facility-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.facility-icon {
  font-size: 48rpx;
}

.facility-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.facility-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.facility-desc {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.facility-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.facility-status.available {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.facility-status.unavailable {
  background: rgba(148, 163, 184, 0.1);
  color: $text-tertiary;
}

.photos-scroll {
  width: 100%;
  white-space: nowrap;
}

.photos-list {
  display: inline-flex;
  gap: $spacing-sm;
}

.photo-item {
  width: 300rpx;
  height: 200rpx;
  border-radius: $border-radius-md;
  overflow: hidden;
  flex-shrink: 0;
}

.photo-image {
  width: 100%;
  height: 100%;
}

.actions-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}
</style>
