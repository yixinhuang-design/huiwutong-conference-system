<template>
  <view class="page">
    <view class="card">
      <view class="row">
        <text class="title">{{ detail.title }}</text>
        <text class="status">{{ detail.status }}</text>
      </view>
      <text class="desc">{{ detail.desc }}</text>
    </view>

    <view class="card" v-for="(v, k) in detail.meta" :key="k">
      <text class="meta-key">{{ k }}</text>
      <text class="meta-val">{{ v }}</text>
    </view>

    <button class="action" @click="handleAction">{{ detail.action }}</button>
  </view>
</template>

<script setup>
import { reactive } from 'vue'

const detail = reactive({
  type: 'feedback',
  id: '',
  title: '任务详情',
  status: '待完成',
  desc: '',
  action: '立即处理',
  meta: {}
})

const applyByType = (type) => {
  if (type === 'feedback') {
    detail.title = '培训满意度调查'
    detail.status = '待完成'
    detail.desc = '请根据真实体验完成问卷，帮助优化课程质量。'
    detail.action = '开始填写'
    detail.meta = {
      截止时间: '明天 18:00',
      题目数量: '共 9 题',
      预计耗时: '约 5 分钟'
    }
    return
  }

  if (type === 'evaluation') {
    detail.title = '讲师评价'
    detail.status = '待完成'
    detail.desc = '请对讲师授课进行客观评分。'
    detail.action = '开始评价'
    detail.meta = {
      评价对象: '张教授 - 党建工作实务',
      截止时间: '今天 22:00',
      评价维度: '内容质量、授课效果、互动性、启发性、综合评分'
    }
    return
  }

  detail.title = '现场合影'
  detail.status = '进行中'
  detail.desc = '请在指定地点完成小组合影并上传。'
  detail.action = '上传凭证'
  detail.meta = {
    任务时间: '今天 14:00-16:00',
    集合地点: '会议中心大厅',
    参与人员: '第一组成员'
  }
}

const handleAction = () => {
  if (detail.type === 'feedback') {
    uni.navigateTo({ url: '/pages/learner/feedback/feedback' })
    return
  }
  if (detail.type === 'evaluation') {
    uni.navigateTo({ url: '/pages/learner/evaluation/evaluation' })
    return
  }
  uni.showToast({ title: '请上传现场照片', icon: 'none' })
}

onLoad((options) => {
  uni.setNavigationBarTitle({ title: '任务详情' })
  detail.type = options?.type || 'feedback'
  detail.id = options?.id || ''
  applyByType(detail.type)
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.row { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; }
.title { font-size: 33rpx; font-weight: 700; color: #111827; }
.status { font-size: 22rpx; color: #f59e0b; }
.desc { display: block; margin-top: 10rpx; font-size: 24rpx; color: #4b5563; }
.meta-key { display: block; font-size: 24rpx; color: #6b7280; }
.meta-val { display: block; font-size: 27rpx; color: #111827; margin-top: 6rpx; }
.action { background: #2563eb; color: #fff; border-radius: 12rpx; font-size: 28rpx; }
</style>
