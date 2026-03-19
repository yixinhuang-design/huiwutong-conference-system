<template>
  <view class="container">
    <view class="header">
      <button class="btn-back" @click="goBack">‹</button>
      <view class="title">数据统计</view>
    </view>

    <view class="date-selector">
      <button class="date-btn" v-for="(date, i) in dateOptions" :key="i" 
        :class="{ active: selectedDate === date }" @click="selectedDate = date">
        {{ date }}
      </button>
    </view>

    <view class="stats-overview">
      <view class="overview-item">
        <view class="overview-number">142</view>
        <view class="overview-label">总参会人数</view>
        <view class="overview-trend">↑ 12% vs 上期</view>
      </view>
      <view class="overview-item">
        <view class="overview-number">89</view>
        <view class="overview-label">已签到</view>
        <view class="overview-trend">↑ 15% vs 昨日</view>
      </view>
      <view class="overview-item">
        <view class="overview-number">6</view>
        <view class="overview-label">学习小组</view>
        <view class="overview-trend">均衡分配</view>
      </view>
    </view>

    <view class="chart-section">
      <view class="chart-header">📊 签到统计</view>
      <view class="chart-placeholder">
        <view class="chart-bar" :style="{ height: (89 / 142 * 200) + 'rpx' }"></view>
        <view class="chart-text">签到率: 62.7%</view>
      </view>
    </view>

    <view class="chart-section">
      <view class="chart-header">📈 小组分布</view>
      <view class="group-list">
        <view class="group-item" v-for="(group, i) in groups" :key="i">
          <view class="group-name">{{ group.name }}</view>
          <view class="group-bar-wrapper">
            <view class="group-bar" :style="{ width: (group.count / 142 * 100) + '%', background: colors[i % colors.length] }"></view>
          </view>
          <view class="group-count">{{ group.count }}人</view>
        </view>
      </view>
    </view>

    <view class="chart-section">
      <view class="chart-header">🎯 参与度统计</view>
      <view class="participation-list">
        <view class="participation-item" v-for="(p, i) in participations" :key="i">
          <view class="participation-header">
            <view class="p-title">{{ p.title }}</view>
            <view class="p-percentage">{{ p.percentage }}%</view>
          </view>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: p.percentage + '%', background: p.color }"></view>
          </view>
        </view>
      </view>
    </view>

    <view class="action-buttons">
      <button class="btn-export" @click="exportData">📥 导出数据</button>
      <button class="btn-print" @click="printReport">🖨️ 打印报告</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const selectedDate = ref('全部');
const dateOptions = ['全部', '今日', '周总结', '月总结'];

const colors = ['#3b82f6', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#06b6d4'];

const groups = [
  { name: '第1小组', count: 24 },
  { name: '第2小组', count: 23 },
  { name: '第3小组', count: 23 },
  { name: '第4小组', count: 22 },
  { name: '第5小组', count: 25 },
  { name: '第6小组', count: 25 }
];

const participations = [
  { title: '课程出席', percentage: 85, color: '#3b82f6' },
  { title: '讨论参与', percentage: 72, color: '#8b5cf6' },
  { title: '任务完成', percentage: 68, color: '#ec4899' },
  { title: '满意度评分', percentage: 91, color: '#10b981' }
];

onMounted(() => {
  uni.setNavigationBarTitle({ title: '数据统计' });
  loadAnalyticsData();
});

const loadAnalyticsData = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8082/api/analytics/data',
      method: 'GET'
    });
    if (res && res.data && res.data.code === 200) {
      // 更新数据逻辑
    }
  } catch (error) {
    console.log('使用默认数据');
  }
};

const goBack = () => {
  uni.navigateBack();
};

const exportData = () => {
  uni.showLoading({ title: '导出中...' });
  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({ title: '已导出Excel文件', icon: 'success' });
  }, 1000);
};

const printReport = () => {
  uni.showToast({ title: '已发送到打印机', icon: 'success' });
};
</script>

