<template>
  <view class="container">
    <view class="header">
      <button class="btn-back" @click="goBack">‹</button>
      <view class="title">通知管理</view>
    </view>

    <view class="stats">
      <view class="stat">
        <view class="num">142</view>
        <view class="lab">总参会</view>
      </view>
      <view class="stat">
        <view class="num">6</view>
        <view class="lab">待发</view>
      </view>
      <view class="stat">
        <view class="num">23</view>
        <view class="lab">已发</view>
      </view>
    </view>

    <view class="quick-send">
      <view class="quick-title">快捷发送</view>
      <view class="quick-grid">
        <button class="quick-btn" @click="quickSend('seat')">🪑 座位</button>
        <button class="quick-btn" @click="quickSend('checkin')">✓ 签到</button>
        <button class="quick-btn" @click="quickSend('bus')">🚗 车次</button>
        <button class="quick-btn" @click="quickSend('hotel')">🏨 住宿</button>
        <button class="quick-btn" @click="quickSend('schedule')">📅 日程</button>
        <button class="quick-btn" @click="quickSend('general')">🔔 通知</button>
      </view>
    </view>

    <view class="notice-list">
      <view class="notice-item sent" v-for="(item, i) in notices" :key="i" @click="editNotice(item)">
        <view class="header">
          <view class="title">{{ item.title }}</view>
          <view class="badge">{{ item.status }}</view>
        </view>
        <view class="desc">{{ item.desc }}</view>
        <button class="btn-send" @click.stop="sendNow(item)">📤 立即发送</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const notices = ref([
  { id: 1, title: '座位分配通知', desc: '尊敬的参会者，您的座位已分配...', status: '已发' },
  { id: 2, title: '签到提醒', desc: '会议将于今天上午09:00开始，请...', status: '待发' },
  { id: 3, title: '日程变更通知', desc: '原定下午14:00的论坛改至15:00...', status: '待发' }
]);

onMounted(() => {
  uni.setNavigationBarTitle({ title: '通知管理' });
});

const goBack = () => {
  uni.navigateBack();
};

const quickSend = (type) => {
  const titles = {
    seat: '座位通知',
    checkin: '签到提醒',
    bus: '车次通知',
    hotel: '住宿通知',
    schedule: '日程提醒',
    general: '常规通知'
  };
  uni.showToast({ title: '发送' + titles[type], icon: 'none' });
};

const editNotice = (item) => {
  uni.showToast({ title: '编辑: ' + item.title, icon: 'none' });
};

const sendNow = (item) => {
  uni.showToast({ title: '已发送', icon: 'success' });
};
</script>

<style scoped lang="scss">
.container {
  width: 100%;
  background: #f5f7fb;
}

.header {
  display: flex;
  align-items: center;
  padding: 10rpx;
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

.stats {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10rpx;
  margin: 16rpx;
}

.stat {
  background: linear-gradient(135deg, #eff6ff 0%, #f5f3ff 100%);
  padding: 16rpx;
  border-radius: 8rpx;
  text-align: center;
}

.num {
  font-size: 24rpx;
  font-weight: 700;
  color: #3b82f6;
}

.lab {
  font-size: 12rpx;
  color: #64748b;
  margin-top: 4rpx;
}

.quick-send {
  background: white;
  margin: 0 16rpx 16rpx;
  border-radius: 12rpx;
  padding: 16rpx;
}

.quick-title {
  font-size: 14rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12rpx;
}

.quick-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8rpx;
}

.quick-btn {
  padding: 10rpx;
  border: 1rpx solid #e2e8f0;
  background: white;
  border-radius: 6rpx;
  font-size: 12rpx;
  cursor: pointer;
}

.quick-btn:active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.notice-list {
  padding: 0 16rpx 20rpx;
}

.notice-item {
  background: white;
  border-left: 4rpx solid #3b82f6;
  border-radius: 8rpx;
  padding: 12rpx;
  margin-bottom: 8rpx;
  cursor: pointer;
  transition: all 0.2s;
}

.notice-item:active {
  background: #f8fafc;
  transform: scale(0.98);
}

.notice-item.sent {
  border-left-color: #22c55e;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6rpx;
}

.title {
  font-weight: 600;
  font-size: 14rpx;
  color: #1f2937;
}

.badge {
  font-size: 12rpx;
  padding: 2rpx 8rpx;
  background: #f0f0f0;
  border-radius: 4rpx;
}

.desc {
  font-size: 12rpx;
  color: #6b7280;
  line-height: 1.4;
  margin-bottom: 6rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.btn-send {
  font-size: 11rpx;
  padding: 4rpx 8rpx;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 4rpx;
  cursor: pointer;
}

.btn-send:active {
  opacity: 0.8;
}
</style>