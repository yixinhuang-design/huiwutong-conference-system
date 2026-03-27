<template>
  <view class="logs-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">操作日志</text>
        <text class="header-action" @click="exportLogs">导出</text>
      </view>
    </view>

    <!-- 筛选条件 -->
    <view class="filter-section card">
      <view class="filter-row">
        <view class="filter-item">
          <text class="filter-label">时间范围</text>
          <picker mode="date" :value="filter.startDate" @change="onStartDateChange">
            <view class="picker-input-small">
              {{ filter.startDate || '开始日期' }}
            </view>
          </picker>
          <text class="date-separator">至</text>
          <picker mode="date" :value="filter.endDate" @change="onEndDateChange">
            <view class="picker-input-small">
              {{ filter.endDate || '结束日期' }}
            </view>
          </picker>
        </view>
      </view>

      <view class="filter-row">
        <view class="filter-item">
          <text class="filter-label">操作类型</text>
          <picker mode="selector" :range="operationTypes" @change="onOperationTypeChange">
            <view class="picker-input-small">
              {{ filter.operationType || '全部' }}
            </view>
          </picker>
        </view>

        <view class="filter-item">
          <text class="filter-label">操作模块</text>
          <picker mode="selector" :range="modules" @change="onModuleChange">
            <view class="picker-input-small">
              {{ filter.module || '全部' }}
            </view>
          </picker>
        </view>
      </view>

      <view class="filter-actions">
        <button class="btn btn-sm btn-outline" @click="resetFilter">重置</button>
        <button class="btn btn-sm btn-primary" @click="applyFilter">查询</button>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-section card">
      <view class="stat-item">
        <text class="stat-value">{{ stats.total }}</text>
        <text class="stat-label">总记录数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.today }}</text>
        <text class="stat-label">今日操作</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.users }}</text>
        <text class="stat-label">活跃用户</text>
      </view>
    </view>

    <!-- 日志列表 -->
    <scroll-view class="logs-scroll" scroll-y>
      <view class="logs-list">
        <view
          v-for="log in filteredLogs"
          :key="log.id"
          class="log-item card"
          @click="viewLogDetail(log)"
        >
          <view class="log-header">
            <view class="log-module">
              <text class="module-icon">{{ log.moduleIcon }}</text>
              <text class="module-name">{{ log.module }}</text>
            </view>
            <view class="log-type">
              <text class="type-badge" :class="log.typeClass">
                {{ log.type }}
              </text>
            </view>
          </view>

          <view class="log-content">
            <text class="log-action">{{ log.action }}</text>
            <text class="log-detail">{{ log.detail }}</text>
          </view>

          <view class="log-meta">
            <view class="meta-item">
              <text class="meta-icon"><text class="fa fa-user"></text></text>
              <text class="meta-text">{{ log.operator }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon">🖥️</text>
              <text class="meta-text">{{ log.ip }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-icon"><text class="fa fa-clock"></text></text>
              <text class="meta-text">{{ log.time }}</text>
            </view>
          </view>

          <view class="log-result" v-if="log.result">
            <text class="result-label">结果：</text>
            <text class="result-value" :class="log.resultClass">
              {{ log.result }}
            </text>
          </view>
        </view>
      </view>

      <view v-if="hasMore" class="load-more-btn" @click="loadMore">
        <text>加载更多</text>
      </view>
    </scroll-view>

    <!-- 日志详情弹窗 -->
    <view v-if="showDetailModal" class="modal-overlay" @click="hideDetailModal">
      <view class="detail-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">日志详情</text>
          <text class="modal-close" @click="hideDetailModal">✕</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="detail-section">
            <text class="detail-section-title">基本信息</text>
            <view class="detail-row">
              <text class="detail-label">操作模块：</text>
              <text class="detail-value">{{ currentLog?.module }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">操作类型：</text>
              <text class="detail-value">{{ currentLog?.type }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">操作动作：</text>
              <text class="detail-value">{{ currentLog?.action }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">操作详情：</text>
              <text class="detail-value">{{ currentLog?.detail }}</text>
            </view>
          </view>

          <view class="detail-section">
            <text class="detail-section-title">操作人信息</text>
            <view class="detail-row">
              <text class="detail-label">操作人：</text>
              <text class="detail-value">{{ currentLog?.operator }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">用户名：</text>
              <text class="detail-value">{{ currentLog?.username }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">所属租户：</text>
              <text class="detail-value">{{ currentLog?.tenant }}</text>
            </view>
          </view>

          <view class="detail-section">
            <text class="detail-section-title">请求信息</text>
            <view class="detail-row">
              <text class="detail-label">请求时间：</text>
              <text class="detail-value">{{ currentLog?.time }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">IP地址：</text>
              <text class="detail-value">{{ currentLog?.ip }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">User-Agent：</text>
              <text class="detail-value">{{ currentLog?.userAgent }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">请求方法：</text>
              <text class="detail-value">{{ currentLog?.method }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">请求URL：</text>
              <text class="detail-value">{{ currentLog?.url }}</text>
            </view>
          </view>

          <view class="detail-section">
            <text class="detail-section-title">响应信息</text>
            <view class="detail-row">
              <text class="detail-label">响应状态：</text>
              <text class="detail-value" :class="currentLog?.resultClass">
                {{ currentLog?.result }}
              </text>
            </view>
            <view class="detail-row">
              <text class="detail-label">响应时间：</text>
              <text class="detail-value">{{ currentLog?.responseTime }}ms</text>
            </view>
          </view>

          <view class="detail-section" v-if="currentLog?.requestParams">
            <text class="detail-section-title">请求参数</text>
            <view class="code-block">
              <text class="code-content">{{ currentLog.requestParams }}</text>
            </view>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn btn-outline" @click="hideDetailModal">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      hasMore: true,
      currentPage: 1,
      showDetailModal: false,
      currentLog: null,
      operationTypes: ['全部', '登录', '创建', '更新', '删除', '查询', '导出'],
      modules: ['全部', '培训管理', '学员管理', '用户管理', '系统设置'],
      filter: {
        startDate: '',
        endDate: '',
        operationType: '',
        module: ''
      },
      stats: {
        total: 1586,
        today: 89,
        users: 25
      },
      logList: [
        {
          id: 1,
          module: '用户管理',
          moduleIcon: '<text class="fa fa-users"></text>',
          type: '更新',
          typeClass: 'update',
          action: '更新用户信息',
          detail: '更新了用户"王强"的联系电话',
          operator: '张伟',
          username: 'admin001',
          tenant: '市委组织部',
          ip: '192.168.1.100',
          time: '2025-01-16 14:30:25',
          result: '成功',
          resultClass: 'success',
          userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
          method: 'POST',
          url: '/api/users/123',
          responseTime: 125
        },
        {
          id: 2,
          module: '培训管理',
          moduleIcon: '<text class="fa fa-book-open"></text>',
          type: '创建',
          typeClass: 'create',
          action: '创建培训',
          detail: '创建了新培训"2025年度第二期干部培训"',
          operator: '李娜',
          username: 'staff002',
          tenant: '市教育局',
          ip: '192.168.1.101',
          time: '2025-01-16 14:25:18',
          result: '成功',
          resultClass: 'success',
          userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)',
          method: 'POST',
          url: '/api/trainings',
          responseTime: 234
        },
        {
          id: 3,
          module: '系统设置',
          moduleIcon: '<text class="fa fa-cog"></text>',
          type: '更新',
          typeClass: 'update',
          action: '修改系统配置',
          detail: '修改了短信服务商配置',
          operator: '王强',
          username: 'admin003',
          tenant: '市卫健委',
          ip: '192.168.1.102',
          time: '2025-01-16 14:20:05',
          result: '失败',
          resultClass: 'error',
          userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
          method: 'PUT',
          url: '/api/settings/sms',
          responseTime: 89
        },
        {
          id: 4,
          module: '学员管理',
          moduleIcon: '🎓',
          type: '删除',
          typeClass: 'delete',
          action: '删除学员',
          detail: '删除了学员"李明"',
          operator: '刘芳',
          username: 'admin004',
          tenant: '市财政局',
          ip: '192.168.1.103',
          time: '2025-01-16 14:15:42',
          result: '成功',
          resultClass: 'success',
          userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
          method: 'DELETE',
          url: '/api/learners/456',
          responseTime: 156
        },
        {
          id: 5,
          module: '用户管理',
          moduleIcon: '<text class="fa fa-users"></text>',
          type: '登录',
          typeClass: 'login',
          action: '用户登录',
          detail: '用户"张伟"登录系统',
          operator: '张伟',
          username: 'admin001',
          tenant: '市委组织部',
          ip: '192.168.1.100',
          time: '2025-01-16 14:10:30',
          result: '成功',
          resultClass: 'success',
          userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
          method: 'POST',
          url: '/api/login',
          responseTime: 312
        }
      ]
    }
  },

  computed: {
    filteredLogs() {
      // 根据筛选条件过滤日志
      return this.logList
    }
  },

  methods: {
    onStartDateChange(e) {
      this.filter.startDate = e.detail.value
    },

    onEndDateChange(e) {
      this.filter.endDate = e.detail.value
    },

    onOperationTypeChange(e) {
      this.filter.operationType = this.operationTypes[e.detail.value]
    },

    onModuleChange(e) {
      this.filter.module = this.modules[e.detail.value]
    },

    resetFilter() {
      this.filter = {
        startDate: '',
        endDate: '',
        operationType: '',
        module: ''
      }
    },

    applyFilter() {
      uni.showLoading({ title: '查询中...' })

      setTimeout(() => {
        uni.hideLoading()

        uni.showToast({
          title: '查询成功',
          icon: 'success'
        })
      }, 500)
    },

    viewLogDetail(log) {
      this.currentLog = log
      this.showDetailModal = true
    },

    hideDetailModal() {
      this.showDetailModal = false
    },

    exportLogs() {
      uni.showLoading({ title: '导出中...' })

      setTimeout(() => {
        uni.hideLoading()

        uni.showToast({
          title: '导出成功',
          icon: 'success'
        })
      }, 1500)
    },

    loadMore() {
      if (!this.hasMore) return

      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
        this.hasMore = false
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
@import '../../styles/global-patch.scss';

.logs-container {
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

.filter-section {
  margin: $spacing-md;
}

.filter-row {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.filter-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  white-space: nowrap;
}

.picker-input-small {
  flex: 1;
  padding: $spacing-xs $spacing-sm;
  background: $bg-tertiary;
  border-radius: $border-radius-sm;
  text-align: center;
  font-size: $font-size-sm;
  color: $text-primary;
}

.date-separator {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.filter-actions {
  display: flex;
  gap: $spacing-md;
  margin-top: $spacing-lg;
}

.stats-section {
  display: flex;
  gap: $spacing-md;
  padding: 0 $spacing-md $spacing-md;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
}

.stat-value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.logs-scroll {
  height: calc(100vh - 480rpx);
  padding: 0 $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.log-item {
  margin-bottom: $spacing-md;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.log-module {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.module-icon {
  font-size: 24rpx;
}

.module-name {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.type-badge {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
}

.type-badge.create {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.type-badge.update {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.type-badge.delete {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.type-badge.login {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.log-content {
  margin-bottom: $spacing-sm;
}

.log-action {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.log-detail {
  display: block;
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.5;
}

.log-meta {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.meta-icon {
  font-size: 20rpx;
}

.meta-text {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.log-result {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding-top: $spacing-sm;
  border-top: 1rpx solid $border-color;
}

.result-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.result-value {
  font-size: $font-size-sm;
  font-weight: 500;
}

.result-value.success {
  color: #10b981;
}

.result-value.error {
  color: #ef4444;
}

.load-more-btn {
  padding: $spacing-md;
  text-align: center;
  color: $primary-color;
  font-size: $font-size-md;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg;
}

.detail-modal {
  width: 100%;
  max-width: 650rpx;
  max-height: 85vh;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.modal-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.modal-close {
  font-size: $font-size-xl;
  color: $text-secondary;
  padding: $spacing-sm;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-lg;
  border-bottom: 1rpx solid $border-color;
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-section-title {
  display: block;
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.detail-row {
  display: flex;
  margin-bottom: $spacing-sm;
}

.detail-label {
  width: 160rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
  word-break: break-all;
}

.code-block {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.code-content {
  font-size: $font-size-xs;
  color: $text-primary;
  font-family: monospace;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.modal-footer {
  padding-top: $spacing-lg;
  border-top: 1rpx solid $border-color;
}
</style>
