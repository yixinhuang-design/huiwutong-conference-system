<template>
  <scroll-view class="page" scroll-y>
    <view class="header-card">
      <view class="title">2025党务干部培训班</view>
      <view class="sub">预警配置</view>
    </view>

    <view class="card">
      <view class="card-title">新建预警规则</view>
      <picker class="picker" :range="metricOptions" @change="onMetricChange">
        <view class="picker-value">预警指标：{{ draft.metric }}</view>
      </picker>
      <view class="row">
        <input class="input" v-model.number="draft.threshold" type="number" placeholder="阈值，如 80" />
        <picker class="picker" :range="opOptions" @change="onOpChange">
          <view class="picker-value">{{ draft.op }}</view>
        </picker>
      </view>
      <picker class="picker" :range="levelOptions" @change="onLevelChange">
        <view class="picker-value">预警级别：{{ draft.level }}</view>
      </picker>
      <view class="checks">
        <label class="check"><checkbox :checked="draft.channels.sms" @click="draft.channels.sms = !draft.channels.sms" />短信</label>
        <label class="check"><checkbox :checked="draft.channels.system" @click="draft.channels.system = !draft.channels.system" />系统通知</label>
        <label class="check"><checkbox :checked="draft.channels.email" @click="draft.channels.email = !draft.channels.email" />邮件</label>
      </view>
      <view class="actions">
        <button class="btn light" @click="resetDraft">重置</button>
        <button class="btn" @click="addRule">保存规则</button>
      </view>
    </view>

    <view class="section-title">预警规则列表</view>
    <view class="card" v-for="r in rules" :key="r.id">
      <view class="head">
        <view>
          <view class="r-title">{{ r.name }}</view>
          <view class="r-sub">{{ r.scope }}</view>
        </view>
        <text class="chip" :class="r.enabled ? 'ok' : 'off'">{{ r.enabled ? '启用' : '禁用' }}</text>
      </view>
      <view class="line">预警指标：{{ r.metric }} {{ r.op }} {{ r.threshold }}</view>
      <view class="line">预警级别：{{ r.level }}</view>
      <view class="line">通知方式：{{ r.channels.join('、') }}</view>
      <view class="actions">
        <button class="mini" @click="toggleRule(r)">{{ r.enabled ? '禁用' : '启用' }}</button>
        <button class="mini danger" @click="removeRule(r.id)">删除</button>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'

const metricOptions = ['报名率', '签到率', '任务完成率', 'API响应时间', '系统错误率']
const opOptions = ['<', '>', '<=', '>=']
const levelOptions = ['低', '中', '高', '紧急']

const draft = reactive({
  metric: '报名率',
  op: '<',
  threshold: 80,
  level: '中',
  channels: { sms: true, system: true, email: false },
})

const rules = ref([])

function onMetricChange(e) { draft.metric = metricOptions[Number(e.detail.value)] }
function onOpChange(e) { draft.op = opOptions[Number(e.detail.value)] }
function onLevelChange(e) { draft.level = levelOptions[Number(e.detail.value)] }

function resetDraft() {
  draft.metric = '报名率'
  draft.op = '<'
  draft.threshold = 80
  draft.level = '中'
  draft.channels.sms = true
  draft.channels.system = true
  draft.channels.email = false
}

function mockRules() {
  rules.value = [
    { id: 1, name: '报名率预警', scope: '2025党务干部培训班', metric: '报名率', op: '<', threshold: 80, level: '中', channels: ['短信', '系统通知'], enabled: true },
    { id: 2, name: '签到率预警', scope: '2025党务干部培训班', metric: '签到率', op: '<', threshold: 70, level: '高', channels: ['短信', '系统通知'], enabled: true },
    { id: 3, name: '系统错误率预警', scope: '系统性能监控', metric: '系统错误率', op: '>', threshold: 5, level: '紧急', channels: ['系统通知', '邮件'], enabled: true },
  ]
}

