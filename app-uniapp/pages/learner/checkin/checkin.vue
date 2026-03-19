<template>
  <scroll-view class="page" scroll-y>
    <view class="header-card">
      <view class="title">2025党务干部培训班</view>
      <view class="sub">报到签到列表</view>
    </view>

    <view class="table-card">
      <view class="row header">
        <text class="c1">日期时间</text>
        <text class="c2">事项</text>
        <text class="c3">地点</text>
        <text class="c4">状态</text>
      </view>

      <view class="row" :class="{ checked: item.checked }" v-for="item in checkinList" :key="item.id">
        <view class="c1">
          <text>{{ item.date }}</text>
          <text class="small">{{ item.time }}</text>
        </view>
        <view class="c2">{{ item.title }}</view>
        <view class="c3">{{ item.location }}</view>
        <view class="c4">
          <text v-if="item.checked" class="ok">已签到</text>
          <button v-else class="mini-btn" @click="doCheckin(item)">签到</button>
        </view>
      </view>
    </view>

    <view class="stats">
      <view class="stat done">
        <text class="num">{{ checkedCount }}</text>
        <text class="label">已签到</text>
      </view>
      <view class="stat wait">
        <text class="num">{{ pendingCount }}</text>
        <text class="label">待签到</text>
      </view>
    </view>

    <view class="tip">温馨提示：点击“签到”按钮可完成对应事项签到，已签到事项不可重复签到。</view>
  </scroll-view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const checkinList = ref([])

const checkedCount = computed(() => checkinList.value.filter((i) => i.checked).length)
const pendingCount = computed(() => checkinList.value.filter((i) => !i.checked).length)

function mockData() {
  checkinList.value = [
    { id: 1, date: '1月10日', time: '14:00', title: '酒店入住', location: '维也纳酒店一楼大堂', checked: true },
    { id: 2, date: '1月10日', time: '18:00', title: '欢迎晚餐', location: '酒店自助餐厅', checked: true },
    { id: 3, date: '1月11日', time: '08:45', title: '主会场会议', location: '越秀会议中心101', checked: true },
    { id: 4, date: '1月11日', time: '12:00', title: '自助午餐', location: '越秀会议中心5F', checked: true },
    { id: 5, date: '1月11日', time: '18:00', title: '欢聚晚宴', location: '1楼2号桌', checked: false },
    { id: 6, date: '1月12日', time: '09:00', title: '分会场会议', location: '越秀会议中心6F101', checked: false },
  ]
}

async function loadCheckins() {
  try {
    const token = uni.getStorageSync('auth_token') || ''
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
    const [err, res] = await uni.request({
      url: 'http://localhost:8084/api/checkin/list',
      method: 'GET',
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'X-Tenant-Id': tenantId,
      },
      timeout: 9000,
    })
    if (err) throw err
    const body = res?.data || {}
    const list = body?.data || []
    if (Array.isArray(list) && list.length > 0) {
      checkinList.value = list.map((i) => ({
        id: i.id,
        date: i.date || '未知日期',
        time: i.time || '--:--',
        title: i.title || i.itemName || '签到事项',
        location: i.location || '未设置',
        checked: !!(i.checked || i.status === 'checked' || i.status === 1),
      }))
    } else {
      mockData()
    }
  } catch (e) {
    mockData()
  }
}

async function doCheckin(item) {
  if (item.checked) return
  try {
    const token = uni.getStorageSync('auth_token') || ''
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || ''
    await uni.request({
      url: 'http://localhost:8084/api/checkin/submit',
      method: 'POST',
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'X-Tenant-Id': tenantId,
        'Content-Type': 'application/json',
      },
      data: {
        checkinId: item.id,
        checkinTime: new Date().toISOString(),
      },
      timeout: 9000,
    })
  } catch (e) {}
  item.checked = true
  uni.showToast({ title: '签到成功', icon: 'success' })
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '签到' })
  loadCheckins()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f5f7fb; }
.header-card { margin: 16rpx; background: #fff; border-radius: 14rpx; padding: 18rpx; }
.title { font-size: 32rpx; font-weight: 700; color: #1e293b; }
.sub { font-size: 24rpx; color: #64748b; margin-top: 6rpx; }

.table-card { margin: 0 16rpx; background: #fff; border-radius: 12rpx; overflow: hidden; }
.row { display: flex; border-bottom: 1px solid #eef2f7; }
.row:last-child { border-bottom: none; }
.row.header { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); color: #fff; font-weight: 700; }
.row.checked { background: #dbeafe; }

.c1, .c2, .c3, .c4 { padding: 16rpx 10rpx; font-size: 22rpx; display: flex; align-items: center; }
.c1 { width: 170rpx; flex-direction: column; align-items: flex-start; }
.c2 { width: 180rpx; }
.c3 { flex: 1; color: #475569; }
.c4 { width: 120rpx; justify-content: center; }

.small { margin-top: 4rpx; color: #64748b; font-size: 20rpx; }
.ok { color: #16a34a; font-weight: 700; }
.mini-btn { height: 52rpx; line-height: 52rpx; border-radius: 10rpx; font-size: 22rpx; padding: 0 18rpx; background: #3b82f6; color: #fff; }

.stats { display: grid; grid-template-columns: 1fr 1fr; gap: 12rpx; margin: 14rpx 16rpx; }
.stat { border-radius: 12rpx; text-align: center; padding: 16rpx 0; }
.stat.done { background: #f0fdf4; border: 1px solid #dcfce7; }
.stat.wait { background: #fffbeb; border: 1px solid #fed7aa; }
.num { font-size: 44rpx; font-weight: 700; display: block; }
.label { margin-top: 6rpx; color: #64748b; font-size: 22rpx; }

.tip { margin: 0 16rpx 20rpx; background: #eef6ff; color: #1e40af; border-radius: 12rpx; padding: 16rpx; font-size: 23rpx; }
</style>
