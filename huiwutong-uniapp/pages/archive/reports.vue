<template>
  <view class="reports-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">总结报告</text>
        <text class="header-action" @click="createReport">新建</text>
      </view>
    </view>

    <!-- 培训选择 -->
    <view class="training-selector">
      <picker mode="selector" :range="trainings" @change="onTrainingChange">
        <view class="selector-bar">
          <text class="selector-label">选择培训</text>
          <view class="selector-value">
            <text>{{ selectedTraining }}</text>
            <text class="selector-arrow">▼</text>
          </view>
        </view>
      </picker>
    </view>

    <scroll-view class="reports-scroll" scroll-y>
      <!-- 报告概览 -->
      <view class="overview-section card" v-if="overview">
        <view class="overview-title">培训概览</view>
        <view class="overview-grid">
          <view class="overview-item">
            <text class="overview-label">培训时间</text>
            <text class="overview-value">{{ overview.date }}</text>
          </view>
          <view class="overview-item">
            <text class="overview-label">培训天数</text>
            <text class="overview-value">{{ overview.days }}天</text>
          </view>
          <view class="overview-item">
            <text class="overview-label">参训人数</text>
            <text class="overview-value">{{ overview.participants }}人</text>
          </view>
          <view class="overview-item">
            <text class="overview-label">出勤率</text>
            <text class="overview-value">{{ overview.attendance }}%</text>
          </view>
          <view class="overview-item">
            <text class="overview-label">满意度</text>
            <text class="overview-value">{{ overview.satisfaction }}%</text>
          </view>
          <view class="overview-item">
            <text class="overview-label">结业率</text>
            <text class="overview-value">{{ overview.completion }}%</text>
          </view>
        </view>
      </view>

      <!-- 报告列表 -->
      <view class="reports-section">
        <view class="section-title">报告列表</view>
        <view class="reports-list">
          <view
            v-for="report in reports"
            :key="report.id"
            class="report-card card"
            @click="viewReport(report)"
          >
            <view class="report-header">
              <view class="report-icon">{{ getReportIcon(report.type) }}</view>
              <view class="report-info">
                <text class="report-title">{{ report.title }}</text>
                <text class="report-type">{{ getTypeLabel(report.type) }}</text>
              </view>
              <text class="report-date">{{ report.createTime }}</text>
            </view>

            <view class="report-meta">
              <text class="meta-item">📄 {{ report.format }}</text>
              <text class="meta-item">📊 {{ report.pages }}页</text>
              <text class="meta-item">💾 {{ formatSize(report.size) }}</text>
            </view>

            <view class="report-actions">
              <button
                class="btn btn-sm btn-outline"
                @click.stop="previewReport(report)"
              >
                预览
              </button>
              <button
                class="btn btn-sm btn-primary"
                @click.stop="downloadReport(report)"
              >
                下载
              </button>
            </view>
          </view>
        </view>
      </view>

      <!-- 数据图表 -->
      <view class="charts-section card" v-if="showCharts">
        <view class="section-title">数据统计</view>

        <view class="chart-item">
          <view class="chart-title">每日出勤率</view>
          <view class="chart-placeholder">
            <text class="placeholder-icon">📊</text>
            <text class="placeholder-text">图表区域</text>
          </view>
        </view>

        <view class="chart-item">
          <view class="chart-title">满意度分布</view>
          <view class="chart-placeholder">
            <text class="placeholder-icon">📈</text>
            <text class="placeholder-text">图表区域</text>
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
      selectedTraining: '2024年度第四期干部综合素质提升培训',
      showCharts: true,
      trainings: [
        '2024年度第四期干部综合素质提升培训',
        '2024年度第三期干部综合素质提升培训',
        '2024年度第二期干部综合素质提升培训'
      ],
      overview: {
        date: '2024年12月10日-12日',
        days: 3,
        participants: 75,
        attendance: 95,
        satisfaction: 96,
        completion: 100
      },
      reports: [
        {
          id: 1,
          title: '培训总结报告',
          type: 'summary',
          format: 'PDF',
          pages: 15,
          size: 5242880,
          createTime: '2024-12-13'
        },
        {
          id: 2,
          title: '学员成绩单',
          type: 'grades',
          format: 'Excel',
          pages: 3,
          size: 262144,
          createTime: '2024-12-13'
        },
        {
          id: 3,
          title: '签到统计表',
          type: 'attendance',
          format: 'Excel',
          pages: 2,
          size: 163840,
          createTime: '2024-12-12'
        },
        {
          id: 4,
          title: '满意度调查报告',
          type: 'satisfaction',
          format: 'PDF',
          pages: 8,
          size: 3145728,
          createTime: '2024-12-13'
        },
        {
          id: 5,
          title: '培训照片集',
          type: 'photos',
          format: 'ZIP',
          pages: 50,
          size: 52428800,
          createTime: '2024-12-12'
        },
        {
          id: 6,
          title: '经费使用报告',
          type: 'finance',
          format: 'PDF',
          pages: 5,
          size: 1048576,
          createTime: '2024-12-13'
        }
      ]
    }
  },

  methods: {
    onTrainingChange(e) {
      this.selectedTraining = this.trainings[e.detail.value]
      this.loadReports()
    },

    getReportIcon(type) {
      const iconMap = {
        summary: '<text class="fa fa-clipboard"></text>',
        grades: '📊',
        attendance: '<text class="fa fa-check"></text>',
        satisfaction: '⭐',
        photos: '<text class="fa fa-camera"></text>',
        finance: '💰'
      }
      return iconMap[type] || '📄'
    },

    getTypeLabel(type) {
      const labelMap = {
        summary: '总结报告',
        grades: '成绩单',
        attendance: '签到表',
        satisfaction: '满意度',
        photos: '照片集',
        finance: '经费报告'
      }
      return labelMap[type] || '其他'
    },

    formatSize(bytes) {
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
      return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
    },

    viewReport(report) {
      uni.showModal({
        title: report.title,
        content: `类型：${this.getTypeLabel(report.type)}\n格式：${report.format}\n页数：${report.pages}页\n大小：${this.formatSize(report.size)}\n创建时间：${report.createTime}`,
        success: (res) => {
          if (res.confirm) {
            this.downloadReport(report)
          }
        }
      })
    },

    previewReport(report) {
      uni.showToast({
        title: '预览功能开发中',
        icon: 'none'
      })
    },

    downloadReport(report) {
      uni.showLoading({ title: '下载中...' })

      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({
          title: '下载功能开发中',
          icon: 'none'
        })
      }, 1000)
    },

    createReport() {
      uni.showActionSheet({
        itemList: ['生成总结报告', '生成成绩单', '生成签到表', '生成满意度报告'],
        success: (res) => {
          uni.showToast({
            title: '报告生成功能开发中',
            icon: 'none'
          })
        }
      })
    },

    loadReports() {
      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
      }, 500)
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

.reports-container {
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

.training-selector {
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
}

.selector-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 88rpx;
  padding: 0 $spacing-md;
}

.selector-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.selector-value {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.selector-value text {
  font-size: $font-size-md;
  color: $primary-color;
}

.selector-arrow {
  font-size: $font-size-xs;
}

.reports-scroll {
  height: calc(100vh - 180rpx);
  padding: $spacing-md;
}

.overview-section {
  margin-bottom: $spacing-md;
}

.overview-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.overview-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.overview-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.overview-value {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $primary-color;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.reports-section {
  margin-bottom: $spacing-lg;
}

.reports-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.report-card {
  margin-bottom: 0;
}

.report-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.report-icon {
  font-size: 48rpx;
}

.report-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.report-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.report-type {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.report-date {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.report-meta {
  display: flex;
  gap: $spacing-lg;
  margin-bottom: $spacing-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.report-actions {
  display: flex;
  gap: $spacing-sm;
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
}

.charts-section {
  margin-bottom: $spacing-lg;
}

.chart-item {
  margin-bottom: $spacing-lg;
}

.chart-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.placeholder-icon {
  font-size: 64rpx;
  margin-bottom: $spacing-sm;
}

.placeholder-text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}
</style>
