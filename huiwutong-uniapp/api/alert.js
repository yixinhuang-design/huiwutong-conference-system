/**
 * 预警管理API
 * 对接通知服务 (端口8083)
 */

import { get, post, put, del } from './request'

// 通知服务基础地址
const NOTI_BASE = { baseUrl: 'http://localhost:8083' }

export default {
  // ============ 预警规则 ============
  /** 规则列表 */
  ruleList(conferenceId) {
    return get('/api/alert/rule/list', { conferenceId }, NOTI_BASE)
  },
  /** 保存规则 */
  ruleSave(data) {
    return post('/api/alert/rule/save', data, NOTI_BASE)
  },
  /** 更新规则 */
  ruleUpdate(id, data) {
    return put(`/api/alert/rule/${id}`, data, NOTI_BASE)
  },
  /** 删除规则 */
  ruleDelete(id) {
    return del(`/api/alert/rule/${id}`, {}, NOTI_BASE)
  },
  /** 启用/禁用规则 */
  ruleToggle(id) {
    return put(`/api/alert/rule/${id}/toggle`, {}, NOTI_BASE)
  },

  // ============ 预警事件 ============
  /** 实时指标 */
  getMetrics(conferenceId) {
    return get('/api/alert/metrics', { conferenceId }, NOTI_BASE)
  },
  /** 预警事件列表 */
  list(params) {
    return get('/api/alert/list', params, NOTI_BASE)
  },
  /** 开始处理预警 */
  process(id) {
    return put(`/api/alert/${id}/process`, {}, NOTI_BASE)
  },
  /** 解决预警 */
  resolve(id, data) {
    return put(`/api/alert/${id}/resolve`, data, NOTI_BASE)
  },
  /** 预警统计 */
  getStats(conferenceId) {
    return get('/api/alert/stats', { conferenceId }, NOTI_BASE)
  }
}
