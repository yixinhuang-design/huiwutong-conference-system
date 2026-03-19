<template>
  <scroll-view class="page" scroll-y>
    <view class="header-card">
      <view class="head-row">
        <view>
          <view class="title">2025党务干部培训班</view>
          <view class="sub">通知管理</view>
        </view>
        <button class="new-btn" @click="openCreate('custom')">+ 新建</button>
      </view>

      <view class="pill-group">
        <view class="pill" :class="{ active: filter==='all' }" @click="filter='all'">全部</view>
        <view class="pill" :class="{ active: filter==='pending' }" @click="filter='pending'">待发送</view>
        <view class="pill" :class="{ active: filter==='sent' }" @click="filter='sent'">已发送</view>
        <view class="pill" :class="{ active: filter==='draft' }" @click="filter='draft'">草稿箱</view>
      </view>
    </view>

    <view class="card">
      <view class="card-title">快捷操作</view>
      <view class="grid">
        <button class="quick" @click="openCreate('meeting')">会议通知</button>
        <button class="quick" @click="openCreate('registration')">报名提醒</button>
        <button class="quick" @click="openCreate('checkin')">签到提醒</button>
        <button class="quick" @click="openCreate('schedule')">日程提醒</button>
        <button class="quick" @click="openCreate('seat')">座位通知</button>
        <button class="quick" @click="openCreate('bus')">车次通知</button>
        <button class="quick" @click="openCreate('accommodation')">住宿通知</button>
        <button class="quick" @click="openCreate('full')">完整安排</button>
      </view>
    </view>

    <view class="card">
      <view class="toggle" @click="showTemplates = !showTemplates">
        <text class="card-title">通知模板</text>
        <text>{{ showTemplates ? '收起' : '展开' }}</text>
      </view>
      <view class="template-item" v-if="showTemplates" v-for="t in templates" :key="t.id" @click="useTemplate(t)">
        <view class="t-title">{{ t.name }}</view>
        <view class="t-desc">{{ t.desc }}</view>
      </view>
    </view>

    <view class="section-title">通知记录</view>
    <view class="card" v-for="n in filteredNotices" :key="n.id">
      <view class="item-head">
        <view>
          <view class="n-title">{{ n.title }}</view>
          <view class="n-time">{{ n.time }}</view>
        </view>
        <text class="status" :class="statusClass(n.status)">{{ statusText(n.status) }}</text>
      </view>
      <view class="meta">发送对象：{{ n.target }}</view>
      <view class="meta">发送渠道：{{ n.channel }}</view>
      <view class="meta">发送结果：{{ n.result }}</view>
      <view class="row-actions">
        <button class="mini" @click="viewNotice(n)">查看详情</button>
        <button class="mini" v-if="n.status==='sent'" @click="resend(n)">重新发送</button>
        <button class="mini danger" v-if="n.status==='pending'" @click="cancel(n)">取消</button>
      </view>
    </view>

    <view class="card">
      <view class="toggle" @click="showStats = !showStats">
        <text class="card-title">发送统计</text>
        <text>{{ showStats ? '收起' : '展开' }}</text>
      </view>
      <view class="stats" v-if="showStats">
        <view class="s-item"><text class="k">总发送数</text><text class="v">{{ stats.total }}</text></view>
        <view class="s-item"><text class="k">平均阅读率</text><text class="v">{{ stats.readRate }}</text></view>
        <view class="s-item"><text class="k">发送失败</text><text class="v">{{ stats.fail }}</text></view>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const filter = ref('all')
const showTemplates = ref(false)
const showStats = ref(false)
const notices = ref([])

const templates = ref([
  { id: 1, name: '会议通知模板', desc: '包含 {姓名}、{会议名称}、{时间}、{地点} 变量' },
  { id: 2, name: '报名提醒模板', desc: '包含 {姓名}、{会议名称}、{截止日期} 变量' },
  { id: 3, name: '座位安排模板', desc: '包含 {姓名}、{座位号}、{会场} 变量' },
])

const stats = ref({ total: 1234, readRate: '87%', fail: 23 })

const filteredNotices = computed(() => {
  if (filter.value === 'all') return notices.value
  return notices.value.filter((n) => n.status === filter.value)
})

function statusText(s) {
  const map = { sent: '已发送', pending: '定时发送', draft: '草稿' }
  return map[s] || '未知'
}

function statusClass(s) {
  if (s === 'sent') return 'ok'
  if (s === 'pending') return 'wait'
  return 'draft'
}

function mockData() {
  notices.value = [
    {
      id: 'seat001',
      title: '座位安排通知',
      time: '发送于 2025-02-05 14:30',
      status: 'sent',
      target: '全部参会者 (142人)',
      channel: '短信、APP推送',
      result: '成功 138 · 失败 4 · 阅读率 89%',
    },
    {
      id: 'plan002',
      title: '开班仪式提醒',
      time: '计划于 2025-02-15 08:30 发送',
      status: 'pending',
      target: '全体人员 (142人)',
      channel: 'APP推送',
      result: '等待发送',
    },
    {
      id: 'draft003',
      title: '会议安排查询二维码',
      time: '草稿于 2025-02-05 10:00',
      status: 'draft',
      target: '全部参会者',
      channel: '二维码',
      result: '草稿未发送',
    },
  ]
}

