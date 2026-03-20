<template>
  <view class="seat-manage-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">座位管理</text>
        <text class="header-action" @click="showMenu">⋯</text>
      </view>
    </view>

    <!-- 工具栏 -->
    <view class="toolbar card">
      <view class="tool-item" @click="toggleEditMode">
        <text class="tool-icon">{{ editMode ? '✓' : '<text class="fa fa-pen"></text>' }}</text>
        <text class="tool-text">{{ editMode ? '完成' : '编辑' }}</text>
      </view>
      <view class="tool-item" @click="autoAssign">
        <text class="tool-icon">🔄</text>
        <text class="tool-text">自动分配</text>
      </view>
      <view class="tool-item" @click="importSeats">
        <text class="tool-icon">📥</text>
        <text class="tool-text">导入</text>
      </view>
      <view class="tool-item" @click="exportSeats">
        <text class="tool-icon">📤</text>
        <text class="tool-text">导出</text>
      </view>
    </view>

    <!-- 座位图 -->
    <view class="seat-map-section card">
      <view class="section-title">
        <text>座位图</text>
        <text class="section-subtitle">{{ selectedArea }}</text>
      </view>

      <!-- 区域选择 -->
      <scroll-view class="area-scroll" scroll-x>
        <view class="area-list">
          <view
            v-for="(area, index) in areas"
            :key="index"
            class="area-item"
            :class="{ active: selectedAreaIndex === index }"
            @click="selectArea(index)"
          >
            {{ area }}
          </view>
        </view>
      </scroll-view>

      <!-- 座位网格 -->
      <scroll-view class="seat-scroll" scroll-x scroll-y>
        <view class="seat-grid">
          <view
            v-for="(seat, index) in seatList"
            :key="index"
            class="seat-item"
            :class="getSeatClass(seat)"
            @click="handleSeat(seat)"
          >
            <text class="seat-number">{{ seat.row }}-{{ seat.col }}</text>
            <text v-if="seat.user" class="seat-user">{{ seat.user }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 图例 -->
      <view class="legend">
        <view class="legend-item">
          <view class="legend-color available"></view>
          <text class="legend-text">可选</text>
        </view>
        <view class="legend-item">
          <view class="legend-color occupied"></view>
          <text class="legend-text">已占</text>
        </view>
        <view class="legend-item">
          <view class="legend-color selected"></view>
          <text class="legend-text">已选</text>
        </view>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-section card">
      <view class="section-title">座位统计</view>
      <view class="stats-grid grid-2">
        <view class="stat-item">
          <text class="stat-value">{{ seatStats.total }}</text>
          <text class="stat-label">总座位</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ seatStats.occupied }}</text>
          <text class="stat-label">已分配</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ seatStats.available }}</text>
          <text class="stat-label">可用</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ seatStats.selected }}</text>
          <text class="stat-label">已选</text>
        </view>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view v-if="editMode && selectedSeats.length > 0" class="batch-bar">
      <view class="batch-info">
        已选 {{ selectedSeats.length }} 个座位
      </view>
      <view class="batch-actions">
        <button class="batch-btn btn btn-primary btn-sm" @click="batchAssign">
          分配
        </button>
        <button class="batch-btn btn btn-outline btn-sm" @click="batchClear">
          清除
        </button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      meetingId: '',
      editMode: false,
      selectedAreaIndex: 0,
      selectedArea: 'A区',
      selectedSeats: [],
      areas: ['A区', 'B区', 'C区', 'D区'],
      seatList: [],
      seatStats: {
        total: 80,
        occupied: 45,
        available: 35,
        selected: 0
      }
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
      this.loadSeats()
    }
  },

  methods: {
    /**
     * 加载座位
     */
    async loadSeats() {
      try {
        // TODO: 调用API获取座位
        // const res = await this.$api.seat.getList(this.meetingId)

        // 模拟数据 - 生成8x10的座位网格
        const seats = []
        for (let r = 1; r <= 8; r++) {
          for (let c = 1; c <= 10; c++) {
            const occupied = Math.random() > 0.4
            seats.push({
              row: r,
              col: c,
              status: occupied ? 'occupied' : 'available',
              user: occupied ? `学员${r}${c}` : null,
              selected: false
            })
          }
        }
        this.seatList = seats
      } catch (error) {
        console.error('Load seats error:', error)
      }
    },

    /**
     * 选择区域
     */
    selectArea(index) {
      this.selectedAreaIndex = index
      this.selectedArea = this.areas[index]
      this.loadSeats()
    },

    /**
     * 获取座位样式类
     */
    getSeatClass(seat) {
      const classes = []
      if (seat.status === 'occupied') classes.push('occupied')
      if (seat.selected) classes.push('selected')
      return classes.join(' ')
    },

    /**
     * 处理座位点击
     */
    handleSeat(seat) {
      if (!this.editMode) {
        // 查看座位详情
        this.showSeatDetail(seat)
        return
      }

      if (seat.status === 'occupied') {
        uni.showToast({
          title: '该座位已分配',
          icon: 'none'
        })
        return
      }

      // 切换选中状态
      seat.selected = !seat.selected

      if (seat.selected) {
        this.selectedSeats.push(seat)
      } else {
        const index = this.selectedSeats.findIndex(s => s === seat)
        if (index > -1) {
          this.selectedSeats.splice(index, 1)
        }
      }

      this.updateStats()
    },

    /**
     * 显示座位详情
     */
    showSeatDetail(seat) {
      const content = seat.user
        ? `座位：${seat.row}行${seat.col}列\n占用者：${seat.user}`
        : `座位：${seat.row}行${seat.col}列\n状态：可用`

      uni.showModal({
        title: '座位详情',
        content: content,
        showCancel: true
      })
    },

    /**
     * 切换编辑模式
     */
    toggleEditMode() {
      this.editMode = !this.editMode

      if (!this.editMode) {
        // 退出编辑模式，清除选中
        this.clearSelection()
      }
    },

    /**
     * 自动分配
     */
    autoAssign() {
      uni.showToast({
        title: '自动分配功能开发中',
        icon: 'none'
      })
    },

    /**
     * 导入座位
     */
    importSeats() {
      uni.showToast({
        title: '导入功能开发中',
        icon: 'none'
      })
    },

    /**
     * 导出座位
     */
    exportSeats() {
      uni.showToast({
        title: '导出功能开发中',
        icon: 'none'
      })
    },

    /**
     * 批量分配
     */
    batchAssign() {
      uni.showToast({
        title: '批量分配功能开发中',
        icon: 'none'
      })
    },

    /**
     * 批量清除
     */
    batchClear() {
      this.selectedSeats.forEach(seat => {
        seat.selected = false
      })
      this.selectedSeats = []
      this.updateStats()
    },

    /**
     * 清除选择
     */
    clearSelection() {
      this.seatList.forEach(seat => {
        seat.selected = false
      })
      this.selectedSeats = []
      this.updateStats()
    },

    /**
     * 更新统计
     */
    updateStats() {
      this.seatStats.selected = this.selectedSeats.length
    },

    /**
     * 显示菜单
     */
    showMenu() {
      uni.showActionSheet({
        itemList: ['保存座位图', '重置座位', '预览座位图'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.saveSeats()
          } else if (res.tapIndex === 1) {
            this.resetSeats()
          } else if (res.tapIndex === 2) {
            this.previewSeats()
          }
        }
      })
    },

    /**
     * 保存座位
     */
    saveSeats() {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
    },

    /**
     * 重置座位
     */
    resetSeats() {
      uni.showModal({
        title: '确认重置',
        content: '确定要重置所有座位分配吗？',
        success: (res) => {
          if (res.confirm) {
            this.loadSeats()
            uni.showToast({
              title: '已重置',
              icon: 'success'
            })
          }
        }
      })
    },

    /**
     * 预览座位
     */
    previewSeats() {
      uni.navigateTo({
        url: `/pages/learner/seat?id=${this.meetingId}`
      })
    },

    /**
     * 返回
     */
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.seat-manage-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  border-bottom: 1rpx solid $border-color;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 $spacing-md;
}

