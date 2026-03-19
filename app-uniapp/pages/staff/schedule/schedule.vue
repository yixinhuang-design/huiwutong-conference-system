<template>
  <scroll-view class="page" scroll-y>
    <view class="header-card">
      <view class="title-row">
        <text class="title">2025党务干部培训班</text>
        <button class="small-btn" @click="showAdd = true">+ 新增日程</button>
      </view>
      <view class="pill-group">
        <view class="pill" :class="{ active: filter === 'all' }" @click="filter = 'all'">全部日程</view>
        <view class="pill" :class="{ active: filter === 'today' }" @click="filter = 'today'">今日</view>
        <view class="pill" :class="{ active: filter === 'pending' }" @click="filter = 'pending'">待确认</view>
      </view>
    </view>

    <view class="block" v-for="part in groupedList" :key="part.key">
      <view class="block-title">{{ part.label }}</view>
      <view class="item" v-for="s in part.items" :key="s.id">
        <view class="item-main">
          <view class="line1">{{ s.timeRange }} {{ s.title }}</view>
          <view class="line2">负责人：{{ s.host || '未设置' }} · 地点：{{ s.location || '未设置' }}</view>
        </view>
        <text class="status" :class="statusClass(s.status)">{{ statusText(s.status) }}</text>
      </view>
      <view class="empty" v-if="part.items.length === 0">暂无日程</view>
    </view>

    <view class="mask" v-if="showAdd" @click="showAdd = false">
      <view class="modal" @click.stop>
        <view class="modal-title">新增日程</view>
        <input class="input" v-model="draft.title" placeholder="日程标题 *" />
        <view class="row">
          <input class="input" v-model="draft.start" type="datetime-local" placeholder="开始时间" />
          <input class="input" v-model="draft.end" type="datetime-local" placeholder="结束时间" />
        </view>
        <input class="input" v-model="draft.location" placeholder="日程地点" />
        <input class="input" v-model="draft.host" placeholder="负责人/讲师" />
        <picker class="picker" :range="statusOpts" @change="onStatusChange">
          <view class="picker-value">状态：{{ draft.status }}</view>
        </picker>
        <view class="row" style="margin-top:10rpx">
          <button class="btn light" @click="showAdd = false">取消</button>
          <button class="btn" @click="saveSchedule">保存</button>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import ScheduleAPI from '../../../common/api/schedule-api'

const filter = ref('all')
const showAdd = ref(false)
const loading = ref(false)
const schedules = ref([])
const statusOpts = ['已发布', '待确认', '草稿']

const draft = reactive({
  title: '',
  start: '',
  end: '',
  location: '',
  host: '',
  speaker: '',
  status: '待确认',
  priority: 1,
  meetingId: null,
})

/**
 * 状态转换
 */
const statusMap = {
  '待确认': 0,  // draft
  '已发布': 1,  // published
  '进行中': 2,  // ongoing
  '已结束': 3,  // completed
  '已取消': 4,  // cancelled
}

const statusReverseMap = {
  0: '待确认',
  1: '已发布',
  2: '进行中',
  3: '已结束',
  4: '已取消',
}

function onStatusChange(e) {
  draft.status = statusOpts[Number(e.detail.value)]
}

/**
 * 模拟数据
 */
function mockList() {
  schedules.value = [
    { id: 1, part: 'morning', timeRange: '08:00 - 09:00', title: '报到签到', host: '会务组', location: '报告厅门厅', status: '已发布' },
    { id: 2, part: 'morning', timeRange: '09:00 - 10:30', title: '开班仪式', host: '张主任', location: '报告厅', status: '已发布' },
    { id: 3, part: 'morning', timeRange: '10:30 - 12:00', title: '第一堂课-党务工作创新', host: '李教授', location: 'A202', status: '已发布' },
    { id: 4, part: 'noon', timeRange: '12:00 - 13:00', title: '午餐休息', host: '', location: '餐厅', status: '已发布' },
    { id: 5, part: 'afternoon', timeRange: '13:00 - 14:30', title: '分组讨论', host: '', location: '各分会场', status: '待确认' },
    { id: 6, part: 'afternoon', timeRange: '14:30 - 16:00', title: '第二堂课-实践经验分享', host: '王教授', location: 'A202', status: '草稿' },
  ]
}

/**
 * 转换后端数据为前端显示格式
 */
function formatScheduleData(apiSchedules) {
  return apiSchedules.map((item) => {
    const startTime = item.startTime ? new Date(item.startTime) : null
    const endTime = item.endTime ? new Date(item.endTime) : null
    
    const startHour = startTime ? String(startTime.getHours()).padStart(2, '0') : '00'
    const startMin = startTime ? String(startTime.getMinutes()).padStart(2, '0') : '00'
    const endHour = endTime ? String(endTime.getHours()).padStart(2, '0') : '00'
    const endMin = endTime ? String(endTime.getMinutes()).padStart(2, '0') : '00'
    
    const timeRange = `${startHour}:${startMin} - ${endHour}:${endMin}`
    
    // 根据时间判断部分（上午9-12，下午13-18）
    const startHourNum = parseInt(startHour)
    let part = 'afternoon'
    if (startHourNum < 12) part = 'morning'
    else if (startHourNum < 13) part = 'noon'
    
    return {
      id: item.id,
      part,
      timeRange,
      title: item.title,
      host: item.host || '',
      location: item.location || '',
      status: statusReverseMap[item.status] || '待确认',
      ...item,
    }
  })
}

