<template>
  <scroll-view class="page" scroll-y>
    <view class="header">
      <text class="header-title">🏢 租户管理</text>
      <view class="header-actions">
        <button class="icon-btn" @click="refreshData">🔄</button>
        <button class="icon-btn" @click="showSettings">⚙️</button>
      </view>
    </view>

    <view class="stats">
      <view class="stat-item">
        <text class="label">当前租户</text>
        <text class="value">{{ tenantInfo ? 1 : 0 }}</text>
      </view>
      <view class="stat-item">
        <text class="label">协管员</text>
        <text class="value">{{ cooperators.length }}</text>
      </view>
    </view>

    <view class="section">
      <view class="section-title">📋 租户信息</view>
      <view class="card" v-if="tenantInfo">
        <view class="tenant-title">{{ tenantInfo.tenantName || tenantInfo.name }}</view>
        <view class="info-row"><text>编码</text><text>{{ tenantInfo.tenantCode || tenantInfo.code || '-' }}</text></view>
        <view class="info-row">
          <text>状态</text>
          <text class="badge" :class="tenantInfo.status === 'active' || tenantInfo.status === 1 ? 'ok' : 'stop'">
            {{ tenantInfo.status === 'active' || tenantInfo.status === 1 ? '活跃' : '停用' }}
          </text>
        </view>
        <view class="info-row"><text>描述</text><text>{{ tenantInfo.description || '无' }}</text></view>
      </view>
      <view class="empty" v-else>暂无租户信息</view>
    </view>

    <view class="section">
      <view class="section-title">👥 协管员管理</view>
      <view class="action-bar">
        <button class="btn" @click="openAddModal">➕ 添加协管员</button>
        <button class="btn light" @click="openStatsModal">📊 统计信息</button>
      </view>

      <view class="card" v-for="item in cooperators" :key="item.id">
        <view class="tenant-title">{{ item.name }}</view>
        <view class="info-row"><text>手机</text><text>{{ item.phone }}</text></view>
        <view class="info-row"><text>部门</text><text>{{ item.dept || '-' }}</text></view>
        <view class="actions-row">
          <button class="mini danger" @click="removeCooperator(item.id)">移除</button>
        </view>
      </view>

      <view class="empty" v-if="cooperators.length === 0">暂无协管员</view>
    </view>

    <view class="modal-mask" v-if="showAddModal" @click="closeAddModal">
      <view class="modal" @click.stop>
        <view class="modal-title">添加协管员</view>
        <input class="input" v-model="addForm.name" placeholder="姓名 *" />
        <input class="input" v-model="addForm.phone" type="number" placeholder="手机号 *" />
        <input class="input" v-model="addForm.email" placeholder="邮箱" />
        <input class="input" v-model="addForm.dept" placeholder="部门" />
        <view class="action-bar">
          <button class="btn light" @click="closeAddModal">取消</button>
          <button class="btn" @click="submitAdd">添加</button>
        </view>
      </view>
    </view>

    <view class="modal-mask" v-if="showStats" @click="showStats = false">
      <view class="modal" @click.stop>
        <view class="modal-title">租户统计信息</view>
        <view class="info-row"><text>总用户数</text><text>{{ tenantStats.userCount || 0 }}</text></view>
        <view class="info-row"><text>总会议数</text><text>{{ tenantStats.conferenceCount || 0 }}</text></view>
        <view class="info-row"><text>总注册数</text><text>{{ tenantStats.registrationCount || 0 }}</text></view>
        <view class="info-row"><text>协管员数</text><text>{{ cooperators.length }}</text></view>
        <view class="info-row"><text>最后活动</text><text>{{ tenantStats.lastActivity || '无' }}</text></view>
        <button class="btn" style="margin-top: 16rpx" @click="showStats = false">关闭</button>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const tenantInfo = ref(null)
