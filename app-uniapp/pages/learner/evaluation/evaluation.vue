<template>
  <view class="page">
    <view class="card">
      <text class="title">讲师评价</text>
      <text class="sub">请对课程维度进行打分（1~5分）</text>
    </view>

    <view class="card" v-for="item in fields" :key="item.key">
      <view class="line">
        <text class="name">{{ item.label }}</text>
        <text class="score">{{ ratings[item.key] || 0 }} 分</text>
      </view>
      <view class="stars">
        <text
          v-for="n in 5"
          :key="n"
          class="star"
          :class="{ active: ratings[item.key] >= n }"
          @click="setRate(item.key, n)"
        >★</text>
      </view>
    </view>

    <view class="card">
      <text class="name">文字评价（选填）</text>
      <textarea v-model="comment" class="ta" placeholder="请输入评价内容" />
    </view>

    <button class="submit" @click="submit">提交评价</button>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'

const fields = [
  { key: 'content', label: '内容质量' },
  { key: 'delivery', label: '授课效果' },
  { key: 'interaction', label: '互动性' },
  { key: 'inspiration', label: '启发性' },
  { key: 'overall', label: '综合评分' }
]

const ratings = reactive({
  content: 0,
  delivery: 0,
  interaction: 0,
  inspiration: 0,
  overall: 0
})

const comment = ref('')

const setRate = (field, value) => {
  ratings[field] = value
}

const submit = async () => {
  const unfilled = fields.find((f) => !ratings[f.key])
  if (unfilled) {
    uni.showToast({ title: `请完成${unfilled.label}评分`, icon: 'none' })
    return
  }

  try {
    await uni.request({
      url: 'http://localhost:8082/api/evaluation/submit',
      method: 'POST',
      data: {
        ratings: { ...ratings },
        comment: comment.value
      }
    })
  } catch (e) {}

  uni.showToast({ title: '评价提交成功', icon: 'success' })
  setTimeout(() => uni.navigateBack(), 500)
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '评价打分' })
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f7fb; padding: 24rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 20rpx; margin-bottom: 20rpx; }
.title { display: block; font-size: 34rpx; font-weight: 700; }
.sub { display: block; color: #6b7280; font-size: 24rpx; margin-top: 8rpx; }
.line { display: flex; justify-content: space-between; align-items: center; }
.name { font-size: 28rpx; font-weight: 600; color: #111827; }
.score { font-size: 24rpx; color: #6b7280; }
.stars { margin-top: 12rpx; }
.star { font-size: 46rpx; color: #d1d5db; margin-right: 10rpx; }
.star.active { color: #f59e0b; }
.ta { width: 100%; min-height: 150rpx; margin-top: 12rpx; border: 1rpx solid #d1d5db; border-radius: 10rpx; padding: 12rpx; box-sizing: border-box; }
.submit { background: #2563eb; color: #fff; font-size: 28rpx; border-radius: 12rpx; }
</style>
