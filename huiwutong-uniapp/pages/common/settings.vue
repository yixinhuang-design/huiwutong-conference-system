<template>
  <view class="settings-container">
    <!-- 自定义头部 -->
    <view class="custom-header">
      <view class="header-content">
        <text class="header-title">设置</text>
      </view>
    </view>

    <!-- 设置列表 -->
    <view class="settings-list card">
      <view class="settings-group">
        <view class="group-title">账号设置</view>
        <view
          v-for="(item, index) in accountSettings"
          :key="index"
          class="setting-item"
          @click="handleSetting(item.action)"
        >
          <view class="setting-icon"><text :class="item.icon"></text></view>
          <view class="setting-info">{{ item.title }}</view>
          <view class="setting-value" v-if="item.value">{{ item.value }}</view>
          <text class="setting-arrow" v-else>›</text>
        </view>
      </view>

      <view class="settings-group">
        <view class="group-title">通用设置</view>
        <view
          v-for="(item, index) in generalSettings"
          :key="index"
          class="setting-item"
          @click="handleSetting(item.action)"
        >
          <view class="setting-icon"><text :class="item.icon"></text></view>
          <view class="setting-info">{{ item.title }}</view>
          <switch
            v-if="item.type === 'switch'"
            :checked="item.checked"
            @change="handleSwitch(item, $event)"
            color="#667eea"
          />
          <text class="setting-arrow" v-else>›</text>
        </view>
      </view>

      <view class="settings-group">
        <view class="group-title">其他</view>
        <view
          v-for="(item, index) in otherSettings"
          :key="index"
          class="setting-item"
          @click="handleSetting(item.action)"
        >
          <view class="setting-icon"><text :class="item.icon"></text></view>
          <view class="setting-info">{{ item.title }}</view>
          <text class="setting-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text>当前版本 v1.0.0</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      accountSettings: [
        {
          icon: 'fa-lock',
          title: '修改密码',
          action: 'password'
        },
        {
          icon: 'fa-mobile-alt',
          title: '手机号',
          value: '138****8888',
          action: 'phone'
        },
        {
          icon: 'fa-envelope',
          title: '邮箱',
          value: '未绑定',
          action: 'email'
        }
      ],
      generalSettings: [
        {
          icon: 'fa-bell',
          title: '消息通知',
          type: 'switch',
          checked: true,
          action: 'notification'
        },
        {
          icon: 'fa-moon',
          title: '深色模式',
          type: 'switch',
          checked: false,
          action: 'darkMode'
        },
        {
          icon: 'fa-text-height',
          title: '字体大小',
          action: 'fontSize'
        },
        {
          icon: 'fa-globe',
          title: '语言',
          value: '简体中文',
          action: 'language'
        }
      ],
      otherSettings: [
        {
          icon: 'fa-broom',
          title: '清除缓存',
          action: 'cache'
        },
        {
          icon: 'fa-file-alt',
          title: '用户协议',
          action: 'agreement'
        },
        {
          icon: 'fa-lock',
          title: '隐私政策',
          action: 'privacy'
        },
        {
          icon: 'fa-info-circle',
          title: '关于我们',
          action: 'about'
        }
      ]
    }
  },

  methods: {
    /**
     * 处理设置点击
     */
    handleSetting(action) {
      const actions = {
        password: '修改密码',
        phone: '更换手机号',
        email: '绑定邮箱',
        fontSize: '字体大小',
        language: '语言设置',
        cache: '清除缓存',
        agreement: '用户协议',
        privacy: '隐私政策',
        about: '关于我们'
      }

      if (actions[action]) {
        uni.showToast({
          title: actions[action] + '功能开发中',
          icon: 'none'
        })
      }
    },

    /**
     * 处理开关
     */
    handleSwitch(item, event) {
      item.checked = event.detail.value

      uni.showToast({
        title: item.checked ? '已开启' : '已关闭',
        icon: 'success'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../styles/variables.scss';
@import '../../styles/common.scss';
@import '../../styles/global-patch.scss';

.settings-container {
  min-height: 100vh;
  background: $bg-secondary;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.custom-header {
  background: $bg-primary;
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-color;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.settings-list {
  margin: $spacing-md;
}

.settings-group {
  margin-bottom: $spacing-lg;
}

.settings-group:last-child {
  margin-bottom: 0;
}

.group-title {
  font-size: $font-size-sm;
  color: $text-secondary;
  padding: $spacing-md 0;
  margin-left: $spacing-sm;
}

.setting-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-primary;
  border-bottom: 1rpx solid $border-color;
  transition: $transition-base;
}

.setting-item:first-child {
  border-top-left-radius: $border-radius-md;
  border-top-right-radius: $border-radius-md;
}

.setting-item:last-child {
  border-bottom: none;
  border-bottom-left-radius: $border-radius-md;
  border-bottom-right-radius: $border-radius-md;
}

.setting-item:active {
  background: $bg-tertiary;
  margin: 0 (-$spacing-md);
  padding-left: $spacing-md;
  padding-right: $spacing-md;
}

.setting-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.setting-info {
  flex: 1;
  font-size: $font-size-md;
  color: $text-primary;
}

.setting-value {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-right: $spacing-sm;
}

.setting-arrow {
  font-size: 48rpx;
  color: $text-tertiary;
  font-weight: 300;
  flex-shrink: 0;
}

.version-info {
  text-align: center;
  padding: $spacing-xl;
  font-size: $font-size-sm;
  color: $text-tertiary;
}
</style>