const cooperators = ref([])
const tenantStats = ref({})
const showAddModal = ref(false)
const showStats = ref(false)

const addForm = reactive({
  name: '',
  phone: '',
  email: '',
  dept: '',
})

function getAuthHeader() {
  const token = uni.getStorageSync('auth_token')
  const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id')
  return {
    'Content-Type': 'application/json',
    Authorization: token ? `Bearer ${token}` : '',
    'X-Tenant-Id': tenantId || '',
  }
}

async function loadTenantInfo() {
  const tenantId = uni.getStorageSync('current_tenant_id') || uni.getStorageSync('tenant_id')
  if (!tenantId) return
  try {
    const [err, res] = await uni.request({
      url: `http://localhost:8081/api/tenant/${tenantId}`,
      method: 'GET',
      header: getAuthHeader(),
      timeout: 8000,
    })
    if (err) throw err
    const body = res?.data || {}
    if (body.code === 200 || body.code === 0) {
      tenantInfo.value = body.data || {}
    }
  } catch (e) {
    tenantInfo.value = {
      tenantName: uni.getStorageSync('current_tenant_name') || '默认租户',
      tenantCode: uni.getStorageSync('current_tenant_code') || 'DEFAULT',
      status: 'active',
      description: '本地兜底数据',
    }
  }
}

async function loadTenantStats() {
  const tenantId = uni.getStorageSync('current_tenant_id') || uni.getStorageSync('tenant_id')
  if (!tenantId) return
  try {
    const [err, res] = await uni.request({
      url: `http://localhost:8081/api/tenant/${tenantId}/stats`,
      method: 'GET',
      header: getAuthHeader(),
      timeout: 8000,
    })
    if (err) throw err
    const body = res?.data || {}
    tenantStats.value = body.data || {}
  } catch (e) {
    tenantStats.value = { userCount: 0, conferenceCount: 0, registrationCount: 0, lastActivity: '无' }
  }
}

async function loadCooperators() {
  const tenantId = uni.getStorageSync('current_tenant_id') || uni.getStorageSync('tenant_id')
  if (!tenantId) return
  try {
    const [err, res] = await uni.request({
      url: `http://localhost:8081/api/tenant/${tenantId}/cooperators`,
      method: 'GET',
      header: getAuthHeader(),
      timeout: 8000,
    })
    if (err) throw err
    const body = res?.data || {}
    cooperators.value = body.data || []
  } catch (e) {
    cooperators.value = [
      { id: 1, name: '张三', phone: '13800138000', email: 'zhangsan@example.com', dept: '市场部' },
      { id: 2, name: '李四', phone: '13800138001', email: 'lisi@example.com', dept: '技术部' },
    ]
  }
}

function openAddModal() {
  addForm.name = ''
  addForm.phone = ''
  addForm.email = ''
  addForm.dept = ''
  showAddModal.value = true
}

function closeAddModal() {
  showAddModal.value = false
}

async function submitAdd() {
  if (!addForm.name.trim() || !/^1[3-9]\d{9}$/.test(addForm.phone)) {
    uni.showToast({ title: '请填写正确的姓名和手机号', icon: 'none' })
    return
  }
  const tenantId = uni.getStorageSync('current_tenant_id') || uni.getStorageSync('tenant_id')
  try {
    const [err, res] = await uni.request({
      url: `http://localhost:8081/api/tenant/${tenantId}/cooperators`,
      method: 'POST',
      header: getAuthHeader(),
      data: { ...addForm },
      timeout: 8000,
    })
    if (err) throw err
    const body = res?.data || {}
    if (body.code !== 200 && body.code !== 0) throw new Error(body.message || '添加失败')
    cooperators.value.push(body.data || { id: Date.now(), ...addForm })
    uni.showToast({ title: '添加成功', icon: 'success' })
  } catch (e) {
    cooperators.value.push({ id: Date.now(), ...addForm })
    uni.showToast({ title: '已本地添加', icon: 'none' })
  } finally {
    closeAddModal()
  }
}

