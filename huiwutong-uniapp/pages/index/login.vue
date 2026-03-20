<template>
  <view class="login-page login-container">
    <view class="login-card">
      <!-- Logo区域 -->
      <view class="login-logo">
        <text class="logo-icon"><text class="fa fa-bullseye"></text></text>
      </view>
      <view class="login-title">智能会议助手</view>
      <view class="login-subtitle">欢迎登录智能会议系统</view>

      <!-- 身份切换Tab -->
      <view class="tab-group">
        <view
          class="tab-item"
          :class="{ active: userType === 'learner' }"
          @click="switchUserType('learner')"
        >
          学员登录
        </view>
        <view
          class="tab-item"
          :class="{ active: userType === 'staff' }"
          @click="switchUserType('staff')"
        >
          工作人员
        </view>
      </view>

      <!-- 登录方式切换 -->
      <view class="login-mode-group">
        <view
          class="mode-item"
          :class="{ active: loginMode === 'password' }"
          @click="switchLoginMode('password')"
        >
          密码登录
        </view>
        <view
          class="mode-item"
          :class="{ active: loginMode === 'sms' }"
          @click="switchLoginMode('sms')"
        >
          验证码登录
        </view>
      </view>

      <!-- 账号密码登录表单 -->
      <view v-if="loginMode === 'password'" class="login-form">
        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-icon"><text class="fa fa-user"></text></text>
            <input
              v-model="formData.username"
              placeholder="请输入用户名/手机号"
              class="input-field input-with-icon"
              @focus="onFocus"
              @blur="onBlur"
            />
          </view>
        </view>

        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-icon">🔒</text>
            <input
              v-model="formData.password"
              :password="!showPassword"
              placeholder="请输入密码"
              class="input-field input-with-icon"
              @focus="onFocus"
              @blur="onBlur"
            />
            <text
              class="password-toggle"
              @touchend.prevent="showPassword = !showPassword"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? '👁️' : '👁️‍🗨️' }}
            </text>
          </view>
        </view>

        <view class="checkbox-group">
          <checkbox
            :checked="rememberPassword"
            @click="toggleRemember"
            color="#667eea"
            style="transform: scale(0.8)"
          />
          <text class="checkbox-label">记住密码</text>
        </view>
      </view>

      <!-- 验证码登录表单 -->
      <view v-else class="login-form">
        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-icon">📱</text>
            <input
              v-model="formData.phone"
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
              class="input-field input-with-icon"
              @focus="onFocus"
              @blur="onBlur"
            />
          </view>
        </view>

        <view class="form-group">
          <view class="input-wrapper">
            <text class="input-icon"><text class="fa fa-comments"></text></text>
            <input
              v-model="formData.smsCode"
              placeholder="请输入验证码"
              type="number"
              maxlength="6"
              class="input-field input-with-icon"
              @focus="onFocus"
              @blur="onBlur"
            />
            <button
              class="code-btn"
              :disabled="countdown > 0"
              @touchend.prevent="sendSmsCode"
              @click="sendSmsCode"
            >
              {{ countdown > 0 ? `${countdown}秒` : '获取验证码' }}
            </button>
          </view>
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
      userType: 'learner', // learner | staff
      loginMode: 'password', // password | sms
      showPassword: false,
      rememberPassword: false,
      countdown: 0,
      loading: false,
      formData: {
        username: '',
        password: '',
        phone: '',
        smsCode: ''
      }
    }
  },

  onLoad() {
    // 检查是否已登录
    const userStore = useUserStore()
    if (userStore.isLoggedIn) {
      this.redirectToHome()
    }

    // 恢复记住的密码
    this.loadRememberedPassword()
  },

  methods: {
    /**
     * 输入框获取焦点
     */
    onFocus(e) {
      console.log('Input focused:', e)
    },

    /**
     * 输入框失去焦点
     */
    onBlur(e) {
      console.log('Input blurred:', e)
    },

    /**
     * 切换用户类型
     */
    switchUserType(type) {
      this.userType = type
    },

    /**
     * 切换登录方式
     */
    switchLoginMode(mode) {
      this.loginMode = mode
    },

    /**
     * 切换记住密码
     */
    toggleRemember() {
      this.rememberPassword = !this.rememberPassword
    },

    /**
     * 发送验证码
     */
    sendSmsCode() {
      if (!this.formData.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return
      }

      if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
        uni.showToast({
          title: '手机号格式不正确',
          icon: 'none'
        })
        return
      }

      // TODO: 调用发送验证码API
      uni.showToast({
        title: '验证码已发送',
        icon: 'success'
      })

      // 开始倒计时
      this.countdown = 60
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    },

    /**
     * 表单验证
     */
    validateForm() {
      if (this.loginMode === 'password') {
        if (!this.formData.username) {
          uni.showToast({
            title: '请输入用户名',
            icon: 'none'
          })
          return false
        }
        if (!this.formData.password) {
          uni.showToast({
            title: '请输入密码',
            icon: 'none'
          })
          return false
        }
      } else {
        if (!this.formData.phone) {
          uni.showToast({
            title: '请输入手机号',
            icon: 'none'
          })
          return false
        }
        if (!this.formData.smsCode) {
          uni.showToast({
            title: '请输入验证码',
            icon: 'none'
          })
          return false
        }
      }
      return true
    },

    /**
     * 处理登录
     */
    async handleLogin() {
      // 1. 表单验证
      if (!this.validateForm()) return

      this.loading = true

      try {
        // 2. 准备登录数据
        const loginData = {
          userType: this.userType
        }

        if (this.loginMode === 'password') {
          loginData.username = this.formData.username
          loginData.password = this.formData.password
        } else {
          loginData.phone = this.formData.phone
          loginData.smsCode = this.formData.smsCode
        }

        // 3. 调用登录接口
        const userStore = useUserStore()
        await userStore.login(loginData)

        // 4. 记住密码
        if (this.rememberPassword && this.loginMode === 'password') {
          this.savePassword()
        }

        // 5. 登录成功提示
        uni.showToast({
          title: '登录成功',
          icon: 'success',
          duration: 1500
        })

        // 6. 跳转到首页
        setTimeout(() => {
          this.redirectToHome()
        }, 1500)

      } catch (error) {
        console.error('Login error:', error)
        uni.showToast({
          title: error.message || '登录失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    /**
     * 跳转到首页
     */
    redirectToHome() {
      uni.switchTab({
        url: '/pages/common/home'
      })
    },

    /**
     * 保存密码
     */
    savePassword() {
      const passwordData = {
        username: this.formData.username,
        password: this.formData.password
      }
      uni.setStorageSync('rememberedPassword', JSON.stringify(passwordData))
    },

    /**
     * 加载记住的密码
     */
    loadRememberedPassword() {
      const saved = uni.getStorageSync('rememberedPassword')
      if (saved) {
        try {
          const passwordData = JSON.parse(saved)
          this.formData.username = passwordData.username
          this.formData.password = passwordData.password
          this.rememberPassword = true
        } catch (e) {
          console.error('Load remembered password error:', e)
        }
      }
    }
  }
}
</script>

<style lang="scss">
@import '../../styles/variables.scss';

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
  border-radius: $border-radius-xl;
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

.login-page .input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.login-page .input-icon {
  position: absolute;
  left: $spacing-md;
  color: $text-tertiary;
  font-size: $font-size-lg;
  z-index: 0;
  pointer-events: none;
}

/* #ifdef H5 */
.login-page .input-wrapper {
  z-index: 1;
}

.login-page .input-icon {
  z-index: 0;
}
/* #endif */

/* #ifdef H5 */
/* UniApp H5 input 容器 */
.login-page uni-input {
  display: block !important;
  width: 100% !important;
  font-size: inherit !important;
}

/* UniApp input 包装器 */
.login-page .uni-input-wrapper {
  position: relative !important;
  display: flex !important;
  width: 100% !important;
  height: 100% !important;
  flex-direction: column !important;
  justify-content: center !important;
}
/* #endif */

.login-page .input-field {
  width: 100%;
  padding: $spacing-md;
  padding-left: 88rpx;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  transition: $transition-base;
  background: $bg-primary;
  color: $text-primary;
  position: relative;
  z-index: 1;
  /* #ifdef H5 */
  outline: none;
  cursor: text;
  -webkit-appearance: none;
  /* #endif */
}

/* #ifdef H5 */
/* UniApp H5 input 组件实际渲染的 input 元素类名是 uni-input-input */
.login-page .uni-input-input {
  padding: $spacing-md !important;
  padding-left: 88rpx !important;
  border: 2rpx solid $border-color !important;
  border-radius: $border-radius-md !important;
  background: $bg-primary !important;
  color: $text-primary !important;
  outline: none !important;
  cursor: text !important;
  -webkit-appearance: none !important;
  min-height: 1.4em !important;
}

.login-page .uni-input-input:focus {
  border-color: $primary-color !important;
  box-shadow: 0 0 0 6rpx rgba(102, 126, 234, 0.1) !important;
  outline: none !important;
}

.login-page .uni-input-input::-webkit-input-placeholder {
  color: $text-placeholder !important;
}

.login-page .uni-input-input:-moz-placeholder {
  color: $text-placeholder !important;
}

.login-page .uni-input-input::-moz-placeholder {
  color: $text-placeholder !important;
}

.login-page .uni-input-input:-ms-input-placeholder {
  color: $text-placeholder !important;
}
/* #endif */

.login-page .input-field:focus {
  border-color: $primary-color;
  box-shadow: 0 0 0 6rpx rgba(102, 126, 234, 0.1);
  /* #ifdef H5 */
  outline: none !important;
  /* #endif */
}

.login-page .input-with-icon {
  padding-left: 88rpx;
}

.login-page .password-toggle {
  position: absolute;
  right: $spacing-md;
  font-size: $font-size-lg;
  padding: $spacing-sm;
  cursor: pointer;
  z-index: 3;
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
  z-index: 3;
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

.login-page .login-button {
  width: 100%;
  padding: $spacing-md;
  background: $primary-gradient;
  color: $text-white;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-lg;
  font-weight: 600;
  margin-top: $spacing-sm;
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
