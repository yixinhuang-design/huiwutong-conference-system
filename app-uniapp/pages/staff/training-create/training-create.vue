<template>
  <view class="training-create">
    <view class="header-card">
      <view class="header-back">
        <text class="back-btn" @tap="goBack">←</text>
        <view class="header-title">{{ form.trainingName || '新建培训' }}</view>
      </view>
    </view>

    <view class="steps-container">
      <view class="step-item" v-for="i in 4" :key="i" :class="{ active: currentStep === i }">
        <view class="step-circle">{{ i }}</view>
        <view class="step-label">{{ ['基本信息', '会议日程', '报名设置', '其他配置'][i-1] }}</view>
      </view>
    </view>

    <view v-if="currentStep === 1" class="form-section">
      <view class="section-title">基本信息</view>
      <view class="form-card">
        <view class="form-group">
          <view class="form-label">培训名称 <text class="required">*</text></view>
          <input v-model="form.trainingName" type="text" placeholder="请输入培训名称" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">培训时间 <text class="required">*</text></view>
          <view class="date-range">
            <input v-model="form.startDate" type="date" class="form-input" />
            <text class="date-separator">至</text>
            <input v-model="form.endDate" type="date" class="form-input" />
          </view>
        </view>
        <view class="form-group">
          <view class="form-label">培训地点 <text class="required">*</text></view>
          <input v-model="form.location" type="text" placeholder="请输入培训地点" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">培训描述</view>
          <textarea v-model="form.description" placeholder="请输入培训描述" class="form-textarea"></textarea>
        </view>
      </view>
    </view>

    <view v-if="currentStep === 2" class="form-section">
      <view class="section-title">会议日程</view>
      <view class="form-card">
        <view class="add-schedule-btn" @tap="addSchedule">+ 添加日程</view>
        <view v-for="(schedule, index) in form.schedules" :key="index" class="schedule-item">
          <view class="schedule-header">
            <view class="schedule-title">第{{ index + 1 }}天</view>
            <view class="schedule-delete" @tap="removeSchedule(index)">删除</view>
          </view>
          <view class="form-group">
            <view class="form-label">会议主题</view>
            <input v-model="schedule.title" type="text" placeholder="请输入会议主题" class="form-input" />
          </view>
          <view class="time-group">
            <view class="form-group" style="flex: 1">
              <view class="form-label">开始时间</view>
              <input v-model="schedule.startTime" type="time" class="form-input" />
            </view>
            <view class="form-group" style="flex: 1">
              <view class="form-label">结束时间</view>
              <input v-model="schedule.endTime" type="time" class="form-input" />
            </view>
          </view>
          <view class="form-group">
            <view class="form-label">详细地址</view>
            <input v-model="schedule.address" type="text" placeholder="请输入详细地址" class="form-input" />
          </view>
          <view class="form-group">
            <view class="form-label">主持人</view>
            <input v-model="schedule.moderator" type="text" placeholder="请输入主持人" class="form-input" />
          </view>
          <view class="form-group">
            <view class="form-label">主讲人</view>
            <input v-model="schedule.speaker" type="text" placeholder="请输入主讲人" class="form-input" />
          </view>
          <view class="divider"></view>
          <view class="form-group">
            <view class="form-label">是否需要签到</view>
            <view class="checkbox-group">
              <checkbox v-model="schedule.requireCheckin" class="checkbox" />
              <text>需要签到</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="currentStep === 3" class="form-section">
      <view class="section-title">报名设置</view>
      <view class="form-card">
        <view class="form-group">
          <view class="form-label">报名方式 <text class="required">*</text></view>
          <picker mode="selector" :value="['open', 'limited', 'import'].indexOf(form.registrationType)" @change="(e) => form.registrationType = ['open', 'limited', 'import'][e.detail.value]">
            <view class="picker-value">{{ form.registrationType === 'open' ? '开放式报名' : form.registrationType === 'limited' ? '限定式报名' : '导入式报名' }}</view>
          </picker>
        </view>
        <view class="form-group">
          <view class="form-label">报名开始时间 <text class="required">*</text></view>
          <input v-model="form.registrationStartTime" type="datetime-local" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">报名截止时间 <text class="required">*</text></view>
          <input v-model="form.registrationEndTime" type="datetime-local" class="form-input" />
        </view>
        <view class="form-group">
          <view class="form-label">报名审核方式</view>
          <picker mode="selector" :value="form.auditRequired ? 1 : 0" @change="(e) => form.auditRequired = e.detail.value === 1">
            <view class="picker-value">{{ form.auditRequired ? '需要审核' : '不需要审核' }}</view>
          </picker>
        </view>
      </view>
    </view>

    <view v-if="currentStep === 4" class="form-section">
      <view class="section-title">报到与通知设置</view>
      <view class="form-card">
        <view class="form-group">
          <view class="form-label">报到方式</view>
          <picker mode="selector" :value="form.checkinMode === 'print' ? 0 : 1" @change="(e) => form.checkinMode = e.detail.value === 0 ? 'print' : 'qrcode'">
            <view class="picker-value">{{ form.checkinMode === 'print' ? '打印表单签字' : '二维码扫码 + 位置信息' }}</view>
          </picker>
        </view>
        <view class="form-group">
          <view class="form-label">报到详细地址</view>
          <input v-model="form.checkinAddress" type="text" placeholder="请输入报到的详细地址" class="form-input" />
        </view>
        <view class="divider"></view>
        <view class="form-group">
          <view class="form-label">默认通知方式</view>
          <view class="checkbox-list">
            <view class="checkbox-item">
              <checkbox :value="'sms'" :checked="form.notificationChannels.includes('sms')" @change="toggleNotification('sms')" />
              <text>短信通知</text>
            </view>
            <view class="checkbox-item">
              <checkbox :value="'app'" :checked="form.notificationChannels.includes('app')" @change="toggleNotification('app')" />
              <text>APP推送</text>
            </view>
            <view class="checkbox-item">
              <checkbox :value="'wechat'" :checked="form.notificationChannels.includes('wechat')" @change="toggleNotification('wechat')" />
              <text>微信通知</text>
            </view>
          </view>
        </view>
        <view class="form-group">
          <view class="form-label">通知文本模板</view>
          <textarea v-model="form.notificationTemplate" placeholder="请输入默认通知文本" class="form-textarea"></textarea>
        </view>
      </view>
    </view>

    <view class="action-bar">
      <button class="btn-secondary" @tap="saveDraft" :disabled="isSaving">{{ isSaving ? '保存中...' : '保存草稿' }}</button>
      <button v-if="currentStep < 4" class="btn-primary" @tap="nextStep">下一步 →</button>
      <button v-else class="btn-primary" @tap="createTraining" :disabled="isCreating">{{ isCreating ? '创建中...' : '创建培训' }}</button>
    </view>

    <view class="tip-banner">提示：创建培训后，系统将自动初始化会议记录、群组和统计数据。</view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'

