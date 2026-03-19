<template>
  <view class="login-container">
    <view class="login-card">
      <view class="login-logo">🎓</view>
      <view class="login-title">智能会议系统</view>
      <view class="login-subtitle">欢迎使用 · 登录或注册</view>

      <view class="tab-group">
        <view class="tab-item" :class="{ active: activeTab === 'login' }" @tap="switchTab('login')">登录</view>
        <view class="tab-item" :class="{ active: activeTab === 'register' }" @tap="switchTab('register')">注册</view>
        <view class="tab-item" :class="{ active: activeTab === 'code' }" @tap="switchTab('code')">验证码登录</view>
      </view>

      <!-- 登录表单 -->
      <view v-if="activeTab === 'login'" class="form-container">
        <view class="form-group">
          <view class="form-label">用户名</view>
          <input v-model="loginForm.username" type="text" placeholder="请输入用户名" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">密码</view>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" class="form-input" />
        </view>
        <view class="checkbox-group">
          <checkbox v-model="loginForm.remember" />
          <view class="checkbox-label">记住密码</view>
        </view>
        <button class="login-button" :disabled="loginLoading" @tap="handleLogin">
          {{ loginLoading ? '登录中...' : '登录' }}
        </button>
      </view>

      <!-- 注册表单 -->
      <view v-if="activeTab === 'register'" class="form-container">
        <view class="form-group">
          <view class="form-label">姓名 <text class="required">*</text></view>
          <input v-model="registerForm.name" type="text" placeholder="请输入真实姓名" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">手机号 <text class="required">*</text></view>
          <input v-model="registerForm.phone" type="tel" placeholder="请输入手机号" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">验证码 <text class="required">*</text></view>
          <view class="code-input-wrapper">
            <input v-model="registerForm.code" type="text" placeholder="请输入验证码" class="form-input" />
            <button class="code-btn" :disabled="codeCountdown > 0" @tap="sendCode">
              {{ codeCountdown > 0 ? codeCountdown + 's' : '获取验证码' }}
            </button>
          </view>
        </view>
        <view class="form-group">
          <view class="form-label">密码 <text class="required">*</text></view>
          <input v-model="registerForm.password" type="password" placeholder="请设置密码（6-20位）" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">单位</view>
          <input v-model="registerForm.unit" type="text" placeholder="请输入单位名称" class="form-input" />
        </view>
        <view class="checkbox-group">
          <checkbox v-model="registerForm.agree" />
          <view class="checkbox-label">我已阅读并同意用户协议和隐私政策</view>
        </view>
        <button class="login-button" :disabled="registerLoading" @tap="handleRegister">
          {{ registerLoading ? '注册中...' : '注册' }}
        </button>
      </view>

      <!-- 验证码登录表单 -->
      <view v-if="activeTab === 'code'" class="form-container">
        <view class="form-group">
          <view class="form-label">手机号</view>
          <input v-model="codeLoginForm.phone" type="tel" placeholder="请输入手机号" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">验证码</view>
          <view class="code-input-wrapper">
            <input v-model="codeLoginForm.code" type="text" placeholder="请输入验证码" class="form-input" />
            <button class="code-btn" :disabled="codeCountdown > 0" @tap="sendCode">
              {{ codeCountdown > 0 ? codeCountdown + 's' : '获取验证码' }}
            </button>
          </view>
        </view>
        <button class="login-button" :disabled="codeLoginLoading" @tap="handleCodeLogin">
          {{ codeLoginLoading ? '登录中...' : '登录' }}
        </button>
      </view>

      <view class="login-footer">
        <text class="footer-link" @tap="showForgotPassword">忘记密码？</text>
        <text style="margin: 0 8px; color: #cbd5e1;">|</text>
        <text class="footer-link" @tap="showHelp">登录帮助</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'

const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)
const codeLoginLoading = ref(false)
const codeCountdown = ref(0)

const loginForm = reactive({ username: 'admin', password: '123456', remember: false })
const registerForm = reactive({ name: '', phone: '', code: '', password: '', unit: '', agree: false })
const codeLoginForm = reactive({ phone: '', code: '' })

const switchTab = (tab) => { activeTab.value = tab }

