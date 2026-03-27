<template>
  <view class="task-list-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">工作任务</text>
        <text class="header-action" @click="showFilter">筛选</text>
      </view>
    </view>

    <!-- 任务统计 -->
    <view class="task-stats">
      <view class="stat-item" :class="{ active: filterStatus === 'all' }" @click="filterByStatus('all')">
        <text class="stat-num">{{ taskStats.all }}</text>
        <text class="stat-label">全部</text>
      </view>
      <view class="stat-item" :class="{ active: filterStatus === 'pending' }" @click="filterByStatus('pending')">
        <text class="stat-num">{{ taskStats.pending }}</text>
        <text class="stat-label">待处理</text>
      </view>
      <view class="stat-item" :class="{ active: filterStatus === 'processing' }" @click="filterByStatus('processing')">
        <text class="stat-num">{{ taskStats.processing }}</text>
        <text class="stat-label">进行中</text>
      </view>
      <view class="stat-item" :class="{ active: filterStatus === 'completed' }" @click="filterByStatus('completed')">
        <text class="stat-num">{{ taskStats.completed }}</text>
        <text class="stat-label">已完成</text>
      </view>
    </view>

    <!-- 任务列表 -->
    <scroll-view class="task-scroll" scroll-y @scrolltolower="loadMore">
      <view v-if="filteredTasks.length === 0" class="empty-state">
        <text class="empty-icon"><text class="fa fa-clipboard"></text></text>
        <text class="empty-text">暂无任务</text>
      </view>

      <view v-for="task in filteredTasks" :key="task.id" class="task-card card" @click="viewTaskDetail(task)">
        <view class="task-header">
          <view class="task-title-row">
            <text class="task-title">{{ task.title }}</text>
            <view class="task-priority" :class="'priority-' + task.priority">
              {{ task.priority === 'high' ? '高' : task.priority === 'medium' ? '中' : '低' }}
            </view>
          </view>
          <view class="task-meta">
            <text class="task-time"><text class="fa fa-calendar-alt"></text> {{ task.deadline }}</text>
            <text class="task-assignee"><text class="fa fa-user"></text> {{ task.assignee }}</text>
          </view>
        </view>

        <view class="task-content">
          <text class="task-desc">{{ task.description }}</text>
        </view>

        <view class="task-footer">
          <view class="task-status" :class="'status-' + task.status">
            {{ statusMap[task.status] }}
          </view>
          <view class="task-progress" v-if="task.status === 'processing'">
            <text class="progress-label">进度：</text>
            <text class="progress-value">{{ task.progress }}%</text>
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: task.progress + '%' }"></view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && filteredTasks.length > 0" class="no-more-text">
        <text>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 筛选弹窗 -->
    <view v-if="showFilterPopup" class="filter-popup" @click="hideFilter">
      <view class="filter-content" @click.stop>
        <view class="filter-header">
          <text class="filter-title">筛选条件</text>
          <text class="filter-close" @click="hideFilter">✕</text>
        </view>

        <view class="filter-section">
          <text class="filter-label">优先级</text>
          <view class="filter-options">
            <view
              v-for="priority in ['all', 'high', 'medium', 'low']"
              :key="priority"
              class="filter-option"
              :class="{ active: filterPriority === priority }"
              @click="selectPriority(priority)"
            >
              {{ priority === 'all' ? '全部' : priority === 'high' ? '高' : priority === 'medium' ? '中' : '低' }}
            </view>
          </view>
        </view>

        <view class="filter-section">
          <text class="filter-label">任务类型</text>
          <view class="filter-options">
            <view
              v-for="type in ['all', 'registration', 'grouping', 'seat', 'notice', 'checkin']"
              :key="type"
              class="filter-option"
              :class="{ active: filterType === type }"
              @click="selectType(type)"
            >
              {{ typeMap[type] }}
            </view>
          </view>
        </view>

        <view class="filter-actions">
          <button class="btn btn-secondary" @click="resetFilter">重置</button>
          <button class="btn btn-primary" @click="applyFilter">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import TaskAPI from '@/api/task'

