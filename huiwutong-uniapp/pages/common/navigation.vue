<template>
  <view class="navigation-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">位置导航</text>
      </view>
    </view>

    <!-- 当前位置 -->
    <view class="current-location card">
      <view class="location-header">
        <text class="location-icon">📍</text>
        <text class="location-text">当前位置</text>
      </view>
      <view class="location-address">{{ currentAddress }}</view>
      <button class="refresh-btn btn btn-primary btn-block" @click="refreshLocation">
        📍 刷新位置
      </button>
    </view>

    <!-- 目的地点 -->
    <view class="destination-card card">
      <view class="section-title">导航到</view>
      <view class="destination-list">
        <view
          v-for="(dest, index) in destinations"
          :key="index"
          class="destination-item"
          @click="navigateTo(dest)"
        >
          <view class="dest-icon" :style="{ background: dest.color }">
            <text>{{ dest.icon }}</text>
          </view>
          <view class="dest-info">
            <text class="dest-name">{{ dest.name }}</text>
            <text class="dest-distance">{{ dest.distance }}</text>
          </view>
          <text class="dest-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 地图预览 -->
    <view class="map-preview card">
      <view class="section-title">
        <text>会场地图</text>
        <text class="section-action" @click="openFullMap">查看完整地图 ›</text>
      </view>
      <view class="map-placeholder">
        <text class="map-icon">🗺️</text>
        <text class="map-text">会场平面图</text>
      </view>
    </view>

    <!-- 路线信息 -->
    <view class="route-info card" v-if="selectedDestination">
      <view class="section-title">路线信息</view>
      <view class="route-details">
        <view class="route-item">
          <text class="route-label">目的地：</text>
          <text class="route-value">{{ selectedDestination.name }}</text>
        </view>
        <view class="route-item">
          <text class="route-label">距离：</text>
          <text class="route-value">{{ selectedDestination.distance }}</text>
        </view>
        <view class="route-item">
          <text class="route-label">预计用时：</text>
          <text class="route-value">{{ selectedDuration }}</text>
        </view>
      </view>
      <button class="start-nav-btn btn btn-primary btn-block" @click="startNavigation">
        🚀 开始导航
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentAddress: '获取中...',
      selectedDestination: null,
      selectedDuration: '约3分钟',
      destinations: [
        { id: 1, name: '报告厅', icon: '🏛️', distance: '50米', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
        { id: 2, name: 'A栋302教室', icon: '🏫', distance: '150米', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
        { id: 3, name: '餐厅', icon: '🍽️', distance: '200米', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
        { id: 4, name: '宿舍楼', icon: '🏠', distance: '300米', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
      ]
    }
  },

  methods: {
    async getCurrentLocation() {
      try {
        const res = await uni.getLocation({ type: 'gcj02' })
        this.currentAddress = `${res.latitude.toFixed(4)}, ${res.longitude.toFixed(4)}`
      } catch (error) {
        this.currentAddress = '无法获取位置'
      }
    },

    refreshLocation() {
      uni.showLoading({ title: '定位中...' })
      this.getCurrentLocation()
      setTimeout(() => uni.hideLoading(), 1000)
    },

    navigateTo(dest) {
      this.selectedDestination = dest
      uni.showModal({
        title: '开始导航',
        content: `导航到：${dest.name}\n距离：${dest.distance}`,
        success: (res) => {
          if (res.confirm) this.startNavigation()
        }
      })
    },

    startNavigation() {
      uni.showToast({ title: '导航功能开发中', icon: 'none' })
    },

    openFullMap() {
      uni.showToast({ title: '完整地图功能开发中', icon: 'none' })
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

.navigation-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
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

.current-location,
.destination-card,
.map-preview,
.route-info {
  margin: $spacing-md;
}
</style>
