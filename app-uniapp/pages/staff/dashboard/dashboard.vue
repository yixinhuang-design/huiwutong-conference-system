<template>
  <view class="page">
    <view class="card">
      <text class="title">会议数据看板</text>
      <text class="sub">2025党务干部培训班 · 进行中</text>
    </view>

    <view class="card grid">
      <view class="metric blue"><text class="num">{{ metrics.total }}</text><text class="label">总报名人数</text></view>
      <view class="metric green"><text class="num">{{ metrics.approved }}</text><text class="label">已审核通过</text></view>
      <view class="metric yellow"><text class="num">{{ metrics.pending }}</text><text class="label">待审核</text></view>
      <view class="metric purple"><text class="num">{{ metrics.checkedIn }}</text><text class="label">已签到</text></view>
    </view>

    <view class="card">
      <text class="sec-title">快捷操作</text>
      <view class="quick" @click="go('/pages/staff/registration-manage/registration-manage')">报名审核</view>
      <view class="quick" @click="go('/pages/staff/seat-manage/seat-manage')">座位分配</view>
      <view class="quick" @click="go('/pages/staff/grouping-manage/grouping-manage')">分组管理</view>
    </view>

    <view class="card">
      <text class="sec-title">异常预警</text>
      <view class="warn-item" v-for="(w, i) in warnings" :key="i">
        <text class="warn-title">{{ w.title }}</text>
        <text class="warn-desc">{{ w.desc }}</text>
        <button size="mini" class="btn" @click="sendWarn(w)">{{ w.action }}</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'

const metrics = reactive({
  total: 160,
  approved: 142,
  pending: 12,
  checkedIn: 138
})

const warnings = ref([
  { title: '未签到学员 4 人', desc: '王梅、李强等已逾期，建议推送提醒', action: '推送' },
  { title: '未报名学员 2 人', desc: '请确认是否缺席或资格不符', action: '标记' }
])

const loadDashboard = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8084/api/dashboard/stats',
      method: 'GET'
    })
    const data = res?.data?.data
    if (data) Object.assign(metrics, data)
  } catch (e) {}
}

const go = (url) => uni.navigateTo({ url })

const sendWarn = (item) => {
  uni.showToast({ title: `${item.action}已发送`, icon: 'none' })
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '数据看板' })
  loadDashboard()
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.title { display: block; font-size: 34rpx; font-weight: 700; color: #111827; }
.sub { display: block; margin-top: 8rpx; font-size: 24rpx; color: #6b7280; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; }
.metric { padding: 16rpx; border-radius: 12rpx; text-align: center; }
.metric.blue { background: #e0f2fe; }
.metric.green { background: #dcfce7; }
.metric.yellow { background: #fef3c7; }
.metric.purple { background: #ede9fe; }
.num { display: block; font-size: 36rpx; font-weight: 700; color: #1f2937; }
.label { display: block; margin-top: 4rpx; font-size: 22rpx; color: #4b5563; }
.sec-title { display: block; font-size: 28rpx; font-weight: 600; margin-bottom: 10rpx; }
.quick { padding: 16rpx; border-radius: 10rpx; background: #f8fafc; margin-top: 10rpx; color: #2563eb; font-size: 26rpx; }
.warn-item { padding: 14rpx 0; border-bottom: 1rpx solid #f1f5f9; }
.warn-title { display: block; font-size: 26rpx; color: #111827; }
.warn-desc { display: block; margin-top: 6rpx; font-size: 23rpx; color: #6b7280; }
.btn { margin-top: 10rpx; border: 1rpx solid #d1d5db; background: #fff; color: #374151; }
</style>
