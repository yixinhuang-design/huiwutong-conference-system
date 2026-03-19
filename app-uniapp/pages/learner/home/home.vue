<template>
  <scroll-view class="page" scroll-y>
    <view class="card">
      <text class="title">学员首页</text>
      <text class="sub">欢迎回来，李华</text>
    </view>

    <view class="card remind">
      <text class="r-title">今日待办</text>
      <text class="r-item">1. 完成签到（09:00 前）</text>
      <text class="r-item">2. 完成讲师评价（22:00 前）</text>
    </view>

    <view class="card">
      <text class="sec">快捷入口</text>
      <view class="grid">
        <view class="tile" @click="go('/pages/learner/checkin/checkin')">签到</view>
        <view class="tile" @click="go('/pages/learner/task-list/task-list')">任务</view>
        <view class="tile" @click="go('/pages/learner/schedule/schedule')">日程</view>
        <view class="tile" @click="go('/pages/learner/seat/seat')">座位</view>
      </view>
    </view>

    <view class="card" v-for="m in myMeetings" :key="m.id">
      <view class="row">
        <text class="m-title">{{ m.name }}</text>
        <text class="status">{{ m.status }}</text>
      </view>
      <text class="m-meta">{{ m.date }}</text>
      <text class="m-meta">{{ m.location }}</text>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref } from 'vue'

const myMeetings = ref([
  { id: 1, name: '2025党务干部培训班', status: '进行中', date: '1月15日 - 1月19日', location: '市委党校报告厅' },
  { id: 2, name: '青年干部能力提升班', status: '即将开始', date: '1月22日 - 1月26日', location: '市委党校教学楼' }
])

const go = (url) => uni.navigateTo({ url })

onLoad(() => {
  uni.setNavigationBarTitle({ title: '学员首页' })
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 18rpx; }
.title { display: block; font-size: 34rpx; font-weight: 700; color: #111827; }
.sub { display: block; margin-top: 8rpx; color: #6b7280; font-size: 24rpx; }
.remind { background: #eff6ff; border: 1rpx solid #bfdbfe; }
.r-title { display: block; font-size: 26rpx; font-weight: 600; color: #1e3a8a; margin-bottom: 8rpx; }
.r-item { display: block; font-size: 24rpx; color: #1e40af; margin-top: 6rpx; }
.sec { display: block; font-size: 27rpx; font-weight: 600; margin-bottom: 10rpx; }
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12rpx; }
.tile { background: #f8fafc; border-radius: 10rpx; text-align: center; padding: 18rpx 8rpx; color: #2563eb; font-size: 24rpx; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; }
.m-title { font-size: 28rpx; font-weight: 600; color: #111827; }
.status { font-size: 20rpx; color: #f59e0b; }
.m-meta { display: block; margin-top: 8rpx; font-size: 23rpx; color: #6b7280; }
</style>
