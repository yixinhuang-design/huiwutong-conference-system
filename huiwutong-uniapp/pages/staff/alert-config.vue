<template>
  <view class="alert-config-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">预警配置</text>
        <text class="header-action" @click="addConfig">+ 新增</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section card">
      <view class="stat-card">
        <text class="stat-num">{{ stats.total }}</text>
        <text class="stat-label">全部预警</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ stats.enabled }}</text>
        <text class="stat-label">已启用</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ stats.triggered }}</text>
        <text class="stat-label">已触发</text>
      </view>
    </view>

    <scroll-view class="config-scroll" scroll-y>
      <!-- 配置列表 -->
      <view class="config-list">
        <view
          v-for="(config, index) in configList"
          :key="config.id"
          class="config-card card"
        >
          <view class="config-header">
            <view class="config-info">
              <text class="config-name">{{ config.name }}</text>
              <text class="config-type">{{ getTypeLabel(config.type) }}</text>
            </view>
            <switch
              :checked="config.enabled"
              @change="toggleConfig(config)"
              color="#667eea"
            />
          </view>

          <view class="config-details">
            <view class="detail-item">
              <text class="detail-label">触发条件</text>
              <text class="detail-value">{{ config.condition }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">阈值</text>
              <text class="detail-value">{{ config.threshold }}</text>
            </view>
            <view class="detail-item" v-if="config.recipients">
              <text class="detail-label">通知对象</text>
              <text class="detail-value">{{ config.recipients }}</text>
            </view>
          </view>

          <view class="config-status" v-if="config.triggered">
            <view class="status-badge triggered">
              <text class="status-icon"><text class="fa fa-exclamation-triangle"></text></text>
              <text class="status-text">已触发</text>
            </view>
            <text class="trigger-time">{{ config.triggerTime }}</text>
          </view>

          <view class="config-actions">
            <button
              class="btn btn-sm btn-outline"
              @click="editConfig(config)"
            >
              编辑
            </button>
            <button
              class="btn btn-sm btn-text"
              @click="deleteConfig(config)"
            >
              删除
            </button>
          </view>
        </view>
      </view>

      <view v-if="configList.length === 0" class="empty-state">
        <text class="empty-icon"><text class="fa fa-bell"></text></text>
        <text class="empty-text">暂无预警配置</text>
        <button class="btn btn-primary" @click="addConfig">
          + 添加预警配置
        </button>
      </view>
    </scroll-view>

    <!-- 新增/编辑弹窗 -->
    <view class="modal-overlay" v-if="showFormModal" @click.self="closeFormModal">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">{{ editingRule ? '编辑预警规则' : '新增预警规则' }}</text>
          <text class="modal-close" @click="closeFormModal">✕</text>
        </view>
        <scroll-view scroll-y class="modal-body">
          <view class="form-group">
            <text class="form-label">规则名称</text>
            <input class="form-input" v-model="ruleForm.name" placeholder="请输入规则名称" />
          </view>
          <view class="form-group">
            <text class="form-label">预警指标</text>
            <picker :range="metricOptions" range-key="label" @change="onMetricChange">
              <view class="form-picker">
                <text>{{ getMetricLabel(ruleForm.metric) || '请选择指标' }}</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
          <view class="form-group">
            <text class="form-label">比较运算符</text>
            <view class="operator-group">
              <text
                v-for="op in ['<', '<=', '>', '>=', '==']"
                :key="op"
                class="operator-btn"
                :class="{ active: ruleForm.operator === op }"
                @click="ruleForm.operator = op"
              >{{ op }}</text>
            </view>
          </view>
          <view class="form-group">
            <text class="form-label">阈值</text>
            <input class="form-input" type="digit" v-model="ruleForm.threshold" placeholder="请输入阈值" />
          </view>
          <view class="form-group">
            <text class="form-label">预警级别</text>
            <view class="severity-group">
              <text
                v-for="s in severityOptions"
                :key="s.value"
                class="severity-btn"
                :class="[s.value, { active: ruleForm.severity === s.value }]"
                @click="ruleForm.severity = s.value"
              >{{ s.label }}</text>
            </view>
          </view>
          <view class="form-group">
            <text class="form-label">通知方式</text>
            <view class="notify-group">
              <label class="notify-option">
                <switch :checked="ruleForm.notifySystem" @change="ruleForm.notifySystem = $event.detail.value" color="#667eea" style="transform: scale(0.7)" />
                <text>系统通知</text>
              </label>
              <label class="notify-option">
                <switch :checked="ruleForm.notifySms" @change="ruleForm.notifySms = $event.detail.value" color="#667eea" style="transform: scale(0.7)" />
                <text>短信通知</text>
              </label>
            </view>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <button class="btn btn-outline" @click="closeFormModal">取消</button>
          <button class="btn btn-primary" @click="submitRule" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认保存' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import alertApi from '@/api/alert'

export default {
  data() {
    return {
      conferenceId: '',
      stats: {
        total: 0,
        enabled: 0,
        triggered: 0
      },
      configList: [],
      showFormModal: false,
      editingRule: null,
      submitting: false,
      ruleForm: {
        name: '',
        metric: '',
        operator: '<',
        threshold: '',
        severity: 'medium',
        notifySystem: true,
        notifySms: false
      },
      metricOptions: [
        { value: 'registrationRate', label: '报名率' },
        { value: 'checkinRate', label: '签到率' },
        { value: 'taskCompletionRate', label: '任务完成率' },
        { value: 'apiResponseTime', label: 'API响应时间' },
        { value: 'systemErrorRate', label: '系统错误率' }
      ],
      severityOptions: [
        { value: 'high', label: '高危' },
        { value: 'medium', label: '中危' },
        { value: 'low', label: '低危' }
      ]
    }
  },

  onLoad(options) {
    this.conferenceId = options.conferenceId || uni.getStorageSync('conferenceId') || '2030309010523144194'
    this.loadRules()
  },

  methods: {
    async loadRules() {
      try {
        const result = await alertApi.ruleList(this.conferenceId)
        const list = Array.isArray(result) ? result : []
        this.configList = list.map(r => ({
          id: r.id,
          name: r.name,
          type: r.metric,
          enabled: r.enabled === 1,
          triggered: false,
          condition: `${r.metric} ${r.operator}`,
          threshold: `${r.threshold}`,
          recipients: (r.notifySystem ? '系统通知' : '') + (r.notifySms ? '+短信' : ''),
          _raw: r
        }))
        this.stats.total = list.length
        this.stats.enabled = list.filter(r => r.enabled === 1).length
      } catch (e) {
        console.error('加载规则失败', e)
      }
    },
    getTypeLabel(type) {
      const typeMap = {
        registration: '报名预警',
        checkin: '签到预警',
        task: '任务预警',
        seat: '座位预警',
        satisfaction: '满意度预警',
        equipment: '设备预警',
        registrationRate: '报名率预警',
        checkinRate: '签到率预警',
        taskCompletionRate: '任务完成率预警',
        apiResponseTime: 'API响应时间预警',
        systemErrorRate: '系统错误率预警'
      }
      return typeMap[type] || '其他预警'
    },

    getMetricLabel(metric) {
      const item = this.metricOptions.find(m => m.value === metric)
      return item ? item.label : ''
    },

    onMetricChange(e) {
      this.ruleForm.metric = this.metricOptions[e.detail.value].value
      if (!this.ruleForm.name) {
        this.ruleForm.name = this.metricOptions[e.detail.value].label + '预警'
      }
    },

    async toggleConfig(config) {
      try {
        await alertApi.ruleToggle(config.id)
        config.enabled = !config.enabled
        this.stats.enabled += config.enabled ? 1 : -1
        uni.showToast({ title: config.enabled ? '已启用' : '已禁用', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    addConfig() {
      this.editingRule = null
      this.ruleForm = {
        name: '',
        metric: '',
        operator: '<',
        threshold: '',
        severity: 'medium',
        notifySystem: true,
        notifySms: false
      }
      this.showFormModal = true
    },

    editConfig(config) {
      this.editingRule = config
      this.ruleForm = {
        name: config.name || '',
        metric: config.type || '',
        operator: config.condition ? config.condition.replace(config.type, '').trim() : '<',
        threshold: config.threshold || '',
        severity: this.getSeverityFromConfig(config),
        notifySystem: (config.recipients || '').includes('系统'),
        notifySms: (config.recipients || '').includes('短信')
      }
      this.showFormModal = true
    },

    getSeverityFromConfig(config) {
      // 尝试从原始数据推断severity
      if (config._raw && config._raw.severity) return config._raw.severity
      return 'medium'
    },

    closeFormModal() {
      this.showFormModal = false
      this.editingRule = null
    },

    async submitRule() {
      if (!this.ruleForm.name) {
        uni.showToast({ title: '请输入规则名称', icon: 'none' })
        return
      }
      if (!this.ruleForm.metric) {
        uni.showToast({ title: '请选择预警指标', icon: 'none' })
        return
      }
      if (!this.ruleForm.threshold) {
        uni.showToast({ title: '请输入阈值', icon: 'none' })
        return
      }

      this.submitting = true
      try {
        const body = {
          conferenceId: this.conferenceId,
          name: this.ruleForm.name,
          metric: this.ruleForm.metric,
          operator: this.ruleForm.operator,
          threshold: parseFloat(this.ruleForm.threshold),
          severity: this.ruleForm.severity,
          notifySms: this.ruleForm.notifySms ? 1 : 0,
          notifySystem: this.ruleForm.notifySystem ? 1 : 0
        }

        if (this.editingRule) {
          await alertApi.ruleUpdate(this.editingRule.id, body)
          uni.showToast({ title: '修改成功', icon: 'success' })
        } else {
          await alertApi.ruleSave(body)
          uni.showToast({ title: '添加成功', icon: 'success' })
        }

        this.closeFormModal()
        await this.loadRules()
      } catch (e) {
        uni.showToast({ title: '操作失败: ' + (e.message || ''), icon: 'none' })
      } finally {
        this.submitting = false
      }
    },

    deleteConfig(config) {
      uni.showModal({
        title: '删除配置',
        content: `确认删除预警配置"${config.name}"？`,
        confirmColor: '#ef4444',
        success: async (res) => {
          if (res.confirm) {
            try {
              await alertApi.ruleDelete(config.id)
              const index = this.configList.findIndex(c => c.id === config.id)
              if (index > -1) {
                this.configList.splice(index, 1)
                this.stats.total--
                if (config.enabled) this.stats.enabled--
              }
              uni.showToast({ title: '删除成功', icon: 'success' })
            } catch (e) {
              uni.showToast({ title: '删除失败', icon: 'none' })
            }
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

.alert-config-container {
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

.config-scroll {
  height: calc(100vh - 200rpx);
  padding: $spacing-md;
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.config-card {
  margin-bottom: 0;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.config-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.config-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.config-type {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.config-details {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.detail-value {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.config-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: rgba(239, 68, 68, 0.05);
  border-radius: $border-radius-md;
  margin-bottom: $spacing-md;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.status-badge.triggered .status-icon {
  font-size: $font-size-md;
}

.status-badge.triggered .status-text {
  font-size: $font-size-sm;
  font-weight: 500;
  color: #ef4444;
}

.trigger-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.config-actions {
  display: flex;
  gap: $spacing-sm;
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
  margin-bottom: $spacing-lg;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 90%;
  max-height: 80vh;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.modal-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.modal-close {
  font-size: 36rpx;
  color: $text-tertiary;
  padding: 10rpx;
}

.modal-body {
  max-height: 55vh;
  padding: $spacing-md;
}

.modal-footer {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md;
  border-top: 1rpx solid $border-color;
}

.modal-footer .btn {
  flex: 1;
}

.form-group {
  margin-bottom: $spacing-md;
}

.form-label {
  display: block;
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-secondary;
  margin-bottom: $spacing-xs;
}

.form-input {
  width: 100%;
  padding: $spacing-sm $spacing-md;
  border: 1rpx solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  color: $text-primary;
  background: $bg-tertiary;
}

.form-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  border: 1rpx solid $border-color;
  border-radius: $border-radius-md;
  background: $bg-tertiary;
  color: $text-primary;
}

.picker-arrow {
  font-size: 24rpx;
  color: $text-tertiary;
}

.operator-group {
  display: flex;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.operator-btn {
  padding: $spacing-xs $spacing-md;
  border: 1rpx solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  color: $text-secondary;
  background: $bg-tertiary;
}

.operator-btn.active {
  border-color: $primary-color;
  color: $primary-color;
  background: rgba(102, 126, 234, 0.1);
}

.severity-group {
  display: flex;
  gap: $spacing-sm;
}

.severity-btn {
  flex: 1;
  text-align: center;
  padding: $spacing-sm;
  border: 1rpx solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  font-weight: 500;
}

.severity-btn.high {
  color: #ef4444;
}
.severity-btn.high.active {
  background: rgba(239, 68, 68, 0.1);
  border-color: #ef4444;
}

.severity-btn.medium {
  color: #f59e0b;
}
.severity-btn.medium.active {
  background: rgba(245, 158, 11, 0.1);
  border-color: #f59e0b;
}

.severity-btn.low {
  color: #3b82f6;
}
.severity-btn.low.active {
  background: rgba(59, 130, 246, 0.1);
  border-color: #3b82f6;
}

.notify-group {
  display: flex;
  gap: $spacing-lg;
}

.notify-option {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-primary;
}
</style>
