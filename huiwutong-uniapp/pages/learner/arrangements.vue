<template>
  <view class="arrangements-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">辅助安排</text>
        <text class="header-action"></text>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 住宿安排 -->
      <view class="section-card card" v-if="arrangements.accommodation">
        <view class="section-header">
          <text class="section-icon">🏨</text>
          <text class="section-title">住宿安排</text>
        </view>

        <view class="arrangement-info">
          <view class="info-row">
            <text class="row-label">酒店名称</text>
            <text class="row-value">{{ arrangements.accommodation.hotel }}</text>
          </view>
          <view class="info-row">
            <text class="row-label">房间类型</text>
            <text class="row-value">{{ arrangements.accommodation.roomType }}</text>
          </view>
          <view class="info-row">
            <text class="row-label">房间号</text>
            <text class="row-value">{{ arrangements.accommodation.roomNumber }}</text>
          </view>
          <view class="info-row">
            <text class="row-label">入住时间</text>
            <text class="row-value">{{ arrangements.accommodation.checkIn }}</text>
          </view>
          <view class="info-row">
            <text class="row-label">退房时间</text>
            <text class="row-value">{{ arrangements.accommodation.checkOut }}</text>
          </view>
        </view>

        <view class="arrangement-actions">
          <button class="btn btn-outline btn-block btn-sm" @click="callHotel">
            <text class="fa fa-phone"></text> 联系酒店
          </button>
          <button class="btn btn-primary btn-block btn-sm" @click="showHotelMap">
            <text class="fa fa-map-marker-alt"></text> 查看位置
          </button>
        </view>
      </view>

      <!-- 餐饮安排 -->
      <view class="section-card card" v-if="arrangements.dining">
        <view class="section-header">
          <text class="section-icon">🍽️</text>
          <text class="section-title">餐饮安排</text>
        </view>

        <view class="dining-list">
          <view
            v-for="(meal, index) in arrangements.dining.meals"
            :key="index"
            class="meal-item"
          >
            <view class="meal-header">
              <text class="meal-type">{{ meal.type }}</text>
              <text class="meal-time">{{ meal.time }}</text>
            </view>
            <view class="meal-info">
              <text class="meal-location"><text class="fa fa-map-marker-alt"></text> {{ meal.location }}</text>
              <text class="meal-menu" v-if="meal.menu">菜单：{{ meal.menu }}</text>
            </view>
          </view>
        </view>

        <view class="dining-tips">
          <text class="tips-title">💡 用餐须知</text>
          <text class="tips-text">• 请凭学员证用餐</text>
          <text class="tips-text">• 请按指定时间用餐</text>
          <text class="tips-text">• 如有饮食禁忌请联系工作人员</text>
        </view>
      </view>

      <!-- 交通安排 -->
      <view class="section-card card" v-if="arrangements.transportation">
        <view class="section-header">
          <text class="section-icon">🚌</text>
          <text class="section-title">交通安排</text>
        </view>

        <view class="transport-info">
          <view class="info-row">
            <text class="row-label">交通方式</text>
            <text class="row-value">{{ arrangements.transportation.type }}</text>
          </view>
          <view class="info-row" v-if="arrangements.transportation.route">
            <text class="row-label">乘车路线</text>
            <text class="row-value">{{ arrangements.transportation.route }}</text>
          </view>
          <view class="info-row" v-if="arrangements.transportation.pickupTime">
            <text class="row-label">发车时间</text>
            <text class="row-value">{{ arrangements.transportation.pickupTime }}</text>
          </view>
          <view class="info-row" v-if="arrangements.transportation.pickupLocation">
            <text class="row-label">上车地点</text>
            <text class="row-value">{{ arrangements.transportation.pickupLocation }}</text>
          </view>
        </view>

        <button
          v-if="arrangements.transportation.driver"
          class="btn btn-outline btn-block btn-sm"
          @click="callDriver"
        >
          <text class="fa fa-phone"></text> 联系司机
        </button>
      </view>

      <!-- 物资发放 -->
      <view class="section-card card" v-if="arrangements.materials">
        <view class="section-header">
          <text class="section-icon">📦</text>
          <text class="section-title">物资发放</text>
        </view>

        <view class="materials-list">
          <view
            v-for="(item, index) in arrangements.materials.items"
            :key="index"
            class="material-item"
          >
            <view class="material-check" :class="{ received: item.received }">
              <text v-if="item.received" class="check-icon">✓</text>
            </view>
            <view class="material-info">
              <text class="material-name">{{ item.name }}</text>
              <text class="material-desc">{{ item.description }}</text>
            </view>
            <view class="material-status" :class="item.received ? 'received' : 'pending'">
              {{ item.received ? '已领取' : '待领取' }}
            </view>
          </view>
        </view>

        <view class="pickup-info">
          <text class="pickup-label">领取地点</text>
          <text class="pickup-value">{{ arrangements.materials.location }}</text>
          <text class="pickup-time">领取时间：{{ arrangements.materials.pickupTime }}</text>
        </view>
      </view>

      <!-- 联系方式 -->
      <view class="contact-section card">
        <view class="section-header">
          <text class="section-icon"><text class="fa fa-phone"></text></text>
          <text class="section-title">联系方式</text>
        </view>

        <view class="contact-list">
          <view
            v-for="(contact, index) in contacts"
            :key="index"
            class="contact-item"
            @click="makeContact(contact)"
          >
            <view class="contact-avatar">
              {{ contact.name.charAt(contact.name.length - 1) }}
            </view>
            <view class="contact-info">
              <text class="contact-name">{{ contact.name }}</text>
              <text class="contact-role">{{ contact.role }}</text>
            </view>
            <text class="contact-phone">{{ contact.phone }}</text>
          </view>
        </view>
      </view>

      <!-- 温馨提示 -->
      <view class="tips-section card">
        <view class="section-title">💡 温馨提示</view>
        <view class="tips-list">
          <text class="tips-item">• 请妥善保管个人物品</text>
          <text class="tips-item">• 注意人身和财产安全</text>
          <text class="tips-item">• 遇到问题及时联系工作人员</text>
          <text class="tips-item">• 保持手机畅通，注意查收通知</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      arrangements: {
        accommodation: {
          hotel: '市委党校招待所',
          roomType: '标准间',
          roomNumber: '306',
          checkIn: '2025-01-17 14:00',
          checkOut: '2025-01-20 12:00'
        },
        dining: {
          meals: [
            { type: '早餐', time: '07:00-08:00', location: '一楼餐厅', menu: '自助早餐' },
            { type: '午餐', time: '12:00-13:00', location: '一楼餐厅', menu: '套餐' },
            { type: '晚餐', time: '18:00-19:00', location: '一楼餐厅', menu: '自助晚餐' }
          ]
        },
        transportation: {
          type: '统一乘车',
          route: '市委党校 ⇄ 培训会场',
          pickupTime: '07:30',
          pickupLocation: '党校大门口',
          driver: '王师傅',
          phone: '139****8888'
        },
        materials: {
          location: '培训签到处',
          pickupTime: '培训签到时',
          items: [
            { name: '学员手册', description: '培训指南和日程', received: true },
            { name: '学习资料', description: '课程相关资料', received: true },
            { name: '笔记本', description: '记录本', received: false },
            { name: '签字笔', description: '黑色签字笔', received: false },
            { name: '学员证', description: '身份识别', received: true }
          ]
        }
      },
      contacts: [
        { name: '李老师', role: '班主任', phone: '138****0001' },
        { name: '王老师', role: '生活委员', phone: '138****0002' },
        { name: '张老师', role: '学习委员', phone: '138****0003' }
      ]
    }
  },

  methods: {
    callHotel() {
      uni.makePhoneCall({
        phoneNumber: '138****8888'
      })
    },

    showHotelMap() {
      uni.navigateTo({
        url: '/pages/common/navigation'
      })
    },

    callDriver() {
      uni.makePhoneCall({
        phoneNumber: this.arrangements.transportation.phone
      })
    },

    makeContact(contact) {
      uni.showActionSheet({
        itemList: ['拨打电话', '发送消息'],
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.makePhoneCall({
              phoneNumber: contact.phone
            })
          } else {
            uni.showToast({ title: '消息功能开发中', icon: 'none' })
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

.arrangements-container {
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

.content-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.section-card {
  margin-bottom: $spacing-md;
}

.section-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.section-icon {
  font-size: 40rpx;
}

.section-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.arrangement-info,
.transport-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.arrangement-actions {
  display: flex;
  gap: $spacing-sm;
}

.btn-sm {
  padding: $spacing-sm $spacing-md;
  font-size: $font-size-sm;
}

.dining-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.meal-item {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.meal-type {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.meal-time {
  font-size: $font-size-md;
  color: $text-secondary;
}

.meal-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.meal-location {
  font-size: $font-size-md;
  color: $text-secondary;
}

.meal-menu {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.dining-tips {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: rgba(102, 126, 234, 0.05);
  border-radius: $border-radius-md;
}

.tips-title {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $primary-color;
  margin-bottom: $spacing-xs;
}

.tips-text {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.8;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.material-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.material-check {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid $border-color;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.material-check.received {
  background: $primary-color;
  border-color: $primary-color;
}

.check-icon {
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: bold;
}

.material-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.material-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.material-desc {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.material-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
}

.material-status.received {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.material-status.pending {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.pickup-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: rgba(102, 126, 234, 0.05);
  border-radius: $border-radius-md;
}

.pickup-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.pickup-value {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.pickup-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.contact-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-md;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.contact-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.contact-role {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.contact-phone {
  font-size: $font-size-md;
  color: $primary-color;
}

.tips-section {
  margin-bottom: $spacing-lg;
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