const sendCode = () => {
  if (codeCountdown.value > 0) return
  const phone = activeTab.value === 'register' ? registerForm.phone : codeLoginForm.phone
  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  codeCountdown.value = 60
  const timer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
  uni.showToast({ title: '验证码已发送', icon: 'success' })
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loginLoading.value = true
  try {
    const response = await new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8081/api/auth/login',
        method: 'POST',
        data: { username: loginForm.username, password: loginForm.password },
        success: (res) => resolve(res),
        fail: (err) => reject(err)
      })
    })
    if (response.statusCode === 200 && response.data.code === 200) {
      uni.setStorageSync('auth_token', response.data.data.token)
      uni.setStorageSync('user_info', JSON.stringify(response.data.data.user))
      uni.setStorageSync('tenant_id', '2027317834622709762')
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => { uni.reLaunch({ url: '/pages/common/index/index' }) }, 500)
    } else {
      uni.showToast({ title: response.data.message || '登录失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: '登录出错', icon: 'none' })
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.name || !registerForm.phone || !registerForm.code || !registerForm.password) {
    uni.showToast({ title: '请填写所有必填项', icon: 'none' })
    return
  }
  if (!registerForm.agree) {
    uni.showToast({ title: '请阅读并同意用户协议', icon: 'none' })
    return
  }
  if (registerForm.password.length < 6 || registerForm.password.length > 20) {
    uni.showToast({ title: '密码长度应为6-20位', icon: 'none' })
    return
  }
  registerLoading.value = true
  try {
    const response = await new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8081/api/auth/register',
        method: 'POST',
        data: { name: registerForm.name, phone: registerForm.phone, password: registerForm.password, unit: registerForm.unit, code: registerForm.code },
        success: (res) => resolve(res),
        fail: (err) => reject(err)
      })
    })
    if (response.statusCode === 200 && response.data.code === 200) {
      uni.showToast({ title: '注册成功', icon: 'success' })
      setTimeout(() => { activeTab.value = 'login' }, 500)
    } else {
      uni.showToast({ title: response.data.message || '注册失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: '注册出错', icon: 'none' })
  } finally {
    registerLoading.value = false
  }
}

const handleCodeLogin = async () => {
  if (!codeLoginForm.phone || !codeLoginForm.code) {
    uni.showToast({ title: '请输入手机号和验证码', icon: 'none' })
    return
  }
  codeLoginLoading.value = true
  try {
    const response = await new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8081/api/auth/loginByCode',
        method: 'POST',
        data: { phone: codeLoginForm.phone, code: codeLoginForm.code },
        success: (res) => resolve(res),
        fail: (err) => reject(err)
      })
    })
    if (response.statusCode === 200 && response.data.code === 200) {
      uni.setStorageSync('auth_token', response.data.data.token)
      uni.setStorageSync('user_info', JSON.stringify(response.data.data.user))
      uni.setStorageSync('tenant_id', '2027317834622709762')
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => { uni.reLaunch({ url: '/pages/common/index/index' }) }, 500)
    } else {
      uni.showToast({ title: response.data.message || '登录失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: '登录出错', icon: 'none' })
  } finally {
    codeLoginLoading.value = false
  }
}

const showForgotPassword = () => {
  uni.showModal({ title: '忘记密码', content: '请通过验证码登录或联系系统管理员重置密码', showCancel: false, confirmText: '我知道了' })
}

const showHelp = () => {
  uni.showModal({ title: '登录帮助', content: '常见问题：\n\n1. 忘记密码？\n可以使用验证码登录或联系管理员\n\n2. 无法接收验证码？\n请检查手机号是否正确', showCancel: false, confirmText: '我知道了' })
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.login-card {
  background: white;
  border-radius: 20px;
  padding: 32px 24px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-logo {
  text-align: center;
  margin-bottom: 24px;
  font-size: 56px;
}
.login-title {
  font-size: 24px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 8px;
  color: #1e293b;
}
.login-subtitle {
  font-size: 14px;
  text-align: center;
  color: #64748b;
  margin-bottom: 32px;
}
.tab-group {
  display: flex;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 24px;
}
.tab-item {
  flex: 1;
  padding: 12px;
  text-align: center;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.3s;
  &.active {
    background: white;
    color: #667eea;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
}
.form-container {
  animation: slideIn 0.3s ease-out;
}
@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.form-group {
  margin-bottom: 16px;
}
.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 8px;
}
.required {
  color: #ef4444;
}
.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 16px;
  transition: all 0.3s;
  box-sizing: border-box;
  &:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
}
.code-input-wrapper {
  display: flex;
  gap: 8px;
  .form-input {
    flex: 1;
  }
}
.code-btn {
  padding: 12px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.3s;
  &:disabled {
    background: #cbd5e1;
    cursor: not-allowed;
  }
}
.checkbox-group {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 8px;
}
.checkbox-label {
  font-size: 13px;
  color: #64748b;
}
.login-button {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: all 0.3s;
  &:active {
    transform: translateY(-2px);
  }
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}
.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
  color: #64748b;
}
.footer-link {
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s;
  &:active {
    opacity: 0.7;
  }
}
</style>
