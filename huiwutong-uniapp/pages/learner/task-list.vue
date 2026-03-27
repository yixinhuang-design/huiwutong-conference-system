<template>
  <view class="task-list-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">我的任务</text>
      </view>
    </view>

    <!-- 任务统计 -->
    <view class="stats-card card">
      <view class="stats-grid grid-3">
        <view class="stat-item">
          <view class="stat-value">{{ taskStats.total }}</view>
          <view class="stat-label">全部任务</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ taskStats.completed }}</view>
          <view class="stat-label">已完成</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ taskStats.pending }}</view>
          <view class="stat-label">待完成</view>
        </view>
      </view>
    </view>

    <!-- 筛选Tab -->
    <view class="filter-bar">
      <scroll-view class="filter-scroll" scroll-x>
        <view
          v-for="(filter, index) in filters"
          :key="index"
          class="filter-item"
          :class="{ active: selectedFilter === index }"
          @click="selectFilter(index)"
        >
          {{ filter.name }}
        </view>
      </scroll-view>
    </view>

    <!-- 任务列表 -->
    <view class="task-list">
      <view v-if="taskList.length > 0">
        <view
          v-for="(task, index) in taskList"
          :key="index"
          class="task-item card"
          :class="{ completed: task.completed }"
          @click="goToDetail(task)"
        >
          <view class="task-header">
            <view class="task-title">{{ task.title }}</view>
            <view class="task-status" :class="task.statusClass">
              {{ task.statusText }}
            </view>
          </view>

          <view class="task-meta">
            <text class="meta-item">📅 {{ task.deadline }}</text>
            <text class="meta-item">⏰ {{ task.duration }}</text>
          </view>

          <view class="task-desc">
            {{ task.description }}
          </view>

          <view class="task-footer">
            <view class="task-priority" :class="task.priorityClass">
              {{ task.priorityText }}
            </view>
            <button
              v-if="!task.completed"
              class="task-btn btn btn-primary btn-sm"
              @click.stop="completeTask(task)"
            >
              完成
            </button>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无任务</text>
      </view>
    </view>
  </view>
</template>

<script>
import TaskAPI from '@/api/task'

export default {
  data() {
    return {
      taskStats: {
        total: 0,
        completed: 0,
        pending: 0
      },
      selectedFilter: 0,
      filters: [
        { name: '全部', value: 'all' },
        { name: '待完成', value: 'pending' },
        { name: '已完成', value: 'completed' },
        { name: '已逾期', value: 'overdue' }
      ],
      taskList: []
    }
  },

  onLoad() {
    this.loadTasks()
  },

  methods: {
    selectFilter(index) {
      this.selectedFilter = index
      this.loadTasks()
    },

    filterTasks() {
      this.loadTasks()
    },

    async loadTasks() {
      try {
        const filter = this.filters[this.selectedFilter]
        const data = await TaskAPI.myTasks(filter.value !== 'all' ? { status: filter.value } : {})
        const tasks = data.records || data || []
        this.taskList = tasks.map(t => ({
          id: t.id,
          title: t.taskName,
          description: t.description || '',
          deadline: t.deadline || '',
          duration: '',
          status: t.status || 'pending',
          statusText: t.status === 'completed' ? '已完成' : t.status === 'overdue' ? '已逾期' : '待完成',
          statusClass: 'status-' + (t.status || 'pending'),
          priority: t.priority || 'medium',
          priorityText: t.priority === 'high' ? '高优先级' : t.priority === 'low' ? '低优先级' : '中优先级',
          priorityClass: 'priority-' + (t.priority || 'medium'),
          completed: t.status === 'completed'
        }))
        // 更新统计
        this.taskStats.total = this.taskList.length
        this.taskStats.completed = this.taskList.filter(t => t.completed).length
        this.taskStats.pending = this.taskStats.total - this.taskStats.completed
      } catch (error) {
        console.error('Load tasks error:', error)
      }
    },

    /**
     * 查看详情
     */
    goToDetail(task) {
      uni.navigateTo({
        url: `/pages/learner/task-detail?id=${task.id}`
      })
    },

    /**
     * 完成任务
     */
    completeTask(task) {
      uni.showModal({
        title: '确认完成',
        content: `确认完成"${task.title}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await TaskAPI.completeTask(task.id)
              task.completed = true
              task.status = 'completed'
              task.statusText = '已完成'
              task.statusClass = 'status-completed'

              this.taskStats.completed++
              this.taskStats.pending--

              uni.showToast({
                title: '任务已完成',
                icon: 'success'
              })
            } catch(e) {
              uni.showToast({ title: '操作失败', icon: 'none' })
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

.task-list-container {
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

.filter-bar {
  padding: 0 $spacing-md;
  margin-bottom: $spacing-md;
}

.filter-scroll {
  white-space: nowrap;
}

.filter-item {
  display: inline-block;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  border-radius: $border-radius-lg;
  font-size: $font-size-sm;
  color: $text-secondary;
  background: $bg-primary;
  transition: $transition-base;
}

.filter-item.active {
  background: $primary-color;
  color: $text-white;
}

.task-list {
  padding: 0 $spacing-md;
}

.task-item {
  margin-bottom: $spacing-md;
  transition: $transition-base;
}

.task-item.completed {
  opacity: 0.6;
}

.task-item:active {
  transform: scale(0.98);
}

.task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-sm;
}

.task-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
}

.task-status {
  padding: 6rpx $spacing-sm;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.status-pending {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
}

.status-completed {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: $success-color;
}

.task-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  margin-bottom: $spacing-sm;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.task-desc {
  font-size: $font-size-sm;
  color: $text-primary;
  line-height: 1.6;
  margin-bottom: $spacing-md;
}

.task-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.task-priority {
  padding: 6rpx $spacing-sm;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.priority-high {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: $danger-color;
}

.priority-medium {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #f59e0b;
}

.priority-low {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: $accent-color;
}

.task-btn {
  padding: $spacing-sm $spacing-lg;
}
</style>
