<template>
  <view />
</template>

<script>
import { useUserStore } from '@/store/modules/user'

export default {
  onLaunch: function() {
    console.log('App Launch - 智能会议助手启动')

    // 动态加载 Font Awesome 本地字体（App 原生端需要）
    // #ifdef APP-PLUS
    uni.loadFontFace({
      global: true,
      family: 'Font Awesome 6 Free',
      source: 'url("/static/fonts/fa-solid-900.ttf")',
      success() {
        console.log('Font Awesome 字体加载成功')
      },
      fail(err) {
        console.warn('Font Awesome 字体加载失败:', err)
      }
    })
    // #endif

    // 从本地存储恢复用户登录状态
    try {
      const userStore = useUserStore()
      userStore.initUserState()
    } catch (e) {
      console.error('Init user state failed:', e)
    }
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
@import './styles/font-awesome.scss';

/* Font Awesome 图标库 - 已通过 font-awesome.scss 本地加载 */

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

/* 确保图标正确显示 */
.fa {
  font-family: 'Font Awesome 6 Free', 'Font Awesome 6 Brands' !important;
}

/* ========== 图标备用方案 - Emoji Fallback ========== */
/* 当 Font Awesome 字体未加载时，使用 emoji 备用 */

/* 常用图标 emoji 备用 */
text.fa-check:empty::before {
  content: '✓';
  font-family: sans-serif !important;
}

text.fa-times:empty::before {
  content: '✕';
  font-family: sans-serif !important;
}

text.fa-star:empty::before {
  content: '★';
  font-family: sans-serif !important;
}

text.fa-star-o:empty::before {
  content: '☆';
  font-family: sans-serif !important;
}

text.fa-home:empty::before {
  content: '🏠';
  font-family: sans-serif !important;
}

text.fa-user:empty::before {
  content: '👤';
  font-family: sans-serif !important;
}

text.fa-clock:empty::before {
  content: '🕐';
  font-family: sans-serif !important;
}

text.fa-calendar-alt:empty::before {
  content: '📅';
  font-family: sans-serif !important;
}

text.fa-lock:empty::before {
  content: '🔒';
  font-family: sans-serif !important;
}

text.fa-mobile-alt:empty::before {
  content: '📱';
  font-family: sans-serif !important;
}

text.fa-eye:empty::before {
  content: '👁';
  font-family: sans-serif !important;
}

text.fa-eye-slash:empty::before {
  content: '🙈';
  font-family: sans-serif !important;
}

text.fa-search:empty::before {
  content: '🔍';
  font-family: sans-serif !important;
}

text.fa-bell:empty::before {
  content: '🔔';
  font-family: sans-serif !important;
}

text.fa-cog:empty::before {
  content: '⚙';
  font-family: sans-serif !important;
}

text.fa-edit:empty::before {
  content: '✏';
  font-family: sans-serif !important;
}

text.fa-trash:empty::before {
  content: '🗑';
  font-family: sans-serif !important;
}

text.fa-book:empty::before {
  content: '📖';
  font-family: sans-serif !important;
}

text.fa-book-open:empty::before {
  content: '📖';
  font-family: sans-serif !important;
}

text.fa-users:empty::before {
  content: '👥';
  font-family: sans-serif !important;
}

text.fa-comments:empty::before {
  content: '💬';
  font-family: sans-serif !important;
}

text.fa-heart:empty::before {
  content: '♥';
  font-family: sans-serif !important;
}

text.fa-camera:empty::before {
  content: '📷';
  font-family: sans-serif !important;
}

text.fa-chart-bar:empty::before {
  content: '📊';
  font-family: sans-serif !important;
}

text.fa-chart-line:empty::before {
  content: '📈';
  font-family: sans-serif !important;
}

text.fa-th-large:empty::before {
  content: '⊞';
  font-family: sans-serif !important;
}

text.fa-qrcode:empty::before {
  content: '⚃';
  font-family: sans-serif !important;
}

text.fa-map-marker-alt:empty::before {
  content: '📍';
  font-family: sans-serif !important;
}

text.fa-phone:empty::before {
  content: '📞';
  font-family: sans-serif !important;
}

text.fa-envelope:empty::before {
  content: '✉';
  font-family: sans-serif !important;
}

text.fa-file-alt:empty::before {
  content: '📄';
  font-family: sans-serif !important;
}

text.fa-download:empty::before {
  content: '⬇';
  font-family: sans-serif !important;
}

text.fa-folder:empty::before {
  content: '📁';
  font-family: sans-serif !important;
}

text.fa-sync:empty::before {
  content: '🔄';
  font-family: sans-serif !important;
}

text.fa-check-circle:empty::before {
  content: '✔';
  font-family: sans-serif !important;
}

text.fa-exclamation-circle:empty::before {
  content: '⚠';
  font-family: sans-serif !important;
}

text.fa-exclamation-triangle:empty::before {
  content: '⚠';
  font-family: sans-serif !important;
}

text.fa-pause:empty::before {
  content: '⏸';
  font-family: sans-serif !important;
}

text.fa-play:empty::before {
  content: '▶';
  font-family: sans-serif !important;
}

text.fa-save:empty::before {
  content: '💾';
  font-family: sans-serif !important;
}

text.fa-paperclip:empty::before {
  content: '📎';
  font-family: sans-serif !important;
}

text.fa-building:empty::before {
  content: '🏢';
  font-family: sans-serif !important;
}

text.fa-microphone:empty::before {
  content: '🎤';
  font-family: sans-serif !important;
}

text.fa-film:empty::before {
  content: '🎬';
  font-family: sans-serif !important;
}

text.fa-glass-cheers:empty::before {
  content: '🥂';
  font-family: sans-serif !important;
}

text.fa-bookmark:empty::before {
  content: '🔖';
  font-family: sans-serif !important;
}

text.fa-bullhorn:empty::before {
  content: '📢';
  font-family: sans-serif !important;
}

text.fa-redo:empty::before {
  content: '↻';
  font-family: sans-serif !important;
}

text.fa-lightbulb:empty::before {
  content: '💡';
  font-family: sans-serif !important;
}

text.fa-info-circle:empty::before {
  content: 'ⓘ';
  font-family: sans-serif !important;
}

text.fa-question-circle:empty::before {
  content: '❓';
  font-family: sans-serif !important;
}

text.fa-chalkboard-teacher:empty::before {
  content: '👨‍🏫';
  font-family: sans-serif !important;
}

text.fa-university:empty::before {
  content: '🏛';
  font-family: sans-serif !important;
}

text.fa-plug:empty::before {
  content: '🔌';
  font-family: sans-serif !important;
}

text.fa-sort-alpha-down:empty::before {
  content: '🔤';
  font-family: sans-serif !important;
}

text.fa-broom:empty::before {
  content: '🧹';
  font-family: sans-serif !important;
}

text.fa-clipboard:empty::before {
  content: '📋';
  font-family: sans-serif !important;
}

text.fa-globe:empty::before {
  content: '🌐';
  font-family: sans-serif !important;
}

text.fa-moon:empty::before {
  content: '🌙';
  font-family: sans-serif !important;
}

text.fa-text-height:empty::before {
  content: 'T';
  font-family: sans-serif !important;
}
</style>
