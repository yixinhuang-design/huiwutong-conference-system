<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <text class="title">{{ meeting.title }}</text>
    </view>
    <scroll-view scroll-y class="scroll-content">
      <!-- Seat Info -->
      <view class="card">
        <view class="card-header">
          <text class="card-icon">💺</text>
          <text class="card-title">我的座位</text>
        </view>
        <view class="seat-info-box">
          <view class="seat-display">
            <text class="seat-zone">{{ seat.zone }}</text>
          </view>
          <view class="seat-details">
            <text class="detail-row">第 {{ seat.row }} 排</text>
            <text class="detail-row">{{ seat.column }} 座</text>
          </view>
        </view>
        <view class="location-item">
          <text class="location-icon">🏙</text>
          <text class="location-text">{{ seat.location }}</text>
          <text class="status-badge status-checked">✓ 已签到</text>
        </view>
      </view>

      <!-- Group Info -->
      <view class="card">
        <view class="card-header">
          <text class="card-icon">👫</text>
          <text class="card-title">同组成员</text>
        </view>
        <view class="group-item">
          <text class="group-icon">👥</text>
          <view class="group-info">
            <text class="group-name">{{ group.name }}</text>
            <button class="view-btn" @click="viewGroup">查看组员</button>
          </view>
        </view>
      </view>

      <!-- Navigation -->
      <view class="card">
        <view class="card-header">
          <text class="card-icon">🗣</text>
          <text class="card-title">会场导航</text>
        </view>
        <view class="nav-items">
          <view class="nav-item-row">
            <text class="nav-label">步行路线:</text>
            <text class="nav-value">{{ navigation.walkingRoute }}</text>
          </view>
          <view class="nav-item-row">
            <text class="nav-label">入口位置:</text>
            <text class="nav-value">{{ navigation.entrance }}</text>
          </view>
          <button class="start-nav-btn" @click="startNavigation">🗛 启动AR导航</button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const meeting = ref({
  id: '1',
  title: '2025党务干部培训班'
});

const seat = ref({
  zone: '前区',
  row: 3,
  column: 12,
  location: '越秀会议中必101室 - 报告厅'
});

const group = ref({
  id: 1,
  name: '第一组 - 党建工作研讨组'
});

const navigation = ref({
  walkingRoute: '步行约5分钟，云主途道向前',
  entrance: '正门入口 → 右侧途道 → 第3排'
});

const viewGroup = () => {
  uni.navigateTo({ url: '/pages/learner/groups/groups' });
};

const startNavigation = () => {
  uni.showToast({
    title: '正在启动AR导航...',
    icon: 'loading'
  });
};

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '座位详情' });
  loadSeatData();
});

const loadSeatData = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/seat/detail', method: 'GET' });
    if (res[1]?.data) {
      seat.value = res[1].data.seat || seat.value;
      group.value = res[1].data.group || group.value;
    }
  } catch (e) {
    console.log('Using mock data');
  }
};
</script>

<style scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f7fb;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  gap: 12px;
}

.back-btn {
  width: 32px;
  height: 32px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  flex: 1;
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
}

.scroll-content {
  flex: 1;
  padding: 12px;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.card-icon {
  font-size: 18px;
  margin-right: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
}

.seat-info-box {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.seat-display {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.seat-zone {
  font-size: 28px;
  font-weight: bold;
  color: white;
}

.seat-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.detail-row {
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
}

.location-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 4px;
}

.location-icon {
  font-size: 20px;
}

.location-text {
  flex: 1;
  font-size: 14px;
  color: #475569;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
}

.status-checked {
  background: #dcfce7;
  color: #166534;
}

.group-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.05) 0%, rgba(59, 130, 246, 0.05) 100%);
  border-radius: 4px;
  border: 1px solid #e0e7ff;
}

.group-icon {
  font-size: 24px;
}

.group-info {
  flex: 1;
}

.group-name {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
  display: block;
  margin-bottom: 8px;
}

.view-btn {
  padding: 6px 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
}

.nav-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nav-item-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.nav-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.nav-value {
  font-size: 12px;
  color: #475569;
}

.start-nav-btn {
  width: 100%;
  padding: 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  margin-top: 8px;
}
</style>
