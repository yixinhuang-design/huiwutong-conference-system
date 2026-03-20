<template>
  <view class="highlights-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">精彩瞬间</text>
        <text class="header-action" @click="filterPhotos">筛选</text>
      </view>
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
            <text class="tab-icon">{{ category.icon }}</text>
            <text class="tab-label">{{ category.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 照片墙 -->
    <scroll-view class="photos-scroll" scroll-y @scrolltolower="loadMore">
      <view class="photos-grid">
        <view
          v-for="(photo, index) in filteredPhotos"
          :key="photo.id"
          class="photo-item"
          :class="'layout-' + photo.layout"
          @click="previewPhoto(index)"
        >
          <image class="photo-image" :src="photo.url" mode="aspectFill"></image>
          <view class="photo-mask">
            <view class="photo-info">
              <view class="photo-likes" @click.stop="likePhoto(photo)">
                <text class="like-icon" :class="{ liked: photo.isLiked }">
                  {{ photo.isLiked ? '❤️' : '🤍' }}
                </text>
                <text class="like-count">{{ photo.likeCount }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore" class="no-more-text">
        <text>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 浮动按钮 -->
    <view class="fab" @click="uploadPhoto">
      <text class="fab-icon">📷</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      activeCategory: 'all',
      loading: false,
      hasMore: true,
      currentPage: 1,
      categories: [
        { icon: '<text class="fa fa-camera"></text>', label: '全部', value: 'all' },
        { icon: '🎤', label: '授课', value: 'lecture' },
        { icon: '<text class="fa fa-comments"></text>', label: '讨论', value: 'discussion' },
        { icon: '<text class="fa fa-users"></text>', label: '合影', value: 'group' },
        { icon: '🎉', label: '活动', value: 'activity' }
      ],
      photos: [
        {
          id: 1,
          url: 'https://via.placeholder.com/400x300?text=开班仪式',
          category: 'activity',
          layout: 'large',
          likeCount: 45,
          isLiked: true,
          time: '2025-01-18 09:00',
          description: '开班仪式'
        },
        {
          id: 2,
          url: 'https://via.placeholder.com/400x600?text=专家授课',
          category: 'lecture',
          layout: 'tall',
          likeCount: 38,
          isLiked: false,
          time: '2025-01-18 10:00',
          description: '专家授课'
        },
        {
          id: 3,
          url: 'https://via.placeholder.com/400x300?text=分组讨论',
          category: 'discussion',
          layout: 'normal',
          likeCount: 28,
          isLiked: false,
          time: '2025-01-18 14:00',
          description: '分组讨论'
        },
        {
          id: 4,
          url: 'https://via.placeholder.com/400x400?text=学员合影',
          category: 'group',
          layout: 'normal',
          likeCount: 56,
          isLiked: true,
          time: '2025-01-18 16:00',
          description: '学员合影'
        },
        {
          id: 5,
          url: 'https://via.placeholder.com/400x300?text=互动交流',
          category: 'lecture',
          layout: 'normal',
          likeCount: 32,
          isLiked: false,
          time: '2025-01-19 09:00',
          description: '互动交流'
        },
        {
          id: 6,
          url: 'https://via.placeholder.com/400x600?text=现场教学',
          category: 'activity',
          layout: 'tall',
          likeCount: 42,
          isLiked: false,
          time: '2025-01-19 14:00',
          description: '现场教学'
        },
        {
          id: 7,
          url: 'https://via.placeholder.com/400x300?text=茶歇时光',
          category: 'activity',
          layout: 'normal',
          likeCount: 25,
          isLiked: false,
          time: '2025-01-19 15:00',
          description: '茶歇时光'
        },
        {
          id: 8,
          url: 'https://via.placeholder.com/400x400?text=小组展示',
          category: 'discussion',
          layout: 'normal',
          likeCount: 35,
          isLiked: false,
          time: '2025-01-20 09:00',
          description: '小组展示'
        },
        {
          id: 9,
          url: 'https://via.placeholder.com/400x300?text=结业仪式',
          category: 'activity',
          layout: 'large',
          likeCount: 68,
          isLiked: true,
          time: '2025-01-20 16:00',
          description: '结业仪式'
        }
      ]
    }
  },

  computed: {
    filteredPhotos() {
      if (this.activeCategory === 'all') {
        return this.photos
      }
      return this.photos.filter(photo => photo.category === this.activeCategory)
    }
  },

  methods: {
    switchCategory(category) {
      this.activeCategory = category
    },

    filterPhotos() {
      const items = this.categories.map(cat => cat.label)
      uni.showActionSheet({
        itemList: items,
        success: (res) => {
          if (res.tapIndex > 0) {
            this.activeCategory = this.categories[res.tapIndex].value
          } else {
            this.activeCategory = 'all'
          }
        }
      })
    },

    likePhoto(photo) {
      photo.isLiked = !photo.isLiked
      photo.likeCount += photo.isLiked ? 1 : -1
    },

    previewPhoto(index) {
      const urls = this.filteredPhotos.map(photo => photo.url)
      uni.previewImage({
        current: index,
        urls: urls
      })
    },

    uploadPhoto() {
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
              sizeType: ['compressed'],
              sourceType: ['album'],
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
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  transition: all 0.3s;
}

.tab-item.active {
  background: rgba(102, 126, 234, 0.1);
}

.tab-icon {
  font-size: 32rpx;
}

.tab-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  white-space: nowrap;
}

.tab-item.active .tab-label {
  color: $primary-color;
  font-weight: 500;
}

.photos-scroll {
  height: calc(100vh - 180rpx);
  padding: $spacing-sm;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
}

.photo-item {
  position: relative;
  border-radius: $border-radius-md;
  overflow: hidden;
  background: $bg-tertiary;
}

.photo-item.layout-normal {
  grid-column: span 1;
  grid-row: span 1;
  aspect-ratio: 1;
}

.photo-item.layout-large {
  grid-column: span 2;
  grid-row: span 2;
  aspect-ratio: 4/3;
}

.photo-item.layout-tall {
  grid-column: span 1;
  grid-row: span 2;
  aspect-ratio: 3/4;
}

.photo-image {
  width: 100%;
  height: 100%;
}

.photo-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-sm;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
}

.photo-likes {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.like-icon {
  font-size: $font-size-md;
}

.like-icon.liked {
  animation: likeAnim 0.3s ease;
}

@keyframes likeAnim {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

.like-count {
  font-size: $font-size-sm;
  color: $text-white;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-lg;
  color: $text-tertiary;
  font-size: $font-size-sm;
}

.fab {
  position: fixed;
  right: $spacing-lg;
  bottom: calc(#{$spacing-lg} + constant(safe-area-inset-bottom));
  bottom: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
  width: 112rpx;
  height: 112rpx;
  background: $primary-gradient;
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.fab-icon {
  font-size: 48rpx;
}

.fab:active {
  transform: scale(0.95);
}
</style>
