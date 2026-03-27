<template>
  <view class="training-create-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">{{ isEdit ? '编辑培训' : '新建培训' }}</text>
        <text class="header-action" @click="saveDraft">保存草稿</text>
      </view>
    </view>

    <scroll-view class="form-scroll" scroll-y>
      <!-- 基本信息 -->
      <view class="form-section card">
        <view class="section-title">基本信息</view>
        <view class="form-item">
          <text class="form-label required">培训名称</text>
          <input
            v-model="formData.title"
            class="form-input"
            placeholder="请输入培训名称"
            :maxlength="100"
          />
        </view>
        <view class="form-item">
          <text class="form-label required">培训期次</text>
          <input
            v-model="formData.period"
            class="form-input"
            placeholder="例如：2025年度第一期"
          />
        </view>
        <view class="form-item">
          <text class="form-label required">培训时间</text>
          <view class="date-range-picker">
            <view class="date-input" @click="selectStartDate">
              <text class="date-text" v-if="formData.startDate">{{ formData.startDate }}</text>
              <text class="date-placeholder" v-else>开始日期</text>
            </view>
            <text class="date-separator">至</text>
            <view class="date-input" @click="selectEndDate">
              <text class="date-text" v-if="formData.endDate">{{ formData.endDate }}</text>
              <text class="date-placeholder" v-else>结束日期</text>
            </view>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label required">培训地点</text>
          <input
            v-model="formData.location"
            class="form-input"
            placeholder="请输入培训地点"
          />
        </view>
        <view class="form-item">
          <text class="form-label">培训描述</text>
          <textarea
            v-model="formData.description"
            class="form-textarea"
            placeholder="请输入培训描述（选填）"
            :maxlength="500"
          ></textarea>
        </view>
      </view>

      <!-- 培训规模 -->
      <view class="form-section card">
        <view class="section-title">培训规模</view>
        <view class="form-item">
          <text class="form-label required">计划人数</text>
          <input
            v-model="formData.plannedCount"
            class="form-input"
            type="number"
            placeholder="请输入计划人数"
          />
        </view>
        <view class="form-item">
          <text class="form-label">最大人数</text>
          <input
            v-model="formData.maxCount"
            class="form-input"
            type="number"
            placeholder="请输入最大人数（选填）"
          />
        </view>
        <view class="form-item">
          <text class="form-label">分组数量</text>
          <input
            v-model="formData.groupCount"
            class="form-input"
            type="number"
            placeholder="请输入分组数量（选填）"
          />
        </view>
      </view>

      <!-- 报名设置 -->
      <view class="form-section card">
        <view class="section-title">报名设置</view>
        <view class="form-item">
          <view class="switch-row">
            <text class="form-label">需要审核</text>
            <switch
              :checked="formData.needApproval"
              @change="toggleApproval"
              color="#667eea"
            />
          </view>
        </view>
        <view class="form-item" v-if="formData.needApproval">
          <text class="form-label">审核人</text>
          <input
            v-model="formData.approver"
            class="form-input"
            placeholder="请输入审核人"
          />
        </view>
        <view class="form-item">
          <view class="switch-row">
            <text class="form-label">报名截止</text>
            <switch
              :checked="formData.hasDeadline"
              @change="toggleDeadline"
              color="#667eea"
            />
          </view>
        </view>
        <view class="form-item" v-if="formData.hasDeadline">
          <view class="date-input" @click="selectDeadline">
            <text class="date-text" v-if="formData.deadline">{{ formData.deadline }}</text>
            <text class="date-placeholder" v-else>选择截止日期</text>
          </view>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="form-section card">
        <view class="section-title">通知设置</view>
        <view class="form-item">
          <view class="switch-row">
            <text class="form-label">自动发送通知</text>
            <switch
              :checked="formData.autoNotify"
              @change="toggleAutoNotify"
              color="#667eea"
            />
          </view>
        </view>
        <view class="form-item" v-if="formData.autoNotify">
          <text class="form-label">通知时间</text>
          <picker mode="time" :value="formData.notifyTime" @change="onNotifyTimeChange">
            <view class="picker-value">
              {{ formData.notifyTime || '请选择通知时间' }}
            </view>
          </picker>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button class="btn btn-outline btn-block" @click="preview">
          👁️ 预览
        </button>
        <button class="btn btn-primary btn-block" @click="submit">
          ✓ {{ isEdit ? '保存修改' : '创建培训' }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      trainingId: '',
      isEdit: false,
      formData: {
        title: '',
        period: '',
        startDate: '',
        endDate: '',
        location: '',
        description: '',
        plannedCount: '',
        maxCount: '',
        groupCount: '',
        needApproval: true,
        approver: '班主任',
        hasDeadline: true,
        deadline: '',
        autoNotify: true,
        notifyTime: '09:00'
      }
    }
  },

  onLoad(options) {
    if (options.id) {
      this.trainingId = options.id
      this.isEdit = true
      this.loadTraining()
    }
  },

  methods: {
    loadTraining() {
      uni.showLoading({ title: '加载中...' })
      setTimeout(() => {
        uni.hideLoading()
        // 这里应该从API加载培训详情
      }, 500)
    },

    selectStartDate() {
      uni.showModal({
        title: '选择开始日期',
        content: '日期选择器开发中',
        showCancel: false
      })
    },

    selectEndDate() {
      uni.showModal({
        title: '选择结束日期',
        content: '日期选择器开发中',
        showCancel: false
      })
    },

    selectDeadline() {
      uni.showModal({
        title: '选择截止日期',
        content: '日期选择器开发中',
        showCancel: false
      })
    },

    toggleApproval(e) {
      this.formData.needApproval = e.detail.value
    },

    toggleDeadline(e) {
      this.formData.hasDeadline = e.detail.value
    },

    toggleAutoNotify(e) {
      this.formData.autoNotify = e.detail.value
    },

    onNotifyTimeChange(e) {
      this.formData.notifyTime = e.detail.value
    },

    validateForm() {
      if (!this.formData.title.trim()) {
        uni.showToast({
          title: '请输入培训名称',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.period.trim()) {
        uni.showToast({
          title: '请输入培训期次',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.startDate || !this.formData.endDate) {
        uni.showToast({
          title: '请选择培训时间',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.location.trim()) {
        uni.showToast({
          title: '请输入培训地点',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.plannedCount) {
        uni.showToast({
          title: '请输入计划人数',
          icon: 'none'
        })
        return false
      }

      return true
    },

    preview() {
      if (!this.validateForm()) {
        return
      }

      uni.showToast({
        title: '预览功能开发中',
        icon: 'none'
      })
    },

    saveDraft() {
      uni.setStorageSync('training_draft', this.formData)
      uni.showToast({
        title: '草稿已保存',
        icon: 'success'
      })
    },

    submit() {
      if (!this.validateForm()) {
        return
      }

      uni.showModal({
        title: this.isEdit ? '保存修改' : '创建培训',
        content: `确认${this.isEdit ? '保存' : '创建'}此培训？`,
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '提交中...' })

            setTimeout(() => {
              uni.hideLoading()
              uni.removeStorageSync('training_draft')

              uni.showToast({
                title: this.isEdit ? '保存成功' : '创建成功',
                icon: 'success'
              })

              setTimeout(() => {
                uni.navigateBack()
              }, 1500)
            }, 1500)
          }
        }
      })
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

.training-create-container {
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

.form-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.form-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
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

.form-label.required::after {
  content: '*';
  color: #ef4444;
  margin-left: 4rpx;
}

.form-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
}

.form-textarea {
  width: 100%;
  min-height: 160rpx;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  line-height: 1.6;
}

.date-range-picker {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.date-input {
  flex: 1;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  text-align: center;
}

.date-text {
  font-size: $font-size-md;
  color: $text-primary;
}

.date-placeholder {
  font-size: $font-size-md;
  color: $text-tertiary;
}

.date-separator {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.picker-value {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  text-align: center;
  font-size: $font-size-md;
  color: $text-primary;
}

.submit-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}
</style>
