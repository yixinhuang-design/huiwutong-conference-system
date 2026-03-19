<template>
  <view class="page">
    <view class="card head">
      <text class="title">培训日程表</text>
      <text class="sub">2025党务干部培训班</text>
    </view>

    <view class="card" v-for="item in scheduleList" :key="item.id" :class="{ important: item.important }">
      <view class="row">
        <text class="time">{{ item.date }} {{ item.time }}</text>
        <text class="badge" :class="item.type">{{ item.typeText }}</text>
      </view>
      <text class="name">{{ item.name }}</text>
      <text class="loc">{{ item.location }}</text>
      <view class="actions">
        <button class="btn" size="mini" @click="remind(item)">提醒</button>
        <button v-if="item.type === 'checkin'" class="btn primary" size="mini" @click="goCheckin">签到</button>
      </view>
    </view>

    <view class="tips">温馨提示：点击“提醒”可设置本地提醒。</view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ScheduleAPI from '../../../common/api/schedule-api'

const scheduleList = ref([])
const loading = ref(false)

/**
 * 模拟数据（备用）
 */
const mockData = () => ([
  { id: 1, date: '1月10日', time: '14:00', name: '入住酒店', location: '维也纳酒店一楼大堂', type: 'checkin', typeText: '签到', important: false },
  { id: 2, date: '1月10日', time: '18:00', name: '自助晚餐', location: '酒店自助餐厅', type: 'meal', typeText: '餐饮', important: false },
  { id: 3, date: '1月11日', time: '08:45', name: '主会场会议', location: '越秀会议中心101 · B区2排3座', type: 'meeting', typeText: '会议', important: true },
  { id: 4, date: '1月11日', time: '12:00', name: '自助午餐', location: '越秀会议中心5F', type: 'meal', typeText: '餐饮', important: false },
  { id: 5, date: '1月12日', time: '09:00', name: '分会场会议', location: '越秀会议中心6F101', type: 'meeting', typeText: '会议', important: false }
])

/**
 * 转换后端数据为前端展示格式
 */
const formatScheduleData = (schedules) => {
  if (!Array.isArray(schedules)) return []
  return schedules.map(item => {
    // 解析后端时间格式
    const startTime = item.startTime ? new Date(item.startTime) : null
    const date = startTime ? `${startTime.getMonth() + 1}月${startTime.getDate()}日` : '未知'
    const time = startTime ? `${String(startTime.getHours()).padStart(2, '0')}:${String(startTime.getMinutes()).padStart(2, '0')}` : '未知'
    
    // 判断类型和优先级
    const needsCheckin = item.settings?.needCheckin || false
    const isImportant = item.priority >= 2
    const type = needsCheckin ? 'checkin' : 'meeting'
    const typeText = needsCheckin ? '签到' : (item.status === 2 ? '进行中' : '会议')
    
    return {
      id: item.id,
      date,
      time,
      name: item.title,
      location: item.location || '待定',
      type,
      typeText,
      important: isImportant,
      needsCheckin,
      ...item
    }
  })
}

/**
 * 加载日程列表
 */
const loadSchedule = async () => {
  loading.value = true
  try {
    // 从本地存储获取会议ID（应在登录时保存）
    const meetingId = uni.getStorageSync('currentMeetingId') || 1
    
    // 调用API获取日程
    const schedules = await ScheduleAPI.allSchedules(meetingId)
    
    if (Array.isArray(schedules) && schedules.length > 0) {
      scheduleList.value = formatScheduleData(schedules)
    } else {
      // 使用模拟数据作为备用
      scheduleList.value = mockData()
      uni.showToast({ title: '暂无日程数据', icon: 'none' })
    }
  } catch (error) {
    console.error('加载日程失败:', error)
    // 失败时使用本地模拟数据
    scheduleList.value = mockData()
    uni.showToast({ title: '加载失败，使用本地数据', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/**
 * 设置提醒
 */
const remind = (item) => {
  // 可集成推送通知服务
  uni.showToast({ title: `已设置提醒：${item.name}`, icon: 'none' })
  
  // TODO: 调用后端提醒API
  // ScheduleAPI.setReminder(item.id, { remindTime: 15 })
}

/**
 * 跳转到签到页面
 */
const goCheckin = () => {
  uni.navigateTo({ url: '/pages/learner/checkin/checkin' })
}

/**
 * 页面加载时调用
 */
onMounted(() => {
  uni.setNavigationBarTitle({ title: '日程安排' })
  loadSchedule()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 14rpx; padding: 20rpx; margin-bottom: 16rpx; }
.head .title { display: block; font-size: 32rpx; font-weight: 700; }
.head .sub { display: block; margin-top: 8rpx; font-size: 24rpx; color: #6b7280; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 10rpx; }
.time { font-size: 24rpx; color: #2563eb; font-weight: 600; }
.badge { font-size: 20rpx; color: #fff; padding: 4rpx 10rpx; border-radius: 999rpx; }
.badge.meeting { background: #ef4444; }
.badge.checkin { background: #22c55e; }
.badge.meal { background: #3b82f6; }
.name { display: block; margin-top: 10rpx; font-size: 28rpx; color: #111827; font-weight: 600; }
.loc { display: block; margin-top: 6rpx; font-size: 23rpx; color: #6b7280; }
.actions { margin-top: 12rpx; display: flex; gap: 10rpx; }
.btn { border: 1rpx solid #d1d5db; background: #fff; color: #374151; }
.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; }
.important { background: #fffbeb; border: 1rpx solid #fde68a; }
.tips { margin-top: 8rpx; font-size: 23rpx; color: #0f766e; background: #ecfeff; border-radius: 12rpx; padding: 14rpx; }
</style>
