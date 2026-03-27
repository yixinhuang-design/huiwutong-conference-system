<template>
  <view class="handbook-generate-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">名册生成</text>
        <text class="header-action" @click="showHistory">历史</text>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 培训选择 -->
      <view class="training-section card">
        <view class="section-title">选择培训</view>
        <view class="training-selector" @click="selectTraining">
          <view class="selector-content">
            <text class="selector-label">当前培训</text>
            <text class="selector-value">{{ selectedTraining || '请选择培训' }}</text>
          </view>
          <text class="selector-arrow">›</text>
        </view>
      </view>

      <!-- 生成选项 -->
      <view class="options-section card">
        <view class="section-title">生成选项</view>
        <view class="option-list">
          <view
            v-for="option in generateOptions"
            :key="option.key"
            class="option-item"
            @click="toggleOption(option.key)"
          >
            <view class="option-checkbox" :class="{ checked: option.checked }">
              <text v-if="option.checked" class="checkbox-icon">✓</text>
            </view>
            <view class="option-info">
              <text class="option-label">{{ option.label }}</text>
              <text class="option-desc">{{ option.desc }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 排序方式 -->
      <view class="sort-section card">
        <view class="section-title">排序方式</view>
        <view class="sort-options">
          <view
            v-for="sort in sortOptions"
            :key="sort.value"
            class="sort-option"
            :class="{ active: sortType === sort.value }"
            @click="selectSort(sort.value)"
          >
            <text class="sort-icon">{{ sort.icon }}</text>
            <text class="sort-label">{{ sort.label }}</text>
          </view>
        </view>
      </view>

      <!-- 预览统计 -->
      <view class="preview-section card">
        <view class="section-title">数据统计</view>
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-num">{{ stats.total }}</text>
            <text class="stat-label">总人数</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ stats.groups }}</text>
            <text class="stat-label">分组数</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ stats.departments }}</text>
            <text class="stat-label">单位数</text>
          </view>
        </view>
      </view>

      <!-- 生成按钮 -->
      <view class="generate-section">
        <button class="btn btn-outline btn-block" @click="previewHandbook">
          👁️ 预览名册
        </button>
        <button class="btn btn-primary btn-block" @click="generateHandbook">
          📄 生成名册
        </button>
      </view>

      <!-- 生成历史 -->
      <view class="history-section card" v-if="historyList.length > 0">
        <view class="section-title">生成历史</view>
        <view class="history-list">
          <view
            v-for="(history, index) in historyList"
            :key="history.id"
            class="history-item"
          >
            <view class="history-info">
              <text class="history-name">{{ history.name }}</text>
              <text class="history-time">{{ history.time }}</text>
            </view>
            <view class="history-actions">
              <button class="btn btn-sm btn-outline" @click="downloadHistory(history)">
                下载
              </button>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedTraining: '2025年度第一期干部综合素质提升培训',
      sortType: 'group',
      generateOptions: [
        { key: 'photo', label: '包含照片', desc: '在名册中显示学员照片', checked: true },
        { key: 'group', label: '分组信息', desc: '显示学员所属分组', checked: true },
        { key: 'seat', label: '座位信息', desc: '显示学员座位号', checked: true },
        { key: 'contact', label: '联系方式', desc: '显示学员联系电话', checked: false }
      ],
      sortOptions: [
        { icon: '👥', label: '按分组', value: 'group' },
        { icon: '🏢', label: '按单位', value: 'department' },
        { icon: '🔤', label: '按姓名', value: 'name' }
      ],
      stats: {
        total: 80,
        groups: 8,
        departments: 15
      },
      historyList: [
        {
          id: 1,
          name: '学员名册_按分组_20250115.pdf',
          time: '2025-01-15 10:30'
        },
        {
          id: 2,
          name: '学员名册_按单位_20250114.pdf',
          time: '2025-01-14 16:20'
        }
      ]
    }
  },

  methods: {
    selectTraining() {
      uni.showToast({ title: '选择培训功能开发中', icon: 'none' })
    },

    toggleOption(key) {
      const option = this.generateOptions.find(opt => opt.key === key)
      if (option) {
        option.checked = !option.checked
      }
    },

    selectSort(value) {
      this.sortType = value
    },

    previewHandbook() {
      uni.showLoading({ title: '生成预览...' })

      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({
          title: '预览功能开发中',
          icon: 'none'
        })
      }, 1000)
    },

    generateHandbook() {
      const checkedOptions = this.generateOptions
        .filter(opt => opt.checked)
        .map(opt => opt.label)
        .join('、')

      uni.showModal({
        title: '生成名册',
        content: `确认生成名册？\n\n排序：${this.getSortLabel()}\n选项：${checkedOptions}`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '生成中...' })

            setTimeout(() => {
              uni.hideLoading()

              const history = {
                id: Date.now(),
                name: `学员名册_${this.getSortLabel()}_${this.getCurrentDate()}.pdf`,
                time: this.getCurrentTime()
              }

              this.historyList.unshift(history)

              uni.showToast({
                title: '生成成功',
                icon: 'success'
              })
            }, 2000)
          }
        }
      })
    },

    getSortLabel() {
      const sort = this.sortOptions.find(opt => opt.value === this.sortType)
      return sort ? sort.label : '分组'
    },

    downloadHistory(history) {
      uni.showLoading({ title: '下载中...' })

      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({
          title: '下载功能开发中',
          icon: 'none'
        })
      }, 1000)
    },

    showHistory() {
      if (this.historyList.length === 0) {
        uni.showToast({
          title: '暂无历史记录',
          icon: 'none'
        })
        return
      }

      uni.showModal({
        title: '生成历史',
        content: `共${this.historyList.length}条记录`,
        showCancel: false
      })
    },

    getCurrentDate() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      return `${year}${month}${day}`
    },

    getCurrentTime() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    },

    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.handbook-generate-container {
  min-height: 100vh;
  background: $bg-secondary;
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
  flex: 1;
  text-align: center;
}

.header-action {
  font-size: $font-size-md;
  color: $primary-color;
}

.content-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.card {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.training-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.selector-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.selector-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.selector-value {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.selector-arrow {
  font-size: 32rpx;
  color: $text-tertiary;
  margin-left: $spacing-md;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.option-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.option-checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s;
}

.option-checkbox.checked {
  background: $primary-color;
  border-color: $primary-color;
}

.checkbox-icon {
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: bold;
}

.option-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.option-label {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.option-desc {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.sort-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.sort-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.sort-option.active {
  background: rgba(102, 126, 234, 0.1);
  border-color: $primary-color;
}

.sort-icon {
  font-size: 40rpx;
}

.sort-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.sort-option.active .sort-label {
  color: $primary-color;
  font-weight: 500;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-lg;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.stat-num {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.generate-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.history-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.history-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.history-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
}
</style>
