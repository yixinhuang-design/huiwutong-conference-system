/**
 * Pinia Store 入口文件
 */

import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

// 导出各个模块
export { useUserStore } from './modules/user'
export { useAppStore } from './modules/app'
