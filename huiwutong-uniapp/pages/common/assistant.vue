<template>
  <view class="assistant-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">AI助理</text>
      </view>
    </view>

    <!-- 会议概览 -->
    <view class="overview-card card">
      <view class="section-title">会议概览</view>
      <view class="overview-stats grid-3">
        <view class="stat-item">
          <view class="stat-value">{{ overview.totalDays }}</view>
          <view class="stat-label">培训天数</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ overview.completedDays }}</view>
          <view class="stat-label">已完成</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ overview.remainingDays }}</view>
          <view class="stat-label">剩余天数</view>
        </view>
      </view>
    </view>

    <!-- 今日日程 -->
    <view class="today-schedule card">
      <view class="section-title">今日日程</view>
      <view v-if="todaySchedule.length > 0">
        <view
          v-for="(item, index) in todaySchedule"
          :key="index"
          class="schedule-item"
        >
          <view class="time-badge">
            <text class="time-text">{{ item.time }}</text>
          </view>
          <view class="schedule-info">
            <view class="schedule-title">{{ item.title }}</view>
            <view class="schedule-location">
              <text>📍</text> {{ item.location }}
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">今日暂无日程安排</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions card">
      <view class="section-title">快捷操作</view>
      <view class="action-list">
        <view
          v-for="(action, index) in quickActions"
          :key="index"
          class="action-item"
          @click="handleAction(action.action)"
        >
          <view class="action-icon" :style="{ background: action.gradient }">
            <text>{{ action.icon }}</text>
          </view>
          <view class="action-info">
            <view class="action-title">{{ action.title }}</view>
            <view class="action-desc">{{ action.desc }}</view>
          </view>
          <text class="action-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 智能问答 -->
    <view class="ai-qa card">
      <view class="section-title">智能问答</view>
      <view class="qa-input-wrapper">
        <input
          v-model="qaInput"
          class="qa-input"
          placeholder="输入您的问题..."
          @confirm="handleQuestion"
        />
        <button class="qa-btn btn btn-primary" @click="handleQuestion">
          发送
        </button>
      </view>

      <view v-if="qaHistory.length > 0" class="qa-history">
        <view
          v-for="(item, index) in qaHistory"
          :key="index"
          class="qa-item"
          :class="item.role"
        >
          <view class="qa-content">{{ item.content }}</view>
        </view>
      </view>
    </view>

    <!-- 待办事项 -->
    <view class="todo-list card">
      <view class="section-title">待办事项</view>
      <view v-if="todoList.length > 0">
        <view
          v-for="(todo, index) in todoList"
          :key="index"
          class="todo-item"
          @click="handleTodo(todo)"
        >
          <view class="todo-check" :class="{ checked: todo.completed }">
            <text v-if="todo.completed">✓</text>
          </view>
          <view class="todo-content" :class="{ completed: todo.completed }">
            <view class="todo-title">{{ todo.title }}</view>
            <view class="todo-time">{{ todo.time }}</view>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">暂无待办事项</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      overview: {
        totalDays: 5,
        completedDays: 2,
        remainingDays: 3
      },
      todaySchedule: [
        {
          time: '09:00',
          title: '专题讲座：新时代党的建设',
          location: '报告厅'
        },
        {
          time: '14:00',
          title: '分组讨论',
          location: 'A栋302室'
        },
        {
          time: '18:00',
          title: '晚餐',
          location: '餐厅'
        }
      ],
      quickActions: [
        {
          icon: '📅',
          title: '查看日程',
          desc: '查看完整的培训日程',
          action: 'schedule',
          gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        },
        {
          icon: '🪑',
          title: '我的座位',
          desc: '查看座位信息和导航',
          action: 'seat',
          gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
        },
        {
          icon: '📚',
          title: '学习资料',
          desc: '下载培训相关资料',
          action: 'materials',
          gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
        },
        {
          icon: '📢',
          title: '通知消息',
          desc: '查看最新通知',
          action: 'notifications',
          gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
        },
        {
          icon: '📒',
          title: '通讯录',
          desc: '查看学员联系方式',
          action: 'contact',
          gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
        },
        {
          icon: '💬',
          title: '群组聊天',
          desc: '进入群组聊天',
          action: 'chat',
          gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)'
        }
      ],
      qaInput: '',
      qaHistory: [
        {
          role: 'assistant',
          content: '您好！我是智能助理，有什么可以帮助您的吗？'
        }
      ],
      todoList: [
        {
          id: 1,
          title: '完成下午课程签到',
          time: '14:00 前',
          completed: false
        },
        {
          id: 2,
          title: '提交课程反馈',
          time: '今天 18:00 前',
          completed: false
        },
        {
          id: 3,
          title: '查看明日课程安排',
          time: '今天 22:00 前',
          completed: false
        }
      ]
    }
  },

  methods: {
    /**
     * 处理快捷操作
     */
    handleAction(action) {
      const routeMap = {
        schedule: '/pages/learner/schedule',
        seat: '/pages/learner/seat',
        materials: '/pages/learner/materials',
        notifications: '/pages/learner/notifications',
        contact: '/pages/learner/contact',
        chat: '/pages/learner/groups'
      }

      if (routeMap[action]) {
        uni.navigateTo({
          url: routeMap[action]
        })
      }
    },

    /**
     * 处理问题 - 调用后端AI服务
     */
    async handleQuestion() {
      if (!this.qaInput.trim()) {
        uni.showToast({ title: '请输入问题', icon: 'none' })
        return
      }

      const question = this.qaInput.trim()
      this.qaHistory.push({ role: 'user', content: question })
      this.qaInput = ''

      // 调用后端AI聊天接口
      try {
        const [err, res] = await uni.request({
          url: 'http://localhost:8085/api/ai/chat',
          method: 'POST',
          header: { 'Content-Type': 'application/json', 'X-Tenant-Id': '2027317834622709762' },
          data: { message: question }
        })
        if (res && res.data && res.data.code === 200 && res.data.data) {
          const aiMsg = res.data.data.aiMessage
          this.qaHistory.push({ role: 'assistant', content: aiMsg ? aiMsg.content : '已收到您的问题，正在处理中...' })
        } else {
          this.qaHistory.push({ role: 'assistant', content: this.getLocalAnswer(question) })
        }
      } catch (e) {
        // API失败时降级为本地回答
        this.qaHistory.push({ role: 'assistant', content: this.getLocalAnswer(question) })
      }
    },

    /**
     * 本地兜底回答（API不可用时使用）
     */
    getLocalAnswer(question) {
      if (question.includes('日程') || question.includes('时间')) return '今天的日程安排是：09:00 专题讲座，14:00 分组讨论，18:00 晚餐。您可以在日程页面查看完整安排。'
      if (question.includes('座位') || question.includes('位置')) return '您可以在"我的座位"页面查看详细座位信息和导航。'
      if (question.includes('资料') || question.includes('课件')) return '培训资料已上传到学习资料模块，您可以在快捷操作中点击"学习资料"进行下载。'
      if (question.includes('签到')) return '签到时间为课程开始前30分钟至结束后10分钟。您可以使用扫码签到或定位签到功能。'
      if (question.includes('联系') || question.includes('电话')) return '请联系会务组获取帮助，可在通知消息中查看联系方式。'
      return '感谢您的问题，AI助手暂时离线。请稍后重试或联系工作人员获取帮助。'
    },

    /**
     * 处理待办事项
     */
    handleTodo(todo) {
      todo.completed = !todo.completed

      if (todo.completed) {
        uni.showToast({
          title: '已完成',
          icon: 'success'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.assistant-container {
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

.overview-card,
.today-schedule,
.quick-actions,
.ai-qa,
.todo-list {
  margin: $spacing-md;
}

.overview-stats {
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

.schedule-item {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.schedule-item:last-child {
  border-bottom: none;
}

.time-badge {
  min-width: 120rpx;
  padding: $spacing-sm $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-sm;
  text-align: center;
}

.time-text {
  font-size: $font-size-md;
  font-weight: 600;
  color: $primary-color;
}

.schedule-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.schedule-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.schedule-location {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.action-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  transition: $transition-base;
}

.action-item:active {
  transform: scale(0.98);
  background: $border-color;
}

.action-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: $border-radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.action-info {
  flex: 1;
}

.action-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 6rpx;
}

.action-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.action-arrow {
  font-size: 48rpx;
  color: $text-tertiary;
  font-weight: 300;
}

.qa-input-wrapper {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.qa-input {
  flex: 1;
  padding: $spacing-md;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
}

.qa-btn {
  padding: $spacing-md $spacing-lg;
  flex-shrink: 0;
}

.qa-history {
  max-height: 600rpx;
  overflow-y: auto;
}

.qa-item {
  margin-bottom: $spacing-md;
  display: flex;
}

.qa-item.user {
  justify-content: flex-end;
}

.qa-item.assistant {
  justify-content: flex-start;
}

.qa-content {
  max-width: 70%;
  padding: $spacing-md;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  line-height: 1.6;
}

.qa-item.user .qa-content {
  background: $primary-color;
  color: $text-white;
}

.qa-item.assistant .qa-content {
  background: $bg-tertiary;
  color: $text-primary;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-check {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid $border-color;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  color: $text-white;
  flex-shrink: 0;
  transition: $transition-base;
}

.todo-check.checked {
  background: $success-color;
  border-color: $success-color;
}

.todo-content {
  flex: 1;
}

.todo-content.completed {
  opacity: 0.5;
}

.todo-content.completed .todo-title {
  text-decoration: line-through;
}

.todo-title {
  font-size: $font-size-md;
  color: $text-primary;
  margin-bottom: 6rpx;
}

.todo-time {
  font-size: $font-size-sm;
  color: $text-secondary;
}
</style>