const currentStep = ref(1)
const isSaving = ref(false)
const isCreating = ref(false)
const errors = reactive({})

const form = reactive({
  trainingName: '',
  startDate: '',
  endDate: '',
  location: '',
  description: '',
  schedules: [{ date: '', title: '', startTime: '09:00', endTime: '10:00', address: '', moderator: '', speaker: '', requireCheckin: true, checkinType: 'qrcode', requireReminder: true }],
  registrationType: 'open',
  registrationStartTime: '',
  registrationEndTime: '',
  auditRequired: false,
  checkinMode: 'qrcode',
  checkinAddress: '',
  notificationChannels: ['app', 'sms'],
  notificationTemplate: '尊敬的{姓名}，您好！您报名的{培训名称}将于{时间}在{地点}举行，请准时参加。'
})

const goBack = () => { uni.navigateBack() }

const validateStep = (step) => {
  if (step === 1) return !!(form.trainingName && form.startDate && form.endDate && form.location)
  if (step === 3) return !!(form.registrationStartTime && form.registrationEndTime)
  return true
}

const nextStep = () => {
  if (!validateStep(currentStep.value)) {
    uni.showToast({ title: '请填写必填项', icon: 'none' })
    return
  }
  if (currentStep.value < 4) currentStep.value++
}

const addSchedule = () => {
  form.schedules.push({ date: form.startDate, title: '', startTime: '09:00', endTime: '10:00', address: '', moderator: '', speaker: '', requireCheckin: true, checkinType: 'qrcode', requireReminder: true })
}

const removeSchedule = (index) => {
  if (form.schedules.length > 1) form.schedules.splice(index, 1)
}

const toggleNotification = (channel) => {
  const idx = form.notificationChannels.indexOf(channel)
  if (idx > -1) form.notificationChannels.splice(idx, 1)
  else form.notificationChannels.push(channel)
}

const saveDraft = () => {
  isSaving.value = true
  uni.setStorageSync('trainingDraft', JSON.stringify({ currentStep: currentStep.value, form, savedAt: new Date().toISOString() }))
  setTimeout(() => {
    isSaving.value = false
    uni.showToast({ title: '草稿已保存', icon: 'success' })
  }, 800)
}

