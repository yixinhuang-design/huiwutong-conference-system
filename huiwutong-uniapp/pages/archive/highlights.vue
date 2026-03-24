<template>
  <view class="highlights-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">精彩花絮</text>
        <text class="header-action" @click="uploadHighlight">上传</text>
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

    <!-- 分类标签 -->
    <view class="category-tabs">
      <scroll-view class="tabs-scroll" scroll-x>
        <view class="tabs-list">
          <view
            v-for="category in categories"
            :key="category.value"
            class="tab-item"
            :class="{ active: activeCategory === category.value }"
            @click="switchCategory(category.value)"
          >
            <text class="tab-icon"><text :class="category.icon"></text></text>
            <text class="tab-label">{{ category.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <scroll-view class="highlights-scroll" scroll-y @scrolltolower="loadMore">
      <!-- 花絮列表 -->
      <view class="highlights-grid">
        <view
          v-for="(highlight, index) in filteredHighlights"
          :key="highlight.id"
          class="highlight-item"
          :class="'layout-' + highlight.layout"
          @click="viewHighlight(highlight)"
        >
          <image class="highlight-image" :src="highlight.image" mode="aspectFill"></image>
          <view class="highlight-overlay">
            <view class="highlight-info">
              <text class="highlight-title">{{ highlight.title }}</text>
              <view class="highlight-meta">
                <text class="meta-item"><text class="fa fa-eye"></text> {{ highlight.views }}</text>
                <text class="meta-item"><text class="fa fa-heart"></text> {{ highlight.likes }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && filteredHighlights.length > 0" class="no-more-text">
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
      activeCategory: 'all',
      loading: false,
      hasMore: true,
      currentPage: 1,
      trainings: [
        '2024年度第四期干部综合素质提升培训',
        '2024年度第三期干部综合素质提升培训',
        '2024年度第二期干部综合素质提升培训'
      ],
      categories: [
        { icon: 'fa-camera', label: '全部', value: 'all' },
        { icon: 'fa-microphone', label: '授课', value: 'lecture' },
        { icon: 'fa-comments', label: '讨论', value: 'discussion' },
        { icon: 'fa-users', label: '合影', value: 'group' },
        { icon: 'fa-glass-cheers', label: '活动', value: 'activity' }
      ],
      highlights: [
        {
          id: 1,
          title: '开班仪式',
          image: 'https://via.placeholder.com/400x300?text=开班仪式',
          category: 'activity',
          layout: 'large',
          views: 156,
          likes: 45
        },
        {
          id: 2,
          title: '专家授课',
          image: 'https://via.placeholder.com/400x600?text=专家授课',
          category: 'lecture',
          layout: 'tall',
          views: 128,
          likes: 38
        },
        {
          id: 3,
          title: '分组讨论',
          image: 'https://via.placeholder.com/400x300?text=分组讨论',
          category: 'discussion',
          layout: 'normal',
          views: 95,
          likes: 28
        },
        {
          id: 4,
          title: '学员合影',
          image: 'https://via.placeholder.com/400x400?text=学员合影',
          category: 'group',
          layout: 'normal',
          views: 210,
          likes: 68
        },
        {
          id: 5,
          title: '茶歇时光',
          image: 'https://via.placeholder.com/400x300?text=茶歇时光',
          category: 'activity',
          layout: 'normal',
          views: 87,
          likes: 25
        },
        {
          id: 6,
          title: '结业典礼',
          image: 'https://via.placeholder.com/400x600?text=结业典礼',
          category: 'activity',
          layout: 'tall',
          views: 178,
          likes: 52
        }
      ]
    }
  },

  computed: {
    filteredHighlights() {
      if (this.activeCategory === 'all') {
        return this.highlights
      }
      return this.highlights.filter(h => h.category === this.activeCategory)
    }
  },

  methods: {
    onTrainingChange(e) {
      this.selectedTraining = this.trainings[e.detail.value]
      this.loadHighlights()
    },

    switchCategory(category) {
      this.activeCategory = category
    },

    viewHighlight(highlight) {
      const urls = this.filteredHighlights.map(h => h.image)
      uni.previewImage({
        current: highlight.image,
        urls: urls
      })
    },

    uploadHighlight() {
      uni.showActionSheet({
        itemList: ['拍照', '从相册选择'],
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.chooseImage({
              count: 1,
              sourceType: ['camera'],
              success: () => {
                uni.showToast({
                  title: '上传功能开发中',
                  icon: 'none'
                })
              }
            })
          } else {
            uni.chooseImage({
              count: 9,
              success: () => {
                uni.showToast({
                  title: '上传功能开发中',
                  icon: 'none'
                })
              }
            })
          }
        }
      })
    },

    loadHighlights() {
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

.highlights-container {
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

.category-tabs {
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
}

.tabs-scroll {
  width: 100%;
  white-space: nowrap;
}

.tabs-list {
  display: inline-flex;
  padding: $spacing-sm $spacing-md;
  gap: $spacing-md;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  transition: all 0.3s;
}

.tab-item.active {
  background: rgba(102, 126, 234, 0.1);
}

.tab-icon {
  font-size: $font-size-md;
}

.tab-label {
  font-size: $font-size-md;
  color: $text-secondary;
  white-space: nowrap;
}

.tab-item.active .tab-label {
  color: $primary-color;
  font-weight: 500;
}

.highlights-scroll {
  height: calc(100vh - 240rpx);
  padding: $spacing-sm;
}

.highlights-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
}

.highlight-item {
  position: relative;
  border-radius: $border-radius-md;
  overflow: hidden;
  background: $bg-tertiary;
}

.highlight-item.layout-normal {
  grid-column: span 1;
  grid-row: span 1;
  aspect-ratio: 1;
}

.highlight-item.layout-large {
  grid-column: span 2;
  grid-row: span 2;
  aspect-ratio: 4/3;
}

.highlight-item.layout-tall {
  grid-column: span 1;
  grid-row: span 2;
  aspect-ratio: 3/4;
}

.highlight-image {
  width: 100%;
  height: 100%;
}

.highlight-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-sm;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
}

.highlight-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.highlight-title {
  font-size: $font-size-sm;
  color: $text-white;
  font-weight: 500;
}

.highlight-meta {
  display: flex;
  gap: $spacing-md;
}

.meta-item {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.8);
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-lg;
  color: $text-tertiary;
  font-size: $font-size-sm;
}
</style>
