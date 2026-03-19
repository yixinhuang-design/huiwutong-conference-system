<template>
  <view class="status-bar" :style="{ height: statusBarHeight + 'px' }">
    <view class="status-content">
      <slot></slot>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0
    }
  },

  mounted() {
    this.getStatusBarHeight()
  },

  methods: {
    /**
     * 获取状态栏高度
     */
    getStatusBarHeight() {
      const systemInfo = uni.getSystemInfoSync()
      this.statusBarHeight = systemInfo.statusBarHeight || 20
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.status-bar {
  width: 100%;
  background: transparent;
  position: relative;
}

.status-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
}
</style>
