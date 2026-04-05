<template>
  <view class="meeting-detail-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <view class="header-back" @click="goBack">
          <text class="fa fa-arrow-left"></text>
        </view>
        <text class="header-title">培训详情</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 加载失败 -->
    <view v-else-if="loadError" class="empty-container">
      <text class="fa fa-exclamation-circle empty-icon"></text>
      <text class="empty-text">{{ loadError }}</text>
      <button class="btn btn-primary btn-sm" @click="loadMeetingDetail">重新加载</button>
    </view>

    <!-- 主内容 -->
    <view v-else>
      <!-- 封面图 -->
      <view class="cover-section" v-if="meetingInfo.coverImageUrl">
        <image class="cover-image" :src="meetingInfo.coverImageUrl" mode="aspectFill" />
        <view class="cover-overlay">
          <view :class="['status-chip', getStatusClass(meetingInfo.status)]">
            <text class="status-dot"></text>
            {{ getStatusText(meetingInfo.status) }}
          </view>
        </view>
      </view>

      <!-- 培训基本信息 -->
      <view class="info-card card">
        <view class="meeting-title">{{ meetingInfo.meetingName }}</view>
        <view v-if="meetingInfo.theme" class="meeting-theme">{{ meetingInfo.theme }}</view>
        <view class="meeting-meta">
          <view class="meta-item">
            <text class="fa fa-calendar-alt"></text>
            <text>{{ formatDateRange(meetingInfo.startTime, meetingInfo.endTime) }}</text>
          </view>
          <view class="meta-item" v-if="meetingInfo.registrationStart">
            <text class="fa fa-user-edit"></text>
            <text>报名：{{ formatDateRange(meetingInfo.registrationStart, meetingInfo.registrationEnd) }}</text>
          </view>
          <view class="meta-item" v-if="meetingInfo.venueName">
            <text class="fa fa-map-marker-alt"></text>
            <text>{{ meetingInfo.venueName }}{{ meetingInfo.venueAddress ? ' · ' + meetingInfo.venueAddress : '' }}</text>
          </view>
          <view class="meta-item" v-if="meetingInfo.meetingCode">
            <text class="fa fa-hashtag"></text>
            <text>{{ meetingInfo.meetingCode }}</text>
          </view>
        </view>
        <!-- 无封面时在此显示状态 -->
        <view class="meeting-status" v-if="!meetingInfo.coverImageUrl">
          <view :class="['status-chip', getStatusClass(meetingInfo.status)]">
            <text class="status-dot"></text>
            {{ getStatusText(meetingInfo.status) }}
          </view>
        </view>
      </view>

      <!-- 统计数据 -->
      <view class="stats-section card">
        <view class="section-title">培训概况</view>
        <view class="stats-grid grid-2">
          <view class="stat-item">
            <view class="stat-value">{{ meetingInfo.currentParticipants || 0 }}</view>
            <view class="stat-label">已报名</view>
          </view>
          <view class="stat-item">
            <view class="stat-value">{{ meetingInfo.maxParticipants || '不限' }}</view>
            <view class="stat-label">名额上限</view>
          </view>
          <view class="stat-item">
            <view class="stat-value">{{ calcDays }}</view>
            <view class="stat-label">培训天数</view>
          </view>
          <view class="stat-item">
            <view class="stat-value">{{ meetingInfo.meetingType ? getMeetingTypeName(meetingInfo.meetingType) : '--' }}</view>
            <view class="stat-label">会议类型</view>
          </view>
        </view>
      </view>

      <!-- 快捷操作 -->
      <view class="actions-section card">
        <view class="section-title">快捷操作</view>
        <view class="actions-grid grid-3">
          <view class="action-tile feature-tile" @click="goToSchedule">
            <view class="action-icon-lg feature-icon-lg">
              <text class="fa fa-calendar-alt"></text>
            </view>
            <view class="action-title feature-title">日程</view>
          </view>
          <view class="action-tile feature-tile" @click="goToSeat">
            <view class="action-icon-lg feature-icon-lg">
              <text class="fa fa-th-large"></text>
            </view>
            <view class="action-title feature-title">座位</view>
          </view>
          <view class="action-tile feature-tile" @click="goToCheckin">
            <view class="action-icon-lg feature-icon-lg">
              <text class="fa fa-qrcode"></text>
            </view>
            <view class="action-title feature-title">签到</view>
          </view>
          <view class="action-tile feature-tile" @click="goToMaterials">
            <view class="action-icon-lg feature-icon-lg">
              <text class="fa fa-book-open"></text>
            </view>
            <view class="action-title feature-title">资料</view>
          </view>
          <view class="action-tile feature-tile" @click="goToGroups">
            <view class="action-icon-lg feature-icon-lg">
              <text class="fa fa-users"></text>
            </view>
            <view class="action-title feature-title">群组</view>
          </view>
          <view class="action-tile feature-tile" @click="goToContact">
            <view class="action-icon-lg feature-icon-lg">
              <text class="fa fa-address-book"></text>
            </view>
            <view class="action-title feature-title">通讯录</view>
          </view>
        </view>
      </view>

      <!-- 培训简介 -->
      <view class="intro-section card" v-if="meetingInfo.description">
        <view class="section-title">培训简介</view>
        <view class="intro-content card-content">
          {{ meetingInfo.description }}
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { meeting as meetingApi } from '@/api/index'