const createTraining = async () => {
  for (let step = 1; step <= 4; step++) {
    if (!validateStep(step)) {
      currentStep.value = step
      uni.showToast({ title: `第${step}步有未填写的必填项`, icon: 'none' })
      return
    }
  }

  isCreating.value = true
  try {
    const trainingData = {
      meetingCode: 'TRAIN_' + Date.now(),
      meetingName: form.trainingName,
      meetingType: 'training',
      description: form.description,
      startTime: form.startDate + 'T09:00:00',
      endTime: form.endDate + 'T17:00:00',
      registrationStart: form.registrationStartTime,
      registrationEnd: form.registrationEndTime,
      venueName: form.location,
      venueAddress: form.location,
      registrationConfig: JSON.stringify({ mode: form.registrationType, auditRequired: form.auditRequired, schedules: form.schedules }),
      checkinConfig: JSON.stringify({ mode: form.checkinMode, address: form.checkinAddress }),
      notificationConfig: JSON.stringify({ channels: form.notificationChannels, template: form.notificationTemplate })
    }

    const token = uni.getStorageSync('auth_token') || ''
    const tenantId = uni.getStorageSync('tenant_id') || '2027317834622709762'

    const response = await new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8084/api/meeting/create',
        method: 'POST',
        header: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`, 'X-Tenant-Id': tenantId },
        data: trainingData,
        success: (res) => resolve(res),
        fail: (err) => reject(err)
      })
    })

    if (response.statusCode === 200 && response.data.code === 0) {
      uni.removeStorageSync('trainingDraft')
      uni.showToast({ title: '培训创建成功', icon: 'success' })
      setTimeout(() => { uni.navigateTo({ url: '/pages/staff/training/training' }) }, 500)
    } else {
      uni.showToast({ title: response.data.message || '创建失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: '创建培训失败', icon: 'none' })
  } finally {
    isCreating.value = false
  }
}

uni.onLoad(() => {
  const draft = uni.getStorageSync('trainingDraft')
  if (draft) {
    try {
      const draftData = JSON.parse(draft)
      currentStep.value = draftData.currentStep || 1
      Object.assign(form, draftData.form)
    } catch (e) {}
  }
})
</script>

<style scoped lang="scss">
.training-create { min-height: 100vh; background: #f5f7fa; padding: 0 }
.header-card { background: white; padding: 16px; border-bottom: 1px solid #e2e8f0; position: sticky; top: 0; z-index: 100 }
.header-back { display: flex; align-items: center; gap: 12px }
.back-btn { font-size: 24px; color: #667eea; padding: 8px; cursor: pointer }
.header-title { font-size: 18px; font-weight: 600; color: #1e293b; flex: 1 }
.steps-container { display: flex; justify-content: space-between; padding: 20px 16px; background: white; margin-bottom: 16px; gap: 4px; align-items: center }
.step-item { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 8px }
.step-circle { width: 36px; height: 36px; border-radius: 50%; background: #e2e8f0; color: #64748b; display: flex; align-items: center; justify-content: center; font-weight: 600; transition: all 0.3s; .step-item.active & { background: #667eea; color: white } }
.step-label { font-size: 12px; color: #64748b; text-align: center; .step-item.active & { color: #667eea; font-weight: 600 } }
.form-section { padding: 0 16px 16px; animation: slideIn 0.3s ease-out }
@keyframes slideIn { from { opacity: 0; transform: translateY(10px) } to { opacity: 1; transform: translateY(0) } }
.section-title { font-size: 14px; font-weight: 600; color: #1e293b; margin-bottom: 12px }
.form-card { background: white; border-radius: 12px; padding: 16px; margin-bottom: 16px }
.form-group { margin-bottom: 16px; &:last-child { margin-bottom: 0 } }
.form-label { display: block; font-size: 14px; font-weight: 500; color: #475569; margin-bottom: 8px }
.required { color: #ef4444 }
.form-input { width: 100%; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; box-sizing: border-box; transition: all 0.3s; &:focus { border-color: #667eea; box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) } }
.form-textarea { width: 100%; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; min-height: 80px; box-sizing: border-box; font-family: inherit; &:focus { border-color: #667eea; box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1) } }
.date-range { display: flex; align-items: center; gap: 8px; .form-input { flex: 1 } }
.date-separator { color: #64748b; padding: 0 4px }
.time-group { display: flex; gap: 12px; .form-group { flex: 1; margin-bottom: 0 } }
.add-schedule-btn { display: block; width: 100%; padding: 12px; text-align: center; background: #f1f5f9; border: 1px dashed #cbd5e1; border-radius: 8px; color: #667eea; font-size: 14px; font-weight: 500; margin-bottom: 16px; cursor: pointer; transition: all 0.3s; &:active { background: #e2e8f0 } }
.schedule-item { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; margin-bottom: 16px }
.schedule-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #e2e8f0 }
.schedule-title { font-weight: 600; color: #1e293b }
.schedule-delete { color: #ef4444; font-size: 12px; cursor: pointer; padding: 4px 8px; &:active { opacity: 0.7 } }
.divider { height: 1px; background: #e2e8f0; margin: 12px 0 }
.checkbox-group { display: flex; align-items: center; gap: 8px; .checkbox { width: 18px; height: 18px } }
.checkbox-list { display: flex; flex-direction: column; gap: 8px }
.checkbox-item { display: flex; align-items: center; gap: 8px }
.picker-value { padding: 12px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; color: #475569; min-height: 40px; display: flex; align-items: center }
.action-bar { display: flex; gap: 12px; padding: 16px; background: white; border-top: 1px solid #e2e8f0; position: sticky; bottom: 0; z-index: 100 }
button { flex: 1; padding: 14px; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.3s; &:active { transform: translateY(-1px) } &:disabled { opacity: 0.6; cursor: not-allowed } }
.btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white }
.btn-secondary { background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1 }
.tip-banner { padding: 12px 16px; background: #fef3c7; border-top: 1px solid #fcd34d; text-align: center; font-size: 12px; color: #92400e; line-height: 1.5 }
</style>
