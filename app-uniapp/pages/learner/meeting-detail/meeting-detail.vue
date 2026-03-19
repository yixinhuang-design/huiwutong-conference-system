<template>
  <view class="container">
    <scroll-view scroll-y class="scroll-content">
      <!-- Meeting Info Card -->
      <view class="card">
        <view class="card-header">
          <text class="card-title">{{ meeting.title }}</text>
        </view>
        <view class="meeting-meta">
          <text class="meta-item">
            <text class="icon">📅</text>
            {{ meeting.startDate }} - {{ meeting.endDate }} · 共{{ meeting.duration }}天
          </text>
          <view class="meta-location">
            <text class="location-text">
              <text class="icon">📍</text>
              {{ meeting.location }}
            </text>
            <button class="nav-btn" @click="navigateTo(meeting.location)">
              <text class="icon">🧭</text> 立即导航
            </button>
          </view>
          <text class="org-badge">主办单位：{{ meeting.organizer }}</text>
        </view>
        <view class="description" style="margin-top: 16px; padding-top: 16px; border-top: 1px solid #e2e8f0;">
          <text>{{ meeting.description }}</text>
        </view>
      </view>

      <!-- Feature Entries -->
      <view class="section-title">功能入口</view>
      <view class="grid-3">
        <view class="feature-tile" @click="goToPage('/pages/learner/meeting-guide/meeting-guide')">
          <text class="feature-icon">📚</text>
          <text class="feature-title">参会须知</text>
        </view>
        <view class="feature-tile" @click="goToPage('/pages/learner/schedule/schedule')">
          <text class="feature-icon">📅</text>
          <text class="feature-title">日程安排</text>
        </view>
        <view class="feature-tile" @click="goToPage('/pages/learner/contact/contact')">
          <text class="feature-icon">👥</text>
          <text class="feature-title">通讯录</text>
        </view>
        <view class="feature-tile" @click="goToPage('/pages/learner/seat/seat')">
          <text class="feature-icon">🎫</text>
          <text class="feature-title">座位图</text>
        </view>
        <view class="feature-tile" @click="goToPage('/pages/learner/checkin/checkin')">
          <text class="feature-icon">✅</text>
          <text class="feature-title">报到签到</text>
        </view>
        <view class="feature-tile" @click="goToPage('/pages/learner/groups/groups')">
          <text class="feature-icon">👫</text>
          <text class="feature-title">学员群组</text>
        </view>
        <view class="feature-tile" @click="goToPage('/pages/learner/materials/materials')">
          <text class="feature-icon">📥</text>
          <text class="feature-title">资料下载</text>
        </view>
        <view class="feature-tile" @click="goToPage('/pages/learner/highlights/highlights')">
          <text class="feature-icon">📷</text>
          <text class="feature-title">精彩花絮</text>
        </view>
        <view class="feature-tile" @click="goToPage('/pages/learner/message/message')">
          <text class="feature-icon">💬</text>
          <text class="feature-title">留言赠语</text>
        </view>
      </view>

      <!-- Today's Tasks -->
      <view class="section-title">今日任务与提醒</view>
      <view class="task-list">
        <view v-for="task in tasks" :key="task.id" class="task-card" @click="openTask(task)">
          <view class="task-header">
            <text class="task-title">{{ task.title }}</text>
            <text class="status-badge" :class="'status-' + task.status">{{ taskStatus(task.status) }}</text>
          </view>
          <text class="task-body">{{ task.description }}</text>
          <view class="task-footer">
            <text>{{ task.location }}</text>
          </view>
        </view>
      </view>

      <!-- Notifications -->
      <view class="section-title">通知公告</view>
      <view class="card">
        <view class="notification-list">
          <view v-for="notice in notices" :key="notice.id" class="notification-item">
            <text class="notice-icon">🔔</text>
            <view class="notice-content">
              <text class="notice-title">{{ notice.title }}</text>
              <text class="notice-desc">{{ notice.content }}</text>
            </view>
            <text class="status-badge" :class="'status-' + (notice.read ? 'read' : 'unread')">{{ notice.read ? '已读' : '未读' }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const meeting = ref({
  id: '1',
  title: '2025党务干部培训班',
  startDate: '1月15日',
  endDate: '1月19日',
  duration: 5,
  location: '市委党校报告厅',
  organizer: '市委组织部',
  description: '本次培训旨在提升党务干部的理论水平和实践能力，通过专题讲座、分组讨论、实地调研等多种形式，深入学习党的创新理论，交流工作经验，提升履职能力。'
});

const tasks = ref([
  { id: 1, title: '08:30 入场签到', status: 'pending', description: '请携带证件扫描二维码签到。若已完成，可点击补录上传凭证。', location: '地点：报告厅门口' },
  { id: 2, title: '晚间查寝反馈', status: 'completed', description: '提交时间：昨天 21:40，班主任已确认。', location: '查看提交记录' }
]);

const notices = ref([
  { id: 1, title: '今日课程有调整', content: '下午课程改至第二会议厅，请留意座位图更新。', read: true },
  { id: 2, title: '天气提示', content: '今晚有小雨，离场请提前准备雨具，车辆停靠 5 号口。', read: false }
]);

const taskStatus = (status) => {
  const map = { pending: '待完成', processing: '进行中', completed: '已完成' };
  return map[status] || status;
};

const goToPage = (url) => {
  uni.navigateTo({ url });
};

const navigateTo = (location) => {
  uni.openMap({
    latitude: 39.9042,
    longitude: 116.4074,
    name: location
  });
};

const openTask = (task) => {
  uni.navigateTo({ url: `/pages/learner/task-detail/task-detail?id=${task.id}` });
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '培训详情' });
  loadMeetingData();
});

const loadMeetingData = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/meeting/detail', method: 'GET' });
    if (res[1]?.data) meeting.value = res[1].data;
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
}

.scroll-content {
  padding: 12px 0;
}

.card {
  margin: 12px 12px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.card-header {
  margin-bottom: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #1e293b;
}

.meeting-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-item {
  font-size: 14px;
  color: #64748b;
}

.icon {
  margin-right: 6px;
}

.meta-location {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.location-text {
  font-size: 14px;
  color: #64748b;
}

.nav-btn {
  padding: 6px 12px;
  background: #2563eb;
  color: white;
  border-radius: 4px;
  font-size: 12px;
  border: none;
}

.org-badge {
  display: inline-block;
  padding: 4px 8px;
  background: #fee2e2;
  color: #991b1b;
  border-radius: 4px;
  font-size: 12px;
}

.description {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
  margin: 20px 12px 12px 12px;
}

.grid-3 {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 0 12px;
  margin-bottom: 12px;
}

.feature-tile {
  flex: 0 0 calc(33.333% - 8px);
  padding: 16px 8px;
  background: white;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}

.feature-title {
  font-size: 12px;
  color: #475569;
}

.task-list {
  margin: 12px 12px 0 12px;
}

.task-card {
  margin-bottom: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border-left: 4px solid #2563eb;
}

.task-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.task-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.task-body {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}

.task-footer {
  font-size: 12px;
  color: #94a3b8;
}

.status-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 3px;
}

.status-pending {
  background: #fef3c7;
  color: #92400e;
}

.status-completed {
  background: #dcfce7;
  color: #166534;
}

.status-read {
  background: #dcfce7;
  color: #166534;
}

.status-unread {
  background: #fef3c7;
  color: #92400e;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 4px;
  background: #f8fafc;
}

.notice-icon {
  font-size: 20px;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
  display: block;
}

.notice-desc {
  font-size: 13px;
  color: #64748b;
  display: block;
  margin-top: 4px;
}
</style>