async function loadRules() {
  try {
    const token = uni.getStorageSync('auth_token') || ''
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
    const [err, res] = await uni.request({
      url: 'http://localhost:8084/api/alert/config/list',
      method: 'GET',
      header: { Authorization: token ? `Bearer ${token}` : '', 'X-Tenant-Id': tenantId },
      timeout: 9000,
    })
    if (err) throw err
    const body = res?.data || {}
    if (Array.isArray(body?.data) && body.data.length) {
      rules.value = body.data
    } else {
      mockRules()
    }
  } catch (e) {
    mockRules()
  }
}

async function addRule() {
  const channels = []
  if (draft.channels.sms) channels.push('短信')
  if (draft.channels.system) channels.push('系统通知')
  if (draft.channels.email) channels.push('邮件')
  if (!channels.length) {
    uni.showToast({ title: '至少选择一种通知方式', icon: 'none' })
    return
  }

  const payload = {
    id: Date.now(),
    name: `${draft.metric}预警`,
    scope: '当前培训',
    metric: draft.metric,
    op: draft.op,
    threshold: Number(draft.threshold) || 0,
    level: draft.level,
    channels,
    enabled: true,
  }

  try {
    const token = uni.getStorageSync('auth_token') || ''
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
    await uni.request({
      url: 'http://localhost:8084/api/alert/config/create',
      method: 'POST',
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'X-Tenant-Id': tenantId,
        'Content-Type': 'application/json',
      },
      data: payload,
      timeout: 9000,
    })
  } catch (e) {}

  rules.value.unshift(payload)
  uni.showToast({ title: '规则已保存', icon: 'success' })
}

function toggleRule(r) {
  r.enabled = !r.enabled
  uni.showToast({ title: r.enabled ? '已启用' : '已禁用', icon: 'none' })
}

function removeRule(id) {
  rules.value = rules.value.filter((r) => r.id !== id)
  uni.showToast({ title: '已删除', icon: 'none' })
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '预警配置' })
  loadRules()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f6f8fb; }
.header-card { margin: 16rpx; background: #fff; border-radius: 14rpx; padding: 18rpx; }
.title { font-size: 32rpx; font-weight: 700; color: #1e293b; }
.sub { margin-top: 6rpx; color: #64748b; font-size: 24rpx; }

.card { margin: 0 16rpx 12rpx; background: #fff; border-radius: 12rpx; padding: 14rpx; }
.card-title { font-size: 28rpx; font-weight: 700; color: #334155; margin-bottom: 10rpx; }

.picker, .input { height: 74rpx; border: 2rpx solid #e2e8f0; border-radius: 10rpx; padding: 0 16rpx; display: flex; align-items: center; font-size: 25rpx; margin-bottom: 10rpx; }
.picker-value { color: #475569; }
.row { display: flex; gap: 10rpx; }
.row .input, .row .picker { flex: 1; }

.checks { display: flex; gap: 16rpx; margin: 8rpx 0 12rpx; flex-wrap: wrap; }
.check { font-size: 24rpx; color: #334155; display: flex; align-items: center; }

.actions { display: flex; gap: 10rpx; }
.btn { flex: 1; height: 70rpx; line-height: 70rpx; border-radius: 10rpx; background: #667eea; color: #fff; font-size: 24rpx; }
.btn.light { background: #e2e8f0; color: #334155; }

.section-title { margin: 12rpx 16rpx 8rpx; font-size: 28rpx; font-weight: 700; color: #334155; }
.head { display: flex; justify-content: space-between; gap: 12rpx; }
.r-title { font-size: 27rpx; font-weight: 700; color: #1e293b; }
.r-sub { margin-top: 4rpx; color: #64748b; font-size: 22rpx; }

.chip { align-self: flex-start; padding: 6rpx 12rpx; border-radius: 999rpx; font-size: 22rpx; }
.chip.ok { background: #ecfdf3; color: #16a34a; }
.chip.off { background: #f1f5f9; color: #64748b; }

.line { margin-top: 6rpx; color: #475569; font-size: 23rpx; }
.mini { flex: 1; height: 60rpx; line-height: 60rpx; border-radius: 10rpx; background: #e2e8f0; color: #334155; font-size: 23rpx; }
.mini.danger { background: #fee2e2; color: #dc2626; }
</style>
