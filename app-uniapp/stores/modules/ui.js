import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // State
  const theme = ref('light') // light, dark
  const language = ref('zh-CN') // zh-CN, en
  const sidebarVisible = ref(true)
  const notificationVisible = ref(false)
  const loadingCount = ref(0)
  const toastMessage = ref('')
  const toastType = ref('info') // info, success, warning, error

  // Actions
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    uni.setStorageSync('app_theme', theme.value)
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
    uni.setStorageSync('app_theme', theme.value)
  }

  const setLanguage = (newLanguage) => {
    language.value = newLanguage
    uni.setStorageSync('app_language', language.value)
  }

  const toggleSidebar = () => {
    sidebarVisible.value = !sidebarVisible.value
  }

  const setSidebarVisible = (visible) => {
    sidebarVisible.value = visible
  }

  const setNotificationVisible = (visible) => {
    notificationVisible.value = visible
  }

  const showLoading = () => {
    loadingCount.value++
    if (loadingCount.value === 1) {
      uni.showLoading({
        title: '加载中...',
        mask: true,
      })
    }
  }

  const hideLoading = () => {
    loadingCount.value = Math.max(0, loadingCount.value - 1)
    if (loadingCount.value === 0) {
      uni.hideLoading()
    }
  }

  const showToast = (message, type = 'info') => {
    toastMessage.value = message
    toastType.value = type

    uni.showToast({
      title: message,
      icon: type === 'success' ? 'success' : type === 'error' ? 'error' : 'none',
      duration: 2000,
    })
  }

  const restoreFromStorage = () => {
    const storedTheme = uni.getStorageSync('app_theme')
    const storedLanguage = uni.getStorageSync('app_language')

    if (storedTheme) {
      theme.value = storedTheme
    }

    if (storedLanguage) {
      language.value = storedLanguage
    }
  }

  return {
    // State
    theme,
    language,
    sidebarVisible,
    notificationVisible,
    loadingCount,
    toastMessage,
    toastType,

    // Actions
    toggleTheme,
    setTheme,
    setLanguage,
    toggleSidebar,
    setSidebarVisible,
    setNotificationVisible,
    showLoading,
    hideLoading,
    showToast,
    restoreFromStorage,
  }
})
