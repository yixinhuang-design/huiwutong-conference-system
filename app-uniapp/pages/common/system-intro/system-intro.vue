<template>
  <scroll-view scroll-y class="page">
    <view class="hero">
      <view class="hero-title">智能会议系统</view>
      <view class="hero-desc">覆盖会前、会中、会后的全流程协同平台</view>
      <view class="tag-row">
        <view v-for="tag in tags" :key="tag" class="tag">{{ tag }}</view>
      </view>
    </view>

    <view v-for="phase in phases" :key="phase.id" class="phase-card">
      <view class="phase-title">{{ phase.title }}</view>
      <view v-for="(item, idx) in phase.items" :key="idx" class="phase-item">• {{ item }}</view>
    </view>

    <view class="footer-card">
      <view class="hotline">服务支持：400-800-2025</view>
      <view class="tip">工作时间：08:30 - 18:00</view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref } from 'vue';

uni.setNavigationBarTitle({ title: '系统介绍' });

const tags = ref(['角色驱动', '任务闭环', '数据沉淀', '移动优先']);

const phases = ref([
  {
    id: 1,
    title: '会前准备 · 动态精准',
    items: ['扫码报名与批量审核', '电子手册自动生成并实时同步', '智能排座+拖拽微调', '定向推送座位/房间/日程提醒']
  },
  {
    id: 2,
    title: '会中执行 · 协同高效',
    items: ['报名自动建群', '任务派发与时间线追踪', '签到与查寝闭环', '短信/微信/群公告多渠道触达']
  },
  {
    id: 3,
    title: '会后沉淀 · 价值延伸',
    items: ['资料归档与授权下载', '数据看板实时复盘', '课程评价与问卷分析', '留言与花絮沉淀培训影响力']
  }
]);
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: linear-gradient(160deg, #1f3a93 0%, #3a0ca3 45%, #5440d6 100%); padding: 24rpx; }
.hero { background: rgba(255,255,255,0.1); border: 1rpx solid rgba(255,255,255,0.2); border-radius: 20rpx; padding: 24rpx; margin-bottom: 20rpx; }
.hero-title { color: #fff; font-size: 40rpx; font-weight: 700; }
.hero-desc { color: #dbeafe; font-size: 26rpx; margin-top: 8rpx; }
.tag-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.tag { color: #eef2ff; font-size: 22rpx; padding: 8rpx 14rpx; border-radius: 999rpx; background: rgba(255,255,255,0.16); }
.phase-card { background: rgba(14,24,64,0.45); border: 1rpx solid rgba(255,255,255,0.15); border-radius: 18rpx; padding: 20rpx; margin-bottom: 16rpx; }
.phase-title { color: #fff; font-size: 30rpx; font-weight: 700; margin-bottom: 10rpx; }
.phase-item { color: #dbeafe; font-size: 24rpx; line-height: 1.7; margin-bottom: 4rpx; }
.footer-card { background: rgba(14,24,64,0.7); border-radius: 16rpx; padding: 20rpx; margin-top: 8rpx; }
.hotline { color: #fff; font-size: 28rpx; font-weight: 600; }
.tip { color: #cbd5e1; font-size: 24rpx; margin-top: 6rpx; }
</style>
