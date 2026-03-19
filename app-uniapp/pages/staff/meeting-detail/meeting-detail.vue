<template>
  <view class="container">
    <scroll-view scroll-y class="scroll-content">
      <!-- Meeting Info Card -->
      <view class="card">
        <view class="card-header">
          <text class="card-title">{{ meeting.title }}</text>
        </view>
      </view>

      <!-- Feature Entry Section -->
      <view class="card">
        <view class="card-header">
          <text class="card-header-icon">📋</text>
          <text class="card-title">功能入口</text>
        </view>
        <view class="grid-3">
          <view class="feature-tile" @click="goToPage('/pages/staff/registration-manage/registration-manage')">
            <text class="feature-icon">✓</text>
            <text class="feature-title">报名管理</text>
          </view>
          <view class="feature-tile" @click="goToPage('/pages/staff/seat-manage/seat-manage')">
            <text class="feature-icon">🎫</text>
            <text class="feature-title">座位管理</text>
          </view>
          <view class="feature-tile" @click="goToPage('/pages/staff/grouping-manage/grouping-manage')">
            <text class="feature-icon">👥</text>
            <text class="feature-title">分组编排</text>
          </view>
          <view class="feature-tile" @click="goToPage('/pages/staff/notice-manage/notice-manage')">
            <text class="feature-icon">📢</text>
            <text class="feature-title">通知公告</text>
          </view>
          <view class="feature-tile" @click="goToPage('/pages/staff/task-list/task-list')">
            <text class="feature-icon">✓</text>
            <text class="feature-title">任务派发</text>
          </view>
          <view class="feature-tile" @click="goToPage('/pages/staff/data-analysis/data-analysis')">
            <text class="feature-icon">📊</text>
            <text class="feature-title">数据分析</text>
          </view>
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
            <text>{{ task.info }}</text>
          </view>
        </view>
      </view>

      <!-- Notifications -->
      <view class="section-title">通知公告</view>
      <view class="card">
        <view class="notification-list">
          <view v-for="notice in notices" :key="notice.id" class="notification-item">
            <text class="notice-icon">📢</text>
            <view class="notice-content">
              <text class="notice-title">{{ notice.title }}</text>
              <text class="notice-desc">{{ notice.content }}</text>
            </view>
            <text class="status-badge status-read">✓ 已读</text>
          </view>
        </view>
      </view>

      <!-- Statistics Overview -->
      <view class="section-title">实时数据概览</view>
      <view class="card">
        <view class="summary-grid">
          <view class="summary-card">
            <text class="summary-label">报名完成度</text>
            <text class="summary-value">{{ stats.registrationRate }}%</text>
            <text class="summary-detail">通过审核 {{ stats.registeredCount }} / 目标 {{ stats.targetCount }}</text>
          </view>
          <view class="summary-card">
            <text class="summary-label">签到率</text>
            <text class="summary-value">{{ stats.checkinRate }}%</text>
            <text class="summary-detail">已签到 {{ stats.checkinCount }} 人 · 异常 {{ stats.abnormalCount }} 人</text>
          </view>
          <view class="summary-card">
            <text class="summary-label">任务完成率</text>
            <text class="summary-value">{{ stats.taskRate }}%</text>
            <text class="summary-detail">执行中 {{ stats.processingTasks }} 项 · 待完成 {{ stats.pendingTasks }} 项</text>
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
  title: '2025党务干部培训班'
});

const tasks = ref([
  { id: 1, title: '审核最新报名信息', status: 'pending', description: '需核对 12 名学员的报名资料并确认住宿需求。', info: '截止：今日 12:00' },
  { id: 2, title: '下午课程座位调整', status: 'processing', description: '讲师调整为王教授，请更新座位及讲师介绍推送。', info: '负责人：李伟' },
  { id: 3, title: '车次提醒待审核', status: 'completed', description: 'AI 助理为 06:30 到站学员生成了接驳车提醒，请确认后推送。', info: '待审核：2 条' }
]);

const notices = ref([
  { id: 1, title: '今日课程有调整', content: '下午课程改至第二会议厅，请留意座位图更新。', read: true }
]);

const stats = ref({
  registrationRate: 94,
  registeredCount: 142,
  targetCount: 150,
  checkinRate: 92,
  checkinCount: 131,
  abnormalCount: 4,
  taskRate: 78,
  processingTasks: 3,
  pendingTasks: 2
});

const taskStatus = (status) => {
  const map = { pending: '待完成', processing: '进行中', completed: '已提交' };
  return map[status] || status;
};

const goToPage = (url) => {
  uni.navigateTo({ url });
};

const openTask = (task) => {
  uni.navigateTo({ url: `/pages/staff/task-detail/task-detail?id=${task.id}` });
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
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.card-header-icon {
  font-size: 18px;
  margin-right: 8px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #1e293b;
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
}

.feature-tile {
  flex: 0 0 calc(33.333% - 8px);
  padding: 16px 8px;
  background: #f8fafc;
  border-radius: 8px;
  text-align: center;
  border: 1px solid #e2e8f0;
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
  border: 1px solid #e2e8f0;
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

.status-processing {
  background: #dbeafe;
  color: #1e40af;
}

.status-completed {
  background: #dcfce7;
  color: #166534;
}

.status-read {
  background: #dcfce7;
  color: #166534;
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

.summary-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-card {
  padding: 12px;
  background: #f8fafc;
  border-radius: 4px;
  border-left: 4px solid #2563eb;
}

.summary-label {
  font-size: 12px;
  color: #64748b;
  display: block;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #2563eb;
  display: block;
  margin-bottom: 4px;
}

.summary-detail {
  font-size: 12px;
  color: #94a3b8;
  display: block;
}
</style>
