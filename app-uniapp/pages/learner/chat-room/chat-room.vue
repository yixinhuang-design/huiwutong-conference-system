<template>
  <view class="container">
    <view class="header">
      <button class="back-btn" @click="goBack">←</button>
      <view class="header-content">
        <text class="group-name">{{ group.name }}</text>
        <text class="group-info">{{ group.memberCount }}成员 · {{ group.topic }}</text>
      </view>
    </view>
    <view class="announcement" v-if="announcement">
      <text class="ann-title">📢 最新公告</text>
      <text class="ann-content">{{ announcement }}</text>
    </view>
    <scroll-view scroll-y class="messages-scroll" :scroll-into-view="scrollIntoView">
      <view v-for="(msg, idx) in messages" :key="msg.id" :id="'msg-' + msg.id" class="message-wrapper" :class="{ 'user-msg': msg.isUser }">
        <view class="message-divider" v-if="idx > 0 && isDifferentDay(messages[idx-1], msg)">{{ msg.time }}</view>
        <view class="message-content">
          <text v-if="!msg.isUser" class="avatar" :class="'avatar-' + msg.color">{{ msg.initials }}</text>
          <view class="bubble" :class="{ 'user-bubble': msg.isUser }">
            <text class="bubble-text">{{ msg.content }}</text>
            <text class="bubble-time">{{ msg.time }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
    <view class="input-area">
      <button class="action-btn" @click="showActions = !showActions">🔧</button>
      <input v-model="inputText" type="text" class="message-input" placeholder="发送消息..." />
      <button class="send-btn" @click="sendMessage">✈</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const group = ref({
  id: '1',
  name: '全体学员群 · 2025党务干部培训班',
  memberCount: 142,
  topic: '上午签到'
});

const announcement = ref('请全体学员在 09:00 前完成报到签到，并执会务工作人员帮忙');

const inputText = ref('');
const showActions = ref(false);
const scrollIntoView = ref('');

const messages = ref([
  {
    id: 1,
    author: '张主任',
    content: '上午好，各位学员请携带证件在 09:00 前完成签到。签到地点在报告厅门口，若遇排队请听从工作人员指引。',
    time: '08:00',
    isUser: false,
    color: 'blue',
    initials: 'Z'
  },
  {
    id: 2,
    author: '李华',
    content: '已到达党校，请问签到完后资料领取点在哪？',
    time: '08:05',
    isUser: true,
    color: 'blue',
    initials: 'L'
  },
  {
    id: 3,
    author: '李伟',
    content: '签到完毕后在签到处旁边领取资料礼包，里面有日程表、胸牌、课堂笔记本等。',
    time: '08:06',
    isUser: false,
    color: 'green',
    initials: 'L'
  },
  {
    id: 4,
    author: '王梅',
    content: '请问下午分组讨论室在哪？有没有路线图？',
    time: '08:20',
    isUser: true,
    color: 'orange',
    initials: 'W'
  },
  {
    id: 5,
    author: '管理员',
    content: '下午 17:00 的分组讨论安排在 A 栋三层 302-304 室，可在"座位图"查看详细路线图。',
    time: '08:25',
    isUser: false,
    color: 'purple',
    initials: 'G'
  }
]);

const isDifferentDay = (prev, curr) => {
  return prev.time.split(' ')[0] !== curr.time.split(' ')[0];
};

const sendMessage = () => {
  if (!inputText.value.trim()) return;
  const now = new Date();
  const time = now.getHours() + ':' + String(now.getMinutes()).padStart(2, '0');
  messages.value.push({
    id: messages.value.length + 1,
    author: '我',
    content: inputText.value,
    time: time,
    isUser: true,
    color: 'blue',
    initials: 'W'
  });
  inputText.value = '';
  scrollIntoView.value = 'msg-' + (messages.value.length);
};

const goBack = () => {
  uni.navigateBack();
};

onMounted(() => {
  uni.setNavigationBarTitle({ title: group.value.name.split(' ')[0] });
});
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
  position: relative;
  z-index: 10;
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

.header-content {
  flex: 1;
}

.group-name {
  font-size: 14px;
  font-weight: bold;
  color: #1e293b;
  display: block;
}

.group-info {
  font-size: 11px;
  color: #94a3b8;
}

.announcement {
  background: #fef3c7;
  border-left: 4px solid #f59e0b;
  padding: 12px 16px;
  color: #92400e;
  font-size: 13px;
  line-height: 1.5;
}

.ann-title {
  font-weight: bold;
  display: block;
  margin-bottom: 4px;
}

.ann-content {
  display: block;
}

.messages-scroll {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}

.message-wrapper {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-wrapper.user-msg {
  align-items: flex-end;
}

.message-divider {
  font-size: 11px;
  color: #94a3b8;
  text-align: center;
  width: 100%;
  margin: 12px 0;
}

.message-content {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.message-wrapper.user-msg .message-content {
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
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

.bubble {
  max-width: 70%;
  background: white;
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
}

.bubble.user-bubble {
  background: #2563eb;
  border-color: #2563eb;
}

.bubble-text {
  font-size: 13px;
  color: #475569;
  display: block;
  word-wrap: break-word;
}

.bubble.user-bubble .bubble-text {
  color: white;
}

.bubble-time {
  font-size: 11px;
  color: #94a3b8;
  display: block;
  margin-top: 4px;
}

.bubble.user-bubble .bubble-time {
  color: rgba(255, 255, 255, 0.7);
}

.input-area {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #e2e8f0;
  align-items: center;
}

.action-btn {
  width: 36px;
  height: 36px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 18px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  font-size: 14px;
  color: #1e293b;
}

.send-btn {
  width: 36px;
  height: 36px;
  background: #2563eb;
  border: none;
  border-radius: 50%;
  color: white;
  font-size: 16px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
