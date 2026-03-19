/**
 * 任务分派API
 * 对接协同服务 (端口8089)
 */

import { get, post, put, del } from './request'

// 协同服务基础地址
const COLLAB_BASE = { baseUrl: 'http://localhost:8089' }

export default {
  // ============ 任务CRUD ============
  /** 创建任务 */
  createTask(data) {
    return post('/api/task/create', data, COLLAB_BASE)
  },
  /** 更新任务 */
  updateTask(taskId, data) {
    return put(`/api/task/${taskId}`, data, COLLAB_BASE)
  },
  /** 任务详情 */
  getTaskDetail(taskId) {
    return get(`/api/task/${taskId}`, {}, COLLAB_BASE)
  },
  /** 任务列表 */
  listTasks(params) {
    return get('/api/task/list', params, COLLAB_BASE)
  },
  /** 我的任务 */
  myTasks(params) {
    return get('/api/task/my-tasks', params, COLLAB_BASE)
  },

  // ============ 执行人管理 ============
  /** 分配执行人 */
  assignTask(taskId, assignees) {
    return post(`/api/task/${taskId}/assign`, assignees, COLLAB_BASE)
  },
  /** 获取执行人 */
  getAssignees(taskId) {
    return get(`/api/task/${taskId}/assignees`, {}, COLLAB_BASE)
  },

  // ============ 任务执行 ============
  /** 提交任务 */
  submitTask(taskId, data) {
    return post(`/api/task/${taskId}/submit`, data, COLLAB_BASE)
  },
  /** 完成任务 */
  completeTask(taskId, data) {
    return post(`/api/task/${taskId}/complete`, data, COLLAB_BASE)
  },
  /** 取消任务 */
  cancelTask(taskId, data) {
    return post(`/api/task/${taskId}/cancel`, data, COLLAB_BASE)
  },
  /** 催办任务 */
  urgeTask(taskId, data) {
    return post(`/api/task/${taskId}/urge`, data, COLLAB_BASE)
  },

  // ============ 日志 & 统计 ============
  /** 任务日志 */
  getTaskLogs(taskId) {
    return get(`/api/task/${taskId}/logs`, {}, COLLAB_BASE)
  },
  /** 任务统计 */
  taskStats(params) {
    return get('/api/task/stats', params, COLLAB_BASE)
  }
}
