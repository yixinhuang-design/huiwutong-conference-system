<template>
  <view class="registration-form-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">培训报名</text>
        <text class="header-action"></text>
      </view>
    </view>

    <!-- 进度提示 -->
    <view class="progress-bar">
      <view class="progress-fill" :style="{ width: ((currentStep + 1) / totalSteps) * 100 + '%' }"></view>
    </view>
    <view class="step-indicator">
      <text class="step-text">步骤 {{ currentStep + 1 }}/{{ totalSteps }}</text>
    </view>

    <scroll-view class="form-scroll" scroll-y>
      <!-- 步骤1：培训信息 -->
      <view v-if="currentStep === 0" class="step-content">
        <view class="info-card card">
          <view class="training-icon">📚</view>
          <text class="training-title">{{ trainingInfo.title }}</text>
          <view class="training-meta">
            <text class="meta-item">📅 {{ trainingInfo.date }}</text>
            <text class="meta-item">📍 {{ trainingInfo.location }}</text>
          </view>
        </view>

        <view class="notice-section card">
          <view class="section-title">📋 报名须知</view>
          <view class="notice-list">
            <text class="notice-item">1. 请确保填写信息真实准确</text>
            <text class="notice-item">2. 报名后请等待审核</text>
            <text class="notice-item">3. 审核通过后将收到通知</text>
            <text class="notice-item">4. 如有疑问请联系主办方</text>
          </view>
        </view>
      </view>

      <!-- 步骤2：个人信息 -->
      <view v-if="currentStep === 1" class="step-content">
        <view class="form-section card">
          <view class="section-title">基本信息</view>

          <view class="form-item required">
            <text class="form-label">姓名</text>
            <input
              v-model="formData.name"
              class="form-input"
              placeholder="请输入真实姓名"
            />
          </view>

          <view class="form-item required">
            <text class="form-label">性别</text>
            <radio-group @change="onGenderChange">
              <label class="radio-item">
                <radio value="male" :checked="formData.gender === 'male'" color="#667eea" />
                <text class="radio-label">男</text>
              </label>
              <label class="radio-item">
                <radio value="female" :checked="formData.gender === 'female'" color="#667eea" />
                <text class="radio-label">女</text>
              </label>
            </radio-group>
          </view>

          <view class="form-item required">
            <text class="form-label">身份证号</text>
            <input
              v-model="formData.idCard"
              class="form-input"
              type="idcard"
              placeholder="请输入身份证号"
              maxlength="18"
            />
          </view>

          <view class="form-item required">
            <text class="form-label">联系电话</text>
            <input
              v-model="formData.phone"
              class="form-input"
              type="number"
              placeholder="请输入手机号"
              maxlength="11"
            />
          </view>

          <view class="form-item">
            <text class="form-label">电子邮箱</text>
            <input
              v-model="formData.email"
              class="form-input"
              placeholder="请输入邮箱（选填）"
            />
          </view>
        </view>
      </view>

      <!-- 步骤3：工作信息 -->
      <view v-if="currentStep === 2" class="step-content">
        <view class="form-section card">
          <view class="section-title">工作信息</view>

          <view class="form-item required">
            <text class="form-label">工作单位</text>
            <input
              v-model="formData.department"
              class="form-input"
              placeholder="请输入工作单位全称"
            />
          </view>

          <view class="form-item required">
            <text class="form-label">单位性质</text>
            <picker mode="selector" :range="departmentTypes" @change="onDepartmentTypeChange">
              <view class="picker-input">
                {{ formData.departmentType || '请选择单位性质' }}
              </view>
            </picker>
          </view>

          <view class="form-item required">
            <text class="form-label">职务</text>
            <input
              v-model="formData.position"
              class="form-input"
              placeholder="请输入现任职务"
            />
          </view>

          <view class="form-item required">
            <text class="form-label">职级</text>
            <picker mode="selector" :range="rankLevels" @change="onRankChange">
              <view class="picker-input">
                {{ formData.rank || '请选择职级' }}
              </view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">参加工作时间</text>
            <picker mode="date" :value="formData.workStartDate" @change="onWorkStartDateChange">
              <view class="picker-input">
                {{ formData.workStartDate || '请选择时间' }}
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 步骤4：其他信息 -->
      <view v-if="currentStep === 3" class="step-content">
        <view class="form-section card">
          <view class="section-title">其他信息</view>

          <view class="form-item">
            <text class="form-label">学历</text>
            <picker mode="selector" :range="educationLevels" @change="onEducationChange">
              <view class="picker-input">
                {{ formData.education || '请选择学历' }}
              </view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">政治面貌</text>
            <picker mode="selector" :range="politicalStatus" @change="onPoliticalStatusChange">
              <view class="picker-input">
                {{ formData.political || '请选择政治面貌' }}
              </view>
            </picker>
          </view>

          <view class="form-item">
            <text class="form-label">健康状况</text>
            <radio-group @change="onHealthChange">
              <label class="radio-item">
                <radio value="good" :checked="formData.health === 'good'" color="#667eea" />
                <text class="radio-label">健康</text>
              </label>
              <label class="radio-item">
                <radio value="general" :checked="formData.health === 'general'" color="#667eea" />
                <text class="radio-label">一般</text>
              </label>
            </radio-group>
          </view>

          <view class="form-item">
            <text class="form-label">特殊饮食需求</text>
            <textarea
              v-model="formData.dietary"
              class="form-textarea"
              placeholder="如有特殊饮食需求请说明（选填）"
              :maxlength="200"
            ></textarea>
          </view>

          <view class="form-item">
            <text class="form-label">备注说明</text>
            <textarea
              v-model="formData.remark"
              class="form-textarea"
              placeholder="其他需要说明的内容（选填）"
              :maxlength="500"
            ></textarea>
          </view>
        </view>
      </view>

      <!-- 步骤5：确认信息 -->
      <view v-if="currentStep === 4" class="step-content">
        <view class="form-section card">
          <view class="section-title">确认报名信息</view>

          <view class="confirm-list">
            <view class="confirm-item">
              <text class="confirm-label">姓名</text>
              <text class="confirm-value">{{ formData.name }}</text>
            </view>
            <view class="confirm-item">
              <text class="confirm-label">性别</text>
              <text class="confirm-value">{{ formData.gender === 'male' ? '男' : '女' }}</text>
            </view>
            <view class="confirm-item">
              <text class="confirm-label">身份证号</text>
              <text class="confirm-value">{{ formData.idCard }}</text>
            </view>
            <view class="confirm-item">
              <text class="confirm-label">联系电话</text>
              <text class="confirm-value">{{ formData.phone }}</text>
            </view>
            <view class="confirm-item">
              <text class="confirm-label">工作单位</text>
              <text class="confirm-value">{{ formData.department }}</text>
            </view>
            <view class="confirm-item">
              <text class="confirm-label">职务</text>
              <text class="confirm-value">{{ formData.position }}</text>
            </view>
          </view>

          <view class="agreement-section">
            <view class="agreement-item" @click="toggleAgreement">
              <view class="agreement-checkbox" :class="{ checked: agreed }">
                <text v-if="agreed" class="checkbox-icon">✓</text>
              </view>
              <text class="agreement-text">我已阅读并同意《培训报名须知》</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作按钮 -->
    <view class="action-bar">
      <button
        v-if="currentStep > 0"
        class="btn btn-outline"
        @click="previousStep"
      >
        上一步
      </button>
      <button
        v-if="currentStep < totalSteps - 1"
        class="btn btn-primary"
        @click="nextStep"
      >
        下一步
      </button>
      <button
        v-if="currentStep === totalSteps - 1"
        class="btn btn-primary"
        @click="submitRegistration"
      >
        提交报名
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      trainingId: '',
      currentStep: 0,
      totalSteps: 5,
      agreed: false,
      trainingInfo: {
        title: '2025年度第一期干部综合素质提升培训',
        date: '2025年1月18日-20日',
        location: '市委党校报告厅'
      },
      departmentTypes: ['行政机关', '事业单位', '国有企业', '其他'],
      rankLevels: ['正处级', '副处级', '正科级', '副科级', '科员', '其他'],
      educationLevels: ['博士', '硕士', '本科', '大专', '高中及以下'],
      politicalStatus: ['中共党员', '共青团员', '民主党派', '群众'],
      formData: {
        name: '',
        gender: 'male',
        idCard: '',
        phone: '',
        email: '',
        department: '',
        departmentType: '',
        position: '',
        rank: '',
        workStartDate: '',
        education: '',
        political: '',
        health: 'good',
        dietary: '',
        remark: ''
      }
    }
  },

  onLoad(options) {
    if (options.trainingId) {
      this.trainingId = options.trainingId
    }
  },

  methods: {
    onGenderChange(e) {
      this.formData.gender = e.detail.value
    },

    onDepartmentTypeChange(e) {
      this.formData.departmentType = this.departmentTypes[e.detail.value]
    },

    onRankChange(e) {
      this.formData.rank = this.rankLevels[e.detail.value]
    },

    onWorkStartDateChange(e) {
      this.formData.workStartDate = e.detail.value
    },

    onEducationChange(e) {
      this.formData.education = this.educationLevels[e.detail.value]
    },

    onPoliticalStatusChange(e) {
      this.formData.political = this.politicalStatus[e.detail.value]
    },

    onHealthChange(e) {
      this.formData.health = e.detail.value
    },

    toggleAgreement() {
      this.agreed = !this.agreed
    },

    validateStep() {
      if (this.currentStep === 1) {
        if (!this.formData.name) {
          uni.showToast({ title: '请输入姓名', icon: 'none' })
          return false
        }
        if (!this.formData.idCard) {
          uni.showToast({ title: '请输入身份证号', icon: 'none' })
          return false
        }
        if (!this.formData.phone) {
          uni.showToast({ title: '请输入联系电话', icon: 'none' })
          return false
        }
      }

      if (this.currentStep === 2) {
        if (!this.formData.department) {
          uni.showToast({ title: '请输入工作单位', icon: 'none' })
          return false
        }
        if (!this.formData.position) {
          uni.showToast({ title: '请输入职务', icon: 'none' })
          return false
        }
      }

      if (this.currentStep === 4) {
        if (!this.agreed) {
          uni.showToast({ title: '请同意报名须知', icon: 'none' })
          return false
        }
      }

      return true
    },

    nextStep() {
      if (!this.validateStep()) {
        return
      }

      if (this.currentStep < this.totalSteps - 1) {
        this.currentStep++
      }
    },

    previousStep() {
      if (this.currentStep > 0) {
        this.currentStep--
      }
    },

    submitRegistration() {
      if (!this.validateStep()) {
        return
      }

      uni.showModal({
        title: '提交报名',
        content: '确认提交报名信息？提交后等待审核',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '提交中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '报名成功',
                icon: 'success',
                duration: 2000
              })

              setTimeout(() => {
                uni.redirectTo({
                  url: '/pages/learner/registration-status?id=1'
                })
              }, 2000)
            }, 1500)
          }
        }
      })
    },

    goBack() {
      if (this.currentStep > 0) {
        uni.showModal({
          title: '提示',
          content: '确认返回？已填写的信息将不会保存',
          success: (res) => {
            if (res.confirm) {
              uni.navigateBack()
            }
          }
        })
      } else {
        uni.navigateBack()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';

.registration-form-container {
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

.progress-bar {
  height: 4rpx;
  background: rgba(102, 126, 234, 0.2);
}

.progress-fill {
  height: 100%;
  background: $primary-gradient;
  transition: width 0.3s;
}

.step-indicator {
  padding: $spacing-md;
  text-align: center;
  background: $bg-primary;
}

.step-text {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.form-scroll {
  height: calc(100vh - 200rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.info-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-xl;
}

.training-icon {
  font-size: 96rpx;
}

.training-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  text-align: center;
}

.training-meta {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.meta-item {
  font-size: $font-size-md;
  color: $text-secondary;
}

.notice-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.notice-item {
  font-size: $font-size-md;
  color: $text-secondary;
  line-height: 1.8;
}

.form-section {
  margin-bottom: $spacing-md;
}

.form-item {
  margin-bottom: $spacing-lg;
}

.form-item.required .form-label::after {
  content: '*';
  color: #ef4444;
  margin-left: 4rpx;
}

.form-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-md;
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

.picker-input {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  color: $text-primary;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
  margin-right: $spacing-xl;
}

.radio-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.confirm-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color;
}

.confirm-label {
  font-size: $font-size-md;
  color: $text-secondary;
}

.confirm-value {
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
}

.agreement-section {
  margin-top: $spacing-xl;
}

.agreement-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.agreement-checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid $border-color;
  border-radius: $border-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
}

.agreement-checkbox.checked {
  background: $primary-color;
  border-color: $primary-color;
}

.checkbox-icon {
  color: $text-white;
  font-size: $font-size-sm;
  font-weight: bold;
}

.agreement-text {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.action-bar {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-primary;
  border-top: 1rpx solid $border-color;
}

.action-bar .btn {
  flex: 1;
}
</style>
