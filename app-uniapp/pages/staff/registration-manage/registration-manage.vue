<template>
  <view class="registration-manage">
    <!-- 页面标题 -->
    <view class="header-card">
      <view class="header-title">报名审核</view>
      <view class="header-subtitle">待审核({{ pendingCount }}) | 已通过({{ approvedCount }}) | 需退回({{ rejectedCount }})</view>
    </view>

    <!-- 搜索筛选 -->
    <view class="search-card">
      <view class="search-group">
        <input v-model="searchKeyword" type="text" placeholder="按姓名、手机号、单位搜索" class="search-input" @input="handleSearch" />
        <view class="filter-btn" @tap="showFilterPanel">🔍</view>
      </view>
      <view class="sort-btns">
        <button class="sort-btn" @tap="sortBy('time')" :class="{ active: sortType === 'time' }">按时间</button>
        <button class="sort-btn" @tap="sortBy('name')" :class="{ active: sortType === 'name' }">按姓名</button>
        <button class="sort-btn" @tap="sortBy('unit')" :class="{ active: sortType === 'unit' }">按单位</button>
      </view>
    </view>

    <!-- 状态标签 -->
    <view class="tab-card">
      <view class="tab-item" :class="{ active: filterStatus === 'pending' }" @tap="filterStatus = 'pending'">待审核</view>
      <view class="tab-item" :class="{ active: filterStatus === 'approved' }" @tap="filterStatus = 'approved'">已通过</view>
      <view class="tab-item" :class="{ active: filterStatus === 'rejected' }" @tap="filterStatus = 'rejected'">需退回</view>
      <view class="tab-item" :class="{ active: filterStatus === 'all' }" @tap="filterStatus = 'all'">全部</view>
    </view>

    <!-- 批量操作栏 -->
    <view v-if="selectedItems.size > 0" class="batch-bar">
      <view class="batch-info">已选 {{ selectedItems.size }} 项</view>
      <view class="batch-btns">
        <button class="batch-btn approve-btn" @tap="batchApprove">✓ 批量通过</button>
        <button class="batch-btn reject-btn" @tap="batchReject">↩ 批量退回</button>
      </view>
    </view>

    <!-- 报名列表 -->
    <view class="list-container">
      <view v-if="filteredList.length === 0" class="empty-state">
        <view class="empty-icon">📋</view>
        <view class="empty-text">暂无数据</view>
      </view>

      <view v-for="item in filteredList" :key="item.id" class="registration-card">
        <view class="card-header">
          <view class="info-row">
            <view class="checkbox">
              <checkbox :checked="selectedItems.has(item.id)" @change="toggleSelect(item.id)" />
            </view>
            <view class="basic-info">
              <view class="name">{{ item.name }}</view>
              <view class="unit">{{ item.unit }}</view>
            </view>
          </view>
          <view class="status-badge" :class="item.status">{{ getStatusText(item.status) }}</view>
        </view>

        <view class="card-body">
          <view class="info-item">
            <view class="info-label">手机号</view>
            <view class="info-value">{{ item.phone }}</view>
          </view>
          <view class="info-item">
            <view class="info-label">证件号</view>
            <view class="info-value">{{ item.idCard }}</view>
          </view>
          <view class="info-item">
            <view class="info-label">报名时间</view>
            <view class="info-value">{{ formatDate(item.registrationTime) }}</view>
          </view>
        </view>

        <view v-if="item.status === 'pending'" class="card-actions">
          <button class="action-btn approve" @tap="approve(item.id)">通过</button>
          <button class="action-btn reject" @tap="reject(item.id)">退回</button>
          <button class="action-btn detail" @tap="viewDetail(item.id)">详情</button>
        </view>
      </view>
    </view>

    <!-- 加载更多 -->
    <view v-if="isLoading" class="loading">加载中...</view>
    <view v-if="hasMore" class="load-more-btn" @tap="loadMore">加载更多</view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const searchKeyword = ref('')
const sortType = ref('time')
const filterStatus = ref('pending')
const isLoading = ref(false)
const hasMore = ref(true)
const selectedItems = reactive(new Set())

const registrations = reactive([
  { id: '1', name: '李华', phone: '13800138000', unit: '市委组织部', idCard: '110101199001011234', registrationTime: '2024-01-10 10:30', status: 'pending' },
  { id: '2', name: '王文', phone: '13800138001', unit: '市发改委', idCard: '110101199002011234', registrationTime: '2024-01-09 14:20', status: 'pending' },
  { id: '3', name: '张三', phone: '13800138002', unit: '市财政局', idCard: '110101199003011234', registrationTime: '2024-01-08 09:15', status: 'approved' },
  { id: '4', name: '刘四', phone: '13800138003', unit: '市教育局', idCard: '110101199004011234', registrationTime: '2024-01-07 16:45', status: 'approved' },
  { id: '5', name: '陈五', phone: '13800138004', unit: '市卫健委', idCard: '110101199005011234', registrationTime: '2024-01-06 11:00', status: 'rejected' }
])

