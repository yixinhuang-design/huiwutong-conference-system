<template>
  <view class="groups-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">群组列表</text>
      </view>
    </view>

    <!-- 群组列表 -->
    <view class="group-list">
      <view v-if="groupList.length > 0">
        <view
          v-for="(group, index) in groupList"
          :key="index"
          class="group-item card"
          @click="enterGroup(group)"
        >
          <view class="group-top">
            <view class="group-info">
              <view class="group-name">{{ group.name }}</view>
              <view class="group-meta">
                <text class="meta-item">👥 {{ group.memberCount }}人</text>
                <text class="meta-item">💬 {{ group.messageCount }}条消息</text>
              </view>
            </view>
            <view
              v-if="group.unread > 0"
              class="group-badge"
            >
              {{ group.unread }}
            </view>
          </view>

          <view class="group-bottom">
            <view class="group-members">
              <view
                v-for="(member, idx) in group.members.slice(0, 5)"
                :key="idx"
                class="member-avatar"
              >
                {{ member.name.charAt(member.name.length - 1) }}
              </view>
              <view v-if="group.memberCount > 5" class="member-more">
                +{{ group.memberCount - 5 }}
              </view>
            </view>
            <view class="group-time">{{ group.lastTime }}</view>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">👥</text>
        <text class="empty-text">暂无群组</text>
        <button class="empty-btn btn btn-primary" @click="createGroup">
          创建群组
        </button>
      </view>
    </view>

    <!-- 创建群组按钮 -->
    <view class="fab-btn" @click="createGroup">
      <text class="fab-icon">➕</text>
    </view>
  </view>
</template>

<script>
import GroupAPI from '@/api/group'

export default {
  data() {
    return {
      meetingId: '',
      groupList: []
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
      this.loadGroups()
    }
  },

  methods: {
    /**
     * 加载群组列表
     */
    async loadGroups() {
      try {
        const data = await GroupAPI.listGroups({ conferenceId: this.meetingId })
        const groups = data || []
        this.groupList = groups.map(g => ({
          id: g.id,
          name: g.groupName,
          memberCount: g.memberCount || 0,
          messageCount: 0,
          unread: 0,
          lastTime: g.updateTime ? new Date(g.updateTime).toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'}) : '',
          members: []
        }))
      } catch (error) {
        console.error('Load groups error:', error)
      }
    },

    /**
     * 进入群组
     */
    enterGroup(group) {
      // 清除未读
      group.unread = 0

      uni.navigateTo({
        url: `/pages/learner/chat-room?id=${group.id}&name=${group.name}&type=group`
      })
    },

    /**
     * 创建群组
     */
    createGroup() {
      uni.showModal({
        title: '创建群组',
        editable: true,
        placeholderText: '请输入群组名称',
        success: async (res) => {
          if (res.confirm && res.content) {
            try {
              await groupApi.createGroup({
                groupName: res.content,
                description: '',
                type: 'attendee',
                conferenceId: uni.getStorageSync('conferenceId'),
                tenantId: uni.getStorageSync('tenantId')
              })
              uni.showToast({ title: '创建成功', icon: 'success' })
              this.loadGroups()
            } catch(e) {
              uni.showToast({ title: '创建失败', icon: 'none' })
            }
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.groups-container {
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

.group-list {
  padding: $spacing-md;
}

.group-item {
  margin-bottom: $spacing-md;
  transition: $transition-base;
}

.group-item:active {
  transform: scale(0.98);
}

.group-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;
}

.group-info {
  flex: 1;
}

.group-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.group-meta {
  display: flex;
  gap: $spacing-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.group-badge {
  min-width: 40rpx;
  height: 40rpx;
  padding: 0 10rpx;
  background: $danger-color;
  color: $text-white;
  font-size: $font-size-xs;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.group-members {
  display: flex;
  align-items: center;
  gap: -$spacing-sm;
}

.member-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid $bg-primary;
}

.member-more {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $bg-tertiary;
  color: $text-secondary;
  font-size: $font-size-xs;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid $bg-primary;
}

.group-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.fab-btn {
  position: fixed;
  bottom: calc(120rpx + constant(safe-area-inset-bottom));
  bottom: calc(120rpx + env(safe-area-inset-bottom));
  right: $spacing-lg;
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: $primary-gradient;
  box-shadow: $shadow-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: $z-index-fixed;
}

.fab-icon {
  font-size: 56rpx;
  color: $text-white;
}

.empty-btn {
  margin-top: $spacing-lg;
}
</style>
