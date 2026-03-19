<template>
  <view class="page">
    <view class="card user">
      <view class="avatar">李</view>
      <text class="name">李华（学员）</text>
      <text class="dept">市委组织部｜副处长</text>
    </view>

    <view class="card menu">
      <view class="item" @click="go('/pages/learner/schedule/schedule')">
        <text>我的日程</text>
        <text class="arrow">></text>
      </view>
      <view class="item" @click="go('/pages/learner/task-list/task-list')">
        <text>我的任务</text>
        <text class="arrow">></text>
      </view>
      <view class="item" @click="showAllFeature">
        <text>全部功能</text>
        <text class="arrow">></text>
      </view>
      <view class="item" @click="go('/pages/learner/notifications/notifications')">
        <text>我的提醒</text>
        <text class="arrow">></text>
      </view>
    </view>

    <view class="section">我的设置</view>
    <view class="card menu">
      <view class="item" @click="showRole"><text>账号与身份</text><text class="desc">普通学员</text></view>
      <view class="item" @click="go('/pages/common/settings/settings')"><text>密码与安全</text><text class="arrow">></text></view>
      <view class="item" @click="go('/pages/common/settings/settings')"><text>通知权限</text><text class="arrow">></text></view>
    </view>

    <button class="logout" @click="logout">退出当前账号</button>
  </view>
</template>

<script setup>
const go = (url) => uni.navigateTo({ url })

const showAllFeature = () => uni.showToast({ title: '功能总览入口已预留', icon: 'none' })
const showRole = () => uni.showToast({ title: '当前角色：普通学员', icon: 'none' })

const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '是否退出当前账号？',
    success: (res) => {
      if (!res.confirm) return
      uni.removeStorageSync('token')
      uni.reLaunch({ url: '/pages/common/login/login' })
    }
  })
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '我的' })
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 18rpx; }
.user { text-align: center; }
.avatar { width: 90rpx; height: 90rpx; line-height: 90rpx; margin: 0 auto; border-radius: 20rpx; background: linear-gradient(135deg, #38bdf8, #0ea5e9); color: #fff; font-size: 36rpx; font-weight: 700; }
.name { display: block; margin-top: 12rpx; font-size: 30rpx; font-weight: 700; color: #111827; }
.dept { display: block; margin-top: 6rpx; font-size: 24rpx; color: #6b7280; }
.menu .item { display: flex; justify-content: space-between; align-items: center; padding: 18rpx 0; border-bottom: 1rpx solid #f1f5f9; font-size: 27rpx; color: #1f2937; }
.menu .item:last-child { border-bottom: none; }
.arrow { color: #9ca3af; }
.desc { color: #6b7280; font-size: 23rpx; }
.section { font-size: 27rpx; font-weight: 600; color: #374151; margin: 10rpx 0; }
.logout { background: #e11d48; color: #fff; border-radius: 12rpx; font-size: 28rpx; }
</style>