const filteredList = computed(() => {
  let result = registrations.filter(item => {
    const keyword = searchKeyword.value.toLowerCase()
    const match = !keyword || item.name.includes(keyword) || item.phone.includes(keyword) || item.unit.includes(keyword)
    const statusMatch = filterStatus.value === 'all' || item.status === filterStatus.value
    return match && statusMatch
  })

  if (sortType.value === 'name') result.sort((a, b) => a.name.localeCompare(b.name))
  else if (sortType.value === 'unit') result.sort((a, b) => a.unit.localeCompare(b.unit))
  else result.sort((a, b) => new Date(b.registrationTime) - new Date(a.registrationTime))

  return result
})

const pendingCount = computed(() => registrations.filter(r => r.status === 'pending').length)
const approvedCount = computed(() => registrations.filter(r => r.status === 'approved').length)
const rejectedCount = computed(() => registrations.filter(r => r.status === 'rejected').length)

const toggleSelect = (id) => {
  if (selectedItems.has(id)) selectedItems.delete(id)
  else selectedItems.add(id)
}

const sortBy = (type) => {
  sortType.value = type
}

const handleSearch = () => {
  // 搜索已在computed中实现
}

const approve = (id) => {
  const item = registrations.find(r => r.id === id)
  if (item) {
    item.status = 'approved'
    uni.showToast({ title: '已通过', icon: 'success' })
  }
}

const reject = (id) => {
  const item = registrations.find(r => r.id === id)
  if (item) {
    item.status = 'rejected'
    uni.showToast({ title: '已退回', icon: 'success' })
  }
}

const batchApprove = () => {
  registrations.forEach(r => {
    if (selectedItems.has(r.id)) r.status = 'approved'
  })
  selectedItems.clear()
  uni.showToast({ title: '批量通过成功', icon: 'success' })
}

const batchReject = () => {
  registrations.forEach(r => {
    if (selectedItems.has(r.id)) r.status = 'rejected'
  })
  selectedItems.clear()
  uni.showToast({ title: '批量退回成功', icon: 'success' })
}

const viewDetail = (id) => {
  uni.navigateTo({ url: `/pages/staff/registration-detail/registration-detail?id=${id}` })
}

const loadMore = () => {
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
    hasMore.value = false
  }, 1000)
}

const formatDate = (dateStr) => {
  return dateStr.split(' ')[0]
}

const getStatusText = (status) => {
  const map = { pending: '待审核', approved: '已通过', rejected: '需退回' }
  return map[status] || '未知'
}

const showFilterPanel = () => {
  uni.showToast({ title: '高级筛选功能开发中', icon: 'none' })
}
</script>

<style scoped lang="scss">
.registration-manage {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 0;
}

.header-card {
  background: white;
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.header-subtitle {
  font-size: 12px;
  color: #64748b;
}

.search-card {
  background: white;
  padding: 12px 16px;
  margin: 12px 12px 0;
  border-radius: 12px;
}

.search-group {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.search-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
}

.filter-btn {
  padding: 10px 12px;
  background: #667eea;
  color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.sort-btns {
  display: flex;
  gap: 6px;
}

.sort-btn {
  flex: 1;
  padding: 8px;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.3s;

  &.active {
    background: #667eea;
    color: white;
    border-color: #667eea;
  }
}

.tab-card {
  display: flex;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  margin: 0;
  gap: 0;
}

.tab-item {
  flex: 1;
  padding: 12px;
  text-align: center;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;

  &.active {
    color: #667eea;
    border-bottom-color: #667eea;
    font-weight: 600;
  }
}

.batch-bar {
  background: white;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0;
}

.batch-info {
  font-size: 14px;
  color: #475569;
}

.batch-btns {
  display: flex;
  gap: 8px;
}

.batch-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;

  &.approve-btn {
    background: #10b981;
    color: white;
  }

  &.reject-btn {
    background: #ef4444;
    color: white;
  }
}

.list-container {
  padding: 12px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  color: #94a3b8;
  font-size: 14px;
}

.registration-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.checkbox {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.basic-info {
  flex: 1;
}

.name {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.unit {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;

  &.pending {
    background: #fef3c7;
    color: #92400e;
  }

  &.approved {
    background: #d1fae5;
    color: #065f46;
  }

  &.rejected {
    background: #fee2e2;
    color: #991b1b;
  }
}

.card-body {
  padding: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;

  &:last-child {
    margin-bottom: 0;
  }
}

.info-label {
  color: #64748b;
}

.info-value {
  color: #1e293b;
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #f1f5f9;
}

.action-btn {
  flex: 1;
  padding: 8px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: white;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;

  &.approve {
    background: #d1fae5;
    border-color: #10b981;
    color: #065f46;
  }

  &.reject {
    background: #fee2e2;
    border-color: #ef4444;
    color: #991b1b;
  }

  &.detail {
    background: #dbeafe;
    border-color: #3b82f6;
    color: #1e40af;
  }
}

.loading {
  text-align: center;
  padding: 20px;
  color: #64748b;
}

.load-more-btn {
  text-align: center;
  padding: 16px;
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}
</style>
