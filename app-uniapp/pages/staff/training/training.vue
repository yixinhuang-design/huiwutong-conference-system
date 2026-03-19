<template>
  <scroll-view class="page" scroll-y>
    <view class="header-card">
      <text class="title">培训管理</text>
    </view>

    <view class="pill-group">
      <view class="pill" :class="{ active: filterStatus === 'all' }" @click="filterStatus = 'all'">全部</view>
      <view class="pill" :class="{ active: filterStatus === '3' }" @click="filterStatus = '3'">进行中</view>
      <view class="pill" :class="{ active: filterStatus === '4' }" @click="filterStatus = '4'">已结束</view>
      <view class="pill" :class="{ active: filterStatus === '1' }" @click="filterStatus = '1'">报名中</view>
    </view>

    <view class="loading" v-if="loading">加载中...</view>

    <view v-else>
      <view class="training-card" v-for="training in filteredTrainings" :key="training.id" @click="goDetail(training)">
        <view class="top-row">
          <view>
            <view class="name">{{ training.meetingName }}</view>
            <view class="meta">{{ formatDate(training.startTime) }} 至 {{ formatDate(training.endTime) }}</view>
            <view class="meta">📍 {{ training.venueName || '未设置' }}</view>
          </view>
          <view class="status" :class="statusClass(training.status)">{{ statusText(training.status) }}</view>
        </view>
        <view class="bottom-row">
          <text>👥 {{ training.currentParticipants || 0 }} 人报名</text>
        </view>
      </view>

      <view class="empty" v-if="filteredTrainings.length === 0">暂无培训，点击下方按钮创建新培训</view>
    </view>

    <view class="footer">
      <button class="create-btn" @click="goCreate">+ 新建培训</button>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const trainings = ref([])
const filterStatus = ref('all')
const loading = ref(false)

const filteredTrainings = computed(() => {
  if (filterStatus.value === 'all') return trainings.value
  return trainings.value.filter((t) => String(t.status) === filterStatus.value)
})

function statusText(status) {
  const map = { 0: '草稿', 1: '报名中', 2: '报名截止', 3: '进行中', 4: '已结束', 5: '已取消' }
  return map[status] || '未知'
}

function statusClass(status) {
  if (status === 3 || status === 1) return 'active'
  if (status === 4) return 'done'
  if (status === 5) return 'cancel'
  return 'draft'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function loadSampleData() {
  const now = Date.now()
  trainings.value = [
    {
      id: 'demo_1',
      meetingName: '2025年党务干部培训班',
      startTime: new Date(now).toISOString(),
      endTime: new Date(now + 5 * 24 * 60 * 60 * 1000).toISOString(),
      venueName: '市委党校报告厅',
      status: 3,
      currentParticipants: 156,
    },
    {
      id: 'demo_2',
      meetingName: '青年干部能力提升班',
      startTime: new Date(now - 10 * 24 * 60 * 60 * 1000).toISOString(),
      endTime: new Date(now - 5 * 24 * 60 * 60 * 1000).toISOString(),
      venueName: '市委党校教学楼',
      status: 4,
      currentParticipants: 89,
    },
    {
      id: 'demo_3',
      meetingName: '基层党建工作者培训班',
      startTime: new Date(now + 16 * 24 * 60 * 60 * 1000).toISOString(),
      endTime: new Date(now + 20 * 24 * 60 * 60 * 1000).toISOString(),
      venueName: '市委党校综合楼',
      status: 1,
      currentParticipants: 78,
    },
  ]
}

async function loadTrainings() {
  loading.value = true
  try {
    const token = uni.getStorageSync('auth_token') || uni.getStorageSync('token') || ''
    const tenantId = uni.getStorageSync('tenant_id') || uni.getStorageSync('current_tenant_id') || '2027317834622709762'
    const [err, res] = await uni.request({
      url: 'http://localhost:8084/api/meeting/list?pageNum=1&pageSize=100',
      method: 'GET',
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'X-Tenant-Id': tenantId,
      },
      timeout: 10000,
    })
    if (err) throw err
    const body = res?.data || {}
    if ((body.code === 0 || body.code === 200) && body.data?.records) {
      trainings.value = body.data.records
    } else {
      loadSampleData()
    }
  } catch (e) {
    loadSampleData()
  } finally {
    loading.value = false
  }
}

function goCreate() {
  uni.navigateTo({ url: '/pages/staff/training-create/training-create' })
}

function goDetail(training) {
  uni.navigateTo({
    url: `/pages/staff/training-detail/training-detail?id=${training.id}`,
  })
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '培训管理' })
  loadTrainings()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f6f8fb; padding-bottom: 120rpx; }

.header-card {
  margin: 16rpx;
  background: #fff;
  border-radius: 14rpx;
  padding: 22rpx;
}
.title { font-size: 34rpx; font-weight: 700; color: #1e293b; }

.pill-group {
  display: flex;
  gap: 10rpx;
  padding: 0 16rpx 12rpx;
  overflow-x: auto;
}
.pill {
  flex-shrink: 0;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #e2e8f0;
  color: #475569;
  font-size: 24rpx;
}
.pill.active { background: #667eea; color: #fff; }

.loading,
.empty {
  margin: 20rpx 16rpx;
  background: #fff;
  border-radius: 14rpx;
  text-align: center;
  color: #94a3b8;
  padding: 40rpx 20rpx;
  font-size: 26rpx;
}

.training-card {
  margin: 0 16rpx 12rpx;
  background: #fff;
  border-radius: 14rpx;
  padding: 20rpx;
}
.top-row { display: flex; justify-content: space-between; gap: 16rpx; }
.name { font-size: 30rpx; font-weight: 700; color: #1e293b; margin-bottom: 6rpx; }
.meta { font-size: 24rpx; color: #64748b; margin-top: 4rpx; }
.bottom-row { margin-top: 14rpx; font-size: 24rpx; color: #475569; }

.status {
  align-self: flex-start;
  font-size: 22rpx;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
}
.status.active { background: #ecfdf3; color: #16a34a; }
.status.done { background: #eff6ff; color: #2563eb; }
.status.cancel { background: #fef2f2; color: #dc2626; }
.status.draft { background: #f1f5f9; color: #64748b; }

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-top: 1px solid #e2e8f0;
  padding: 14rpx 16rpx 26rpx;
}
.create-btn {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 12rpx;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 30rpx;
  font-weight: 600;
}
</style>
