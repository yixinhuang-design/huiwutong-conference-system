<template>
  <view class="container">
    <view class="progress-bar" :style="{ width: progressWidth }"></view>

    <view class="step-indicator">
      <view :class="['step-item', currentStep >= 1 ? 'step-item--active' : '']">1. 选择</view>
      <view :class="['step-item', currentStep >= 2 ? 'step-item--active' : '']">2. 编辑</view>
      <view :class="['step-item', currentStep >= 3 ? 'step-item--active' : '']">3. 预览</view>
      <view :class="['step-item', currentStep >= 4 ? 'step-item--active' : '']">4. 生成</view>
    </view>

    <view v-if="currentStep === 1" class="step-content">
      <view class="section-title">选择手册模板</view>
      <view class="template-grid">
        <view 
          v-for="tpl in templates" 
          :key="tpl.id"
          class="template-card"
          :class="{ 'template-card--selected': selectedTemplate === tpl.id }"
          @click="selectedTemplate = tpl.id"
        >
          <view class="template-icon">{{ tpl.icon }}</view>
          <view class="template-name">{{ tpl.name }}</view>
        </view>
      </view>
      <button class="btn-primary" @click="currentStep++">下一步</button>
    </view>

    <view v-if="currentStep === 2" class="step-content">
      <view class="section-title">编辑内容</view>
      <view class="content-list">
        <view class="content-item" @click="showToast('编辑封面')">
          <view class="icon">🖼</view>
          <view class="info">
            <view class="label">封面图片</view>
            <view class="desc">已上传</view>
          </view>
        </view>
        <view class="content-item" @click="showToast('编辑须知')">
          <view class="icon">📖</view>
          <view class="info">
            <view class="label">参会须知</view>
            <view class="desc">已编辑</view>
          </view>
        </view>
        <view class="content-item" @click="showToast('编辑日程')">
          <view class="icon">📅</view>
          <view class="info">
            <view class="label">会议日程</view>
            <view class="desc">已同步</view>
          </view>
        </view>
        <view class="content-item" @click="showToast('编辑名册')">
          <view class="icon">👥</view>
          <view class="info">
            <view class="label">学员名册</view>
            <view class="desc">142人</view>
          </view>
        </view>
      </view>
      <view class="button-group">
        <button class="btn-secondary" @click="currentStep--">上一步</button>
        <button class="btn-primary" @click="currentStep++">下一步</button>
      </view>
    </view>

    <view v-if="currentStep === 3" class="step-content">
      <view class="section-title">预览效果</view>
      <view class="preview-buttons">
        <button class="btn-preview" @click="showToast('封面预览')">📸 封面</button>
        <button class="btn-preview" @click="showToast('手册预览')">📖 手册</button>
      </view>
      <view class="info-box">
        <text>ℹ 预览效果可能与最终生成有差异</text>
      </view>
      <view class="button-group">
        <button class="btn-secondary" @click="currentStep--">上一步</button>
        <button class="btn-primary" @click="currentStep++">下一步</button>
      </view>
    </view>

    <view v-if="currentStep === 4" class="step-content">
      <view class="section-title">生成设置</view>
      <view class="format-options">
        <view 
          v-for="fmt in formats"
          :key="fmt.id"
          class="format-radio"
          :class="{ 'format-radio--checked': selectedFormat === fmt.id }"
          @click="selectedFormat = fmt.id"
        >
          <view class="radio-circle"></view>
          <view class="radio-label">{{ fmt.name }}</view>
        </view>
      </view>
      <button class="btn-generate" @click="generate" :disabled="isGenerating">
        <text v-if="!isGenerating">🚀 生成名册</text>
        <text v-else>⏳ 生成中...</text>
      </button>
      <button class="btn-secondary" @click="currentStep--" :disabled="isGenerating">上一步</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';

const currentStep = ref(1);
const selectedTemplate = ref('standard');
const selectedFormat = ref('pdf');
const isGenerating = ref(false);

const templates = [
  { id: 'standard', name: '标准模板', icon: '📄' },
  { id: 'detailed', name: '详细模板', icon: '📋' },
  { id: 'custom', name: '自定义模板', icon: '🎨' }
];

const formats = [
  { id: 'pdf', name: 'PDF (推荐)' },
  { id: 'docx', name: 'Word' },
  { id: 'both', name: '两种格式' }
];

