<template>
  <view class="notice-manage-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">通知管理</text>
        <text class="header-action" @click="createNotice">+ 新建</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section card">
      <view class="stat-card">
        <text class="stat-num">{{ stats.total }}</text>
        <text class="stat-label">全部通知</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ stats.published }}</text>
        <text class="stat-label">已发布</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ stats.draft }}</text>
        <text class="stat-label">草稿</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ stats.scheduled }}</text>
        <text class="stat-label">定时发送</text>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 通知列表 -->
    <scroll-view class="notice-scroll" scroll-y @scrolltolower="loadMore">
      <view v-if="filteredNotices.length === 0" class="empty-state">
        <text class="empty-icon">📢</text>
        <text class="empty-text">暂无通知</text>
      </view>

      <view
        v-for="notice in filteredNotices"
        :key="notice.id"
        class="notice-card card"
        @click="viewNotice(notice)"
      >
        <view class="notice-header">
          <view class="notice-title-row">
            <text class="notice-title">{{ notice.title }}</text>
            <view class="notice-status" :class="'status-' + notice.status">
              {{ statusMap[notice.status] }}
            </view>
          </view>
          <text class="notice-time">{{ notice.publishTime }}</text>
        </view>

        <view class="notice-content">
          <text class="notice-desc">{{ notice.summary }}</text>
        </view>

        <view class="notice-footer">
          <view class="notice-target">
            <text class="target-icon"><text class="fa fa-users"></text></text>
            <text class="target-text">{{ notice.target }}</text>
          </view>
          <view class="notice-stats">
            <text class="stat-item">✓ 已读 {{ notice.readCount }}</text>
            <text class="stat-item">👁️ 浏览 {{ notice.viewCount }}</text>
          </view>
        </view>

        <view class="notice-actions" v-if="notice.status !== 'published'" @click.stop>
          <button
            v-if="notice.status === 'draft'"
            class="btn btn-sm btn-primary"
            @click="publishNotice(notice)"
          >
            发布
          </button>
          <button
            v-if="notice.status === 'draft'"
            class="btn btn-sm btn-outline"
            @click="editNotice(notice)"
          >
            编辑
          </button>
          <button
            v-if="notice.status === 'scheduled'"
            class="btn btn-sm btn-outline"
            @click="cancelSchedule(notice)"
          >
            取消定时
          </button>
          <button
            class="btn btn-sm btn-text"
            @click="deleteNotice(notice)"
          >
            删除
          </button>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && filteredNotices.length > 0" class="no-more-text">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import notificationApi from '@/api/notification'

