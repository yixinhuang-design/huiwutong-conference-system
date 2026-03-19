<template>
  <scroll-view class="page" scroll-y>
    <view class="steps">
      <view class="step" :class="{ active: step >= 1 }">1 基本信息</view>
      <view class="step" :class="{ active: step >= 2 }">2 参数配置</view>
      <view class="step" :class="{ active: step >= 3 }">3 管理员设置</view>
    </view>

    <view class="card" v-if="step === 1">
      <view class="title">租户基本信息</view>
      <input class="input" v-model="form.tenantName" placeholder="租户名称 *" />
      <input class="input" v-model="form.tenantCode" placeholder="租户编码（如T004）*" />
      <picker class="picker" :range="tenantTypes" @change="onTypeChange">
        <view class="picker-value">{{ form.tenantType || '租户类型 *' }}</view>
      </picker>
      <input class="input" v-model="form.contactName" placeholder="联系人" />
      <input class="input" v-model="form.contactPhone" type="number" placeholder="联系电话 *" />
      <input class="input" v-model="form.contactEmail" placeholder="联系邮箱" />
    </view>

    <view class="card" v-if="step === 2">
      <view class="title">参数配置</view>
      <picker class="picker" :range="scopeOptions" @change="onScopeChange">
        <view class="picker-value">{{ form.permissionScope || '权限范围' }}</view>
      </picker>
      <view class="row">
        <input class="input" v-model="form.startDate" type="date" />
        <input class="input" v-model="form.endDate" type="date" />
      </view>
      <input class="input" v-model.number="form.maxUsers" type="number" placeholder="最大用户数" />
      <input class="input" v-model.number="form.maxMeetings" type="number" placeholder="最大会议数" />
      <input class="input" v-model.number="form.storageQuotaGb" type="number" placeholder="存储配额(GB)" />
    </view>

    <view class="card" v-if="step === 3">
      <view class="title">管理员设置</view>
      <input class="input" v-model="form.adminUsername" placeholder="管理员账号 *" />
      <view class="row">
        <input class="input" v-model="form.adminPassword" password placeholder="初始密码 *" />
        <button class="ghost" @click="generatePassword">随机</button>
      </view>
      <input class="input" v-model="form.adminName" placeholder="管理员姓名 *" />
      <input class="input" v-model="form.adminPhone" type="number" placeholder="管理员手机 *" />
    </view>

    <view class="action-bar">
      <button class="btn secondary" @click="saveDraft">保存草稿</button>
      <button class="btn" v-if="step < 3" @click="nextStep">下一步</button>
      <button class="btn" v-else :disabled="submitting" @click="submitCreate">
        {{ submitting ? '创建中...' : '创建租户' }}
      </button>
    </view>
  </scroll-view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'

const step = ref(1)
const submitting = ref(false)
const draftKey = 'tenant_create_draft'

const tenantTypes = ['政府机关', '事业单位', '企业', '社会组织', '其他']
const scopeOptions = ['全部功能', '基础功能', '自定义']

const form = reactive({
  tenantName: '',
  tenantCode: '',
  tenantType: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  permissionScope: '全部功能',
  startDate: '',
  endDate: '',
  maxUsers: 100,
  maxMeetings: 50,
  storageQuotaGb: 50,
  adminUsername: '',
  adminPassword: '123456',
  adminName: '',
  adminPhone: '',
})

function onTypeChange(e) {
  form.tenantType = tenantTypes[Number(e.detail.value)]
}

function onScopeChange(e) {
  form.permissionScope = scopeOptions[Number(e.detail.value)]
}

function validateCurrentStep() {
  if (step.value === 1) {
    if (!form.tenantName.trim() || !form.tenantCode.trim() || !form.tenantType || !form.contactPhone.trim()) {
      uni.showToast({ title: '请完善第1步必填信息', icon: 'none' })
      return false
    }
    if (!/^1[3-9]\d{9}$/.test(form.contactPhone)) {
      uni.showToast({ title: '联系电话格式不正确', icon: 'none' })
      return false
    }
  }
  if (step.value === 2 && form.startDate && form.endDate && form.startDate > form.endDate) {
    uni.showToast({ title: '使用期限开始时间不能大于结束时间', icon: 'none' })
    return false
  }
  if (step.value === 3) {
    if (!form.adminUsername.trim() || !form.adminPassword || !form.adminName.trim() || !form.adminPhone.trim()) {
      uni.showToast({ title: '请完善第3步必填信息', icon: 'none' })
      return false
    }
    if (!/^1[3-9]\d{9}$/.test(form.adminPhone)) {
      uni.showToast({ title: '管理员手机号格式不正确', icon: 'none' })
      return false
    }
  }
  return true
}

