<template>
  <view class="training-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">培训管理</text>
        <text class="header-action" @click="createTraining">+ 新建</text>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-tabs">
        <view
          v-for="tab in filterTabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: activeFilter === tab.value }"
          @click="switchFilter(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>
      <view class="filter-search" @click="showSearch">
        <text class="search-icon">🔍</text>
      </view>
    </view>

    <scroll-view class="training-scroll" scroll-y @scrolltolower="loadMore">
      <!-- 培训列表 -->
      <view class="training-list">
        <view
          v-for="training in filteredTrainings"
          :key="training.id"
          class="training-card card"
          @click="viewTraining(training)"
        >
          <view class="training-header">
            <view class="training-title-row">
              <text class="training-title">{{ training.title }}</text>
              <view class="training-status" :class="'status-' + training.status">
                {{ getStatusLabel(training.status) }}
              </view>
            </view>
            <text class="training-period">{{ training.period }}</text>
          </view>

          <view class="training-info">
            <view class="info-item">
              <text class="info-icon">📅</text>
              <text class="info-text">{{ training.date }}</text>
            </view>
            <view class="info-item">
              <text class="info-icon">📍</text>
              <text class="info-text">{{ training.location }}</text>
            </view>
            <view class="info-item">
              <text class="info-icon">👥</text>
              <text class="info-text">{{ training.participants }}</text>
            </view>
          </view>

          <view class="training-progress" v-if="training.status === 'ongoing'">
            <view class="progress-header">
              <text class="progress-label">培训进度</text>
              <text class="progress-value">{{ training.progress }}%</text>
            </view>
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: training.progress + '%' }"></view>
            </view>
          </view>

          <view class="training-actions" @click.stop>
            <button
              class="btn btn-sm btn-outline"
              @click="editTraining(training)"
            >
              编辑
            </button>
            <button
              class="btn btn-sm btn-primary"
              @click="manageTraining(training)"
            >
              管理
            </button>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && filteredTrainings.length > 0" class="no-more-text">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      activeFilter: 'all',
      loading: false,
      hasMore: true,
      currentPage: 1,
      filterTabs: [
        { label: '全部', value: 'all' },
        { label: '进行中', value: 'ongoing' },
        { label: '已完成', value: 'completed' },
        { label: '未开始', value: 'upcoming' }
      ],
      trainingList: [
        {
          id: 1,
          title: '2025年度第一期干部综合素质提升培训',
          period: '第5期',
          status: 'ongoing',
          date: '2025-01-18 至 2025-01-20',
          location: '市委党校报告厅',
          participants: '80人参训',
          progress: 60
        },
        {
          id: 2,
          title: '2025年度第二期干部综合素质提升培训',
          period: '第6期',
          status: 'upcoming',
          date: '2025-02-15 至 2025-02-17',
          location: '市委党校报告厅',
          participants: '100人参训',
          progress: 0
        },
        {
          id: 3,
          title: '2024年度第四期干部综合素质提升培训',
          period: '第4期',
          status: 'completed',
          date: '2024-12-10 至 2024-12-12',
          location: '市委党校报告厅',
          participants: '75人参训',
          progress: 100
        },
        {
          id: 4,
          title: '2024年度第三期干部综合素质提升培训',
          period: '第3期',
          status: 'completed',
          date: '2024-11-08 至 2024-11-10',
          location: '市委党校报告厅',
          participants: '82人参训',
          progress: 100
        }
      ]
    }
  },

  computed: {
    filteredTrainings() {
      if (this.activeFilter === 'all') {
        return this.trainingList
      }
      return this.trainingList.filter(t => t.status === this.activeFilter)
    }
  },

  methods: {
    switchFilter(filter) {
      this.activeFilter = filter
    },

    getStatusLabel(status) {
      const labelMap = {
        ongoing: '进行中',
        completed: '已完成',
        upcoming: '未开始'
      }
      return labelMap[status] || '未知'
    },

    viewTraining(training) {
      uni.navigateTo({
        url: `/pages/staff/training-detail?id=${training.id}`
      })
    },

    createTraining() {
      uni.navigateTo({
        url: '/pages/staff/training-create'
      })
    },

    editTraining(training) {
      uni.navigateTo({
        url: `/pages/staff/training-create?id=${training.id}`
      })
    },

    manageTraining(training) {
      uni.showActionSheet({
        itemList: ['报名管理', '分组管理', '座位管理', '通知发布'],
        success: (res) => {
          const pages = [
            '/pages/staff/registration-manage',
            '/pages/staff/grouping-manage',
            '/pages/staff/seat-manage',
            '/pages/staff/notice-manage'
          ]

          if (res.tapIndex >= 0 && res.tapIndex < pages.length) {
            uni.navigateTo({
              url: pages[res.tapIndex]
            })
          }
        }
      })
    },

    showSearch() {
      uni.showToast({
        title: '搜索功能开发中',
        icon: 'none'
      })
    },

    loadMore() {
      if (this.loading || !this.hasMore) return

      this.loading = true
      setTimeout(() => {
        this.currentPage++
        this.loading = false

        if (this.currentPage >= 3) {
          this.hasMore = false
        }
      }, 1000)
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

.training-container {
  min-height: 100vh;
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
  flex: 1;
  text-align: center;
}

.header-action {
  font-size: $font-size-md;
  color: $primary-color;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
}

.filter-tabs {
  display: flex;
  gap: $spacing-sm;
}

.filter-tab {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
  color: $text-secondary;
  border-radius: $border-radius-sm;
  transition: all 0.3s;
}

.filter-tab.active {
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
  font-weight: 500;
}

.filter-search {
  padding: $spacing-xs;
}

.search-icon {
  font-size: $font-size-lg;
}

.training-scroll {
  height: calc(100vh - 180rpx);
  padding: $spacing-md;
}

.training-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.training-card {
  margin-bottom: 0;
}

.training-header {
  margin-bottom: $spacing-md;
}

.training-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-xs;
}

.training-title {
  flex: 1;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.4;
  margin-right: $spacing-sm;
}

.training-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
  white-space: nowrap;
}

.training-status.status-ongoing {
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
}

.training-status.status-completed {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.training-status.status-upcoming {
  background: rgba(148, 163, 184, 0.1);
  color: #94a3b8;
}

.training-period {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.training-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.info-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.info-icon {
  font-size: $font-size-md;
}

.info-text {
  font-size: $font-size-md;
  color: $text-secondary;
}

.training-progress {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.progress-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.progress-value {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $primary-color;
}

.training-progress .progress-bar {
  height: 8rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 4rpx;
  overflow: hidden;
}

.training-progress .progress-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.training-actions {
  display: flex;
  gap: $spacing-sm;
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-lg;
  color: $text-tertiary;
  font-size: $font-size-sm;
}
</style>
