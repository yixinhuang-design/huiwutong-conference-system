<template>
  <view class="materials-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">学习资料</text>
      </view>
    </view>

    <!-- 分类筛选 -->
    <view class="filter-bar card">
      <scroll-view class="filter-scroll" scroll-x>
        <view
          v-for="(category, index) in categories"
          :key="index"
          class="filter-item"
          :class="{ active: selectedCategory === index }"
          @click="selectCategory(index)"
        >
          {{ category.name }}
        </view>
      </scroll-view>
    </view>

    <!-- 资料列表 -->
    <view class="materials-list">
      <view v-if="materialList.length > 0">
        <view
          v-for="(material, index) in materialList"
          :key="index"
          class="material-item card"
          @click="handleMaterial(material)"
        >
          <view class="material-icon" :style="{ background: material.color }">
            <text>{{ material.icon }}</text>
          </view>
          <view class="material-info">
            <view class="material-title">{{ material.title }}</view>
            <view class="material-meta">
              <text class="meta-item">{{ material.type }}</text>
              <text class="meta-item">{{ material.size }}</text>
              <text class="meta-item">{{ material.date }}</text>
            </view>
          </view>
          <view class="material-action">
            <text class="action-text">{{ material.downloaded ? '已下载' : '下载' }}</text>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon"><text class="fa fa-book-open"></text></text>
        <text class="empty-text">暂无学习资料</text>
      </view>
    </view>
  </view>
</template>

<script>
import MaterialAPI from '@/api/material'

export default {
  data() {
    return {
      meetingId: '',
      selectedCategory: 0,
      categories: [
        { name: '全部', value: 'all' },
        { name: '课件', value: 'courseware' },
        { name: '讲义', value: 'handout' },
        { name: '参考资料', value: 'reference' },
        { name: '视频', value: 'video' }
      ],
      materialList: []
    }
  },

  onLoad(options) {
    if (options.id) {
      this.meetingId = options.id
      this.loadMaterials()
    }
  },

  methods: {
    selectCategory(index) {
      this.selectedCategory = index
      this.filterMaterials()
    },

    filterMaterials() {
      this.loadMaterials()
    },

    async loadMaterials() {
      try {
        const category = this.categories[this.selectedCategory]
        const params = { conferenceId: this.meetingId }
        if (category.value !== 'all') params.category = category.value
        const data = await MaterialAPI.list(params)
        const materials = data.records || data || []
        const typeIcons = { pdf: '📄', ppt: '📊', doc: '<text class="fa fa-edit"></text>', xls: '📊', video: '🎬', image: '🖼️' }
        const typeColors = { pdf: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', ppt: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', doc: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', xls: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', video: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', image: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' }
        this.materialList = materials.map(m => ({
          id: m.id,
          title: m.fileName,
          type: (m.fileType || 'pdf').toUpperCase(),
          size: m.fileSize ? (m.fileSize / 1024 / 1024).toFixed(1) + 'MB' : '',
          date: m.createTime ? m.createTime.substring(0, 10) : '',
          icon: typeIcons[m.fileType] || '📄',
          color: typeColors[m.fileType] || typeColors.pdf,
          category: m.category || '',
          downloaded: false
        }))
      } catch (error) {
        console.error('Load materials error:', error)
      }
    },

    /**
     * 处理资料点击
     */
    handleMaterial(material) {
      if (material.type === 'VIDEO') {
        // 视频预览
        uni.navigateTo({
          url: `/pages/learner/video-preview?id=${material.id}`
        })
      } else {
        // 文件预览或下载
        uni.showActionSheet({
          itemList: ['预览', '下载'],
          success: (res) => {
            if (res.tapIndex === 0) {
              this.previewMaterial(material)
            } else {
              this.downloadMaterial(material)
            }
          }
        })
      }
    },

    /**
     * 预览资料
     */
    previewMaterial(material) {
      uni.showToast({
        title: '预览功能开发中',
        icon: 'none'
      })
    },

    /**
     * 下载资料
     */
    async downloadMaterial(material) {
      try {
        uni.showLoading({ title: '下载中...' })
        await MaterialAPI.download(material.id)
        uni.hideLoading()
        material.downloaded = true
        uni.showToast({ title: '下载成功', icon: 'success' })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({ title: '下载失败', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.materials-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.filter-bar {
  margin: $spacing-md;
  padding: 0;
}

.filter-scroll {
  white-space: nowrap;
}

.filter-item {
  display: inline-block;
  padding: $spacing-sm $spacing-md;
  margin: $spacing-sm;
  border-radius: $border-radius-lg;
  font-size: $font-size-sm;
  color: $text-secondary;
  transition: $transition-base;
}

.filter-item.active {
  background: $primary-color;
  color: $text-white;
}

.materials-list {
  padding: 0 $spacing-md;
}

.material-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  transition: $transition-base;
}

.material-item:active {
  transform: scale(0.98);
}

.material-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: $border-radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  flex-shrink: 0;
}

.material-info {
  flex: 1;
  min-width: 0;
}

.material-title {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 6rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.material-meta {
  display: flex;
  gap: $spacing-md;
}

.meta-item {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.material-action {
  flex-shrink: 0;
  padding: $spacing-sm $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-sm;
}

.action-text {
  font-size: $font-size-sm;
  color: $primary-color;
  font-weight: 500;
}
</style>