async function removeCooperator(id) {
  const confirmed = await new Promise((resolve) => {
    uni.showModal({
      title: '确认移除',
      content: '确定移除此协管员吗？',
      success: (res) => resolve(res.confirm),
      fail: () => resolve(false),
    })
  })
  if (!confirmed) return

  const tenantId = uni.getStorageSync('current_tenant_id') || uni.getStorageSync('tenant_id')
  try {
    await uni.request({
      url: `http://localhost:8081/api/tenant/${tenantId}/cooperators/${id}`,
      method: 'DELETE',
      header: getAuthHeader(),
      timeout: 8000,
    })
  } catch (e) {}
  cooperators.value = cooperators.value.filter((it) => it.id !== id)
  uni.showToast({ title: '已移除', icon: 'success' })
}

function openStatsModal() {
  showStats.value = true
}

function refreshData() {
  uni.showToast({ title: '刷新中...', icon: 'none' })
  loadTenantInfo()
  loadTenantStats()
  loadCooperators()
}

function showSettings() {
  uni.showToast({ title: '设置功能开发中', icon: 'none' })
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '租户管理' })
  refreshData()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f0f2f5; }
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 22rpx 24rpx;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title { font-size: 34rpx; font-weight: 700; }
.header-actions { display: flex; gap: 10rpx; }
.icon-btn {
  width: 58rpx;
  height: 58rpx;
  line-height: 58rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: none;
  padding: 0;
}

.stats { display: grid; grid-template-columns: 1fr 1fr; gap: 14rpx; padding: 16rpx; }
.stat-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  text-align: center;
}
.label { color: #94a3b8; font-size: 24rpx; }
.value { color: #667eea; font-size: 42rpx; font-weight: 700; margin-top: 8rpx; display: block; }

.section { padding: 0 16rpx 16rpx; }
.section-title { font-size: 28rpx; font-weight: 700; color: #334155; margin: 8rpx 0 10rpx; }

.card {
  background: #fff;
  border-radius: 12rpx;
  padding: 18rpx;
  margin-bottom: 12rpx;
  border-left: 6rpx solid #667eea;
}
.tenant-title { font-size: 30rpx; font-weight: 700; margin-bottom: 10rpx; color: #1e293b; }
.info-row { display: flex; justify-content: space-between; margin-top: 8rpx; font-size: 25rpx; color: #475569; }

.badge { padding: 4rpx 14rpx; border-radius: 999rpx; font-size: 22rpx; }
.badge.ok { background: #ecfeff; color: #0284c7; }
.badge.stop { background: #fef2f2; color: #dc2626; }

.action-bar { display: flex; gap: 10rpx; margin-bottom: 12rpx; }
.actions-row { margin-top: 14rpx; display: flex; justify-content: flex-end; }

.btn {
  flex: 1;
  height: 74rpx;
  line-height: 74rpx;
  border-radius: 10rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 26rpx;
}
.btn.light { background: #e2e8f0; color: #334155; }

.mini {
  padding: 0 20rpx;
  height: 56rpx;
  line-height: 56rpx;
  border-radius: 10rpx;
  font-size: 24rpx;
}
.mini.danger { background: #fee2e2; color: #dc2626; }

.empty {
  background: #fff;
  border-radius: 12rpx;
  text-align: center;
  color: #94a3b8;
  font-size: 26rpx;
  padding: 40rpx 0;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: flex-end;
}
.modal {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 22rpx;
}
.modal-title { font-size: 30rpx; font-weight: 700; margin-bottom: 14rpx; color: #1e293b; }
.input {
  width: 100%;
  height: 78rpx;
  border: 2rpx solid #e2e8f0;
  border-radius: 10rpx;
  padding: 0 18rpx;
  margin-bottom: 10rpx;
  font-size: 27rpx;
}
</style>
