<template>
  <view class="page">
    <view class="head-card">
      <view class="head-title">往期会议 / 培训</view>
      <view class="head-sub">沉淀互动内容、资料归档与数据复盘</view>
    </view>

    <scroll-view scroll-x class="filter-row" show-scrollbar="false">
      <view
        v-for="f in filters"
        :key="f"
        class="filter-item"
        :class="{ active: currentFilter === f }"
        @click="currentFilter = f"
      >
        {{ f }}
      </view>
    </scroll-view>

    <view v-for="meeting in filteredMeetings" :key="meeting.id" class="meeting-card">
      <view class="meeting-title">{{ meeting.title }}</view>
      <view class="meeting-meta">{{ meeting.date }} · {{ meeting.location }}</view>

      <view class="metric-row">
        <view class="metric"><text class="strong">{{ meeting.signRate }}</text><text>签到率</text></view>
        <view class="metric"><text class="strong">{{ meeting.extraRate }}</text><text>{{ meeting.extraLabel }}</text></view>
        <view class="metric"><text class="strong">{{ meeting.score }}</text><text>课程评价</text></view>
      </view>

      <view class="btn-row">
        <button class="btn-primary" @click="openMaterials(meeting)">资料目录</button>
        <button class="btn-outline" @click="openReport(meeting)">数据与反馈</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue';

uni.setNavigationBarTitle({ title: '往期' });

const filters = ref(['全部', '2025年', '党建专题', '干部提升', '3个月内']);
const currentFilter = ref('全部');

const meetings = ref([
  {
    id: 1,
    title: '2024数智党建专题班',
    date: '2024-11-01 至 2024-11-05',
    location: '杭州 · 党校综合楼',
    tags: ['党建专题'],
    signRate: '98%',
    extraLabel: '就寝率',
    extraRate: '96%',
    score: '4.8'
  },
  {
    id: 2,
    title: '2024中青年干部能力提升班',
    date: '2024-08-18 至 2024-08-23',
    location: '宁波 · 行政学院',
    tags: ['干部提升'],
    signRate: '92%',
    extraLabel: '问卷完成率',
    extraRate: '87%',
    score: '4.6'
  }
]);

const filteredMeetings = computed(() => {
  if (currentFilter.value === '全部' || currentFilter.value === '2025年' || currentFilter.value === '3个月内') {
    return meetings.value;
  }
  return meetings.value.filter((m) => m.tags.includes(currentFilter.value));
});

const openMaterials = (meeting) => {
  uni.showToast({ title: `${meeting.title} 资料目录`, icon: 'none' });
};

const openReport = (meeting) => {
  uni.showToast({ title: `${meeting.title} 数据复盘`, icon: 'none' });
};
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.head-card, .meeting-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.head-title { font-size: 34rpx; font-weight: 700; color: #1e293b; }
.head-sub { font-size: 24rpx; color: #64748b; margin-top: 8rpx; }
.filter-row { white-space: nowrap; margin-bottom: 18rpx; }
.filter-item { display: inline-block; margin-right: 12rpx; padding: 10rpx 20rpx; border-radius: 999rpx; background: #fff; color: #475569; font-size: 24rpx; }
.filter-item.active { background: #2563eb; color: #fff; }
.meeting-title { font-size: 30rpx; font-weight: 700; color: #0f172a; }
.meeting-meta { margin-top: 8rpx; font-size: 24rpx; color: #64748b; }
.metric-row { display: flex; gap: 12rpx; margin-top: 16rpx; }
.metric { flex: 1; background: #f8fafc; border-radius: 10rpx; text-align: center; padding: 12rpx 8rpx; font-size: 22rpx; color: #64748b; display: flex; flex-direction: column; }
.strong { font-size: 28rpx; color: #1e293b; font-weight: 700; }
.btn-row { display: flex; gap: 12rpx; margin-top: 16rpx; }
.btn-primary, .btn-outline { flex: 1; border-radius: 10rpx; font-size: 24rpx; padding: 14rpx 0; }
.btn-primary { background: #2563eb; color: #fff; border: none; }
.btn-outline { background: #fff; color: #2563eb; border: 1rpx solid #2563eb; }
</style>
