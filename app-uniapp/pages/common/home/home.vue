<template>
  <scroll-view class="page" scroll-y>
    <view class="card user-card">
      <view class="avatar">丽</view>
      <view>
        <text class="title">上午好，丽华（学员）</text>
        <text class="sub">市委组织部｜副处长</text>
      </view>
    </view>

    <view class="notice">今日提醒：请在 09:00 前完成报到签到，下午讨论安排在 A 栋 302 室。</view>

    <view class="section-title">我参加的培训</view>
    <view class="card" v-for="m in meetings" :key="m.id">
      <view class="row">
        <text class="m-title">{{ m.name }}</text>
        <text class="chip" :class="m.status">{{ m.statusText }}</text>
      </view>
      <text class="m-meta">{{ m.date }}</text>
      <text class="m-meta">{{ m.location }}</text>
    </view>

    <view class="section-title">功能导航</view>
    <view class="grid">
      <view class="tile" v-for="item in navs" :key="item.name" @click="go(item.url)">
        <text class="tile-title">{{ item.name }}</text>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref } from 'vue'

const meetings = ref([
  {
    id: 1,
    name: '2025党务干部培训班',
    date: '1月15日 - 1月19日 · 共5天',
    location: '市委党校报告厅 · 主办：市委组织部',
    status: 'ongoing',
    statusText: '进行中'
  },
  {
    id: 2,
    name: '青年干部能力提升班',
    date: '1月22日 - 1月26日 · 共5天',
    location: '市委党校教学楼 · 主办：市委组织部',
    status: 'upcoming',
    statusText: '即将开始'
  }
])

const navs = [
  { name: '参会须知', url: '/pages/learner/guide/guide' },
  { name: '日程安排', url: '/pages/learner/schedule/schedule' },
  { name: '通讯录', url: '/pages/learner/contact/contact' },
  { name: '座位图', url: '/pages/learner/seat/seat' },
  { name: '报到签到', url: '/pages/learner/checkin/checkin' },
  { name: '群组分组', url: '/pages/learner/groups/groups' },
  { name: '资料下载', url: '/pages/learner/materials/materials' },
  { name: '精彩花絮', url: '/pages/learner/highlights/highlights' },
  { name: '留言赠语', url: '/pages/learner/message/message' }
]

const go = (url) => uni.navigateTo({ url })

onLoad(() => {
  uni.setNavigationBarTitle({ title: '首页' })
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 18rpx; }
.user-card { display: flex; gap: 16rpx; align-items: center; }
.avatar { width: 72rpx; height: 72rpx; border-radius: 14rpx; background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 700; }
.title { display: block; font-size: 32rpx; font-weight: 700; color: #111827; }
.sub { display: block; font-size: 24rpx; color: #6b7280; margin-top: 6rpx; }
.notice { background: #ecfeff; color: #0f766e; border: 1rpx solid #99f6e4; border-radius: 12rpx; padding: 16rpx; margin-bottom: 18rpx; font-size: 24rpx; }
.section-title { font-size: 28rpx; font-weight: 600; color: #1f2937; margin: 12rpx 0; }
.row { display: flex; justify-content: space-between; gap: 10rpx; align-items: center; }
.m-title { font-size: 28rpx; font-weight: 600; color: #111827; }
.m-meta { display: block; margin-top: 8rpx; font-size: 23rpx; color: #6b7280; }
.chip { font-size: 20rpx; color: #fff; padding: 4rpx 10rpx; border-radius: 999rpx; }
.chip.ongoing { background: #6366f1; }
.chip.upcoming { background: #f59e0b; }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12rpx; margin-bottom: 20rpx; }
.tile { background: #fff; border-radius: 12rpx; padding: 20rpx 10rpx; text-align: center; }
.tile-title { font-size: 24rpx; color: #374151; }
</style>
