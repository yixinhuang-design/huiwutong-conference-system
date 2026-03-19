<template>
  <view class="page">
    <view class="head">
      <text class="title">AI助手</text>
      <text class="sub">随时为您服务</text>
    </view>

    <scroll-view scroll-y class="msg-list">
      <view v-if="!messages.length" class="welcome">
        <text class="w-title">智能会议助手</text>
        <text class="w-sub">您可以试试这些问题：</text>
        <view class="chips">
          <text class="chip" v-for="q in hotQuestions" :key="q" @click="quickAsk(q)">{{ q }}</text>
        </view>
      </view>

      <view class="msg" :class="m.role" v-for="m in messages" :key="m.id">
        <text class="bubble">{{ m.content }}</text>
      </view>
    </scroll-view>

    <view class="input-row">
      <input v-model="inputText" class="input" placeholder="请输入问题..." confirm-type="send" @confirm="send" />
      <button class="send" @click="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const inputText = ref('')
const messages = ref([])
let idSeed = 0

const hotQuestions = [
  '今天的培训日程是什么？',
  '我的座位在哪里？',
  '如何完成签到？',
  '如何联系会务组？'
]

const mockReply = (q) => {
  if (q.includes('日程')) return '今日 08:45 主会场会议，12:00 午餐，14:00 分组讨论。'
  if (q.includes('座位')) return '您的座位在前区第3排12座，可在“座位图”页面查看。'
  if (q.includes('签到')) return '请在“报到签到”页面扫码或手动签到。'
  if (q.includes('联系')) return '会务组电话：020-12345678，现场服务台位于一层入口。'
  return '已收到您的问题，我正在整理会议信息。'
}

const pushMsg = (role, content) => {
  messages.value.push({ id: ++idSeed, role, content })
}

const ask = async (text) => {
  const q = text.trim()
  if (!q) return
  pushMsg('user', q)
  inputText.value = ''

  try {
    const res = await uni.request({
      url: 'http://localhost:8081/api/assistant/chat',
      method: 'POST',
      data: { message: q }
    })
    const ans = res?.data?.data?.reply
    pushMsg('ai', ans || mockReply(q))
  } catch (e) {
    pushMsg('ai', mockReply(q))
  }
}

const send = () => ask(inputText.value)
const quickAsk = (q) => ask(q)

onLoad(() => {
  uni.setNavigationBarTitle({ title: 'AI助手' })
})
</script>

<style scoped>
.page { min-height: 100vh; display: flex; flex-direction: column; background: #f8fafc; }
.head { padding: 24rpx; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; }
.title { display: block; font-size: 32rpx; font-weight: 700; }
.sub { display: block; margin-top: 6rpx; font-size: 22rpx; opacity: 0.9; }
.msg-list { flex: 1; padding: 20rpx; box-sizing: border-box; }
.welcome { background: #fff; border-radius: 14rpx; padding: 20rpx; }
.w-title { display: block; font-size: 28rpx; font-weight: 600; color: #1f2937; }
.w-sub { display: block; margin-top: 8rpx; color: #64748b; font-size: 23rpx; }
.chips { margin-top: 12rpx; display: flex; flex-wrap: wrap; gap: 10rpx; }
.chip { background: #f1f5f9; border: 1rpx solid #e2e8f0; border-radius: 999rpx; padding: 8rpx 14rpx; font-size: 22rpx; color: #475569; }
.msg { display: flex; margin-top: 12rpx; }
.msg.user { justify-content: flex-end; }
.bubble { max-width: 76%; padding: 12rpx 16rpx; border-radius: 12rpx; font-size: 24rpx; line-height: 1.5; }
.msg.user .bubble { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; }
.msg.ai .bubble { background: #fff; color: #1f2937; border: 1rpx solid #e2e8f0; }
.input-row { display: flex; gap: 10rpx; padding: 12rpx 16rpx; background: #fff; border-top: 1rpx solid #e2e8f0; }
.input { flex: 1; background: #f8fafc; border: 1rpx solid #e2e8f0; border-radius: 999rpx; padding: 0 18rpx; height: 72rpx; font-size: 24rpx; }
.send { width: 120rpx; background: #667eea; color: #fff; border-radius: 999rpx; font-size: 24rpx; }
</style>