.back-btn {
  font-size: 48rpx;
  color: $text-primary;
  font-weight: 300;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.header-action {
  font-size: 48rpx;
  color: $text-primary;
}

.toolbar {
  margin: $spacing-md;
  padding: $spacing-md;
  display: flex;
  justify-content: space-around;
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.tool-icon {
  font-size: 40rpx;
}

.tool-text {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.seat-map-section,
.stats-section {
  margin: $spacing-md;
}

.area-scroll {
  white-space: nowrap;
  margin-bottom: $spacing-md;
}

.area-list {
  display: inline-flex;
  gap: $spacing-sm;
}

.area-item {
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-lg;
  font-size: $font-size-sm;
  color: $text-secondary;
  background: $bg-tertiary;
  transition: $transition-base;
}

.area-item.active {
  background: $primary-color;
  color: $text-white;
}

.seat-scroll {
  width: 100%;
  height: 500rpx;
  margin-bottom: $spacing-md;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(10, 64rpx);
  gap: 8rpx;
  padding: $spacing-md;
}

.seat-item {
  width: 64rpx;
  height: 64rpx;
  border-radius: $border-radius-sm;
  background: #d1fae5;
  border: 2rpx solid transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: $font-size-xs;
  font-weight: 500;
  color: $success-color;
  transition: $transition-base;
}

.seat-item.occupied {
  background: #fee2e2;
  color: $danger-color;
}

.seat-item.selected {
  background: $primary-color;
  color: $text-white;
  border-color: $primary-color;
  box-shadow: $shadow-md;
}

.seat-number {
  font-size: 16rpx;
}

.seat-user {
  font-size: 10rpx;
  opacity: 0.8;
}

.legend {
  display: flex;
  gap: $spacing-lg;
  padding: $spacing-md 0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.legend-color {
  width: 32rpx;
  height: 32rpx;
  border-radius: $border-radius-xs;
}

.legend-color.available {
  background: #d1fae5;
}

.legend-color.occupied {
  background: #fee2e2;
}

.legend-color.selected {
  background: $primary-color;
}

.legend-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.stats-grid {
  padding: $spacing-lg 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6rpx;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.batch-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  background: $bg-primary;
  border-top: 1rpx solid $border-color;
  z-index: $z-index-fixed;
}

.batch-info {
  font-size: $font-size-md;
  color: $text-primary;
}

.batch-actions {
  display: flex;
  gap: $spacing-sm;
}

.batch-btn {
  padding: $spacing-sm $spacing-md;
}
</style>
