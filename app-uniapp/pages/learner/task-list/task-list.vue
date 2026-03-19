<template>
  <view class="page">
    <view class="head card">
      <text class="title">任务列表</text>
      <text class="sub">按截止时间优先完成任务</text>
    </view>

    <view class="card task" v-for="item in tasks" :key="item.id" @click="openDetail(item)">
      <view class="row">
        <text class="task-title">{{ item.title }}</text>
        <text class="status" :class="item.status">{{ statusText[item.status] }}</text>
      </view>
      <text class="desc">{{ item.desc }}</text>
      <text class="time">{{ item.deadline }}</text>
      <button class="btn" :class="{ primary: item.status !== 'optional' }" size="mini">{{ item.action }}</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const statusText = {
  pending: '待完成',
  ongoing: '进行中',
  optional: '可选'
}

const tasks = ref([])

const mockTasks = () => ([
  { id: 1, type: 'feedback', title: '培训满意度调查', desc: '请填写培训反馈问卷，帮助我们改进培训质量', deadline: '明天 18:00截止', status: 'pending', action: '填写' },
  { id: 2, type: 'evaluation', title: '讲师评价', desc: '为今日讲师进行评分和评价', deadline: '今天 22:00截止', status: 'pending', action: '评价' },
  { id: 3, type: 'scene', title: '现场合影', desc: '在指定地点完成小组合影任务', deadline: '今天 14:00-16:00', status: 'ongoing', action: '上传' },
  { id: 4, type: 'collection', title: '心得体会征集', desc: '分享培训心得，优秀作品将展示', deadline: '本周五截止', status: 'optional', action: '提交' }
])

const loadTasks = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8082/api/task/list',
      method: 'GET'
    })
    const rows = res?.data?.data || res?.data?.rows
    tasks.value = Array.isArray(rows) && rows.length ? rows : mockTasks()
  } catch (e) {
    tasks.value = mockTasks()
  }
}

const openDetail = (item) => {
  uni.navigateTo({
    url: `/pages/learner/task-detail/task-detail?type=${item.type}&id=${item.id}`
  })
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '任务列表' })
  loadTasks()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.title { display: block; font-size: 34rpx; font-weight: 700; }
.sub { display: block; margin-top: 8rpx; color: #6b7280; font-size: 24rpx; }
.task-title { font-size: 29rpx; font-weight: 600; color: #111827; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; }
.status { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 999rpx; color: #fff; }
.status.pending { background: #f59e0b; }
.status.ongoing { background: #2563eb; }
.status.optional { background: #6b7280; }
.desc { display: block; font-size: 24rpx; color: #4b5563; margin: 12rpx 0 8rpx; }
.time { display: block; font-size: 22rpx; color: #6b7280; }
.btn { margin-top: 14rpx; border: 1rpx solid #d1d5db; background: #fff; color: #374151; }
.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; }
</style>
