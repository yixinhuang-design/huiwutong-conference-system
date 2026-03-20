<template>
  <view />
</template>

<script>
export default {
  onLaunch: function() {
    console.log('App Launch - 智能会议助手启动')
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
@import './styles/common.scss';

/* Font Awesome 图标库 - 与app高保真原型保持一致 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

/* 全局样式 */
page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f8fafc;
}

/* 重置样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

view, text, image, scroll-view, swiper, button, input {
  box-sizing: border-box;
}

/* 滚动条隐藏 */
::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  color: transparent;
}
</style>
