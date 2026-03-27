<template>
  <view class="system-settings-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="header-title">系统设置</text>
        <text class="header-action" @click="saveSettings">保存</text>
      </view>
    </view>

    <scroll-view class="settings-scroll" scroll-y>
      <!-- 基础设置 -->
      <view class="settings-section card">
        <view class="section-title">基础设置</view>

        <view class="setting-item">
          <text class="setting-label">系统名称</text>
          <input
            v-model="settings.systemName"
            class="setting-input"
            placeholder="请输入系统名称"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">系统简称</text>
          <input
            v-model="settings.systemShortName"
            class="setting-input"
            placeholder="请输入系统简称"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">系统Logo</text>
          <view class="logo-upload" @click="uploadLogo">
            <image
              v-if="settings.logo"
              :src="settings.logo"
              class="logo-image"
              mode="aspectFit"
            />
            <view v-else class="upload-placeholder">
              <text class="upload-icon">+</text>
              <text class="upload-text">上传Logo</text>
            </view>
          </view>
        </view>

        <view class="setting-item">
          <text class="setting-label">登录页背景</text>
          <view class="background-upload" @click="uploadBackground">
            <image
              v-if="settings.loginBackground"
              :src="settings.loginBackground"
              class="background-image"
              mode="aspectFill"
            />
            <view v-else class="upload-placeholder">
              <text class="upload-icon">+</text>
              <text class="upload-text">上传背景</text>
            </view>
          </view>
        </view>

        <view class="setting-item">
          <text class="setting-label">系统描述</text>
          <textarea
            v-model="settings.description"
            class="setting-textarea"
            placeholder="请输入系统描述"
            :maxlength="500"
          ></textarea>
        </view>
      </view>

      <!-- 安全设置 -->
      <view class="settings-section card">
        <view class="section-title">安全设置</view>

        <view class="setting-item">
          <view class="switch-row">
            <view class="switch-info">
              <text class="setting-label">启用验证码</text>
              <text class="setting-desc">登录时是否需要验证码</text>
            </view>
            <switch
              :checked="settings.enableCaptcha"
              @change="toggleCaptcha"
              color="#667eea"
            />
          </view>
        </view>

        <view class="setting-item">
          <view class="switch-row">
            <view class="switch-info">
              <text class="setting-label">强制密码复杂度</text>
              <text class="setting-desc">密码必须包含大小写字母、数字和特殊字符</text>
            </view>
            <switch
              :checked="settings.passwordComplexity"
              @change="togglePasswordComplexity"
              color="#667eea"
            />
          </view>
        </view>

        <view class="setting-item">
          <text class="setting-label">密码过期天数</text>
          <input
            v-model="settings.passwordExpiry"
            class="setting-input"
            type="number"
            placeholder="0表示永不过期"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">最大登录失败次数</text>
          <input
            v-model="settings.maxLoginAttempts"
            class="setting-input"
            type="number"
            placeholder="超过此次数将锁定账号"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">会话超时时间（分钟）</text>
          <input
            v-model="settings.sessionTimeout"
            class="setting-input"
            type="number"
            placeholder="用户无操作自动退出时间"
          />
        </view>
      </view>

      <!-- 短信设置 -->
      <view class="settings-section card">
        <view class="section-title">短信设置</view>

        <view class="setting-item">
          <text class="setting-label">短信服务商</text>
          <picker mode="selector" :range="smsProviders" @change="onSmsProviderChange">
            <view class="picker-input">
              {{ settings.smsProvider || '请选择服务商' }}
            </view>
          </picker>
        </view>

        <view class="setting-item">
          <text class="setting-label">AccessKey ID</text>
          <input
            v-model="settings.smsAccessKey"
            class="setting-input"
            placeholder="请输入AccessKey ID"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">AccessKey Secret</text>
          <input
            v-model="settings.smsAccessSecret"
            class="setting-input"
            placeholder="请输入AccessKey Secret"
            password
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">短信签名</text>
          <input
            v-model="settings.smsSignature"
            class="setting-input"
            placeholder="请输入短信签名"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">验证码模板ID</text>
          <input
            v-model="settings.smsTemplateCode"
            class="setting-input"
            placeholder="请输入验证码模板ID"
          />
        </view>

        <view class="setting-item">
          <view class="test-sms-btn" @click="testSms">
            <text>📧 发送测试短信</text>
          </view>
        </view>
      </view>

      <!-- 存储设置 -->
      <view class="settings-section card">
        <view class="section-title">存储设置</view>

        <view class="setting-item">
          <text class="setting-label">存储方式</text>
          <radio-group @change="onStorageTypeChange">
            <label class="radio-item">
              <radio value="local" :checked="settings.storageType === 'local'" color="#667eea" />
              <text class="radio-label">本地存储</text>
            </label>
            <label class="radio-item">
              <radio value="oss" :checked="settings.storageType === 'oss'" color="#667eea" />
              <text class="radio-label">阿里云OSS</text>
            </label>
            <label class="radio-item">
              <radio value="qcloud" :checked="settings.storageType === 'qcloud'" color="#667eea" />
              <text class="radio-label">腾讯云COS</text>
            </label>
          </radio-group>
        </view>

        <view v-if="settings.storageType !== 'local'" class="storage-config">
          <view class="setting-item">
            <text class="setting-label">Bucket名称</text>
            <input
              v-model="settings.storageBucket"
              class="setting-input"
              placeholder="请输入Bucket名称"
            />
          </view>

          <view class="setting-item">
            <text class="setting-label">地域节点</text>
            <input
              v-model="settings.storageRegion"
              class="setting-input"
              placeholder="请输入地域节点"
            />
          </view>

          <view class="setting-item">
            <text class="setting-label">AccessKey ID</text>
            <input
              v-model="settings.storageAccessKey"
              class="setting-input"
              placeholder="请输入AccessKey ID"
            />
          </view>

          <view class="setting-item">
            <text class="setting-label">AccessKey Secret</text>
            <input
              v-model="settings.storageAccessSecret"
              class="setting-input"
              placeholder="请输入AccessKey Secret"
              password
            />
          </view>

          <view class="setting-item">
            <text class="setting-label">访问域名</text>
            <input
              v-model="settings.storageDomain"
              class="setting-input"
              placeholder="请输入自定义访问域名（可选）"
            />
          </view>
        </view>

        <view class="setting-item">
          <text class="setting-label">单个文件大小限制（MB）</text>
          <input
            v-model="settings.maxFileSize"
            class="setting-input"
            type="number"
            placeholder="请输入文件大小限制"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">允许的文件类型</text>
          <textarea
            v-model="settings.allowedFileTypes"
            class="setting-textarea"
            placeholder="例如：jpg,png,pdf,docx"
            :maxlength="500"
          ></textarea>
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="settings-section card">
        <view class="section-title">其他设置</view>

        <view class="setting-item">
          <text class="setting-label">技术支持电话</text>
          <input
            v-model="settings.supportPhone"
            class="setting-input"
            type="number"
            placeholder="请输入技术支持电话"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">技术支持邮箱</text>
          <input
            v-model="settings.supportEmail"
            class="setting-input"
            placeholder="请输入技术支持邮箱"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">ICP备案号</text>
          <input
            v-model="settings.icpNumber"
            class="setting-input"
            placeholder="请输入ICP备案号"
          />
        </view>

        <view class="setting-item">
          <view class="switch-row">
            <view class="switch-info">
              <text class="setting-label">启用系统公告</text>
              <text class="setting-desc">登录时显示系统公告</text>
            </view>
            <switch
              :checked="settings.enableAnnouncement"
              @change="toggleAnnouncement"
              color="#667eea"
            />
          </view>
        </view>

        <view v-if="settings.enableAnnouncement" class="setting-item">
          <text class="setting-label">系统公告内容</text>
          <textarea
            v-model="settings.announcementContent"
            class="setting-textarea"
            placeholder="请输入系统公告内容"
            :maxlength="1000"
          ></textarea>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      smsProviders: ['阿里云', '腾讯云', '华为云'],
      settings: {
        // 基础设置
        systemName: '智能会议助手',
        systemShortName: '会务通',
        logo: '',
        loginBackground: '',
        description: '专业的培训会议管理平台',

        // 安全设置
        enableCaptcha: true,
        passwordComplexity: true,
        passwordExpiry: 90,
        maxLoginAttempts: 5,
        sessionTimeout: 30,

        // 短信设置
        smsProvider: '阿里云',
        smsAccessKey: '',
        smsAccessSecret: '',
        smsSignature: '',
        smsTemplateCode: '',

        // 存储设置
        storageType: 'local',
        storageBucket: '',
        storageRegion: '',
        storageAccessKey: '',
        storageAccessSecret: '',
        storageDomain: '',
        maxFileSize: 10,
        allowedFileTypes: 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx',

        // 其他设置
        supportPhone: '',
        supportEmail: '',
        icpNumber: '',
        enableAnnouncement: true,
        announcementContent: ''
      }
    }
  },

  onLoad() {
    this.loadSettings()
  },

  methods: {
    loadSettings() {
      uni.showLoading({ title: '加载中...' })

      setTimeout(() => {
        uni.hideLoading()
      }, 500)
    },

    uploadLogo() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.settings.logo = res.tempFilePaths[0]
        }
      })
    },

    uploadBackground() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.settings.loginBackground = res.tempFilePaths[0]
        }
      })
    },

    toggleCaptcha(e) {
      this.settings.enableCaptcha = e.detail.value
    },

    togglePasswordComplexity(e) {
      this.settings.passwordComplexity = e.detail.value
    },

    toggleAnnouncement(e) {
      this.settings.enableAnnouncement = e.detail.value
    },

    onSmsProviderChange(e) {
      this.settings.smsProvider = this.smsProviders[e.detail.value]
    },

    onStorageTypeChange(e) {
      this.settings.storageType = e.detail.value
    },

    testSms() {
      uni.showModal({
        title: '发送测试短信',
        content: '请输入接收手机号',
        editable: true,
        placeholderText: '请输入手机号',
        success: (res) => {
          if (res.confirm && res.content) {
            uni.showLoading({ title: '发送中...' })

            setTimeout(() => {
              uni.hideLoading()
              uni.showToast({
                title: '发送成功',
                icon: 'success'
              })
            }, 1500)
          }
        }
      })
    },

    saveSettings() {
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

.system-settings-container {
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

.settings-scroll {
  height: calc(100vh - 88rpx);
  padding: $spacing-md;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.settings-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.setting-item {
  margin-bottom: $spacing-lg;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-label {
  display: block;
  font-size: $font-size-md;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.setting-desc {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
  margin-top: 4rpx;
}

.setting-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
}

.setting-textarea {
  width: 100%;
  min-height: 120rpx;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  line-height: 1.6;
}

.logo-upload,
.background-upload {
  width: 100%;
  height: 200rpx;
  border: 2rpx dashed $border-color;
  border-radius: $border-radius-md;
  overflow: hidden;
}

.logo-image {
  width: 100%;
  height: 100%;
}

.background-image {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  background: $bg-tertiary;
}

.upload-icon {
  font-size: 48rpx;
  color: $text-tertiary;
}

.upload-text {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-info {
  flex: 1;
}

.picker-input {
  width: 100%;
  padding: $spacing-md;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
  text-align: center;
  font-size: $font-size-md;
  color: $text-primary;
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

.storage-config {
  padding: $spacing-md;
  margin: $spacing-md 0;
  background: $bg-tertiary;
  border-radius: $border-radius-md;
}

.test-sms-btn {
  padding: $spacing-md;
  text-align: center;
  background: $primary-gradient;
  color: $text-white;
  border-radius: $border-radius-md;
  font-size: $font-size-md;
  font-weight: 500;
}
</style>
