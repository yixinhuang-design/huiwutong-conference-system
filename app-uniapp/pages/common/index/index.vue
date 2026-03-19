<template>
  <view class="container">
    <view class="card">
      <view class="title">智能会议助手（uni-app）</view>
      <view class="desc">已完成核心页面改造，并保留原型页面兜底入口。</view>
    </view>

    <view class="card">
      <view class="section">核心改造页面</view>
      <button class="btn" @click="go('/pages/common/login/login')">登录页</button>
      <button class="btn" @click="go('/pages/learner/scan-register/scan-register?conferenceId=2030309010523144200')">学员扫码报名</button>
      <button class="btn" @click="go('/pages/staff/training-create/training-create')">协管员新建培训</button>
    </view>

    <view class="card">
      <view class="section">原型页面兜底（legacy）</view>
      <button class="btn" @click="openLegacy('common/index.html')">原型首页</button>
      <button class="btn" @click="openLegacy('learner/scan-register.html?conferenceId=2030309010523144200')">原型扫码报名</button>
      <button class="btn" @click="openLegacy('staff/training-create.html')">原型新建培训</button>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    go(url) {
      uni.navigateTo({ url });
    },
    openLegacy(path) {
      uni.navigateTo({
        url: `/pages/legacy/webview/index?path=${encodeURIComponent(path)}`
      });
    }
  }
};
</script>

<style scoped>
.title { font-size: 36rpx; font-weight: 700; margin-bottom: 10rpx; }
.desc { color: #64748b; font-size: 26rpx; }
.section { font-size: 30rpx; font-weight: 600; margin-bottom: 20rpx; }
.btn { margin-bottom: 16rpx; font-size: 28rpx; }
</style>
