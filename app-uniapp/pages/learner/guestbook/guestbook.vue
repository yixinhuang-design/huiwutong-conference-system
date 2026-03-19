<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <text class="title">留言赠语</text>
    </view>
    <scroll-view scroll-y class="scroll-content">
      <!-- Stats -->
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-number">{{ stats.totalMessages }}</text>
          <text class="stat-label">总消息数</text>
        </view>
        <view class="stat-card">
          <text class="stat-number">{{ stats.participants }}</text>
          <text class="stat-label">参与人数</text>
        </view>
        <view class="stat-card">
          <text class="stat-number">{{ stats.today }}</text>
          <text class="stat-label">今日消息</text>
        </view>
      </view>

      <!-- Composer -->
      <view class="composer-card">
        <view class="composer-title">发表留言</view>
        <textarea v-model="newMessage" class="message-input" placeholder="输入您的喜悦、公轴或疏灶..." />
        <view class="composer-tags">
          <view v-for="tag in tags" :key="tag" class="tag" :class="{ active: selectedTag === tag }" @click="selectedTag = tag">
            {{ tag }}
          </view>
        </view>
        <view class="composer-actions">
          <button class="btn-post" @click="postMessage">📋 发表</button>
          <button class="btn-cancel" @click="newMessage = ''">取消</button>
        </view>
      </view>

      <!-- Messages -->
      <view class="messages-section">
        <view v-for="message in messages" :key="message.id" class="message-card">
          <view class="message-header">
            <text class="user-avatar" :class="'avatar-' + message.color">{{ message.initials }}</text>
            <view class="user-info">
              <text class="user-name">{{ message.author }}</text>
              <text class="user-time">{{ message.time }}</text>
            </view>
          </view>
          <text class="message-content">{{ message.content }}</text>
          <view v-if="message.tag" class="message-tag">{{ message.tag }}</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const newMessage = ref('');
const selectedTag = ref('悦读');

const tags = ref(['悦读', '公轴', '疏灖', '资料', '体会']);

const stats = ref({
  totalMessages: 142,
  participants: 86,
  today: 12
});

const messages = ref([
  {
    id: 1,
    author: '椎子断',
    time: '2小时前',
    content: '这次培训真是收获源滿！各位讲师都是业界个中作手，阻掛了！',
    tag: '悦读',
    color: 'blue',
    initials: 'L'
  },
  {
    id: 2,
    author: '刘媗',
    time: '1小时前',
    content: '同上！这个并配资料也很新颖，急推给四个人的大能伊',
    tag: '悦读',
    color: 'purple',
    initials: 'L'
  },
  {
    id: 3,
    author: '学员C',
    time: '中午',
    content: '感谢大家的支持！特别晖应各路下庭同好的麻橆、支持。公众的集体下功，旅程車钨不好，作为泰民，就是当和克做了。',
    tag: '疏灖',
    color: 'orange',
    initials: 'S'
  }
]);

const postMessage = () => {
  if (!newMessage.value.trim()) {
    uni.showToast({
      title: '请漆序内容',
      icon: 'none'
    });
    return;
  }
  messages.value.unshift({
    id: messages.value.length + 1,
    author: '你',
    time: '岚时',
    content: newMessage.value,
    tag: selectedTag.value,
    color: 'blue',
    initials: 'Y'
  });
  stats.value.totalMessages++;
  stats.value.today++;
  newMessage.value = '';
  uni.showToast({
    title: '发表成功',
    icon: 'success',
    duration: 1500
  });
};

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '留言赠语' });
  loadGuestbookData();
});

const loadGuestbookData = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/meeting/guestbook', method: 'GET' });
    if (res[1]?.data) {
      messages.value = res[1].data.messages || messages.value;
      stats.value = res[1].data.stats || stats.value;
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

.stats-grid {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.stat-card {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 20px;
  font-weight: bold;
  color: #2563eb;
  display: block;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 11px;
  color: #94a3b8;
}

.composer-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.composer-title {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
  margin-bottom: 12px;
}

.message-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 14px;
  color: #1e293b;
  resize: vertical;
  min-height: 80px;
  margin-bottom: 12px;
}

.composer-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.tag {
  padding: 6px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: #f8fafc;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.composer-actions {
  display: flex;
  gap: 8px;
}

.btn-post {
  flex: 1;
  padding: 10px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
}

.btn-cancel {
  flex: 1;
  padding: 10px;
  background: #f8fafc;
  color: #475569;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 14px;
}

.messages-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.avatar-blue {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}

.avatar-green {
  background: linear-gradient(135deg, #10b981, #059669);
}

.avatar-purple {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.avatar-orange {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
}

.user-time {
  font-size: 12px;
  color: #94a3b8;
}

.message-content {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin-bottom: 12px;
  display: block;
}

.message-tag {
  display: inline-block;
  padding: 4px 8px;
  background: #f0f9ff;
  border-radius: 3px;
  font-size: 11px;
  color: #1e40af;
  font-weight: 500;
}
</style>
