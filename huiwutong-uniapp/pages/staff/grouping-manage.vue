<template>
  <view class="grouping-manage-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">分组管理</text>
        <text class="header-action" @click="createGroup">➕ 新建</text>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-card card">
      <view class="stats-grid grid-2">
        <view class="stat-item">
          <text class="stat-value">{{ groups.length }}</text>
          <text class="stat-label">分组数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ totalMembers }}</text>
          <text class="stat-label">总人数</text>
        </view>
      </view>
    </view>

    <!-- 分组列表 -->
    <view class="group-list">
      <view v-if="groups.length > 0">
        <view
          v-for="(group, index) in groups"
          :key="index"
          class="group-item card"
          @click="viewGroup(group)"
        >
          <view class="group-header">
            <view class="group-info">
              <text class="group-name">{{ group.name }}</text>
              <text class="group-meta"><text class="fa fa-users"></text> {{ group.memberCount }}人</text>
            </view>
            <text class="group-arrow">›</text>
          </view>

          <view class="group-members">
            <view
              v-for="(member, idx) in group.members.slice(0, 6)"
              :key="idx"
              class="member-avatar"
            >
              {{ member.name.charAt(member.name.length - 1) }}
            </view>
            <view v-if="group.memberCount > 6" class="member-more">
              +{{ group.memberCount - 6 }}
            </view>
          </view>

          <view class="group-actions">
            <button
              class="action-btn btn btn-outline btn-sm"
              @click.stop="editGroup(group)"
            >
              编辑
            </button>
            <button
              class="action-btn btn btn-outline btn-sm"
              @click.stop="deleteGroup(group)"
            >
              删除
            </button>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon"><text class="fa fa-users"></text></text>
        <text class="empty-text">暂无分组</text>
        <button class="empty-btn btn btn-primary" @click="createGroup">
          创建第一个分组
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import GroupAPI from '@/api/group'

export default {
  data() {
    return {
      meetingId: '',
      groups: []
    }
  },

  computed: {
    totalMembers() {
      return this.groups.reduce((sum, group) => sum + group.memberCount, 0)
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
     * 加载分组列表
     */
    async loadGroups() {
      try {
        const data = await GroupAPI.listGroups({ conferenceId: this.meetingId })
        this.groups = (data || []).map(g => ({
          id: g.id, name: g.groupName, memberCount: g.memberCount || 0,
          members: []
        }))
      } catch (error) {
        console.error('Load groups error:', error)
      }
    },

    /**
     * 查看分组
     */
    viewGroup(group) {
      uni.navigateTo({
        url: `/pages/staff/group-detail?id=${group.id}&name=${group.name}`
      })
    },

    /**
     * 创建分组
     */
    createGroup() {
      uni.showToast({
        title: '创建分组功能开发中',
        icon: 'none'
      })
    },

    /**
     * 编辑分组
     */
    editGroup(group) {
      uni.navigateTo({
        url: `/pages/staff/group-edit?id=${group.id}`
      })
    },

    /**
     * 删除分组
     */
    deleteGroup(group) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除"${group.name}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await groupApi.deleteGroup(group.id)
              const index = this.groups.findIndex(g => g.id === group.id)
              if (index > -1) {
                this.groups.splice(index, 1)
              }
              uni.showToast({ title: '删除成功', icon: 'success' })
            } catch(e) { uni.showToast({ title: '删除失败: ' + (e.message || ''), icon: 'none' }) }
          }
        }
      })
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

.grouping-manage-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
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
  justify-content: space-between;
  height: 88rpx;
  padding: 0 $spacing-md;
}

.back-btn {
  font-size: 48rpx;
  color: $text-primary;
  font-weight: 300;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.header-action {
  font-size: $font-size-lg;
  color: $primary-color;
}

.stats-card {
  margin: $spacing-md;
}

.stats-grid {
  padding: $spacing-lg 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6rpx;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.group-list {
  padding: 0 $spacing-md;
}

.group-item {
  margin-bottom: $spacing-md;
}

.group-header {
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
  margin-right: $spacing-sm;
}

.group-meta {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.group-arrow {
  font-size: 48rpx;
  color: $text-tertiary;
  font-weight: 300;
}

.group-members {
  display: flex;
  align-items: center;
  gap: -$spacing-sm;
  margin-bottom: $spacing-md;
}

.member-avatar {
  width: 64rpx;
  height: 64rpx;
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
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $bg-tertiary;
  color: $text-secondary;
  font-size: $font-size-xs;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid $bg-primary;
}

.group-actions {
  display: flex;
  gap: $spacing-sm;
}

.action-btn {
  flex: 1;
}
</style>
