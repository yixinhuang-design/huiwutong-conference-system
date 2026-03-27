<template>
  <view class="chat-private-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">{{ userName }}</text>
        <text class="header-action" @click="showUserInfo">⋮</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="message-list"
      scroll-y
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
    >
      <view
        v-for="(message, index) in messageList"
        :key="index"
        :id="'msg-' + index"
        class="message-item"
        :class="{ 'is-self': message.isSelf }"
      >
        <view v-if="!message.isSelf" class="message-avatar">
          {{ message.senderName.charAt(message.senderName.length - 1) }}
        </view>
        <view class="message-content">
          <view class="message-bubble" :class="{ 'is-self': message.isSelf }">
            <text class="message-text">{{ message.content }}</text>
            <text class="message-time">{{ message.time }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入框 -->
    <view class="input-bar">
      <view class="input-left">
        <text class="input-icon" @click="chooseImage">📷</text>
      </view>
      <view class="input-wrapper">
        <input
          v-model="inputText"
          class="message-input"
          placeholder="输入消息..."
          @confirm="sendMessage"
        />
      </view>
      <button
        class="send-btn"
        :disabled="!inputText.trim()"
        @click="sendMessage"
      >
        发送
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      targetUserId: '',
      userName: '',
      inputText: '',
      scrollIntoView: '',
      messageList: [
        { id: 1, senderId: 2, senderName: '王小明', content: '你好！', time: '09:30', isSelf: false },
        { id: 2, senderId: 1, senderName: '我', content: '你好！', time: '09:31', isSelf: true }
      ]
    }
  },

  onLoad(options) {
    if (options.id) this.targetUserId = options.id
    if (options.name) this.userName = options.name
    this.scrollToBottom()
  },

  methods: {
    sendMessage() {
      if (!this.inputText.trim()) return

      this.messageList.push({
        id: Date.now(),
        senderId: 1,
        senderName: '我',
        content: this.inputText,
        time: this.getCurrentTime(),
        isSelf: true
      })

      this.inputText = ''
      this.scrollToBottom()

      setTimeout(() => {
        this.messageList.push({
          id: Date.now(),
          senderId: 2,
          senderName: this.userName,
          content: '收到！',
          time: this.getCurrentTime(),
          isSelf: false
        })
        this.scrollToBottom()
      }, 2000)
    },

    scrollToBottom() {
      this.$nextTick(() => {
        if (this.messageList.length > 0) {
          this.scrollIntoView = 'msg-' + (this.messageList.length - 1)
        }
      })
    },

    getCurrentTime() {
      const now = new Date()
      return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
    },

    chooseImage() {
      uni.showToast({ title: '图片功能开发中', icon: 'none' })
    },

    showUserInfo() {
      uni.showModal({ title: this.userName, content: '市委组织部\n副处长', showCancel: true })
    },

    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.chat-private-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $bg-secondary;
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  border-bottom: 1rpx solid $border-color;
}

.header-content {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 $spacing-md;
}

.back-btn {
  font-size: 48rpx;
  color: $text-primary;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
  text-align: center;
}

.header-action {
  font-size: 40rpx;
  color: $text-primary;
}

.message-list {
  flex: 1;
  padding: $spacing-md;
}

.message-item {
  display: flex;
  margin-bottom: $spacing-md;
}

.message-item.is-self {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-sm;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-content {
  flex: 1;
  max-width: 70%;
  margin: 0 $spacing-sm;
}

.message-bubble {
  padding: $spacing-md;
  border-radius: $border-radius-md;
  background: $bg-primary;
}

.message-bubble.is-self {
  background: $primary-color;
}

.message-text {
  font-size: $font-size-md;
  line-height: 1.6;
}

.message-bubble.is-self .message-text {
  color: $text-white;
}

.message-time {
  display: block;
  font-size: $font-size-xs;
  color: $text-tertiary;
  margin-top: $spacing-xs;
}

.message-bubble.is-self .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.input-bar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-primary;
  border-top: 1rpx solid $border-color;
}

.input-icon {
  font-size: 44rpx;
  padding: $spacing-sm;
}

.input-wrapper {
  flex: 1;
}

.message-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
}

.send-btn {
  padding: $spacing-md $spacing-lg;
  background: $primary-color;
  color: $text-white;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
}

.send-btn:disabled {
  opacity: 0.5;
}
</style>
