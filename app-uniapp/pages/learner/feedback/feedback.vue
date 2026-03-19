<template>
  <scroll-view class="page" scroll-y>
    <view class="card">
      <text class="title">培训反馈问卷</text>
      <text class="sub">请完成必填项，提交后将用于教学改进</text>
    </view>

    <view class="card" v-for="q in questions" :key="q.key">
      <text class="q-title">* {{ q.title }}</text>
      <radio-group @change="onRadioChange(q.key, $event)">
        <label class="option" v-for="op in q.options" :key="op">
          <radio :value="op" :checked="form[q.key] === op" />
          <text>{{ op }}</text>
        </label>
      </radio-group>
    </view>

    <view class="card">
      <text class="q-title">改进建议（选填）</text>
      <textarea v-model="form.q8" placeholder="请输入建议" class="ta" />
      <text class="q-title">其他意见（选填）</text>
      <textarea v-model="form.q9" placeholder="请输入其他意见" class="ta" />
    </view>

    <view class="actions">
      <button class="btn" @click="saveDraft">保存草稿</button>
      <button class="btn primary" @click="submit">提交问卷</button>
    </view>
  </scroll-view>
</template>

<script setup>
import { reactive } from 'vue'

const questions = [
  { key: 'q1', title: '您对本次培训内容的满意度如何？', options: ['非常满意', '满意', '一般', '不满意'] },
  { key: 'q2', title: '讲师的专业程度和授课能力', options: ['非常满意', '满意', '一般', '需要改进'] },
  { key: 'q3', title: '培训材料和参考资源的质量', options: ['非常充分', '充分', '一般', '不充分'] },
  { key: 'q4', title: '培训时间安排和组织管理是否合理', options: ['非常合理', '合理', '一般', '需改进'] },
  { key: 'q5', title: '本次培训学到的知识在工作中的实用性', options: ['非常实用', '比较实用', '一般', '不实用'] },
  { key: 'q6', title: '会场设施和后勤服务质量', options: ['非常满意', '满意', '一般', '不满意'] },
  { key: 'q7', title: '您会推荐其他人参加这类培训吗？', options: ['会，非常推荐', '会，比较推荐', '可能会', '不会'] }
]

const form = reactive({
  q1: '', q2: '', q3: '', q4: '', q5: '', q6: '', q7: '', q8: '', q9: ''
})

const onRadioChange = (key, e) => {
  form[key] = e.detail.value
}

const saveDraft = () => {
  uni.setStorageSync('feedback_draft', JSON.stringify(form))
  uni.showToast({ title: '草稿已保存', icon: 'success' })
}

const submit = async () => {
  const missed = questions.filter((q) => !form[q.key])
  if (missed.length) {
    uni.showToast({ title: '请完成所有必填项', icon: 'none' })
    return
  }

  try {
    await uni.request({
      url: 'http://localhost:8082/api/feedback/submit',
      method: 'POST',
      data: { ...form }
    })
  } catch (e) {}

  uni.removeStorageSync('feedback_draft')
  uni.showToast({ title: '提交成功', icon: 'success' })
  setTimeout(() => {
    uni.navigateBack()
  }, 500)
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '问卷填写' })
  const draft = uni.getStorageSync('feedback_draft')
  if (draft) {
    const parsed = JSON.parse(draft)
    Object.assign(form, parsed)
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.title { display: block; font-size: 34rpx; font-weight: 700; }
.sub { display: block; margin-top: 8rpx; color: #6b7280; font-size: 24rpx; }
.q-title { display: block; font-size: 27rpx; font-weight: 600; margin-bottom: 10rpx; }
.option { display: flex; align-items: center; gap: 12rpx; padding: 10rpx 0; font-size: 25rpx; }
.ta { width: 100%; min-height: 130rpx; border: 1rpx solid #d1d5db; border-radius: 10rpx; padding: 12rpx; margin-bottom: 16rpx; box-sizing: border-box; }
.actions { display: flex; gap: 16rpx; margin-bottom: 40rpx; }
.btn { flex: 1; background: #fff; border: 1rpx solid #d1d5db; color: #374151; font-size: 26rpx; }
.btn.primary { background: #2563eb; color: #fff; border-color: #2563eb; }
</style>
