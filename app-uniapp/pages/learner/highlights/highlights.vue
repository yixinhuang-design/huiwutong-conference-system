<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <text class="title">{{ meeting.title }}</text>
    </view>
    <scroll-view scroll-y class="scroll-content">
      <view class="card">
        <view class="card-header">
          <text class="card-icon">📷</text>
          <text class="card-title">图文精选</text>
        </view>
        <view class="highlight-list">
          <view v-for="highlight in highlights" :key="highlight.id" class="highlight-item">
            <view class="highlight-image">
              <text class="placeholder-img">{{ highlight.icon }}</text>
            </view>
            <view class="highlight-info">
              <text class="highlight-title">{{ highlight.title }}</text>
              <text class="highlight-meta">{{ highlight.date }} · {{ highlight.location }}</text>
            </view>
            <view class="highlight-actions">
              <button class="action-btn-sm" @click="download(highlight)">📥</button>
              <button class="action-btn-sm" @click="share(highlight)">📄</button>
            </view>
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

const highlights = ref([
  { id: 1, title: '开班仪式合影', date: '2026-02-20', location: '培训开班仪式现场', icon: '📷' },
  { id: 2, title: '小组讨论精彩绝间', date: '2026-02-19', location: '分组头脑风暗', icon: '📷' },
  { id: 3, title: '讲师授课风采', date: '2026-02-18', location: '现场互动提问', icon: '📷' },
  { id: 4, title: '学员才艺展示', date: '2026-02-17', location: '晚会节目花绛', icon: '📷' }
]);

const download = (highlight) => {
  uni.showToast({
    title: '开始下载: ' + highlight.title,
    icon: 'none'
  });
};

const share = (highlight) => {
  uni.showToast({
    title: '已复制分享链接',
    icon: 'success',
    duration: 1500
  });
};

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '精彩花絮' });
  loadHighlights();
});

const loadHighlights = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/meeting/highlights', method: 'GET' });
    if (res[1]?.data) highlights.value = res[1].data;
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

.highlight-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.highlight-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 4px;
}

.highlight-image {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.placeholder-img {
  font-size: 24px;
}

.highlight-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.highlight-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.highlight-meta {
  font-size: 12px;
  color: #94a3b8;
}

.highlight-actions {
  display: flex;
  gap: 6px;
  flex-direction: column;
  justify-content: center;
}

.action-btn-sm {
  width: 32px;
  height: 32px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}
</style>
