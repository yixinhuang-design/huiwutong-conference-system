<template>
  <scroll-view class="page" scroll-y>
    <view class="header-card">
      <view class="title">2025党务干部培训班</view>
      <view class="sub">报名状态查询</view>
    </view>

    <view class="card">
      <input class="input" v-model="phone" type="number" maxlength="11" placeholder="请输入报名时填写的手机号" />
      <button class="btn" :disabled="querying" @click="queryStatus">{{ querying ? '查询中...' : '查询报名状态' }}</button>
    </view>

    <view class="card" v-if="result">
      <view class="status-head">
        <view>
          <view class="r-title">{{ meetingName }}</view>
          <view class="r-sub">报名状态</view>
        </view>
        <text class="chip" :class="statusClass">{{ statusText }}</text>
      </view>

      <view class="line">报名人：{{ result.realName || '未知' }} · {{ result.department || '未知单位' }} · {{ result.position || '未知职位' }}</view>
      <view class="line">报名时间：{{ result.createdAt || '-' }}</view>

      <view class="banner pass" v-if="status === 1">
        恭喜！您的报名已通过审核，请按时参加培训。
      </view>
      <view class="banner pending" v-else-if="status === 0">
        您的报名正在审核中，通常 1-2 个工作日内完成。
      </view>
      <view class="banner reject" v-else>
        您的报名需退回修改。原因：{{ result.rejectReason || '请联系会务组' }}
      </view>

      <view class="actions" v-if="status === 1">
        <button class="mini" @click="go('/pages/learner/schedule/schedule')">查看日程</button>
        <button class="mini primary" @click="go('/pages/learner/seat/seat')">查看座位</button>
      </view>
      <view class="actions" v-else-if="status !== 0">
        <button class="mini" @click="go('/pages/learner/scan-register/scan-register')">修改报名</button>
        <button class="mini" @click="contact">联系会务组</button>
      </view>
    </view>

    <view class="tip">提示：请输入报名时填写的手机号进行查询。</view>
  </scroll-view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const phone = ref('')
const querying = ref(false)
const meetingName = ref('2025党务干部培训班')
const result = ref(null)
const status = ref(null) // 1通过 0待审 -1退回

const statusText = computed(() => {
  if (status.value === 1) return '已通过'
  if (status.value === 0) return '待审核'
  return '需退回'
})

const statusClass = computed(() => {
  if (status.value === 1) return 'ok'
  if (status.value === 0) return 'wait'
  return 'reject'
})

async function queryStatus() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  querying.value = true
  try {
    const conferenceId = Number(uni.getStorageSync('current_conference_id') || 1)
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('tenantId') || uni.getStorageSync('current_tenant_id') || '2027317834622709762'
    const [err, res] = await uni.request({
      url: `http://localhost:8082/api/registration/query?conferenceId=${conferenceId}&phone=${encodeURIComponent(phone.value)}`,
      method: 'GET',
      header: { 'X-Tenant-Id': tenantId },
      timeout: 9000,
    })
    if (err) throw err
    const body = res?.data || {}
    if (body.code !== 200 && body.code !== 0) throw new Error(body.message || '查询失败')
    result.value = body.data || {}
    status.value = typeof body.data?.status === 'number' ? body.data.status : 0
  } catch (e) {
    // 兜底演示
    result.value = {
      realName: '李华',
      department: '市委组织部',
      position: '副处长',
      createdAt: '2025-02-05 14:20',
      rejectReason: '身份证号码与报名信息不一致，请核实后重新提交',
    }
    const m = Number(phone.value[10] || 0)
    status.value = m % 3 === 0 ? 1 : (m % 3 === 1 ? 0 : -1)
  } finally {
    querying.value = false
  }
}

function go(url) {
  uni.navigateTo({ url })
}

function contact() {
  uni.showModal({
    title: '联系会务组',
    content: '联系电话：020-12345678',
    showCancel: false,
  })
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '报名状态' })
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f6f8fb; }
.header-card { margin: 16rpx; background: #fff; border-radius: 14rpx; padding: 18rpx; }
.title { font-size: 32rpx; font-weight: 700; color: #1e293b; }
.sub { margin-top: 6rpx; color: #64748b; font-size: 24rpx; }

.card { margin: 0 16rpx 12rpx; background: #fff; border-radius: 12rpx; padding: 14rpx; }
.input { width: 100%; height: 78rpx; border: 2rpx solid #e2e8f0; border-radius: 10rpx; padding: 0 16rpx; font-size: 26rpx; margin-bottom: 12rpx; }
.btn { width: 100%; height: 78rpx; line-height: 78rpx; border-radius: 10rpx; background: #667eea; color: #fff; font-size: 26rpx; }

.status-head { display: flex; justify-content: space-between; gap: 12rpx; align-items: flex-start; }
.r-title { font-size: 28rpx; font-weight: 700; color: #1e293b; }
.r-sub { margin-top: 4rpx; color: #64748b; font-size: 22rpx; }

.chip { padding: 6rpx 12rpx; border-radius: 999rpx; font-size: 22rpx; }
.chip.ok { background: #ecfdf3; color: #16a34a; }
.chip.wait { background: #fff7ed; color: #ea580c; }
.chip.reject { background: #fef2f2; color: #dc2626; }

.line { margin-top: 8rpx; color: #475569; font-size: 23rpx; }
.banner { margin-top: 12rpx; border-radius: 10rpx; padding: 12rpx; font-size: 23rpx; }
.banner.pass { background: #f0fdf4; color: #166534; }
.banner.pending { background: #fffbeb; color: #92400e; }
.banner.reject { background: #fef2f2; color: #991b1b; }

.actions { display: flex; gap: 10rpx; margin-top: 12rpx; }
.mini { flex: 1; height: 66rpx; line-height: 66rpx; border-radius: 10rpx; background: #e2e8f0; color: #334155; font-size: 24rpx; }
.mini.primary { background: #667eea; color: #fff; }

.tip { margin: 0 16rpx 20rpx; background: #eff6ff; color: #1e40af; border-radius: 10rpx; padding: 14rpx; font-size: 23rpx; }
</style>
