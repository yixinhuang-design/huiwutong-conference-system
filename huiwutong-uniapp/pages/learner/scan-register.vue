<template>
  <view class="scan-register-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">扫码报名</text>
        <text class="header-action"></text>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 扫码区域 -->
      <view class="scan-section card">
        <view class="scan-title">扫描培训二维码</view>
        <view class="scan-desc">请扫描培训通知上的二维码进行报名</view>

        <view class="scan-box" @click="startScan">
          <text class="scan-icon">📷</text>
          <text class="scan-text">点击开始扫描</text>
        </view>

        <button class="btn btn-primary btn-block" @click="startScan">
          📷 启动相机扫描
        </button>

        <view class="scan-tips">
          <text class="tips-title">💡 扫码提示</text>
          <text class="tips-text">• 请确保二维码完整清晰</text>
          <text class="tips-text">• 保持相机稳定，对准二维码</text>
          <text class="tips-text">• 如无法扫描，请手动输入报名码</text>
        </view>
      </view>

      <!-- 分割线 -->
      <view class="divider">
        <text class="divider-text">或手动输入报名码</text>
      </view>

      <!-- 手动输入 -->
      <view class="input-section card">
        <view class="form-item">
          <text class="form-label">报名码</text>
          <input
            v-model="registerCode"
            class="input-field"
            type="text"
            placeholder="请输入6位报名码"
            maxlength="6"
          />
        </view>

        <button class="btn btn-primary btn-block" @click="verifyCode">
          ✓ 验证报名码
        </button>
      </view>

      <!-- 扫描结果 -->
      <view v-if="scanResult" class="result-section card">
        <view class="result-header">
          <text class="result-icon">✓</text>
          <text class="result-title">识别成功</text>
        </view>

        <view class="training-info">
          <view class="info-item">
            <text class="info-label">培训名称</text>
            <text class="info-value">{{ scanResult.title }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">培训时间</text>
            <text class="info-value">{{ scanResult.date }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">培训地点</text>
            <text class="info-value">{{ scanResult.location }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">主办单位</text>
            <text class="info-value">{{ scanResult.organizer }}</text>
          </view>
        </view>

        <button class="btn btn-primary btn-block" @click="goToRegister">
          立即报名
        </button>
      </view>

      <!-- 报名须知 -->
      <view class="notice-section card">
        <view class="section-title"><text class="fa fa-clipboard"></text> 报名须知</view>
        <view class="notice-list">
          <view class="notice-item">
            <text class="notice-dot">•</text>
            <text class="notice-text">请确保填写信息真实准确</text>
          </view>
          <view class="notice-item">
            <text class="notice-dot">•</text>
            <text class="notice-text">报名成功后请等待审核</text>
          </view>
          <view class="notice-item">
            <text class="notice-dot">•</text>
            <text class="notice-text">审核通过后将收到通知</text>
          </view>
          <view class="notice-item">
            <text class="notice-dot">•</text>
            <text class="notice-text">如有疑问请联系主办方</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      registerCode: '',
      scanResult: null
    }
  },

  methods: {
    startScan() {
      uni.scanCode({
        success: (res) => {
          console.log('扫码结果:', res)

          const code = res.result
          this.processScanResult(code)
        },
        fail: (err) => {
          console.error('扫码失败:', err)
          uni.showToast({
            title: '扫码失败，请重试',
            icon: 'none'
          })
        }
      })
    },

    processScanResult(code) {
      uni.showLoading({ title: '识别中...' })

      setTimeout(() => {
        uni.hideLoading()

        this.scanResult = {
          title: '2025年度第一期干部综合素质提升培训',
          date: '2025年1月18日-20日',
          location: '市委党校报告厅',
          organizer: '市委组织部',
          code: code
        }

        uni.showToast({
          title: '识别成功',
          icon: 'success'
        })
      }, 1000)
    },

    verifyCode() {
      if (!this.registerCode) {
        uni.showToast({
          title: '请输入报名码',
          icon: 'none'
        })
        return
      }

      if (this.registerCode.length !== 6) {
        uni.showToast({
          title: '报名码格式不正确',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '验证中...' })

      setTimeout(() => {
        uni.hideLoading()

        this.scanResult = {
          title: '2025年度第一期干部综合素质提升培训',
          date: '2025年1月18日-20日',
          location: '市委党校报告厅',
          organizer: '市委组织部',
          code: this.registerCode
        }

        uni.showToast({
          title: '验证成功',
          icon: 'success'
        })
      }, 1000)
    },

    goToRegister() {
      if (!this.scanResult) return

      uni.navigateTo({
        url: `/pages/learner/registration-form?trainingId=${this.scanResult.code}`
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

.scan-register-container {
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

.scan-section {
  margin-bottom: $spacing-md;
}

.scan-title {
  display: block;
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  text-align: center;
  margin-bottom: $spacing-sm;
}

.scan-desc {
  display: block;
  font-size: $font-size-md;
  color: $text-secondary;
  text-align: center;
  margin-bottom: $spacing-lg;
}

.scan-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;
  padding: 80rpx $spacing-lg;
  background: $bg-tertiary;
  border: 2rpx dashed $border-color;
  border-radius: $border-radius-lg;
  margin-bottom: $spacing-lg;
}

.scan-icon {
  font-size: 120rpx;
}

.scan-text {
  font-size: $font-size-md;
  color: $text-tertiary;
}

.scan-tips {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: rgba(102, 126, 234, 0.05);
  border-radius: $border-radius-md;
  margin-top: $spacing-lg;
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

.divider {
  display: flex;
  align-items: center;
  margin: $spacing-xl 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1rpx;
  background: $border-color;
}

.divider-text {
  padding: 0 $spacing-lg;
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.input-section {
  margin-bottom: $spacing-md;
}

.form-item {
  margin-bottom: $spacing-lg;
}

.form-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.form-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-lg;
  text-align: center;
  letter-spacing: 4rpx;
}

.result-section {
  margin-bottom: $spacing-md;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.result-icon {
  font-size: 48rpx;
  color: #10b981;
}

.result-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: #10b981;
}

.training-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
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

.notice-section {
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.notice-item {
  display: flex;
  gap: $spacing-sm;
}

.notice-dot {
  color: $primary-color;
  font-size: $font-size-lg;
}

.notice-text {
  flex: 1;
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
}
</style>
