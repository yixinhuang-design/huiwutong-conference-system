<template>
  <view class="page">
    <view class="head card">
      <text class="title">通知中心</text>
      <button class="all-read" size="mini" @click="markAllRead">全部已读</button>
    </view>

    <text class="section">未读通知</text>
    <view class="card item" v-for="item in unreadList" :key="item.id" @click="open(item)">
      <view class="row">
        <text class="n-title">{{ item.title }}</text>
        <text class="tag unread">未读</text>
      </view>
      <text class="time">{{ item.time }}</text>
      <text class="desc">{{ item.desc }}</text>
    </view>

    <text class="section">已读通知</text>
    <view class="card item read" v-for="item in readList" :key="item.id" @click="open(item)">
      <view class="row">
        <text class="n-title">{{ item.title }}</text>
        <text class="tag read">已读</text>
      </view>
      <text class="time">{{ item.time }}</text>
      <text class="desc">{{ item.desc }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'

const notifications = ref([])

const unreadList = computed(() => notifications.value.filter((x) => !x.read))
const readList = computed(() => notifications.value.filter((x) => x.read))

const mockList = () => ([
  { id: 1, title: '座位安排通知', time: '2025-02-05 14:30', desc: '前区 · 第3排 · 12座，市委党校报告厅', read: false, type: 'seat' },
  { id: 2, title: '培训会议通知', time: '2025-02-05 09:00', desc: '2025党务干部培训班，市委党校报告厅', read: false, type: 'meeting' },
  { id: 3, title: '签到提醒', time: '2025-02-06 08:00', desc: '请携带身份证件，提前15分钟到达签到', read: false, type: 'checkin' },
  { id: 4, title: '报名成功通知', time: '2025-02-04 16:30', desc: '您的报名已通过审核，欢迎参加培训班', read: true, type: 'home' }
])

const loadList = async () => {
  try {
    // 通过网关代理访问通知服务
    const res = await uni.request({
      url: 'http://localhost:8080/api/notification/list',
      method: 'GET',
      data: { page: 1, pageSize: 50 }
    })
    const body = res?.data || {}
    const records = body.data?.records || body.data || body.rows || []
    if (Array.isArray(records) && records.length > 0) {
      notifications.value = records.map(n => ({
        id: n.id,
        title: n.title,
        time: n.sentTime || n.createTime || '',
        desc: n.content || '',
        read: false,
        type: n.type || 'system'
      }))
    } else {
      notifications.value = mockList()
    }
  } catch (e) {
    notifications.value = mockList()
  }
}

const markAllRead = async () => {
  // 调用后端标记全部已读
  try {
    await uni.request({
      url: 'http://localhost:8080/api/notification/read-all',
      method: 'PUT',
      header: { 'Content-Type': 'application/json' },
      data: JSON.stringify({ conferenceId: 0, userId: 0 })
    })
  } catch (e) { /* ignore */ }
  notifications.value = notifications.value.map((x) => ({ ...x, read: true }))
  uni.showToast({ title: '已全部标记已读', icon: 'none' })
}

const open = (item) => {
  item.read = true
  // 调用后端标记单条已读
  try {
    uni.request({
      url: `http://localhost:8080/api/notification/${item.id}/read`,
      method: 'PUT'
    })
  } catch (e) { /* ignore */ }
  if (item.type === 'seat') {
    uni.navigateTo({ url: '/pages/learner/seat/seat' })
    return
  }
  if (item.type === 'meeting') {
    uni.navigateTo({ url: '/pages/learner/meeting-detail/meeting-detail' })
    return
  }
  if (item.type === 'checkin') {
    uni.navigateTo({ url: '/pages/learner/checkin/checkin' })
    return
  }
  uni.navigateTo({ url: '/pages/learner/home/home' })
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '通知中心' })
  loadList()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 14rpx; padding: 20rpx; margin-bottom: 14rpx; }
.head { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 32rpx; font-weight: 700; color: #111827; }
.all-read { background: #f3f4f6; color: #374151; border: 1rpx solid #d1d5db; }
.section { display: block; margin: 10rpx 0; color: #4b5563; font-size: 24rpx; font-weight: 600; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 8rpx; }
.n-title { font-size: 28rpx; color: #111827; font-weight: 600; }
.tag { font-size: 20rpx; padding: 4rpx 10rpx; border-radius: 999rpx; color: #fff; }
.tag.unread { background: #f59e0b; }
.tag.read { background: #9ca3af; }
.time { display: block; margin-top: 8rpx; color: #6b7280; font-size: 22rpx; }
.desc { display: block; margin-top: 6rpx; color: #374151; font-size: 24rpx; }
.item.read { opacity: 0.75; }
</style>
