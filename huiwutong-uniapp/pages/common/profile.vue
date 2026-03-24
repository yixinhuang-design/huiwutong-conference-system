<template>
  <view class="profile-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">个人中心</text>
      </view>
    </view>

    <!-- 用户信息卡片 -->
    <view class="user-card card">
      <view class="user-top">
        <view class="user-avatar">{{ avatarText }}</view>
        <view class="user-info">
          <view class="user-name">{{ userInfo.realName || '学员' }}</view>
          <view class="user-meta">
            <text>{{ userInfo.organization || '' }}</text>
            <text class="sep" v-if="userInfo.organization && userInfo.position">｜</text>
            <text>{{ userInfo.position || '' }}</text>
          </view>
        </view>
        <button class="edit-btn btn btn-outline btn-sm" @click="editProfile">
          编辑
        </button>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-card card">
      <view class="stats-grid grid-3">
        <view class="stat-item">
          <view class="stat-value">{{ stats.totalMeetings }}</view>
          <view class="stat-label">参加培训</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ stats.totalHours }}</view>
          <view class="stat-label">培训学时</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ stats.completedTasks }}</view>
          <view class="stat-label">完成任务</view>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="function-list card">
      <view
        v-for="(item, index) in functionList"
        :key="index"
        class="function-item"
        @click="handleFunction(item.action)"
      >
        <view class="function-icon"><text :class="item.icon"></text></view>
        <view class="function-info">
          <text class="function-title">{{ item.title }}</text>
          <text v-if="item.badge" class="function-badge">{{ item.badge }}</text>
        </view>
        <text class="function-arrow">›</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <button class="logout-btn btn btn-outline btn-block" @click="handleLogout">
      退出登录
    </button>
  </view>
</template>

<script>
import { useUserStore } from '@/store/modules/user'

export default {
  data() {
    return {
      userInfo: {
        realName: '张丽华',
        organization: '市委组织部',
        position: '副处长'
      },
      stats: {
        totalMeetings: 12,
        totalHours: 96,
        completedTasks: 45
      },
      functionList: [
        {
          icon: 'fa-user',
          title: '个人信息',
          action: 'profile'
        },
        {
          icon: 'fa-bell',
          title: '消息通知',
          action: 'notifications',
          badge: 3
        },
        {
          icon: 'fa-clipboard',
          title: '我的任务',
          action: 'tasks'
        },
        {
          icon: 'fa-folder',
          title: '我的档案',
          action: 'archives'
        },
        {
          icon: 'fa-edit',
          title: '我的反馈',
          action: 'feedback'
        },
        {
          icon: 'fa-star',
          title: '我的收藏',
          action: 'favorites'
        },
        {
          icon: 'fa-lock',
          title: '账号安全',
          action: 'security'
        },
        {
          icon: 'fa-cog',
          title: '系统设置',
          action: 'settings'
        },
        {
          icon: 'fa-question-circle',
          title: '帮助中心',
          action: 'help'
        }
      ]
    }
  },

  computed: {
    avatarText() {
      const name = this.userInfo.realName
      return name ? name.charAt(name.length - 1) : '学'
    }
  },

  onLoad() {
    this.loadUserInfo()
    this.loadStats()
  },

  methods: {
    /**
     * 加载用户信息
     */
    loadUserInfo() {
      const userStore = useUserStore()
      if (userStore.userInfo) {
        this.userInfo = {
          ...this.userInfo,
          ...userStore.userInfo
        }
      }
    },

    /**
     * 加载统计数据
     */
    async loadStats() {
      try {
        // TODO: 调用API获取统计数据
        // const res = await this.$api.user.getStats()
      } catch (error) {
        console.error('Load stats error:', error)
      }
    },

    /**
     * 编辑个人信息
     */
    editProfile() {
      uni.navigateTo({
        url: '/pages/common/profile-edit'
      })
    },

    /**
     * 处理功能点击
     */
    handleFunction(action) {
      const routeMap = {
        profile: '/pages/common/profile-edit',
        notifications: '/pages/learner/notifications',
        tasks: '/pages/learner/task-list',
        archives: '/pages/common/past',
        feedback: '/pages/learner/feedback',
        favorites: '/pages/common/favorites',
        security: '/pages/common/security',
        settings: '/pages/common/settings',
        help: '/pages/common/help'
      }

      if (routeMap[action]) {
        uni.navigateTo({
          url: routeMap[action]
        })
      } else {
        uni.showToast({
          title: '功能开发中',
          icon: 'none'
        })
      }
    },

    /**
     * 退出登录
     */
    handleLogout() {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            this.doLogout()
          }
        }
      })
    },

    /**
     * 执行退出登录
     */
    async doLogout() {
      try {
        const userStore = useUserStore()
        await userStore.logout()
      } catch (error) {
        console.error('Logout error:', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.profile-container {
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

.user-card,
.stats-card,
.function-list {
  margin: $spacing-md;
}

.user-top {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: $primary-gradient;
  color: $text-white;
  font-size: 48rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 6rpx;
}

.user-meta {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.sep {
  color: $border-color;
}

.edit-btn {
  flex-shrink: 0;
}

.stats-grid {
  padding: $spacing-lg 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 48rpx;
  font-weight: 600;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: $spacing-sm;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.function-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
  transition: $transition-base;
}

.function-item:last-child {
  border-bottom: none;
}

.function-item:active {
  background: $bg-tertiary;
  margin: 0 (-$spacing-md);
  padding-left: $spacing-md;
  padding-right: $spacing-md;
}

.function-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.function-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.function-title {
  font-size: $font-size-md;
  color: $text-primary;
}

.function-badge {
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
}

.function-arrow {
  font-size: 48rpx;
  color: $text-tertiary;
  font-weight: 300;
  flex-shrink: 0;
}

.logout-btn {
  margin: $spacing-md;
}
</style>
