<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <text class="title">{{ meeting.title }}</text>
      <button class="action-btn" @click="downloadAll">📥 一键打包</button>
    </view>
    <scroll-view scroll-y class="scroll-content">
      <view class="card">
        <view class="card-header">
          <text class="card-icon">📚</text>
          <text class="card-title">课程资料</text>
        </view>
        <view class="material-list">
          <view v-for="material in materials" :key="material.id" class="material-item">
            <text class="material-icon">{{ getIcon(material.type) }}</text>
            <view class="material-info">
              <text class="material-title">{{ material.title }}</text>
              <text class="material-meta">{{ material.type }} · {{ material.uploadDate }}</text>
            </view>
            <view class="material-actions">
              <button class="action-btn-sm" @click="download(material)">📥 下载</button>
              <button class="action-btn-sm" @click="favorite(material)">⭐ {{ material.starred ? '已' : '' }}收藏</button>
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

const materials = ref([
  { id: 1, title: '《党务干部培训手册》', type: 'PDF', uploadDate: '2026-02-20 上传', starred: false },
  { id: 2, title: '培训开班仪式PPT', type: 'PPT', uploadDate: '2026-02-18 上传', starred: true },
  { id: 3, title: '心得体会范文.docx', type: 'Word', uploadDate: '2026-02-17 上传', starred: false },
  { id: 4, title: '培训课程回放', type: '视频', uploadDate: '2026-02-16 上传', starred: false }
]);

const getIcon = (type) => {
  const icons = {
    'PDF': '📄',
    'PPT': '📏',
    'Word': '📄',
    '视频': '🎥'
  };
  return icons[type] || '📊';
};

const download = (material) => {
  uni.showToast({
    title: '开始下载: ' + material.title,
    icon: 'none'
  });
};

const downloadAll = () => {
  uni.showToast({
    title: '正在打包下载...',
    icon: 'loading'
  });
};

const favorite = (material) => {
  material.starred = !material.starred;
  uni.showToast({
    title: material.starred ? '已收藏' : '已取消收藏',
    icon: 'success',
    duration: 1500
  });
};

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '资料下载' });
  loadMaterials();
});

const loadMaterials = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/meeting/materials', method: 'GET' });
    if (res[1]?.data) materials.value = res[1].data;
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
  gap: 8px;
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

.action-btn {
  padding: 6px 12px;
  background: #2563eb;
  color: white;
  border-radius: 4px;
  font-size: 12px;
  border: none;
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

.material-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.material-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 4px;
}

.material-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.material-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.material-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.material-meta {
  font-size: 12px;
  color: #94a3b8;
}

.material-actions {
  display: flex;
  gap: 6px;
  flex-direction: column;
  justify-content: center;
}

.action-btn-sm {
  padding: 4px 8px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 3px;
  font-size: 11px;
  color: #475569;
}
</style>
