<template>
  <view class="container">
    <view class="header">
      <button class="btn-back" @click="goBack">‹</button>
      <view class="title">私聊 · {{ chatName }}</view>
    </view>

    <view class="info-banner">
      <text>💡 私聊消息仅双方可见，重要信息系统会自动通知班主任</text>
    </view>

    <view class="chat-box">
      <view class="chat-divider">今日 08:35</view>

      <view class="msg-item user" v-for="(msg, i) in messages" :key="i">
        <view v-if="msg.fromUser" class="bubble-user">
          <view class="text">{{ msg.text }}</view>
          <view class="time">{{ msg.time }}</view>
        </view>
        <view v-else class="bubble-other">
          <view class="text">{{ msg.text }}</view>
          <view class="time">{{ msg.time }}</view>
        </view>
      </view>
    </view>

    <view class="input-bar">
      <button class="btn-attach">📎</button>
      <input 
        v-model="inputText" 
        type="text" 
        class="input-field" 
        placeholder="输入消息..."
      />
      <button class="btn-send" @click="sendMessage">✈</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const chatName = ref('李华 & 张主任');
const inputText = ref('');

const messages = [
  { fromUser: true, text: '张主任早上好，我因为临时高铁延误，预计09:30到达', time: '08:35' },
  { fromUser: false, text: '可以的，抵达后向工作人员说明情况即可', time: '08:37' },
  { fromUser: false, text: '晚间查寝的提醒我已经帮你延后至22:30', time: '08:38' },
  { fromUser: true, text: '太感谢了，我到了之后再跟您确认', time: '08:39' }
];

onMounted(() => {
  uni.setNavigationBarTitle({ title: '私聊' });
});

const goBack = () => {
  uni.navigateBack();
};

const sendMessage = () => {
  if (!inputText.value.trim()) return;
  messages.push({
    fromUser: true,
    text: inputText.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  });
  inputText.value = '';
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

.info-banner {
  background: #eff6ff;
  border-left: 4rpx solid #3b82f6;
  padding: 12rpx 16rpx;
  margin: 12rpx 16rpx;
  border-radius: 6rpx;
  font-size: 12rpx;
  color: #1e40af;
}

.chat-box {
  flex: 1;
  overflow-y: auto;
  padding: 16rpx;
}

.chat-divider {
  text-align: center;
  font-size: 11rpx;
  color: #94a3b8;
  margin: 16rpx 0;
}

.msg-item {
  margin-bottom: 12rpx;
}

.msg-item.user {
  text-align: right;
}

.bubble-user {
  display: inline-block;
  max-width: 75%;
  background: #3b82f6;
  color: white;
  padding: 10rpx 12rpx;
  border-radius: 12rpx;
  margin-left: auto;
  text-align: left;
}

.bubble-other {
  display: inline-block;
  max-width: 75%;
  background: white;
  color: #475569;
  padding: 10rpx 12rpx;
  border-radius: 12rpx;
  border: 1rpx solid #e2e8f0;
}

.text {
  font-size: 14rpx;
  line-height: 1.5;
}

.time {
  font-size: 10rpx;
  opacity: 0.7;
  margin-top: 4rpx;
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 10rpx 16rpx;
  background: white;
  border-top: 1rpx solid #e2e8f0;
  gap: 8rpx;
}

.btn-attach {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #f8fafc;
  border: none;
  font-size: 20rpx;
  color: #475569;
}

.input-field {
  flex: 1;
  padding: 10rpx 12rpx;
  border: 1rpx solid #e2e8f0;
  border-radius: 20rpx;
  font-size: 14rpx;
  color: #1e293b;
}

.btn-send {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  font-size: 16rpx;
  color: white;
}

.btn-send:active {
  opacity: 0.9;
}
</style>