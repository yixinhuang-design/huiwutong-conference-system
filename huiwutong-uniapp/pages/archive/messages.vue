<template>
  <view class="messages-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">留言赠言</text>
        <text class="header-action" @click="writeMessage"><text class="fa fa-pen"></text> 写留言</text>
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

    <scroll-view class="messages-scroll" scroll-y @scrolltolower="loadMore">
      <!-- 统计信息 -->
      <view class="stats-bar">
        <text class="stats-text">共 {{ messages.length }} 条留言</text>
      </view>

      <!-- 留言列表 -->
      <view class="messages-list">
        <view
          v-for="message in messages"
          :key="message.id"
          class="message-card card"
        >
          <view class="message-header">
            <view class="author-info">
              <view class="author-avatar">
                {{ message.authorName.charAt(message.authorName.length - 1) }}
              </view>
              <view class="author-details">
                <text class="author-name">{{ message.authorName }}</text>
                <text class="author-dept">{{ message.authorDept }}</text>
              </view>
            </view>
            <text class="message-time">{{ message.submitTime }}</text>
          </view>

          <view class="message-content">
            <text class="message-text">{{ message.content }}</text>
          </view>

          <view class="message-images" v-if="message.images && message.images.length > 0">
            <view
              v-for="(img, index) in message.images"
              :key="index"
              class="message-image"
              @click="previewImage(message.images, index)"
            >
              <image class="image" :src="img" mode="aspectFill"></image>
            </view>
          </view>

          <view class="message-footer">
            <view class="message-actions">
              <view class="action-item" @click="likeMessage(message)">
                <text class="action-icon" :class="{ liked: message.isLiked }">
                  {{ message.isLiked ? '❤️' : '🤍' }}
                </text>
                <text class="action-text">{{ message.likeCount }}</text>
              </view>
              <view class="action-item" @click="shareMessage(message)">
                <text class="action-icon">🔗</text>
                <text class="action-text">分享</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-text">
        <text>加载中...</text>
      </view>

      <view v-if="!hasMore && messages.length > 0" class="no-more-text">
        <text>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 写留言弹窗 -->
    <view v-if="showWriteModal" class="modal-overlay" @click="hideWriteModal">
      <view class="write-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">写留言</text>
          <text class="modal-close" @click="hideWriteModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="form-item">
            <textarea
              v-model="newMessage.content"
              class="message-input"
              placeholder="分享你的培训心得、祝福或建议..."
              :maxlength="500"
            ></textarea>
            <view class="input-count">
              <text>{{ newMessage.content.length }}/500</text>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">添加图片（可选）</text>
            <view class="image-upload">
              <view
                v-for="(img, index) in newMessage.images"
                :key="index"
                class="upload-item"
              >
                <image class="upload-image" :src="img" mode="aspectFill"></image>
                <text class="upload-remove" @click="removeImage(index)">✕</text>
              </view>
              <view v-if="newMessage.images.length < 3" class="upload-btn" @click="chooseImage">
                <text class="upload-icon">+</text>
                <text class="upload-text">添加图片</text>
              </view>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn btn-secondary btn-block" @click="hideWriteModal">取消</button>
          <button class="btn btn-primary btn-block" @click="submitMessage">发布</button>
        </view>
      </view>
    </view>
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
      showWriteModal: false,
      newMessage: {
        content: '',
        images: []
      },
      trainings: [
        '2024年度第四期干部综合素质提升培训',
        '2024年度第三期干部综合素质提升培训',
        '2024年度第二期干部综合素质提升培训'
      ],
      messages: [
        {
          id: 1,
          authorName: '张伟',
          authorDept: '市教育局',
          content: '非常感谢组织和各位老师的辛勤付出！这次培训让我收获满满，不仅增长了知识，还结识了许多优秀的同事。祝愿大家工作顺利，万事如意！',
          submitTime: '2024-12-12 18:30',
          likeCount: 48,
          isLiked: true,
          images: []
        },
        {
          id: 2,
          authorName: '李娜',
          authorDept: '市卫健委',
          content: '三天的培训充实而短暂，感谢班主任和生活委员的周到安排。培训内容丰富实用，讲师经验丰富，互动环节设计巧妙。希望今后能多举办这样的培训活动！',
          submitTime: '2024-12-12 19:15',
          likeCount: 35,
          isLiked: false,
          images: [
            'https://via.placeholder.com/300x200?text=培训合影'
          ]
        },
        {
          id: 3,
          authorName: '王强',
          authorDept: '市委组织部',
          content: '通过这次培训，我对工作有了新的认识和思考，回到岗位后一定学以致用，把培训所学运用到实际工作中去。感谢组织的培养！',
          submitTime: '2024-12-12 20:00',
          likeCount: 28,
          isLiked: false,
          images: []
        },
        {
          id: 4,
          authorName: '刘芳',
          authorDept: '市财政局',
          content: '培训环境设施一流，学习资料准备充分，餐饮住宿安排周到。特别感谢各位老师的精心指导，让我受益匪浅。期待下次再相聚！',
          submitTime: '2024-12-12 21:30',
          likeCount: 32,
          isLiked: true,
          images: []
        }
      ]
    }
  },

  methods: {
    onTrainingChange(e) {
      this.selectedTraining = this.trainings[e.detail.value]
      this.loadMessages()
    },

    likeMessage(message) {
      message.isLiked = !message.isLiked
      message.likeCount += message.isLiked ? 1 : -1
    },

    shareMessage(message) {
      uni.showActionSheet({
        itemList: ['分享给好友', '生成海报'],
        success: (res) => {
          uni.showToast({
            title: '分享功能开发中',
            icon: 'none'
          })
        }
      })
    },

    previewImage(images, index) {
      uni.previewImage({
        current: index,
        urls: images
      })
    },

    writeMessage() {
      this.newMessage = {
        content: '',
        images: []
      }
      this.showWriteModal = true
    },

    hideWriteModal() {
      this.showWriteModal = false
    },

    chooseImage() {
      const maxCount = 3 - this.newMessage.images.length
      uni.chooseImage({
        count: maxCount,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.newMessage.images.push(...res.tempFilePaths)
        }
      })
    },

    removeImage(index) {
      this.newMessage.images.splice(index, 1)
    },

    submitMessage() {
      if (!this.newMessage.content.trim()) {
        uni.showToast({
          title: '请输入留言内容',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '发布中...' })

      setTimeout(() => {
        uni.hideLoading()

        const message = {
          id: Date.now(),
          authorName: '我',
          authorDept: '市教育局',
          content: this.newMessage.content,
          submitTime: '刚刚',
          likeCount: 0,
          isLiked: false,
          images: [...this.newMessage.images]
        }

        this.messages.unshift(message)

        uni.showToast({
          title: '发布成功',
          icon: 'success'
        })

        this.showWriteModal = false
      }, 1000)
    },

    loadMessages() {
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

.messages-container {
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

.stats-bar {
  display: flex;
  justify-content: center;
  padding: $spacing-md;
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
}

.stats-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.messages-scroll {
  height: calc(100vh - 200rpx);
  padding: $spacing-md;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.message-card {
  margin-bottom: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.author-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.author-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $primary-gradient;
  color: $text-white;
  font-size: $font-size-md;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.author-name {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.author-dept {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.message-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.message-content {
  margin-bottom: $spacing-md;
}

.message-text {
  font-size: $font-size-md;
  color: $text-primary;
  line-height: 1.8;
}

.message-images {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
  flex-wrap: wrap;
}

.message-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: $border-radius-md;
  overflow: hidden;
}

.message-image .image {
  width: 100%;
  height: 100%;
}

.message-footer {
  border-top: 1rpx solid $border-color;
  padding-top: $spacing-md;
}

.message-actions {
  display: flex;
  gap: $spacing-xl;
}

.action-item {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.action-icon {
  font-size: $font-size-md;
}

.action-icon.liked {
  animation: likeAnim 0.3s ease;
}

@keyframes likeAnim {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

.action-text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.loading-text,
.no-more-text {
  text-align: center;
  padding: $spacing-lg;
  color: $text-tertiary;
  font-size: $font-size-sm;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.write-modal {
  width: 100%;
  background: $bg-primary;
  border-radius: $border-radius-lg $border-radius-lg 0 0;
  padding: $spacing-lg;
  padding-bottom: calc(#{$spacing-lg} + constant(safe-area-inset-bottom));
  padding-bottom: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.modal-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.modal-close {
  font-size: $font-size-xl;
  color: $text-secondary;
  padding: $spacing-sm;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  margin-bottom: $spacing-lg;
}

.form-item {
  margin-bottom: $spacing-lg;
}

.form-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.message-input {
  width: 100%;
  min-height: 240rpx;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  line-height: 1.6;
}

.input-count {
  text-align: right;
  margin-top: $spacing-sm;
}

.input-count text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.image-upload {
  display: flex;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.upload-item {
  position: relative;
  width: 160rpx;
  height: 160rpx;
}

.upload-image {
  width: 100%;
  height: 100%;
  border-radius: $border-radius-md;
}

.upload-remove {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  color: $text-white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-lg;
}

.upload-btn {
  width: 160rpx;
  height: 160rpx;
  border: 2rpx dashed $border-color;
  border-radius: $border-radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
}

.upload-icon {
  font-size: 48rpx;
  color: $text-tertiary;
}

.upload-text {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.modal-footer {
  display: flex;
  gap: $spacing-md;
}
</style>
