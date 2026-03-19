<template>
  <view class="page">
    <view class="card">
      <view class="row">
        <text class="title">{{ detail.title }}</text>
        <text class="status">{{ statusText[detail.status] }}</text>
      </view>
      <text class="meta">负责人：{{ detail.owners }}</text>
      <text class="meta">开始：{{ detail.startTime }} · 截止：{{ detail.deadline }}</text>
    </view>

    <view class="card">
      <text class="sec-title">任务内容</text>
      <view class="check-item" v-for="(item, idx) in detail.contents" :key="idx">
        <text class="dot">•</text>
        <text class="check-text">{{ item }}</text>
      </view>
    </view>

    <view class="card">
      <text class="sec-title">进度与反馈</text>
      <view class="timeline-item" v-for="(t, i) in detail.timeline" :key="i">
        <text class="time">{{ t.time }}</text>
        <text class="content">{{ t.content }}</text>
      </view>

      <textarea v-model="newFeedback" class="ta" placeholder="填写进度更新或备注说明" />
      <button class="btn" @click="submitFeedback">提交反馈</button>
    </view>

    <view class="actions">
      <button class="btn" @click="applyDelay">延期申请</button>
      <button class="btn primary" @click="markDone">标记完成</button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'

const detail = reactive({
  id: '',
  title: '会场布置验收',
  status: 'processing',
  owners: '李伟（会场组长）、陈静（物料组）',
  startTime: '01-15 07:00',
  deadline: '01-15 08:30',
  contents: [
    '主席台布置：摆放背景板、横幅，检查音响设备。',
    '签到处搭建：启用签到机，摆放资料礼包与指引牌。',
    '现场巡检：确认投影、照明、温度等设备正常。'
  ],
  timeline: [
    { time: '07:10 · 李伟', content: '舞台布置完成，音响已通电测试。' },
    { time: '07:45 · 陈静', content: '签到设备连接完成，物料礼包摆放完毕。' },
    { time: '当前状态', content: '等待物料组补充 2 张海报，预计 08:05 完成。' }
  ]
})

const newFeedback = ref('')

const statusText = {
  pending: '待执行',
  processing: '进行中',
  done: '已完成',
  overdue: '已逾期'
}

const loadDetail = async () => {
  try {
    const res = await uni.request({
      url: `http://localhost:8084/api/task/detail?id=${detail.id}`,
      method: 'GET'
    })
    const d = res?.data?.data
    if (d) {
      detail.title = d.title || detail.title
      detail.status = d.status || detail.status
      detail.owners = d.owners || detail.owners
      detail.startTime = d.startTime || detail.startTime
      detail.deadline = d.deadline || detail.deadline
      detail.contents = Array.isArray(d.contents) && d.contents.length ? d.contents : detail.contents
      detail.timeline = Array.isArray(d.timeline) && d.timeline.length ? d.timeline : detail.timeline
    }
  } catch (e) {}
}

const submitFeedback = async () => {
  if (!newFeedback.value.trim()) {
    uni.showToast({ title: '请输入反馈内容', icon: 'none' })
    return
  }

  detail.timeline.unshift({
    time: '刚刚 · 我',
    content: newFeedback.value.trim()
  })

  try {
    await uni.request({
      url: 'http://localhost:8084/api/task/feedback',
      method: 'POST',
      data: { id: detail.id, content: newFeedback.value.trim() }
    })
  } catch (e) {}

  newFeedback.value = ''
  uni.showToast({ title: '反馈已提交', icon: 'success' })
}

const applyDelay = async () => {
  try {
    await uni.request({
      url: 'http://localhost:8084/api/task/delay/apply',
      method: 'POST',
      data: { id: detail.id }
    })
  } catch (e) {}
  uni.showToast({ title: '已提交延期申请', icon: 'none' })
}

const markDone = async () => {
  detail.status = 'done'
  try {
    await uni.request({
      url: 'http://localhost:8084/api/task/complete',
      method: 'POST',
      data: { id: detail.id }
    })
  } catch (e) {}
  uni.showToast({ title: '任务已标记完成', icon: 'success' })
}

onLoad((options) => {
  uni.setNavigationBarTitle({ title: '任务详情' })
  detail.id = options?.id || '1'
  loadDetail()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; }
.title { font-size: 32rpx; font-weight: 700; color: #111827; }
.status { color: #6366f1; font-size: 22rpx; }
.meta { display: block; margin-top: 8rpx; color: #4b5563; font-size: 24rpx; }
.sec-title { display: block; font-size: 28rpx; font-weight: 600; color: #111827; margin-bottom: 10rpx; }
.check-item { display: flex; align-items: flex-start; margin: 8rpx 0; }
.dot { margin-right: 8rpx; color: #2563eb; }
.check-text { flex: 1; color: #374151; font-size: 24rpx; }
.timeline-item { padding: 10rpx 0; border-bottom: 1rpx solid #f1f5f9; }
.time { display: block; font-size: 22rpx; color: #6b7280; }
.content { display: block; margin-top: 4rpx; font-size: 24rpx; color: #111827; }
.ta { width: 100%; min-height: 120rpx; margin-top: 14rpx; border: 1rpx solid #d1d5db; border-radius: 10rpx; padding: 12rpx; box-sizing: border-box; }
.actions { display: flex; gap: 16rpx; }
.btn { flex: 1; border: 1rpx solid #d1d5db; background: #fff; color: #374151; font-size: 26rpx; margin-top: 10rpx; }
.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; }
</style>