function nextStep() {
  if (!validateCurrentStep()) return
  step.value = Math.min(3, step.value + 1)
}

function saveDraft() {
  uni.setStorageSync(draftKey, JSON.stringify(form))
  uni.showToast({ title: '草稿已保存', icon: 'success' })
}

function restoreDraft() {
  const raw = uni.getStorageSync(draftKey)
  if (!raw) return
  try {
    const data = JSON.parse(raw)
    Object.assign(form, data)
  } catch (e) {}
}

function generatePassword() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$'
  let pwd = ''
  for (let i = 0; i < 10; i++) pwd += chars[Math.floor(Math.random() * chars.length)]
  form.adminPassword = pwd
}

async function submitCreate() {
  if (!validateCurrentStep() || submitting.value) return
  submitting.value = true
  uni.showLoading({ title: '创建中...', mask: true })
  try {
    const token = uni.getStorageSync('auth_token')
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id')
    const [err, res] = await uni.request({
      url: 'http://localhost:8081/api/tenant/create',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        'X-Tenant-Id': tenantId || '',
      },
      data: {
        tenantName: form.tenantName,
        tenantCode: form.tenantCode,
        tenantType: form.tenantType,
        contactName: form.contactName,
        contactPhone: form.contactPhone,
        contactEmail: form.contactEmail,
        permissionScope: form.permissionScope,
        startDate: form.startDate,
        endDate: form.endDate,
        maxUsers: Number(form.maxUsers) || 0,
        maxMeetings: Number(form.maxMeetings) || 0,
        storageQuotaGb: Number(form.storageQuotaGb) || 0,
        admin: {
          username: form.adminUsername,
          password: form.adminPassword,
          name: form.adminName,
          phone: form.adminPhone,
        },
      },
      timeout: 12000,
    })
    if (err) throw err
    const body = res?.data || {}
    if (body.code !== 200 && body.code !== 0) throw new Error(body.message || '创建失败')
    uni.removeStorageSync(draftKey)
    uni.showToast({ title: '租户创建成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack({ delta: 1 })
    }, 350)
  } catch (error) {
    uni.showToast({ title: error.message || '创建失败', icon: 'none' })
  } finally {
    submitting.value = false
    uni.hideLoading()
  }
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '创建租户' })
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  form.startDate = `${y}-${m}-${d}`
  form.endDate = `${y + 1}-${m}-${d}`
  restoreDraft()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f5f7fb; }
.steps { display: flex; gap: 8rpx; padding: 20rpx; }
.step {
  flex: 1;
  text-align: center;
  padding: 12rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  font-size: 22rpx;
  color: #64748b;
}
.step.active { background: #667eea; color: #fff; }

.card {
  background: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}
.title { font-size: 30rpx; font-weight: 700; margin-bottom: 20rpx; color: #1e293b; }

.input,
.picker {
  width: 100%;
  height: 82rpx;
  border: 2rpx solid #e2e8f0;
  border-radius: 12rpx;
  padding: 0 20rpx;
  margin-bottom: 14rpx;
  display: flex;
  align-items: center;
  font-size: 28rpx;
  background: #fff;
}
.picker-value { color: #334155; }

.row { display: flex; gap: 12rpx; align-items: center; }
.row .input { flex: 1; }

.ghost {
  height: 82rpx;
  border-radius: 12rpx;
  border: 2rpx solid #cbd5e1;
  background: #f8fafc;
  color: #475569;
  font-size: 24rpx;
  padding: 0 20rpx;
}

.action-bar {
  position: sticky;
  bottom: 0;
  background: #fff;
  padding: 16rpx 20rpx 30rpx;
  display: flex;
  gap: 12rpx;
  border-top: 1px solid #edf2f7;
}
.btn {
  flex: 1;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 12rpx;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 28rpx;
}
.btn.secondary {
  color: #334155;
  background: #e2e8f0;
}
</style>
