<template>
  <view class="data-analysis-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">数据分析</text>
        <text class="header-action" @click="exportReport">导出</text>
      </view>
    </view>

    <scroll-view class="analysis-scroll" scroll-y>
      <!-- 时间选择 -->
      <view class="time-selector card">
        <view class="selector-tabs">
          <view
            v-for="tab in timeTabs"
            :key="tab.value"
            class="selector-tab"
            :class="{ active: activeTimeTab === tab.value }"
            @click="switchTimeTab(tab.value)"
          >
            {{ tab.label }}
          </view>
        </view>
        <view class="date-range" @click="showDatePicker">
          <text class="date-text">{{ dateRangeText }}</text>
          <text class="date-icon"><text class="fa fa-calendar-alt"></text></text>
        </view>
      </view>

      <!-- 核心指标 -->
      <view class="metrics-section">
        <view class="section-title">核心指标</view>
        <view class="metrics-grid">
          <view class="metric-card" v-for="(metric, index) in coreMetrics" :key="index">
            <text class="metric-icon">{{ metric.icon }}</text>
            <view class="metric-info">
              <text class="metric-value">{{ metric.value }}</text>
              <text class="metric-label">{{ metric.label }}</text>
              <view class="metric-trend" v-if="metric.trend">
                <text class="trend-icon" :class="metric.trend > 0 ? 'up' : 'down'">
                  {{ metric.trend > 0 ? '↑' : '↓' }}
                </text>
                <text class="trend-value" :class="metric.trend > 0 ? 'up' : 'down'">
                  {{ Math.abs(metric.trend) }}%
                </text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 报名趋势 -->
      <view class="chart-section card">
        <view class="section-header">
          <text class="section-title">报名趋势</text>
          <view class="chart-legend">
            <view class="legend-item">
              <view class="legend-dot" style="background: #667eea"></view>
              <text class="legend-text">报名人数</text>
            </view>
            <view class="legend-item">
              <view class="legend-dot" style="background: #10b981"></view>
              <text class="legend-text">审核通过</text>
            </view>
          </view>
        </view>

        <view class="chart-container">
          <view class="chart-bars">
            <view
              v-for="(item, index) in registrationTrend"
              :key="index"
              class="chart-bar-group"
            >
              <view class="bar-wrapper">
                <view
                  class="bar bar-primary"
                  :style="{ height: (item.registrations / 100) * 200 + 'rpx' }"
                ></view>
                <view
                  class="bar bar-success"
                  :style="{ height: (item.approved / 100) * 200 + 'rpx' }"
                ></view>
              </view>
              <text class="bar-label">{{ item.date }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 学员构成 -->
      <view class="chart-section card">
        <view class="section-title">学员构成分析</view>
        <view class="composition-grid">
          <view
            v-for="(item, index) in compositionData"
            :key="index"
            class="composition-item"
          >
            <view class="composition-header">
              <text class="composition-label">{{ item.label }}</text>
              <text class="composition-value">{{ item.value }}人</text>
            </view>
            <view class="progress-bar">
              <view
                class="progress-fill"
                :style="{ width: item.percent + '%', background: item.color }"
              ></view>
            </view>
            <text class="composition-percent">{{ item.percent }}%</text>
          </view>
        </view>
      </view>

      <!-- 签到统计 -->
      <view class="chart-section card">
        <view class="section-title">签到统计</view>
        <view class="checkin-stats">
          <view class="checkin-row">
            <text class="checkin-label">应签到人数</text>
            <text class="checkin-value">{{ checkinData.total }}人</text>
          </view>
          <view class="checkin-row">
            <text class="checkin-label">实签到人数</text>
            <text class="checkin-value success">{{ checkinData.checked }}人</text>
          </view>
          <view class="checkin-row">
            <text class="checkin-label">签到率</text>
            <text class="checkin-value highlight">{{ checkinData.rate }}%</text>
          </view>

          <view class="checkin-method">
            <view class="method-item">
              <view class="method-chart">
                <view
                  class="chart-pie"
                  :style="{
                    background: `conic-gradient(#667eea 0% ${checkinData.qrPercent}%, #f59e0b ${checkinData.qrPercent}% 100%)`
                  }"
                ></view>
              </view>
              <view class="method-info">
                <text class="method-label">扫码签到</text>
                <text class="method-value">{{ checkinData.qrPercent }}%</text>
              </view>
            </view>
            <view class="method-item">
              <view class="method-chart">
                <view
                  class="chart-pie"
                  :style="{
                    background: `conic-gradient(#667eea 0% ${checkinData.gpsPercent}%, #f59e0b ${checkinData.gpsPercent}% 100%)`
                  }"
                ></view>
              </view>
              <view class="method-info">
                <text class="method-label">GPS签到</text>
                <text class="method-value">{{ checkinData.gpsPercent }}%</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 活跃度排行 -->
      <view class="ranking-section card">
        <view class="section-title">活跃度排行</view>
        <view class="ranking-list">
          <view
            v-for="(item, index) in rankingList"
            :key="index"
            class="ranking-item"
          >
            <view class="ranking-index" :class="'rank-' + (index + 1)">
              {{ index + 1 }}
            </view>
            <view class="ranking-info">
              <text class="ranking-name">{{ item.name }}</text>
              <text class="ranking-dept">{{ item.dept }}</text>
            </view>
            <view class="ranking-score">
              <text class="score-value">{{ item.score }}</text>
              <text class="score-label">分</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 数据对比 -->
      <view class="chart-section card">
        <view class="section-title">历史对比</view>
        <view class="comparison-table">
          <view class="table-header">
            <text class="th">期次</text>
            <text class="th">报名数</text>
            <text class="th">签到率</text>
            <text class="th">满意度</text>
          </view>
          <view class="table-body">
            <view
              v-for="(item, index) in comparisonData"
              :key="index"
              class="table-row"
              :class="{ highlight: index === 0 }"
            >
              <text class="td">{{ item.period }}</text>
              <text class="td">{{ item.registrations }}</text>
              <text class="td">{{ item.checkinRate }}%</text>
              <text class="td">{{ item.satisfaction }}%</text>
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
      activeTimeTab: 'week',
      dateRange: '2025-01-09',
      timeTabs: [
        { label: '本周', value: 'week' },
        { label: '本月', value: 'month' },
        { label: '本年', value: 'year' }
      ],
      coreMetrics: [
        { icon: '<text class="fa fa-users"></text>', label: '总报名数', value: '1,234', trend: 15.6 },
        { icon: '<text class="fa fa-check"></text>', label: '审核通过', value: '1,180', trend: 12.3 },
        { icon: '<text class="fa fa-map-marker-alt"></text>', label: '签到人数', value: '1,156', trend: 8.9 },
        { icon: '⭐', label: '满意度', value: '96.8%', trend: 2.1 }
      ],
      registrationTrend: [
        { date: '周一', registrations: 85, approved: 78 },
        { date: '周二', registrations: 92, approved: 88 },
        { date: '周三', registrations: 105, approved: 98 },
        { date: '周四', registrations: 78, approved: 72 },
        { date: '周五', registrations: 95, approved: 90 },
        { date: '周六', registrations: 112, approved: 105 },
        { date: '周日', registrations: 88, approved: 82 }
      ],
      compositionData: [
        { label: '市级部门', value: 45, percent: 56, color: '#667eea' },
        { label: '区县部门', value: 28, percent: 35, color: '#10b981' },
        { label: '乡镇街道', value: 8, percent: 10, color: '#f59e0b' }
      ],
      checkinData: {
        total: 80,
        checked: 76,
        rate: 95,
        qrPercent: 65,
        gpsPercent: 35
      },
      rankingList: [
        { name: '张伟', dept: '市教育局', score: 98 },
        { name: '李娜', dept: '市卫健委', score: 95 },
        { name: '王强', dept: '市委组织部', score: 93 },
        { name: '刘芳', dept: '市财政局', score: 91 },
        { name: '陈明', dept: '市人社局', score: 88 }
      ],
      comparisonData: [
        { period: '第5期', registrations: 156, checkinRate: 95, satisfaction: 97 },
        { period: '第4期', registrations: 142, checkinRate: 92, satisfaction: 94 },
        { period: '第3期', registrations: 138, checkinRate: 89, satisfaction: 92 },
        { period: '第2期', registrations: 125, checkinRate: 91, satisfaction: 90 },
        { period: '第1期', registrations: 118, checkinRate: 88, satisfaction: 89 }
      ]
    }
  },

  computed: {
    dateRangeText() {
      if (this.activeTimeTab === 'week') {
        return '本周 (01.09-01.15)'
      } else if (this.activeTimeTab === 'month') {
        return '本月 (01.01-01.31)'
      } else {
        return '2025年'
      }
    }
  },

  methods: {
    goBack() {
      uni.navigateBack()
    },

    switchTimeTab(tab) {
      this.activeTimeTab = tab
      uni.showToast({ title: '数据已更新', icon: 'success' })
    },

    showDatePicker() {
      uni.showToast({ title: '日期选择功能开发中', icon: 'none' })
    },

    exportReport() {
      uni.showLoading({ title: '导出中...' })
      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({ title: '导出成功', icon: 'success' })
      }, 1500)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.data-analysis-container {
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

.analysis-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.time-selector {
  margin-bottom: $spacing-md;
}

.selector-tabs {
  display: flex;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  padding: 4rpx;
  margin-bottom: $spacing-md;
}

.selector-tab {
  flex: 1;
  text-align: center;
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
  color: $text-secondary;
  border-radius: $border-radius-sm;
  transition: all 0.3s;
}

.selector-tab.active {
  background: $bg-primary;
  color: $primary-color;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.date-range {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.date-text {
  font-size: $font-size-md;
  color: $text-primary;
}

.date-icon {
  font-size: $font-size-lg;
}

.metrics-section,
.chart-section,
.ranking-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-md;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
  box-shadow: $shadow-sm;
}

.metric-icon {
  font-size: 48rpx;
}

.metric-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.metric-value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.metric-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.trend-icon {
  font-size: $font-size-xs;
  font-weight: bold;
}

.trend-icon.up {
  color: #10b981;
}

.trend-icon.down {
  color: #ef4444;
}

.trend-value {
  font-size: $font-size-xs;
  font-weight: 500;
}

.trend-value.up {
  color: #10b981;
}

.trend-value.down {
  color: #ef4444;
}

.chart-legend {
  display: flex;
  gap: $spacing-md;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.legend-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.legend-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.chart-container {
  padding: $spacing-md 0;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 240rpx;
}

.chart-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
}

.bar-wrapper {
  display: flex;
  gap: 8rpx;
  align-items: flex-end;
  height: 200rpx;
}

.bar {
  width: 24rpx;
  border-radius: 4rpx 4rpx 0 0;
  transition: height 0.3s;
}

.bar-primary {
  background: $primary-color;
}

.bar-success {
  background: #10b981;
}

.bar-label {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.composition-grid {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.composition-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.composition-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.composition-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.composition-value {
  font-size: $font-size-md;
  font-weight: 600;
  color: $text-primary;
}

.composition-item .progress-bar {
  height: 12rpx;
  background: $bg-tertiary;
  border-radius: 6rpx;
  overflow: hidden;
}

.composition-item .progress-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.3s;
}

.composition-percent {
  font-size: $font-size-sm;
  color: $text-tertiary;
  text-align: right;
}

.checkin-stats {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.checkin-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.checkin-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.checkin-value {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.checkin-value.success {
  color: #10b981;
}

.checkin-value.highlight {
  color: $primary-color;
}

.checkin-method {
  display: flex;
  justify-content: space-around;
}

.method-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.method-chart {
  width: 120rpx;
  height: 120rpx;
}

.chart-pie {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.method-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.method-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.method-value {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $primary-color;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.ranking-index {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: $bg-primary;
  color: $text-secondary;
  font-size: $font-size-md;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ranking-index.rank-1 {
  background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
  color: $text-white;
}

.ranking-index.rank-2 {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  color: $text-white;
}

.ranking-index.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #a0522d 100%);
  color: $text-white;
}

.ranking-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.ranking-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.ranking-dept {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.ranking-score {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.score-value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.score-label {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.comparison-table {
  border: 1rpx solid $border-color;
  border-radius: $border-radius-md;
  overflow: hidden;
}

.table-header {
  display: flex;
  background: $bg-tertiary;
}

.table-header .th {
  flex: 1;
  text-align: center;
  padding: $spacing-md;
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
  border-right: 1rpx solid $border-color;
}

.table-header .th:last-child {
  border-right: none;
}

.table-body {
  display: flex;
  flex-direction: column;
}

.table-row {
  display: flex;
  border-top: 1rpx solid $border-color;
}

.table-row.highlight {
  background: rgba(102, 126, 234, 0.05);
}

.table-row .td {
  flex: 1;
  text-align: center;
  padding: $spacing-md;
  font-size: $font-size-sm;
  color: $text-secondary;
  border-right: 1rpx solid $border-color;
}

.table-row .td:last-child {
  border-right: none;
}
</style>
