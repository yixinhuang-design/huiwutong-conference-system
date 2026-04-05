/**
 * 通知管理API
 * 对接通知服务 (端口8083)
 */

import { get, post, put, del } from './request'

export default {
  // ============ 通知发送 ============
  /** 发送通知 */
  send(data) {
    return post('/notification/send', data)
  },
  /** 保存草稿 */
  saveDraft(data) {
    return post('/notification/draft', data)
  },
  /** 发送催报 */
  sendUrge(data) {
    return post('/notification/urge', data)
  },

  // ============ 通知列表/详情 ============
  /** 通知列表(分页) */
  list(params) {
    return get('/notification/list', params)
  },
  /** 通知详情 */
  detail(id) {
    return get(`/notification/${id}`)
  },
  /** 删除通知 */
  remove(id) {
    return del(`/notification/${id}`)
  },
  /** 发送统计 */
  getStats(conferenceId) {
    return get('/notification/stats', { conferenceId })
  },

  // ============ 已读跟踪 ============
  /** 标记单条通知已读 */
  markRead(id, userId) {
    return put(`/notification/${id}/read`, {}, { params: { userId } })
  },
  /** 标记会议下所有通知已读 */
  markAllRead(conferenceId, userId) {
    return put('/notification/read-all', { conferenceId, userId })
  },

  // ============ 模板管理 ============
  /** 模板列表 */
  templateList(conferenceId) {
    return get('/notification/template/list', { conferenceId })
  },
  /** 模板详情 */
  templateDetail(id) {
    return get(`/notification/template/${id}`)
  },
  /** 保存模板 */
  templateSave(data) {
    return post('/notification/template/save', data)
  },
  /** 删除模板 */
  templateDelete(id) {
    return del(`/notification/template/${id}`)
  }
}
