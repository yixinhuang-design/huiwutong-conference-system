<template>
  <view class="past-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">往期档案</text>
      </view>
    </view>

    <!-- 筛选器 -->
    <view class="filter-bar card">
      <scroll-view class="filter-scroll" scroll-x>
        <view
          v-for="(filter, index) in filters"
          :key="index"
          class="filter-item"
          :class="{ active: selectedFilter === index }"
          @click="selectFilter(index)"
        >
          {{ filter.name }}
        </view>
      </scroll-view>
    </view>

    <!-- 档案列表 -->
    <view class="archive-list">
      <view v-if="archiveList.length > 0">
        <view
          v-for="(archive, index) in archiveList"
          :key="index"
          class="archive-item card"
          @click="goToDetail(archive)"
        >
          <view class="archive-header">
            <view class="archive-title">{{ archive.title }}</view>
            <view class="archive-date">{{ archive.date }}</view>
          </view>

          <view class="archive-meta">
            <text class="meta-item"><text class="fa fa-calendar-alt"></text> {{ archive.duration }}</text>
            <text class="meta-item"><text class="fa fa-map-marker-alt"></text> {{ archive.location }}</text>
          </view>

          <view class="archive-stats">
            <view class="stat-item">
              <text class="stat-label">参与人数</text>
              <text class="stat-value">{{ archive.participants }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">培训天数</text>
              <text class="stat-value">{{ archive.days }}天</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">满意度</text>
              <text class="stat-value">{{ archive.satisfaction }}%</text>
            </view>
          </view>

          <view class="archive-actions">
            <button class="action-btn btn btn-outline btn-sm" @click.stop="viewMaterials(archive)">
              <text class="fa fa-book-open"></text> 资料
            </button>
            <button class="action-btn btn btn-outline btn-sm" @click.stop="viewPhotos(archive)">
              📷 照片
            </button>
            <button class="action-btn btn btn-outline btn-sm" @click.stop="viewReport(archive)">
              📊 总结
            </button>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📁</text>
        <text class="empty-text">暂无往期档案</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedFilter: 0,
      filters: [
        { name: '全部', value: 'all' },
        { name: '2024年', value: '2024' },
        { name: '2023年', value: '2023' },
        { name: '2022年', value: '2022' }
      ],
      archiveList: [
        {
          id: 1,
          title: '2024年党务干部培训班',
          date: '2024.06.10-14',
          duration: '6月10日 - 6月14日',
          location: '市委党校',
          participants: 120,
          days: 5,
          satisfaction: 96,
          year: '2024'
        },
        {
          id: 2,
          title: '2024年青年干部能力提升班',
          date: '2024.03.15-19',
          duration: '3月15日 - 3月19日',
          location: '培训中心',
          participants: 85,
          days: 5,
          satisfaction: 94,
          year: '2024'
        },
        {
          id: 3,
          title: '2023年处级干部培训班',
          date: '2023.11.05-09',
          duration: '11月5日 - 11月9日',
          location: '市委党校',
          participants: 98,
          days: 5,
          satisfaction: 95,
          year: '2023'
        },
        {
          id: 4,
          title: '2023年基层党组织书记培训班',
          date: '2023.08.20-24',
          duration: '8月20日 - 8月24日',
          location: '培训中心',
          participants: 150,
          days: 5,
          satisfaction: 97,
          year: '2023'
        }
      ]
    }
  },

  methods: {
    /**
     * 选择筛选器
     */
    selectFilter(index) {
      this.selectedFilter = index
      this.filterArchives()
    },

    /**
     * 筛选档案
     */
    filterArchives() {
      const filter = this.filters[this.selectedFilter]
      if (filter.value === 'all') {
        this.loadArchives()
      } else {
        // 模拟筛选
        this.archiveList = this.archiveList.filter(item => item.year === filter.value)
      }
    },

    /**
     * 加载档案
     */
    async loadArchives() {
      // TODO: 调用API获取档案列表
    },

    /**
     * 查看详情
     */
    goToDetail(archive) {
      uni.navigateTo({
        url: `/pages/archive/detail?id=${archive.id}`
      })
    },

    /**
     * 查看资料
     */
    viewMaterials(archive) {
      uni.navigateTo({
        url: `/pages/archive/materials?id=${archive.id}`
      })
    },

    /**
     * 查看照片
     */
    viewPhotos(archive) {
      uni.navigateTo({
        url: `/pages/archive/photos?id=${archive.id}`
      })
    },

    /**
     * 查看总结
     */
    viewReport(archive) {
      uni.navigateTo({
        url: `/pages/archive/report?id=${archive.id}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.past-container {
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

.archive-list {
  padding: 0 $spacing-md;
}

.archive-item {
  margin-bottom: $spacing-md;
  transition: $transition-base;
}

.archive-item:active {
  transform: scale(0.98);
}

.archive-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-sm;
}

.archive-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
}

.archive-date {
  font-size: $font-size-sm;
  color: $text-tertiary;
  flex-shrink: 0;
}

.archive-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  margin-bottom: $spacing-md;
}

.meta-item {
  font-size: $font-size-sm;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.archive-stats {
  display: flex;
  justify-content: space-around;
  padding: $spacing-md 0;
  margin-bottom: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: $font-size-xs;
  color: $text-secondary;
  margin-bottom: 6rpx;
}

.stat-value {
  display: block;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $primary-color;
}

.archive-actions {
  display: flex;
  gap: $spacing-sm;
}

.action-btn {
  flex: 1;
  padding: $spacing-sm;
  font-size: $font-size-sm;
}
</style>