async function loadNotices() {
  try {
    const token = uni.getStorageSync('auth_token') || ''
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
    const [err, res] = await uni.request({
      url: 'http://localhost:8084/api/notice/history',
      method: 'GET',
      header: { Authorization: token ? `Bearer ${token}` : '', 'X-Tenant-Id': tenantId },
      timeout: 9000,
    })
    if (err) throw err
    const body = res?.data || {}
    const list = body?.data || []
    if (Array.isArray(list) && list.length) {
      notices.value = list.map((n) => ({
        id: n.id,
        title: n.title || '通知',
        time: n.timeText || n.sendTime || '-',
        status: n.status || 'draft',
        target: n.targetText || '-',
        channel: n.channelText || '-',
        result: n.resultText || '-',
      }))
    } else {
      mockData()
    }
  } catch (e) {
    mockData()
  }
}

function openCreate(type) {
  uni.showToast({ title: `创建${type}通知`, icon: 'none' })
}

function useTemplate(t) {
  uni.showToast({ title: `已选择：${t.name}`, icon: 'none' })
}

function viewNotice(n) {
  uni.showModal({ title: n.title, content: `${n.time}\n${n.result}`, showCancel: false })
}

async function resend(n) {
  try {
    const token = uni.getStorageSync('auth_token') || ''
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
    await uni.request({
      url: `http://localhost:8084/api/notice/resend/${n.id}`,
      method: 'POST',
      header: { Authorization: token ? `Bearer ${token}` : '', 'X-Tenant-Id': tenantId },
      timeout: 9000,
    })
  } catch (e) {}
  uni.showToast({ title: '已发起重发', icon: 'success' })
}

function cancel(n) {
  n.status = 'draft'
  n.result = '已取消发送'
  uni.showToast({ title: '已取消', icon: 'none' })
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '通知管理' })
  loadNotices()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f6f8fb; padding-bottom: 20rpx; }
.header-card { margin: 16rpx; background: #fff; border-radius: 14rpx; padding: 18rpx; }
.head-row { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 32rpx; font-weight: 700; color: #1e293b; }
.sub { margin-top: 6rpx; color: #64748b; font-size: 24rpx; }
.new-btn { height: 62rpx; line-height: 62rpx; border-radius: 10rpx; font-size: 24rpx; background: #667eea; color: #fff; }

.pill-group { display: flex; gap: 8rpx; margin-top: 12rpx; overflow-x: auto; }
.pill { flex-shrink: 0; padding: 8rpx 16rpx; border-radius: 999rpx; font-size: 22rpx; background: #e2e8f0; color: #475569; }
.pill.active { background: #667eea; color: #fff; }

.card { margin: 0 16rpx 12rpx; background: #fff; border-radius: 12rpx; padding: 14rpx; }
.card-title { font-size: 28rpx; font-weight: 700; color: #334155; }

.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10rpx; margin-top: 10rpx; }
.quick { height: 64rpx; line-height: 64rpx; border-radius: 10rpx; font-size: 24rpx; background: #f1f5f9; color: #334155; }

.toggle { display: flex; justify-content: space-between; align-items: center; }
.template-item { margin-top: 10rpx; padding: 12rpx; border-radius: 10rpx; background: #f8fafc; }
.t-title { font-size: 25rpx; color: #1e293b; font-weight: 600; }
.t-desc { margin-top: 4rpx; font-size: 22rpx; color: #64748b; }

.section-title { margin: 12rpx 16rpx 8rpx; font-size: 28rpx; font-weight: 700; color: #334155; }
.item-head { display: flex; justify-content: space-between; gap: 12rpx; }
.n-title { font-size: 28rpx; font-weight: 700; color: #1e293b; }
.n-time { margin-top: 6rpx; font-size: 22rpx; color: #64748b; }

.status { align-self: flex-start; padding: 6rpx 12rpx; border-radius: 999rpx; font-size: 22rpx; }
.status.ok { background: #ecfdf3; color: #16a34a; }
.status.wait { background: #fff7ed; color: #ea580c; }
.status.draft { background: #f1f5f9; color: #64748b; }

.meta { margin-top: 6rpx; font-size: 23rpx; color: #475569; }
.row-actions { display: flex; gap: 10rpx; margin-top: 12rpx; }
.mini { flex: 1; height: 60rpx; line-height: 60rpx; border-radius: 10rpx; font-size: 24rpx; background: #e2e8f0; color: #334155; }
.mini.danger { background: #fee2e2; color: #dc2626; }

.stats { margin-top: 10rpx; }
.s-item { display: flex; justify-content: space-between; padding: 10rpx 0; border-bottom: 1px solid #eef2f7; }
.s-item:last-child { border-bottom: none; }
.k { color: #64748b; font-size: 24rpx; }
.v { color: #1e293b; font-size: 28rpx; font-weight: 700; }
</style>
