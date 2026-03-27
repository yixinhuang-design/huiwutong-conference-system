<template>
  <view class="user-detail-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">用户详情</text>
        <text class="header-action" @click="editUser">编辑</text>
      </view>
    </view>

    <scroll-view class="detail-scroll" scroll-y>
      <!-- 用户头像和基本信息 -->
      <view class="profile-section card">
        <view class="profile-header">
          <view class="user-avatar-large">
            {{ userInfo.name.charAt(userInfo.name.length - 1) }}
          </view>
          <view class="profile-info">
            <text class="user-name">{{ userInfo.name }}</text>
            <view class="user-tags">
              <text class="user-tag">{{ userInfo.role }}</text>
              <text
                class="user-status"
                :class="userInfo.status"
              >
                {{ getStatusLabel(userInfo.status) }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="info-section card">
        <view class="section-title">基本信息</view>

        <view class="info-row">
          <text class="info-label">用户名</text>
          <text class="info-value">{{ userInfo.username }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">姓名</text>
          <text class="info-value">{{ userInfo.name }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">性别</text>
          <text class="info-value">{{ getGenderLabel(userInfo.gender) }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">手机号</text>
          <text class="info-value">{{ userInfo.phone }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">邮箱</text>
          <text class="info-value">{{ userInfo.email }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">身份证号</text>
          <text class="info-value">{{ maskIdCard(userInfo.idCard) }}</text>
        </view>
      </view>

      <!-- 组织信息 -->
      <view class="org-section card">
        <view class="section-title">组织信息</view>

        <view class="info-row">
          <text class="info-label">所属租户</text>
          <text class="info-value">{{ userInfo.tenant }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">部门</text>
          <text class="info-value">{{ userInfo.department || '-' }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">职位</text>
          <text class="info-value">{{ userInfo.position || '-' }}</text>
        </view>
      </view>

      <!-- 账号信息 -->
      <view class="account-section card">
        <view class="section-title">账号信息</view>

        <view class="info-row">
          <text class="info-label">账号状态</text>
          <view
            class="status-badge"
            :class="userInfo.status"
          >
            <text class="status-text">{{ getStatusLabel(userInfo.status) }}</text>
          </view>
        </view>

        <view class="info-row">
          <text class="info-label">注册时间</text>
          <text class="info-value">{{ userInfo.createTime }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">最后登录</text>
          <text class="info-value">{{ userInfo.lastLoginTime }}</text>
        </view>

        <view class="info-row">
          <text class="info-label">登录次数</text>
          <text class="info-value">{{ userInfo.loginCount }}次</text>
        </view>

        <view class="info-row">
          <text class="info-label">最后登录IP</text>
          <text class="info-value">{{ userInfo.lastLoginIp }}</text>
        </view>
      </view>

      <!-- 培训统计 -->
      <view class="training-stats-section card">
        <view class="section-title">培训统计</view>

        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{ userInfo.trainingCount }}</text>
            <text class="stat-label">参与培训</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ userInfo.completedCount }}</text>
            <text class="stat-label">完成培训</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ userInfo.attendanceRate }}%</text>
            <text class="stat-label">签到率</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ userInfo.avgScore }}</text>
            <text class="stat-label">平均分</text>
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
            <text class="log-detail">{{ log.detail }}</text>
          </view>
        </view>

        <view class="view-all-btn" @click="viewAllLogs">
          <text>查看全部操作 →</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="actions-section">
        <button class="btn btn-outline btn-block" @click="resetPassword">
          🔑 重置密码
        </button>
        <button
          class="btn btn-outline btn-block"
          @click="toggleUserStatus"
        >
          {{ userInfo.status === 'active' ? '⏸️ 禁用账号' : '▶️ 启用账号' }}
        </button>
        <button class="btn btn-outline btn-block" @click="viewAsUser">
          👁️ 以该用户身份查看
        </button>
        <button class="btn btn-danger btn-block" @click="deleteUser">
          🗑️ 删除用户
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      userId: '',
      userInfo: {
        username: 'learner003',
        name: '王强',
        gender: 'male',
        phone: '13800138003',
        email: 'wangqiang@hea.gov.cn',
        idCard: '420102198001011234',
        tenant: '市卫健委',
        department: '办公室',
        position: '科员',
        role: '学员',
        status: 'active',
        createTime: '2024-06-15 10:30',
        lastLoginTime: '2025-01-16 14:25',
        loginCount: 45,
        lastLoginIp: '192.168.1.100',
        trainingCount: 8,
        completedCount: 6,
        attendanceRate: 92,
        avgScore: 89.5
      },
      operationLogs: [
        {
          id: 1,
          action: '登录系统',
          time: '2025-01-16 14:25',
          detail: '从IP 192.168.1.100登录'
        },
        {
          id: 2,
          action: '提交培训评价',
          time: '2025-01-15 16:30',
          detail: '提交了《综合素质提升培训》的评价'
        },
        {
          id: 3,
          action: '完成签到',
          time: '2025-01-15 09:00',
          detail: '完成上午课程签到'
        },
        {
          id: 4,
          action: '修改个人信息',
          time: '2025-01-14 11:20',
          detail: '更新了联系电话'
        }
      ]
    }
  },

  onLoad(options) {
    if (options.id) {
      this.userId = options.id
      this.loadUserDetail()
    }
  },

  methods: {
    getStatusLabel(status) {
      const labelMap = {
        active: '正常',
        disabled: '已禁用'
      }
      return labelMap[status] || status
    },

    getGenderLabel(gender) {
      const labelMap = {
        male: '男',
        female: '女'
      }
      return labelMap[gender] || gender
    },

    maskIdCard(idCard) {
      if (!idCard) return '-'
      return idCard.substring(0, 6) + '********' + idCard.substring(14)
    },

    loadUserDetail() {
      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    editUser() {
      uni.navigateTo({
        url: `/pages/staff/user-manage?edit=true&id=${this.userId}`
      })
    },

    resetPassword() {
      uni.showModal({
        title: '重置密码',
        content: '确定要重置该用户的密码吗？新密码将发送到用户邮箱。',
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

    toggleUserStatus() {
      const action = this.userInfo.status === 'active' ? '禁用' : '启用'

      uni.showModal({
        title: `${action}账号`,
        content: `确定要${action}该账号吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })

            setTimeout(() => {
              uni.hideLoading()
              this.userInfo.status = this.userInfo.status === 'active' ? 'disabled' : 'active'

              uni.showToast({
                title: `${action}成功`,
                icon: 'success'
              })
            }, 1000)
          }
        }
      })
    },

    viewAsUser() {
      uni.showModal({
        title: '身份切换',
        content: '将以该用户身份登录系统，确定继续？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '切换中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '切换成功',
                icon: 'success'
              })
            }, 1000)
          }
        }
      })
    },

    deleteUser() {
      uni.showModal({
        title: '删除用户',
        content: '删除用户将删除所有相关数据，此操作不可恢复！确定要删除吗？',
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
        url: `/pages/staff/logs?userId=${this.userId}`
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

.user-detail-container {
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

.profile-section,
.info-section,
.org-section,
.account-section,
.training-stats-section,
.logs-section {
  margin-bottom: $spacing-md;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.user-avatar-large {
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
}

.profile-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.user-name {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.user-tags {
  display: flex;
  gap: $spacing-sm;
}

.user-tag {
  padding: 4rpx 12rpx;
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.user-status {
  padding: 4rpx 12rpx;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.user-status.active {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.user-status.disabled {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
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

.status-badge.disabled {
  background: rgba(239, 68, 68, 0.1);
}

.status-badge.disabled .status-text {
  color: #ef4444;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
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

.log-detail {
  display: block;
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.5;
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

.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}
</style>
