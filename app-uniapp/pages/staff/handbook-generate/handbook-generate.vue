<template>
  <view class="container">
    <view class="header-bar">
      <button class="btn-back" @click="goBack">‹</button>
      <view class="header-title">{{ meetingName }}</view>
    </view>

    <view class="section-card">
      <view class="section-header">
        <view class="icon">👥</view>
        <view>
          <view class="title">分组设置</view>
          <view class="desc">{{ groupCount }}个小组</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">📊</view>
        <view class="info-text">
          <view class="label">分组数量</view>
          <view class="value">{{ groupCount }}个小组</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">👔</view>
        <view class="info-text">
          <view class="label">已指定组长</view>
          <view class="value">6人已确定</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">👨‍💼</view>
        <view class="info-text">
          <view class="label">工作人员</view>
          <view class="value">12人已分配</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">✅</view>
        <view class="info-text">
          <view class="label">自动生成群组</view>
          <view class="value status-ok">已生成</view>
        </view>
      </view>
      <view class="action-buttons">
        <button class="btn-secondary" @click="dragSort">🔄 拖动排序</button>
        <button class="btn-primary" @click="settingGroup">⚙ 设置分组</button>
      </view>
    </view>

    <view class="section-card">
      <view class="section-header">
        <view class="icon">📋</view>
        <view>
          <view class="title">排序方式</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">🏢</view>
        <view class="info-text">
          <view class="label">按单位排序</view>
          <view class="value status-ok">已选择</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">↕</view>
        <view class="info-text">
          <view class="label">拖动排序</view>
          <view class="value">手动调整顺序</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">🔢</view>
        <view class="info-text">
          <view class="label">序号排序</view>
          <view class="value">按指定序号排列</view>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-header">
        <view class="icon">📄</view>
        <view>
          <view class="title">模板选择</view>
        </view>
      </view>
      <view class="info-item" @click="selectTemplate('standard')">
        <view class="info-icon">📘</view>
        <view class="info-text">
          <view class="label">标准模板</view>
          <view class="value status-ok">当前使用</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">📗</view>
        <view class="info-text">
          <view class="label">详细模板</view>
          <view class="value">包含更多详细信息</view>
        </view>
      </view>
      <view class="info-item">
        <view class="info-icon">🎨</view>
        <view class="info-text">
          <view class="label">自定义模板</view>
          <view class="value">上传自定义封面</view>
        </view>
      </view>
    </view>

    <view class="section-card">
      <view class="section-header">
        <view class="icon">✏</view>
        <view>
          <view class="title">内容设置</view>
        </view>
      </view>
      <view class="content-item clickable" @click="editCover">
        <view class="content-icon">🖼</view>
        <view class="content-text">
          <view class="label">封面图片</view>
          <view class="desc">已上传封面.jpg · 点击更换</view>
        </view>
        <view class="arrow">›</view>
      </view>
      <view class="content-item clickable" @click="editGuide">
        <view class="content-icon">📖</view>
        <view class="content-text">
          <view class="label">培训须知</view>
          <view class="desc">已编辑 · 共325字 · 点击修改</view>
        </view>
        <view class="arrow">›</view>
      </view>
      <view class="content-item clickable" @click="editSchedule">
        <view class="content-icon">📅</view>
        <view class="content-text">
          <view class="label">会议日程</view>
          <view class="desc">已同步 · 共3天日程 · 可编辑修改</view>
        </view>
        <view class="arrow">›</view>
      </view>
      <view class="content-item clickable" @click="editRoster">
        <view class="content-icon">👥</view>
        <view class="content-text">
          <view class="label">学员名册</view>
          <view class="desc">142人已审核通过 · 可调整排序</view>
        </view>
        <view class="arrow">›</view>
      </view>
      <view class="action-buttons">
        <button class="btn-secondary" @click="saveTemplate">💾 保存模板</button>
        <button class="btn-primary" @click="preview">👁 预览</button>
      </view>
    </view>

    <view class="section-card">
      <view class="section-header">
        <view class="icon">👁</view>
        <view>
          <view class="title">名册预览</view>
        </view>
      </view>
      <view class="preview-buttons">
        <button class="btn-preview" @click="previewCover">🖼 封面</button>
        <button class="btn-preview" @click="previewHandbook">📖 手册</button>
        <button class="btn-preview" @click="previewRoster">📋 名册</button>
      </view>
    </view>

    <view class="section-card card-gradient">
      <button class="btn-generate" @click="generatePDF" :disabled="isGenerating">
        <text v-if="!isGenerating">📄 生成名册PDF</text>
        <text v-else>⏳ 生成中...</text>
      </button>
      <button class="btn-generate-word" @click="generateWord" :disabled="isGenerating">
        <text v-if="!isGenerating">📘 生成名册Word</text>
        <text v-else>⏳ 生成中...</text>
      </button>
    </view>

    <view class="page-spacing"></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const meetingName = ref('2025博务干部培训班');
const groupCount = ref(6);
const isGenerating = ref(false);

onMounted(() => {
  uni.setNavigationBarTitle({ title: '名册生成' });
  loadHandbookData();
});

const loadHandbookData = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8084/api/handbook/config',
      method: 'GET'
    });
    if (res && res.data && res.data.code === 200) {
      const data = res.data.data;
      meetingName.value = data.meetingName || '2025博务干部培训班';
      groupCount.value = data.groupCount || 6;
    }
  } catch (error) {
    console.log('使用默认数据');
  }
};