const progressWidth = computed(() => {
  return (currentStep.value / 4) * 100 + '%';
});

const showToast = (msg) => {
  uni.showToast({ title: msg, icon: 'none' });
};

const generate = async () => {
  isGenerating.value = true;
  try {
    await new Promise(r => setTimeout(r, 1500));
    uni.showToast({ title: '生成成功', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1000);
  } catch (e) {
    uni.showToast({ title: '生成失败', icon: 'none' });
  } finally {
    isGenerating.value = false;
  }
};
</script>

<style scoped lang="scss">
.container {
  width: 100%;
  padding-bottom: 20rpx;
  background: #f5f7fb;
}

.progress-bar {
  background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%);
  height: 4rpx;
  margin-bottom: 16rpx;
  transition: width 0.3s ease;
}

.step-indicator {
  display: flex;
  justify-content: space-between;
  gap: 8rpx;
  padding: 0 20rpx;
  margin-bottom: 20rpx;
  font-size: 12rpx;
  color: #64748b;
}

.step-item {
  flex: 1;
  text-align: center;
}

.step-item--active {
  color: #3b82f6;
  font-weight: bold;
}

.step-content {
  padding: 0 20rpx;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-title {
  font-size: 14rpx;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16rpx;
}

.template-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.template-card {
  border: 2rpx solid #e2e8f0;
  border-radius: 8rpx;
  padding: 16rpx;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.template-card:active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.template-card--selected {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 0 0 3rpx rgba(59, 130, 246, 0.1);
}

.template-icon {
  font-size: 32rpx;
  margin-bottom: 8rpx;
}

.template-name {
  font-size: 13rpx;
  font-weight: 600;
  color: #1e293b;
}

.content-list {
  background: white;
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
}

.content-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  border-bottom: 1rpx solid #f1f5f9;
  cursor: pointer;
  transition: background 0.2s ease;
}

.content-item:last-child {
  border-bottom: none;
}

.content-item:active {
  background: #f8fafc;
}

.icon {
  font-size: 20rpx;
  flex-shrink: 0;
}

.info {
  flex: 1;
}

.label {
  font-size: 14rpx;
  font-weight: 600;
  color: #1e293b;
}

.desc {
  font-size: 12rpx;
  color: #64748b;
  margin-top: 2rpx;
}

.preview-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.btn-preview {
  padding: 12rpx;
  background: white;
  border: 2rpx solid #e2e8f0;
  border-radius: 8rpx;
  font-size: 14rpx;
  font-weight: 600;
  color: #475569;
}

.btn-preview:active {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

.info-box {
  background: #eff6ff;
  border-left: 4rpx solid #3b82f6;
  padding: 12rpx 16rpx;
  border-radius: 6rpx;
  font-size: 12rpx;
  color: #1e40af;
  margin-bottom: 16rpx;
}

.format-options {
  background: white;
  border-radius: 8rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
}

.format-radio {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx;
  cursor: pointer;
  border-radius: 6rpx;
  transition: background 0.2s ease;
}

.format-radio:active {
  background: #f8fafc;
}

.radio-circle {
  width: 20rpx;
  height: 20rpx;
  border: 2rpx solid #cbd5e1;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.format-radio--checked .radio-circle {
  border-color: #3b82f6;
  background: #3b82f6;
  box-shadow: inset 0 0 0 4rpx white;
}

.radio-label {
  font-size: 14rpx;
  color: #475569;
}

.button-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.btn-primary {
  padding: 12rpx 16rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  border-radius: 8rpx;
  font-weight: 600;
  font-size: 14rpx;
  color: white;
}

.btn-primary:active {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
}

.btn-secondary {
  padding: 12rpx 16rpx;
  background: white;
  border: 2rpx solid #e2e8f0;
  border-radius: 8rpx;
  font-weight: 600;
  font-size: 14rpx;
  color: #475569;
}

.btn-secondary:active {
  border-color: #3b82f6;
  background: #eff6ff;
  color: #3b82f6;
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-generate {
  width: 100%;
  padding: 16rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  border-radius: 12rpx;
  font-weight: 600;
  font-size: 16rpx;
  color: white;
  margin-bottom: 10rpx;
  cursor: pointer;
}

.btn-generate:active:not(:disabled) {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 16rpx rgba(59, 130, 246, 0.4);
}

.btn-generate:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>