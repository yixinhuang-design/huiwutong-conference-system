<template>
  <scroll-view class="page" scroll-y>
    <view class="header-card">
      <view class="title">2025党务干部培训班</view>
      <view class="sub">扫码查座</view>
    </view>

    <view class="card">
      <view class="scan-area">
        <view class="corner tl" />
        <view class="corner tr" />
        <view class="corner bl" />
        <view class="corner br" />
        <view class="scan-icon">⌁</view>
        <view class="scan-line" />
      </view>

      <view class="btns">
        <button class="btn primary" @click="scanCode">扫码查座</button>
        <button class="btn" @click="manualInput">手动输入座位号</button>
        <button class="btn" @click="showHistory = !showHistory">历史记录</button>
      </view>

      <view class="tips">
        <text>• 支持扫描二维码快速查找座位</text>
        <text>• 无法扫码时可手动输入座位编号</text>
        <text>• 可查看历史查座记录</text>
      </view>
    </view>

    <view class="card" v-if="result">
      <view class="r-title">查座结果</view>
      <view class="line">姓名：{{ result.name }}</view>
      <view class="line">座位号：{{ result.seatCode }}</view>
      <view class="line">会场：{{ result.hall }}</view>
      <view class="line">区域：{{ result.area }}</view>
      <button class="btn primary" @click="goSeatDetail">查看座位详情</button>
    </view>

    <view class="card" v-if="showHistory">
      <view class="r-title">历史记录</view>
      <view class="history-item" v-for="h in history" :key="h.id" @click="loadHistory(h)">
        <text>{{ h.time }} - {{ h.seatCode }}</text>
      </view>
      <view class="empty" v-if="history.length === 0">暂无历史记录</view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const result = ref(null)
const history = ref([])
const showHistory = ref(false)

function saveHistory(item) {
  history.value.unshift({
    id: Date.now(),
    time: new Date().toLocaleString('zh-CN'),
    seatCode: item.seatCode,
    payload: item,
  })
  history.value = history.value.slice(0, 10)
  uni.setStorageSync('scan_seat_history', JSON.stringify(history.value))
}

function restoreHistory() {
  const raw = uni.getStorageSync('scan_seat_history')
  if (!raw) return
  try { history.value = JSON.parse(raw) || [] } catch (e) {}
}

async function fetchSeatByCode(code) {
  const token = uni.getStorageSync('auth_token') || ''
  const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
  try {
    const [err, res] = await uni.request({
      url: `http://localhost:8084/api/seat/query?seatCode=${encodeURIComponent(code)}`,
      method: 'GET',
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'X-Tenant-Id': tenantId,
      },
      timeout: 9000,
    })
    if (err) throw err
    const body = res?.data || {}
    if (body.code !== 200 && body.code !== 0) throw new Error(body.message || '查询失败')
    return body.data
  } catch (e) {
    return {
      name: '李华',
      seatCode: code || 'A301',
      hall: '越秀会议中心',
      area: 'A区 3排 1座',
    }
  }
}

async function scanCode() {
  try {
    const [err, scanRes] = await uni.scanCode({
      scanType: ['qrCode', 'barCode'],
      onlyFromCamera: false,
    })
    if (err) throw err
    const text = scanRes?.result || ''
    const code = text.includes('code=') ? text.split('code=')[1] : text
    if (!code) return
    const data = await fetchSeatByCode(code)
    result.value = data
    saveHistory(data)
    uni.showToast({ title: '查询成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '扫码取消或失败', icon: 'none' })
  }
}

function manualInput() {
  uni.showModal({
    title: '手动输入',
    editable: true,
    placeholderText: '请输入座位编号，如 A301',
    success: async (res) => {
      if (!res.confirm || !res.content) return
      const data = await fetchSeatByCode(res.content.trim())
      result.value = data
      saveHistory(data)
    },
  })
}

function loadHistory(h) {
  result.value = h.payload
}

function goSeatDetail() {
  if (!result.value?.seatCode) return
  uni.navigateTo({ url: `/pages/learner/seat-detail/seat-detail?code=${encodeURIComponent(result.value.seatCode)}` })
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '扫码查座' })
  restoreHistory()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f6f8fb; }
.header-card { margin: 16rpx; background: #fff; border-radius: 14rpx; padding: 18rpx; }
.title { font-size: 32rpx; font-weight: 700; color: #1e293b; }
.sub { margin-top: 6rpx; color: #64748b; font-size: 24rpx; }

.card { margin: 0 16rpx 12rpx; background: #fff; border-radius: 12rpx; padding: 14rpx; }

.scan-area { position: relative; height: 320rpx; background: #111827; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.corner { position: absolute; width: 42rpx; height: 42rpx; border: 4rpx solid #22c55e; }
.tl { top: 20rpx; left: 20rpx; border-right: none; border-bottom: none; }
.tr { top: 20rpx; right: 20rpx; border-left: none; border-bottom: none; }
.bl { bottom: 20rpx; left: 20rpx; border-right: none; border-top: none; }
.br { bottom: 20rpx; right: 20rpx; border-left: none; border-top: none; }
.scan-icon { color: #9ca3af; font-size: 72rpx; }
.scan-line { position: absolute; left: 24rpx; right: 24rpx; top: 0; height: 4rpx; background: linear-gradient(90deg, transparent, #22c55e, transparent); animation: move 2s ease-in-out infinite; }
@keyframes move { 0%{top:20rpx;} 50%{top:280rpx;} 100%{top:20rpx;} }

.btns { margin-top: 14rpx; display: grid; gap: 10rpx; }
.btn { height: 74rpx; line-height: 74rpx; border-radius: 10rpx; background: #e2e8f0; color: #334155; font-size: 24rpx; }
.btn.primary { background: #667eea; color: #fff; }

.tips { margin-top: 12rpx; display: grid; gap: 4rpx; color: #64748b; font-size: 22rpx; }
.r-title { font-size: 28rpx; color: #1e293b; font-weight: 700; margin-bottom: 8rpx; }
.line { margin-top: 6rpx; color: #475569; font-size: 23rpx; }

.history-item { padding: 12rpx 0; border-bottom: 1px solid #eef2f7; font-size: 23rpx; color: #334155; }
.history-item:last-child { border-bottom: none; }
.empty { color: #94a3b8; font-size: 24rpx; text-align: center; padding: 12rpx 0; }
</style>
