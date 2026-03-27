<template>
  <view class="registration-manage-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">报名管理</text>
        <text class="header-action" @click="exportData">导出</text>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-bar card">
      <view class="stat-item">
        <text class="stat-value">{{ stats.total }}</text>
        <text class="stat-label">总报名</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.approved }}</text>
        <text class="stat-label">已通过</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.pending }}</text>
        <text class="stat-label">待审核</text>
      </view>
    </view>

    <!-- 搜索和筛选 -->
    <view class="filter-bar card">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索姓名或单位"
          @input="handleSearch"
        />
      </view>
      <view class="filter-tabs">
        <view
          v-for="(tab, index) in tabs"
          :key="index"
          class="filter-tab"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          {{ tab.name }}
          <text v-if="tab.badge > 0" class="tab-badge">{{ tab.badge }}</text>
        </view>
      </view>
    </view>

    <!-- 报名列表 -->
    <view class="registration-list">
      <view v-if="registrationList.length > 0">
        <view
          v-for="(item, index) in registrationList"
          :key="index"
          class="registration-item card"
          @click="viewDetail(item)"
        >
          <view class="reg-top">
            <view class="reg-info">
              <text class="reg-name">{{ item.name }}</text>
              <text class="reg-org">{{ item.organization }}</text>
            </view>
            <view class="reg-status" :class="item.statusClass">
              {{ item.statusText }}
            </view>
          </view>

          <view class="reg-meta">
            <text class="meta-item">📱 {{ item.phone }}</text>
            <text class="meta-item">📧 {{ item.email }}</text>
          </view>

          <view class="reg-time">
            报名时间：{{ item.applyTime }}
          </view>

          <view v-if="item.status === 'pending'" class="reg-actions">
            <button
              class="action-btn approve-btn btn btn-primary btn-sm"
              @click.stop="approve(item)"
            >
              通过
            </button>
            <button
              class="action-btn reject-btn btn btn-outline btn-sm"
              @click.stop="reject(item)"
            >
              拒绝
            </button>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无报名信息</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      meetingId: '',
      searchKeyword: '',
      currentTab: 0,
      tabs: [
        { name: '全部', value: 'all', badge: 0 },
        { name: '待审核', value: 'pending', badge: 3 },
        { name: '已通过', value: 'approved', badge: 0 },
        { name: '已拒绝', value: 'rejected', badge: 0 }
      ],
      stats: {
        total: 120,
        approved: 115,
        pending: 5
      },
      registrationList: [
        {
          id: 1,
          name: '张三',
          organization: '市委组织部',
          phone: '138****1234',
          email: 'zhangsan@example.com',
          applyTime: '2025-01-14 10:30',
          status: 'pending',
          statusText: '待审核',
          statusClass: 'status-pending'
        },
        {
          id: 2,
          name: '李四',
          organization: '市委宣传部',
          phone: '139****5678',
          email: 'lisi@example.com',
          applyTime: '2025-01-14 11:20',
          status: 'approved',
          statusText: '已通过',
          statusClass: 'status-approved'
        },
        {
          id: 3,
          name: '王五',
          organization: '市教育局',
          phone: '137****9012',
          email: 'wangwu@example.com',
          applyTime: '2025-01-13 15:45',
          status: 'approved',
          statusText: '已通过',
          statusClass: 'status-approved'
        }
      ]
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
      this.loadRegistrations()
    }
  },

  methods: {
    /**
     * 加载报名列表
     */
    async loadRegistrations() {
      try {
        // TODO: 调用API获取报名列表
        // const res = await this.$api.registration.getList(this.meetingId)
      } catch (error) {
        console.error('Load registrations error:', error)
      }
    },

    /**
     * 搜索
     */
    handleSearch() {
      // 前端搜索
    },

    /**
     * 切换Tab
     */
    switchTab(index) {
      this.currentTab = index
    },

    /**
     * 查看详情
     */
    viewDetail(item) {
      uni.showModal({
        title: item.name,
        content: `单位：${item.organization}\n电话：${item.phone}\n邮箱：${item.email}\n报名时间：${item.applyTime}`,
        showCancel: true
      })
    },

    /**
     * 审核通过
     */
    approve(item) {
      uni.showModal({
        title: '确认通过',
        content: `确认通过${item.name}的报名申请吗？`,
        success: (res) => {
          if (res.confirm) {
            item.status = 'approved'
            item.statusText = '已通过'
            item.statusClass = 'status-approved'

            this.stats.pending--
            this.stats.approved++

            uni.showToast({
              title: '已通过',
              icon: 'success'
            })
          }
        }
      })
    },

    /**
     * 拒绝
     */
    reject(item) {
      uni.showModal({
        title: '确认拒绝',
        content: `确认拒绝${item.name}的报名申请吗？`,
        success: (res) => {
          if (res.confirm) {
            item.status = 'rejected'
            item.statusText = '已拒绝'
            item.statusClass = 'status-rejected'

            this.stats.pending--

            uni.showToast({
              title: '已拒绝',
              icon: 'success'
            })
          }
        }
      })
    },

    /**
     * 导出数据
     */
    exportData() {
      uni.showToast({
        title: '导出功能开发中',
        icon: 'none'
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

.registration-manage-container {
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
  font-size: $font-size-md;
  color: $primary-color;
}

.stats-bar {
  margin: $spacing-md;
  padding: $spacing-lg 0;
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
  flex: 1;
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

.stat-divider {
  width: 1rpx;
  background: $border-color;
}

.filter-bar {
  margin: $spacing-md;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.search-icon {
  font-size: $font-size-lg;
}

.search-input {
  flex: 1;
  font-size: $font-size-md;
}

.filter-tabs {
  display: flex;
  gap: $spacing-sm;
}

.filter-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: $spacing-sm;
  border-radius: $border-radius-lg;
  font-size: $font-size-sm;
  color: $text-secondary;
  background: $bg-tertiary;
}

.filter-tab.active {
  background: $primary-color;
  color: $text-white;
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

.registration-list {
  padding: 0 $spacing-md;
}

.registration-item {
  margin-bottom: $spacing-md;
}

.reg-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-sm;
}

.reg-info {
  flex: 1;
}

.reg-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-right: $spacing-sm;
}

.reg-org {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.reg-status {
  padding: 6rpx $spacing-md;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  font-weight: 500;
}

.status-pending {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
}

.status-approved {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: $success-color;
}

.status-rejected {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: $danger-color;
}

.reg-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  margin-bottom: $spacing-sm;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.reg-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
  margin-bottom: $spacing-md;
}

.reg-actions {
  display: flex;
  gap: $spacing-sm;
}

.action-btn {
  flex: 1;
}

.reject-btn {
  border-color: $border-color;
  color: $text-secondary;
}
</style>
