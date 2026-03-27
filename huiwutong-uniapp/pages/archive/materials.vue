<template>
  <view class="materials-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">资料归档</text>
        <text class="header-action" @click="filterMaterials">筛选</text>
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

    <!-- 统计信息 -->
    <view class="stats-section card">
      <view class="stat-item">
        <text class="stat-icon">📄</text>
        <text class="stat-num">{{ stats.total }}</text>
        <text class="stat-label">资料总数</text>
      </view>
      <view class="stat-item">
        <text class="stat-icon">📥</text>
        <text class="stat-num">{{ stats.downloads }}</text>
        <text class="stat-label">下载次数</text>
      </view>
      <view class="stat-item">
        <text class="stat-icon">💾</text>
        <text class="stat-num">{{ formatSize(stats.size) }}</text>
        <text class="stat-label">总大小</text>
      </view>
    </view>

    <scroll-view class="materials-scroll" scroll-y @scrolltolower="loadMore">
      <!-- 资料列表 -->
      <view class="materials-list">
        <view
          v-for="(material, index) in filteredMaterials"
          :key="material.id"
          class="material-card card"
          @click="previewMaterial(material)"
        >
          <view class="material-icon" :class="'type-' + material.type">
            {{ getFileIcon(material.type) }}
          </view>

          <view class="material-info">
            <text class="material-name">{{ material.name }}</text>
            <view class="material-meta">
              <text class="meta-item">{{ material.type.toUpperCase() }}</text>
              <text class="meta-item">{{ formatSize(material.size) }}</text>
              <text class="meta-item">{{ material.uploadTime }}</text>
            </view>
            <text class="material-desc" v-if="material.description">{{ material.description }}</text>
          </view>

          <view class="material-actions" @click.stop>
            <button class="btn btn-sm btn-outline" @click="downloadMaterial(material)">
              下载
            </button>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && filteredMaterials.length > 0" class="no-more-text">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedTraining: '2024年度第四期干部综合素质提升培训',
      loading: false,
      hasMore: true,
      currentPage: 1,
      trainings: [
        '2024年度第四期干部综合素质提升培训',
        '2024年度第三期干部综合素质提升培训',
        '2024年度第二期干部综合素质提升培训'
      ],
      stats: {
        total: 28,
        downloads: 156,
        size: 52428800
      },
      materials: [
        {
          id: 1,
          name: '培训手册.pdf',
          type: 'pdf',
          size: 2097152,
          uploadTime: '2024-12-10',
          description: '培训指南和课程安排'
        },
        {
          id: 2,
          name: '课程讲义.docx',
          type: 'docx',
          size: 524288,
          uploadTime: '2024-12-10',
          description: '讲师课程讲义合集'
        },
        {
          id: 3,
          name: '培训照片.zip',
          type: 'zip',
          size: 15728640,
          uploadTime: '2024-12-12',
          description: '培训现场照片合集'
        },
        {
          id: 4,
          name: '学员名单.xlsx',
          type: 'xlsx',
          size: 163840,
          uploadTime: '2024-12-10',
          description: '学员基本信息和联系方式'
        },
        {
          id: 5,
          name: '签到表.pdf',
          type: 'pdf',
          size: 1048576,
          uploadTime: '2024-12-10',
          description: '每日签到记录'
        },
        {
          id: 6,
          name: '培训视频.mp4',
          type: 'mp4',
          size: 52428800,
          uploadTime: '2024-12-12',
          description: '培训录像精选'
        }
      ],
      categoryFilter: 'all'
    }
  },

  computed: {
    filteredMaterials() {
      return this.materials
    }
  },

  methods: {
    onTrainingChange(e) {
      this.selectedTraining = this.trainings[e.detail.value]
      this.loadMaterials()
    },

    getFileIcon(type) {
      const iconMap = {
        pdf: '📕',
        docx: '📘',
        xlsx: '📗',
        pptx: '📙',
        zip: '📦',
        mp4: '🎬',
        jpg: '🖼️',
        png: '🖼️'
      }
      return iconMap[type] || '📄'
    },

    formatSize(bytes) {
      if (bytes < 1024) return bytes + 'B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
      if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
      return (bytes / (1024 * 1024 * 1024)).toFixed(1) + 'GB'
    },

    previewMaterial(material) {
      if (material.type === 'pdf') {
        uni.showToast({
          title: 'PDF预览功能开发中',
          icon: 'none'
        })
      } else {
        uni.showModal({
          title: material.name,
          content: `大小：${this.formatSize(material.size)}\n上传时间：${material.uploadTime}\n\n是否下载？`,
          success: (res) => {
            if (res.confirm) {
              this.downloadMaterial(material)
            }
          }
        })
      }
    },

    downloadMaterial(material) {
      uni.showLoading({ title: '下载中...' })

      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({
          title: '下载功能开发中',
          icon: 'none'
        })
      }, 1000)
    },

    filterMaterials() {
      uni.showActionSheet({
        itemList: ['全部', 'PDF文档', 'Word文档', 'Excel表格', '压缩文件', '图片视频'],
        success: (res) => {
          const types = ['', 'pdf', 'docx', 'xlsx', 'zip', '']
          this.categoryFilter = types[res.tapIndex] || 'all'
        }
      })
    },

    loadMaterials() {
      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    loadMore() {
      if (this.loading || !this.hasMore) return

      this.loading = true
      setTimeout(() => {
        this.currentPage++
        this.loading = false

        if (this.currentPage >= 3) {
          this.hasMore = false
        }
      }, 1000)
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
@import '../../styles/global-patch.scss';

.materials-container {
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

.stats-section {
  display: flex;
  justify-content: space-around;
  padding: $spacing-md;
  background: $bg-primary;
  margin-bottom: $spacing-sm;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
}

.stat-icon {
  font-size: 40rpx;
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

.materials-scroll {
  height: calc(100vh - 260rpx);
  padding: $spacing-md;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.material-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: 0;
}

.material-icon {
  font-size: 64rpx;
  flex-shrink: 0;
}

.material-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.material-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.material-meta {
  display: flex;
  gap: $spacing-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.material-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.material-actions {
  flex-shrink: 0;
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-lg;
  color: $text-tertiary;
  font-size: $font-size-sm;
}
</style>
