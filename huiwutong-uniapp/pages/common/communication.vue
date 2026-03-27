<template>
  <view class="communication-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">沟通中心</text>
      </view>
    </view>

    <!-- Tab切换 -->
    <view class="tab-container">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        <text class="tab-text">{{ tab.name }}</text>
        <text v-if="tab.badge > 0" class="tab-badge">{{ tab.badge }}</text>
      </view>
    </view>

    <!-- 群组列表 -->
    <view v-if="currentTab === 0" class="group-list">
      <view v-if="groupList.length > 0">
        <view
          v-for="(group, index) in groupList"
          :key="index"
          class="group-item list-item"
          @click="goToChat(group, 'group')"
        >
          <view class="group-avatar">{{ group.avatar }}</view>
          <view class="group-info">
            <view class="group-top">
              <text class="group-name">{{ group.name }}</text>
              <text class="group-time">{{ group.time }}</text>
            </view>
            <view class="group-bottom">
              <text class="group-message">{{ group.lastMessage }}</text>
              <view v-if="group.unread > 0" class="group-badge">{{ group.unread }}</view>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-icon"><text class="fa fa-users"></text></text>
        <text class="empty-text">暂无群组</text>
      </view>
    </view>

    <!-- 私聊列表 -->
    <view v-if="currentTab === 1" class="private-list">
      <view v-if="privateList.length > 0">
        <view
          v-for="(chat, index) in privateList"
          :key="index"
          class="private-item list-item"
          @click="goToChat(chat, 'private')"
        >
          <view class="private-avatar">
            <text>{{ chat.avatar }}</text>
          </view>
          <view class="private-info">
            <view class="private-top">
              <text class="private-name">{{ chat.name }}</text>
              <text class="private-time">{{ chat.time }}</text>
            </view>
            <view class="private-bottom">
              <text class="private-message">{{ chat.lastMessage }}</text>
              <view v-if="chat.unread > 0" class="private-badge">{{ chat.unread }}</view>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-icon"><text class="fa fa-comments"></text></text>
        <text class="empty-text">暂无私聊</text>
      </view>
    </view>

    <!-- 创建群组按钮 -->
    <view class="create-group-btn" @click="createGroup">
      <text class="create-icon">➕</text>
      <text class="create-text">创建群组</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentTab: 0,
      tabs: [
        { name: '群组', badge: 5 },
        { name: '私聊', badge: 2 }
      ],
      groupList: [
        {
          id: 1,
          name: '第1小组',
          avatar: '1组',
          time: '10:30',
          lastMessage: '明天上午的课程记得准时参加',
          unread: 3
        },
        {
          id: 2,
          name: '党务干部培训班',
          avatar: '全体',
          time: '09:15',
          lastMessage: '班主任：请大家提交课程反馈',
          unread: 2
        },
        {
          id: 3,
          name: '讨论组A',
          avatar: '讨论',
          time: '昨天',
          lastMessage: '讨论材料已上传',
          unread: 0
        }
      ],
      privateList: [
        {
          id: 1,
          name: '张老师',
          avatar: '张',
          time: '11:20',
          lastMessage: '好的，我会按时参加',
          unread: 1
        },
        {
          id: 2,
          name: '李同学',
          avatar: '李',
          time: '10:45',
          lastMessage: '请问明天的课程在哪里？',
          unread: 1
        },
        {
          id: 3,
          name: '王组长',
          avatar: '王',
          time: '昨天',
          lastMessage: '收到',
          unread: 0
        }
      ]
    }
  },

  methods: {
    /**
     * 切换Tab
     */
    switchTab(index) {
      this.currentTab = index
    },

    /**
     * 进入聊天
     */
    goToChat(chat, type) {
      // 清除未读
      chat.unread = 0

      uni.navigateTo({
        url: `/pages/learner/chat-room?id=${chat.id}&type=${type}&name=${chat.name}`
      })
    },

    /**
     * 创建群组
     */
    createGroup() {
      uni.showToast({
        title: '创建群组功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.communication-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.tab-container {
  display: flex;
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  height: 88rpx;
  position: relative;
}

.tab-item.active {
  color: $primary-color;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: $primary-color;
  border-radius: 2rpx;
}

.tab-text {
  font-size: $font-size-md;
  font-weight: 500;
}

.tab-badge {
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: $danger-color;
  color: $text-white;
  font-size: $font-size-xs;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-list,
.private-list {
  padding: $spacing-md;
}

.list-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-sm;
  transition: $transition-base;
}

.list-item:active {
  transform: scale(0.98);
  background: $bg-tertiary;
}

.group-avatar,
.private-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: $border-radius-lg;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-lg;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.group-info,
.private-info {
  flex: 1;
  min-width: 0;
}

.group-top,
.private-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6rpx;
}

.group-name,
.private-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-time,
.private-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
  flex-shrink: 0;
}

.group-bottom,
.private-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.group-message,
.private-message {
  font-size: $font-size-sm;
  color: $text-secondary;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-badge,
.private-badge {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  background: $danger-color;
  color: $text-white;
  font-size: $font-size-xs;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.create-group-btn {
  position: fixed;
  bottom: calc(100rpx + constant(safe-area-inset-bottom));
  bottom: calc(100rpx + env(safe-area-inset-bottom));
  right: $spacing-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: $spacing-md;
  background: $primary-color;
  color: $text-white;
  border-radius: 50%;
  box-shadow: $shadow-md;
  z-index: $z-index-fixed;
}

.create-icon {
  font-size: 40rpx;
  line-height: 1;
}

.create-text {
  font-size: $font-size-xs;
  font-weight: 500;
}
</style>
