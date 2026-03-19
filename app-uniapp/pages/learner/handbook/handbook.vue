<template>
  <scroll-view scroll-y class="page">
    <view class="cover-card">
      <view class="cover-icon">📘</view>
      <view class="cover-title">学员手册</view>
      <view class="cover-sub">{{ meeting.name }}</view>
      <view class="cover-date">{{ meeting.date }}</view>
    </view>

    <view class="toc-card">
      <view class="section-title">目录</view>
      <view v-for="(item, index) in toc" :key="item.key" class="toc-item">
        <text class="toc-index">{{ index + 1 }}</text>
        <text class="toc-text">{{ item.name }}</text>
      </view>
    </view>

    <view class="section-card">
      <view class="section-title">会议日程</view>
      <view v-for="item in schedule" :key="item.id" class="schedule-item">
        <view class="time">{{ item.time }}</view>
        <view class="event">
          <view class="event-title">{{ item.title }}</view>
          <view class="event-location">{{ item.location }}</view>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-title">参会须知</view>
      <view v-for="(tip, idx) in tips" :key="idx" class="tip-item">• {{ tip }}</view>
    </view>

    <view class="section-card">
      <view class="section-title">分组与名册</view>
      <view class="stats-row">
        <view class="stat-box">
          <view class="stat-value">{{ roster.total }}</view>
          <view class="stat-label">学员总数</view>
        </view>
        <view class="stat-box">
          <view class="stat-value">{{ roster.groups }}</view>
          <view class="stat-label">分组数量</view>
        </view>
        <view class="stat-box">
          <view class="stat-value">{{ roster.staff }}</view>
          <view class="stat-label">工作人员</view>
        </view>
      </view>

      <view v-for="group in roster.groupList" :key="group.id" class="group-item">
        <view class="group-name">{{ group.name }}</view>
        <view class="group-members">{{ group.members.join('、') }}</view>
      </view>
    </view>

    <view class="btn-row">
      <button class="btn primary" @click="download('pdf')">下载 PDF</button>
      <button class="btn" @click="download('doc')">下载 Word</button>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref } from 'vue';

uni.setNavigationBarTitle({ title: '学员手册' });

const meeting = ref({
  name: '2025党务干部培训班',
  date: '2026-03-10'
});

const toc = ref([
  { key: 'schedule', name: '会议日程' },
  { key: 'tips', name: '参会须知' },
  { key: 'roster', name: '分组与名册' }
]);

const schedule = ref([
  { id: 1, time: '08:30', title: '开班仪式', location: '主会场' },
  { id: 2, time: '10:00', title: '专题授课：党建创新实践', location: '报告厅A' },
  { id: 3, time: '14:30', title: '分组研讨', location: 'A302/A303/A305' },
  { id: 4, time: '16:30', title: '研讨汇报与总结', location: '主会场' }
]);

const tips = ref([
  '请提前20分钟到场，按座位号就座。',
  '培训期间请保持手机静音。',
  '请按时签到、完成学习任务与问卷。',
  '如需请假或外出，请向班主任报备。'
]);

const roster = ref({
  total: 142,
  groups: 6,
  staff: 12,
  groupList: [
    { id: 1, name: '第一组（红旗组）', members: ['张伟', '李敏', '周涛', '王芳'] },
    { id: 2, name: '第二组（先锋组）', members: ['陈刚', '赵楠', '刘静', '吴丹'] }
  ]
});

const download = (type) => {
  uni.showToast({
    title: type === 'pdf' ? '正在准备PDF' : '正在准备Word',
    icon: 'none'
  });
};
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 24rpx;
}

.cover-card,
.toc-card,
.section-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 18rpx;
}

.cover-card {
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
  color: #fff;
}

.cover-icon { font-size: 52rpx; }
.cover-title { margin-top: 8rpx; font-size: 38rpx; font-weight: 700; }
.cover-sub { margin-top: 8rpx; font-size: 26rpx; opacity: 0.95; }
.cover-date { margin-top: 6rpx; font-size: 22rpx; opacity: 0.9; }

.section-title { font-size: 30rpx; font-weight: 700; color: #1e293b; margin-bottom: 14rpx; }

.toc-item { display: flex; align-items: center; padding: 10rpx 0; border-top: 1rpx solid #e2e8f0; }
.toc-index {
  width: 36rpx; height: 36rpx; border-radius: 50%;
  background: #eff6ff; color: #2563eb; text-align: center; line-height: 36rpx;
  margin-right: 10rpx; font-size: 22rpx;
}
.toc-text { color: #334155; font-size: 26rpx; }

.schedule-item { display: flex; gap: 14rpx; border-top: 1rpx solid #e2e8f0; padding: 14rpx 0; }
.time { width: 90rpx; color: #2563eb; font-size: 24rpx; font-weight: 700; }
.event-title { color: #0f172a; font-size: 27rpx; font-weight: 600; }
.event-location { margin-top: 4rpx; color: #64748b; font-size: 24rpx; }

.tip-item { border-top: 1rpx solid #e2e8f0; padding: 12rpx 0; color: #334155; font-size: 25rpx; }

.stats-row { display: flex; gap: 10rpx; margin-bottom: 10rpx; }
.stat-box { flex: 1; text-align: center; background: #f8fafc; border-radius: 10rpx; padding: 12rpx 6rpx; }
.stat-value { font-size: 30rpx; color: #1e293b; font-weight: 700; }
.stat-label { margin-top: 2rpx; font-size: 22rpx; color: #64748b; }

.group-item { border-top: 1rpx solid #e2e8f0; padding: 12rpx 0; }
.group-name { font-size: 26rpx; color: #0f172a; font-weight: 600; }
.group-members { margin-top: 4rpx; font-size: 24rpx; color: #64748b; }

.btn-row { display: flex; gap: 12rpx; margin-top: 6rpx; margin-bottom: 30rpx; }
.btn { flex: 1; border-radius: 10rpx; background: #fff; color: #2563eb; border: 1rpx solid #2563eb; font-size: 26rpx; }
.btn.primary { background: #2563eb; color: #fff; border: none; }
</style>
