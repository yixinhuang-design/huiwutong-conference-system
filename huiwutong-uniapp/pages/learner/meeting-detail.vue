<template>
  <view class="meeting-detail-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">培训详情</text>
      </view>
    </view>

    <!-- 培训基本信息 -->
    <view class="info-card card">
      <view class="meeting-title">{{ meetingInfo.title }}</view>
      <view class="meeting-meta">
        <text class="meta-item">
          <text class="fa fa-calendar-alt"></text>
          {{ meetingInfo.dateRange }}
        </text>
        <text class="meta-item">
          <text class="fa fa-map-marker-alt"></text>
          {{ meetingInfo.location }}
        </text>
        <text class="meta-item">
          <text class="fa fa-building"></text>
          主办：{{ meetingInfo.organizer }}
        </text>
      </view>
      <view class="meeting-status">
        <view class="status-chip status-accent">
          <text class="status-dot"></text>
          进行中
        </view>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-section card">
      <view class="section-title">培训概况</view>
      <view class="stats-grid grid-2">
        <view class="stat-item">
          <view class="stat-value">{{ meetingInfo.participants }}</view>
          <view class="stat-label">参与人数</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ meetingInfo.days }}</view>
          <view class="stat-label">培训天数</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ meetingInfo.courses }}</view>
          <view class="stat-label">课程数</view>
        </view>
        <view class="stat-item">
          <view class="stat-value">{{ meetingInfo.hours }}</view>
          <view class="stat-label">总学时</view>
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
    <view class="intro-section card">
      <view class="section-title">培训简介</view>
      <view class="intro-content card-content">
        {{ meetingInfo.description }}
      </view>
    </view>

    <!-- 注意事项 -->
    <view class="notice-section card">
      <view class="section-title">注意事项</view>
      <view class="notice-list">
        <view
          v-for="(notice, index) in meetingInfo.notices"
          :key="index"
          class="notice-item"
        >
          <text class="notice-bullet">•</text>
          <text class="notice-text">{{ notice }}</text>
        </view>
      </view>
    </view>

    <!-- 联系方式 -->
    <view class="contact-section card">
      <view class="section-title">联系方式</view>
      <view class="contact-item">
        <text class="contact-label">班主任：</text>
        <text class="contact-value">{{ meetingInfo.teacher }}</text>
      </view>
      <view class="contact-item">
        <text class="contact-label">联系电话：</text>
        <text class="contact-value">{{ meetingInfo.phone }}</text>
      </view>
      <button class="contact-btn btn btn-primary btn-block">
        <text class="fa fa-phone"></text>
        联系班主任
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      meetingId: '',
      meetingInfo: {
        title: '2025党务干部培训班',
        dateRange: '2025年1月15日 - 1月19日',
        location: '市委党校报告厅',
        organizer: '市委组织部',
        participants: 120,
        days: 5,
        courses: 15,
        hours: 40,
        description: '本次培训旨在提高党务干部的政治理论水平和业务能力，深入学习习近平新时代中国特色社会主义思想和党的二十大精神，全面提升党务干部的综合素质。',
        notices: [
          '请提前15分钟到达教室签到',
          '培训期间请佩戴胸牌',
          '请关闭手机或调至静音模式',
          '禁止在教室内吸烟和饮食'
        ],
        teacher: '张老师',
        phone: '138-0000-8888'
      }
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
      this.loadMeetingDetail()
    }
  },

  methods: {
    /**
     * 加载培训详情
     */
    async loadMeetingDetail() {
      try {
        // TODO: 调用API获取培训详情
        // const res = await this.$api.meeting.getDetail(this.meetingId)
      } catch (error) {
        console.error('Load meeting detail error:', error)
      }
    },

    /**
     * 跳转到日程
     */
    goToSchedule() {
      uni.navigateTo({
        url: `/pages/learner/schedule?id=${this.meetingId}`
      })
    },

    /**
     * 跳转到座位
     */
    goToSeat() {
      uni.navigateTo({
        url: `/pages/learner/seat?id=${this.meetingId}`
      })
    },

    /**
     * 跳转到签到
     */
    goToCheckin() {
      uni.navigateTo({
        url: `/pages/learner/checkin?id=${this.meetingId}`
      })
    },

    /**
     * 跳转到资料
     */
    goToMaterials() {
      uni.navigateTo({
        url: `/pages/learner/materials?id=${this.meetingId}`
      })
    },

    /**
     * 跳转到群组
     */
    goToGroups() {
      uni.navigateTo({
        url: `/pages/learner/groups?id=${this.meetingId}`
      })
    },

    /**
     * 跳转到通讯录
     */
    goToContact() {
      uni.navigateTo({
        url: `/pages/learner/contact?id=${this.meetingId}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

/* Font Awesome 图标支持 */
@import url('@/static/fontawesome/css/all.css');

.meeting-detail-container {
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

.info-card,
.stats-section,
.actions-section,
.intro-section,
.notice-section,
.contact-section {
  margin: $spacing-md;
  @extend .card;
}

.meeting-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.meeting-meta {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 8rpx;

  .fa {
    color: $primary-color;
    font-size: $font-size-md;
    width: 32rpx;
    text-align: center;
  }
}

.meeting-status {
  display: flex;
  justify-content: center;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx $spacing-md;
  border-radius: $border-radius-lg;
  font-size: $font-size-sm;
  font-weight: 500;
}

.status-accent {
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
  color: $accent-color;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: currentColor;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
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

.intro-content {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: $line-height-lg;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.notice-item {
  display: flex;
  gap: $spacing-sm;
  font-size: $font-size-sm;
  color: $text-primary;
  line-height: $line-height-md;
}

.notice-bullet {
  flex-shrink: 0;
  color: $primary-color;
  font-size: $font-size-lg;
}

.notice-text {
  flex: 1;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: $spacing-sm 0;
  font-size: $font-size-md;
}

.contact-label {
  color: $text-secondary;
  flex-shrink: 0;
}

.contact-value {
  color: $text-primary;
  font-weight: 500;
}

.contact-btn {
  margin-top: $spacing-md;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;

  .fa {
    font-size: $font-size-md;
  }
}
</style>