export default {
  data() {
    return {
      meetingId: '',
      loading: true,
      loadError: '',
      meetingInfo: {}
    }
  },

  computed: {
    /**
     * 根据 startTime / endTime 计算培训天数
     */
    calcDays() {
      if (!this.meetingInfo.startTime || !this.meetingInfo.endTime) return '--'
      const start = new Date(this.meetingInfo.startTime.replace(/-/g, '/'))
      const end = new Date(this.meetingInfo.endTime.replace(/-/g, '/'))
      const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
      return diff > 0 ? diff : 1
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
      this.loadMeetingDetail()
    } else {
      this.loading = false
      this.loadError = '缺少会议ID参数'
    }
  },

  methods: {
    /**
     * 加载培训详情 - 调用真实后端接口
     */
    async loadMeetingDetail() {
      this.loading = true
      this.loadError = ''
      try {
        const res = await meetingApi.getDetail(this.meetingId)
        // response interceptor 返回 data.data || data
        const data = res.data || res
        if (data && data.meetingName) {
          this.meetingInfo = data
        } else if (data && data.id) {
          // 兼容直接返回对象的情况
          this.meetingInfo = data
        } else {
          this.loadError = '未找到该会议信息'
        }
      } catch (error) {
        console.error('Load meeting detail error:', error)
        this.loadError = '加载失败，请检查网络后重试'
      } finally {
        this.loading = false
      }
    },

    /**
     * 返回上一页
     */
    goBack() {
      uni.navigateBack({ delta: 1 })
    },

    /**
     * 格式化日期范围
     */
    formatDateRange(start, end) {
      if (!start) return '待定'
      const s = this.formatDateTime(start)
      const e = end ? this.formatDateTime(end) : ''
      return e ? `${s} ~ ${e}` : s
    },

    /**
     * 格式化日期时间
     * "2026-04-10 09:00:00" → "4月10日 09:00"
     */
    formatDateTime(dateStr) {
      if (!dateStr) return ''
      try {
        const d = new Date(dateStr.replace(/-/g, '/'))
        const month = d.getMonth() + 1
        const day = d.getDate()
        const hour = String(d.getHours()).padStart(2, '0')
        const min = String(d.getMinutes()).padStart(2, '0')
        return `${month}月${day}日 ${hour}:${min}`
      } catch {
        return dateStr
      }
    },

    /**
     * 状态对应样式 class
     * 0=草稿 1=报名中 2=进行中 3=已完成 4=已取消
     */
    getStatusClass(status) {
      const map = {
        0: 'status-draft',
        1: 'status-registering',
        2: 'status-ongoing',
        3: 'status-finished',
        4: 'status-cancelled'
      }
      return map[status] || 'status-draft'
    },

    /**
     * 状态文本
     */
    getStatusText(status) {
      const map = {
        0: '草稿',
        1: '报名中',
        2: '进行中',
        3: '已完成',
        4: '已取消'
      }
      return map[status] || '未知'
    },

    /**
     * 会议类型名称
     */
    getMeetingTypeName(type) {
      const map = {
        training: '培训',
        conference: '会议',
        seminar: '研讨',
        workshop: '工作坊',
        forum: '论坛',
        lecture: '讲座',
        exam: '考试',
        other: '其他'
      }
      return map[type] || type || '--'
    },

    goToSchedule() {
      uni.navigateTo({ url: `/pages/learner/schedule?id=${this.meetingId}` })
    },
    goToSeat() {
      uni.navigateTo({ url: `/pages/learner/seat?id=${this.meetingId}` })
    },
    goToCheckin() {
      uni.navigateTo({ url: `/pages/learner/checkin?id=${this.meetingId}` })
    },
    goToMaterials() {
      uni.navigateTo({ url: `/pages/learner/materials?id=${this.meetingId}` })
    },
    goToGroups() {
      uni.navigateTo({ url: `/pages/learner/groups?id=${this.meetingId}` })
    },
    goToContact() {
      uni.navigateTo({ url: `/pages/learner/contact?id=${this.meetingId}` })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.meeting-detail-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* ---- Header ---- */
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
  justify-content: space-between;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  .fa {
    font-size: 32rpx;
    color: $text-primary;
  }
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.header-placeholder {
  width: 60rpx;
}

/* ---- Loading / Empty ---- */
.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 40rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid rgba(0,0,0,0.08);
  border-top-color: $primary-color;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: $spacing-md;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.empty-icon {
  font-size: 80rpx;
  color: $text-placeholder;
  margin-bottom: $spacing-md;
}

.empty-text {
  font-size: $font-size-md;
  color: $text-secondary;
  margin-bottom: $spacing-lg;
}

/* ---- Cover Image ---- */
.cover-section {
  position: relative;
  width: 100%;
  height: 360rpx;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-md;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
  display: flex;
  justify-content: flex-start;
}

/* ---- Cards ---- */
.info-card,
.stats-section,
.actions-section,
.intro-section {
  margin: $spacing-md;
  @extend .card;
}

.meeting-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8rpx;
  line-height: 1.4;
}

.meeting-theme {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-md;
  padding: 6rpx 16rpx;
  background: rgba(59,130,246,0.06);
  border-radius: $border-radius-sm;
  display: inline-block;
}

.meeting-meta {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-bottom: $spacing-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  line-height: 1.5;

  .fa {
    color: $primary-color;
    font-size: $font-size-md;
    width: 32rpx;
    text-align: center;
    flex-shrink: 0;
    margin-top: 2rpx;
  }
}

.meeting-status {
  display: flex;
  justify-content: center;
  margin-top: $spacing-sm;
}

/* ---- Status Chips ---- */
.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 20rpx;
  border-radius: 32rpx;
  font-size: $font-size-sm;
  font-weight: 500;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: currentColor;
}

.status-draft {
  background: #f1f5f9;
  color: #64748b;
}

.status-registering {
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  color: #3b82f6;
  .status-dot { animation: pulse 2s infinite; }
}

.status-ongoing {
  background: linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%);
  color: #10b981;
  .status-dot { animation: pulse 2s infinite; }
}

.status-finished {
  background: #f1f5f9;
  color: #94a3b8;
}

.status-cancelled {
  background: #fef2f2;
  color: #ef4444;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ---- Stats ---- */
.stats-grid {
  padding: $spacing-lg 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 44rpx;
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

/* ---- Actions ---- */
.actions-grid {
  padding: $spacing-lg 0;
}

.action-tile {
  @extend .feature-tile;
}

.action-icon-lg {
  @extend .feature-icon-lg;
  .fa {
    font-size: 48rpx;
    color: $text-white;
  }
}

.action-title {
  @extend .feature-title;
}

/* ---- Intro ---- */
.intro-content {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
