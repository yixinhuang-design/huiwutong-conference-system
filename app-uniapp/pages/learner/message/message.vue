<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <text class="title">{{ meeting.title }}</text>
      <button class="write-btn" @click="openComposer">📝 写留言</button>
    </view>
    <scroll-view scroll-y class="scroll-content">
      <view class="message-list">
        <view v-for="message in messages" :key="message.id" class="message-card">
          <view class="message-header">
            <text class="user-avatar" :class="'avatar-' + message.color">{{ message.initials }}</text>
            <view class="user-info">
              <text class="user-name">{{ message.author }} · {{ message.organization }}</text>
              <text class="message-content">"{{ message.content }}"</text>
            </view>
          </view>
          <view class="message-footer">
            <button class="like-btn" :class="{ liked: message.liked }" @click="toggleLike(message)">👏 {{ message.likes }}</button>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Composer Modal -->
    <view v-if="showComposer" class="modal-overlay" @click="closeComposer">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">写下您的喜悦、公轴或疏灖</text>
          <button class="close-btn" @click="closeComposer">×</button>
        </view>
        <textarea v-model="newMessage" class="message-input" placeholder="留下您的言读..." />
        <view class="modal-actions">
          <button class="btn-post" @click="postMessage">发表</button>
          <button class="btn-cancel" @click="closeComposer">取消</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const meeting = ref({
  id: '1',
  title: '2025党务干部培训班'
});

const showComposer = ref(false);
const newMessage = ref('');

const messages = ref([
  {
    id: 1,
    author: '张伟',
    organization: '市委组织部',
    content: '本次培训让我深判理解党建数字化的重要意义，感谢老师们的惂心指导。',
    likes: 23,
    liked: false,
    color: 'blue',
    initials: 'Z'
  },
  {
    id: 2,
    author: '刘媗',
    organization: '市直机关',
    content: '期待与大家保持联系，共同推进基层党建工作。',
    likes: 15,
    liked: false,
    color: 'green',
    initials: 'L'
  }
]);

const openComposer = () => {
  showComposer.value = true;
};

const closeComposer = () => {
  showComposer.value = false;
  newMessage.value = '';
};

const postMessage = () => {
  if (!newMessage.value.trim()) {
    uni.showToast({
      title: '请输入消息',
      icon: 'none'
    });
    return;
  }
  messages.value.unshift({
    id: messages.value.length + 1,
    author: '我',
    organization: '未日会议',
    content: newMessage.value,
    likes: 0,
    liked: false,
    color: 'blue',
    initials: 'W'
  });
  closeComposer();
  uni.showToast({
    title: '发表成功',
    icon: 'success',
    duration: 1500
  });
};

const toggleLike = (message) => {
  if (message.liked) {
    message.likes--;
  } else {
    message.likes++;
  }
  message.liked = !message.liked;
};

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: '留言赠语' });
  loadMessages();
});

const loadMessages = async () => {
  try {
    const res = await uni.request({ url: 'http://localhost:8082/api/meeting/messages', method: 'GET' });
    if (res[1]?.data) messages.value = res[1].data;
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

.write-btn {
  padding: 6px 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
}

.scroll-content {
  flex: 1;
  padding: 12px;
}

.message-list {
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

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
  display: block;
  margin-bottom: 6px;
}

.message-content {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.message-footer {
  display: flex;
  gap: 8px;
}

.like-btn {
  padding: 4px 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 12px;
  color: #475569;
}

.like-btn.liked {
  background: #fef3c7;
  color: #92400e;
  border-color: #fcd34d;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  background: white;
  border-radius: 12px 12px 0 0;
  padding: 16px;
  max-height: 80vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.modal-title {
  font-size: 16px;
  font-weight: bold;
  color: #1e293b;
}

.close-btn {
  width: 32px;
  height: 32px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 24px;
  color: #64748b;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 14px;
  color: #1e293b;
  min-height: 100px;
  resize: vertical;
  margin-bottom: 12px;
}

.modal-actions {
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
</style>