export default {
  data() {
    return {
      conferenceId: '',
      activeTab: 'all',
      loading: false,
      hasMore: true,
      currentPage: 1,
      stats: {
        total: 0,
        published: 0,
        draft: 0,
        scheduled: 0
      },
      tabs: [
        { label: '全部', value: 'all' },
        { label: '已发布', value: 'published' },
        { label: '草稿', value: 'draft' },
        { label: '定时发送', value: 'scheduled' }
      ],
      noticeList: [],
      statusMap: {
        published: '已发布',
        draft: '草稿',
        scheduled: '定时发送'
      }
    }
  },

  onLoad(options) {
    this.conferenceId = options.conferenceId || uni.getStorageSync('conferenceId') || '2030309010523144194'
    this.loadNoticeList()
    this.loadStats()
  },

  computed: {
    filteredNotices() {
      if (this.activeTab === 'all') {
        return this.noticeList
      }
      return this.noticeList.filter(notice => notice.status === this.activeTab)
    }
  },

  methods: {
    async loadNoticeList() {
      try {
        this.loading = true
        const result = await notificationApi.list({
          conferenceId: this.conferenceId,
          page: this.currentPage,
          pageSize: 20
        })
        const records = result.records || result || []
        if (this.currentPage === 1) {
          this.noticeList = records
        } else {
          this.noticeList = [...this.noticeList, ...records]
        }
        this.hasMore = records.length >= 20
      } catch (e) {
        console.error('加载通知列表失败', e)
      } finally {
        this.loading = false
      }
    },

    async loadStats() {
      try {
        const result = await notificationApi.getStats(this.conferenceId)
        if (result) {
          this.stats.total = result.total || 0
          this.stats.published = result.totalSent || 0
          this.stats.draft = result.statusDistribution?.draft || 0
          this.stats.scheduled = result.statusDistribution?.scheduled || 0
        }
      } catch (e) {
        console.error('加载统计失败', e)
      }
    },

    switchTab(tab) {
      this.activeTab = tab
    },

    createNotice() {
      uni.navigateTo({
        url: '/pages/staff/notice-edit'
      })
    },

    viewNotice(notice) {
      uni.navigateTo({
        url: `/pages/staff/notice-detail?id=${notice.id}`
      })
    },

    editNotice(notice) {
      uni.navigateTo({
        url: `/pages/staff/notice-edit?id=${notice.id}`
      })
    },

    publishNotice(notice) {
      uni.showModal({
        title: '发布通知',
        content: `确认发布通知《${notice.title}》？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await notificationApi.send({
                conferenceId: this.conferenceId,
                type: notice.type || 'custom',
                title: notice.title,
                content: notice.summary || notice.content,
                channels: ['system'],
                sendTiming: 'now'
              })
              notice.status = 'published'
              notice.publishTime = this.getCurrentTime()
              uni.showToast({ title: '发布成功', icon: 'success' })
              this.loadStats()
            } catch (e) {
              uni.showToast({ title: '发布失败', icon: 'none' })
            }
          }
        }
      })
    },

    cancelSchedule(notice) {
      uni.showModal({
        title: '取消定时发送',
        content: '确认取消此通知的定时发送？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await notificationApi.saveDraft({
                id: notice.id,
                status: 'draft',
                title: notice.title,
                content: notice.content
              })
              notice.status = 'draft'
              uni.showToast({ title: '已取消', icon: 'success' })
            } catch(e) {
              uni.showToast({ title: '取消失败', icon: 'none' })
            }
          }
        }
      })
    },

    deleteNotice(notice) {
      uni.showModal({
        title: '删除通知',
        content: '确认删除此通知？删除后不可恢复',
        confirmColor: '#ef4444',
        success: async (res) => {
          if (res.confirm) {
            try {
              await notificationApi.remove(notice.id)
              const index = this.noticeList.findIndex(n => n.id === notice.id)
              if (index > -1) {
                this.noticeList.splice(index, 1)
              }
              uni.showToast({ title: '删除成功', icon: 'success' })
              this.loadStats()
            } catch (e) {
              uni.showToast({ title: '删除失败', icon: 'none' })
            }
          }
        }
      })
    },

    async loadMore() {
      if (this.loading || !this.hasMore) return
      this.currentPage++
      await this.loadNoticeList()
    },

    getCurrentTime() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.notice-manage-container {
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

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.header-action {
  font-size: $font-size-md;
  font-weight: 600;
  color: $primary-color;
}

.stats-section {
  display: flex;
  justify-content: space-around;
  padding: $spacing-md;
  background: $bg-primary;
  margin-bottom: $spacing-sm;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
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

.filter-tabs {
  display: flex;
  background: $bg-primary;
  padding: $spacing-sm $spacing-md;
  margin-bottom: $spacing-sm;
}

.filter-tab {
  flex: 1;
  text-align: center;
  padding: $spacing-sm $spacing-md;
  font-size: $font-size-md;
  color: $text-secondary;
  border-radius: $border-radius-md;
  transition: all 0.3s;
}

.filter-tab.active {
  background: $primary-color;
  color: $text-white;
  font-weight: 500;
}

.notice-scroll {
  height: calc(100vh - 320rpx);
  padding: 0 $spacing-md;
}

.notice-card {
  margin-bottom: $spacing-md;
}

.notice-header {
  margin-bottom: $spacing-md;
}

.notice-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-xs;
}

.notice-title {
  flex: 1;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-right: $spacing-sm;
}

.notice-status {
  padding: 4rpx 12rpx;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  font-weight: 500;
  white-space: nowrap;
}

.notice-status.status-published {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.notice-status.status-draft {
  background: rgba(148, 163, 184, 0.1);
  color: $text-tertiary;
}

.notice-status.status-scheduled {
  background: rgba(102, 126, 234, 0.1);
  color: $primary-color;
}

.notice-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.notice-content {
  margin-bottom: $spacing-md;
}

.notice-desc {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color;
}

.notice-target {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.target-icon {
  font-size: $font-size-md;
}

.target-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.notice-stats {
  display: flex;
  gap: $spacing-md;
}

.stat-item {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.notice-actions {
  display: flex;
  gap: $spacing-sm;
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color;
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-size-sm;
}

.btn-text {
  background: transparent;
  border: none;
  color: $text-tertiary;
}

.btn-text:active {
  color: #ef4444;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: $spacing-md;
}

.empty-text {
  font-size: $font-size-md;
  color: $text-tertiary;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-md;
  color: $text-tertiary;
  font-size: $font-size-sm;
}
</style>
