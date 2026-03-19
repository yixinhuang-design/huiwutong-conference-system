<template>
  <view class="seat-manage">
    <!-- 页面标题 -->
    <view class="header-card">
      <view class="header-back" @tap="goBack">
        <text class="back-btn">←</text>
        <view class="header-title">座位分配</view>
      </view>
      <view class="header-actions">
        <button class="action-btn" @tap="autoAllocate">🤖 智能分配</button>
        <button class="action-btn" @tap="exportSeats">📥 导出</button>
      </view>
    </view>

    <!-- 操作栏 -->
    <view class="toolbar">
      <view class="tool-item">
        <view class="label">选中:</view>
        <view class="value">{{ selectedCount }} 人</view>
      </view>
      <view class="tool-item">
        <view class="label">已分配:</view>
        <view class="value">{{ allocatedCount }} 人</view>
      </view>
      <view class="tool-item">
        <view class="label">未分配:</view>
        <view class="value">{{ unallocatedCount }} 人</view>
      </view>
    </view>

    <!-- 座位布局 -->
    <view class="seat-layout">
      <view class="layout-title">会议室座位布局</view>
      <view class="seats-grid">
        <view v-for="seat in seats" :key="seat.id" class="seat" :class="[seat.status, { selected: selectedSeats.has(seat.id) }]" @tap="selectSeat(seat.id)">
          <view class="seat-number">{{ seat.number }}</view>
          <view v-if="seat.person" class="seat-person">{{ seat.person }}</view>
        </view>
      </view>
      <view class="legend">
        <view class="legend-item available">
          <view class="color available"></view>
          <view>可用</view>
        </view>
        <view class="legend-item occupied">
          <view class="color occupied"></view>
          <view>已占用</view>
        </view>
        <view class="legend-item unavailable">
          <view class="color unavailable"></view>
          <view>禁用</view>
        </view>
      </view>
    </view>

    <!-- 待分配人员 -->
    <view class="unallocated-section">
      <view class="section-title">待分配人员 ({{ unallocatedPeople.length }})</view>
      <view class="people-list">
        <view v-for="person in unallocatedPeople" :key="person.id" class="person-card" :class="{ selected: selectedPeople.has(person.id) }" @tap="selectPerson(person.id)">
          <view class="person-info">
            <view class="person-name">{{ person.name }}</view>
            <view class="person-unit">{{ person.unit }}</view>
          </view>
          <view class="person-check">
            <checkbox :checked="selectedPeople.has(person.id)" />
          </view>
        </view>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view v-if="selectedSeats.size > 0 && selectedPeople.size > 0" class="batch-bar">
      <button class="batch-btn allocate" @tap="batchAllocate">
        ✓ 分配 {{ selectedPeople.size }} 人到 {{ selectedSeats.size }} 个座位
      </button>
      <button class="batch-btn clear" @tap="clearSelection">✕ 清除选择</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const selectedSeats = reactive(new Set())
const selectedPeople = reactive(new Set())

const seats = reactive([
  { id: 'A1', number: 'A1', status: 'available', person: '' },
  { id: 'A2', number: 'A2', status: 'available', person: '' },
  { id: 'A3', number: 'A3', status: 'available', person: '' },
  { id: 'A4', number: 'A4', status: 'available', person: '' },
  { id: 'B1', number: 'B1', status: 'available', person: '' },
  { id: 'B2', number: 'B2', status: 'occupied', person: '李华' },
  { id: 'B3', number: 'B3', status: 'available', person: '' },
  { id: 'B4', number: 'B4', status: 'available', person: '' },
  { id: 'C1', number: 'C1', status: 'available', person: '' },
  { id: 'C2', number: 'C2', status: 'available', person: '' },
  { id: 'C3', number: 'C3', status: 'unavailable', person: '✕' },
  { id: 'C4', number: 'C4', status: 'available', person: '' },
])

const people = reactive([
  { id: '1', name: '王文', unit: '市发改委', allocated: false },
  { id: '2', name: '张三', unit: '市财政局', allocated: false },
  { id: '3', name: '刘四', unit: '市教育局', allocated: false },
  { id: '4', name: '陈五', unit: '市卫健委', allocated: false },
  { id: '5', name: '杨六', unit: '市交通局', allocated: false },
  { id: '6', name: '赵七', unit: '市农业局', allocated: false },
])

const unallocatedPeople = computed(() => people.filter(p => !p.allocated))