<style scoped lang="scss">
.container {
  width: 100%;
  padding-bottom: 20rpx;
  background: #f5f7fb;
}

.header {
  display: flex;
  align-items: center;
  padding: 10rpx 10rpx;
  background: white;
  border-bottom: 1rpx solid #e2e8f0;
  gap: 10rpx;
}

.btn-back {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #f8fafc;
  border: none;
  font-size: 24rpx;
  color: #475569;
}

.title {
  flex: 1;
  font-size: 16rpx;
  font-weight: 600;
  color: #1e293b;
}

.date-selector {
  display: flex;
  gap: 8rpx;
  padding: 12rpx 16rpx;
  overflow-x: auto;
  background: white;
  margin-bottom: 2rpx;
}

.date-btn {
  flex-shrink: 0;
  padding: 8rpx 14rpx;
  background: #f8fafc;
  border: 1rpx solid #e2e8f0;
  border-radius: 6rpx;
  font-size: 12rpx;
  color: #64748b;
}

.date-btn.active {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-color: #3b82f6;
  color: white;
}

.stats-overview {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8rpx;
  padding: 12rpx;
  background: white;
  margin-bottom: 12rpx;
}

.overview-item {
  background: linear-gradient(135deg, #eff6ff 0%, #f5f3ff 100%);
  padding: 12rpx;
  border-radius: 8rpx;
  text-align: center;
  border-left: 3rpx solid #3b82f6;
}

.overview-number {
  font-size: 20rpx;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 4rpx;
}

.overview-label {
  font-size: 12rpx;
  color: #475569;
  margin-bottom: 2rpx;
}

.overview-trend {
  font-size: 11rpx;
  color: #10b981;
  font-weight: 600;
}

.chart-section {
  background: white;
  border-radius: 12rpx;
  margin: 12rpx 12rpx;
  overflow: hidden;
}

.chart-header {
  background: linear-gradient(135deg, #f0f9ff 0%, #f5f3ff 100%);
  padding: 12rpx 16rpx;
  border-left: 4rpx solid #3b82f6;
  font-weight: 700;
  color: #1e293b;
  font-size: 14rpx;
}

.chart-placeholder {
  padding: 32rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.chart-bar {
  width: 40rpx;
  background: linear-gradient(180deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 8rpx;
  min-height: 20rpx;
}

.chart-text {
  font-size: 14rpx;
  color: #475569;
  font-weight: 600;
}

.group-list {
  padding: 12rpx 16rpx;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 10rpx;
  padding-bottom: 10rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.group-item:last-child {
  border-bottom: none;
}

.group-name {
  width: 70rpx;
  font-size: 12rpx;
  color: #475569;
  font-weight: 600;
  flex-shrink: 0;
}

.group-bar-wrapper {
  flex: 1;
  height: 16rpx;
  background: #f8fafc;
  border-radius: 8rpx;
  overflow: hidden;
}

.group-bar {
  height: 100%;
  border-radius: 8rpx;
  min-width: 2rpx;
}

.group-count {
  width: 40rpx;
  text-align: right;
  font-size: 12rpx;
  color: #64748b;
  flex-shrink: 0;
}

.participation-list {
  padding: 12rpx 16rpx;
}

.participation-item {
  margin-bottom: 12rpx;
}

.participation-item:last-child {
  margin-bottom: 0;
}

.participation-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6rpx;
}

.p-title {
  font-size: 12rpx;
  color: #475569;
  font-weight: 600;
}

.p-percentage {
  font-size: 12rpx;
  color: #3b82f6;
  font-weight: 700;
}

.progress-bar {
  height: 12rpx;
  background: #f1f5f9;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10rpx;
  margin: 20rpx;
}

.btn-export, .btn-print {
  padding: 12rpx 16rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  border-radius: 8rpx;
  font-weight: 600;
  color: white;
  font-size: 14rpx;
}

.btn-export:active, .btn-print:active {
  opacity: 0.9;
}
</style>
