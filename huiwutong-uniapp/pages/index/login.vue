<template>
  <view class="login-page login-container">
    <view class="login-card">
      <!-- Logo区域 -->
      <view class="login-logo">
        <text class="logo-icon"><text class="fa fa-bullseye"></text></text>
      </view>
      <view class="login-title">智能会议助手</view>
      <view class="login-subtitle">欢迎登录智能会议系统</view>

      <!-- 登录方式切换Tab -->
      <view class="tab-group">
        <view
          class="tab-item"
          :class="{ active: loginMode === 'sms' }"
          @click="switchLoginMode('sms')"
        >
          验证码登录
        </view>
        <view
          class="tab-item"
          :class="{ active: loginMode === 'password' }"
          @click="switchLoginMode('password')"
        >
          密码登录
        </view>
      </view>

      <!-- ======== 验证码登录表单 ======== -->
      <view v-if="loginMode === 'sms'" class="login-form">
        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-icon"><text class="fa fa-mobile-alt"></text></text>
            <input
              v-model="formData.phone"
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
              class="login-input input-with-icon"
            />
          </view>
        </view>

        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-icon"><text class="fa fa-shield-alt"></text></text>
            <input
              v-model="formData.smsCode"
              placeholder="请输入验证码"
              type="number"
              maxlength="6"
              class="login-input input-with-icon input-with-btn"
            />
            <button
              class="code-btn"
              :disabled="countdown > 0 || smsSending"
              @click="sendSmsCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </button>
          </view>
        </view>

        <!-- 租户切换（可选） -->
        <view class="form-group" v-if="showTenantInput">
          <view class="input-wrapper">
            <text class="input-icon"><text class="fa fa-building"></text></text>
            <input
              v-model="formData.tenantCode"
              placeholder="租户代码（默认DEFAULT）"
              class="login-input input-with-icon"
            />
          </view>
        </view>

        <view class="form-options">
          <view class="form-options-left"></view>
          <text class="tenant-toggle" @click="showTenantInput = !showTenantInput">
            {{ showTenantInput ? '隐藏租户' : '切换租户' }}
          </text>
        </view>
      </view>

      <!-- ======== 密码登录表单 ======== -->
      <view v-else class="login-form">
        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-icon"><text class="fa fa-user"></text></text>
            <input
              v-model="formData.username"
              placeholder="请输入手机号/用户名"
              class="login-input input-with-icon"
            />
          </view>
        </view>

        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-icon"><text class="fa fa-lock"></text></text>
            <input
              v-model="formData.password"
              :password="!showPassword"
              placeholder="请输入密码"
              class="login-input input-with-icon"
            />
            <text
              class="password-toggle"
              @touchend.prevent="showPassword = !showPassword"
              @click="showPassword = !showPassword"
            >
              <text v-if="showPassword" class="fa fa-eye"></text>
              <text v-else class="fa fa-eye-slash"></text>
            </text>
          </view>
        </view>

        <!-- 租户代码(可选) -->
        <view class="form-group" v-if="showTenantInput">
          <view class="input-wrapper">
            <text class="input-icon"><text class="fa fa-building"></text></text>
            <input
              v-model="formData.tenantCode"
              placeholder="租户代码（默认DEFAULT）"
              class="login-input input-with-icon"
            />
          </view>
        </view>

        <view class="form-options">
          <view class="checkbox-group">
            <checkbox
              :checked="rememberPassword"
              @click="rememberPassword = !rememberPassword"
              color="#667eea"
              style="transform: scale(0.8)"
            />
            <text class="checkbox-label">记住密码</text>
          </view>
          <text class="tenant-toggle" @click="showTenantInput = !showTenantInput">
            {{ showTenantInput ? '隐藏租户' : '切换租户' }}
          </text>
        </view>
      </view>

      <!-- 登录按钮 -->
      <button
        class="login-button"
        :disabled="loading"
        @click="handleLogin"
      >
        {{ loading ? '登录中...' : '登录' }}
      </button>

      <!-- 底部提示 -->
      <view class="login-tips">
        <text v-if="loginMode === 'sms'">开发模式验证码：123456</text>
        <text v-else>首次登录请使用验证码，登录后可在设置中设置密码</text>
      </view>
      <view class="login-tips">
        <text>登录即表示同意《用户协议》和《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script>
