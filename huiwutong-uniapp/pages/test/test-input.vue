<template>
  <view class="test-container">
    <view class="test-section">
      <text class="test-title">输入测试页面</text>

      <!-- Test 1: 最基础的input -->
      <view class="test-item">
        <text class="test-label">测试1: 基础input</text>
        <input v-model="test1" placeholder="最基本的input" class="test-input" />
        <text class="test-value">值: {{ test1 }}</text>
      </view>

      <!-- Test 2: 带type的input -->
      <view class="test-item">
        <text class="test-label">测试2: type="text"</text>
        <input v-model="test2" type="text" placeholder="带type的input" class="test-input" />
        <text class="test-value">值: {{ test2 }}</text>
      </view>

      <!-- Test 3: 带type="tel" -->
      <view class="test-item">
        <text class="test-label">测试3: type="tel"</text>
        <input v-model="test3" type="tel" placeholder="tel类型" class="test-input" />
        <text class="test-value">值: {{ test3 }}</text>
      </view>

      <!-- Test 4: 带type="password" -->
      <view class="test-item">
        <text class="test-label">测试4: type="password"</text>
        <input v-model="test4" type="password" placeholder="password类型" class="test-input" />
        <text class="test-value">值: {{ test4 }}</text>
      </view>

      <!-- Test 5: 带图标的input（类似登录页） -->
      <view class="test-item">
        <text class="test-label">测试5: 带图标的input</text>
        <view class="input-wrapper">
          <text class="input-icon"><text class="fa fa-user"></text></text>
          <input v-model="test5" type="text" placeholder="带图标" class="input-with-icon" />
        </view>
        <text class="test-value">值: {{ test5 }}</text>
      </view>

      <!-- Test 6: 原生HTML input -->
      <view class="test-item">
        <text class="test-label">测试6: 原生HTML (条件编译)</text>
        <!-- #ifdef H5 -->
        <input v-model="test6" type="text" placeholder="原生H5 input" class="test-input" />
        <!-- #endif -->
        <!-- #ifndef H5 -->
        <input v-model="test6" placeholder="非H5 input" class="test-input" />
        <!-- #endif -->
        <text class="test-value">值: {{ test6 }}</text>
      </view>

      <!-- Test 7: textarea -->
      <view class="test-item">
        <text class="test-label">测试7: textarea</text>
        <textarea v-model="test7" placeholder="textarea测试" class="test-textarea"></textarea>
        <text class="test-value">值: {{ test7 }}</text>
      </view>

      <view class="test-info">
        <text class="info-text">平台: {{ platform }}</text>
        <text class="info-text">点击每个输入框测试是否能获取焦点</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      test1: '',
      test2: '',
      test3: '',
      test4: '',
      test5: '',
      test6: '',
      test7: '',
      platform: ''
    }
  },

  onLoad() {
    this.platform = uni.getSystemInfoSync().platform
    console.log('当前平台:', this.platform)
  }
}
</script>

<style lang="scss" scoped>
.test-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40rpx;
}

.test-section {
  background: white;
  border-radius: 20rpx;
  padding: 40rpx;
}

.test-title {
  font-size: 48rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 60rpx;
  display: block;
}

.test-item {
  margin-bottom: 60rpx;
  padding-bottom: 40rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.test-label {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.test-input {
  width: 100%;
  padding: 24rpx;
  border: 2rpx solid #ddd;
  border-radius: 12rpx;
  font-size: 32rpx;
  background: white;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 24rpx;
  font-size: 36rpx;
  z-index: 1;
  pointer-events: none;
}

.input-with-icon {
  width: 100%;
  padding: 24rpx;
  padding-left: 88rpx;
  border: 2rpx solid #ddd;
  border-radius: 12rpx;
  font-size: 32rpx;
  background: white;
}

.test-textarea {
  width: 100%;
  padding: 24rpx;
  border: 2rpx solid #ddd;
  border-radius: 12rpx;
  font-size: 32rpx;
  min-height: 120rpx;
  background: white;
}

.test-value {
  font-size: 28rpx;
  color: #666;
  margin-top: 16rpx;
  display: block;
}

.test-info {
  margin-top: 40rpx;
  padding: 24rpx;
  background: #f0f7ff;
  border-radius: 12rpx;
}

.info-text {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 8rpx;
}
</style>
