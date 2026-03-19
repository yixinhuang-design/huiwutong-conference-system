<template>
  <view class="page">
    <view class="card">
      <text class="title">设置</text>
      <text class="sub">管理应用设置和偏好</text>
    </view>

    <view class="card">
      <text class="sec">通知设置</text>
      <view class="row"><text>推送通知</text><switch :checked="form.push" @change="setSwitch('push', $event)" /></view>
      <view class="row"><text>短信通知</text><switch :checked="form.sms" @change="setSwitch('sms', $event)" /></view>
      <view class="row"><text>免打扰时段</text><switch :checked="form.dnd" @change="setSwitch('dnd', $event)" /></view>
    </view>

    <view class="card">
      <text class="sec">显示设置</text>
      <view class="row"><text>深色模式</text><switch :checked="form.dark" @change="setSwitch('dark', $event)" /></view>
      <view class="row" @click="showTip('字体大小：标准')"><text>字体大小</text><text class="arrow">></text></view>
      <view class="row" @click="showTip('语言：简体中文')"><text>语言</text><text class="arrow">></text></view>
    </view>

    <view class="card">
      <text class="sec">存储与缓存</text>
      <view class="row" @click="clearCache"><text>清除缓存</text><text>{{ cacheSize }}</text></view>
      <view class="row" @click="showTip('开始下载离线数据')"><text>离线数据</text><text class="arrow">></text></view>
    </view>

    <button class="logout" @click="logout">退出登录</button>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'

const cacheSize = ref('128 MB')
const form = reactive({ push: true, sms: true, dnd: true, dark: false })

const setSwitch = (key, e) => {
  form[key] = e.detail.value
  uni.setStorageSync(`setting_${key}`, String(form[key]))
}

const clearCache = () => {
  cacheSize.value = '0 MB'
  uni.showToast({ title: '缓存已清除', icon: 'success' })
}

const showTip = (msg) => uni.showToast({ title: msg, icon: 'none' })

const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (!res.confirm) return
      uni.removeStorageSync('token')
      uni.reLaunch({ url: '/pages/common/login/login' })
    }
  })
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '设置' })
  Object.keys(form).forEach((k) => {
    const val = uni.getStorageSync(`setting_${k}`)
    if (val !== '') form[k] = val === 'true'
  })
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 14rpx; padding: 20rpx; margin-bottom: 16rpx; }
.title { display: block; font-size: 32rpx; font-weight: 700; }
.sub { display: block; margin-top: 8rpx; font-size: 24rpx; color: #6b7280; }
.sec { display: block; font-size: 27rpx; font-weight: 600; margin-bottom: 8rpx; }
.row { display: flex; justify-content: space-between; align-items: center; padding: 14rpx 0; border-bottom: 1rpx solid #f1f5f9; font-size: 26rpx; color: #1f2937; }
.row:last-child { border-bottom: none; }
.arrow { color: #9ca3af; }
.logout { background: #e11d48; color: #fff; border-radius: 12rpx; font-size: 28rpx; }
</style>
