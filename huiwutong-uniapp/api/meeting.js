/**
 * 会议相关API
 */

import { get, post, put, del } from './request'

export default {
  /**
   * 获取会议列表
   */
  getList(params) {
    return get('/meeting/list', params)
  },

  /**
   * 获取会议详情
   */
  getDetail(id) {
    return get(`/meeting/${id}`)
  },

  /**
   * 创建会议
   */
  create(data) {
    return post('/meeting', data)
  },

  /**
   * 更新会议
   */
  update(id, data) {
    return put(`/meeting/${id}`, data)
  },

  /**
   * 删除会议
   */
  delete(id) {
    return del(`/meeting/${id}`)
  },

  /**
   * 报名会议
   */
  register(id) {
    return post(`/meeting/${id}/register`)
  },

  /**
   * 取消报名
   */
  cancelRegister(id) {
    return post(`/meeting/${id}/cancel-register`)
  },

  /**
   * 获取会议日程
   */
  getSchedule(id) {
    return get(`/meeting/${id}/schedule`)
  },

  /**
   * 签到
   */
  checkin(id, data) {
    return post(`/meeting/${id}/checkin`, data)
  },

  /**
   * 获取座位信息
   */
  getSeats(id) {
    return get(`/meeting/${id}/seats`)
  },

  /**
   * 获取我的座位
   */
  getMySeat(id) {
    return get(`/meeting/${id}/my-seat`)
  }
}
