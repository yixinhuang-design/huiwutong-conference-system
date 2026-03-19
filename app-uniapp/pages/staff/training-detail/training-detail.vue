<template>
  <view class="container">
    <view class="header">
      <button class="btn-back" @click="goBack">‹</button>
      <view class="title">{{ trainingName }}</view>
    </view>

    <view class="cover-section">
      <view class="cover-image">📚</view>
      <view class="cover-title">{{ trainingName }}</view>
      <view class="cover-date">2025年3月1日-3月3日</view>
    </view>

    <view class="section-card">
      <view class="section-header">📋 培训简介</view>
      <view class="content">
        本次培训旨在提升党务干部的综合素质，加强政策学习和实践能力。培训内容涵盖党建理论、党务管理、组织建设等多个方面。
      </view>
    </view>

    <view class="section-card">
      <view class="section-header">👥 培训规模</view>
      <view class="stats-grid">
        <view class="stat">
          <view class="value">142</view>
          <view class="label">参会人数</view>
        </view>
        <view class="stat">
          <view class="value">6</view>
          <view class="label">学习小组</view>
        </view>
        <view class="stat">
          <view class="value">3</view>
          <view class="label">培训天数</view>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-header">📅 日程安排</view>
      <view class="schedule-item" v-for="(item, i) in schedules" :key="i">
        <view class="day-badge">{{ item.day }}</view>
        <view class="day-content">
          <view class="day-title">{{ item.title }}</view>
          <view class="day-items">
            <view class="day-item" v-for="(it, j) in item.items" :key="j">
              {{ it.time }} - {{ it.name }}
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-header">📍 地点信息</view>
      <view class="location-item">
        <view class="icon">🏢</view>
        <view class="info">
          <view class="label">主会场</view>
          <view class="address">国家会议中心一层大厅</view>
        </view>
      </view>
      <view class="location-item">
        <view class="icon">🚇</view>
        <view class="info">
          <view class="label">交通方式</view>
          <view class="address">地铁10号线/公交52路，会议中心站下车</view>
        </view>
      </view>
    </view>

    <view class="action-buttons">
      <button class="btn-primary" @click="addCalendar">📅 加入日历</button>
      <button class="btn-secondary" @click="shareTraining">📤 分享</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const trainingName = ref('2025党务干部培训班');

const schedules = [
  {
    day: '第1天',
    title: '理论学习',
    items: [
      { time: '09:00', name: '开幕式' },
      { time: '10:00', name: '主题报告' },
      { time: '14:00', name: '分组讨论' }
    ]
  },
  {
    day: '第2天',
    title: '实践操作',
    items: [
      { time: '09:00', name: '案例分析' },
      { time: '11:00', name: '工作坊' },
      { time: '15:00', name: '实地考察' }
    ]
  },
  {
    day: '第3天',
    title: '成果展示',
    items: [
      { time: '09:00', name: '小组汇报' },
      { time: '11:00', name: '评议交流' },
      { time: '14:00', name: '闭幕式' }
    ]
  }
];

onMounted(() => {
  uni.setNavigationBarTitle({ title: '培训详情' });
  loadTrainingData();
});

const loadTrainingData = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8082/api/training/detail',
      method: 'GET'
    });
    if (res && res.data && res.data.code === 200) {
      const data = res.data.data;
      trainingName.value = data.name || '2025党务干部培训班';
    }
  } catch (error) {
    console.log('使用默认数据');
  }
};

const goBack = () => {
  uni.navigateBack();
};

const addCalendar = () => {
  uni.showToast({ title: '已添加到日历', icon: 'success' });
};

const shareTraining = () => {
  uni.showToast({ title: '已复制分享链接', icon: 'success' });
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

.cover-section {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%);
  color: white;
  padding: 40rpx 20rpx;
  text-align: center;
  margin: 16rpx;
  border-radius: 12rpx;
}

.cover-image {
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.cover-title {
  font-size: 24rpx;
  font-weight: 700;
  margin-bottom: 8rpx;
}

.cover-date {
  font-size: 14rpx;
  opacity: 0.9;
}

.section-card {
  background: white;
  border-radius: 12rpx;
  margin: 0 16rpx 12rpx;
  overflow: hidden;
}

.section-header {
  background: linear-gradient(135deg, #f0f9ff 0%, #f5f3ff 100%);
  padding: 12rpx 16rpx;
  border-left: 4rpx solid #3b82f6;
  font-weight: 700;
  color: #1e293b;
  font-size: 14rpx;
}

.content {
  padding: 16rpx;
  font-size: 14rpx;
  line-height: 1.8;
  color: #475569;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12rpx;
  padding: 16rpx;
}

.stat {
  background: #f8fafc;
  padding: 12rpx;
  border-radius: 8rpx;
  text-align: center;
}

.value {
  font-size: 20rpx;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 4rpx;
}

.label {
  font-size: 12rpx;
  color: #64748b;
}

.schedule-item {
  display: flex;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.schedule-item:last-child {
  border-bottom: none;
}

.day-badge {
  flex-shrink: 0;
  width: 60rpx;
  padding: 6rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  color: white;
  border-radius: 6rpx;
  font-size: 12rpx;
  text-align: center;
  font-weight: 600;
}

.day-content {
  flex: 1;
}

.day-title {
  font-weight: 600;
  color: #1e293b;
  font-size: 14rpx;
  margin-bottom: 4rpx;
}

.day-items {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.day-item {
  font-size: 12rpx;
  color: #64748b;
}

.location-item {
  display: flex;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.location-item:last-child {
  border-bottom: none;
}

.icon {
  font-size: 20rpx;
  flex-shrink: 0;
}

.info {
  flex: 1;
}

.label {
  font-size: 14rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2rpx;
}

.address {
  font-size: 12rpx;
  color: #64748b;
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10rpx;
  margin: 20rpx;
}

.btn-primary {
  padding: 12rpx 16rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  border-radius: 8rpx;
  font-weight: 600;
  color: white;
  font-size: 14rpx;
}

.btn-primary:active {
  opacity: 0.9;
}

.btn-secondary {
  padding: 12rpx 16rpx;
  background: white;
  border: 2rpx solid #e2e8f0;
  border-radius: 8rpx;
  font-weight: 600;
  color: #475569;
  font-size: 14rpx;
}

.btn-secondary:active {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}
</style>