const goBack = () => {
  uni.navigateBack();
};

const dragSort = () => {
  uni.showToast({ title: '拖拽分组功能', icon: 'none' });
};

const settingGroup = () => {
  uni.showToast({ title: '打开分组设置', icon: 'none' });
};

const selectTemplate = (template) => {
  uni.showToast({ title: '已选择' + template + '模板', icon: 'success' });
};

const editCover = () => {
  uni.showToast({ title: '编辑封面图片', icon: 'none' });
};

const editGuide = () => {
  uni.showToast({ title: '编辑培训须知', icon: 'none' });
};

const editSchedule = () => {
  uni.showToast({ title: '编辑会议日程', icon: 'none' });
};

const editRoster = () => {
  uni.showToast({ title: '编辑学员名册', icon: 'none' });
};

const saveTemplate = () => {
  uni.showToast({ title: '保存模板成功', icon: 'success' });
};

const preview = () => {
  uni.showToast({ title: '打开预览', icon: 'none' });
};

const previewCover = () => {
  uni.showToast({ title: '封面预览', icon: 'none' });
};

const previewHandbook = () => {
  uni.showToast({ title: '手册预览', icon: 'none' });
};

const previewRoster = () => {
  uni.showToast({ title: '名册预览', icon: 'none' });
};

const generatePDF = async () => {
  isGenerating.value = true;
  try {
    await new Promise(r => setTimeout(r, 1500));
    uni.showToast({ title: 'PDF生成成功', icon: 'success' });
  } catch (error) {
    uni.showToast({ title: '生成失败', icon: 'none' });
  } finally {
    isGenerating.value = false;
  }
};

const generateWord = async () => {
  isGenerating.value = true;
  try {
    await new Promise(r => setTimeout(r, 1500));
    uni.showToast({ title: 'Word生成成功', icon: 'success' });
  } catch (error) {
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

.header-bar {
  display: flex;
  align-items: center;
  padding: 10rpx 10rpx;
  background: white;
  border-bottom: 1rpx solid #e2e8f0;
  gap: 10rpx;
}

.btn-back {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #f8fafc;
  border: none;
  font-size: 24rpx;
  color: #475569;
}

.header-title {
  flex: 1;
  font-size: 16rpx;
  font-weight: 600;
  color: #1e293b;
}

.section-card {
  background: white;
  border-radius: 12rpx;
  margin: 12rpx 16rpx;
  overflow: hidden;
  box-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 16rpx;
  border-bottom: 1rpx solid #e2e8f0;
}

.icon {
  font-size: 24rpx;
  flex-shrink: 0;
}

.title {
  font-weight: 700;
  font-size: 16rpx;
  color: #1e293b;
}

.desc {
  font-size: 12rpx;
  color: #64748b;
  margin-top: 2rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.info-item:last-child {
  border-bottom: none;
}

.info-icon {
  font-size: 20rpx;
  flex-shrink: 0;
}

.info-text {
  flex: 1;
}

.label {
  font-size: 14rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 2rpx;
}

.value {
  font-size: 12rpx;
  color: #64748b;
}

.status-ok {
  color: #16a34a;
  font-weight: 600;
}

.content-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 16rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.content-item:last-child {
  border-bottom: none;
}

.content-item.clickable {
  cursor: pointer;
  transition: background 0.2s ease;
}

.content-item.clickable:active {
  background: #f8fafc;
}

.content-icon {
  font-size: 20rpx;
  flex-shrink: 0;
}

.content-text {
  flex: 1;
}

.desc {
  font-size: 12rpx;
  color: #64748b;
  margin-top: 2rpx;
}

.arrow {
  color: #cbd5e1;
  font-size: 16rpx;
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10rpx;
  padding: 12rpx 16rpx;
  border-top: 1rpx solid #f1f5f9;
}

.preview-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8rpx;
  padding: 12rpx 16rpx;
  border-top: 1rpx solid #f1f5f9;
}

.btn-primary {
  padding: 10rpx 12rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border: none;
  border-radius: 6rpx;
  font-weight: 600;
  font-size: 12rpx;
  color: white;
}

.btn-primary:active {
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
}

.btn-secondary {
  padding: 10rpx 12rpx;
  background: white;
  border: 2rpx solid #e2e8f0;
  border-radius: 6rpx;
  font-weight: 600;
  font-size: 12rpx;
  color: #475569;
}

.btn-secondary:active {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

.btn-preview {
  padding: 8rpx 10rpx;
  background: white;
  border: 1rpx solid #e2e8f0;
  border-radius: 6rpx;
  font-weight: 600;
  font-size: 11rpx;
  color: #475569;
}

.btn-preview:active {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #eff6ff;
}

.card-gradient {
  background: linear-gradient(135deg, #f5f3ff 0%, #f8f5ff 100%);
}

.btn-generate {
  width: 100%;
  padding: 16rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  color: white;
  border: none;
  border-radius: 8rpx;
  font-weight: 600;
  font-size: 14rpx;
  margin: 12rpx 16rpx 0;
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

.btn-generate-word {
  margin-top: 8rpx;
  margin-bottom: 16rpx;
}

.page-spacing {
  height: 20rpx;
}
</style>