const allocatedCount = computed(() => seats.filter(s => s.person && s.status !== 'unavailable').length)
const unallocatedCount = computed(() => unallocatedPeople.value.length)
const selectedCount = computed(() => selectedPeople.size)

const goBack = () => {
  uni.navigateBack()
}

const selectSeat = (seatId) => {
  const seat = seats.find(s => s.id === seatId)
  if (!seat || seat.status === 'occupied' || seat.status === 'unavailable') return
  
  if (selectedSeats.has(seatId)) {
    selectedSeats.delete(seatId)
  } else {
    selectedSeats.add(seatId)
  }
}

const selectPerson = (personId) => {
  if (selectedPeople.has(personId)) {
    selectedPeople.delete(personId)
  } else {
    selectedPeople.add(personId)
  }
}

const batchAllocate = () => {
  if (selectedSeats.size === 0 || selectedPeople.size === 0) {
    uni.showToast({ title: '请选择座位和人员', icon: 'none' })
    return
  }

  const seatArray = Array.from(selectedSeats)
  const peopleArray = Array.from(selectedPeople)

  peopleArray.forEach((personId, index) => {
    if (index < seatArray.length) {
      const seatId = seatArray[index]
      const seat = seats.find(s => s.id === seatId)
      const person = people.find(p => p.id === personId)
      
      if (seat && person) {
        seat.person = person.name
        seat.status = 'occupied'
        person.allocated = true
      }
    }
  })

  selectedSeats.clear()
  selectedPeople.clear()
  uni.showToast({ title: '分配成功', icon: 'success' })
}

const autoAllocate = () => {
  unallocatedPeople.value.forEach(person => {
    const availableSeat = seats.find(s => s.status === 'available' && !s.person)
    if (availableSeat) {
      availableSeat.person = person.name
      availableSeat.status = 'occupied'
      person.allocated = true
    }
  })
  uni.showToast({ title: '智能分配完成', icon: 'success' })
}

const clearSelection = () => {
  selectedSeats.clear()
  selectedPeople.clear()
}

const exportSeats = () => {
  const data = seats.map(s => ({ 座位: s.number, 人员: s.person || '未分配', 状态: s.status }))
  console.log('导出数据:', data)
  uni.showToast({ title: '导出成功', icon: 'success' })
}
</script>

<style scoped lang="scss">
.seat-manage {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 0 0 80px 0;
}

.header-card {
  background: white;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-back {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.back-btn {
  font-size: 20px;
  color: #667eea;
  cursor: pointer;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 8px;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;

  &:active {
    opacity: 0.7;
  }
}

.toolbar {
  background: white;
  padding: 12px 16px;
  margin: 12px 12px 0;
  border-radius: 12px;
  display: flex;
  gap: 16px;
}

.tool-item {
  flex: 1;
  text-align: center;
}

.label {
  font-size: 12px;
  color: #64748b;
}

.value {
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
}

.seat-layout {
  background: white;
  padding: 16px;
  margin: 12px;
  border-radius: 12px;
}

.layout-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
}

.seats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.seat {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 12px;
  transition: all 0.3s;
  border: 2px solid transparent;

  &.available {
    background: #dbeafe;
    color: #1e40af;
  }

  &.occupied {
    background: #d1d5db;
    color: #374151;
    cursor: not-allowed;
  }

  &.unavailable {
    background: #f3f4f6;
    color: #9ca3af;
    cursor: not-allowed;
  }

  &.selected {
    border-color: #667eea;
    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
    transform: scale(1.05);
  }
}

.seat-number {
  font-size: 13px;
  font-weight: 600;
}

.seat-person {
  font-size: 10px;
  margin-top: 2px;
}

.legend {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding: 12px;
  border-top: 1px solid #e2e8f0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #475569;
}

.color {
  width: 12px;
  height: 12px;
  border-radius: 2px;

  &.available {
    background: #dbeafe;
  }

  &.occupied {
    background: #d1d5db;
  }

  &.unavailable {
    background: #f3f4f6;
  }
}

.unallocated-section {
  background: white;
  padding: 16px;
  margin: 12px;
  border-radius: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
}

.people-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.person-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &.selected {
    background: #dbeafe;
    border-color: #3b82f6;
  }
}

.person-info {
  flex: 1;
}

.person-name {
  font-weight: 500;
  color: #1e293b;
  font-size: 14px;
}

.person-unit {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.person-check {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.batch-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 8px;
  z-index: 100;
}

.batch-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;

  &.allocate {
    background: #10b981;
    color: white;
  }

  &.clear {
    background: #f3f4f6;
    color: #6b7280;
  }
}
</style>
