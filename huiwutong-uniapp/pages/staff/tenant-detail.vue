<template>
  <view class="tenant-detail-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">租户详情</text>
        <text class="header-action" @click="editTenant">编辑</text>
      </view>
    </view>

    <scroll-view class="detail-scroll" scroll-y>
      <!-- 基本信息 -->
      <view class="info-section card">
        <view class="section-title">基本信息</view>

        <view class="info-row">
          <text class="info-label">租户名称</text>
          <text class="info-value">{{ tenantInfo.name }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">租户代码</text>
          <text class="info-value">{{ tenantInfo.code }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">租户状态</text>
          <view
            class="status-badge"
            :class="tenantInfo.status"
          >
            <text class="status-text">{{ getStatusLabel(tenantInfo.status) }}</text>
          </view>
        </view>

        <view class="info-row">
          <text class="info-label">联系人</text>
          <text class="info-value">{{ tenantInfo.contact }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">联系电话</text>
          <text class="info-value">{{ tenantInfo.phone }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">联系邮箱</text>
          <text class="info-value">{{ tenantInfo.email }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">创建时间</text>
          <text class="info-value">{{ tenantInfo.createTime }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">到期时间</text>
          <text class="info-value" :class="{ expiring: isExpiring(tenantInfo.expiryDate) }">
            {{ tenantInfo.expiryDate }}
          </text>
        </view>
      </view>

      <!-- 使用统计 -->
      <view class="stats-section card">
        <view class="section-title">使用统计</view>

        <view class="stats-grid">
          <view class="stat-box">
            <text class="stat-value">{{ tenantInfo.userCount }}</text>
            <text class="stat-label">用户数量</text>
            <view class="stat-progress">
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: (tenantInfo.userCount / tenantInfo.maxUsers * 100) + '%' }"
                ></view>
              </view>
              <text class="progress-text">{{ tenantInfo.userCount }}/{{ tenantInfo.maxUsers }}</text>
            </view>
          </view>

          <view class="stat-box">
            <text class="stat-value">{{ formatStorage(tenantInfo.usedStorage) }}</text>
            <text class="stat-label">存储空间</text>
            <view class="stat-progress">
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: (tenantInfo.usedStorage / tenantInfo.totalStorage * 100) + '%' }"
                ></view>
              </view>
              <text class="progress-text">{{ formatStorage(tenantInfo.usedStorage, tenantInfo.totalStorage) }}</text>
            </view>
          </view>
        </view>

        <view class="usage-details">
          <view class="usage-item">
            <text class="usage-label">培训数量：</text>
            <text class="usage-value">{{ tenantInfo.trainingCount }}场</text>
          </view>
          <view class="usage-item">
            <text class="usage-label">学员总数：</text>
            <text class="usage-value">{{ tenantInfo.learnerCount }}人</text>
          </view>
          <view class="usage-item">
            <text class="usage-label">本月活跃：</text>
            <text class="usage-value">{{ tenantInfo.activeUsers }}人</text>
          </view>
        </view>
      </view>

      <!-- 功能模块 -->
      <view class="modules-section card">
        <view class="section-title">功能模块</view>

        <view class="module-list">
          <view
            v-for="module in tenantInfo.modules"
            :key="module.id"
            class="module-item"
          >
            <view class="module-info">
              <text class="module-icon">{{ module.icon }}</text>
              <view class="module-details">
                <text class="module-name">{{ module.name }}</text>
                <text class="module-desc">{{ module.description }}</text>
              </view>
            </view>
            <view
              class="module-status"
              :class="{ active: module.enabled }"
            >
              <text class="status-text">{{ module.enabled ? '已启用' : '未启用' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 操作记录 -->
      <view class="logs-section card">
        <view class="section-title">最近操作</view>

        <view class="log-list">
          <view
            v-for="log in operationLogs"
            :key="log.id"
            class="log-item"
          >
            <view class="log-header">
              <text class="log-action">{{ log.action }}</text>
              <text class="log-time">{{ log.time }}</text>
            </view>
            <text class="log-user">操作人：{{ log.operator }}</text>
            <text class="log-detail" v-if="log.detail">{{ log.detail }}</text>
          </view>
        </view>

        <view class="view-all-btn" @click="viewAllLogs">
          <text>查看全部操作 →</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="actions-section">
        <button class="btn btn-outline btn-block" @click="resetPassword">
          🔄 重置管理员密码
        </button>
        <button class="btn btn-outline btn-block" @click="renewTenant">
          <text class="fa fa-calendar-alt"></text> 续费租户
        </button>
        <button
          class="btn btn-outline btn-block"
          :class="{ disabled: tenantInfo.status === 'suspended' }"
          @click="toggleTenantStatus"
        >
          {{ tenantInfo.status === 'active' ? '⏸️ 暂停租户' : '▶️ 启用租户' }}
        </button>
        <button class="btn btn-danger btn-block" @click="deleteTenant">
          🗑️ 删除租户
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      tenantId: '',
      tenantInfo: {
        name: '市委组织部',
        code: 'ORG001',
        status: 'active',
        contact: '张主任',
        phone: '13800138001',
        email: 'zhang@org.gov.cn',
        createTime: '2024-01-15 10:30',
        expiryDate: '2025-12-31',
        userCount: 85,
        maxUsers: 100,
        usedStorage: 68.5,
        totalStorage: 100,
        trainingCount: 12,
        learnerCount: 156,
        activeUsers: 45,
        modules: [
          { id: 1, name: '培训管理', icon: '<text class="fa fa-book-open"></text>', description: '创建和管理培训活动', enabled: true },
          { id: 2, name: '学员管理', icon: '<text class="fa fa-users"></text>', description: '管理学员信息和分组', enabled: true },
          { id: 3, name: '签到管理', icon: '<text class="fa fa-check"></text>', description: '学员签到和考勤', enabled: true },
          { id: 4, name: '评价反馈', icon: '⭐', description: '收集学员反馈', enabled: true },
          { id: 5, name: '数据分析', icon: '📊', description: '培训数据统计分析', enabled: true },
          { id: 6, name: '档案管理', icon: '📁', description: '历史数据归档', enabled: false }
        ]
      },
      operationLogs: [
        {
          id: 1,
          action: '更新租户信息',
          operator: '张伟',
          time: '2025-01-16 14:30',
          detail: '更新了联系人信息'
        },
        {
          id: 2,
          action: '增加用户配额',
          operator: '李娜',
          time: '2025-01-15 10:20',
          detail: '用户配额从80增加到100'
        },
        {
          id: 3,
          action: '启用功能模块',
          operator: '王强',
          time: '2025-01-14 16:45',
          detail: '启用了数据分析模块'
        }
      ]
    }
  },

  onLoad(options) {
    if (options.id) {
      this.tenantId = options.id
      this.loadTenantDetail()
    }
  },

  methods: {
    getStatusLabel(status) {
      const labelMap = {
        active: '活跃',
        suspended: '暂停',
        expiring: '即将到期'
      }
      return labelMap[status] || status
    },

    isExpiring(date) {
      const expiryDate = new Date(date)
      const today = new Date()
      const diffDays = Math.ceil((expiryDate - today) / (1000 * 60 * 60 * 24))
      return diffDays <= 30
    },

    formatStorage(used, total) {
      if (total) {
        return `${used.toFixed(1)}GB/${total}GB`
      }
      return `${used.toFixed(1)}GB`
    },

    loadTenantDetail() {
      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    editTenant() {
      uni.navigateTo({
        url: `/pages/staff/tenant-manage?edit=true&id=${this.tenantId}`
      })
    },

    resetPassword() {
      uni.showModal({
        title: '重置管理员密码',
        content: '确定要重置该租户的管理员密码吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '密码已重置',
                icon: 'success'
              })
            }, 1000)
          }
        }
      })
    },

    renewTenant() {
      uni.showModal({
        title: '租户续费',
        content: '请选择续费时长',
        editable: true,
        placeholderText: '请输入续费月数',
        success: (res) => {
          if (res.confirm && res.content) {
            uni.showLoading({ title: '处理中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '续费成功',
                icon: 'success'
              })
            }, 1000)
          }
        }
      })
    },

    toggleTenantStatus() {
      const action = this.tenantInfo.status === 'active' ? '暂停' : '启用'

      uni.showModal({
        title: `${action}租户`,
        content: `确定要${action}该租户吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })

            setTimeout(() => {
              uni.hideLoading()
              this.tenantInfo.status = this.tenantInfo.status === 'active' ? 'suspended' : 'active'

              uni.showToast({
                title: `${action}成功`,
                icon: 'success'
              })
            }, 1000)
          }
        }
      })
    },

    deleteTenant() {
      uni.showModal({
        title: '删除租户',
        content: '删除租户将删除所有相关数据，此操作不可恢复！确定要删除吗？',
        confirmColor: '#ef4444',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '删除中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '删除成功',
                icon: 'success'
              })

              setTimeout(() => {
                uni.navigateBack()
              }, 1500)
            }, 1000)
          }
        }
      })
    },

    viewAllLogs() {
      uni.navigateTo({
        url: `/pages/staff/logs?tenantId=${this.tenantId}`
      })
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

.tenant-detail-container {
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

.detail-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.info-section,
.stats-section,
.modules-section,
.logs-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.info-value {
  font-size: $font-size-md;
  color: $text-primary;
  font-weight: 500;
}

.info-value.expiring {
  color: #f59e0b;
}

.status-badge {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
}

.status-badge.active {
  background: rgba(16, 185, 129, 0.1);
}

.status-badge.active .status-text {
  color: #10b981;
}

.status-badge.suspended {
  background: rgba(239, 68, 68, 0.1);
}

.status-badge.suspended .status-text {
  color: #ef4444;
}

.status-badge.expiring {
  background: rgba(245, 158, 11, 0.1);
}

.status-badge.expiring .status-text {
  color: #f59e0b;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.stat-box {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-tertiary;
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

.stat-progress {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.progress-bar {
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

.progress-text {
  font-size: $font-size-xs;
  color: $text-tertiary;
  text-align: center;
}

.usage-details {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.usage-item {
  display: flex;
  justify-content: space-between;
  font-size: $font-size-md;
}

.usage-label {
  color: $text-secondary;
}

.usage-value {
  color: $text-primary;
  font-weight: 500;
}

.module-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.module-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.module-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex: 1;
}

.module-icon {
  font-size: 32rpx;
}

.module-details {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.module-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.module-desc {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.module-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  background: rgba(0, 0, 0, 0.05);
}

.module-status.active {
  background: rgba(16, 185, 129, 0.1);
}

.module-status .status-text {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.module-status.active .status-text {
  color: #10b981;
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.log-item {
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.log-item:last-child {
  border-bottom: none;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xs;
}

.log-action {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.log-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.log-user {
  display: block;
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: 4rpx;
}

.log-detail {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.view-all-btn {
  padding: $spacing-md;
  text-align: center;
  color: $primary-color;
  font-size: $font-size-md;
}

.actions-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.btn.disabled {
  opacity: 0.5;
}

.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}
</style>
