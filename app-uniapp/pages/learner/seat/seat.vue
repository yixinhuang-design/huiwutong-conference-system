<template>
  <view class="page">
    <view class="card">
      <text class="title">座位信息</text>
      <text class="sub">请按分配座位入场</text>
    </view>

    <view class="card">
      <text class="sec">我的座位</text>
      <text class="seat">前区 · 第 3 排 · 12 座</text>
      <text class="status">已签到</text>
      <button class="btn" size="mini" @click="goGroups">查看分组</button>
    </view>

    <view class="card">
      <text class="sec">会场导航</text>
      <text class="line">报告厅路线：步行约 5 分钟，沿教学楼东侧主路进入</text>
      <text class="line">分组讨论室：A 栋三层 302 室</text>
      <button class="btn primary" size="mini" @click="goNavigation">导航前往</button>
    </view>

    <view class="card">
      <text class="sec">座位图入口</text>
      <button class="btn" @click="openSeatDetail">查看座位图详情</button>
    </view>
  </view>
</template>

<script setup>
const goGroups = () => uni.navigateTo({ url: '/pages/learner/groups/groups' })
const goNavigation = () => uni.navigateTo({ url: '/pages/common/navigation/navigation' })
const openSeatDetail = () => uni.navigateTo({ url: '/pages/learner/seat-detail/seat-detail' })

onLoad(() => {
  uni.setNavigationBarTitle({ title: '座位图' })
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 14rpx; padding: 20rpx; margin-bottom: 16rpx; }
.title { display: block; font-size: 32rpx; font-weight: 700; color: #111827; }
.sub { display: block; margin-top: 8rpx; font-size: 24rpx; color: #6b7280; }
.sec { display: block; font-size: 27rpx; font-weight: 600; margin-bottom: 10rpx; }
.seat { display: block; font-size: 30rpx; font-weight: 700; color: #1d4ed8; margin-top: 6rpx; }
.status { display: inline-block; margin-top: 8rpx; background: #dcfce7; color: #166534; border-radius: 999rpx; padding: 4rpx 10rpx; font-size: 20rpx; }
.line { display: block; margin-top: 8rpx; font-size: 24rpx; color: #374151; }
.btn { margin-top: 12rpx; border: 1rpx solid #d1d5db; background: #fff; color: #374151; }
.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; }
</style>