import { useUserStore } from '@/store/modules/user'

export default {
  data() {
    return {
      loginMode: 'sms', // sms | password (默认验证码登录)
      showPassword: false,
      rememberPassword: false,
      showTenantInput: false,
      loading: false,
      smsSending: false,
      countdown: 0,
      formData: {
        tenantCode: 'DEFAULT',
        phone: '',
        smsCode: '',
        username: '',
        password: ''
      }
    }
  },

  onLoad() {
    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      this.redirectByRole()
    }
    this.loadRememberedData()
  },

  methods: {
    /**
     * 切换登录方式
     */
    switchLoginMode(mode) {
      this.loginMode = mode
    },

    /**
     * 发送短信验证码
     */
    async sendSmsCode() {
      const phone = this.formData.phone
      if (!phone) {
        uni.showToast({ title: '请输入手机号', icon: 'none' })
        return
      }
      if (!/^1[3-9]\d{9}$/.test(phone)) {
        uni.showToast({ title: '手机号格式不正确', icon: 'none' })
        return
      }

      this.smsSending = true
      try {
        const userStore = useUserStore()
        await userStore.sendSmsCode(phone)
        uni.showToast({ title: '验证码已发送', icon: 'success' })
        // 开始倒计时
        this.countdown = 60
        const timer = setInterval(() => {
          this.countdown--
          if (this.countdown <= 0) clearInterval(timer)
        }, 1000)
      } catch (error) {
        uni.showToast({ title: error.message || '发送失败，请稍后重试', icon: 'none' })
      } finally {
        this.smsSending = false
      }
    },

    /**
     * 表单验证
     */
    validateForm() {
      if (this.loginMode === 'sms') {
        if (!this.formData.phone) {
          uni.showToast({ title: '请输入手机号', icon: 'none' })
          return false
        }
        if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
          uni.showToast({ title: '手机号格式不正确', icon: 'none' })
          return false
        }
        if (!this.formData.smsCode) {
          uni.showToast({ title: '请输入验证码', icon: 'none' })
          return false
        }
      } else {
        if (!this.formData.username) {
          uni.showToast({ title: '请输入手机号/用户名', icon: 'none' })
          return false
        }
        if (!this.formData.password) {
          uni.showToast({ title: '请输入密码', icon: 'none' })
          return false
        }
      }
      return true
    },

    /**
     * 处理登录
     */
    async handleLogin() {
      if (!this.validateForm()) return
      this.loading = true

      try {
        const userStore = useUserStore()
        const tenantCode = this.formData.tenantCode || 'DEFAULT'

        if (this.loginMode === 'sms') {
          await userStore.smsLogin({
            phone: this.formData.phone,
            smsCode: this.formData.smsCode,
            tenantCode
          })
        } else {
          await userStore.login({
            username: this.formData.username,
            password: this.formData.password,
            tenantCode
          })
          // 密码登录记住密码
          if (this.rememberPassword) {
            this.saveRememberedData()
          } else {
            uni.removeStorageSync('rememberedLogin')
          }
        }

        // 登录成功
        const welcomeName = userStore.realName || userStore.username
        uni.showToast({ title: `欢迎，${welcomeName}`, icon: 'success', duration: 1500 })

        setTimeout(() => {
          this.redirectByRole()
        }, 1500)
      } catch (error) {
        console.error('Login error:', error)
        uni.showToast({
          title: error.message || '登录失败，请检查输入信息',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    /**
     * 根据角色跳转首页
     */
    redirectByRole() {
      uni.switchTab({ url: '/pages/common/home' })
    },

    /**
     * 保存密码登录信息
     */
    saveRememberedData() {
      const data = {
        username: this.formData.username,
        password: this.formData.password,
        tenantCode: this.formData.tenantCode
      }
      uni.setStorageSync('rememberedLogin', JSON.stringify(data))
    },

    /**
     * 加载记住的登录信息
     */
    loadRememberedData() {
      const saved = uni.getStorageSync('rememberedLogin')
      if (saved) {
        try {
          const data = JSON.parse(saved)
          this.formData.username = data.username || ''
          this.formData.password = data.password || ''
          this.formData.tenantCode = data.tenantCode || 'DEFAULT'
          this.rememberPassword = true
          if (data.tenantCode && data.tenantCode !== 'DEFAULT') {
            this.showTenantInput = true
          }
        } catch (e) {
          console.error('Load remembered data error:', e)
        }
      }
    }
  }
}
</script>

<style lang="scss">
@import '../../styles/variables.scss';

/* ========================================
   登录页输入框层级体系
   ========================================
   层级说明：
   - z-index: 1  -> input-wrapper（容器）
   - z-index: 10 -> login-input, uni-input-input（输入框）
   - z-index: 15 -> input-icon（图标，在输入框之上，不阻挡点击）
   - z-index: 10 -> password-toggle, code-btn（按钮）

   事件传递：
   - pointer-events: none  -> 图标、placeholder
   - pointer-events: auto  -> 输入框、按钮
   ======================================== */

.login-page {
  min-height: 100vh;
  background: $primary-gradient;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl;
}

.login-page .login-card {
  background: $bg-primary;
  border-radius: 40rpx; /* 匹配app原型的20px */
  padding: 64rpx 48rpx;
  box-shadow: $shadow-lg;
  width: 100%;
}

.login-page .login-logo {
  text-align: center;
  margin-bottom: $spacing-lg;
}

.login-page .logo-icon {
  font-size: 112rpx;
  display: block;
}

.login-page .login-title {
  font-size: $font-size-xxl;
  font-weight: 600;
  text-align: center;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.login-page .login-subtitle {
  font-size: $font-size-md;
  text-align: center;
  color: $text-secondary;
  margin-bottom: $spacing-xl;
}

.login-page .tab-group {
  display: flex;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  padding: 8rpx;
  margin-bottom: $spacing-lg;
}

.login-page .tab-item {
  flex: 1;
  padding: $spacing-md;
  text-align: center;
  border-radius: $border-radius-sm;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-secondary;
  transition: $transition-base;
}

.login-page .tab-item.active {
  background: $bg-primary;
  color: $primary-color;
  box-shadow: $shadow-xs;
}

.login-page .login-mode-group {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.login-page .mode-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  padding: $spacing-sm 0;
  border-bottom: 4rpx solid transparent;
  transition: $transition-base;
}

.login-page .mode-item.active {
  color: $primary-color;
  border-bottom-color: $primary-color;
}

.login-page .form-group {
  margin-bottom: $spacing-md;
}

/* 容器层级：z-index: 1 */
.login-page .input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  z-index: 1;
}

/* 图标层级：z-index: 15，在输入框之上，但不阻挡点击事件 */
.login-page .input-icon {
  position: absolute;
  left: 32rpx; /* 匹配app原型的16px */
  top: 50%;
  transform: translateY(-50%);
  color: $text-tertiary;
  font-size: $font-size-lg;
  z-index: 15;
  pointer-events: none;
}

/* 输入框层级：z-index: 10，确保可点击 */
.login-page .login-input {
  width: 100%;
  padding: 28rpx 32rpx 28rpx 96rpx; /* 匹配app原型的14px 16px 14px 48px */
  border: 2rpx solid $border-color;
  border-radius: 24rpx; /* 匹配app原型的12px */
  font-size: $font-size-md;
  transition: $transition-base;
  background: $bg-primary;
  color: $text-primary;
  position: relative;
  z-index: 10;

  /* #ifdef H5 */
  outline: none;
  cursor: text;
  -webkit-appearance: none;
  pointer-events: auto;
  /* #endif */
}

/* 带图标的输入框：左侧留出图标空间 */
.login-page .input-with-icon {
  padding-left: 96rpx; /* 与图标的 left + 图标宽度对齐 */
}

/* 带右侧按钮的输入框：右侧留出按钮空间 */
.login-page .input-with-btn {
  padding-right: 220rpx;
}

.login-page .form-options-left {
  flex: 1;
}

/* #ifdef H5 */
/* UniApp H5 input 容器 */
.login-page uni-input {
  display: block;
  width: 100%;
  font-size: inherit;
}

/* UniApp input 包装器 */
.login-page .uni-input-wrapper {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  flex-direction: column;
  justify-content: center;
  pointer-events: auto;
}

/* placeholder 不阻挡点击 */
.login-page .uni-input-placeholder {
  pointer-events: none;
}

/* UniApp H5 input 组件实际渲染的 input 元素类名是 uni-input-input */
.login-page .uni-input-input {
  padding: 28rpx 32rpx 28rpx 96rpx;
  border: none;
  background: transparent;
  color: $text-primary;
  outline: none;
  cursor: text;
  -webkit-appearance: none;
  min-height: 1.4em;
  position: relative;
  z-index: 10;
  pointer-events: auto;
  -webkit-user-select: text;
  user-select: text;
}

.login-page .uni-input-input:focus {
  outline: none;
}

.login-page .uni-input-input::-webkit-input-placeholder {
  color: $text-placeholder;
}

.login-page .uni-input-input:-moz-placeholder {
  color: $text-placeholder;
}

.login-page .uni-input-input::-moz-placeholder {
  color: $text-placeholder;
}

.login-page .uni-input-input:-ms-input-placeholder {
  color: $text-placeholder;
}
/* #endif */

.login-page .login-input:focus {
  border-color: $primary-color;
  box-shadow: 0 0 0 6rpx rgba(102, 126, 234, 0.1);
  /* #ifdef H5 */
  outline: none;
  /* #endif */
}

.login-page .password-toggle {
  position: absolute;
  right: $spacing-md;
  font-size: $font-size-lg;
  padding: $spacing-sm;
  cursor: pointer;
  z-index: 10;
  /* #ifdef H5 */
  pointer-events: auto;
  user-select: none;
  /* #endif */
}

.login-page .code-btn {
  position: absolute;
  right: $spacing-sm;
  top: 50%;
  transform: translateY(-50%);
  background: $primary-color;
  color: $text-white;
  border: none;
  padding: 12rpx 24rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-sm;
  line-height: 1;
  z-index: 10;
  /* #ifdef H5 */
  pointer-events: auto;
  user-select: none;
  /* #endif */
}

.login-page .code-btn:disabled {
  background: $text-tertiary;
  opacity: 0.6;
}

.login-page .checkbox-group {
  display: flex;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.login-page .checkbox-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-left: $spacing-xs;
}

.login-page .form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.login-page .tenant-toggle {
  font-size: $font-size-sm;
  color: $primary-color;
  cursor: pointer;
}

.login-page .login-hint {
  text-align: center;
  margin-bottom: $spacing-lg;
}

.login-page .hint-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.login-page .login-button {
  width: 100%;
  padding: 32rpx; /* 匹配app原型的16px */
  background: $primary-gradient;
  color: $text-white;
  border: none;
  border-radius: 24rpx; /* 匹配app原型的12px */
  font-size: 32rpx; /* 匹配app原型的16px */
  font-weight: 600;
  margin-top: 16rpx; /* 匹配app原型的8px */
  transition: $transition-base;
}

.login-page .login-button:active {
  transform: translateY(-4rpx);
  box-shadow: $shadow-md;
}

.login-page .login-button:disabled {
  opacity: 0.6;
}

.login-page .login-tips {
  text-align: center;
  margin-top: $spacing-lg;
  font-size: $font-size-xs;
  color: $text-tertiary;
  line-height: 1.6;
}
</style>
