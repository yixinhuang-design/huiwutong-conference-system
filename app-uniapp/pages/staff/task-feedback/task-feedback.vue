<template>
  <view class="page">
    <view class="card">
      <text class="title">任务完成反馈</text>
      <text class="sub">提交后进入审核流程</text>
    </view>

    <view class="card">
      <text class="label">实际完成时间</text>
      <picker mode="date" @change="onDateChange">
        <view class="input-like">{{ form.date || '请选择日期' }}</view>
      </picker>

      <text class="label">完成情况</text>
      <picker :range="statusOptions" :value="statusIndex" @change="onStatusChange">
        <view class="input-like">{{ statusOptions[statusIndex] }}</view>
      </picker>

      <text class="label">补充说明</text>
      <textarea v-model="form.remark" class="ta" placeholder="可填写执行情况、问题与建议" />
    </view>

    <view class="card">
      <text class="label">现场凭证</text>
      <button class="btn" @click="pickImages">上传图片</button>
      <view v-if="form.images.length" class="img-list">
        <text v-for="(img, idx) in form.images" :key="idx" class="img-name">{{ fileName(img) }}</text>
      </view>
    </view>

    <view class="actions">
      <button class="btn" @click="saveTemp">临时保存</button>
      <button class="btn primary" @click="submit">提交完成</button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'

const statusOptions = ['全部完成（无异常）', '部分完成（待补充）', '无法完成（需重新安排）']
const statusIndex = ref(0)

const form = reactive({
  date: '',
  status: statusOptions[0],
  remark: '',
  images: []
})

const onDateChange = (e) => {
  form.date = e.detail.value
}

const onStatusChange = (e) => {
  statusIndex.value = Number(e.detail.value)
  form.status = statusOptions[statusIndex.value]
}

const fileName = (path) => path.split('/').pop()

const pickImages = async () => {
  try {
    const res = await uni.chooseImage({ count: 3, sizeType: ['compressed'] })
    const paths = res?.tempFilePaths || []
    form.images = [...form.images, ...paths].slice(0, 6)
  } catch (e) {}
}

const saveTemp = () => {
  uni.setStorageSync('task_feedback_draft', JSON.stringify(form))
  uni.showToast({ title: '已临时保存', icon: 'success' })
}

const submit = async () => {
  if (!form.date) {
    uni.showToast({ title: '请选择完成日期', icon: 'none' })
    return
  }
  try {
    await uni.request({
      url: 'http://localhost:8084/api/task/feedback/submit',
      method: 'POST',
      data: { ...form }
    })
  } catch (e) {}
  uni.removeStorageSync('task_feedback_draft')
  uni.showToast({ title: '提交成功', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 500)
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '任务反馈' })
  const draft = uni.getStorageSync('task_feedback_draft')
  if (draft) {
    const parsed = JSON.parse(draft)
    Object.assign(form, parsed)
    statusIndex.value = Math.max(0, statusOptions.indexOf(form.status))
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.title { display: block; font-size: 34rpx; font-weight: 700; }
.sub { display: block; margin-top: 8rpx; font-size: 24rpx; color: #6b7280; }
.label { display: block; font-size: 26rpx; margin-bottom: 10rpx; margin-top: 8rpx; color: #374151; }
.input-like { background: #f9fafb; border: 1rpx solid #d1d5db; border-radius: 10rpx; padding: 12rpx; margin-bottom: 10rpx; color: #111827; }
.ta { width: 100%; min-height: 140rpx; border: 1rpx solid #d1d5db; border-radius: 10rpx; padding: 12rpx; box-sizing: border-box; }
.actions { display: flex; gap: 16rpx; }
.btn { flex: 1; border: 1rpx solid #d1d5db; background: #fff; color: #374151; font-size: 26rpx; }
.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; }
.img-list { margin-top: 12rpx; }
.img-name { display: block; font-size: 23rpx; color: #6b7280; margin-top: 6rpx; }
</style>
