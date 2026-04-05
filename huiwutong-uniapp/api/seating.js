/**
 * 座位管理API
 * 对接座位服务 (端口8085)
 */

import { get, post, put, del } from './request'

export default {
  // ==================== 会场 ====================

  /**
   * 获取会场列表
   * GET /api/seating/venues/{conferenceId}
   */
  getVenues(conferenceId) {
    return get(`/seating/venues/${conferenceId}`)
  },

  /**
   * 获取会场详情
   * GET /api/seating/venues/detail/{venueId}
   */
  getVenueDetail(venueId) {
    return get(`/seating/venues/detail/${venueId}`)
  },

  /**
   * 创建会场
   * POST /api/seating/venues
   */
  createVenue(data) {
    return post('/seating/venues', data)
  },

  /**
   * 更新会场
   * PUT /api/seating/venues/{venueId}
   */
  updateVenue(venueId, data) {
    return put(`/seating/venues/${venueId}`, data)
  },

  /**
   * 删除会场
   * DELETE /api/seating/venues/{venueId}
   */
  deleteVenue(venueId) {
    return del(`/seating/venues/${venueId}`)
  },

  /**
   * 获取会场座位统计
   * GET /api/seating/venues/stats/{venueId}
   */
  getVenueStats(venueId) {
    return get(`/seating/venues/stats/${venueId}`)
  },

  // ==================== 座位 ====================

  /**
   * 获取座位列表
   * GET /api/seating/seats
   */
  getSeats(venueId) {
    return get('/seating/seats', { venueId })
  },

  /**
   * 分配座位
   * POST /api/seating/seats/assign
   */
  assignSeat(data) {
    return post('/seating/seats/assign', data)
  },

  /**
   * 交换座位
   * POST /api/seating/seats/swap
   */
  swapSeats(data) {
    return post('/seating/seats/swap', data)
  },

  /**
   * 获取座位统计
   * GET /api/seating/seats/stats/{venueId}
   */
  getSeatStats(venueId) {
    return get(`/seating/seats/stats/${venueId}`)
  },

  // ==================== 布局 ====================

  /**
   * 保存座位布局
   * POST /api/seating/layout/save
   */
  saveLayout(data) {
    return post('/seating/layout/save', data)
  },

  /**
   * 加载座位布局
   * GET /api/seating/layout/load
   */
  loadLayout(conferenceId, scheduleId) {
    return get('/seating/layout/load', {
      conferenceId,
      scheduleId
    })
  },

  // ==================== 用餐 ====================

  /**
   * 获取用餐列表
   * GET /api/seating/dinings/{conferenceId}
   */
  getDinings(conferenceId) {
    return get(`/seating/dinings/${conferenceId}`)
  },

  /**
   * 创建用餐
   * POST /api/seating/dinings
   */
  createDining(data) {
    return post('/seating/dinings', data)
  },

  // ==================== 住宿 ====================

  /**
   * 获取住宿列表
   * GET /api/seating/accommodations/{conferenceId}
   */
  getAccommodations(conferenceId) {
    return get(`/seating/accommodations/${conferenceId}`)
  },

  /**
   * 创建住宿
   * POST /api/seating/accommodations
   */
  createAccommodation(data) {
    return post('/seating/accommodations', data)
  },

  // ==================== 车辆 ====================

  /**
   * 获取车辆列表
   * GET /api/seating/transports/{conferenceId}
   */
  getTransports(conferenceId) {
    return get(`/seating/transports/${conferenceId}`)
  },

  /**
   * 创建车辆
   * POST /api/seating/transports
   */
  createTransport(data) {
    return post('/seating/transports', data)
  }
}
