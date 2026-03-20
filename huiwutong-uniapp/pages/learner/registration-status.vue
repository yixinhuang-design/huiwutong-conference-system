<template>
  <view class="registration-status-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">报名状态</text>
        <text class="header-action" @click="refresh">刷新</text>
      </view>
    </view>

    <scroll-view class="status-scroll" scroll-y>
      <!-- 当前状态 -->
      <view class="status-card" :class="'status-' + currentStatus.type">
        <view class="status-icon">{{ currentStatus.icon }}</view>
        <view class="status-info">
          <text class="status-title">{{ currentStatus.title }}</text>
          <text class="status-desc">{{ currentStatus.desc }}</text>
          <text class="status-time" v-if="currentStatus.time">{{ currentStatus.time }}</text>
        </view>
      </view>

      <!-- 报名信息 -->
      <view class="info-section card">
        <view class="section-title">报名信息</view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">培训名称</text>
            <text class="info-value">{{ registrationInfo.trainingName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">报名编号</text>
            <text class="info-value">{{ registrationInfo.registrationNo }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">报名时间</text>
            <text class="info-value">{{ registrationInfo.submitTime }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">培训时间</text>
            <text class="info-value">{{ registrationInfo.trainingDate }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">培训地点</text>
            <text class="info-value">{{ registrationInfo.location }}</text>
          </view>
        </view>
      </view>

      <!-- 个人信息 -->
      <view class="personal-section card">
        <view class="section-title">个人信息</view>
        <view class="personal-info">
          <view class="info-row">
            <text class="row-label">姓名</text>
            <text class="row-value">{{ registrationInfo.name }}</text>
          </view>
          <view class="info-row">
            <text class="row-label">单位</text>
            <text class="row-value">{{ registrationInfo.department }}</text>
          </view>
          <view class="info-row">
            <text class="row-label">职务</text>
            <text class="row-value">{{ registrationInfo.position }}</text>
          </view>
          <view class="info-row">
            <text class="row-label">联系电话</text>
            <text class="row-value">{{ registrationInfo.phone }}</text>
          </view>
        </view>
      </view>

      <!-- 审核信息 -->
      <view class="review-section card" v-if="showReviewInfo">
        <view class="section-title">审核信息</view>
        <view class="review-info">
          <view class="review-item">
            <text class="review-label">审核状态</text>
            <view class="review-status" :class="'status-' + registrationInfo.reviewStatus">
              {{ getReviewStatusText(registrationInfo.reviewStatus) }}
            </view>
          </view>
          <view class="review-item" v-if="registrationInfo.reviewer">
            <text class="review-label">审核人</text>
            <text class="review-value">{{ registrationInfo.reviewer }}</text>
          </view>
          <view class="review-item" v-if="registrationInfo.reviewTime">
            <text class="review-label">审核时间</text>
            <text class="review-value">{{ registrationInfo.reviewTime }}</text>
          </view>
          <view class="review-item" v-if="registrationInfo.reviewComment">
            <text class="review-label">审核意见</text>
            <text class="review-value">{{ registrationInfo.reviewComment }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <button
          v-if="registrationInfo.status === 'rejected'"
          class="btn btn-primary btn-block"
          @click="reRegister"
        >
          🔄 重新报名
        </button>
        <button
          v-if="registrationInfo.status === 'approved'"
          class="btn btn-outline btn-block"
          @click="viewDetails"
        >
          <text class="fa fa-clipboard"></text> 查看培训详情
        </button>
        <button
          v-if="registrationInfo.status === 'pending'"
          class="btn btn-text btn-block"
          @click="cancelRegistration"
        >
          取消报名
        </button>
      </view>

      <!-- 温馨提示 -->
      <view class="tips-section card">
        <view class="tips-title">💡 温馨提示</view>
        <view class="tips-list">
          <text class="tips-item">• 审核通过后将收到短信通知</text>
          <text class="tips-item">• 请保持手机畅通，注意查收通知</text>
          <text class="tips-item">• 如有疑问请联系主办方</text>
          <text class="tips-item">• 培训前3天将发送参会提醒</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      registrationId: '',
      registrationInfo: {
        status: 'approved',
        trainingName: '2025年度第一期干部综合素质提升培训',
        registrationNo: 'BW20250115001',
        submitTime: '2025-01-15 10:30',
        trainingDate: '2025年1月18日-20日',
        location: '市委党校报告厅',
        name: '张伟',
        department: '市教育局',
        position: '科长',
        phone: '138****8888',
        reviewStatus: 'approved',
        reviewer: '李老师',
        reviewTime: '2025-01-15 14:20',
        reviewComment: '审核通过，请按时参加培训'
      }
    }
  },

  computed: {
    currentStatus() {
      const statusMap = {
        pending: {
          type: 'pending',
          icon: '⏳',
          title: '待审核',
          desc: '您的报名正在审核中，请耐心等待',
          time: '预计1-2个工作日完成审核'
        },
        approved: {
          type: 'approved',
          icon: '<text class="fa fa-check"></text>',
          title: '审核通过',
          desc: '恭喜您，报名审核已通过',
          time: '请按时参加培训'
        },
        rejected: {
          type: 'rejected',
          icon: '❌',
          title: '审核未通过',
          desc: '很遗憾，您的报名未通过审核',
          time: '您可以重新报名'
        }
      }
      return statusMap[this.registrationInfo.status] || statusMap.pending
    },

    showReviewInfo() {
      return this.registrationInfo.status !== 'pending'
    }
  },

  onLoad(options) {
    if (options.id) {
      this.registrationId = options.id
      this.loadRegistrationStatus()
    }
  },

  methods: {
    loadRegistrationStatus() {
      uni.showLoading({ title: '加载中...' })
      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    getReviewStatusText(status) {
      const textMap = {
        approved: '已通过',
        rejected: '未通过'
      }
      return textMap[status] || ''
    },

    refresh() {
      uni.showLoading({ title: '刷新中...' })
      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({
          title: '已更新',
          icon: 'success'
        })
      }, 1000)
    },

    reRegister() {
      uni.showModal({
        title: '重新报名',
        content: '确认重新报名此培训？',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({
              url: '/pages/learner/scan-register'
            })
          }
        }
      })
    },

    viewDetails() {
      uni.navigateTo({
        url: '/pages/learner/meeting-detail'
      })
    },

    cancelRegistration() {
      uni.showModal({
        title: '取消报名',
        content: '确认取消本次报名？取消后需重新报名',
        confirmColor: '#ef4444',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            setTimeout(() => {
              uni.hideLoading()
              uni.showToast({
                title: '已取消报名',
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

    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.registration-status-container {
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

.status-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.status-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.status-card.status-pending {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(245, 158, 11, 0.05) 100%);
  border: 1rpx solid rgba(245, 158, 11, 0.3);
}

.status-card.status-approved {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.05) 100%);
  border: 1rpx solid rgba(16, 185, 129, 0.3);
}

.status-card.status-rejected {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.05) 100%);
  border: 1rpx solid rgba(239, 68, 68, 0.3);
}

.status-icon {
  font-size: 64rpx;
}

.status-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.status-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.status-desc {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.5;
}

.status-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.card {
  margin-bottom: $spacing-md;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.info-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.info-value {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.personal-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
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

.row-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.row-value {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.review-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.review-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.review-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.review-value {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: 1.6;
}

.review-status {
  display: inline-block;
  padding: 8rpx 16rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  font-weight: 500;
}

.review-status.status-approved {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.review-status.status-rejected {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.btn-text {
  background: transparent;
  border: none;
  color: $text-tertiary;
}

.tips-section {
  margin-bottom: $spacing-lg;
}

.tips-title {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.tips-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.8;
}
</style>
