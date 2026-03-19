<template>
  <view class="container">
    <view class="header">
      <button class="btn-back" @click="goBack">‹</button>
      <view class="title">团队讨论</view>
    </view>

    <view class="chat-info">
      <view class="group-avatar">👥</view>
      <view class="group-details">
        <view class="group-name">{{ groupName }}</view>
        <view class="member-count">{{ members.length }} 人在线</view>
      </view>
    </view>

    <view class="messages-container">
      <view class="message-group" v-for="(msg, i) in messages" :key="i" :class="{ 'from-user': msg.fromUser }">
        <view class="message-avatar">{{ msg.avatar }}</view>
        <view class="message-body">
          <view class="message-sender">{{ msg.sender }}</view>
          <view class="message-text">{{ msg.text }}</view>
          <view class="message-time">{{ msg.time }}</view>
        </view>
      </view>
    </view>

    <view class="input-bar">
      <button class="btn-attach">📎</button>
      <input v-model="inputText" class="input-field" placeholder="输入讨论内容..."/>
      <button class="btn-send" @click="sendMessage" :disabled="!inputText">📤</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const groupName = ref('第1小组讨论');
const inputText = ref('');

const members = [
  { id: 1, name: '李华' },
  { id: 2, name: '王小明' },
  { id: 3, name: '张三' },
  { id: 4, name: '李四' }
];

const messages = ref([
  { fromUser: false, avatar: '👨‍💼', sender: '李华', text: '大家好，今天我们讨论一下项目的进展情况。', time: '09:30' },
  { fromUser: true, avatar: '👨', sender: '你', text: '好的，李主任。我觉得目前的进度还不错。', time: '09:31' },
  { fromUser: false, avatar: '👩', sender: '王小明', text: '我同意。不过在预算方面还需要调整。', time: '09:32' },
  { fromUser: false, avatar: '👨‍💼', sender: '李华', text: '那我们先整理一份详细的预算清单，然后再讨论。', time: '09:33' }
]);

onMounted(() => {
  uni.setNavigationBarTitle({ title: '团队讨论' });
  loadChatHistory();
  scrollToBottom();
});

const loadChatHistory = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8082/api/chat/group/history',
      method: 'GET'
    });
    if (res && res.data && res.data.code === 200) {
      // 更新消息列表
    }
  } catch (error) {
    console.log('使用默认数据');
  }
};

const scrollToBottom = () => {
  setTimeout(() => {
    uni.pageScrollTo({ selector: '.messages-container', offsetTop: 10 });
  }, 100);
};

const goBack = () => {
  uni.navigateBack();
};

const sendMessage = () => {
  if (!inputText.value.trim()) return;
  
  messages.value.push({
    fromUser: true,
    avatar: '👨',
    sender: '你',
    text: inputText.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  });
  
  inputText.value = '';
  scrollToBottom();
};
</script>

<style scoped lang="scss">
.container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
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

.chat-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  background: white;
  border-bottom: 1rpx solid #e2e8f0;
}

.group-avatar {
  font-size: 32rpx;
  flex-shrink: 0;
}

.group-details {
  flex: 1;
}

.group-name {
  font-size: 14rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2rpx;
}

.member-count {
  font-size: 12rpx;
  color: #64748b;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 12rpx 16rpx;
}

.message-group {
  display: flex;
  gap: 8rpx;
  margin-bottom: 12rpx;
  align-items: flex-end;
}

.message-group.from-user {
  flex-direction: row-reverse;
}

.message-avatar {
  font-size: 24rpx;
  flex-shrink: 0;
  width: 32rpx;
  text-align: center;
}

.message-body {
  max-width: 75%;
  display: flex;
  flex-direction: column;
}

.message-group.from-user .message-body {
  align-items: flex-end;
}

.message-sender {
  font-size: 11rpx;
  color: #64748b;
  margin-bottom: 4rpx;
}

.message-text {
  background: white;
  padding: 10rpx 12rpx;
  border-radius: 8rpx;
  font-size: 14rpx;
  color: #1e293b;
  word-break: break-all;
  line-height: 1.6;
  border: 1rpx solid #e2e8f0;
}

.message-group.from-user .message-text {
  background: #3b82f6;
  color: white;
  border: none;
}

.message-time {
  font-size: 11rpx;
  color: #94a3b8;
  margin-top: 4rpx;
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx;
  background: white;
  border-top: 1rpx solid #e2e8f0;
}

.btn-attach {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: #f8fafc;
  border: none;
  font-size: 16rpx;
}

.btn-attach:active {
  background: #e2e8f0;
}

.input-field {
  flex: 1;
  padding: 10rpx 12rpx;
  background: #f8fafc;
  border: 1rpx solid #e2e8f0;
  border-radius: 8rpx;
  font-size: 14rpx;
  height: 32rpx;
}

.btn-send {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: #3b82f6;
  border: none;
  font-size: 16rpx;
  color: white;
}

.btn-send:active:not(:disabled) {
  opacity: 0.9;
}

.btn-send:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}
</style>
