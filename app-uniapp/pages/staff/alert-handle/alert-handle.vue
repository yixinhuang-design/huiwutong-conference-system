<template>
  <view class="page">
    <view class="section title-card">
      <text class="title">预警处理</text>
      <text class="sub">实时跟进预警并闭环处理</text>
    </view>

    <view class="section filter-row">
      <picker :range="levelOptions" :value="levelIndex" @change="onLevelChange">
        <view class="picker">级别：{{ levelOptions[levelIndex] }}</view>
      </picker>
      <picker :range="statusOptions" :value="statusIndex" @change="onStatusChange">
        <view class="picker">状态：{{ statusOptions[statusIndex] }}</view>
      </picker>
    </view>

    <view class="section stats">
      <view class="stat-item">
        <text class="num">{{ stats.total }}</text>
        <text class="label">总数</text>
      </view>
      <view class="stat-item warn">
        <text class="num">{{ stats.high }}</text>
        <text class="label">高级别</text>
      </view>
      <view class="stat-item done">
        <text class="num">{{ stats.done }}</text>
        <text class="label">已处理</text>
      </view>
    </view>

    <view class="section" v-for="item in filteredAlerts" :key="item.id">
      <view class="alert-head">
        <view>
          <text class="alert-title">{{ item.title }}</text>
          <text class="alert-sub">{{ item.time }} · {{ item.metric }}</text>
        </view>
        <text class="tag" :class="item.level">{{ levelText(item.level) }}</text>
      </view>
      <text class="desc">{{ item.reason }}</text>
      <view class="action-row">
        <button class="btn" @click="markProcessing(item)" v-if="item.status === 'pending'">标记处理中</button>
        <button class="btn primary" @click="markDone(item)" v-if="item.status !== 'done'">完成</button>
        <button class="btn" @click="reopen(item)" v-else>重新打开</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'

const levelOptions = ['全部', '高', '中', '低']
const statusOptions = ['全部', '待处理', '处理中', '已处理']
const levelIndex = ref(0)
const statusIndex = ref(0)

const alerts = ref([])

const stats = reactive({
  total: 0,
  high: 0,
  done: 0
})

const levelMap = {
  high: '高',
  medium: '中',
  low: '低'
}

const statusMap = {
  pending: '待处理',
  processing: '处理中',
  done: '已处理'
}

const levelText = (level) => levelMap[level] || level

const filteredAlerts = computed(() => {
  const levelTextValue = levelOptions[levelIndex.value]
  const statusTextValue = statusOptions[statusIndex.value]
  return alerts.value.filter((item) => {
    const levelOk = levelTextValue === '全部' || levelMap[item.level] === levelTextValue
    const statusOk = statusTextValue === '全部' || statusMap[item.status] === statusTextValue
    return levelOk && statusOk
  })
})

const updateStats = () => {
  stats.total = alerts.value.length
  stats.high = alerts.value.filter((x) => x.level === 'high').length
  stats.done = alerts.value.filter((x) => x.status === 'done').length
}

const mockAlerts = () => [
  { id: 1, title: '签到率过低预警', time: '2026-03-09 10:30', metric: '签到率 65%', level: 'high', status: 'pending', reason: '开班仪式签到人数不足' },
  { id: 2, title: '报名率偏低预警', time: '2026-03-09 09:15', metric: '报名率 78%', level: 'medium', status: 'processing', reason: '距离报名截止仅剩2天' },
  { id: 3, title: '任务完成率预警', time: '2026-03-08 16:45', metric: '完成率 55%', level: 'medium', status: 'done', reason: '部分执行任务延期' }
]

const loadAlerts = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8084/api/alerts/list',
      method: 'GET'
    })
    const rows = res?.data?.data || res?.data?.rows
    alerts.value = Array.isArray(rows) && rows.length ? rows : mockAlerts()
  } catch (e) {
    alerts.value = mockAlerts()
  }
  updateStats()
}

const markProcessing = async (item) => {
  item.status = 'processing'
  updateStats()
  try {
    await uni.request({
      url: 'http://localhost:8084/api/alerts/updateStatus',
      method: 'POST',
      data: { id: item.id, status: 'processing' }
    })
  } catch (e) {}
}

const markDone = async (item) => {
  item.status = 'done'
  updateStats()
  try {
    await uni.request({
      url: 'http://localhost:8084/api/alerts/updateStatus',
      method: 'POST',
      data: { id: item.id, status: 'done' }
    })
  } catch (e) {}
}

const reopen = async (item) => {
  item.status = 'pending'
  updateStats()
  try {
    await uni.request({
      url: 'http://localhost:8084/api/alerts/updateStatus',
      method: 'POST',
      data: { id: item.id, status: 'pending' }
    })
  } catch (e) {}
}

const onLevelChange = (e) => {
  levelIndex.value = Number(e.detail.value)
}

const onStatusChange = (e) => {
  statusIndex.value = Number(e.detail.value)
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '预警处理' })
  loadAlerts()
})
</script>

<style scoped>
.page { padding: 24rpx; background: #f5f7fb; min-height: 100vh; }
.section { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.title-card .title { font-size: 34rpx; font-weight: 700; display: block; }
.title-card .sub { font-size: 24rpx; color: #6b7280; margin-top: 8rpx; display: block; }
.filter-row { display: flex; gap: 16rpx; }
.picker { background: #f3f4f6; padding: 12rpx 16rpx; border-radius: 12rpx; font-size: 24rpx; }
.stats { display: flex; justify-content: space-between; }
.stat-item { width: 31%; background: #eef2ff; padding: 14rpx; border-radius: 12rpx; text-align: center; }
.stat-item.warn { background: #fff7ed; }
.stat-item.done { background: #ecfdf5; }
.num { display: block; font-weight: 700; font-size: 30rpx; }
.label { color: #6b7280; font-size: 22rpx; }
.alert-head { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; }
.alert-title { display: block; font-size: 30rpx; font-weight: 600; }
.alert-sub { display: block; color: #6b7280; font-size: 22rpx; margin-top: 4rpx; }
.desc { display: block; margin: 12rpx 0; color: #374151; font-size: 24rpx; }
.tag { font-size: 20rpx; padding: 6rpx 12rpx; border-radius: 999rpx; color: #fff; }
.tag.high { background: #ef4444; }
.tag.medium { background: #f59e0b; }
.tag.low { background: #10b981; }
.action-row { display: flex; gap: 12rpx; }
.btn { flex: 1; border: 1rpx solid #d1d5db; background: #fff; color: #374151; font-size: 24rpx; border-radius: 10rpx; }
.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; }
</style>
