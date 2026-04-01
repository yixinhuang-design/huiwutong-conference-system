/**
 * 通知管理API
 * 对接通知服务 (端口8083)
 */

import { get, post, put, del } from './request'

// 通知服务基础地址（通过Node代理/网关转发，不直连后端端口）
const NOTI_BASE = { baseUrl: '' }

export default {
  // ============ 通知发送 ============
  /** 发送通知 */
  send(data) {
    return post('/api/notification/send', data, NOTI_BASE)
  },
  /** 保存草稿 */
  saveDraft(data) {
    return post('/api/notification/draft', data, NOTI_BASE)
  },
  /** 发送催报 */
  sendUrge(data) {
    return post('/api/notification/urge', data, NOTI_BASE)
  },

  // ============ 通知列表/详情 ============
  /** 通知列表(分页) */
  list(params) {
    return get('/api/notification/list', params, NOTI_BASE)
  },
  /** 通知详情 */
  detail(id) {
    return get(`/api/notification/${id}`, {}, NOTI_BASE)
  },
  /** 删除通知 */
  remove(id) {
    return del(`/api/notification/${id}`, {}, NOTI_BASE)
  },
  /** 发送统计 */
  getStats(conferenceId) {
    return get('/api/notification/stats', { conferenceId }, NOTI_BASE)
  },

  // ============ 已读跟踪 ============
  /** 标记单条通知已读 */
  markRead(id, userId) {
    return put(`/api/notification/${id}/read`, {}, { ...NOTI_BASE, params: { userId } })
  },
  /** 标记会议下所有通知已读 */
  markAllRead(conferenceId, userId) {
    return put('/api/notification/read-all', { conferenceId, userId }, NOTI_BASE)
  },

  // ============ 模板管理 ============
  /** 模板列表 */
  templateList(conferenceId) {
    return get('/api/notification/template/list', { conferenceId }, NOTI_BASE)
  },
  /** 模板详情 */
  templateDetail(id) {
    return get(`/api/notification/template/${id}`, {}, NOTI_BASE)
  },
  /** 保存模板 */
  templateSave(data) {
    return post('/api/notification/template/save', data, NOTI_BASE)
  },
  /** 删除模板 */
  templateDelete(id) {
    return del(`/api/notification/template/${id}`, {}, NOTI_BASE)
  }
}
