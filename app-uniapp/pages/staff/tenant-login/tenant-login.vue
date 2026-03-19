<template>
  <view class="login-container">
    <view class="login-card">
      <view class="login-logo">🎓</view>
      <view class="login-title">智能会议系统</view>
      <view class="login-subtitle">租户管理员登录</view>

      <view class="form-item">
        <text class="label">选择租户</text>
        <picker
          class="picker"
          :range="tenantOptions"
          range-key="label"
          @change="handleTenantChange"
        >
          <view class="picker-value">
            {{ selectedTenant ? selectedTenant.label : '请选择租户' }}
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">管理员账号</text>
        <input class="input" v-model="form.username" placeholder="请输入管理员账号" />
      </view>

      <view class="form-item">
        <text class="label">登录密码</text>
        <input class="input" v-model="form.password" password placeholder="请输入登录密码" />
      </view>

      <button class="login-button" :disabled="submitting" @click="submitLogin">
        {{ submitting ? '登录中...' : '登录' }}
      </button>

      <view class="footer-links">
        <text class="link">忘记密码？</text>
        <text class="divider">|</text>
        <text class="link">联系系统管理员</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const tenantOptions = ref([])
const selectedTenant = ref(null)
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
})

function mockTenants() {
  return [
    { value: 'T001', label: '市委党校 (T001)', id: 'T001', name: '市委党校' },
    { value: 'T002', label: '市教育局 (T002)', id: 'T002', name: '市教育局' },
    { value: 'T003', label: '某国企分公司 (T003)', id: 'T003', name: '某国企分公司' },
  ]
}

async function loadTenants() {
  try {
    const res = await uni.request({
      url: 'http://localhost:8081/api/tenant/list',
      method: 'GET',
      timeout: 6000,
    })
    const body = res?.data || {}
    const raw = body?.data || []
    if (Array.isArray(raw) && raw.length > 0) {
      tenantOptions.value = raw.map((t) => ({
        value: t.tenantCode || t.code || String(t.id),
        label: `${t.tenantName || t.name} (${t.tenantCode || t.code || t.id})`,
        id: t.id || t.tenantId || t.tenantCode,
        code: t.tenantCode || t.code,
        name: t.tenantName || t.name,
      }))
    } else {
      tenantOptions.value = mockTenants()
    }
  } catch (e) {
    tenantOptions.value = mockTenants()
  }
}

function handleTenantChange(e) {
  const idx = Number(e.detail.value)
  selectedTenant.value = tenantOptions.value[idx]
}

function validate() {
  if (!selectedTenant.value) {
    uni.showToast({ title: '请选择租户', icon: 'none' })
    return false
  }
  if (!form.username.trim()) {
    uni.showToast({ title: '请输入管理员账号', icon: 'none' })
    return false
  }
  if (!form.password || form.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return false
  }
  return true
}

async function submitLogin() {
  if (!validate() || submitting.value) return
  submitting.value = true
  uni.showLoading({ title: '登录中...', mask: true })
  try {
    const [err, res] = await uni.request({
      url: 'http://localhost:8081/api/auth/login',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'X-Tenant-Id': selectedTenant.value.id,
      },
      data: {
        username: form.username,
        password: form.password,
      },
      timeout: 10000,
    })

    if (err) throw err

    const body = res?.data || {}
    if (body.code !== 200 && body.code !== 0) {
      throw new Error(body.message || '登录失败')
    }

    const authData = body.data || {}
    uni.setStorageSync('auth_token', authData.token || authData.accessToken || '')
    uni.setStorageSync('user_info', JSON.stringify(authData.user || {}))
    uni.setStorageSync('current_tenant_id', selectedTenant.value.id)
    uni.setStorageSync('tenant_id', selectedTenant.value.id)
    uni.setStorageSync('current_tenant_code', selectedTenant.value.code || selectedTenant.value.value)
    uni.setStorageSync('current_tenant_name', selectedTenant.value.name || selectedTenant.value.label)

    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/staff/tenant-manage/tenant-manage' })
    }, 300)
  } catch (error) {
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  } finally {
    uni.hideLoading()
    submitting.value = false
  }
}

onMounted(() => {
  loadTenants()
  uni.setNavigationBarTitle({ title: '租户管理员登录' })
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  padding: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 100%;
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 36rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.16);
}

.login-logo {
  text-align: center;
  font-size: 72rpx;
  margin-bottom: 12rpx;
}

.login-title {
  text-align: center;
  font-size: 44rpx;
  font-weight: 700;
  color: #1e293b;
}

.login-subtitle {
  text-align: center;
  font-size: 26rpx;
  color: #64748b;
  margin: 10rpx 0 36rpx;
}

.form-item { margin-bottom: 24rpx; }
.label { display: block; color: #475569; font-size: 26rpx; margin-bottom: 10rpx; }

.picker,
.input {
  width: 100%;
  height: 84rpx;
  border: 2rpx solid #e2e8f0;
  border-radius: 16rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  font-size: 28rpx;
  background: #fff;
}

.picker-value { color: #334155; }

.login-button {
  margin-top: 18rpx;
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 16rpx;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 30rpx;
  font-weight: 600;
}

.footer-links {
  margin-top: 30rpx;
  text-align: center;
  color: #64748b;
  font-size: 24rpx;
}
.link { color: #667eea; }
.divider { margin: 0 12rpx; color: #cbd5e1; }
</style>
