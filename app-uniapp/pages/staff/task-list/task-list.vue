<template>
  <view class="page">
    <view class="card head">
      <view>
        <text class="title">任务派发</text>
        <text class="sub">会议执行任务统一调度</text>
      </view>
      <button class="create-btn" size="mini" @click="createTask">+ 新建</button>
    </view>

    <view class="card stats">
      <view class="stat"><text class="num pending">{{ summary.pending }}</text><text class="label">待执行</text></view>
      <view class="stat"><text class="num doing">{{ summary.processing }}</text><text class="label">进行中</text></view>
      <view class="stat"><text class="num done">{{ summary.done }}</text><text class="label">已完成</text></view>
      <view class="stat"><text class="num overdue">{{ summary.overdue }}</text><text class="label">已逾期</text></view>
    </view>

    <scroll-view class="tabs" scroll-x>
      <view class="tab" :class="{ active: filterStatus === 'all' }" @click="setFilter('all')">全部</view>
      <view class="tab" :class="{ active: filterStatus === 'pending' }" @click="setFilter('pending')">待执行</view>
      <view class="tab" :class="{ active: filterStatus === 'processing' }" @click="setFilter('processing')">进行中</view>
      <view class="tab" :class="{ active: filterStatus === 'done' }" @click="setFilter('done')">已完成</view>
      <view class="tab" :class="{ active: filterStatus === 'overdue' }" @click="setFilter('overdue')">已逾期</view>
    </scroll-view>

    <view class="card task" v-for="item in showTasks" :key="item.id" @click="openDetail(item)">
      <view class="row">
        <text class="task-title">{{ item.title }}</text>
        <text class="chip" :class="item.status">{{ statusText[item.status] }}</text>
      </view>
      <text class="meta">负责人：{{ item.owners }} · 截止 {{ item.deadline }}</text>
      <text class="desc">{{ item.desc }}</text>
      <text class="foot">优先级：{{ item.priority }} · 最新反馈：{{ item.feedback }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'

const filterStatus = ref('all')
const tasks = ref([])

const statusText = {
  pending: '待执行',
  processing: '进行中',
  done: '已完成',
  overdue: '已逾期'
}

const summary = reactive({
  pending: 0,
  processing: 0,
  done: 0,
  overdue: 0
})

const showTasks = computed(() => {
  if (filterStatus.value === 'all') return tasks.value
  return tasks.value.filter((x) => x.status === filterStatus.value)
})

const mockTasks = () => ([
  {
    id: 1,
    title: '【紧急】会场布置验收',
    status: 'processing',
    owners: '李伟、陈静',
    deadline: '08:30',
    desc: '需提交现场照片并完成设备验收。',
    priority: '高',
    feedback: '舞台搭建完成'
  },
  {
    id: 2,
    title: '资料礼包发放',
    status: 'done',
    owners: '赵敏',
    deadline: '07:50',
    desc: '142 份礼包发放完毕。',
    priority: '中',
    feedback: '执行高效'
  },
  {
    id: 3,
    title: '下午课程讲师更新',
    status: 'pending',
    owners: '王芳',
    deadline: '14:00',
    desc: '更新讲师信息并推送至全体学员。',
    priority: '中',
    feedback: '待开始'
  }
])

const rebuildSummary = () => {
  summary.pending = tasks.value.filter((x) => x.status === 'pending').length
  summary.processing = tasks.value.filter((x) => x.status === 'processing').length
  summary.done = tasks.value.filter((x) => x.status === 'done').length
  summary.overdue = tasks.value.filter((x) => x.status === 'overdue').length
}

const loadTasks = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8084/api/task/staff/list',
      method: 'GET'
    })
    const rows = res?.data?.data || res?.data?.rows
    tasks.value = Array.isArray(rows) && rows.length ? rows : mockTasks()
  } catch (e) {
    tasks.value = mockTasks()
  }
  rebuildSummary()
}

const setFilter = (status) => {
  filterStatus.value = status
}

const openDetail = (item) => {
  uni.navigateTo({
    url: `/pages/staff/task-detail/task-detail?id=${item.id}`
  })
}

const createTask = () => {
  uni.showToast({ title: '任务创建入口已预留', icon: 'none' })
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '任务派发' })
  loadTasks()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.head { display: flex; justify-content: space-between; align-items: center; }
.title { display: block; font-size: 34rpx; font-weight: 700; }
.sub { display: block; margin-top: 8rpx; color: #6b7280; font-size: 24rpx; }
.create-btn { background: #2563eb; color: #fff; border-radius: 10rpx; }
.stats { display: flex; justify-content: space-between; }
.stat { width: 24%; text-align: center; }
.num { display: block; font-size: 32rpx; font-weight: 700; }
.label { display: block; font-size: 22rpx; color: #6b7280; margin-top: 4rpx; }
.num.pending { color: #f59e0b; }
.num.doing { color: #6366f1; }
.num.done { color: #22c55e; }
.num.overdue { color: #ef4444; }
.tabs { white-space: nowrap; margin-bottom: 16rpx; }
.tab { display: inline-block; margin-right: 12rpx; padding: 8rpx 18rpx; background: #e5e7eb; border-radius: 999rpx; font-size: 23rpx; color: #374151; }
.tab.active { background: #2563eb; color: #fff; }
.task-title { font-size: 29rpx; font-weight: 600; color: #111827; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 10rpx; }
.chip { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 999rpx; color: #fff; }
.chip.pending { background: #f59e0b; }
.chip.processing { background: #6366f1; }
.chip.done { background: #22c55e; }
.chip.overdue { background: #ef4444; }
.meta { display: block; margin-top: 10rpx; font-size: 23rpx; color: #4b5563; }
.desc { display: block; margin-top: 6rpx; font-size: 24rpx; color: #111827; }
.foot { display: block; margin-top: 8rpx; font-size: 22rpx; color: #6b7280; }
</style>
