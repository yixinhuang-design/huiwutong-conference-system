<template>
  <view class="page">
    <view class="card">
      <view class="title">页面已升级</view>
      <view class="desc">旧版网页容器已下线，请返回新版本页面继续使用。</view>
      <view class="path">请求路径：{{ requestedPath || '未提供' }}</view>
      <button class="btn-primary" @click="goHome">回到首页</button>
      <button class="btn-secondary" @click="goBack">返回上一页</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';

const requestedPath = ref('');

uni.setNavigationBarTitle({ title: '页面升级提示' });

onLoad((options) => {
  requestedPath.value = decodeURIComponent(options?.path || '');
});

const goHome = () => {
  uni.reLaunch({ url: '/pages/common/home/home' });
};

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f5f7fb; display: flex; align-items: center; justify-content: center; padding: 24rpx; }
.card { width: 100%; background: #fff; border-radius: 16rpx; padding: 28rpx; }
.title { font-size: 36rpx; font-weight: 700; color: #1e293b; }
.desc { margin-top: 12rpx; color: #64748b; font-size: 26rpx; line-height: 1.6; }
.path { margin-top: 16rpx; background: #f8fafc; padding: 12rpx; border-radius: 10rpx; color: #334155; font-size: 23rpx; }
.btn-primary, .btn-secondary { margin-top: 14rpx; border-radius: 10rpx; font-size: 26rpx; }
.btn-primary { background: #2563eb; color: #fff; border: none; }
.btn-secondary { background: #fff; color: #2563eb; border: 1rpx solid #2563eb; }
</style>
