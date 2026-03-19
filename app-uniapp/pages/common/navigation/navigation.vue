<template>
  <view class="page">
    <view class="card">
      <text class="title">位置导航</text>
      <text class="sub">快速找到会场位置</text>
    </view>

    <view class="card">
      <text class="sec">目标位置</text>
      <text class="line">市委党校报告厅</text>
      <text class="line">详细地址：XX市XX区XX路XX号</text>
      <text class="line">所在楼层：1层东侧</text>
      <text class="line">入口位置：正门大厅</text>
    </view>

    <view class="card map">
      <text class="map-icon">🗺️</text>
      <text class="map-text">地图加载区（可接入高德/百度地图）</text>
    </view>

    <view class="route blue" @click="choose('walk')">
      <text class="r-title">步行路线（推荐）</text>
      <text class="r-desc">约500米 · 预计7分钟到达</text>
    </view>
    <view class="route gray" @click="choose('drive')">
      <text class="r-title">自驾路线</text>
      <text class="r-desc">约2.5公里 · 预计10分钟到达</text>
    </view>
    <view class="route green" @click="choose('bus')">
      <text class="r-title">公交路线</text>
      <text class="r-desc">3站 · 预计15分钟到达</text>
    </view>

    <view class="card">
      <text class="sec">周边设施</text>
      <text class="line">停车场：距离200米 · 共120个车位</text>
      <text class="line">餐厅：党校食堂 · 1层西侧</text>
      <text class="line">休息区：2层大厅 · 提供茶水</text>
    </view>

    <button class="start" @click="startNav">开始导航</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const navMode = ref('walk')

const choose = (mode) => {
  navMode.value = mode
  uni.showToast({ title: `已选择${mode === 'walk' ? '步行' : mode === 'drive' ? '自驾' : '公交'}路线`, icon: 'none' })
}

const startNav = () => {
  uni.showToast({ title: '导航已开始（地图API待接入）', icon: 'none' })
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '位置导航' })
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 14rpx; padding: 20rpx; margin-bottom: 14rpx; }
.title { display: block; font-size: 32rpx; font-weight: 700; color: #111827; }
.sub { display: block; margin-top: 8rpx; font-size: 24rpx; color: #6b7280; }
.sec { display: block; font-size: 27rpx; font-weight: 600; color: #1f2937; margin-bottom: 10rpx; }
.line { display: block; font-size: 24rpx; color: #4b5563; margin-top: 6rpx; }
.map { text-align: center; background: #e2e8f0; }
.map-icon { display: block; font-size: 56rpx; }
.map-text { display: block; margin-top: 8rpx; color: #64748b; font-size: 22rpx; }
.route { border-radius: 12rpx; padding: 16rpx; margin-bottom: 10rpx; color: #fff; }
.route.blue { background: #3b82f6; }
.route.gray { background: #64748b; }
.route.green { background: #059669; }
.r-title { display: block; font-size: 27rpx; font-weight: 600; }
.r-desc { display: block; margin-top: 6rpx; font-size: 23rpx; opacity: 0.9; }
.start { margin-top: 8rpx; background: #2563eb; color: #fff; border-radius: 12rpx; font-size: 28rpx; }
</style>
