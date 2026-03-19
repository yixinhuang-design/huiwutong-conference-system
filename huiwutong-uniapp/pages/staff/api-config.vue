<template>
  <view class="api-config-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">接口配置</text>
        <text class="header-action" @click="testConnection">测试连接</text>
      </view>
    </view>

    <scroll-view class="config-scroll" scroll-y>
      <!-- 基础配置 -->
      <view class="config-section card">
        <view class="section-title">基础配置</view>

        <view class="config-item">
          <text class="config-label">API基础地址</text>
          <input
            v-model="apiConfig.baseUrl"
            class="config-input"
            placeholder="例如：http://localhost:8080/api"
          />
          <text class="config-hint">API服务的基础URL地址</text>
        </view>

        <view class="config-item">
          <text class="config-label">请求超时时间（秒）</text>
          <input
            v-model="apiConfig.timeout"
            class="config-input"
            type="number"
            placeholder="默认30秒"
          />
        </view>

        <view class="config-item">
          <view class="switch-row">
            <view class="switch-info">
              <text class="config-label">启用HTTPS</text>
              <text class="config-hint">使用HTTPS协议进行安全通信</text>
            </view>
            <switch
              :checked="apiConfig.https"
              @change="toggleHttps"
              color="#667eea"
            />
          </view>
        </view>

        <view class="config-item">
          <view class="switch-row">
            <view class="switch-info">
              <text class="config-label">启用调试模式</text>
              <text class="config-hint">在控制台输出请求日志</text>
            </view>
            <switch
              :checked="apiConfig.debug"
              @change="toggleDebug"
              color="#667eea"
            />
          </view>
        </view>
      </view>

      <!-- 认证配置 -->
      <view class="config-section card">
        <view class="section-title">认证配置</view>

        <view class="config-item">
          <text class="config-label">认证方式</text>
          <radio-group @change="onAuthTypeChange">
            <label class="radio-item">
              <radio value="token" :checked="apiConfig.authType === 'token'" color="#667eea" />
              <text class="radio-label">Token认证</text>
            </label>
            <label class="radio-item">
              <radio value="oauth2" :checked="apiConfig.authType === 'oauth2'" color="#667eea" />
              <text class="radio-label">OAuth2.0</text>
            </label>
            <label class="radio-item">
              <radio value="apiKey" :checked="apiConfig.authType === 'apiKey'" color="#667eea" />
              <text class="radio-label">API Key</text>
            </label>
          </radio-group>
        </view>

        <view v-if="apiConfig.authType === 'token'" class="auth-config">
          <view class="config-item">
            <text class="config-label">Token名称</text>
            <input
              v-model="apiConfig.tokenName"
              class="config-input"
              placeholder="默认：Authorization"
            />
          </view>

          <view class="config-item">
            <text class="config-label">Token前缀</text>
            <input
              v-model="apiConfig.tokenPrefix"
              class="config-input"
              placeholder="例如：Bearer"
            />
          </view>
        </view>

        <view v-if="apiConfig.authType === 'oauth2'" class="auth-config">
          <view class="config-item">
            <text class="config-label">Client ID</text>
            <input
              v-model="apiConfig.clientId"
              class="config-input"
              placeholder="请输入Client ID"
            />
          </view>

          <view class="config-item">
            <text class="config-label">Client Secret</text>
            <input
              v-model="apiConfig.clientSecret"
              class="config-input"
              placeholder="请输入Client Secret"
              password
            />
          </view>

          <view class="config-item">
            <text class="config-label">授权地址</text>
            <input
              v-model="apiConfig.authUrl"
              class="config-input"
              placeholder="OAuth授权服务器地址"
            />
          </view>
        </view>

        <view v-if="apiConfig.authType === 'apiKey'" class="auth-config">
          <view class="config-item">
            <text class="config-label">API Key</text>
            <input
              v-model="apiConfig.apiKey"
              class="config-input"
              placeholder="请输入API Key"
            />
          </view>

          <view class="config-item">
            <text class="config-label">Key位置</text>
            <radio-group @change="onKeyLocationChange">
              <label class="radio-item">
                <radio value="header" :checked="apiConfig.keyLocation === 'header'" color="#667eea" />
                <text class="radio-label">请求头</text>
              </label>
              <label class="radio-item">
                <radio value="query" :checked="apiConfig.keyLocation === 'query'" color="#667eea" />
                <text class="radio-label">查询参数</text>
              </label>
            </radio-group>
          </view>
        </view>
      </view>

      <!-- 请求头配置 -->
      <view class="config-section card">
        <view class="section-title">
          <text>公共请求头</text>
          <text class="section-action" @click="addHeader">+ 添加</text>
        </view>

        <view class="headers-list">
          <view
            v-for="(header, index) in apiConfig.headers"
            :key="index"
            class="header-item"
          >
            <input
              v-model="header.key"
              class="header-input"
              placeholder="Header名称"
            />
            <text class="header-separator">:</text>
            <input
              v-model="header.value"
              class="header-input"
              placeholder="Header值"
            />
            <text class="header-remove" @click="removeHeader(index)">✕</text>
          </view>

          <view v-if="apiConfig.headers.length === 0" class="empty-hint">
            <text>暂无公共请求头，点击上方"添加"按钮添加</text>
          </view>
        </view>
      </view>

      <!-- 环境配置 -->
      <view class="config-section card">
        <view class="section-title">环境配置</view>

        <view class="env-tabs">
          <text
            class="env-tab"
            :class="{ active: currentEnv === 'dev' }"
            @click="switchEnv('dev')"
          >
            开发环境
          </text>
          <text
            class="env-tab"
            :class="{ active: currentEnv === 'test' }"
            @click="switchEnv('test')"
          >
            测试环境
          </text>
          <text
            class="env-tab"
            :class="{ active: currentEnv === 'prod' }"
            @click="switchEnv('prod')"
          >
            生产环境
          </text>
        </view>

        <view class="env-config">
          <view class="config-item">
            <text class="config-label">环境地址</text>
            <input
              v-model="envConfig.url"
              class="config-input"
              placeholder="请输入环境API地址"
            />
          </view>

          <view class="config-item">
            <view class="switch-row">
              <view class="switch-info">
                <text class="config-label">启用Mock数据</text>
                <text class="config-hint">使用本地模拟数据</text>
              </view>
              <switch
                :checked="envConfig.mock"
                @change="toggleMock"
                color="#667eea"
              />
            </view>
          </view>
        </view>
      </view>

      <!-- 接口监控 -->
      <view class="config-section card">
        <view class="section-title">接口监控</view>

        <view class="monitor-stats">
          <view class="stat-item">
            <text class="stat-value">{{ monitor.totalRequests }}</text>
            <text class="stat-label">今日请求</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ monitor.successRate }}%</text>
            <text class="stat-label">成功率</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ monitor.avgTime }}ms</text>
            <text class="stat-label">平均响应</text>
          </view>
        </view>

        <view class="view-monitor-btn" @click="viewMonitor">
          <text>查看详细监控 →</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="actions-section">
        <button class="btn btn-outline btn-block" @click="resetConfig">
          🔄 重置配置
        </button>
        <button class="btn btn-primary btn-block" @click="saveConfig">
          💾 保存配置
        </button>
      </view>
    </scroll-view>

    <!-- 测试连接结果弹窗 -->
    <view v-if="showResultModal" class="modal-overlay" @click="hideResultModal">
      <view class="result-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">测试结果</text>
          <text class="modal-close" @click="hideResultModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="result-status" :class="testResult.success ? 'success' : 'error'">
            <text class="status-icon">{{ testResult.success ? '✅' : '❌' }}</text>
            <text class="status-text">{{ testResult.success ? '连接成功' : '连接失败' }}</text>
          </view>

          <view class="result-details" v-if="testResult.details">
            <text class="detail-title">详细信息：</text>
            <view class="detail-item">
              <text class="detail-label">响应时间：</text>
              <text class="detail-value">{{ testResult.details.responseTime }}ms</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">状态码：</text>
              <text class="detail-value">{{ testResult.details.statusCode }}</text>
            </view>
            <view class="detail-item" v-if="testResult.message">
              <text class="detail-label">错误信息：</text>
              <text class="detail-value error">{{ testResult.message }}</text>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <button class="btn btn-primary" @click="hideResultModal">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentEnv: 'dev',
      showResultModal: false,
      apiConfig: {
        baseUrl: 'http://localhost:8080/api',
        timeout: 30,
        https: false,
        debug: true,
        authType: 'token',
        tokenName: 'Authorization',
        tokenPrefix: 'Bearer',
        clientId: '',
        clientSecret: '',
        authUrl: '',
        apiKey: '',
        keyLocation: 'header',
        headers: [
          { key: 'Content-Type', value: 'application/json' },
          { key: 'X-Requested-With', value: 'XMLHttpRequest' }
        ]
      },
      envConfig: {
        url: 'http://localhost:8080/api',
        mock: true
      },
      monitor: {
        totalRequests: 1523,
        successRate: 98.5,
        avgTime: 125
      },
      testResult: {
        success: false,
        details: null,
        message: ''
      }
    }
  },

  methods: {
    toggleHttps(e) {
      this.apiConfig.https = e.detail.value
    },

    toggleDebug(e) {
      this.apiConfig.debug = e.detail.value
    },

    toggleMock(e) {
      this.envConfig.mock = e.detail.value
    },

    onAuthTypeChange(e) {
      this.apiConfig.authType = e.detail.value
    },

    onKeyLocationChange(e) {
      this.apiConfig.keyLocation = e.detail.value
    },

    switchEnv(env) {
      this.currentEnv = env

      // 切换环境时加载对应配置
      const configs = {
        dev: { url: 'http://localhost:8080/api', mock: true },
        test: { url: 'http://test.example.com/api', mock: false },
        prod: { url: 'https://api.example.com', mock: false }
      }

      this.envConfig = configs[env]
    },

    addHeader() {
      this.apiConfig.headers.push({ key: '', value: '' })
    },

    removeHeader(index) {
      this.apiConfig.headers.splice(index, 1)
    },

    testConnection() {
      uni.showLoading({ title: '测试中...' })

      setTimeout(() => {
        uni.hideLoading()

        // 模拟测试结果
        this.testResult = {
          success: Math.random() > 0.3,
          details: {
            responseTime: Math.floor(Math.random() * 500) + 50,
            statusCode: 200
          },
          message: ''
        }

        if (!this.testResult.success) {
          this.testResult.message = '无法连接到服务器，请检查网络或配置'
        }

        this.showResultModal = true
      }, 1500)
    },

    hideResultModal() {
      this.showResultModal = false
    },

    viewMonitor() {
      uni.navigateTo({
        url: '/pages/staff/api-monitor'
      })
    },

    resetConfig() {
      uni.showModal({
        title: '重置配置',
        content: '确定要重置为默认配置吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '重置中...' })

            setTimeout(() => {
              uni.hideLoading()

              uni.showToast({
                title: '重置成功',
                icon: 'success'
              })
            }, 500)
          }
        }
      })
    },

    saveConfig() {
      if (!this.apiConfig.baseUrl) {
        uni.showToast({
          title: '请输入API基础地址',
          icon: 'none'
        })
        return
      }

      uni.showLoading({ title: '保存中...' })

      setTimeout(() => {
        uni.hideLoading()

        uni.showToast({
          title: '保存成功',
          icon: 'success'
        })
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

.api-config-container {
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

.config-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.config-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-action {
  font-size: $font-size-sm;
  color: $primary-color;
}

.config-item {
  margin-bottom: $spacing-lg;
}

.config-item:last-child {
  margin-bottom: 0;
}

.config-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.config-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
}

.config-hint {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
  margin-top: 4rpx;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-info {
  flex: 1;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
  margin-right: $spacing-xl;
  margin-bottom: $spacing-sm;
}

.radio-label {
  font-size: $font-size-md;
  color: $text-primary;
}

.auth-config {
  padding: $spacing-md;
  margin-top: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.headers-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.header-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.header-input {
  flex: 1;
  padding: $spacing-sm $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
}

.header-separator {
  font-size: $font-size-md;
  color: $text-tertiary;
}

.header-remove {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-radius: 50%;
  font-size: $font-size-md;
}

.empty-hint {
  padding: $spacing-xl;
  text-align: center;
  color: $text-tertiary;
  font-size: $font-size-sm;
}

.env-tabs {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.env-tab {
  flex: 1;
  padding: $spacing-sm;
  text-align: center;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.env-tab.active {
  background: $primary-color;
  color: $text-white;
}

.env-config {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.monitor-stats {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.stat-value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $primary-color;
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.view-monitor-btn {
  padding: $spacing-md;
  text-align: center;
  color: $primary-color;
  font-size: $font-size-md;
}

.actions-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
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
  align-items: center;
  justify-content: center;
  padding: $spacing-lg;
}

.result-modal {
  width: 100%;
  max-width: 550rpx;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
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
  margin-bottom: $spacing-lg;
}

.result-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-xl;
  border-radius: $border-radius-md;
  margin-bottom: $spacing-lg;
}

.result-status.success {
  background: rgba(16, 185, 129, 0.1);
}

.result-status.error {
  background: rgba(239, 68, 68, 0.1);
}

.status-icon {
  font-size: 64rpx;
}

.status-text {
  font-size: $font-size-lg;
  font-weight: 500;
}

.result-status.success .status-text {
  color: #10b981;
}

.result-status.error .status-text {
  color: #ef4444;
}

.result-details {
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.detail-title {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.detail-item {
  display: flex;
  margin-bottom: $spacing-sm;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 120rpx;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.detail-value {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
}

.detail-value.error {
  color: #ef4444;
}

.modal-footer {
  display: flex;
}
</style>
