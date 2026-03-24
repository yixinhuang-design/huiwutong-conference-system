<template>
  <view class="chat-room-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">{{ chatName }}</text>
        <text class="header-action" @click="showMembers">›</text>
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
          <view v-if="!message.isSelf" class="sender-name">
            {{ message.senderName }}
          </view>
          <view class="message-bubble" :class="{ 'is-self': message.isSelf }">
            <text class="message-text">{{ message.content }}</text>
            <text class="message-time">{{ message.time }}</text>
          </view>
        </view>
        <view v-if="message.isSelf" class="message-avatar">
          我
        </view>
      </view>
    </scroll-view>

    <!-- 输入框 -->
    <view class="input-bar">
      <view class="input-left">
        <text class="input-icon" @click="chooseImage">📷</text>
        <text class="input-icon" @click="chooseFile">📎</text>
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
import GroupAPI from '@/api/group'

export default {
  data() {
    return {
      chatId: '',
      chatName: '',
      chatType: 'group',
      inputText: '',
      scrollIntoView: '',
      messageList: []
    }
  },

  onLoad(options) {
    if (options.id) {
      this.chatId = options.id
    }
    if (options.name) {
      this.chatName = options.name
    }
    if (options.type) {
      this.chatType = options.type
    }

    this.loadMessages()
    this.scrollToBottom()
  },

  onUnload() {
    // 离开页面时标记消息已读
    this.markAsRead()
  },

  methods: {
    /**
     * 加载消息
     */
    async loadMessages() {
      try {
        const data = await GroupAPI.getGroupMessages(this.chatId)
        const msgs = data.records || data || []
        this.messageList = msgs.map(m => ({
          id: m.id,
          senderId: m.senderId,
          senderName: m.senderName || '系统',
          content: m.content,
          time: m.createTime ? new Date(m.createTime).toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'}) : '',
          isSelf: m.senderId == 1
        }))
        this.scrollToBottom()
      } catch (error) {
        console.error('Load messages error:', error)
      }
    },

    /**
     * 发送消息
     */
    async sendMessage() {
      if (!this.inputText.trim()) {
        return
      }

      const message = {
        id: Date.now(),
        senderId: 1,
        senderName: '我',
        content: this.inputText,
        time: this.getCurrentTime(),
        isSelf: true
      }

      this.messageList.push(message)
      const content = this.inputText
      this.inputText = ''
      this.scrollToBottom()

      try {
        await GroupAPI.sendMessage({
          groupId: this.chatId,
          senderId: 1,
          senderName: '学员',
          msgType: 'text',
          content: content
        })
      } catch (error) {
        console.error('Send message error:', error)
      }
    },

    /**
     * 接收消息（轮询）
     */
    receiveMessage() {
      // 实际场景应用WebSocket或轮询
    },

    /**
     * 滚动到底部
     */
    scrollToBottom() {
      this.$nextTick(() => {
        if (this.messageList.length > 0) {
          this.scrollIntoView = 'msg-' + (this.messageList.length - 1)
        }
      })
    },

    /**
     * 获取当前时间
     */
    getCurrentTime() {
      const now = new Date()
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      return `${hour}:${minute}`
    },

    /**
     * 选择图片
     */
    chooseImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          // TODO: 上传图片并发送
          uni.showToast({
            title: '图片功能开发中',
            icon: 'none'
          })
        }
      })
    },

    /**
     * 选择文件
     */
    chooseFile() {
      uni.showToast({
        title: '文件功能开发中',
        icon: 'none'
      })
    },

    /**
     * 显示成员列表
     */
    showMembers() {
      if (this.chatType === 'group') {
        uni.navigateTo({
          url: `/pages/learner/group-members?id=${this.chatId}`
        })
      }
    },

    /**
     * 标记已读
     */
    async markAsRead() {
      try {
        // TODO: 调用API标记已读
        // await this.$api.chat.markRead(this.chatId)
      } catch (error) {
        console.error('Mark as read error:', error)
      }
    },

    /**
     * 返回
     */
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

.chat-room-container {
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
  z-index: $z-index-sticky;
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
}

.back-btn {
  font-size: 48rpx;
  color: $text-primary;
  font-weight: 300;
  padding: 0 $spacing-sm;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
  text-align: center;
}

.header-action {
  font-size: 48rpx;
  color: $text-primary;
  font-weight: 300;
  padding: 0 $spacing-sm;
}

.message-list {
  flex: 1;
  padding: $spacing-md;
  overflow-y: auto;
}

.message-item {
  display: flex;
  margin-bottom: $spacing-md;
  align-items: flex-start;
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
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  max-width: 70%;
  margin: 0 $spacing-sm;
}

.message-item.is-self .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.sender-name {
  font-size: $font-size-xs;
  color: $text-secondary;
  margin-bottom: 6rpx;
  margin-left: $spacing-sm;
}

.message-bubble {
  padding: $spacing-md;
  border-radius: $border-radius-md;
  background: $bg-primary;
  position: relative;
}

.message-bubble.is-self {
  background: $primary-color;
}

.message-text {
  font-size: $font-size-md;
  line-height: 1.6;
  word-wrap: break-word;
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

.input-left {
  display: flex;
  gap: $spacing-sm;
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
  font-weight: 500;
}

.send-btn:disabled {
  opacity: 0.5;
}
</style>
