<template>
  <view class="page">
    <view class="top-card">
      <view class="meeting">2025党务干部培训班</view>
      <view class="setting" @click="openRule">⚙</view>
    </view>

    <view class="stats-card">
      <view class="stat-item">
        <view class="num red">{{ stats.unhandled }}</view>
        <view class="label">待处理</view>
      </view>
      <view class="stat-item">
        <view class="num orange">{{ stats.processing }}</view>
        <view class="label">处理中</view>
      </view>
      <view class="stat-item">
        <view class="num green">{{ stats.resolved }}</view>
        <view class="label">已处理</view>
      </view>
    </view>

    <scroll-view scroll-x class="tabs" show-scrollbar="false">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.name }} ({{ tab.count }})
      </view>
    </scroll-view>

    <view class="section-title">预警事件</view>

    <view
      v-for="item in filteredAlerts"
      :key="item.id"
      class="alert-item"
      :class="item.level"
      @click="openAlert(item)"
    >
      <view class="row">
        <view class="title">{{ item.title }}</view>
        <view class="level-tag" :class="item.level">{{ levelText[item.level] }}</view>
      </view>
      <view class="desc">{{ item.desc }}</view>
      <view class="row">
        <view class="time">{{ item.time }}</view>
        <button class="action-btn" :class="item.status" @click.stop="handleAlert(item)">{{ statusText[item.status] }}</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue';

uni.setNavigationBarTitle({ title: '预警管理' });

const stats = ref({ unhandled: 3, processing: 2, resolved: 15 });

const tabs = ref([
  { key: 'all', name: '全部', count: 20 },
  { key: 'unhandled', name: '待处理', count: 3 },
  { key: 'processing', name: '处理中', count: 2 },
  { key: 'resolved', name: '已处理', count: 15 }
]);

const activeTab = ref('all');

const levelText = {
  high: '高',
  medium: '中',
  low: '低'
};

const statusText = {
  unhandled: '立即处理',
  processing: '继续跟进',
  resolved: '查看结果'
};

const alerts = ref([
  {
    id: 1,
    title: '签到率过低',
    desc: '签到率 65% < 阈值 70%',
    time: '2026-03-10 10:30',
    level: 'high',
    status: 'unhandled'
  },
  {
    id: 2,
    title: '住宿异常申报',
    desc: 'A栋 3 人未完成入住确认',
    time: '2026-03-10 11:10',
    level: 'medium',
    status: 'processing'
  },
  {
    id: 3,
    title: '车辆集合提醒',
    desc: '返程发车前30分钟提醒',
    time: '2026-03-10 12:00',
    level: 'low',
    status: 'resolved'
  }
]);

const filteredAlerts = computed(() => {
  if (activeTab.value === 'all') return alerts.value;
  return alerts.value.filter((a) => a.status === activeTab.value);
});

const openRule = () => {
  uni.showToast({ title: '打开预警规则设置', icon: 'none' });
};

const openAlert = (item) => {
  uni.showToast({ title: `查看：${item.title}`, icon: 'none' });
};

const handleAlert = (item) => {
  if (item.status === 'unhandled') {
    item.status = 'processing';
    uni.showToast({ title: '已转为处理中', icon: 'none' });
    return;
  }
  if (item.status === 'processing') {
    item.status = 'resolved';
    uni.showToast({ title: '已标记处理完成', icon: 'none' });
    return;
  }
  uni.showToast({ title: '查看处理记录', icon: 'none' });
};
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.top-card, .stats-card, .alert-item { background: #fff; border-radius: 14rpx; padding: 20rpx; margin-bottom: 16rpx; }
.top-card { display: flex; justify-content: space-between; align-items: center; }
.meeting { font-size: 30rpx; font-weight: 700; color: #1e293b; }
.setting { font-size: 32rpx; color: #2563eb; }

.stats-card { display: flex; }
.stat-item { flex: 1; text-align: center; }
.num { font-size: 34rpx; font-weight: 700; }
.num.red { color: #ef4444; }
.num.orange { color: #f59e0b; }
.num.green { color: #22c55e; }
.label { font-size: 22rpx; color: #64748b; margin-top: 4rpx; }

.tabs { white-space: nowrap; margin-bottom: 16rpx; }
.tab { display: inline-block; margin-right: 12rpx; padding: 10rpx 16rpx; border-radius: 999rpx; background: #fff; color: #475569; font-size: 23rpx; }
.tab.active { background: #2563eb; color: #fff; }

.section-title { font-size: 28rpx; font-weight: 700; color: #1e293b; margin-bottom: 10rpx; }
.alert-item { border-left: 8rpx solid #cbd5e1; }
.alert-item.high { border-left-color: #ef4444; background: #fef2f2; }
.alert-item.medium { border-left-color: #f59e0b; background: #fffbeb; }
.alert-item.low { border-left-color: #3b82f6; background: #f0f9ff; }

.row { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 28rpx; font-weight: 700; color: #0f172a; }
.desc { margin-top: 8rpx; color: #475569; font-size: 24rpx; }
.time { margin-top: 10rpx; font-size: 22rpx; color: #64748b; }

.level-tag { font-size: 20rpx; border-radius: 8rpx; padding: 4rpx 10rpx; }
.level-tag.high { color: #991b1b; background: #fee2e2; }
.level-tag.medium { color: #92400e; background: #fef3c7; }
.level-tag.low { color: #1e40af; background: #dbeafe; }

.action-btn { margin-top: 10rpx; font-size: 22rpx; border-radius: 8rpx; padding: 8rpx 12rpx; border: none; }
.action-btn.unhandled { background: #fee2e2; color: #991b1b; }
.action-btn.processing { background: #fef3c7; color: #92400e; }
.action-btn.resolved { background: #dcfce7; color: #166534; }
</style>