/**
 * 加载日程列表
 */
async function loadSchedules() {
  loading.value = true
  try {
    // 从本地存储获取会议ID
    const meetingId = uni.getStorageSync('currentMeetingId') || 1
    draft.meetingId = meetingId
    
    // 调用API加载日程
    const apiSchedules = await ScheduleAPI.allSchedules(meetingId)
    
    if (Array.isArray(apiSchedules) && apiSchedules.length > 0) {
      schedules.value = formatScheduleData(apiSchedules)
    } else {
      mockList()
    }
  } catch (error) {
    console.error('加载日程失败:', error)
    mockList()
    uni.showToast({ title: '加载失败，使用本地数据', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function statusText(v) { return v }
function statusClass(v) {
  if (v === '已发布') return 'ok'
  if (v === '草稿' || v === '待确认') return 'draft'
  if (v === '进行中') return 'running'
  return 'wait'
}

const filtered = computed(() => {
  if (filter.value === 'pending') return schedules.value.filter((s) => s.status === '待确认')
  if (filter.value === 'today') return schedules.value
  return schedules.value
})

const groupedList = computed(() => {
  const src = filtered.value
  return [
    { key: 'morning', label: '上午日程', items: src.filter((i) => i.part === 'morning') },
    { key: 'noon', label: '午间休息', items: src.filter((i) => i.part === 'noon') },
    { key: 'afternoon', label: '下午日程', items: src.filter((i) => i.part === 'afternoon') },
    { key: 'evening', label: '晚间日程', items: src.filter((i) => i.part === 'evening') },
  ]
})

/**
 * 保存日程
 */
async function saveSchedule() {
  if (!draft.title.trim()) {
    uni.showToast({ title: '请输入日程标题', icon: 'none' })
    return
  }
  if (!draft.start) {
    uni.showToast({ title: '请输入开始时间', icon: 'none' })
    return
  }

  try {
    loading.value = true
    const scheduleData = {
      title: draft.title,
      startTime: draft.start,
      endTime: draft.end || draft.start,
      location: draft.location,
      host: draft.host,
      speaker: draft.speaker,
      priority: draft.priority,
      notes: '',
      settings: {
        needCheckin: false,
        needReminder: false,
        needReport: false,
      },
    }

    // 调用API保存
    await ScheduleAPI.createSchedule(draft.meetingId, scheduleData)
    
    // 重新加载列表
    await loadSchedules()
    
    // 重置表单
    draft.title = ''
    draft.start = ''
    draft.end = ''
    draft.location = ''
    draft.host = ''
    draft.speaker = ''
    draft.status = '待确认'
    draft.priority = 1
    showAdd.value = false
    
    uni.showToast({ title: '日程已保存', icon: 'success' })
  } catch (error) {
    console.error('保存日程失败:', error)
    uni.showToast({ title: '保存失败: ' + error.message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '日程管理' })
  loadSchedules()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f6f8fb; padding-bottom: 20rpx; }
.header-card { margin: 16rpx; background: #fff; border-radius: 14rpx; padding: 18rpx; }
.title-row { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 32rpx; font-weight: 700; color: #1e293b; }
.small-btn { height: 60rpx; line-height: 60rpx; border-radius: 10rpx; font-size: 24rpx; background: #eef2ff; color: #4f46e5; }

.pill-group { display: flex; gap: 10rpx; margin-top: 14rpx; overflow-x: auto; }
.pill { flex-shrink: 0; padding: 8rpx 16rpx; border-radius: 999rpx; font-size: 22rpx; background: #e2e8f0; color: #475569; }
.pill.active { background: #667eea; color: #fff; }

.block { margin: 0 16rpx 12rpx; background: #fff; border-radius: 12rpx; padding: 14rpx; }
.block-title { font-size: 28rpx; font-weight: 700; color: #334155; margin-bottom: 10rpx; }
.item { display: flex; justify-content: space-between; gap: 12rpx; padding: 12rpx 0; border-bottom: 1px solid #eef2f7; }
.item:last-child { border-bottom: none; }
.line1 { font-size: 26rpx; color: #1e293b; font-weight: 600; }
.line2 { margin-top: 6rpx; font-size: 22rpx; color: #64748b; }

.status { align-self: flex-start; padding: 6rpx 12rpx; border-radius: 999rpx; font-size: 22rpx; }
.status.ok { background: #ecfdf3; color: #16a34a; }
.status.wait { background: #fff7ed; color: #ea580c; }
.status.draft { background: #f1f5f9; color: #64748b; }
.empty { color: #94a3b8; font-size: 24rpx; padding: 12rpx 0; }

.mask { position: fixed; inset: 0; background: rgba(0,0,0,.35); display: flex; align-items: flex-end; }
.modal { width: 100%; background: #fff; border-radius: 24rpx 24rpx 0 0; padding: 20rpx; }
.modal-title { font-size: 30rpx; font-weight: 700; color: #1e293b; margin-bottom: 12rpx; }
.input, .picker { width: 100%; height: 76rpx; border: 2rpx solid #e2e8f0; border-radius: 10rpx; padding: 0 16rpx; margin-bottom: 10rpx; display: flex; align-items: center; font-size: 26rpx; }
.picker-value { color: #475569; }
.row { display: flex; gap: 10rpx; }
.row .input, .row .btn { flex: 1; }
.btn { height: 74rpx; line-height: 74rpx; border-radius: 10rpx; color: #fff; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.btn.light { background: #e2e8f0; color: #334155; }
</style>
