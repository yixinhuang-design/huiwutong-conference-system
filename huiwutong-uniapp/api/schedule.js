/**
 * 日程服务API
 * 对接会议服务 (端口8084)
 */

import { get, post, put, del } from './request'

export default {
  // ============ 日程CRUD ============
  /**
   * 创建日程
   * POST /api/schedule/create
   */
  create(data) {
    return post('/schedule/create', data)
  },

  /**
   * 更新日程
   * PUT /api/schedule/{id}
   */
  update(id, data) {
    return put(`/schedule/${id}`, data)
  },

  /**
   * 删除日程
   * DELETE /api/schedule/{id}
   */
  delete(id) {
    return del(`/schedule/${id}`)
  },

  /**
   * 日程详情
   * GET /api/schedule/{id}
   */
  getDetail(id) {
    return get(`/schedule/${id}`)
  },

  /**
   * 会议的所有日程
   * GET /api/schedule/list?meetingId={meetingId}
   * GET /api/schedule/all?meetingId={meetingId}
   */
  list(meetingId) {
    return get('/schedule/list', { meetingId })
  },

  /**
   * 获取所有日程（备用接口）
   * GET /api/schedule/all?meetingId={meetingId}
   */
  listAll(meetingId) {
    return get('/schedule/all', { meetingId })
  },

  // ============ 日程查询与过滤 ============
  /**
   * 需签到的日程
   * GET /api/schedule/need-checkin?meetingId={id}
   */
  getNeedCheckin(meetingId) {
    return get('/schedule/need-checkin', { meetingId: meetingId })
  },

  /**
   * 需提醒的日程
   * GET /api/schedule/need-reminder?meetingId={id}
   */
  getNeedReminder(meetingId) {
    return get('/schedule/need-reminder', { meetingId: meetingId })
  },

  /**
   * 进行中的日程
   * GET /api/schedule/ongoing?meetingId={id}
   */
  getOngoing(meetingId) {
    return get('/schedule/ongoing', { meetingId: meetingId })
  },

  /**
   * 即将开始的日程
   * GET /api/schedule/upcoming?meetingId={id}
   */
  getUpcoming(meetingId) {
    return get('/schedule/upcoming', { meetingId: meetingId })
  },

  /**
   * 按时间范围查询日程
   * GET /api/schedule/by-time-range?meetingId={id}&startTime=xxx&endTime=xxx
   */
  getByTimeRange(meetingId, startTime, endTime) {
    return get('/schedule/by-time-range', {
      meetingId: meetingId,
      startTime,
      endTime
    })
  },

  /**
   * 下一个日程
   * GET /api/schedule/next?meetingId={id}
   */
  getNext(meetingId) {
    return get('/schedule/next', { meetingId: meetingId })
  },

  /**
   * 当前日程
   * GET /api/schedule/current?meetingId={id}
   */
  getCurrent(meetingId) {
    return get('/schedule/current', { meetingId: meetingId })
  },

  /**
   * 日程总数
   * GET /api/schedule/count?meetingId={id}
   */
  getCount(meetingId) {
    return get('/schedule/count', { meetingId: meetingId })
  },

  // ============ 日程操作 ============
  /**
   * 发布日程
   * PUT /api/schedule/{id}/publish
   */
  publish(id) {
    return put(`/schedule/${id}/publish`)
  },

  /**
   * 取消日程
   * PUT /api/schedule/{id}/cancel
   */
  cancel(id) {
    return put(`/schedule/${id}/cancel`)
  },

  /**
   * 复制日程
   * POST /api/schedule/{id}/duplicate
   */
  duplicate(id) {
    return post(`/schedule/${id}/duplicate`)
  }
}