export default {
  data() {
    return {
      filterStatus: 'all',
      filterPriority: 'all',
      filterType: 'all',
      showFilterPopup: false,
      loading: false,
      hasMore: true,
      currentPage: 1,
      taskStats: {
        all: 0,
        pending: 0,
        processing: 0,
        completed: 0
      },
      taskList: [],
      statusMap: {
        pending: '待处理',
        processing: '进行中',
        completed: '已完成'
      },
      typeMap: {
        all: '全部',
        registration: '报名审核',
        grouping: '分组管理',
        seat: '座位分配',
        notice: '通知发送',
        checkin: '签到管理',
        other: '其他'
      }
    }
  },

  onLoad() {
    this.loadTasks()
  },

  computed: {
    filteredTasks() {
      return this.taskList.filter(task => {
        const statusMatch = this.filterStatus === 'all' || task.status === this.filterStatus
        const priorityMatch = this.filterPriority === 'all' || task.priority === this.filterPriority
        const typeMatch = this.filterType === 'all' || task.type === this.filterType
        return statusMatch && priorityMatch && typeMatch
      })
    }
  },

  methods: {
    filterByStatus(status) {
      this.filterStatus = status
    },

    showFilter() {
      this.showFilterPopup = true
    },

    hideFilter() {
      this.showFilterPopup = false
    },

    selectPriority(priority) {
      this.filterPriority = priority
    },

    selectType(type) {
      this.filterType = type
    },

    resetFilter() {
      this.filterPriority = 'all'
      this.filterType = 'all'
    },

    applyFilter() {
      this.hideFilter()
      this.loadTasks()
      uni.showToast({ title: '筛选已应用', icon: 'success' })
    },

    viewTaskDetail(task) {
      uni.navigateTo({
        url: `/pages/staff/task-detail?id=${task.id}`
      })
    },

    async loadTasks() {
      try {
        const data = await TaskAPI.listTasks()
        const tasks = data.records || data || []
        this.taskList = tasks.map(t => ({
          id: t.id, title: t.taskName, description: t.description || '',
          deadline: t.deadline || '', assignee: t.ownerName || '',
          priority: t.priority || 'medium', status: t.status || 'pending',
          type: t.taskType || 'other', progress: t.progress || 0
        }))
        this.taskStats.all = this.taskList.length
        this.taskStats.pending = this.taskList.filter(t => t.status === 'pending').length
        this.taskStats.processing = this.taskList.filter(t => t.status === 'in_progress').length
        this.taskStats.completed = this.taskList.filter(t => t.status === 'completed').length
      } catch(e) { console.error('Load tasks error:', e) }
    },

    async loadMore() {
      if (this.loading || !this.hasMore) return
      this.loading = true
      this.currentPage++
      try {
        const data = await TaskAPI.listTasks({ page: this.currentPage })
        const tasks = data.records || data || []
        if (tasks.length === 0) { this.hasMore = false }
        else {
          tasks.forEach(t => this.taskList.push({
            id: t.id, title: t.taskName, description: t.description || '',
            deadline: t.deadline || '', assignee: t.ownerName || '',
            priority: t.priority || 'medium', status: t.status || 'pending',
            type: t.taskType || 'other', progress: t.progress || 0
          }))
        }
      } catch(e) { console.error('Load more error:', e) }
      this.loading = false
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.task-list-container {
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

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.header-action {
  font-size: $font-size-md;
  color: $primary-color;
}

.task-stats {
  display: flex;
  justify-content: space-around;
  background: $bg-primary;
  padding: $spacing-md 0;
  margin-bottom: $spacing-sm;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  transition: all 0.3s;
}

.stat-item.active {
  background: rgba(102, 126, 234, 0.1);
}

.stat-num {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.task-scroll {
  height: calc(100vh - 240rpx);
  padding: 0 $spacing-md;
}

.task-card {
  margin-bottom: $spacing-md;
}

.task-header {
  margin-bottom: $spacing-md;
}

.task-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.task-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
}

.task-priority {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.task-priority.priority-high {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.task-priority.priority-medium {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.task-priority.priority-low {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.task-meta {
  display: flex;
  gap: $spacing-md;
}

.task-time,
.task-assignee {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.task-content {
  margin-bottom: $spacing-md;
}

.task-desc {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-status {
  padding: 8rpx 16rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  font-weight: 500;
}

.task-status.status-pending {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.task-status.status-processing {
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
}

.task-status.status-completed {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.task-progress {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.progress-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.progress-value {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $primary-color;
  min-width: 60rpx;
}

.progress-bar {
  width: 100rpx;
  height: 8rpx;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: $spacing-md;
}

.empty-text {
  font-size: $font-size-md;
  color: $text-tertiary;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-md;
  color: $text-tertiary;
  font-size: $font-size-sm;
}

.filter-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.filter-content {
  width: 100%;
  background: $bg-primary;
  border-radius: $border-radius-lg $border-radius-lg 0 0;
  padding: $spacing-lg;
  padding-bottom: calc(#{$spacing-lg} + constant(safe-area-inset-bottom));
  padding-bottom: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.filter-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.filter-close {
  font-size: $font-size-xl;
  color: $text-secondary;
  padding: $spacing-sm;
}

.filter-section {
  margin-bottom: $spacing-lg;
}

.filter-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.filter-option {
  padding: $spacing-sm $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  color: $text-secondary;
  transition: all 0.3s;
}

.filter-option.active {
  background: $primary-color;
  color: $text-white;
}

.filter-actions {
  display: flex;
  gap: $spacing-md;
  margin-top: $spacing-xl;
}

.filter-actions .btn {
  flex: 1;
}
</style>
