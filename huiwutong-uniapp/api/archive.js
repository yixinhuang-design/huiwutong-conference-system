/**
 * 归档服务API
 * 对接会议服务 (端口8084)
 * 19个REST端点 - 核心归档模块
 */

import { get, post, put, del } from './request'

export default {
  // ============ 归档统计与同步 ============
  /**
   * 归档统计概览
   * GET /api/meeting/{meetingId}/archive/stats
   */
  getStats(meetingId) {
    return get(`/meeting/${meetingId}/archive/stats`)
  },

  /**
   * 从源数据同步
   * POST /api/meeting/{meetingId}/archive/sync
   */
  sync(meetingId) {
    return post(`/meeting/${meetingId}/archive/sync`)
  },

  // ============ 课件资料管理 ============
  /**
   * 课件列表
   * GET /api/meeting/{meetingId}/archive/courseware
   */
  listCourseware(meetingId, params) {
    return get(`/meeting/${meetingId}/archive/courseware`, params)
  },

  /**
   * 添加课件
   * POST /api/meeting/{meetingId}/archive/courseware
   */
  addCourseware(meetingId, data) {
    return post(`/meeting/${meetingId}/archive/courseware`, data)
  },

  /**
   * 删除课件
   * DELETE /api/meeting/{meetingId}/archive/courseware/{id}
   */
  deleteCourseware(meetingId, id) {
    return del(`/meeting/${meetingId}/archive/courseware/${id}`)
  },

  // ============ 互动数据管理 ============
  /**
   * 互动列表
   * GET /api/meeting/{meetingId}/archive/interactions
   */
  listInteractions(meetingId, params) {
    return get(`/meeting/${meetingId}/archive/interactions`, params)
  },

  /**
   * 添加互动记录
   * POST /api/meeting/{meetingId}/archive/interactions
   */
  addInteraction(meetingId, data) {
    return post(`/meeting/${meetingId}/archive/interactions`, data)
  },

  /**
   * 删除互动记录
   * DELETE /api/meeting/{meetingId}/archive/interactions/{id}
   */
  deleteInteraction(meetingId, id) {
    return del(`/meeting/${meetingId}/archive/interactions/${id}`)
  },

  // ============ 业务数据管理 ============
  /**
   * 业务数据列表
   * GET /api/meeting/{meetingId}/archive/business-data
   */
  listBusinessData(meetingId, params) {
    return get(`/meeting/${meetingId}/archive/business-data`, params)
  },

  /**
   * 添加业务数据
   * POST /api/meeting/{meetingId}/archive/business-data
   */
  addBusinessData(meetingId, data) {
    return post(`/meeting/${meetingId}/archive/business-data`, data)
  },

  /**
   * 更新业务数据
   * PUT /api/meeting/{meetingId}/archive/business-data/{id}
   */
  updateBusinessData(meetingId, id, data) {
    return put(`/meeting/${meetingId}/archive/business-data/${id}`, data)
  },

  /**
   * 删除业务数据
   * DELETE /api/meeting/{meetingId}/archive/business-data/{id}
   */
  deleteBusinessData(meetingId, id) {
    return del(`/meeting/${meetingId}/archive/business-data/${id}`)
  },

  // ============ 消息群组管理 ============
  /**
   * 消息群组列表
   * GET /api/meeting/{meetingId}/archive/message-groups
   */
  listMessageGroups(meetingId, params) {
    return get(`/meeting/${meetingId}/archive/message-groups`, params)
  },

  /**
   * 添加消息群组
   * POST /api/meeting/{meetingId}/archive/message-groups
   */
  addMessageGroup(meetingId, data) {
    return post(`/meeting/${meetingId}/archive/message-groups`, data)
  },

  /**
   * 删除消息群组
   * DELETE /api/meeting/{meetingId}/archive/message-groups/{id}
   */
  deleteMessageGroup(meetingId, id) {
    return del(`/meeting/${meetingId}/archive/message-groups/${id}`)
  },

  // ============ 消息管理 ============
  /**
   * 群组消息列表
   * GET /api/meeting/{meetingId}/archive/messages
   */
  listMessages(meetingId, params) {
    return get(`/meeting/${meetingId}/archive/messages`, params)
  },

  /**
   * 添加消息
   * POST /api/meeting/{meetingId}/archive/messages
   */
  addMessage(meetingId, data) {
    return post(`/meeting/${meetingId}/archive/messages`, data)
  },

  /**
   * 删除消息
   * DELETE /api/meeting/{meetingId}/archive/messages/{id}
   */
  deleteMessage(meetingId, id) {
    return del(`/meeting/${meetingId}/archive/messages/${id}`)
  },

  // ============ 归档打包与导出 ============
  /**
   * 打包归档数据
   * POST /api/meeting/{meetingId}/archive/pack
   */
  pack(meetingId) {
    return post(`/meeting/${meetingId}/archive/pack`)
  },

  /**
   * 导出归档数据
   * GET /api/meeting/{meetingId}/archive/export
   */
  export(meetingId) {
    return get(`/meeting/${meetingId}/archive/export`)
  },

  /**
   * 更新打包设置
   * PUT /api/meeting/{meetingId}/archive/settings
   */
  updateSettings(meetingId, data) {
    return put(`/meeting/${meetingId}/archive/settings`, data)
  }
}
