<template>
  <view class="chat-room-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <view class="header-center">
          <text class="header-title">工作群聊</text>
          <text class="header-subtitle">{{ onlineCount }}人在线</text>
        </view>
        <text class="header-action" @click="showMembers">成员</text>
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
        :key="message.id"
        :id="'msg-' + index"
        class="message-item"
        :class="{ 'is-self': message.isSelf, 'is-system': message.isSystem }"
      >
        <!-- 系统消息 -->
        <view v-if="message.isSystem" class="system-message">
          <text class="system-text">{{ message.content }}</text>
        </view>

        <!-- 普通消息 -->
        <view v-else class="user-message">
          <view v-if="!message.isSelf" class="message-avatar" @click="viewMember(message.sender)">
            {{ message.senderName.charAt(message.senderName.length - 1) }}
          </view>
          <view class="message-content">
            <view v-if="!message.isSelf" class="sender-name">{{ message.senderName }}</view>
            <view class="message-bubble" :class="{ 'is-self': message.isSelf }">
              <text class="message-text">{{ message.content }}</text>
              <text class="message-time">{{ message.time }}</text>
            </view>
          </view>
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
      inputText: '',
      scrollIntoView: '',
      onlineCount: 0,
      messageList: []
    }
  },

  onLoad() {
    this.loadMessages()
    this.scrollToBottom()
  },

  methods: {
    async loadMessages() {
      try {
        // 加载管理员工作群消息（第一个群组）
        const groups = await GroupAPI.listGroups({})
        if (groups && groups.length > 0) {
          const groupId = groups[0].id
          const data = await GroupAPI.getGroupMessages(groupId)
          const msgs = data.records || data || []
          this.messageList = msgs.map(m => {
            if (m.msgType === 'system') {
              return { id: m.id, isSystem: true, content: m.content, time: m.createTime ? new Date(m.createTime).toLocaleTimeString('zh-CN', {hour:'2-digit',minute:'2-digit'}) : '' }
            }
            return { id: m.id, senderId: m.senderId, senderName: m.senderName || '未知', content: m.content, time: m.createTime ? new Date(m.createTime).toLocaleTimeString('zh-CN', {hour:'2-digit',minute:'2-digit'}) : '', isSelf: m.senderId == 1 }
          })
          this.scrollToBottom()
        }
      } catch(e) { console.error('Load messages error:', e) }
    },

    async sendMessage() {
      if (!this.inputText.trim()) return
      const content = this.inputText
      this.messageList.push({
        id: Date.now(), senderId: 1, senderName: '我',
        content: content, time: this.getCurrentTime(), isSelf: true
      })
      this.inputText = ''
      this.scrollToBottom()
      try {
        const groups = await GroupAPI.listGroups({})
        if (groups && groups.length > 0) {
          await GroupAPI.sendMessage({
            groupId: groups[0].id, senderId: 1, senderName: '管理员',
            msgType: 'text', content: content
          })
        }
      } catch(e) { console.error('Send error:', e) }
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
      uni.chooseImage({
        count: 1,
        success: () => {
          uni.showToast({ title: '图片功能开发中', icon: 'none' })
        }
      })
    },

    chooseFile() {
      uni.chooseMessageFile({
        count: 1,
        success: () => {
          uni.showToast({ title: '文件功能开发中', icon: 'none' })
        }
      })
    },

    viewMember(sender) {
      uni.showModal({
        title: sender,
        content: '查看成员信息',
        showCancel: false
      })
    },

    showMembers() {
      uni.showModal({
        title: '群成员',
        content: `当前在线：${this.onlineCount}人`,
        showCancel: false
      })
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
  font-weight: 300;
}

.header-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.header-subtitle {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.header-action {
  font-size: $font-size-md;
  color: $primary-color;
}

.message-list {
  flex: 1;
  padding: $spacing-md;
}

.message-item {
  margin-bottom: $spacing-md;
}

.system-message {
  display: flex;
  justify-content: center;
  padding: $spacing-sm 0;
}

.system-text {
  font-size: $font-size-sm;
  color: $text-tertiary;
  background: rgba(0, 0, 0, 0.05);
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
}

.user-message {
  display: flex;
  gap: $spacing-sm;
}

.message-item.is-self .user-message {
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
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.message-item.is-self .message-content {
  align-items: flex-end;
}

.sender-name {
  font-size: $font-size-sm;
  color: $text-tertiary;
  margin-left: $spacing-xs;
}

.message-bubble {
  padding: $spacing-md;
  border-radius: $border-radius-md;
  background: $bg-primary;
  max-width: 100%;
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
  text-align: right;
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
  gap: $spacing-xs;
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
